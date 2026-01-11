#!/bin/bash

# ============================================================================
# GymMate Database Alignment Script
# ============================================================================
# This script provides options for aligning your database schema with the code.
#
# Usage: ./align_database.sh [option]
#
# Options:
#   flyway  - Use Flyway migrations (recommended)
#   jpa     - Use JPA auto-DDL (quick dev)
#   reset   - Complete database reset
# ============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
MIGRATION_DIR="$PROJECT_ROOT/src/main/resources/db/migration"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

echo_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if psql is available
check_psql() {
    if ! command -v psql &> /dev/null; then
        echo_error "psql command not found. Please install PostgreSQL client."
        exit 1
    fi
}

# Load environment variables
load_env() {
    if [ -f "$PROJECT_ROOT/.env" ]; then
        echo_info "Loading environment from .env file..."
        export $(grep -v '^#' "$PROJECT_ROOT/.env" | xargs)
    else
        echo_warn ".env file not found. Using defaults."
        export PG_URI="jdbc:postgresql://localhost:5432/gymmate"
        export PG_USER="gymmate"
        export PG_PASSWORD="gymmate_password"
    fi

    # Extract database details from JDBC URL
    DB_HOST=$(echo $PG_URI | sed -n 's/.*:\/\/\([^:\/]*\).*/\1/p')
    DB_PORT=$(echo $PG_URI | sed -n 's/.*:\([0-9]*\)\/.*/\1/p')
    DB_NAME=$(echo $PG_URI | sed -n 's/.*\/\([^?]*\).*/\1/p')

    DB_HOST=${DB_HOST:-localhost}
    DB_PORT=${DB_PORT:-5432}
    DB_NAME=${DB_NAME:-gymmate}
}

# Reset database using the reset script
reset_database() {
    echo_info "Resetting database..."
    check_psql
    load_env

    PGPASSWORD=$PG_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $PG_USER -d $DB_NAME -f "$SCRIPT_DIR/reset_dev_database.sql"

    echo_info "Database reset complete!"
}

# Run Flyway migrations
run_flyway() {
    echo_info "Running Flyway migrations..."
    load_env

    cd "$PROJECT_ROOT"

    # Clean Flyway if requested
    if [ "$1" == "clean" ]; then
        echo_warn "Cleaning Flyway schema history..."
        ./mvnw flyway:clean -Dflyway.cleanDisabled=false
    fi

    # Run migrations
    ./mvnw flyway:migrate

    echo_info "Flyway migrations complete!"
}

# Show current migration status
show_status() {
    echo_info "Checking Flyway migration status..."
    load_env

    cd "$PROJECT_ROOT"
    ./mvnw flyway:info
}

# Display help
show_help() {
    echo "============================================"
    echo "GymMate Database Alignment Script"
    echo "============================================"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  flyway      Run Flyway migrations"
    echo "  flyway-clean Clean and re-run all migrations"
    echo "  status      Show migration status"
    echo "  reset       Complete database reset"
    echo "  help        Show this help"
    echo ""
    echo "For a complete fresh start:"
    echo "  1. $0 reset"
    echo "  2. $0 flyway-clean"
    echo ""
    echo "Or manually:"
    echo "  1. Run scripts/reset_dev_database.sql in your DB client"
    echo "  2. Enable Flyway in application-dev.yml"
    echo "  3. Start the application"
    echo ""
}

# Main
case "$1" in
    flyway)
        run_flyway
        ;;
    flyway-clean)
        run_flyway clean
        ;;
    status)
        show_status
        ;;
    reset)
        reset_database
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac

