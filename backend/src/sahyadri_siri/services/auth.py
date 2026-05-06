from __future__ import annotations

from uuid import uuid4

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.exc import IntegrityError

from sahyadri_siri.core.config import Settings
from sahyadri_siri.core.security import create_access_token, create_refresh_token, hash_password, verify_password
from sahyadri_siri.db import User
from sahyadri_siri.schemas import TokenPair, UserAuthResponse, UserPublic


async def get_user_by_email(session: AsyncSession, email: str) -> User | None:
    result = await session.execute(select(User).where(User.email == email))
    return result.scalar_one_or_none()


async def create_user(session: AsyncSession, settings: Settings, email: str, password: str, display_name: str) -> UserAuthResponse:
    existing = await get_user_by_email(session, email)
    if existing is not None:
        raise ValueError("Email already registered")

    user = User(email=email.lower(), hashed_password=hash_password(password), display_name=display_name)
    session.add(user)
    try:
        await session.commit()
    except IntegrityError as exc:
        await session.rollback()
        raise ValueError("Email already registered") from exc
    await session.refresh(user)
    return _build_auth_response(user, settings)


async def authenticate_user(session: AsyncSession, settings: Settings, email: str, password: str) -> UserAuthResponse:
    user = await get_user_by_email(session, email.lower())
    if user is None or not verify_password(password, user.hashed_password):
        raise ValueError("Invalid credentials")
    return _build_auth_response(user, settings)


async def authenticate_google_user(
    session: AsyncSession,
    settings: Settings,
    id_token: str,
    display_name_override: str | None = None,
) -> UserAuthResponse:
    token_data = await _verify_google_id_token(id_token, settings.google_web_client_id)
    email = (token_data.get("email") or "").lower()
    if not email:
        raise ValueError("Google token did not include email")

    preferred_name = (display_name_override or token_data.get("name") or email.split("@")[0]).strip()
    user = await get_user_by_email(session, email)
    if user is None:
        user = User(
            email=email,
            hashed_password=hash_password(f"google:{uuid4()}"),
            display_name=preferred_name,
        )
        session.add(user)
        await session.commit()
        await session.refresh(user)
    elif preferred_name and user.display_name != preferred_name:
        user.display_name = preferred_name
        await session.commit()
        await session.refresh(user)

    return _build_auth_response(user, settings)


def _build_auth_response(user: User, settings: Settings) -> UserAuthResponse:
    access_token = create_access_token(user.id, settings)
    refresh_token = create_refresh_token(user.id, settings)
    return UserAuthResponse(
        accessToken=access_token,
        refreshToken=refresh_token,
        user=UserPublic(id=user.id, email=user.email, displayName=user.display_name),
    )


async def refresh_access_token(settings: Settings, refresh_token: str) -> str:
    from sahyadri_siri.core.security import validate_token_type

    payload = validate_token_type(refresh_token, settings, "refresh")
    subject = payload.get("sub")
    if not subject:
        raise ValueError("Invalid refresh token")
    return create_access_token(subject, settings)


async def _verify_google_id_token(id_token: str, expected_audience: str | None) -> dict:
    try:
        from google.auth.transport import requests as google_requests
        from google.oauth2 import id_token as google_id_token
    except ImportError as exc:
        raise ValueError("google-auth dependency is missing on backend") from exc

    request = google_requests.Request()
    try:
        payload = google_id_token.verify_oauth2_token(
            id_token,
            request,
            expected_audience if expected_audience else None,
        )
    except Exception as exc:
        raise ValueError("Invalid Google ID token") from exc
    return payload
