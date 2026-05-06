from __future__ import annotations

import json
import math
import time
from dataclasses import dataclass

from fastapi import Request, status
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware

from sahyadri_siri.core.security import decode_token
from sahyadri_siri.services.cache import RedisLike


@dataclass(slots=True)
class RateLimitState:
    tokens: float
    updated_at: float


class RateLimitMiddleware(BaseHTTPMiddleware):
    def __init__(self, app) -> None:
        super().__init__(app)
        self.exempt_paths = {"/api/health", "/docs", "/openapi.json", "/redoc"}

    async def dispatch(self, request: Request, call_next):
        if request.url.path in self.exempt_paths:
            return await call_next(request)

        settings = request.app.state.settings
        redis: RedisLike | None = request.app.state.redis
        subject = await self._resolve_subject(request)
        bucket = "write" if request.method.upper() not in {"GET", "HEAD", "OPTIONS"} else "read"
        limit = settings.write_rate_limit_per_minute if bucket == "write" else settings.read_rate_limit_per_minute
        allowed, retry_after = await self._consume_token(redis, subject, bucket, limit)
        if not allowed:
            return JSONResponse(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                content={"detail": "Rate limit exceeded"},
                headers={"Retry-After": str(max(1, retry_after))},
            )
        return await call_next(request)

    async def _resolve_subject(self, request: Request) -> str:
        authorization = request.headers.get("authorization")
        if authorization and authorization.startswith("Bearer "):
            token = authorization.removeprefix("Bearer ").strip()
            try:
                payload = decode_token(token, request.app.state.settings)
                subject = payload.get("sub")
                if subject:
                    return f"user:{subject}"
            except Exception:
                pass
        client_host = request.client.host if request.client else "anonymous"
        return f"ip:{client_host}"

    async def _consume_token(self, redis: RedisLike | None, subject: str, bucket: str, limit_per_minute: int) -> tuple[bool, int]:
        if redis is None:
            return True, 0
        key = f"ratelimit:{subject}:{bucket}"
        now = time.time()
        refill_rate = limit_per_minute / 60.0
        capacity = float(limit_per_minute)
        try:
            raw_state = await redis.get(key)
            if raw_state:
                state = RateLimitState(**__import__("json").loads(raw_state))
            else:
                state = RateLimitState(tokens=capacity, updated_at=now)
            elapsed = max(0.0, now - state.updated_at)
            state.tokens = min(capacity, state.tokens + elapsed * refill_rate)
            state.updated_at = now
            if state.tokens < 1.0:
                retry_after = math.ceil((1.0 - state.tokens) / refill_rate)
                await redis.setex(key, 60, json.dumps(state.__dict__))
                return False, retry_after
            state.tokens -= 1.0
            await redis.setex(key, 60, json.dumps(state.__dict__))
            return True, 0
        except Exception:
            return True, 0
