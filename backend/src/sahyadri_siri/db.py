from __future__ import annotations

from collections.abc import AsyncIterator
from datetime import UTC, datetime
from pathlib import Path
from uuid import uuid4

from sqlalchemy import BigInteger, DateTime, Float, ForeignKey, Index, Integer, String, Text, text
from sqlalchemy.ext.asyncio import AsyncEngine, AsyncSession, async_sessionmaker, create_async_engine
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship


class Base(DeclarativeBase):
    pass


class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(
        String(36),
        primary_key=True,
        default=lambda: str(uuid4()),
    )
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    hashed_password: Mapped[str] = mapped_column(String(255), nullable=False)
    display_name: Mapped[str] = mapped_column(String(255), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(UTC), nullable=False)

    reports: Mapped[list["Report"]] = relationship(back_populates="user")


class Advisory(Base):
    __tablename__ = "advisories"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    description: Mapped[str] = mapped_column(Text, nullable=False)
    status: Mapped[str] = mapped_column(String(20), nullable=False)
    timestamp: Mapped[int] = mapped_column(BigInteger, nullable=False)
    report_id: Mapped[str | None] = mapped_column(String(36), ForeignKey("reports.id", ondelete="SET NULL"), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(UTC), nullable=False)

    report: Mapped["Report | None"] = relationship(foreign_keys=[report_id])


class Report(Base):
    __tablename__ = "reports"
    __table_args__ = (
        Index("ix_reports_timestamp", "timestamp"),
        Index("ix_reports_latitude", "latitude"),
        Index("ix_reports_longitude", "longitude"),
        Index("ix_reports_lat_lon", "latitude", "longitude"),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    user_id: Mapped[str] = mapped_column(String(36), ForeignKey("users.id", ondelete="CASCADE"), index=True, nullable=False)
    clarity: Mapped[int] = mapped_column(Integer, nullable=False)
    smell: Mapped[str] = mapped_column(String(10), nullable=False)
    flow: Mapped[str] = mapped_column(String(10), nullable=False)
    latitude: Mapped[float] = mapped_column(Float, nullable=False)
    longitude: Mapped[float] = mapped_column(Float, nullable=False)
    image_path: Mapped[str | None] = mapped_column(Text, nullable=True)
    timestamp: Mapped[int] = mapped_column(BigInteger, nullable=False)
    status: Mapped[str] = mapped_column(String(20), nullable=False, default="SYNCED")
    wqi_score: Mapped[float] = mapped_column(Float, nullable=False)
    advisory_id: Mapped[str | None] = mapped_column(String(36), ForeignKey("advisories.id", ondelete="SET NULL"), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(UTC), nullable=False)

    user: Mapped[User] = relationship(back_populates="reports")
    advisory: Mapped[Advisory | None] = relationship(foreign_keys=[advisory_id], uselist=False)


async def create_session_maker(database_url: str) -> tuple[AsyncEngine, async_sessionmaker[AsyncSession]]:
    engine = create_async_engine(database_url, echo=False, future=True)
    session_maker = async_sessionmaker(engine, expire_on_commit=False)
    return engine, session_maker


async def build_engine(database_url: str, fallback_path: Path | None = None) -> tuple[AsyncEngine, bool]:
    engine = create_async_engine(database_url, echo=False, future=True)
    try:
        async with engine.connect() as connection:
            await connection.execute(text("SELECT 1"))
        return engine, False
    except Exception:
        await engine.dispose()
        fallback_file = fallback_path or Path(__file__).resolve().parents[2] / "dev.db"
        fallback_url = f"sqlite+aiosqlite:///{fallback_file.as_posix()}"
        fallback_engine = create_async_engine(fallback_url, echo=False, future=True)
        return fallback_engine, True


async def init_db(engine: AsyncEngine) -> None:
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


async def drop_db(engine: AsyncEngine) -> None:
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)


async def get_db_session(session_maker: async_sessionmaker[AsyncSession]) -> AsyncIterator[AsyncSession]:
    async with session_maker() as session:
        yield session
