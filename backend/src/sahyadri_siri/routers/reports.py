from __future__ import annotations

import asyncio
import os
import time
from uuid import UUID, uuid4

from fastapi import APIRouter, Depends, HTTPException, Query, Request, status
from sqlalchemy import select
from sqlalchemy.orm import selectinload
from sqlalchemy.ext.asyncio import AsyncSession

from sahyadri_siri.dependencies import get_current_user, get_session
from sahyadri_siri.db import Advisory, Report, User
from sahyadri_siri.schemas import AdvisoryRead, ReportCreate, ReportRead, ReportWithUser, UserPublic
from sahyadri_siri.services.cache import cache_delete, cache_delete_prefix, cache_get_json, cache_set_json
from sahyadri_siri.services.gemini import generate_advisory, to_advisory_create
from sahyadri_siri.services.realtime import broadcast_advisory

router = APIRouter(prefix="/api", tags=["reports"])
MAP_CACHE_KEY = "map-data:list:v1"


@router.post("/report", response_model=ReportRead, status_code=status.HTTP_201_CREATED)
async def create_report(
    payload: ReportCreate,
    request: Request,
    session: AsyncSession = Depends(get_session),
    user: User = Depends(get_current_user),
):
    settings = request.app.state.settings
    wqi_model = request.app.state.wqi_model
    report_id = str(payload.id or uuid4())
    wqi_score = float(wqi_model.predict(payload.clarity, payload.smell, payload.flow))
    timestamp = payload.timestamp or int(time.time() * 1000)

    report = Report(
        id=report_id,
        user_id=user.id,
        clarity=payload.clarity,
        smell=payload.smell,
        flow=payload.flow,
        latitude=payload.latitude,
        longitude=payload.longitude,
        image_path=payload.image_path,
        timestamp=timestamp,
        status="SYNCED",
        wqi_score=wqi_score,
    )
    session.add(report)
    await session.commit()
    await session.refresh(report)

    await cache_delete_prefix(request.app.state.redis, MAP_CACHE_KEY)
    if os.getenv("SKIP_REPORT_FOLLOWUP", "false").lower() != "true":
        _schedule_report_followup(request.app, payload, report_id, wqi_score)

    return ReportRead(
        id=report.id,
        clarity=report.clarity,
        smell=report.smell,
        flow=report.flow,
        latitude=report.latitude,
        longitude=report.longitude,
        imagePath=report.image_path,
        timestamp=report.timestamp,
        status=report.status,
        wqiScore=report.wqi_score,
        userId=report.user_id,
        advisoryId=report.advisory_id,
    )


@router.get("/map-data", response_model=list[ReportWithUser])
async def map_data(
    request: Request,
    session: AsyncSession = Depends(get_session),
    min_lat: float | None = Query(default=None, alias="minLat"),
    max_lat: float | None = Query(default=None, alias="maxLat"),
    min_lon: float | None = Query(default=None, alias="minLon"),
    max_lon: float | None = Query(default=None, alias="maxLon"),
    limit: int = Query(default=200, ge=1, le=1000),
    offset: int = Query(default=0, ge=0),
):
    settings = request.app.state.settings
    limit = min(limit, settings.map_max_limit)

    if any(value is not None for value in (min_lat, max_lat, min_lon, max_lon)):
        if None in (min_lat, max_lat, min_lon, max_lon):
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="All bounding-box parameters are required")

    cache_key = f"{MAP_CACHE_KEY}:{min_lat}:{max_lat}:{min_lon}:{max_lon}:{limit}:{offset}"
    redis = request.app.state.redis
    cached = await cache_get_json(redis, cache_key)
    if cached is not None:
        return cached

    query = select(Report).options(selectinload(Report.user), selectinload(Report.advisory))
    if min_lat is not None:
        query = query.where(Report.latitude >= min_lat, Report.latitude <= max_lat)
    if min_lon is not None:
        query = query.where(Report.longitude >= min_lon, Report.longitude <= max_lon)
    query = query.order_by(Report.timestamp.desc()).limit(limit).offset(offset)

    result = await session.execute(query)
    payload = [
        ReportWithUser(
            id=item.id,
            clarity=item.clarity,
            smell=item.smell,
            flow=item.flow,
            latitude=item.latitude,
            longitude=item.longitude,
            imagePath=item.image_path,
            timestamp=item.timestamp,
            status=item.status,
            wqiScore=item.wqi_score,
            user=UserPublic(id=item.user.id, email=item.user.email, displayName=item.user.display_name),
            advisory=
                AdvisoryRead(
                    id=item.advisory.id,
                    title=item.advisory.title,
                    description=item.advisory.description,
                    status=item.advisory.status,
                    timestamp=item.advisory.timestamp,
                )
                if item.advisory is not None
                else None,
        ).model_dump(by_alias=True)
        for item in result.scalars().all()
    ]
    await cache_set_json(redis, cache_key, request.app.state.settings.map_cache_ttl_seconds, payload)
    return payload


@router.get("/history", response_model=list[ReportWithUser])
async def history(
    request: Request,
    user_id: str | None = None,
    session: AsyncSession = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    query = select(Report).options(selectinload(Report.user), selectinload(Report.advisory)).order_by(Report.timestamp.desc())
    if user_id == "all":
        admin_email = request.app.state.settings.admin_email
        if admin_email and current_user.email != admin_email:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Admin access required")
    else:
        query = query.where(Report.user_id == current_user.id)

    result = await session.execute(query)
    return [
        ReportWithUser(
            id=item.id,
            clarity=item.clarity,
            smell=item.smell,
            flow=item.flow,
            latitude=item.latitude,
            longitude=item.longitude,
            imagePath=item.image_path,
            timestamp=item.timestamp,
            status=item.status,
            wqiScore=item.wqi_score,
            user=UserPublic(id=item.user.id, email=item.user.email, displayName=item.user.display_name),
            advisory=
                AdvisoryRead(
                    id=item.advisory.id,
                    title=item.advisory.title,
                    description=item.advisory.description,
                    status=item.advisory.status,
                    timestamp=item.advisory.timestamp,
                )
                if item.advisory is not None
                else None,
        ).model_dump(by_alias=True)
        for item in result.scalars().all()
    ]


def _schedule_report_followup(app, payload: ReportCreate, report_id: str, wqi_score: float) -> None:
    task = asyncio.create_task(_process_report_followup(app, payload, report_id, wqi_score))
    app.state.background_tasks.add(task)
    task.add_done_callback(app.state.background_tasks.discard)


async def _process_report_followup(app, payload: ReportCreate, report_id: str, wqi_score: float) -> None:
    settings = app.state.settings
    session_maker = app.state.session_maker
    redis = app.state.redis
    advisory_result = await generate_advisory(payload, wqi_score, settings)
    advisory_data = to_advisory_create(advisory_result, payload.timestamp or int(time.time() * 1000))

    async with session_maker() as session:
        advisory = Advisory(
            title=advisory_data.title,
            description=advisory_data.description,
            status=advisory_data.status,
            timestamp=advisory_data.timestamp,
            report_id=report_id,
        )
        session.add(advisory)
        await session.commit()
        await session.refresh(advisory)

        report = await session.get(Report, report_id)
        if report is not None:
            report.advisory_id = advisory.id
            await session.commit()

    await cache_delete(redis, "alerts:list:v1")
    if advisory.status != "Safe":
        await broadcast_advisory(redis, {"id": advisory.id, "title": advisory.title, "description": advisory.description, "status": advisory.status, "timestamp": advisory.timestamp})
