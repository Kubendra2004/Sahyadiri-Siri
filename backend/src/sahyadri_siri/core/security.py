from __future__ import annotations

from datetime import UTC, datetime, timedelta
from typing import Any

from jose import JWTError, jwt
from passlib.context import CryptContext

from sahyadri_siri.core.config import Settings

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(password: str, hashed_password: str) -> bool:
    return pwd_context.verify(password, hashed_password)


def create_token(subject: str, settings: Settings, token_type: str, expires_delta: timedelta) -> str:
    now = datetime.now(UTC)
    payload: dict[str, Any] = {
        "sub": subject,
        "typ": token_type,
        "iat": int(now.timestamp()),
        "exp": int((now + expires_delta).timestamp()),
    }
    return jwt.encode(payload, settings.jwt_secret, algorithm="HS256")


def create_access_token(subject: str, settings: Settings) -> str:
    return create_token(subject, settings, "access", timedelta(minutes=settings.access_token_exp_minutes))


def create_refresh_token(subject: str, settings: Settings) -> str:
    return create_token(subject, settings, "refresh", timedelta(days=settings.refresh_token_exp_days))


def decode_token(token: str, settings: Settings) -> dict[str, Any]:
    return jwt.decode(token, settings.jwt_secret, algorithms=["HS256"])


def validate_token_type(token: str, settings: Settings, expected_type: str) -> dict[str, Any]:
    payload = decode_token(token, settings)
    if payload.get("typ") != expected_type:
        raise JWTError("Invalid token type")
    return payload
