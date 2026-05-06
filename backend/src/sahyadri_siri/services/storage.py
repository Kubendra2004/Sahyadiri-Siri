from __future__ import annotations

import asyncio
import importlib
from dataclasses import dataclass
from pathlib import Path
from uuid import uuid4

from sahyadri_siri.core.config import Settings


@dataclass(slots=True)
class UploadResult:
    url: str
    path: str


class LocalStorageBackend:
    def __init__(self, base_dir: Path, public_base_url: str) -> None:
        self.base_dir = base_dir
        self.public_base_url = public_base_url.rstrip("/")
        self.base_dir.mkdir(parents=True, exist_ok=True)

    async def upload(self, data: bytes, object_name: str, content_type: str) -> UploadResult:
        target = self.base_dir / object_name
        target.parent.mkdir(parents=True, exist_ok=True)
        await asyncio.to_thread(target.write_bytes, data)
        return UploadResult(url=f"{self.public_base_url}/uploads/{object_name}", path=object_name)


class S3StorageBackend:
    def __init__(self, settings: Settings) -> None:
        boto3 = importlib.import_module("boto3")
        self.bucket = settings.aws_s3_bucket or "sahyadri-siri-uploads"
        self.client = boto3.client(
            "s3",
            aws_access_key_id=settings.aws_access_key_id,
            aws_secret_access_key=settings.aws_secret_access_key,
        )

    async def upload(self, data: bytes, object_name: str, content_type: str) -> UploadResult:
        def _upload() -> str:
            self.client.put_object(Bucket=self.bucket, Key=object_name, Body=data, ContentType=content_type)
            return f"https://{self.bucket}.s3.amazonaws.com/{object_name}"

        return UploadResult(url=await asyncio.to_thread(_upload), path=object_name)


class FirebaseStorageBackend:
    def __init__(self, settings: Settings) -> None:
        firebase_admin = importlib.import_module("firebase_admin")
        credentials = importlib.import_module("firebase_admin.credentials")
        storage = importlib.import_module("firebase_admin.storage")

        if not firebase_admin._apps:
            if not settings.firebase_credentials_path:
                raise ValueError("FIREBASE_CREDENTIALS_PATH is required for firebase storage")
            cred = credentials.Certificate(settings.firebase_credentials_path)
            firebase_admin.initialize_app(
                cred,
                {"storageBucket": settings.firebase_storage_bucket or "sahyadri-siri.appspot.com"},
            )
        self.storage = storage
        self.bucket_name = settings.firebase_storage_bucket or "sahyadri-siri.appspot.com"

    async def upload(self, data: bytes, object_name: str, content_type: str) -> UploadResult:
        def _upload() -> str:
            bucket = self.storage.bucket(self.bucket_name)
            blob = bucket.blob(object_name)
            blob.upload_from_string(data, content_type=content_type)
            try:
                blob.make_public()
            except Exception:
                pass
            return blob.public_url or f"https://storage.googleapis.com/{self.bucket_name}/{object_name}"

        return UploadResult(url=await asyncio.to_thread(_upload), path=object_name)


class FallbackStorageBackend:
    def __init__(self, public_base_url: str) -> None:
        self.base_dir = Path("uploads")
        self.public_base_url = public_base_url.rstrip("/")
        self.base_dir.mkdir(parents=True, exist_ok=True)

    async def upload(self, data: bytes, object_name: str, content_type: str) -> UploadResult:
        path = self.base_dir / object_name
        path.parent.mkdir(parents=True, exist_ok=True)
        await asyncio.to_thread(path.write_bytes, data)
        return UploadResult(url=f"{self.public_base_url}/uploads/{object_name}", path=object_name)


async def build_storage_backend(settings: Settings):
    public_base = settings.backend_public_base_url or f"http://127.0.0.1:{settings.app_port}"
    provider = settings.storage_provider.lower()
    if provider == "s3":
        return S3StorageBackend(settings)
    if provider == "firebase":
        try:
            return FirebaseStorageBackend(settings)
        except Exception:
            return FallbackStorageBackend(public_base)
    return FallbackStorageBackend(public_base)


def build_object_name(user_id: str, filename: str) -> str:
    suffix = Path(filename).suffix.lower() or ".jpg"
    return f"{user_id}/{uuid4()}{suffix}"
