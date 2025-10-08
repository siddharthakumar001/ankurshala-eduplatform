#!/bin/bash
set -Eeuo pipefail

# Database Backup Script for Production
# Creates timestamped backups before deployment

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
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/ankurshala_backup_${TIMESTAMP}.sql"
BACKUP_RETENTION_DAYS=7

log INFO "🗄️ Starting Database Backup"
echo "============================="

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Check if database container is running
if ! docker ps --format '{{.Names}}' | grep -q '^ankurshala_db_prod$'; then
    log FAIL "Database container 'ankurshala_db_prod' is not running"
    exit 1
fi

log INFO "Creating database backup: $BACKUP_FILE"

# Create database backup
if docker exec -e PGPASSWORD="$DB_PASSWORD" ankurshala_db_prod pg_dump \
    -h localhost \
    -U "$DB_USERNAME" \
    -d "$DB_NAME" \
    --verbose \
    --no-password \
    --format=custom \
    --compress=9 \
    --file="/tmp/backup_${TIMESTAMP}.dump" 2>/dev/null; then
    
    # Copy backup from container to host
    docker cp "ankurshala_db_prod:/tmp/backup_${TIMESTAMP}.dump" "$BACKUP_FILE"
    
    # Clean up temporary file in container
    docker exec ankurshala_db_prod rm -f "/tmp/backup_${TIMESTAMP}.dump"
    
    # Verify backup file
    if [ -f "$BACKUP_FILE" ] && [ -s "$BACKUP_FILE" ]; then
        BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
        log PASS "Database backup created successfully ($BACKUP_SIZE)"
    else
        log FAIL "Backup file verification failed"
        exit 1
    fi
else
    log FAIL "Database backup creation failed"
    exit 1
fi

# Create backup metadata
cat > "${BACKUP_FILE}.meta" <<EOF
{
  "timestamp": "$TIMESTAMP",
  "database": "$DB_NAME",
  "username": "$DB_USERNAME",
  "backup_file": "$BACKUP_FILE",
  "backup_size": "$BACKUP_SIZE",
  "created_at": "$(date -u +%FT%TZ)",
  "git_commit": "$(git rev-parse HEAD 2>/dev/null || echo 'unknown')",
  "git_branch": "$(git branch --show-current 2>/dev/null || echo 'unknown')"
}
EOF

log PASS "Backup metadata created"

# Clean up old backups (keep only last 7 days)
log INFO "Cleaning up old backups (keeping last $BACKUP_RETENTION_DAYS days)..."
find "$BACKUP_DIR" -name "ankurshala_backup_*.sql" -type f -mtime +$BACKUP_RETENTION_DAYS -delete 2>/dev/null || true
find "$BACKUP_DIR" -name "ankurshala_backup_*.meta" -type f -mtime +$BACKUP_RETENTION_DAYS -delete 2>/dev/null || true

# Count remaining backups
REMAINING_BACKUPS=$(find "$BACKUP_DIR" -name "ankurshala_backup_*.sql" -type f | wc -l)
log INFO "Backup cleanup completed. $REMAINING_BACKUPS backups remaining"

# Test backup integrity using a transient postgres container (ensures pg_restore available)
log INFO "Testing backup integrity..."
if docker run --rm -v "$BACKUP_FILE":/tmp/backup.dump postgres:15-alpine \
    sh -c "pg_restore --list /tmp/backup.dump >/dev/null 2>&1"; then
    log PASS "Backup integrity test passed"
else
    log FAIL "Backup integrity test failed"
    exit 1
fi

log PASS "🎉 Database backup completed successfully!"
echo ""
echo "📊 Backup Summary:"
echo "• Backup File: $BACKUP_FILE"
echo "• Backup Size: $BACKUP_SIZE"
echo "• Timestamp: $TIMESTAMP"
echo "• Total Backups: $REMAINING_BACKUPS"
echo "• Retention: $BACKUP_RETENTION_DAYS days"
echo ""
echo "💾 Database is safely backed up before deployment!"

# Export backup file path for use by other scripts
echo "$BACKUP_FILE" > /tmp/latest_backup_path
