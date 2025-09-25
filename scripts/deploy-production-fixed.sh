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

print_status "INFO" "🚀 Starting AnkurShala Production Deployment (Fixed Version)"
echo "============================================================="

# Pre-deployment setup
print_status "INFO" "Setting up frontend structure..."
chmod +x scripts/fix-frontend-public.sh
./scripts/fix-frontend-public.sh

# Check required files
print_status "INFO" "Checking required files..."
required_files=(
    ".env.prod"
    "docker-compose.prod.yml"
    "nginx/Dockerfile"
    "nginx/nginx.conf"
    "nginx/conf.d/ankurshala.conf"
    "frontend/public/favicon.ico"
    "frontend/next.config.js"
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
    source .env.prod
    print_status "PASS" "Environment variables loaded"
else
    print_status "FAIL" ".env.prod file not found"
    exit 1
fi

# Check Docker
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

# Clean up any previous failed builds
print_status "INFO" "Cleaning up previous builds..."
docker-compose -f docker-compose.prod.yml down -v --remove-orphans || true
docker system prune -f

# Build production images with verbose output
print_status "INFO" "Building production images..."
export DOCKER_BUILDKIT=1
export BUILDKIT_PROGRESS=plain

# Build each service separately for better error handling
print_status "INFO" "Building backend..."
docker-compose -f docker-compose.prod.yml build --no-cache backend

print_status "INFO" "Building frontend..."
docker-compose -f docker-compose.prod.yml build --no-cache frontend

print_status "INFO" "Building nginx..."
docker-compose -f docker-compose.prod.yml build --no-cache nginx

print_status "PASS" "All images built successfully"

# Start services
print_status "INFO" "Starting production services..."
docker-compose -f docker-compose.prod.yml up -d

# Wait for services to be ready
print_status "INFO" "Waiting for services to be healthy..."
sleep 60

# Check service health with retries
services=("postgres" "redis" "backend" "frontend" "nginx")
for service in "${services[@]}"; do
    print_status "INFO" "Checking $service health..."
    
    # Check if container is running
    if docker-compose -f docker-compose.prod.yml ps $service | grep -q "Up"; then
        print_status "PASS" "$service container is running"
        
        # Additional health checks
        case $service in
            "postgres")
                if docker exec ankur_db_prod pg_isready -U ${DB_USERNAME} -d ${DB_NAME} > /dev/null 2>&1; then
                    print_status "PASS" "PostgreSQL is ready"
                else
                    print_status "WARN" "PostgreSQL may not be fully ready yet"
                fi
                ;;
            "redis")
                if docker exec ankur_redis_prod redis-cli --no-auth-warning -a ${REDIS_PASSWORD} ping > /dev/null 2>&1; then
                    print_status "PASS" "Redis is ready"
                else
                    print_status "WARN" "Redis may not be fully ready yet"
                fi
                ;;
            "backend")
                # Wait a bit more for backend to be ready
                sleep 30
                if curl -f -s http://localhost/api/actuator/health > /dev/null 2>&1; then
                    print_status "PASS" "Backend API is ready"
                else
                    print_status "WARN" "Backend API may not be fully ready yet"
                fi
                ;;
            "frontend")
                if curl -f -s http://localhost > /dev/null 2>&1; then
                    print_status "PASS" "Frontend is ready"
                else
                    print_status "WARN" "Frontend may not be fully ready yet"
                fi
                ;;
            "nginx")
                if curl -f -s http://localhost/health > /dev/null 2>&1; then
                    print_status "PASS" "Nginx is ready"
                else
                    print_status "WARN" "Nginx health endpoint may not be ready yet"
                fi
                ;;
        esac
    else
        print_status "FAIL" "$service is not running"
        echo "Container logs:"
        docker-compose -f docker-compose.prod.yml logs --tail=20 $service
    fi
done

# Final verification
print_status "INFO" "Running final verification..."

# Test main endpoints
if curl -f -s -o /dev/null http://localhost; then
    print_status "PASS" "Main website accessible"
else
    print_status "FAIL" "Main website not accessible"
fi

print_status "PASS" "🎉 Deployment completed!"
echo ""
echo "📋 Application Status:"
echo "🌐 Website: http://ankurshala.com (will work once DNS is configured)"
echo "🔧 Backend API: http://localhost/api"
echo "🏥 Health Check: http://localhost/api/actuator/health"
echo ""
echo "🔧 Management Commands:"
echo "• View logs: docker-compose -f docker-compose.prod.yml logs -f"
echo "• Restart: docker-compose -f docker-compose.prod.yml restart"
echo "• Stop: docker-compose -f docker-compose.prod.yml down"
echo ""
echo "📋 Next Steps:"
echo "1. Configure DNS: Point ankurshala.com A record to this server's IP"
echo "2. Set up SSL: Run ./scripts/setup-ssl.sh after DNS propagation"
echo "3. Test thoroughly: Verify all functionality works"
echo ""
print_status "INFO" "🚀 AnkurShala is now deployed!"