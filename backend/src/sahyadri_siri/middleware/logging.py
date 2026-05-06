from __future__ import annotations

import logging
import time
from typing import Any

from fastapi import Request
from fastapi.responses import Response
from pythonjsonlogger import jsonlogger
from starlette.middleware.base import BaseHTTPMiddleware


class JsonRequestLoggingMiddleware(BaseHTTPMiddleware):
    def __init__(self, app, logger_name: str = "sahyadri_siri") -> None:
        super().__init__(app)
        self.logger = logging.getLogger(logger_name)
        self.logger.setLevel(logging.INFO)
        if not self.logger.handlers:
            handler = logging.StreamHandler()
            handler.setFormatter(jsonlogger.JsonFormatter("%(message)s %(method)s %(path)s %(status)s %(duration_ms)s %(user_id)s"))
            self.logger.addHandler(handler)
        self.logger.propagate = False

    async def dispatch(self, request: Request, call_next) -> Response:
        started_at = time.perf_counter()
        response: Response | None = None
        try:
            response = await call_next(request)
            return response
        finally:
            duration_ms = round((time.perf_counter() - started_at) * 1000, 2)
            user_id = getattr(request.state, "user_id", None)
            status_code = response.status_code if response is not None else 500
            self.logger.info(
                "request",
                extra={
                    "method": request.method,
                    "path": request.url.path,
                    "status": status_code,
                    "duration_ms": duration_ms,
                    "user_id": user_id,
                },
            )
