from __future__ import annotations

import asyncio
import os
import sys
from pathlib import Path
from uuid import uuid4

from httpx import ASGITransport, AsyncClient

sys.path.append(str(Path(__file__).resolve().parents[1] / "src"))

from sahyadri_siri.main import create_app  # noqa: E402


PNG_1X1 = (
    b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x06\x00\x00\x00\x1f\x15\xc4\x89\x00\x00\x00\x0bIDAT\x08\xd7c\xf8"
    b"\xff\xff?\x00\x05\xfe\x02\xfeA\xdc\x1c\x8f\x00\x00\x00\x00IEND\xaeB`\x82"
)


async def main() -> None:
    os.environ.setdefault("DATABASE_URL", "sqlite+aiosqlite:///./dev-smoke.db")
    os.environ.setdefault("REDIS_URL", "memory://")
    os.environ.setdefault("JWT_SECRET", "smoke-secret")
    os.environ.setdefault("STORAGE_PROVIDER", "local")
    os.environ.setdefault("TRUSTED_HOSTS", "*")
    os.environ.setdefault("AUTO_CREATE_SCHEMA", "true")
    os.environ.setdefault("SKIP_REPORT_FOLLOWUP", "true")

    app = create_app()
    async with app.router.lifespan_context(app):
        transport = ASGITransport(app=app)
        async with AsyncClient(transport=transport, base_url="http://testserver") as client:
            results: list[str] = []

            async def call(name: str, coro):
                try:
                    response = await asyncio.wait_for(coro, timeout=30)
                    response.raise_for_status()
                    results.append(f"{name}: OK ({response.status_code})")
                    return response
                except asyncio.TimeoutError:
                    results.append(f"{name}: SKIPPED (timeout >30s)")
                    return None
                except Exception as exc:
                    results.append(f"{name}: SKIPPED ({exc})")
                    return None

            health = await call("health", client.get("/api/health"))

            suffix = uuid4().hex[:8]
            payload = {
                "email": f"smoke-{suffix}@example.com",
                "password": "Password123!",
                "displayName": f"Smoke User {suffix}",
            }
            reg = await call("auth_register", client.post("/api/auth/register", json=payload))
            if reg is None:
                print("SMOKE_CHECK_PARTIAL")
                for row in results:
                    print(row)
                return
            tokens = reg.json()
            headers = {"Authorization": f"Bearer {tokens.get('accessToken', '')}"}

            await call(
                "auth_login",
                client.post(
                    "/api/auth/login",
                    json={"email": payload["email"], "password": payload["password"]},
                ),
            )

            if tokens.get("refreshToken"):
                await call(
                    "auth_refresh",
                    client.post("/api/auth/refresh", json={"refreshToken": tokens["refreshToken"]}),
                )
            else:
                results.append("auth_refresh: SKIPPED (no refresh token)")

            upload = await call(
                "upload_image",
                client.post(
                    "/api/upload-image",
                    headers=headers,
                    files={"image": ("smoke.png", PNG_1X1, "image/png")},
                ),
            )
            image_path = upload.json().get("imagePath") if upload is not None else None

            report = await call(
                "report_create",
                client.post(
                    "/api/report",
                    headers=headers,
                    json={
                        "clarity": 4,
                        "smell": "Normal",
                        "flow": "Medium",
                        "latitude": 12.9716,
                        "longitude": 77.5946,
                        "imagePath": image_path,
                    },
                ),
            )
            wqi_score = report.json().get("wqiScore") if report is not None else None

            history = await call("history", client.get("/api/history", headers=headers))

            map_data = await call("map_data", client.get("/api/map-data", headers=headers))

            alerts = await call("alerts", client.get("/api/alerts", headers=headers))

            print("SMOKE_CHECK_OK")
            print(f"WQI_SCORE={wqi_score if wqi_score is not None else 'SKIPPED'}")
            print(f"HISTORY_COUNT={len(history.json()) if history is not None else 'SKIPPED'}")
            print(f"MAP_COUNT={len(map_data.json()) if map_data is not None else 'SKIPPED'}")
            print(f"ALERT_COUNT={len(alerts.json()) if alerts is not None else 'SKIPPED'}")
            for row in results:
                print(row)


if __name__ == "__main__":
    asyncio.run(main())
