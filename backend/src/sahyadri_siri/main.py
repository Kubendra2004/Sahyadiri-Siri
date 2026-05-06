from __future__ import annotations

import asyncio
import time
from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from starlette.middleware.trustedhost import TrustedHostMiddleware
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker

from sahyadri_siri import __version__
from sahyadri_siri.core.config import Settings, get_settings
from sahyadri_siri.db import build_engine, init_db
from sahyadri_siri.middleware.logging import JsonRequestLoggingMiddleware
from sahyadri_siri.middleware.rate_limit import RateLimitMiddleware
from sahyadri_siri.middleware.security import SecurityHeadersMiddleware
from sahyadri_siri.routers import alerts_router, auth_router, health_router, reports_router, upload_router, ws_router
from sahyadri_siri.services.cache import build_redis_client
from sahyadri_siri.services.realtime import WebSocketManager, run_alert_listener
from sahyadri_siri.services.storage import build_storage_backend
from sahyadri_siri.services.wqi import load_wqi_model
from sqlalchemy.ext.asyncio import create_async_engine


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings: Settings = app.state.settings
    app.state.started_at = time.monotonic()
    app.state.background_tasks = set()
    app.state.ws_manager = WebSocketManager()
    app.state.stop_event = asyncio.Event()
    app.state.engine, app.state.using_sqlite_fallback = await build_engine(
        settings.database_url,
        Path(__file__).resolve().parents[2] / "dev.db",
    )
    app.state.session_maker = async_sessionmaker(app.state.engine, expire_on_commit=False)
    app.state.redis = await build_redis_client(settings.redis_url)
    app.state.wqi_model = load_wqi_model(settings.model_path)
    app.state.storage_backend = await build_storage_backend(settings)
    if settings.auto_create_schema or app.state.using_sqlite_fallback:
        await init_db(app.state.engine)

    listener_task = asyncio.create_task(run_alert_listener(app.state.redis, app.state.ws_manager, settings, app.state.stop_event))
    app.state.listener_task = listener_task
    try:
        yield
    finally:
        app.state.stop_event.set()
        listener_task.cancel()
        for task in list(app.state.background_tasks):
            task.cancel()
        await app.state.engine.dispose()
        close = getattr(app.state.redis, "close", None)
        if close is not None:
            result = close()
            if asyncio.iscoroutine(result):
                await result


def create_app(settings: Settings | None = None, enable_lifespan: bool = True) -> FastAPI:
    app_settings = settings or get_settings()
    app = FastAPI(
        title=app_settings.app_name,
        version=app_settings.app_version,
        lifespan=lifespan if enable_lifespan else None,
        docs_url=app_settings.docs_url,
        redoc_url=app_settings.redoc_url,
        openapi_url=app_settings.openapi_url,
    )
    app.state.settings = app_settings
    app.add_middleware(JsonRequestLoggingMiddleware)
    app.add_middleware(RateLimitMiddleware)
    app.add_middleware(SecurityHeadersMiddleware)
    trusted_hosts = app_settings.trusted_host_list
    if trusted_hosts != ["*"]:
        app.add_middleware(TrustedHostMiddleware, allowed_hosts=trusted_hosts)
    app.add_middleware(
        CORSMiddleware,
        allow_origins=app_settings.cors_origin_list,
        allow_credentials=app_settings.cors_origin_list != ["*"],
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(auth_router)
    app.include_router(reports_router)
    app.include_router(upload_router)
    app.include_router(alerts_router)
    app.include_router(ws_router)
    app.include_router(health_router)
    uploads_dir = Path.cwd() / "uploads"
    uploads_dir.mkdir(parents=True, exist_ok=True)
    app.mount("/uploads", StaticFiles(directory=uploads_dir), name="uploads")
    return app


app = create_app()
