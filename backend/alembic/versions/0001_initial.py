"""initial schema

Revision ID: 0001_initial
Revises:
Create Date: 2026-05-03 00:00:00.000000
"""

from __future__ import annotations

from alembic import op
import sqlalchemy as sa


revision = "0001_initial"
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.create_table(
        "users",
        sa.Column("id", sa.String(length=36), primary_key=True, nullable=False),
        sa.Column("email", sa.String(length=255), nullable=False),
        sa.Column("hashed_password", sa.String(length=255), nullable=False),
        sa.Column("display_name", sa.String(length=255), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.UniqueConstraint("email", name="uq_users_email"),
    )
    op.create_index("ix_users_email", "users", ["email"], unique=True)

    op.create_table(
        "advisories",
        sa.Column("id", sa.String(length=36), primary_key=True, nullable=False),
        sa.Column("title", sa.String(length=255), nullable=False),
        sa.Column("description", sa.Text(), nullable=False),
        sa.Column("status", sa.String(length=20), nullable=False),
        sa.Column("timestamp", sa.BigInteger(), nullable=False),
        sa.Column("report_id", sa.String(length=36), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["report_id"], ["reports.id"], name="fk_advisories_report_id_reports", ondelete="SET NULL"),
    )

    op.create_table(
        "reports",
        sa.Column("id", sa.String(length=36), primary_key=True, nullable=False),
        sa.Column("user_id", sa.String(length=36), nullable=False),
        sa.Column("clarity", sa.Integer(), nullable=False),
        sa.Column("smell", sa.String(length=10), nullable=False),
        sa.Column("flow", sa.String(length=10), nullable=False),
        sa.Column("latitude", sa.Float(), nullable=False),
        sa.Column("longitude", sa.Float(), nullable=False),
        sa.Column("image_path", sa.Text(), nullable=True),
        sa.Column("timestamp", sa.BigInteger(), nullable=False),
        sa.Column("status", sa.String(length=20), nullable=False),
        sa.Column("wqi_score", sa.Float(), nullable=False),
        sa.Column("advisory_id", sa.String(length=36), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], name="fk_reports_user_id_users", ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["advisory_id"], ["advisories.id"], name="fk_reports_advisory_id_advisories", ondelete="SET NULL"),
    )
    op.create_index("ix_reports_user_id", "reports", ["user_id"], unique=False)
    op.create_index("ix_reports_timestamp", "reports", ["timestamp"], unique=False)
    op.create_index("ix_reports_latitude", "reports", ["latitude"], unique=False)
    op.create_index("ix_reports_longitude", "reports", ["longitude"], unique=False)
    op.create_index("ix_reports_lat_lon", "reports", ["latitude", "longitude"], unique=False)


def downgrade() -> None:
    op.drop_index("ix_reports_lat_lon", table_name="reports")
    op.drop_index("ix_reports_longitude", table_name="reports")
    op.drop_index("ix_reports_latitude", table_name="reports")
    op.drop_index("ix_reports_timestamp", table_name="reports")
    op.drop_index("ix_reports_user_id", table_name="reports")
    op.drop_table("reports")
    op.drop_table("advisories")
    op.drop_index("ix_users_email", table_name="users")
    op.drop_table("users")
