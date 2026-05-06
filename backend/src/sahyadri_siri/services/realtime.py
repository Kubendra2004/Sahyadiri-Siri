from __future__ import annotations

import asyncio
import json
from typing import Any

from fastapi import WebSocket, WebSocketDisconnect

from sahyadri_siri.core.config import Settings
from sahyadri_siri.services.cache import RedisLike


class WebSocketManager:
    def __init__(self) -> None:
        self.active_connections: set[WebSocket] = set()
        self._lock = asyncio.Lock()

    async def connect(self, websocket: WebSocket) -> None:
        await websocket.accept()
        async with self._lock:
            self.active_connections.add(websocket)

    async def disconnect(self, websocket: WebSocket) -> None:
        async with self._lock:
            self.active_connections.discard(websocket)

    async def broadcast(self, payload: dict[str, Any]) -> None:
        message = json.dumps(payload, default=str)
        async with self._lock:
            connections = list(self.active_connections)
        for connection in connections:
            try:
                await connection.send_text(message)
            except Exception:
                await self.disconnect(connection)


async def broadcast_advisory(redis: RedisLike | None, advisory: dict[str, Any]) -> None:
    if redis is None:
        return
    try:
        await redis.publish("alerts", json.dumps(advisory, default=str))
    except Exception:
        return


async def run_alert_listener(redis: RedisLike | None, manager: WebSocketManager, settings: Settings, stop_event: asyncio.Event) -> None:
    if redis is None:
        await stop_event.wait()
        return
    try:
        pubsub = redis.pubsub()
        await pubsub.subscribe("alerts")
        async for message in pubsub.listen():
            if stop_event.is_set():
                break
            if message.get("type") != "message":
                continue
            data = message.get("data")
            if not data:
                continue
            try:
                advisory = json.loads(data)
            except json.JSONDecodeError:
                continue
            await manager.broadcast({"type": "NEW_ALERT", "data": {"advisory": advisory}})
    except asyncio.CancelledError:
        return


async def run_keepalive(websocket: WebSocket, settings: Settings, stop_event: asyncio.Event) -> None:
    while not stop_event.is_set():
        await asyncio.sleep(settings.websocket_ping_interval_seconds)
        try:
            await websocket.send_json({"type": "PING"})
        except Exception:
            break
