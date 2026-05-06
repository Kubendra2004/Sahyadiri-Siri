from __future__ import annotations

from typing import Annotated

from fastapi import Depends, Header, HTTPException, Request, status
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker

from sahyadri_siri.core.config import Settings, get_settings
from sahyadri_siri.core.security import decode_token, validate_token_type
from sahyadri_siri.db import User


async def get_app_settings() -> Settings:
    return get_settings()


async def get_session(request: Request) -> AsyncSession:
    session_maker = request.app.state.session_maker
    async with session_maker() as session:
        yield session


async def get_redis(request: Request):
    return request.app.state.redis


async def get_current_user(request: Request, authorization: str | None = Header(default=None)) -> User:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing bearer token")
    token = authorization.removeprefix("Bearer ").strip()
    settings: Settings = request.app.state.settings
    try:
        payload = validate_token_type(token, settings, "access")
    except Exception as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token") from exc
    user_id = payload.get("sub")
    if not user_id:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")
    request.state.user_id = user_id
    session_maker = request.app.state.session_maker
    async with session_maker() as session:
        user = await session.get(User, user_id)
        if user is None:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found")
        return user


async def get_optional_current_user(request: Request, authorization: str | None = Header(default=None)) -> User | None:
    if not authorization or not authorization.startswith("Bearer "):
        return None
    token = authorization.removeprefix("Bearer ").strip()
    settings: Settings = request.app.state.settings
    try:
        payload = validate_token_type(token, settings, "access")
    except Exception:
        return None
    user_id = payload.get("sub")
    if not user_id:
        return None
    request.state.user_id = user_id
    session_maker = request.app.state.session_maker
    async with session_maker() as session:
        return await session.get(User, user_id)
