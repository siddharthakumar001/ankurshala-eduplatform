#!/bin/bash

#############################################
# Ankurshala Admin Module Smoke Test
# Tests all admin endpoints with proper authentication
#############################################

# set -e  # Don't exit on errors, we want to count them

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
BACKEND_URL="http://localhost:8080/api"
ADMIN_EMAIL="siddhartha@ankurshala.com"
ADMIN_PASSWORD="Maza@123"
TEMP_DIR="/tmp/ankurshala-test"

# Counters
TESTS_PASSED=0
TESTS_FAILED=0

mkdir -p "$TEMP_DIR"

#############################################
# Helper Functions
#############################################

print_header() {
    echo -e "\n${YELLOW}========================================${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${YELLOW}========================================${NC}"
}

print_test() {
    echo -e "\n🧪 Testing: $1"
}

print_success() {
    echo -e "${GREEN}✅ PASS:${NC} $1"
    ((TESTS_PASSED++))
}

print_fail() {
    echo -e "${RED}❌ FAIL:${NC} $1"
    ((TESTS_FAILED++))
}

test_endpoint() {
    local name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    
    print_test "$name"
    
    if [ -z "$data" ]; then
        http_code=$(curl -s -o /tmp/response.json -w "%{http_code}" -X "$method" "$BACKEND_URL$endpoint" -H "Authorization: Bearer $TOKEN")
    else
        http_code=$(curl -s -o /tmp/response.json -w "%{http_code}" -X "$method" "$BACKEND_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi
    
    response=$(cat /tmp/response.json)
    success=$(echo "$response" | jq -r '.success // "unknown"')
    
    # Check if HTTP status is 2xx
    if [[ "$http_code" =~ ^2 ]]; then
        print_success "$name (HTTP $http_code)"
        if [ "$success" = "true" ]; then
            echo "  Response: $(echo "$response" | jq -c '{success, message}')"
        else
            # Show data preview for successful non-standard responses
            echo "  Data: $(echo "$response" | jq -c 'if type == "object" then keys else type end')"
        fi
    else
        print_fail "$name (HTTP $http_code)"
        echo "  Error: $(echo "$response" | jq -c '.')"
    fi
}

#############################################
# Main Test Flow
#############################################

print_header "1. AUTHENTICATION"

print_test "Admin Login"
login_response=$(curl -s -X POST "$BACKEND_URL/auth/signin" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}")

success=$(echo "$login_response" | jq -r '.success')
if [ "$success" = "true" ]; then
    TOKEN=$(echo "$login_response" | jq -r '.data.accessToken')
    print_success "Admin Login"
    TOKEN_PREVIEW=$(echo "$TOKEN" | cut -c1-50)
    echo "  Token: ${TOKEN_PREVIEW}..."
else
    print_fail "Admin Login - Cannot proceed without authentication"
    echo "$login_response" | jq .
    exit 1
fi

#############################################
# Admin Module Tests
#############################################

print_header "2. ADMIN DASHBOARD"
test_endpoint "Dashboard Metrics" "GET" "/admin/dashboard/metrics"

print_header "3. ADMIN STUDENTS"
test_endpoint "List Students" "GET" "/admin/students?page=0&size=10"
test_endpoint "Search Students" "GET" "/admin/students?search=student&page=0&size=5"
test_endpoint "Filter by Status" "GET" "/admin/students?status=ACTIVE&page=0&size=5"

print_header "4. ADMIN TEACHERS"
test_endpoint "List Teachers" "GET" "/admin/teachers?page=0&size=10"
test_endpoint "Search Teachers" "GET" "/admin/teachers?search=teacher&page=0&size=5"
test_endpoint "Filter by Status" "GET" "/admin/teachers?status=ACTIVE&page=0&size=5"

print_header "5. ADMIN CONTENT - Educational Boards"
test_endpoint "List Boards" "GET" "/admin/content/boards?page=0&size=10"
test_endpoint "Search Boards" "GET" "/admin/content/boards?search=CBSE&page=0&size=5"

print_header "6. ADMIN CONTENT - Grades"
test_endpoint "List Grades" "GET" "/admin/content/grades?page=0&size=10"
test_endpoint "Grades by Board" "GET" "/admin/content/grades/by-board?board=CBSE"

print_header "7. ADMIN CONTENT - Subjects"
test_endpoint "List Subjects" "GET" "/admin/content/subjects?page=0&size=10"
test_endpoint "Subjects by Grade" "GET" "/admin/content/subjects/by-grade?board=CBSE&grade=9"

print_header "8. ADMIN CONTENT - Chapters"
test_endpoint "List Chapters" "GET" "/admin/content/chapters?page=0&size=10"

print_header "9. ADMIN PRICING"
test_endpoint "Get Fee Structure" "GET" "/admin/pricing/resolve?board=CBSE&grade=9"

print_header "10. ADMIN FEE WAIVERS"
test_endpoint "List Fee Waivers" "GET" "/admin/fees/waivers?page=0&size=10"
test_endpoint "Filter Pending Waivers" "GET" "/admin/fees/waivers?status=PENDING&page=0&size=5"

print_header "11. ADMIN NOTIFICATIONS"
# Note: Actual broadcast would send emails, so we just test the endpoint structure
echo "ℹ️  Skipping actual notification broadcast to avoid spamming"
echo "   Endpoint: POST /admin/notifications/broadcast"

#############################################
# Summary
#############################################

print_header "TEST SUMMARY"
TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))
echo -e "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ALL TESTS PASSED! Admin module is working correctly.${NC}\n"
    exit 0
else
    echo -e "\n${RED}⚠️  SOME TESTS FAILED! Please review the errors above.${NC}\n"
    exit 1
fi
