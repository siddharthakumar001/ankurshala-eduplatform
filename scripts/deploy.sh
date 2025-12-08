#!/bin/bash
set -Eeuo pipefail

# =============================================================================
# AnkurShala Production Deployment Script
# =============================================================================
# Usage: ./scripts/deploy.sh [--build] [--backend-only] [--frontend-only]
#
# This script deploys the application to production. By default, it pulls
# pre-built images. Use --build to build images locally on the server.
# =============================================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    local level=$1
    local msg=$2
    case "$level" in
        INFO)  echo -e "${BLUE}ℹ️  $msg${NC}";;
        WARN)  echo -e "${YELLOW}⚠️  $msg${NC}";;
        ERROR) echo -e "${RED}❌ $msg${NC}";;
        OK)    echo -e "${GREEN}✅ $msg${NC}";;
    esac
}

# Parse arguments
BUILD_LOCALLY=false
BACKEND_ONLY=false
FRONTEND_ONLY=false

for arg in "$@"; do
    case $arg in
        --build)
            BUILD_LOCALLY=true
            ;;
        --backend-only)
            BACKEND_ONLY=true
            ;;
        --frontend-only)
            FRONTEND_ONLY=true
            ;;
        --help)
            echo "Usage: $0 [--build] [--backend-only] [--frontend-only]"
            echo ""
            echo "Options:"
            echo "  --build          Build images locally instead of using pre-built"
            echo "  --backend-only   Only deploy backend service"
            echo "  --frontend-only  Only deploy frontend service"
            exit 0
            ;;
    esac
done

# Navigate to project directory
cd /opt/ankurshala

# Load environment variables
if [ -f .env-prod ]; then
    export $(cat .env-prod | grep -v '^#' | xargs)
else
    log ERROR ".env-prod file not found!"
    exit 1
fi

log INFO "🚀 Starting deployment..."
echo ""

# Step 1: Pull latest code
log INFO "Step 1: Pulling latest code..."
git fetch origin
git reset --hard origin/ankurshala/prod-1.0-final
COMMIT_SHA=$(git rev-parse --short HEAD)
log OK "Code updated to commit: $COMMIT_SHA"

# Step 2: Determine what to deploy
SERVICES_TO_DEPLOY=""
if [ "$BACKEND_ONLY" = true ]; then
    SERVICES_TO_DEPLOY="backend"
elif [ "$FRONTEND_ONLY" = true ]; then
    SERVICES_TO_DEPLOY="frontend"
else
    SERVICES_TO_DEPLOY="backend frontend"
fi

# Step 3: Build or pull images
if [ "$BUILD_LOCALLY" = true ]; then
    log INFO "Step 2: Building images locally..."
    for service in $SERVICES_TO_DEPLOY; do
        log INFO "Building $service..."
        docker compose -f docker-compose.prod.yml build --no-cache $service
    done
    log OK "Images built successfully"
else
    log INFO "Step 2: Using existing images (no build)"
fi

# Step 4: Stop services gracefully
log INFO "Step 3: Stopping services..."
for service in $SERVICES_TO_DEPLOY; do
    docker compose -f docker-compose.prod.yml stop $service || true
done
log OK "Services stopped"

# Step 5: Start services
log INFO "Step 4: Starting services..."
docker compose -f docker-compose.prod.yml up -d $SERVICES_TO_DEPLOY
log OK "Services started"

# Step 6: Wait for health checks
log INFO "Step 5: Waiting for services to be healthy..."
sleep 15

# Check backend health
if [[ "$SERVICES_TO_DEPLOY" == *"backend"* ]]; then
    log INFO "Checking backend health..."
    for i in {1..30}; do
        if docker exec ankurshala_backend_prod wget -q --spider http://localhost:8080/api/actuator/health 2>/dev/null; then
            log OK "Backend is healthy"
            break
        fi
        if [ $i -eq 30 ]; then
            log WARN "Backend health check timeout - check logs"
            docker logs --tail 50 ankurshala_backend_prod
        else
            sleep 2
        fi
    done
fi

# Check frontend health
if [[ "$SERVICES_TO_DEPLOY" == *"frontend"* ]]; then
    log INFO "Checking frontend health..."
    for i in {1..30}; do
        if docker exec ankurshala_frontend_prod wget -q --spider http://localhost:3000 2>/dev/null; then
            log OK "Frontend is healthy"
            break
        fi
        if [ $i -eq 30 ]; then
            log WARN "Frontend health check timeout - check logs"
            docker logs --tail 50 ankurshala_frontend_prod
        else
            sleep 2
        fi
    done
fi

# Step 7: Verify external access
log INFO "Step 6: Verifying external access..."
sleep 5

# Check HTTPS
if curl -s -o /dev/null -w "%{http_code}" https://ankurshala.com | grep -q "200"; then
    log OK "HTTPS access working"
else
    log WARN "HTTPS access check failed"
fi

# Check API health
if curl -s -o /dev/null -w "%{http_code}" https://ankurshala.com/api/actuator/health | grep -q "200"; then
    log OK "API health endpoint working"
else
    log WARN "API health check failed"
fi

# Final status
echo ""
echo "=========================================="
log OK "Deployment completed!"
echo "=========================================="
echo ""
echo "📊 Service Status:"
docker compose -f docker-compose.prod.yml ps
echo ""
echo "🔗 URLs:"
echo "   - Homepage: https://ankurshala.com"
echo "   - Admin: https://ankurshala.com/admin/dashboard"
echo "   - API Health: https://ankurshala.com/api/actuator/health"
echo ""
