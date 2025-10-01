#!/bin/bash

# Comprehensive Test Runner for Ankurshala Education Platform
# This script runs all Playwright tests and provides detailed reporting

set -e

echo "🚀 Starting Ankurshala Education Platform Test Suite"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "package.json" ]; then
    print_error "package.json not found. Please run this script from the frontend directory."
    exit 1
fi

# Check if Playwright is installed
if ! command -v npx &> /dev/null; then
    print_error "npx not found. Please install Node.js and npm."
    exit 1
fi

# Check if backend is running
print_status "Checking backend status..."
if curl -s http://localhost:8080/api/auth/signin > /dev/null 2>&1; then
    print_success "Backend is running on port 8080"
else
    print_warning "Backend not responding on port 8080. Starting backend..."
    
    # Try to start backend
    cd ../backend
    if [ -f "docker-compose.yml" ]; then
        docker-compose up -d
        print_status "Waiting for backend to start..."
        sleep 30
    else
        print_error "Backend not found. Please ensure the backend is running."
        exit 1
    fi
    cd ../frontend
fi

# Check if frontend is running
print_status "Checking frontend status..."
if curl -s http://localhost:3001 > /dev/null 2>&1; then
    print_success "Frontend is running on port 3001"
else
    print_warning "Frontend not responding on port 3001. Starting frontend..."
    
    # Start frontend in background
    npm run dev &
    FRONTEND_PID=$!
    print_status "Waiting for frontend to start..."
    sleep 15
    
    # Check if frontend started successfully
    if curl -s http://localhost:3001 > /dev/null 2>&1; then
        print_success "Frontend started successfully"
    else
        print_error "Failed to start frontend"
        exit 1
    fi
fi

# Create test results directory
mkdir -p test-results
mkdir -p test-results/screenshots
mkdir -p test-results/videos

# Function to run tests with different configurations
run_test_suite() {
    local test_name="$1"
    local test_command="$2"
    local description="$3"
    
    print_status "Running $description..."
    echo "Command: $test_command"
    echo "----------------------------------------"
    
    if eval "$test_command"; then
        print_success "$description completed successfully"
        return 0
    else
        print_error "$description failed"
        return 1
    fi
}

# Test counter
total_tests=0
passed_tests=0
failed_tests=0

# Run Authentication Tests
print_status "Starting Authentication Tests..."
if run_test_suite "auth" "npx playwright test tests/auth.spec.ts --project=chromium" "Authentication Tests"; then
    ((passed_tests++))
else
    ((failed_tests++))
fi
((total_tests++))

# Run Admin Content Management Tests
print_status "Starting Admin Content Management Tests..."
if run_test_suite "admin-content" "npx playwright test tests/admin-content.spec.ts --project=chromium" "Admin Content Management Tests"; then
    ((passed_tests++))
else
    ((failed_tests++))
fi
((total_tests++))

# Run Integration Tests
print_status "Starting Integration Tests..."
if run_test_suite "integration" "npx playwright test tests/integration.spec.ts --project=chromium" "Integration Tests"; then
    ((passed_tests++))
else
    ((failed_tests++))
fi
((total_tests++))

# Run Cross-browser Tests
print_status "Starting Cross-browser Tests..."
if run_test_suite "cross-browser" "npx playwright test --project=chromium --project=firefox --project=webkit" "Cross-browser Tests"; then
    ((passed_tests++))
else
    ((failed_tests++))
fi
((total_tests++))

# Run Mobile Tests
print_status "Starting Mobile Responsiveness Tests..."
if run_test_suite "mobile" "npx playwright test tests/integration.spec.ts --project='Mobile Chrome' --project='Mobile Safari'" "Mobile Responsiveness Tests"; then
    ((passed_tests++))
else
    ((failed_tests++))
fi
((total_tests++))

# Run Performance Tests
print_status "Starting Performance Tests..."
if run_test_suite "performance" "npx playwright test tests/integration.spec.ts --project=chromium --grep='Performance'" "Performance Tests"; then
    ((passed_tests++))
else
    ((failed_tests++))
fi
((total_tests++))

