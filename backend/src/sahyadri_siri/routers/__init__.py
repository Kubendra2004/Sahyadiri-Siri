from sahyadri_siri.routers.alerts import router as alerts_router
from sahyadri_siri.routers.auth import router as auth_router
from sahyadri_siri.routers.health import router as health_router
from sahyadri_siri.routers.reports import router as reports_router
from sahyadri_siri.routers.upload import router as upload_router
from sahyadri_siri.routers.ws import router as ws_router

__all__ = ["auth_router", "reports_router", "upload_router", "alerts_router", "ws_router", "health_router"]
