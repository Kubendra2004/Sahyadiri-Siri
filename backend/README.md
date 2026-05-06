# Sahyadri-Siri Backend

Production-ready FastAPI backend for the Sahyadri-Siri Android client.

## Stack

- Python 3.11+
- FastAPI + Uvicorn
- SQLAlchemy async + Alembic
- PostgreSQL 16
- Redis
- Scikit-learn WQI model
- Google Gemini advisories
- Firebase Storage or Amazon S3
- JWT auth with python-jose

## Project Layout

- `src/sahyadri_siri/main.py` FastAPI application factory and startup wiring
- `src/sahyadri_siri/routers/` HTTP and WebSocket routers
- `src/sahyadri_siri/services/` auth, cache, Gemini, storage, and WQI services
- `src/sahyadri_siri/middleware/` logging and rate limiting
- `ml/train_wqi.py` synthetic model training script
- `alembic/versions/` database migrations
- `tests/` pytest coverage for the critical flows

## Environment

Copy `.env.example` to `.env` and set the secrets and provider values.

Required variables:

- `DATABASE_URL`
- `REDIS_URL`
- `JWT_SECRET`
- `GEMINI_API_KEY` for Gemini advisories
- `STORAGE_PROVIDER` set to `firebase` or `s3`

## Local Development

```bash
cd backend
python -m venv .venv
.venv\Scripts\activate
pip install -e .[dev]
alembic upgrade head
uvicorn sahyadri_siri.main:app --reload --app-dir src
```

## Docker

```bash
cd backend
docker compose up --build
```

Run migrations from inside the API container once the database is healthy:

```bash
docker compose exec api alembic upgrade head
```

## WQI Model Training

Train and serialize the model to `src/sahyadri_siri/ml/wqi_model.pkl`:

```bash
python src/sahyadri_siri/ml/train_wqi.py
```

## API Notes

- All JSON fields use camelCase.
- Report timestamps are epoch milliseconds.
- All routes are mounted under `/api`.
- `GET /api/health` is the only unauthenticated HTTP endpoint.
