"""add report indexes

Revision ID: 0002_add_report_indexes
Revises: 0001_initial
Create Date: 2026-05-03 00:00:01.000000
"""

from __future__ import annotations

from alembic import op

revision = "0002_add_report_indexes"
down_revision = "0001_initial"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.create_index("ix_reports_timestamp", "reports", ["timestamp"], unique=False)
    op.create_index("ix_reports_latitude", "reports", ["latitude"], unique=False)
    op.create_index("ix_reports_longitude", "reports", ["longitude"], unique=False)
    op.create_index("ix_reports_lat_lon", "reports", ["latitude", "longitude"], unique=False)


def downgrade() -> None:
    op.drop_index("ix_reports_lat_lon", table_name="reports")
    op.drop_index("ix_reports_longitude", table_name="reports")
    op.drop_index("ix_reports_latitude", table_name="reports")
    op.drop_index("ix_reports_timestamp", table_name="reports")
