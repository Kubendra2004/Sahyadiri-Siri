#!/usr/bin/env python3
"""
Validation script to ensure the Sahyadri-Siri backend is complete and properly structured.
Run this after installation to verify all components are in place.
"""

from __future__ import annotations

import sys
from pathlib import Path

def check_file(path: Path, description: str) -> bool:
    """Check if a file exists and print status."""
    exists = path.exists()
    status = "✓" if exists else "✗"
    print(f"{status} {description}")
    return exists

def check_directory(path: Path, description: str) -> bool:
    """Check if a directory exists and print status."""
    exists = path.is_dir()
    status = "✓" if exists else "✗"
    print(f"{status} {description}")
    return exists

def main() -> int:
    """Run all validation checks."""
    root = Path(__file__).parent
    all_ok = True

    print("\n📋 Sahyadri-Siri Backend Validation\n")
    print("=" * 60)

    print("\n📁 Core Package Structure")
    all_ok &= check_directory(root / "src", "src/ directory")
    all_ok &= check_directory(root / "src/sahyadri_siri", "src/sahyadri_siri/ package")
    all_ok &= check_directory(root / "src/sahyadri_siri/core", "core module")
    all_ok &= check_directory(root / "src/sahyadri_siri/services", "services module")
    all_ok &= check_directory(root / "src/sahyadri_siri/routers", "routers module")
    all_ok &= check_directory(root / "src/sahyadri_siri/middleware", "middleware module")
    all_ok &= check_directory(root / "src/sahyadri_siri/ml", "ml module")

    print("\n📄 Core Application Files")
    all_ok &= check_file(root / "src/sahyadri_siri/__init__.py", "__init__.py")
    all_ok &= check_file(root / "src/sahyadri_siri/main.py", "main.py (FastAPI app)")
    all_ok &= check_file(root / "src/sahyadri_siri/db.py", "db.py (SQLAlchemy models)")
    all_ok &= check_file(root / "src/sahyadri_siri/models.py", "models.py (ORM exports)")
    all_ok &= check_file(root / "src/sahyadri_siri/schemas.py", "schemas.py (Pydantic schemas)")
    all_ok &= check_file(root / "src/sahyadri_siri/dependencies.py", "dependencies.py (FastAPI dependencies)")

    print("\n🔐 Core Module")
    all_ok &= check_file(root / "src/sahyadri_siri/core/__init__.py", "core/__init__.py")
    all_ok &= check_file(root / "src/sahyadri_siri/core/config.py", "core/config.py (Settings)")
    all_ok &= check_file(root / "src/sahyadri_siri/core/security.py", "core/security.py (JWT & hashing)")

    print("\n🔌 Router Endpoints")
    all_ok &= check_file(root / "src/sahyadri_siri/routers/__init__.py", "routers/__init__.py")
    all_ok &= check_file(root / "src/sahyadri_siri/routers/auth.py", "routers/auth.py (register, login, refresh)")
    all_ok &= check_file(root / "src/sahyadri_siri/routers/reports.py", "routers/reports.py (submit, history, map-data)")
    all_ok &= check_file(root / "src/sahyadri_siri/routers/upload.py", "routers/upload.py (image upload)")
    all_ok &= check_file(root / "src/sahyadri_siri/routers/alerts.py", "routers/alerts.py (advisories)")
    all_ok &= check_file(root / "src/sahyadri_siri/routers/health.py", "routers/health.py (health check)")
    all_ok &= check_file(root / "src/sahyadri_siri/routers/ws.py", "routers/ws.py (WebSocket alerts)")

    print("\n⚙️ Services")
    all_ok &= check_file(root / "src/sahyadri_siri/services/__init__.py", "services/__init__.py")
    all_ok &= check_file(root / "src/sahyadri_siri/services/auth.py", "services/auth.py (user creation, authentication)")
    all_ok &= check_file(root / "src/sahyadri_siri/services/cache.py", "services/cache.py (Redis caching)")
    all_ok &= check_file(root / "src/sahyadri_siri/services/gemini.py", "services/gemini.py (Gemini AI)")
    all_ok &= check_file(root / "src/sahyadri_siri/services/storage.py", "services/storage.py (Firebase/S3)")
    all_ok &= check_file(root / "src/sahyadri_siri/services/wqi.py", "services/wqi.py (WQI model)")
    all_ok &= check_file(root / "src/sahyadri_siri/services/realtime.py", "services/realtime.py (WebSocket pub/sub)")

    print("\n🛡️ Middleware")
    all_ok &= check_file(root / "src/sahyadri_siri/middleware/__init__.py", "middleware/__init__.py")
    all_ok &= check_file(root / "src/sahyadri_siri/middleware/logging.py", "middleware/logging.py (JSON logging)")
    all_ok &= check_file(root / "src/sahyadri_siri/middleware/rate_limit.py", "middleware/rate_limit.py (rate limiting)")

    print("\n🤖 ML Module")
    all_ok &= check_file(root / "src/sahyadri_siri/ml/__init__.py", "ml/__init__.py")
    all_ok &= check_file(root / "ml/train_wqi.py", "ml/train_wqi.py (training script)")

    print("\n🧪 Tests")
    all_ok &= check_directory(root / "tests", "tests/ directory")
    all_ok &= check_file(root / "tests/conftest.py", "tests/conftest.py (pytest fixtures)")
    all_ok &= check_file(root / "tests/test_auth.py", "tests/test_auth.py")
    all_ok &= check_file(root / "tests/test_reports.py", "tests/test_reports.py")
    all_ok &= check_file(root / "tests/test_health.py", "tests/test_health.py")
    all_ok &= check_file(root / "tests/test_wqi.py", "tests/test_wqi.py")

    print("\n📦 Deployment & Configuration")
    all_ok &= check_file(root / "pyproject.toml", "pyproject.toml (dependencies)")
    all_ok &= check_file(root / "Dockerfile", "Dockerfile")
    all_ok &= check_file(root / "docker-compose.yml", "docker-compose.yml")
    all_ok &= check_file(root / ".env.example", ".env.example")
    all_ok &= check_file(root / "alembic.ini", "alembic.ini")
    all_ok &= check_file(root / "README.md", "README.md")
    all_ok &= check_file(root / "SETUP.md", "SETUP.md (setup guide)")

    print("\n📜 Database Migrations")
    all_ok &= check_directory(root / "alembic", "alembic/ directory")
    all_ok &= check_directory(root / "alembic/versions", "alembic/versions/ directory")
    all_ok &= check_file(root / "alembic/env.py", "alembic/env.py")
    all_ok &= check_file(root / "alembic/versions/0001_initial.py", "alembic/versions/0001_initial.py")

    print("\n" + "=" * 60)

    if all_ok:
        print("\n✅ All files and directories are in place!")
        print("\nNext steps:")
        print("1. Configure .env with DATABASE_URL, REDIS_URL, JWT_SECRET")
        print("2. Install dependencies: pip install -e .[dev]")
        print("3. Generate WQI model: python ml/train_wqi.py")
        print("4. Run migrations: alembic upgrade head")
        print("5. Run tests: pytest tests/ -v")
        print("6. Start server: uvicorn sahyadri_siri.main:app --reload --app-dir src")
        return 0
    else:
        print("\n❌ Some files are missing. See above for details.")
        return 1

if __name__ == "__main__":
    sys.exit(main())
