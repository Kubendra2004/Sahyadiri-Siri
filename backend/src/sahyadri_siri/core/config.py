from __future__ import annotations

from functools import lru_cache
from pathlib import Path
from typing import Any

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


_ROOT_ENV = Path(__file__).resolve().parents[4] / ".env"
_BACKEND_ENV = Path(__file__).resolve().parents[3] / ".env"
_ENV_FILES = tuple(str(path) for path in (_ROOT_ENV, _BACKEND_ENV) if path.exists()) or (".env",)


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=_ENV_FILES, env_file_encoding="utf-8", extra="ignore")

    app_name: str = "Sahyadri-Siri"
    app_version: str = "1.0.0"
    environment: str = Field(default="development", validation_alias="ENVIRONMENT")
    database_url: str = Field(validation_alias="DATABASE_URL")
    redis_url: str = Field(validation_alias="REDIS_URL")
    jwt_secret: str = Field(validation_alias="JWT_SECRET")
    gemini_api_key: str | None = Field(default=None, validation_alias="GEMINI_API_KEY")
    google_web_client_id: str | None = Field(default=None, validation_alias="GOOGLE_WEB_CLIENT_ID")
    enable_gemini_advisory: bool = Field(default=True, validation_alias="ENABLE_GEMINI_ADVISORY")
    gemini_timeout_seconds: int = Field(default=10, validation_alias="GEMINI_TIMEOUT_SECONDS")
    storage_provider: str = Field(default="firebase", validation_alias="STORAGE_PROVIDER")
    firebase_credentials_path: str | None = Field(default=None, validation_alias="FIREBASE_CREDENTIALS_PATH")
    firebase_storage_bucket: str | None = Field(default=None, validation_alias="FIREBASE_STORAGE_BUCKET")
    aws_s3_bucket: str | None = Field(default=None, validation_alias="AWS_S3_BUCKET")
    aws_access_key_id: str | None = Field(default=None, validation_alias="AWS_ACCESS_KEY_ID")
    aws_secret_access_key: str | None = Field(default=None, validation_alias="AWS_SECRET_ACCESS_KEY")
    cors_origins: str = Field(default="*", validation_alias="CORS_ORIGINS")
    trusted_hosts: str = Field(default="localhost,127.0.0.1,10.0.2.2,::1", validation_alias="TRUSTED_HOSTS")
    app_port: int = Field(default=8000, validation_alias="APP_PORT")
    backend_public_base_url: str | None = Field(default=None, validation_alias="BACKEND_PUBLIC_BASE_URL")
    admin_email: str | None = Field(default=None, validation_alias="ADMIN_EMAIL")
    auto_create_schema: bool = Field(default=False, validation_alias="AUTO_CREATE_SCHEMA")
    enable_api_docs: bool = Field(default=True, validation_alias="ENABLE_API_DOCS")
    access_token_exp_minutes: int = 30
    refresh_token_exp_days: int = 7
    map_cache_ttl_seconds: int = 30
    map_max_limit: int = 500
    alerts_cache_ttl_seconds: int = 60
    write_rate_limit_per_minute: int = 30
    read_rate_limit_per_minute: int = 120
    websocket_ping_interval_seconds: int = 30
    max_upload_bytes: int = 10 * 1024 * 1024
    image_max_dimension: int = 1280
    image_jpeg_quality: int = 80
    image_png_compress_level: int = 6
    enable_webp: bool = Field(default=False, validation_alias="ENABLE_WEBP")
    webp_quality: int = Field(default=75, validation_alias="WEBP_QUALITY")
    model_path: str = "src/sahyadri_siri/ml/wqi_model.pkl"

    @property
    def cors_origin_list(self) -> list[str]:
        value = self.cors_origins.strip()
        if value == "*":
            return ["*"]
        return [origin.strip() for origin in value.split(",") if origin.strip()]

    @property
    def trusted_host_list(self) -> list[str]:
        value = self.trusted_hosts.strip()
        if value == "*":
            return ["*"]
        return [host.strip() for host in value.split(",") if host.strip()]

    @property
    def docs_url(self) -> str | None:
        return "/docs" if self.enable_api_docs else None

    @property
    def redoc_url(self) -> str | None:
        return "/redoc" if self.enable_api_docs else None

    @property
    def openapi_url(self) -> str | None:
        return "/openapi.json" if self.enable_api_docs else None

    def as_dict(self) -> dict[str, Any]:
        return self.model_dump()


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings()
