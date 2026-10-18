#!/usr/bin/env bash
# ==============================================================================
# GymMate VPS Database Cleanup Runner
# ==============================================================================
# This script safely purges all test data before going live.
#
# Usage:
#   chmod +x cleanup_vps_db.sh
#   ./cleanup_vps_db.sh
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="${SCRIPT_DIR}/cleanup_test_data_before_golive.sql"

echo "========================================================"
echo " GymMate Pre-Go-Live Database Cleanup"
echo "========================================================"
echo "WARNING: This will purge ALL test tenants, gyms, users, and transactions."
echo "Table structures, Flyway migrations, and subscription tiers will remain intact."
read -p "Are you sure you want to proceed? (yes/no): " CONFIRM

if [[ "${CONFIRM}" != "yes" ]]; then
    echo "Aborted. No changes were made."
    exit 0
fi

# Detect if PostgreSQL is running in a local Docker container or direct host
CONTAINER_ID=$(docker ps -q --filter "name=postgres" || true)

if [[ -n "${CONTAINER_ID}" ]]; then
    echo "--> Detected running PostgreSQL container (${CONTAINER_ID}). Executing cleanup inside container..."
    docker exec -i "${CONTAINER_ID}" psql -U gymmate -d gymmate < "${SQL_FILE}"
else
    echo "--> Executing cleanup via local psql..."
    DB_USER="${PG_USER:-gymmate}"
    DB_NAME="${PG_DB:-gymmate}"
    DB_HOST="${PG_HOST:-localhost}"
    DB_PORT="${PG_PORT:-5432}"

    PGPASSWORD="${PG_PASSWORD:-${GYMMATE_DB_PASSWORD:-}}" psql \
        -h "${DB_HOST}" \
        -p "${DB_PORT}" \
        -U "${DB_USER}" \
        -d "${DB_NAME}" \
        -f "${SQL_FILE}"
fi

echo "========================================================"
echo " Cleanup complete! Restarting backend container..."
echo "========================================================"
cd /opt/vantroxia/apps/gymmate 2>/dev/null || true
docker compose restart gymmate-backend 2>/dev/null || docker compose restart backend 2>/dev/null || echo "Please restart your backend container manually: docker compose restart"

echo "Done!"
