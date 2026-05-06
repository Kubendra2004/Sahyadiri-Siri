from __future__ import annotations

import asyncio
import json
import time
from collections.abc import Awaitable, Callable
from functools import wraps
from typing import Any, Protocol

import redis.asyncio as redis_asyncio


class RedisLike(Protocol):
    async def get(self, key: str) -> str | None: ...
    async def setex(self, key: str, ttl: int, value: str) -> bool: ...
    async def delete(self, *keys: str) -> int: ...
    async def incrby(self, key: str, amount: int = 1) -> int: ...
    async def expire(self, key: str, ttl: int) -> bool: ...
    async def ping(self) -> bool: ...
    def pubsub(self): ...
    async def publish(self, channel: str, message: str) -> int: ...


class MemoryPubSub:
    def __init__(self, redis: "MemoryRedis") -> None:
        self._redis = redis
        self._channels: set[str] = set()

    async def subscribe(self, *channels: str) -> None:
        for channel in channels:
            self._channels.add(channel)
            self._redis._subscribers.setdefault(channel, set()).add(self)

    async def listen(self):
        queue: asyncio.Queue[dict[str, Any]] = asyncio.Queue()
        self._redis._listener_queues[id(self)] = queue
        try:
            while True:
                message = await queue.get()
                yield message
        finally:
            self._redis._listener_queues.pop(id(self), None)
            for channel in self._channels:
                subscribers = self._redis._subscribers.get(channel)
                if subscribers is not None:
                    subscribers.discard(self)

    async def put(self, channel: str, data: str) -> None:
        queue = self._redis._listener_queues.get(id(self))
        if queue is not None:
            await queue.put({"type": "message", "channel": channel, "data": data})


class MemoryRedis:
    def __init__(self) -> None:
        self._store: dict[str, tuple[str, float | None]] = {}
        self._subscribers: dict[str, set[MemoryPubSub]] = {}
        self._listener_queues: dict[int, asyncio.Queue[dict[str, Any]]] = {}

    def _purge(self, key: str) -> None:
        entry = self._store.get(key)
        if entry is None:
            return
        value, expires_at = entry
        if expires_at is not None and expires_at <= time.time():
            self._store.pop(key, None)

    async def get(self, key: str) -> str | None:
        self._purge(key)
        entry = self._store.get(key)
        return None if entry is None else entry[0]

    async def setex(self, key: str, ttl: int, value: str) -> bool:
        self._store[key] = (value, time.time() + ttl)
        return True

    async def delete(self, *keys: str) -> int:
        removed = 0
        for key in keys:
            if key in self._store:
                removed += 1
                self._store.pop(key, None)
        return removed

    async def incrby(self, key: str, amount: int = 1) -> int:
        current = int((await self.get(key)) or 0)
        updated = current + amount
        await self.setex(key, 300, str(updated))
        return updated

    async def expire(self, key: str, ttl: int) -> bool:
        self._purge(key)
        if key not in self._store:
            return False
        value, _ = self._store[key]
        self._store[key] = (value, time.time() + ttl)
        return True

    async def ping(self) -> bool:
        return True

    def pubsub(self) -> MemoryPubSub:
        return MemoryPubSub(self)

    async def publish(self, channel: str, message: str) -> int:
        subscribers = list(self._subscribers.get(channel, set()))
        for subscriber in subscribers:
            await subscriber.put(channel, message)
        return len(subscribers)

    async def close(self) -> None:
        return None


async def build_redis_client(redis_url: str) -> RedisLike:
    if redis_url.startswith("memory://"):
        return MemoryRedis()
    client = redis_asyncio.from_url(redis_url, decode_responses=True)
    try:
        await client.ping()
        return client
    except Exception:
        close = getattr(client, "close", None)
        if close is not None:
            result = close()
            if asyncio.iscoroutine(result):
                await result
        return MemoryRedis()


async def safe_redis_ping(redis: RedisLike | None) -> bool:
    if redis is None:
        return False
    try:
        return bool(await redis.ping())
    except Exception:
        return False


async def cache_get_json(redis: RedisLike | None, key: str) -> Any | None:
    if redis is None:
        return None
    try:
        payload = await redis.get(key)
    except Exception:
        return None
    if payload is None:
        return None
    return json.loads(payload)


async def cache_set_json(redis: RedisLike | None, key: str, ttl: int, value: Any) -> None:
    if redis is None:
        return
    try:
        await redis.setex(key, ttl, json.dumps(value, default=str))
    except Exception:
        return


async def cache_delete(redis: RedisLike | None, *keys: str) -> None:
    if redis is None:
        return
    try:
        await redis.delete(*keys)
    except Exception:
        return


async def cache_delete_prefix(redis: RedisLike | None, prefix: str) -> None:
    if redis is None:
        return
    try:
        if hasattr(redis, "scan_iter"):
            async for key in redis.scan_iter(f"{prefix}*"):
                await redis.delete(key)
            return
        if hasattr(redis, "_store"):
            keys = [key for key in list(redis._store.keys()) if key.startswith(prefix)]
            if keys:
                await redis.delete(*keys)
    except Exception:
        return


def cached(ttl: int, key_prefix: str | None = None):
    def decorator(func: Callable[..., Awaitable[Any]]):
        @wraps(func)
        async def wrapper(*args, **kwargs):
            redis = kwargs.get("redis")
            cache_key = kwargs.get("cache_key")
            if cache_key is None:
                key_parts = [key_prefix or func.__module__ + "." + func.__name__]
                if args:
                    key_parts.append(json.dumps([str(arg) for arg in args], default=str))
                if kwargs:
                    filtered = {key: value for key, value in kwargs.items() if key not in {"redis", "cache_key"}}
                    key_parts.append(json.dumps(filtered, default=str, sort_keys=True))
                cache_key = ":".join(key_parts)
            cached_value = await cache_get_json(redis, cache_key)
            if cached_value is not None:
                return cached_value
            result = await func(*args, **kwargs)
            await cache_set_json(redis, cache_key, ttl, result)
            return result

        return wrapper

    return decorator
