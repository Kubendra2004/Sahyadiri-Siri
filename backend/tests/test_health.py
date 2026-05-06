from __future__ import annotations

import pytest


@pytest.mark.asyncio
async def test_health_check(client):
    response = await client.get("/api/health")
    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "ok"
    assert body["dbConnected"] is True
    assert body["redisConnected"] is True
    assert body["version"]
