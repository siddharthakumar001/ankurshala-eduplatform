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

print_status "INFO" "🚀 Starting AnkurShala Production Deployment (Environment Fixed)"
echo "================================================================="

# Check if .env.prod exists
if [ ! -f ".env.prod" ]; then
    print_status "FAIL" ".env.prod file not found!"
    echo "Please create .env.prod with all required variables"
    exit 1
fi

# Load and export environment variables
print_status "INFO" "Loading environment variables from .env.prod..."
set -a  # automatically export all variables
source .env.prod
set +a  # stop automatically exporting

# Verify critical environment variables are loaded
print_status "INFO" "Verifying environment variables..."
required_vars=("DB_NAME" "DB_USERNAME" "DB_PASSWORD" "JWT_SECRET" "REDIS_PASSWORD" "BANK_ENC_KEY")
missing_vars=()

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        missing_vars+=("$var")
    else
        print_status "PASS" "$var is set (${#var} chars)"
    fi
done

if [ ${#missing_vars[@]} -gt 0 ]; then
    print_status "FAIL" "Missing environment variables: ${missing_vars[*]}"
    exit 1
fi

print_status "PASS" "All required environment variables are loaded"

# Set up frontend structure
print_status "INFO" "Setting up frontend structure..."
./scripts/fix-frontend-public.sh

# Clean up any previous containers
print_status "INFO" "Cleaning up previous deployment..."
docker-compose -f docker-compose.prod.yml down -v --remove-orphans || true

# Export variables for docker-compose
export DB_NAME DB_USERNAME DB_PASSWORD JWT_SECRET REDIS_PASSWORD BANK_ENC_KEY
export SPRING_PROFILES_ACTIVE NODE_ENV NEXT_PUBLIC_API_URL
export DB_HOST DB_PORT REDIS_HOST REDIS_PORT SERVER_PORT

# Build images
print_status "INFO" "Building production images..."
docker-compose -f docker-compose.prod.yml build --no-cache

# Start services
print_status "INFO" "Starting production services..."
docker-compose -f docker-compose.prod.yml up -d

# Wait and check services
print_status "INFO" "Waiting for services to be healthy..."
sleep 30

# Check service status
print_status "INFO" "Checking service status..."
docker-compose -f docker-compose.prod.yml ps

print_status "PASS" "🎉 Deployment script completed!"
echo ""
echo "📋 Next steps:"
echo "1. Check service logs: docker-compose -f docker-compose.prod.yml logs"
echo "2. Verify services are running: docker-compose -f docker-compose.prod.yml ps"
echo "3. Test endpoints once services are healthy"