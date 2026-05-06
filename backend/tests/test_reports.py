from __future__ import annotations

import asyncio
from uuid import uuid4

import pytest


@pytest.mark.asyncio
async def test_report_submission_and_history(client, unique_user_payload, app):
    register_response = await client.post("/api/auth/register", json=unique_user_payload)
    tokens = register_response.json()
    headers = {"Authorization": f"Bearer {tokens['accessToken']}"}
    report_id = str(uuid4())
    payload = {
        "id": report_id,
        "clarity": 5,
        "smell": "Normal",
        "flow": "Low",
        "latitude": 15.3173,
        "longitude": 75.7139,
        "imagePath": None,
        "timestamp": 1714723200000,
        "status": "PENDING",
    }

    response = await client.post("/api/report", json=payload, headers=headers)
    assert response.status_code == 201
    report = response.json()
    assert report["id"] == report_id
    assert report["status"] == "SYNCED"
    assert 0 <= report["wqiScore"] <= 100

    for _ in range(20):
        if not app.state.background_tasks:
            break
        await asyncio.sleep(0.05)

    history_response = await client.get("/api/history", headers=headers)
    assert history_response.status_code == 200
    history = history_response.json()
    assert len(history) == 1
    assert history[0]["id"] == report_id

    map_response = await client.get("/api/map-data", headers=headers)
    assert map_response.status_code == 200
    map_data = map_response.json()
    assert len(map_data) == 1
    assert map_data[0]["user"]["email"] == unique_user_payload["email"]

    alerts_response = await client.get("/api/alerts", headers=headers)
    assert alerts_response.status_code == 200
    alerts = alerts_response.json()
    assert len(alerts) == 1
    assert alerts[0]["status"] in {"Critical", "Caution", "Safe"}
