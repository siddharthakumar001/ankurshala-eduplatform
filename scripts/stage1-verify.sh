#!/bin/bash

# Stage-1 Verification Script
# This script automates the complete Stage-1 verification process

set -e  # Exit on any error

echo "🚀 Starting Stage-1 Verification Process"
echo "======================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
PASSED=0
FAILED=0

# Function to print status
print_status() {
    local status=$1
    local message=$2
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✅ $message${NC}"
        ((PASSED++))
    elif [ "$status" = "FAIL" ]; then
        echo -e "${RED}❌ $message${NC}"
        ((FAILED++))
    elif [ "$status" = "INFO" ]; then
        echo -e "${BLUE}ℹ️  $message${NC}"
    elif [ "$status" = "WARN" ]; then
        echo -e "${YELLOW}⚠️  $message${NC}"
    fi
}

# Function to check if a service is responding
check_service() {
    local url=$1
    local service_name=$2
    local max_attempts=10
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s -o /dev/null -w "%{http_code}" "$url" | grep -q "200\|302"; then
            print_status "PASS" "$service_name is responding"
            return 0
        fi
        print_status "INFO" "Waiting for $service_name... (attempt $attempt/$max_attempts)"
        sleep 5
        ((attempt++))
    done
    
    print_status "FAIL" "$service_name is not responding after $max_attempts attempts"
    return 1
}

# Function to run command with timeout
run_with_timeout() {
    local timeout_duration=$1
    shift
    timeout $timeout_duration "$@"
}

print_status "INFO" "Step 1: Starting development services"
echo "Starting Docker services..."
make dev-up || {
    print_status "FAIL" "Failed to start development services"
    exit 1
}

print_status "INFO" "Step 2: Waiting for services to be ready"
sleep 10

# Check backend health
print_status "INFO" "Checking backend health..."
if check_service "http://localhost:8080/api/actuator/health" "Backend"; then
    print_status "PASS" "Backend is healthy"
else
    print_status "FAIL" "Backend health check failed"
    exit 1
fi

# Check database
print_status "INFO" "Checking database connection..."
if docker exec ankur_db pg_isready -U ankur -d ankurshala > /dev/null 2>&1; then
    print_status "PASS" "Database is ready"
else
    print_status "FAIL" "Database is not ready"
    exit 1
fi

# Check Redis
print_status "INFO" "Checking Redis connection..."
if docker exec ankur_redis redis-cli ping > /dev/null 2>&1; then
    print_status "PASS" "Redis is ready"
else
    print_status "FAIL" "Redis is not ready"
    exit 1
fi

print_status "INFO" "Step 3: Seeding demo data"
if make seed-dev; then
    print_status "PASS" "Demo data seeded successfully"
else
    print_status "WARN" "Demo data seeding failed (may already exist)"
fi

print_status "INFO" "Step 4: Starting frontend development server"
cd frontend

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    print_status "INFO" "Installing frontend dependencies..."
    npm install
fi

# Start frontend in background
print_status "INFO" "Starting frontend server..."
npm run dev > /dev/null 2>&1 &
FRONTEND_PID=$!

# Wait for frontend to be ready
sleep 15
if check_service "http://localhost:3000" "Frontend"; then
    print_status "PASS" "Frontend is ready"
else
    print_status "FAIL" "Frontend failed to start"
    kill $FRONTEND_PID 2>/dev/null || true
    exit 1
fi

print_status "INFO" "Step 5: Running E2E tests"
echo "Executing comprehensive E2E test suite..."

# Run E2E tests with timeout
if run_with_timeout 300 npm run test:e2e; then
    print_status "PASS" "E2E tests completed successfully"
    E2E_RESULT="PASSED"
else
    print_status "FAIL" "E2E tests failed"
    E2E_RESULT="FAILED"
fi

print_status "INFO" "Step 6: Testing key functionalities manually"

# Test backend endpoints
print_status "INFO" "Testing backend endpoints..."

# Test health endpoint
if curl -s http://localhost:8080/api/actuator/health | grep -q "UP"; then
    print_status "PASS" "Health endpoint working"
else
    print_status "FAIL" "Health endpoint not working"
fi

# Test authentication endpoints
print_status "INFO" "Testing authentication endpoints..."
AUTH_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/signin \
    -H "Content-Type: application/json" \
    -d '{"email":"student@demo.test","password":"Student@1234"}')

if echo "$AUTH_RESPONSE" | grep -q "accessToken"; then
    print_status "PASS" "Authentication endpoint working"
    ACCESS_TOKEN=$(echo "$AUTH_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
else
    print_status "FAIL" "Authentication endpoint not working"
fi

# Test protected endpoint
if [ ! -z "$ACCESS_TOKEN" ]; then
    PROFILE_RESPONSE=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" \
        http://localhost:8080/api/student/profile)
    
    if echo "$PROFILE_RESPONSE" | grep -q -E '"firstName"|"lastName"|"id"'; then
        print_status "PASS" "Protected endpoint working"
    else
        print_status "FAIL" "Protected endpoint not working"
    fi
fi

print_status "INFO" "Step 7: Cleanup"
# Stop frontend
kill $FRONTEND_PID 2>/dev/null || true

# Generate final report
echo ""
echo "🎯 STAGE-1 VERIFICATION COMPLETE"
echo "================================="
echo ""
echo "📊 TEST SUMMARY:"
echo "✅ Passed: $PASSED"
echo "❌ Failed: $FAILED"
echo ""

if [ $FAILED -eq 0 ]; then
    print_status "PASS" "🎉 Stage-1 verification SUCCESSFUL!"
    echo ""
    echo "🚀 Your AnkurShala application is ready!"
    echo ""
    echo "📋 Next Steps:"
    echo "1. Access the application at: http://localhost:3000"
    echo "2. Use demo credentials:"
    echo "   • Student: student@demo.test / Student@1234"
    echo "   • Teacher: teacher@demo.test / Teacher@1234"
    echo "   • Admin: admin@demo.test / Admin@1234"
    echo ""
    echo "🔧 Development Commands:"
    echo "   make dev-up     - Start all services"
    echo "   make fe-dev     - Start frontend only"
    echo "   make seed-dev   - Seed demo data"
    echo "   make health     - Check service status"
    echo ""
    exit 0
else
    print_status "FAIL" "❌ Stage-1 verification FAILED!"
    echo ""
    echo "🔍 Issues found: $FAILED"
    echo "Please check the logs above and fix the failing components."
    echo ""
    echo "💡 Troubleshooting:"
    echo "1. Check service logs: make dev-logs"
    echo "2. Verify environment variables in .env"
    echo "3. Ensure all dependencies are installed"
    echo "4. Check Docker daemon is running"
    echo ""
    exit 1
fi
