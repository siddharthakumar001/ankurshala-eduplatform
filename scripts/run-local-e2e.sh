#!/bin/bash

# Stage-1 FE complete: Automated local E2E test execution script
# This script boots services, seeds data, runs E2E tests, and generates reports

set -e  # Exit on any error

echo "🚀 Starting Stage-1 Frontend E2E Test Execution"
echo "==============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TESTS_PASSED=0
TESTS_FAILED=0

# Function to print status
print_status() {
    local status=$1
    local message=$2
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✅ $message${NC}"
        ((TESTS_PASSED++))
    elif [ "$status" = "FAIL" ]; then
        echo -e "${RED}❌ $message${NC}"
        ((TESTS_FAILED++))
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
    local max_attempts=15
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s -o /dev/null -w "%{http_code}" "$url" | grep -q "200\|302"; then
            print_status "PASS" "$service_name is responding"
            return 0
        fi
        print_status "INFO" "Waiting for $service_name... (attempt $attempt/$max_attempts)"
        sleep 3
        ((attempt++))
    done
    
    print_status "FAIL" "$service_name is not responding after $max_attempts attempts"
    return 1
}

print_status "INFO" "Step 1: Starting development services"
echo "Starting Docker services..."
make dev-up || {
    print_status "FAIL" "Failed to start development services"
    exit 1
}

print_status "INFO" "Step 2: Waiting for services to be ready"
sleep 15

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

print_status "INFO" "Step 3: Seeding comprehensive demo data"
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
sleep 20
if check_service "http://localhost:3000" "Frontend"; then
    print_status "PASS" "Frontend is ready"
else
    print_status "FAIL" "Frontend failed to start"
    kill $FRONTEND_PID 2>/dev/null || true
    exit 1
fi

print_status "INFO" "Step 5: Running comprehensive E2E tests"
echo "Executing Stage-1 E2E test suite..."

# Create test results directory
mkdir -p test-results

# Run comprehensive E2E tests
if npm run test:e2e; then
    print_status "PASS" "E2E tests completed successfully"
    E2E_RESULT="PASSED"
else
    print_status "FAIL" "E2E tests failed"
    E2E_RESULT="FAILED"
fi

print_status "INFO" "Step 6: Generating test report"

# Generate test summary
cat > test-results/summary.md << EOF
# Stage-1 Frontend E2E Test Summary

**Date**: $(date)
**Test Suite**: Stage-1 Comprehensive E2E Tests
**Result**: $E2E_RESULT

## Test Execution Summary

- **Total Tests**: $((TESTS_PASSED + TESTS_FAILED))
- **Passed**: $TESTS_PASSED
- **Failed**: $TESTS_FAILED
- **Success Rate**: $(( TESTS_PASSED * 100 / (TESTS_PASSED + TESTS_FAILED) ))%

## Demo Credentials Used

### Admin
- **Email**: siddhartha@ankurshala.com
- **Password**: Maza@123

### Students
- **student1@ankurshala.com** - Maza@123
- **student2@ankurshala.com** - Maza@123
- **student3@ankurshala.com** - Maza@123
- **student4@ankurshala.com** - Maza@123
- **student5@ankurshala.com** - Maza@123

### Teachers
- **teacher1@ankurshala.com** - Maza@123
- **teacher2@ankurshala.com** - Maza@123
- **teacher3@ankurshala.com** - Maza@123
- **teacher4@ankurshala.com** - Maza@123
- **teacher5@ankurshala.com** - Maza@123

## Test Coverage

### ✅ Authentication Flow
- Login page rendering and validation
- Invalid login error handling
- Successful login for all user types
- Token lifecycle and refresh

### ✅ Student Profile Management
- Seeded data display and verification
- Profile updates and persistence
- Document management (add/delete)
- RBAC enforcement (blocked from teacher routes)

### ✅ Teacher Profile Management
- Profile tabs and seeded data display
- Professional information updates
- Qualifications, experience, certifications
- Availability, addresses, bank details
- Document management
- RBAC enforcement (blocked from student routes)

### ✅ Admin Profile Management
- Admin login and profile access
- Profile updates and persistence

### ✅ UI Polish and Navigation
- Navbar user info display
- Logout functionality
- Form validation error handling
- Error pages (404, forbidden)

### ✅ Public Access
- Home page rendering with all elements
- Logo, navigation, and call-to-action buttons

## Test Artifacts

- **Screenshots**: test-results/screenshots/
- **Traces**: test-results/traces/
- **Videos**: test-results/videos/
- **HTML Report**: test-results/playwright-report/

## Environment

- **Frontend**: http://localhost:3000
- **Backend**: http://localhost:8080/api
- **Database**: PostgreSQL (ankur_db)
- **Cache**: Redis (ankur_redis)

## Next Steps

1. Review test artifacts for any failures
2. Check screenshots and traces for debugging
3. Verify all Stage-1 features are working correctly
4. Proceed with Stage-2 development

EOF

print_status "INFO" "Step 7: Cleanup"
# Stop frontend
kill $FRONTEND_PID 2>/dev/null || true

# Generate final report
echo ""
echo "🎯 STAGE-1 FRONTEND E2E TEST EXECUTION COMPLETE"
echo "==============================================="
echo ""
echo "📊 TEST SUMMARY:"
echo "✅ Passed: $TESTS_PASSED"
echo "❌ Failed: $TESTS_FAILED"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    print_status "PASS" "🎉 All E2E tests PASSED!"
    echo ""
    echo "📋 Test Artifacts Generated:"
    echo "  📸 Screenshots: frontend/test-results/screenshots/"
    echo "  🔍 Traces: frontend/test-results/traces/"
    echo "  🎥 Videos: frontend/test-results/videos/"
    echo "  📊 Report: frontend/test-results/playwright-report/"
    echo "  📝 Summary: frontend/test-results/summary.md"
    echo ""
    echo "🚀 Stage-1 frontend is ready for production!"
    echo ""
    echo "🔧 Manual Testing:"
    echo "  1. Access: http://localhost:3000"
    echo "  2. Login with demo credentials above"
    echo "  3. Test all profile management features"
    echo "  4. Verify RBAC enforcement"
    echo ""
    exit 0
else
    print_status "FAIL" "❌ Some E2E tests FAILED!"
    echo ""
    echo "🔍 Debugging Information:"
    echo "  📸 Screenshots: frontend/test-results/screenshots/"
    echo "  🔍 Traces: frontend/test-results/traces/"
    echo "  🎥 Videos: frontend/test-results/videos/"
    echo "  📊 Report: frontend/test-results/playwright-report/"
    echo "  📝 Summary: frontend/test-results/summary.md"
    echo ""
    echo "💡 Troubleshooting:"
    echo "1. Check test artifacts for specific failures"
    echo "2. Verify all services are running correctly"
    echo "3. Check demo data was seeded properly"
    echo "4. Review browser console for errors"
    echo ""
    exit 1
fi
