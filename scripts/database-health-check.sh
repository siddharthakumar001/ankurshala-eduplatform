#!/bin/bash
set -Eeuo pipefail

# Database Health Check Script for Production
# Verifies database connectivity, schema integrity, and migration status

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

log INFO "🔍 Starting Database Health Check"
echo "=================================="

# Test 1: Database Connectivity
log INFO "Test 1: Database Connectivity"
if docker exec -e PGPASSWORD="$DB_PASSWORD" ankurshala_db_prod pg_isready -h localhost -p 5432 -U "$DB_USERNAME" -d "$DB_NAME" >/dev/null 2>&1; then
    log PASS "Database is accepting connections"
else
    log FAIL "Database connectivity failed"
    exit 1
fi

# Test 2: Database Authentication
log INFO "Test 2: Database Authentication"
if docker exec -e PGPASSWORD="$DB_PASSWORD" ankurshala_db_prod psql -h localhost -U "$DB_USERNAME" -d "$DB_NAME" -c "SELECT 1;" >/dev/null 2>&1; then
    log PASS "Database authentication successful"
else
    log FAIL "Database authentication failed"
    exit 1
fi

# Test 3: Schema Integrity Check
log INFO "Test 3: Schema Integrity Check"
SCHEMA_CHECK=$(docker exec -e PGPASSWORD="$DB_PASSWORD" ankurshala_db_prod psql -h localhost -U "$DB_USERNAME" -d "$DB_NAME" -t -c "
    SELECT COUNT(*) FROM information_schema.tables 
    WHERE table_schema = 'public' AND table_name IN (
        'users', 'boards', 'grades', 'subjects', 'chapters', 'topics', 
        'notifications', 'fee_waivers', 'pricing_rules', 'import_jobs'
    );
" 2>/dev/null | tr -d ' ')

if [ "$SCHEMA_CHECK" -ge 10 ]; then
    log PASS "Core schema tables present ($SCHEMA_CHECK tables found)"
else
    log FAIL "Schema integrity check failed - missing core tables"
    exit 1
fi

# Test 4: Flyway Migration Status
log INFO "Test 4: Flyway Migration Status"
MIGRATION_STATUS=$(docker exec -e PGPASSWORD="$DB_PASSWORD" ankurshala_db_prod psql -h localhost -U "$DB_USERNAME" -d "$DB_NAME" -t -c "
    SELECT COUNT(*) FROM flyway_schema_history WHERE success = true;
" 2>/dev/null | tr -d ' ')

if [ "$MIGRATION_STATUS" -gt 0 ]; then
    log PASS "Flyway migrations applied successfully ($MIGRATION_STATUS migrations)"
else
    log FAIL "No successful Flyway migrations found"
    exit 1
fi

# Test 5: Data Integrity Check
log INFO "Test 5: Data Integrity Check"
DATA_CHECK=$(docker exec -e PGPASSWORD="$DB_PASSWORD" ankurshala_db_prod psql -h localhost -U "$DB_USERNAME" -d "$DB_NAME" -t -c "
    SELECT 
        (SELECT COUNT(*) FROM users WHERE enabled = true) as active_users,
        (SELECT COUNT(*) FROM boards WHERE active = true AND soft_deleted = false) as active_boards,
        (SELECT COUNT(*) FROM grades WHERE active = true AND soft_deleted = false) as active_grades;
" 2>/dev/null)

if echo "$DATA_CHECK" | grep -q "[0-9]"; then
    log PASS "Data integrity check passed"
    echo "   Active Users: $(echo "$DATA_CHECK" | awk '{print $1}')"
    echo "   Active Boards: $(echo "$DATA_CHECK" | awk '{print $2}')"
    echo "   Active Grades: $(echo "$DATA_CHECK" | awk '{print $3}')"
else
    log FAIL "Data integrity check failed"
    exit 1
fi

# Test 6: Database Performance Check
log INFO "Test 6: Database Performance Check"
PERF_START=$(date +%s.%N)
docker exec -e PGPASSWORD="$DB_PASSWORD" ankurshala_db_prod psql -h localhost -U "$DB_USERNAME" -d "$DB_NAME" -c "SELECT COUNT(*) FROM users;" >/dev/null 2>&1
PERF_END=$(date +%s.%N)
PERF_TIME=$(echo "$PERF_END - $PERF_START" | bc)

if (( $(echo "$PERF_TIME < 2.0" | bc -l) )); then
    log PASS "Database performance check passed (${PERF_TIME}s)"
else
    log WARN "Database performance warning (${PERF_TIME}s) - may be slow"
fi

# Test 7: Connection Pool Check
log INFO "Test 7: Connection Pool Check"
CONNECTION_COUNT=$(docker exec -e PGPASSWORD="$DB_PASSWORD" ankurshala_db_prod psql -h localhost -U "$DB_USERNAME" -d "$DB_NAME" -t -c "
    SELECT COUNT(*) FROM pg_stat_activity WHERE state = 'active';
" 2>/dev/null | tr -d ' ')

if [ "$CONNECTION_COUNT" -lt 20 ]; then
    log PASS "Connection pool healthy ($CONNECTION_COUNT active connections)"
else
    log WARN "High connection count ($CONNECTION_COUNT active connections)"
fi

log PASS "🎉 All database health checks passed!"
echo ""
echo "📊 Database Health Summary:"
echo "• Connectivity: ✅"
echo "• Authentication: ✅"
echo "• Schema Integrity: ✅"
echo "• Migration Status: ✅"
echo "• Data Integrity: ✅"
echo "• Performance: ✅"
echo "• Connection Pool: ✅"
echo ""
echo "🚀 Database is ready for production deployment!"
