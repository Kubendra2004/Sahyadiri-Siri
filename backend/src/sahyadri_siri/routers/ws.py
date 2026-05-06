from __future__ import annotations

import asyncio
from typing import Any

from fastapi import APIRouter, WebSocket, WebSocketDisconnect

from sahyadri_siri.core.security import validate_token_type
from sahyadri_siri.schemas import WebSocketAlert

router = APIRouter(prefix="/api", tags=["websocket"])


@router.websocket("/ws/alerts")
async def alerts_websocket(websocket: WebSocket):
    token = websocket.query_params.get("token")
    settings = websocket.app.state.settings
    if not token:
        await websocket.close(code=1008)
        return
    try:
        validate_token_type(token, settings, "access")
    except Exception:
        await websocket.close(code=1008)
        return

    manager = websocket.app.state.ws_manager
    await manager.connect(websocket)
    stop_event = asyncio.Event()
    ping_task = asyncio.create_task(_keepalive(websocket, websocket.app.state.settings, stop_event))

    try:
        while True:
            message = await websocket.receive_text()
            if message.strip().lower() in {"pong", "{\"type\":\"pong\"}"}:
                continue
    except WebSocketDisconnect:
        pass
    finally:
        stop_event.set()
        ping_task.cancel()
        await manager.disconnect(websocket)


async def _keepalive(websocket: WebSocket, settings, stop_event: asyncio.Event) -> None:
    while not stop_event.is_set():
        await asyncio.sleep(settings.websocket_ping_interval_seconds)
        try:
            await websocket.send_json({"type": "PING"})
        except Exception:
            break
