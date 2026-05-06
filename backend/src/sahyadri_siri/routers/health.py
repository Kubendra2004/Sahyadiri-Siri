from __future__ import annotations

import time

from fastapi import APIRouter, Request
from sqlalchemy import text

from sahyadri_siri.schemas import HealthRead
from sahyadri_siri.services.cache import safe_redis_ping

router = APIRouter(prefix="/api", tags=["health"])


@router.get("/health", response_model=HealthRead)
async def health(request: Request) -> HealthRead:
    settings = request.app.state.settings
    db_connected = False
    redis_connected = False

    try:
        async with request.app.state.engine.connect() as connection:
            await connection.execute(text("SELECT 1"))
        db_connected = True
    except Exception:
        db_connected = False

    redis_connected = await safe_redis_ping(getattr(request.app.state, "redis", None))
    uptime_s = time.monotonic() - request.app.state.started_at
    return HealthRead(
        status="ok",
        version=settings.app_version,
        dbConnected=db_connected,
        redisConnected=redis_connected,
        uptimeS=uptime_s,
    )
