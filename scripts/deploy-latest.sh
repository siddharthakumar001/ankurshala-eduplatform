#!/bin/bash
set -Eeuo pipefail

# 🚀 Deploy Latest Code to Production Server
# This script pulls latest code, builds new images, and deploys

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    case "$1" in
        INFO) echo -e "${BLUE}ℹ️  $2${NC}";;
        WARN) echo -e "${YELLOW}⚠️  $2${NC}";;
        FAIL) echo -e "${RED}❌ $2${NC}";;
        PASS) echo -e "${GREEN}✅ $2${NC}";;
    esac
}

# Navigate to project directory
cd /opt/ankurshala

log INFO "🚀 Starting deployment of latest code..."

# Step 1: Pull latest code
log INFO "Step 1: Pulling latest code from GitHub..."
git fetch origin
git checkout ankurshala/prod-1.0-final
git pull origin ankurshala/prod-1.0-final

COMMIT_SHA=$(git rev-parse --short HEAD)
log PASS "Latest code pulled. Commit: $COMMIT_SHA"

# Step 2: Get commit hashes for tagging
BACKEND_SHA=$(git rev-parse --short HEAD:backend 2>/dev/null || echo "$COMMIT_SHA")
FRONTEND_SHA=$(git rev-parse --short HEAD:frontend 2>/dev/null || echo "$COMMIT_SHA")

# Use full commit SHA for images
BACKEND_TAG="prod-$COMMIT_SHA"
FRONTEND_TAG="prod-$COMMIT_SHA"

log INFO "Step 2: Building Docker images with tags..."
log INFO "  Backend tag: $BACKEND_TAG"
log INFO "  Frontend tag: $FRONTEND_TAG"

# Step 3: Build backend image
log INFO "Step 3: Building backend image..."
export BACKEND_TAG="$BACKEND_TAG"
docker compose -f docker-compose.prod.yml build --no-cache backend

# Step 4: Build frontend image
log INFO "Step 4: Building frontend image..."
export FRONTEND_TAG="$FRONTEND_TAG"
docker compose -f docker-compose.prod.yml build --no-cache frontend

# Step 5: Stop existing containers
log INFO "Step 5: Stopping existing containers..."
docker compose -f docker-compose.prod.yml stop backend frontend || true

# Step 6: Start services with new images
log INFO "Step 6: Starting services with new images..."
export BACKEND_TAG="$BACKEND_TAG"
export FRONTEND_TAG="$FRONTEND_TAG"
docker compose -f docker-compose.prod.yml up -d backend frontend

# Step 7: Wait for health checks
log INFO "Step 7: Waiting for services to be healthy..."
sleep 10

# Check backend health
log INFO "Checking backend health..."
for i in {1..30}; do
    if docker exec ankurshala_backend_prod wget -q --spider http://localhost:8080/api/actuator/health 2>/dev/null; then
        log PASS "Backend is healthy"
        break
    fi
    if [ $i -eq 30 ]; then
        log WARN "Backend health check timeout"
    else
        sleep 2
    fi
done

# Check frontend health
log INFO "Checking frontend health..."
for i in {1..30}; do
    if docker exec ankurshala_frontend_prod wget -q --spider http://localhost:3000 2>/dev/null; then
        log PASS "Frontend is healthy"
        break
    fi
    if [ $i -eq 30 ]; then
        log WARN "Frontend health check timeout"
    else
        sleep 2
    fi
done

# Step 8: Reload nginx
log INFO "Step 8: Reloading nginx..."
docker compose -f docker-compose.prod.yml exec -T nginx nginx -s reload 2>/dev/null || log WARN "Nginx reload failed (may need restart)"

# Step 9: Verify deployment
log INFO "Step 9: Verifying deployment..."
echo ""
log INFO "Current running images:"
docker images | grep -E "ankurshala/(backend|frontend)" | grep "prod-$COMMIT_SHA" || log WARN "New images not found in list"

echo ""
log INFO "Container status:"
docker compose -f docker-compose.prod.yml ps backend frontend

echo ""
log PASS "🎉 Deployment completed!"
log INFO "Commit deployed: $COMMIT_SHA"
log INFO "Backend image: ankurshala/backend:$BACKEND_TAG"
log INFO "Frontend image: ankurshala/frontend:$FRONTEND_TAG"
echo ""
log INFO "Application should be available at: https://ankurshala.com"

