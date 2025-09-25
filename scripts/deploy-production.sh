#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_status() {
    local status=$1
    local message=$2
    case $status in
        "INFO") echo -e "${GREEN}ℹ️  $message${NC}" ;;
        "WARN") echo -e "${YELLOW}⚠️  $message${NC}" ;;
        "FAIL") echo -e "${RED}❌ $message${NC}" ;;
        "PASS") echo -e "${GREEN}✅ $message${NC}" ;;
    esac
}

print_status "INFO" "🚀 Starting AnkurShala Production Deployment"
echo "================================================"

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    print_status "WARN" "Running as root. Consider using a non-root user."
fi

# Check required files
print_status "INFO" "Checking required files..."
required_files=(
    ".env.prod"
    "docker-compose.prod.yml"
    "nginx/Dockerfile"
    "nginx/nginx.conf"
    "nginx/conf.d/ankurshala.conf"
)

for file in "${required_files[@]}"; do
    if [ ! -f "$file" ]; then
        print_status "FAIL" "Required file missing: $file"
        exit 1
    fi
done
print_status "PASS" "All required files present"

# Load environment variables
if [ -f ".env.prod" ]; then
    export $(cat .env.prod | grep -v '^#' | xargs)
    print_status "PASS" "Environment variables loaded"
else
    print_status "FAIL" ".env.prod file not found"
    exit 1
fi

# Check Docker and Docker Compose
print_status "INFO" "Checking Docker installation..."
if ! command -v docker &> /dev/null; then
    print_status "FAIL" "Docker is not installed"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_status "FAIL" "Docker Compose is not installed"
    exit 1
fi
print_status "PASS" "Docker and Docker Compose are available"

# Build production images
print_status "INFO" "Building production images..."
docker-compose -f docker-compose.prod.yml build --no-cache

# Stop any existing containers
print_status "INFO" "Stopping existing containers..."
docker-compose -f docker-compose.prod.yml down || true

# Start services
print_status "INFO" "Starting production services..."
docker-compose -f docker-compose.prod.yml up -d

# Wait for services to be ready
print_status "INFO" "Waiting for services to be healthy..."
sleep 30

# Check service health
services=("postgres" "redis" "backend" "frontend" "nginx")
for service in "${services[@]}"; do
    print_status "INFO" "Checking $service health..."
    if docker-compose -f docker-compose.prod.yml ps $service | grep -q "healthy\|Up"; then
        print_status "PASS" "$service is running"
    else
        print_status "FAIL" "$service is not healthy"
        docker-compose -f docker-compose.prod.yml logs $service
        exit 1
    fi
done

# Test application endpoints
print_status "INFO" "Testing application endpoints..."

# Test Nginx health
if curl -f -s http://localhost/health > /dev/null; then
    print_status "PASS" "Nginx health check passed"
else
    print_status "FAIL" "Nginx health check failed"
fi

# Test backend health (through Nginx)
if curl -f -s http://localhost/api/actuator/health > /dev/null; then
    print_status "PASS" "Backend health check passed"
else
    print_status "FAIL" "Backend health check failed"
fi

# Test frontend (through Nginx)
if curl -f -s http://localhost > /dev/null; then
    print_status "PASS" "Frontend accessibility check passed"
else
    print_status "FAIL" "Frontend accessibility check failed"
fi

print_status "INFO" "🎉 Deployment completed successfully!"
echo ""
echo "📋 Application Status:"
echo "🌐 Frontend: http://ankurshala.com"
echo "🔧 Backend API: http://ankurshala.com/api"
echo "🏥 Health Check: http://ankurshala.com/api/actuator/health"
echo ""
echo "🔧 Management Commands:"
echo "• View logs: docker-compose -f docker-compose.prod.yml logs"
echo "• Restart: docker-compose -f docker-compose.prod.yml restart"
echo "• Stop: docker-compose -f docker-compose.prod.yml down"
echo "• Update: ./scripts/deploy-production.sh"
echo ""
print_status "INFO" "🚀 AnkurShala is now live at https://ankurshala.com"