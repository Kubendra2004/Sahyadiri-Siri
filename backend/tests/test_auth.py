from __future__ import annotations

import pytest


@pytest.mark.asyncio
async def test_auth_flow(client, unique_user_payload):
    register_response = await client.post("/api/auth/register", json=unique_user_payload)
    assert register_response.status_code == 201
    register_data = register_response.json()
    assert register_data["user"]["email"] == unique_user_payload["email"]
    assert register_data["user"]["displayName"] == unique_user_payload["displayName"]
    assert register_data["accessToken"]
    assert register_data["refreshToken"]

    login_response = await client.post(
        "/api/auth/login",
        json={"email": unique_user_payload["email"], "password": unique_user_payload["password"]},
    )
    assert login_response.status_code == 200
    login_data = login_response.json()
    assert login_data["user"]["email"] == unique_user_payload["email"]

    refresh_response = await client.post(
        "/api/auth/refresh",
        json={"refreshToken": login_data["refreshToken"]},
    )
    assert refresh_response.status_code == 200
    assert refresh_response.json()["accessToken"]
