#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    local status=$1
    local message=$2
    case $status in
        "INFO") echo -e "${BLUE}ℹ️  $message${NC}" ;;
        "WARN") echo -e "${YELLOW}⚠️  $message${NC}" ;;
        "FAIL") echo -e "${RED}❌ $message${NC}" ;;
        "PASS") echo -e "${GREEN}✅ $message${NC}" ;;
    esac
}

print_status "INFO" "🚀 Smart Deployment Script - Detecting Changes"
echo "================================================================="

# Check if we're in the right directory
if [ ! -f "docker-compose.dev.yml" ]; then
    print_status "FAIL" "Not in project root directory!"
    exit 1
fi

# Check if .env exists
if [ ! -f ".env" ]; then
    print_status "WARN" ".env file not found, creating from example..."
    if [ -f "env.example" ]; then
        cp env.example .env
        print_status "INFO" "Created .env from env.example - please customize it"
    else
        print_status "FAIL" "No .env or env.example file found!"
        exit 1
    fi
fi

# Load environment variables
print_status "INFO" "Loading environment variables..."
set -a
source .env
set +a

# Function to check if files changed
check_changes() {
    local pattern=$1
    local service=$2
    
    if git diff --name-only HEAD~1 HEAD | grep -q "$pattern"; then
        echo "true"
        print_status "INFO" "Changes detected in $service"
    else
        echo "false"
    fi
}

# Detect changes
print_status "INFO" "Detecting changes since last commit..."

BACKEND_CHANGED=$(check_changes "^backend/" "Backend")
FRONTEND_CHANGED=$(check_changes "^frontend/" "Frontend")
DB_CHANGED=$(check_changes "^backend/src/main/resources/db/migration/" "Database")
NGINX_CHANGED=$(check_changes "^nginx/" "Nginx")
CONFIG_CHANGED=$(check_changes "docker-compose.dev.yml\|Makefile\|\.env" "Configuration")

# Determine what to rebuild
REBUILD_BACKEND="false"
REBUILD_FRONTEND="false"
REBUILD_NGINX="false"
RESTART_DB="false"

if [ "$BACKEND_CHANGED" = "true" ] || [ "$DB_CHANGED" = "true" ] || [ "$CONFIG_CHANGED" = "true" ]; then
    REBUILD_BACKEND="true"
fi

if [ "$FRONTEND_CHANGED" = "true" ] || [ "$CONFIG_CHANGED" = "true" ]; then
    REBUILD_FRONTEND="true"
fi

if [ "$NGINX_CHANGED" = "true" ] || [ "$CONFIG_CHANGED" = "true" ]; then
    REBUILD_NGINX="true"
fi

if [ "$DB_CHANGED" = "true" ]; then
    RESTART_DB="true"
fi

# Show what will be rebuilt
print_status "INFO" "Deployment Plan:"
echo "  Backend: $REBUILD_BACKEND"
echo "  Frontend: $REBUILD_FRONTEND"
echo "  Nginx: $REBUILD_NGINX"
echo "  Database: $RESTART_DB"

# Stop services that need rebuilding
if [ "$REBUILD_BACKEND" = "true" ] || [ "$REBUILD_FRONTEND" = "true" ] || [ "$REBUILD_NGINX" = "true" ]; then
    print_status "INFO" "Stopping services for rebuild..."
    docker-compose -f docker-compose.dev.yml down
fi

# Rebuild and start services
print_status "INFO" "Starting services..."

if [ "$REBUILD_BACKEND" = "true" ]; then
    print_status "INFO" "Rebuilding backend..."
    docker-compose -f docker-compose.dev.yml up -d --build backend
else
    print_status "INFO" "Starting backend (no rebuild needed)..."
    docker-compose -f docker-compose.dev.yml up -d backend
fi

if [ "$REBUILD_FRONTEND" = "true" ]; then
    print_status "INFO" "Rebuilding frontend..."
    docker-compose -f docker-compose.dev.yml up -d --build frontend
else
    print_status "INFO" "Starting frontend (no rebuild needed)..."
    docker-compose -f docker-compose.dev.yml up -d frontend
fi

if [ "$REBUILD_NGINX" = "true" ]; then
    print_status "INFO" "Rebuilding nginx..."
    docker-compose -f docker-compose.dev.yml up -d --build nginx
else
    print_status "INFO" "Starting nginx (no rebuild needed)..."
    docker-compose -f docker-compose.dev.yml up -d nginx
fi

# Start other services
docker-compose -f docker-compose.dev.yml up -d postgres redis

# Wait for services to be ready
print_status "INFO" "Waiting for services to be healthy..."
sleep 15

# Check service status
print_status "INFO" "Service Status:"
docker-compose -f docker-compose.dev.yml ps

# Seed data if needed
if [ "$REBUILD_BACKEND" = "true" ] || [ "$RESTART_DB" = "true" ]; then
    print_status "INFO" "Seeding demo data..."
    sleep 10
    docker-compose -f docker-compose.dev.yml exec backend curl -X POST http://localhost:8080/api/test/seed-demo-data || true
fi

print_status "PASS" "🎉 Smart deployment completed!"
echo ""
echo "📋 Access Points:"
echo "  Frontend: http://localhost:3000"
echo "  Backend API: http://localhost:8080/api"
echo "  Admin Login: siddhartha@ankurshala.com / Maza@123"
echo ""
echo "🔧 Debug Commands:"
echo "  View logs: docker-compose -f docker-compose.dev.yml logs [service]"
echo "  Restart service: docker-compose -f docker-compose.dev.yml restart [service]"
echo "  Full rebuild: docker-compose -f docker-compose.dev.yml up -d --build"
