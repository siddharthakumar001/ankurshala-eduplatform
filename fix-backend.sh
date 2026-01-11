#!/bin/bash
# =============================================================================
# Backend Fix Script - Run after SSL certificate setup
# =============================================================================

set -e

echo "🔧 Fixing Backend Container Issue..."

cd /opt/ankurshala

echo "Step 1: Checking backend logs for errors..."
docker logs ankurshala_backend_prod --tail 50 2>&1 | grep -E "Started|ERROR|Exception|database" || echo "No obvious errors found"

echo ""
echo "Step 2: Checking database connection..."
docker exec ankurshala_db_prod pg_isready -U ankurshala -d ankurshala_db

echo ""
echo "Step 3: Checking Redis connection..."
docker exec ankurshala_redis_prod redis-cli -a "${REDIS_PASSWORD}" ping || echo "Redis check failed"

echo ""
echo "Step 4: Restarting backend container..."
docker compose -f docker-compose.prod.yml restart backend

echo ""
echo "Step 5: Waiting for backend to start (90 seconds)..."
sleep 30
echo "  30 seconds..."
sleep 30
echo "  60 seconds..."
sleep 30
echo "  90 seconds..."

echo ""
echo "Step 6: Checking backend health..."
docker exec ankurshala_backend_prod wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || echo "Backend health check failed"

echo ""
echo "Step 7: Checking all container statuses..."
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'

echo ""
echo "Step 8: Starting nginx..."
docker compose -f docker-compose.prod.yml up -d nginx

echo ""
echo "Step 9: Final status check..."
sleep 10
docker ps --format 'table {{.Names}}\t{{.Status}}'

echo ""
echo "✅ Done! Checking if site is accessible..."
curl -k -I https://ankurshala.com/health || echo "Site not responding yet"

echo ""
echo "If backend is still unhealthy, check logs:"
echo "  docker logs ankurshala_backend_prod --tail 100"
