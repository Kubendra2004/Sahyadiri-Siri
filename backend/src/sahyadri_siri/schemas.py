from __future__ import annotations

from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, ConfigDict, EmailStr, Field


def to_camel(value: str) -> str:
    parts = value.split("_")
    return parts[0] + "".join(part.capitalize() for part in parts[1:])


class APIModel(BaseModel):
    model_config = ConfigDict(populate_by_name=True, from_attributes=True, alias_generator=to_camel)


class TokenPair(APIModel):
    access_token: str = Field(..., alias="accessToken")
    refresh_token: str = Field(..., alias="refreshToken")


class TokenResponse(APIModel):
    access_token: str = Field(..., alias="accessToken")


class UserPublic(APIModel):
    id: UUID
    email: EmailStr
    display_name: str = Field(..., alias="displayName")


class UserCreate(APIModel):
    email: EmailStr
    password: str
    display_name: str = Field(..., alias="displayName")


class UserLogin(APIModel):
    email: EmailStr
    password: str


class GoogleAuthRequest(APIModel):
    id_token: str = Field(..., alias="idToken")
    display_name: str | None = Field(default=None, alias="displayName")


class RefreshRequest(APIModel):
    refresh_token: str = Field(..., alias="refreshToken")


class UserAuthResponse(APIModel):
    access_token: str = Field(..., alias="accessToken")
    refresh_token: str = Field(..., alias="refreshToken")
    user: UserPublic


class ReportCreate(APIModel):
    id: UUID | None = None
    clarity: int
    smell: str
    flow: str
    latitude: float
    longitude: float
    image_path: str | None = Field(default=None, alias="imagePath")
    timestamp: int | None = None


class AdvisoryCreate(APIModel):
    title: str
    description: str
    status: str
    timestamp: int


class AdvisoryRead(AdvisoryCreate):
    id: UUID


class ReportRead(APIModel):
    id: UUID
    clarity: int
    smell: str
    flow: str
    latitude: float
    longitude: float
    image_path: str | None = Field(default=None, alias="imagePath")
    timestamp: int
    status: str
    wqi_score: float = Field(..., alias="wqiScore")
    user_id: UUID = Field(..., alias="userId")
    advisory_id: UUID | None = Field(default=None, alias="advisoryId")
    user: UserPublic | None = None
    advisory: AdvisoryRead | None = None


class ReportWithUser(APIModel):
    id: UUID
    clarity: int
    smell: str
    flow: str
    latitude: float
    longitude: float
    image_path: str | None = Field(default=None, alias="imagePath")
    timestamp: int
    status: str
    wqi_score: float = Field(..., alias="wqiScore")
    user: UserPublic
    advisory: AdvisoryRead | None = None


class AlertRead(APIModel):
    id: UUID
    title: str
    description: str
    status: str
    timestamp: int


class HealthRead(APIModel):
    status: str
    version: str
    db_connected: bool = Field(..., alias="dbConnected")
    redis_connected: bool = Field(..., alias="redisConnected")
    uptime_s: float = Field(..., alias="uptimeS")


class UploadResponse(APIModel):
    image_url: str = Field(..., alias="imageUrl")
    image_path: str = Field(..., alias="imagePath")


class WebSocketAlert(APIModel):
    type: str
    data: dict
