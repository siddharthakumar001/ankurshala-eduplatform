#!/bin/bash
# =============================================================================
# Ankurshala Production Deployment Script
# Run this on the VM: ssh AnkurshalaVM@74.225.207.72
# Then: cd /opt/ankurshala && bash deploy-to-vm.sh
# =============================================================================

set -e

echo "🚀 Starting Ankurshala Production Deployment..."
echo ""

# Navigate to project directory
cd /opt/ankurshala

# Step 1: Pull latest code
echo "📥 Step 1: Pulling latest code..."
git fetch origin
git checkout ankurshala/prod-1.0-final
git reset --hard origin/ankurshala/prod-1.0-final
COMMIT_SHA=$(git rev-parse --short HEAD)
echo "✅ Code updated to commit: $COMMIT_SHA"
echo ""

# Step 2: Check current postgres image
echo "🔍 Step 2: Checking current PostgreSQL status..."
CURRENT_PG_IMAGE=$(docker inspect ankurshala_db_prod --format '{{.Config.Image}}' 2>/dev/null || echo "not running")
echo "   Current PostgreSQL image: $CURRENT_PG_IMAGE"

# Check if we need to update postgres for pgvector
if [[ "$CURRENT_PG_IMAGE" == *"pgvector"* ]]; then
    echo "✅ PostgreSQL already has pgvector support"
    PG_UPDATE_NEEDED=false
else
    echo "⚠️  PostgreSQL needs pgvector update for AI embeddings"
    PG_UPDATE_NEEDED=true
fi
echo ""

# Step 3: Load environment variables
echo "📋 Step 3: Loading environment variables..."
if [ -f .env-prod ]; then
    set -a
    source .env-prod
    set +a
    echo "✅ Environment loaded"
else
    echo "❌ .env-prod not found!"
    exit 1
fi
echo ""

# Step 4: Build new images
echo "🏗️  Step 4: Building Docker images..."
docker compose -f docker-compose.prod.yml build backend frontend
echo "✅ Images built successfully"
echo ""

# Step 5: Handle PostgreSQL update if needed
if [ "$PG_UPDATE_NEEDED" = true ]; then
    echo "🔄 Step 5a: Updating PostgreSQL to pgvector..."
    echo "   ⚠️  This will restart PostgreSQL but data is preserved in volume"
    docker compose -f docker-compose.prod.yml up -d postgres
    echo "   Waiting for PostgreSQL to be healthy..."
    sleep 15
    
    # Verify postgres is healthy
    for i in {1..30}; do
        if docker exec ankurshala_db_prod pg_isready -U ${DB_USERNAME} -d ${DB_NAME} 2>/dev/null; then
            echo "✅ PostgreSQL is healthy with pgvector"
            break
        fi
        if [ $i -eq 30 ]; then
            echo "❌ PostgreSQL health check timeout"
            docker logs --tail 20 ankurshala_db_prod
            exit 1
        fi
        sleep 2
    done
    echo ""
fi

# Step 6: Restart backend (this will run migrations)
echo "🔄 Step 6: Restarting backend service..."
docker compose -f docker-compose.prod.yml up -d --force-recreate backend
echo "   Waiting for backend to start and run migrations..."
sleep 30

# Check backend health
echo "   Checking backend health..."
for i in {1..40}; do
    if docker exec ankurshala_backend_prod wget -q --spider http://localhost:8080/api/actuator/health 2>/dev/null; then
        echo "✅ Backend is healthy! Migrations completed."
        break
    fi
    if [ $i -eq 40 ]; then
        echo "⚠️  Backend health check timeout - checking logs..."
        docker logs --tail 50 ankurshala_backend_prod
    else
        echo "   Waiting for backend... ($i/40)"
        sleep 3
    fi
done
echo ""

# Step 7: Restart frontend
echo "🔄 Step 7: Restarting frontend service..."
docker compose -f docker-compose.prod.yml up -d --force-recreate frontend
echo "   Waiting for frontend to start..."
sleep 20

# Check frontend health
echo "   Checking frontend health..."
for i in {1..20}; do
    if docker exec ankurshala_frontend_prod wget -q --spider http://localhost:3000 2>/dev/null; then
        echo "✅ Frontend is healthy!"
        break
    fi
    if [ $i -eq 20 ]; then
        echo "⚠️  Frontend health check timeout - checking logs..."
        docker logs --tail 30 ankurshala_frontend_prod
    else
        echo "   Waiting for frontend... ($i/20)"
        sleep 3
    fi
done
echo ""

# Step 8: Reload nginx (don't restart to preserve SSL)
echo "🔄 Step 8: Reloading nginx configuration..."
docker exec ankurshala_nginx_prod nginx -s reload 2>/dev/null || echo "   Nginx reload skipped (may not be needed)"
echo ""

# Step 9: Verify migrations
echo "📊 Step 9: Checking migration status..."
docker exec ankurshala_db_prod psql -U ${DB_USERNAME} -d ${DB_NAME} -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;" 2>/dev/null || echo "   Could not query migration history"
echo ""

# Step 10: Final status
echo "=========================================="
echo "📊 DEPLOYMENT SUMMARY"
echo "=========================================="
echo ""
echo "Commit deployed: $COMMIT_SHA"
echo ""
echo "Service Status:"
docker compose -f docker-compose.prod.yml ps --format "table {{.Name}}\t{{.Status}}"
echo ""
echo "🔗 Application URLs:"
echo "   - Homepage: https://ankurshala.com"
echo "   - Login: https://ankurshala.com/login"
echo "   - API Health: https://ankurshala.com/api/actuator/health"
echo ""
echo "✅ Deployment completed!"

