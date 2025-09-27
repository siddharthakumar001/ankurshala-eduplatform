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

print_status "INFO" "🚀 Production Deployment Fix"
echo "================================================================="

# Stop all existing containers
print_status "INFO" "Stopping all existing containers..."
docker stop $(docker ps -q) 2>/dev/null || true
docker rm $(docker ps -aq) 2>/dev/null || true

# Fix environment variables for seeding
print_status "INFO" "Fixing environment variables for seeding..."
sed -i 's/DEMO_SEED_ON_START=false/DEMO_SEED_ON_START=true/' .env-prod
sed -i 's/DEMO_FORCE=false/DEMO_FORCE=true/' .env-prod

# Load environment variables
print_status "INFO" "Loading environment variables..."
set -a
source .env-prod
set +a

# Export variables for docker-compose
export DB_NAME DB_USERNAME DB_PASSWORD JWT_SECRET REDIS_PASSWORD BANK_ENC_KEY
export SPRING_PROFILES_ACTIVE NODE_ENV NEXT_PUBLIC_API_URL
export DB_HOST DB_PORT REDIS_HOST REDIS_PORT SERVER_PORT
export DEMO_SEED_ON_START DEMO_ENV DEMO_FORCE

# Build and start production services
print_status "INFO" "Building and starting production services..."
docker-compose -f docker-compose.prod.yml up -d --build

# Wait for services
print_status "INFO" "Waiting for services to be ready..."
sleep 30

# Check status
print_status "INFO" "Service Status:"
docker-compose -f docker-compose.prod.yml ps

# Check seeding
print_status "INFO" "Checking database seeding..."
sleep 15

if docker logs ankur_backend_prod 2>&1 | grep -q "Demo data seeding completed successfully"; then
    print_status "PASS" "Database seeding completed successfully"
else
    print_status "WARN" "Checking seeding logs..."
    docker logs ankur_backend_prod | grep -i seed
fi

print_status "PASS" "🎉 Production deployment completed!"
echo ""
echo "📋 Access Points:"
echo "  Frontend: https://ankurshala.com"
echo "  Backend API: https://ankurshala.com/api"
echo "  Admin Login: siddhartha@ankurshala.com / Maza@123"
