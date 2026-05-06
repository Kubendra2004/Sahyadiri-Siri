from __future__ import annotations

import asyncio
import time
from pathlib import Path
from uuid import uuid4

import pytest
import pytest_asyncio
from httpx import ASGITransport, AsyncClient
from sqlalchemy.ext.asyncio import async_sessionmaker, create_async_engine

from sahyadri_siri.core.config import Settings
from sahyadri_siri.db import init_db
from sahyadri_siri.main import create_app
from sahyadri_siri.services.cache import MemoryRedis
from sahyadri_siri.services.realtime import WebSocketManager
from sahyadri_siri.services.storage import FallbackStorageBackend
from sahyadri_siri.services.wqi import train_model


@pytest_asyncio.fixture
async def app(tmp_path: Path):
    database_path = tmp_path / "test.db"
    settings = Settings(
        DATABASE_URL=f"sqlite+aiosqlite:///{database_path}",
        REDIS_URL="memory://",
        JWT_SECRET="test-secret",
        GEMINI_API_KEY=None,
        STORAGE_PROVIDER="local",
        FIREBASE_CREDENTIALS_PATH=None,
        AWS_S3_BUCKET="test-bucket",
        AWS_ACCESS_KEY_ID=None,
        AWS_SECRET_ACCESS_KEY=None,
        CORS_ORIGINS="*",
        APP_PORT=8000,
        ADMIN_EMAIL="admin@sahyadri-siri.local",
        AUTO_CREATE_SCHEMA=True,
    )
    app = create_app(settings, enable_lifespan=False)
    app.state.settings = settings
    app.state.engine = create_async_engine(settings.database_url, echo=False, future=True)
    app.state.session_maker = async_sessionmaker(app.state.engine, expire_on_commit=False)
    app.state.redis = MemoryRedis()
    app.state.wqi_model = train_model()
    app.state.storage_backend = FallbackStorageBackend()
    app.state.ws_manager = WebSocketManager()
    app.state.stop_event = asyncio.Event()
    app.state.background_tasks = set()
    app.state.started_at = time.monotonic()
    await init_db(app.state.engine)
    yield app
    await app.state.engine.dispose()


@pytest_asyncio.fixture
async def client(app):
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://testserver") as client:
        yield client


@pytest.fixture
def unique_user_payload():
    suffix = uuid4().hex[:8]
    return {
        "email": f"user-{suffix}@example.com",
        "password": "Password123!",
        "displayName": f"User {suffix}",
    }
