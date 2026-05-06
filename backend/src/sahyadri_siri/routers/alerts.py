from __future__ import annotations

from fastapi import APIRouter, Depends, Request
from sqlalchemy import select
from sqlalchemy.orm import selectinload
from sqlalchemy.ext.asyncio import AsyncSession

from sahyadri_siri.dependencies import get_session
from sahyadri_siri.db import Advisory, Report, User
from sahyadri_siri.schemas import AlertRead
from sahyadri_siri.services.cache import cache_get_json, cache_set_json

router = APIRouter(prefix="/api", tags=["alerts"])
CACHE_KEY = "alerts:list:v1"


@router.get("/alerts", response_model=list[AlertRead])
async def alerts(request: Request, session: AsyncSession = Depends(get_session)):
    redis = request.app.state.redis
    cached = await cache_get_json(redis, CACHE_KEY)
    if cached is not None:
        return cached

    result = await session.execute(select(Advisory).order_by(Advisory.timestamp.desc()))
    advisories = [
        AlertRead(id=item.id, title=item.title, description=item.description, status=item.status, timestamp=item.timestamp).model_dump(by_alias=True)
        for item in result.scalars().all()
    ]
    await cache_set_json(redis, CACHE_KEY, request.app.state.settings.alerts_cache_ttl_seconds, advisories)
    return advisories
