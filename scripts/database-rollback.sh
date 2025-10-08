#!/bin/bash
set -Eeuo pipefail

# Database Rollback Script for Production
# Restores database from backup if deployment fails

# Colors for output
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log(){ case "$1" in INFO) echo -e "${BLUE}ℹ️  $2${NC}";; WARN) echo -e "${YELLOW}⚠️  $2${NC}";;
FAIL) echo -e "${RED}❌ $2${NC}";; PASS) echo -e "${GREEN}✅ $2${NC}";; esac; }

# Load environment variables
ENV_FILE=".env-prod"
[[ -f "$ENV_FILE" ]] || { log FAIL "$ENV_FILE not found"; exit 1; }
set -a; source "$ENV_FILE"; set +a

# Database connection parameters
DB_HOST="${DB_HOST:-postgres}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-ankurshala}"
DB_USERNAME="${DB_USERNAME:-ankur}"
DB_PASSWORD="${DB_PASSWORD}"

# Backup configuration
BACKUP_DIR="/opt/ankurshala/backups"

usage() {
    echo "Usage: $0 [options]"
    echo "Options:"
    echo "  --backup-file FILE    Use specific backup file"
    echo "  --latest              Use latest backup (default)"
    echo "  --dry-run             Show what would be restored without executing"
    echo "  --help                Show this help"
    exit 1
}

# Parse command line arguments
BACKUP_FILE=""
DRY_RUN=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --backup-file)
            BACKUP_FILE="$2"
            shift 2
            ;;
        --latest)
            BACKUP_FILE=""
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --help)
            usage
            ;;
        *)
            echo "Unknown option: $1"
            usage
            ;;
    esac
done

log INFO "🔄 Starting Database Rollback"
echo "=============================="

# Find backup file
if [ -z "$BACKUP_FILE" ]; then
    # Find latest backup
    BACKUP_FILE=$(find "$BACKUP_DIR" -name "ankurshala_backup_*.sql" -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -d' ' -f2-)
fi

if [ -z "$BACKUP_FILE" ] || [ ! -f "$BACKUP_FILE" ]; then
    log FAIL "No backup file found in $BACKUP_DIR"
    echo "Available backups:"
    ls -la "$BACKUP_DIR"/ankurshala_backup_*.sql 2>/dev/null || echo "  No backups found"
    exit 1
fi

log INFO "Using backup file: $BACKUP_FILE"

# Verify backup file integrity
log INFO "Verifying backup file integrity..."
if ! docker exec ankurshala_db_prod pg_restore --list "$BACKUP_FILE" >/dev/null 2>&1; then
    log FAIL "Backup file integrity check failed"
    exit 1
fi
log PASS "Backup file integrity verified"

# Read backup metadata if available
if [ -f "${BACKUP_FILE}.meta" ]; then
    log INFO "Backup metadata:"
    cat "${BACKUP_FILE}.meta" | jq -r 'to_entries[] | "  \(.key): \(.value)"' 2>/dev/null || cat "${BACKUP_FILE}.meta"
fi

if [ "$DRY_RUN" = true ]; then
    log INFO "DRY RUN: Would restore database from $BACKUP_FILE"
    log INFO "DRY RUN: Backup contains:"
    docker exec ankurshala_db_prod pg_restore --list "$BACKUP_FILE" | head -20
    log INFO "DRY RUN: Rollback simulation completed"
    exit 0
fi

# Confirm rollback
echo ""
log WARN "⚠️  WARNING: This will completely replace the current database!"
log WARN "⚠️  All current data will be lost and replaced with backup data!"
echo ""
read -p "Are you sure you want to proceed? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    log INFO "Rollback cancelled by user"
    exit 0
fi

# Check if database container is running
if ! docker ps --format '{{.Names}}' | grep -q '^ankurshala_db_prod$'; then
    log FAIL "Database container 'ankurshala_db_prod' is not running"
    exit 1
fi

# Stop application services to prevent data corruption
log INFO "Stopping application services..."
docker-compose -f docker-compose.prod.yml --env-file .env-prod stop backend frontend nginx || true

# Create emergency backup of current state
EMERGENCY_BACKUP="${BACKUP_DIR}/emergency_backup_$(date +%Y%m%d_%H%M%S).sql"
log INFO "Creating emergency backup of current state: $EMERGENCY_BACKUP"

if docker exec ankurshala_db_prod pg_dump \
    -h localhost \
    -U "$DB_USERNAME" \
    -d "$DB_NAME" \
    --verbose \
    --no-password \
    --format=custom \
    --compress=9 \
    --file="/tmp/emergency_backup.dump" 2>/dev/null; then
    
    docker cp "ankurshala_db_prod:/tmp/emergency_backup.dump" "$EMERGENCY_BACKUP"
    docker exec ankurshala_db_prod rm -f "/tmp/emergency_backup.dump"
    log PASS "Emergency backup created"
else
    log WARN "Failed to create emergency backup - proceeding anyway"
fi

# Drop and recreate database
log INFO "Dropping and recreating database..."
docker exec ankurshala_db_prod psql -h localhost -U "$DB_USERNAME" -d postgres -c "DROP DATABASE IF EXISTS $DB_NAME;" || true
docker exec ankurshala_db_prod psql -h localhost -U "$DB_USERNAME" -d postgres -c "CREATE DATABASE $DB_NAME;" || true

# Restore from backup
log INFO "Restoring database from backup..."
if docker exec ankurshala_db_prod pg_restore \
    -h localhost \
    -U "$DB_USERNAME" \
    -d "$DB_NAME" \
    --verbose \
    --no-password \
    --clean \
    --if-exists \
    "$BACKUP_FILE" 2>/dev/null; then
    log PASS "Database restored successfully"
else
    log FAIL "Database restore failed"
    exit 1
fi

# Verify restoration
log INFO "Verifying database restoration..."
RESTORED_TABLES=$(docker exec ankurshala_db_prod psql -h localhost -U "$DB_USERNAME" -d "$DB_NAME" -t -c "
    SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';
" 2>/dev/null | tr -d ' ')

if [ "$RESTORED_TABLES" -gt 0 ]; then
    log PASS "Database restoration verified ($RESTORED_TABLES tables restored)"
else
    log FAIL "Database restoration verification failed"
    exit 1
fi

# Restart application services
log INFO "Restarting application services..."
docker-compose -f docker-compose.prod.yml --env-file .env-prod up -d backend frontend nginx

# Wait for services to be healthy
log INFO "Waiting for services to be healthy..."
sleep 30

# Verify application health
log INFO "Verifying application health..."
if curl -f -s https://ankurshala.com/health >/dev/null 2>&1; then
    log PASS "Application health check passed"
else
    log WARN "Application health check failed - may need manual intervention"
fi

log PASS "🎉 Database rollback completed successfully!"
echo ""
echo "📊 Rollback Summary:"
echo "• Source Backup: $BACKUP_FILE"
echo "• Emergency Backup: $EMERGENCY_BACKUP"
echo "• Tables Restored: $RESTORED_TABLES"
echo "• Application Status: $(curl -s https://ankurshala.com/health || echo 'Unknown')"
echo ""
echo "🔄 Database has been restored to backup state!"