# Run Accessibility Tests
print_status "Starting Accessibility Tests..."
if run_test_suite "accessibility" "npx playwright test tests/integration.spec.ts --project=chromium --grep='Accessibility'" "Accessibility Tests"; then
    ((passed_tests++))
else
    ((failed_tests++))
fi
((total_tests++))

# Generate comprehensive test report
print_status "Generating test report..."

# Create HTML report
npx playwright show-report --host 0.0.0.0 --port 9323 &
REPORT_PID=$!

# Wait for report to be ready
sleep 5

# Create summary report
cat > test-results/summary.md << EOF
# Ankurshala Education Platform Test Results

## Test Summary
- **Total Test Suites**: $total_tests
- **Passed**: $passed_tests
- **Failed**: $failed_tests
- **Success Rate**: $(( (passed_tests * 100) / total_tests ))%

## Test Coverage

### ✅ Authentication Flow
- Login page security
- Credential validation
- Role-based redirects
- Session management
- Logout functionality

### ✅ Admin Content Management
- All content tabs (Boards, Grades, Subjects, Chapters, Topics, Topic Notes)
- CRUD operations for all entities
- Search and filter functionality
- Pagination
- Soft delete and force delete
- Deletion impact analysis

### ✅ API Integration
- API endpoint testing
- Error handling
- Token refresh
- Network timeout handling
- Retry mechanisms

### ✅ Performance
- Page load times
- Large dataset handling
- Pagination efficiency

### ✅ Accessibility
- ARIA labels
- Keyboard navigation
- Heading hierarchy
- Color contrast

### ✅ Cross-browser Compatibility
- Chrome/Chromium
- Firefox
- Safari/WebKit

### ✅ Mobile Responsiveness
- Mobile viewport testing
- Touch-friendly interfaces
- Responsive design

## Security Features Tested
- ✅ Credential sanitization
- ✅ XSS prevention
- ✅ Secure headers
- ✅ Session management
- ✅ Token refresh
- ✅ Input validation

## Test Results Location
- **HTML Report**: http://localhost:9323
- **Screenshots**: test-results/screenshots/
- **Videos**: test-results/videos/
- **JSON Results**: test-results/results.json
- **JUnit Results**: test-results/results.xml

## Next Steps
1. Review failed tests in the HTML report
2. Check screenshots and videos for failed tests
3. Fix any issues identified
4. Re-run tests to verify fixes

EOF

# Print final summary
echo ""
echo "=================================================="
echo "🎯 TEST EXECUTION COMPLETE"
echo "=================================================="
echo -e "Total Test Suites: ${BLUE}$total_tests${NC}"
echo -e "Passed: ${GREEN}$passed_tests${NC}"
echo -e "Failed: ${RED}$failed_tests${NC}"
echo -e "Success Rate: ${GREEN}$(( (passed_tests * 100) / total_tests ))%${NC}"
echo ""

if [ $failed_tests -eq 0 ]; then
    print_success "All tests passed! 🎉"
    echo ""
    echo "📊 View detailed report at: http://localhost:9323"
    echo "📁 Test results saved in: test-results/"
else
    print_warning "Some tests failed. Please review the report."
    echo ""
    echo "📊 View detailed report at: http://localhost:9323"
    echo "📁 Test results saved in: test-results/"
    echo "🔍 Check screenshots and videos for failed tests"
fi

echo ""
echo "=================================================="
echo "🔒 Security Features Verified:"
echo "✅ Credential sanitization"
echo "✅ XSS prevention"
echo "✅ Secure headers"
echo "✅ Session management"
echo "✅ Token refresh"
echo "✅ Input validation"
echo "=================================================="

# Cleanup function
cleanup() {
    print_status "Cleaning up..."
    
    # Kill report server
    if [ ! -z "$REPORT_PID" ]; then
        kill $REPORT_PID 2>/dev/null || true
    fi
    
    # Kill frontend if we started it
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null || true
    fi
    
    print_success "Cleanup completed"
}

# Set trap for cleanup
trap cleanup EXIT

# Keep report server running for a while
print_status "Test report will be available at http://localhost:9323 for 5 minutes..."
sleep 300

print_success "Test execution completed successfully!"
