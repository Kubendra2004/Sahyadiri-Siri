from __future__ import annotations

from io import BytesIO
from pathlib import Path

from fastapi import APIRouter, Depends, File, HTTPException, Request, UploadFile, status

from sahyadri_siri.dependencies import get_current_user
from sahyadri_siri.schemas import UploadResponse
from sahyadri_siri.services.storage import build_object_name

router = APIRouter(prefix="/api", tags=["upload"])

ALLOWED_CONTENT_TYPES = {"image/jpeg": ".jpg", "image/png": ".png"}
JPEG_MAGIC = b"\xff\xd8"
PNG_MAGIC = b"\x89PNG\r\n\x1a\n"


@router.post("/upload-image", response_model=UploadResponse)
async def upload_image(request: Request, image: UploadFile = File(...), user=Depends(get_current_user)):
    if image.content_type not in ALLOWED_CONTENT_TYPES:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Only JPEG and PNG images are allowed")

    data = await image.read()
    if len(data) > request.app.state.settings.max_upload_bytes:
        raise HTTPException(status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE, detail="Image exceeds 10MB limit")

    if image.content_type == "image/jpeg" and not data.startswith(JPEG_MAGIC):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid JPEG file")
    if image.content_type == "image/png" and not data.startswith(PNG_MAGIC):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid PNG file")

    data, output_content_type, output_name = await _compress_image(
        data,
        image.content_type,
        request.app.state.settings,
        image.filename,
    )
    if len(data) > request.app.state.settings.max_upload_bytes:
        raise HTTPException(status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE, detail="Image exceeds 10MB limit")

    object_name = build_object_name(
        str(user.id),
        output_name or image.filename or ALLOWED_CONTENT_TYPES[image.content_type],
    )
    storage_backend = request.app.state.storage_backend
    result = await storage_backend.upload(data, object_name, output_content_type)
    return UploadResponse(imageUrl=result.url, imagePath=result.path)


async def _compress_image(data: bytes, content_type: str, settings, filename: str | None) -> tuple[bytes, str, str | None]:
    if content_type not in {"image/jpeg", "image/png"}:
        return data, content_type, filename

    def _process() -> tuple[bytes, str, str | None]:
        try:
            from PIL import Image
        except Exception:
            return data, content_type, filename

        try:
            with Image.open(BytesIO(data)) as image:
                image.load()
                max_dim = max(1, int(settings.image_max_dimension))
                if max(image.size) > max_dim:
                    image.thumbnail((max_dim, max_dim), Image.Resampling.LANCZOS)

                output = BytesIO()
                output_content_type = content_type
                output_name = filename
                if settings.enable_webp:
                    if image.mode not in {"RGB", "L"}:
                        image = image.convert("RGB")
                    image.save(
                        output,
                        format="WEBP",
                        quality=max(40, min(95, int(settings.webp_quality))),
                        method=6,
                    )
                    output_content_type = "image/webp"
                    stem = Path(filename or "image").stem
                    output_name = f"{stem}.webp"
                elif content_type == "image/png":
                    image.save(
                        output,
                        format="PNG",
                        optimize=True,
                        compress_level=max(0, min(9, int(settings.image_png_compress_level))),
                    )
                else:
                    if image.mode not in {"RGB", "L"}:
                        image = image.convert("RGB")
                    image.save(
                        output,
                        format="JPEG",
                        quality=max(40, min(95, int(settings.image_jpeg_quality))),
                        optimize=True,
                        progressive=True,
                    )
                return output.getvalue(), output_content_type, output_name
        except Exception:
            return data, content_type, filename

    import asyncio

    return await asyncio.to_thread(_process)
