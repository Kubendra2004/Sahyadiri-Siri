from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Request, status
from sqlalchemy.ext.asyncio import AsyncSession

from sahyadri_siri.core.config import Settings
from sahyadri_siri.dependencies import get_current_user, get_session
from sahyadri_siri.schemas import GoogleAuthRequest, RefreshRequest, TokenResponse, UserAuthResponse, UserCreate, UserLogin
from sahyadri_siri.services.auth import authenticate_google_user, authenticate_user, create_user, refresh_access_token

router = APIRouter(prefix="/api/auth", tags=["auth"])


@router.post("/register", response_model=UserAuthResponse, status_code=status.HTTP_201_CREATED)
async def register(payload: UserCreate, request: Request, session: AsyncSession = Depends(get_session)):
    settings: Settings = request.app.state.settings
    try:
        return await create_user(session, settings, payload.email, payload.password, payload.display_name)
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(exc)) from exc


@router.post("/login", response_model=UserAuthResponse)
async def login(payload: UserLogin, request: Request, session: AsyncSession = Depends(get_session)):
    settings: Settings = request.app.state.settings
    try:
        return await authenticate_user(session, settings, payload.email, payload.password)
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=str(exc)) from exc


@router.post("/google", response_model=UserAuthResponse)
async def google_auth(payload: GoogleAuthRequest, request: Request, session: AsyncSession = Depends(get_session)):
    settings: Settings = request.app.state.settings
    try:
        return await authenticate_google_user(
            session=session,
            settings=settings,
            id_token=payload.id_token,
            display_name_override=payload.display_name,
        )
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=str(exc)) from exc


@router.post("/refresh", response_model=TokenResponse)
async def refresh(payload: RefreshRequest, request: Request):
    settings: Settings = request.app.state.settings
    try:
        access_token = await refresh_access_token(settings, payload.refresh_token)
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=str(exc)) from exc
    return TokenResponse(accessToken=access_token)
