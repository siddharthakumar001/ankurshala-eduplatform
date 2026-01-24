#!/bin/bash
# =============================================================================
# Database Migration Helper Script
# Run on VM: ./scripts/migrate.sh
# =============================================================================

set -e

PROJECT_DIR="/opt/ankurshala"
COMPOSE_FILE="docker-compose.prod.yml"

cd "$PROJECT_DIR"

# Load environment
if [ -f .env-prod ]; then
    set -a
    source .env-prod
    set +a
fi

echo "═══════════════════════════════════════════════════════════════"
echo "   DATABASE MIGRATION HELPER"
echo "═══════════════════════════════════════════════════════════════"
echo ""

case "${1:-status}" in
    status|s)
        echo "📊 Current Migration Status:"
        echo ""
        docker compose -f "$COMPOSE_FILE" exec -T postgres psql -U "$DB_USERNAME" -d "$DB_NAME" -c "
            SELECT 
                installed_rank,
                version,
                description,
                type,
                CASE WHEN success THEN '✅' ELSE '❌' END as status,
                installed_on
            FROM flyway_schema_history 
            ORDER BY installed_rank DESC 
            LIMIT 15;
        " 2>/dev/null || echo "❌ Could not connect to database"
        ;;
    
    run|r)
        echo "🔄 Running migrations by restarting backend..."
        docker compose -f "$COMPOSE_FILE" restart backend
        echo "⏳ Waiting 45 seconds for migrations..."
        sleep 45
        echo ""
        echo "📊 Migration status after restart:"
        $0 status
        ;;
    
    repair)
        echo "🔧 Repairing Flyway checksums..."
        docker compose -f "$COMPOSE_FILE" exec -T backend java -jar app.jar \
            --spring.main.web-application-type=none \
            --spring.flyway.repair-on-migrate=true \
            2>/dev/null || echo "⚠️ Repair command failed, try restarting backend"
        ;;
    
    baseline)
        echo "📌 Creating baseline (for existing databases)..."
        docker compose -f "$COMPOSE_FILE" exec -T backend java -jar app.jar \
            --spring.main.web-application-type=none \
            --spring.flyway.baseline-on-migrate=true \
            --spring.flyway.baseline-version=1 \
            2>/dev/null || {
            echo "⚠️ Baseline via command failed, setting env and restarting..."
            docker compose -f "$COMPOSE_FILE" up -d backend
        }
        ;;
    
    reset)
        echo "⚠️  WARNING: This will reset the migration history!"
        read -p "Are you sure? (yes/no): " confirm
        if [ "$confirm" = "yes" ]; then
            docker compose -f "$COMPOSE_FILE" exec -T postgres psql -U "$DB_USERNAME" -d "$DB_NAME" -c "
                DROP TABLE IF EXISTS flyway_schema_history CASCADE;
            "
            echo "Migration history cleared. Restart backend to re-run all migrations."
        else
            echo "Cancelled."
        fi
        ;;
    
    backup)
        BACKUP_FILE="backup-$(date +%Y%m%d-%H%M%S).sql"
        echo "📦 Creating database backup: $BACKUP_FILE"
        docker compose -f "$COMPOSE_FILE" exec -T postgres pg_dump -U "$DB_USERNAME" "$DB_NAME" > "$BACKUP_FILE"
        echo "✅ Backup saved to: $BACKUP_FILE"
        ;;
    
    *)
        echo "Usage: ./scripts/migrate.sh [command]"
        echo ""
        echo "Commands:"
        echo "  status    Show migration history (default)"
        echo "  run       Run pending migrations (restart backend)"
        echo "  repair    Repair Flyway checksums"
        echo "  baseline  Create baseline for existing DB"
        echo "  reset     Clear migration history (DANGEROUS)"
        echo "  backup    Create database backup"
        ;;
esac

echo ""

