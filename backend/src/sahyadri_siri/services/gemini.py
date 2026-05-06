from __future__ import annotations

import asyncio
import json
import re
from dataclasses import dataclass
from typing import Any

from sahyadri_siri.core.config import Settings
from sahyadri_siri.schemas import AdvisoryCreate, ReportCreate
from sahyadri_siri.services.wqi import WQIModel

STATUS_VALUES = {"Critical", "Caution", "Safe"}


@dataclass(slots=True)
class GeminiAdvisoryResult:
    title: str
    description: str
    status: str


def fallback_advisory(wqi_score: float) -> GeminiAdvisoryResult:
    if wqi_score >= 65:
        return GeminiAdvisoryResult(
            title="Water Source Stable",
            description="Water source in good condition. Continue routine monitoring and community reporting.",
            status="Safe",
        )
    if wqi_score >= 35:
        return GeminiAdvisoryResult(
            title="Clarity Decline Detected",
            description="Reduced water clarity detected. Increase sampling frequency and inspect nearby discharge sources.",
            status="Caution",
        )
    return GeminiAdvisoryResult(
        title="Contamination Alert",
        description="Contamination detected. Avoid direct use and escalate the report for immediate field verification.",
        status="Critical",
    )


def _normalize_status(status: str) -> str:
    value = status.strip().title()
    return value if value in STATUS_VALUES else "Caution"


def _parse_json_payload(raw_text: str) -> GeminiAdvisoryResult:
    match = re.search(r"\{.*\}", raw_text, flags=re.DOTALL)
    candidate = match.group(0) if match else raw_text
    payload = json.loads(candidate)
    return GeminiAdvisoryResult(
        title=str(payload.get("title", "Water Quality Advisory")),
        description=str(payload.get("description", "")),
        status=_normalize_status(str(payload.get("status", "Caution"))),
    )


async def generate_advisory(
    report: ReportCreate,
    wqi_score: float,
    settings: Settings,
) -> GeminiAdvisoryResult:
    if not settings.enable_gemini_advisory:
        return fallback_advisory(wqi_score)

    if not settings.gemini_api_key:
        return fallback_advisory(wqi_score)

    try:
        import google.generativeai as genai
    except Exception:
        return fallback_advisory(wqi_score)

    prompt = (
        "You are generating a water quality advisory for the Sahyadri-Siri app. "
        "Return only valid JSON with keys title, description, status. "
        f"Use one of Critical, Caution, Safe. Report details: clarity={report.clarity}, "
        f"smell={report.smell}, flow={report.flow}, wqi={wqi_score:.2f}, "
        f"latitude={report.latitude}, longitude={report.longitude}."
    )

    def _call_gemini() -> GeminiAdvisoryResult:
        genai.configure(api_key=settings.gemini_api_key)
        model = genai.GenerativeModel(
            "gemini-1.5-pro",
            generation_config={"temperature": 0.2, "response_mime_type": "application/json"},
        )
        response = model.generate_content(prompt)
        text = getattr(response, "text", "") or ""
        if not text and getattr(response, "candidates", None):
            text = str(response.candidates[0].content.parts[0].text)
        if not text:
            raise ValueError("Empty Gemini response")
        return _parse_json_payload(text)

    try:
        timeout = max(1, int(settings.gemini_timeout_seconds))
        return await asyncio.wait_for(asyncio.to_thread(_call_gemini), timeout=timeout)
    except Exception:
        return fallback_advisory(wqi_score)


def to_advisory_create(result: GeminiAdvisoryResult, timestamp: int) -> AdvisoryCreate:
    return AdvisoryCreate(title=result.title, description=result.description, status=result.status, timestamp=timestamp)
