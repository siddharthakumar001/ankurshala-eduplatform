#!/bin/bash

# Student Module Smoke Test Script
# Tests all student profile endpoints to ensure proper functionality

set -e

BASE_URL="http://localhost:8080/api"
STUDENT_EMAIL="student1@ankurshala.com"
STUDENT_PASSWORD="Maza@123"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print test results
print_result() {
    local test_name="$1"
    local status_code="$2"
    local expected="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if [ "$status_code" -eq "$expected" ]; then
        echo -e "${GREEN}✓${NC} $test_name (HTTP $status_code)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗${NC} $test_name (Expected: $expected, Got: $status_code)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

echo "========================================"
echo "Student Module Smoke Test"
echo "========================================"
echo ""

# 1. Login as Student
echo "1. Authenticating as Student..."
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/signin" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$STUDENT_EMAIL\",\"password\":\"$STUDENT_PASSWORD\"}")

STATUS_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

print_result "Student Login" "$STATUS_CODE" 200

if [ "$STATUS_CODE" -ne 200 ]; then
    echo -e "${RED}Failed to login. Exiting...${NC}"
    exit 1
fi

TOKEN=$(echo "$RESPONSE_BODY" | jq -r '.data.accessToken')
if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo -e "${RED}No token received. Exiting...${NC}"
    echo "Response: $RESPONSE_BODY"
    exit 1
fi

echo -e "${GREEN}Token obtained successfully${NC}"
echo ""

# 2. Get Student Profile
echo "2. Testing Profile Endpoints..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/student/profile" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /student/profile" "$STATUS_CODE" 200

# 3. Get Student Dashboard
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/student/dashboard" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /student/dashboard" "$STATUS_CODE" 200

# 4. Get Student Documents
echo ""
echo "3. Testing Documents Endpoints..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/student/profile/documents" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /student/profile/documents" "$STATUS_CODE" 200

# 5. Add Student Document
DOCUMENT_PAYLOAD='{
  "documentType": "IDENTITY_PROOF",
  "documentName": "Aadhaar Card",
  "documentUrl": "https://example.com/documents/aadhaar.pdf",
  "verificationStatus": "PENDING"
}'

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/student/profile/documents" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$DOCUMENT_PAYLOAD")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')
print_result "POST /student/profile/documents" "$STATUS_CODE" 200

# Extract document ID if creation was successful
DOCUMENT_ID=""
if [ "$STATUS_CODE" -eq 200 ]; then
    DOCUMENT_ID=$(echo "$RESPONSE_BODY" | jq -r '.id')
fi

# Cleanup: Delete created test data
echo ""
echo "4. Cleaning up test data..."

if [ -n "$DOCUMENT_ID" ] && [ "$DOCUMENT_ID" != "null" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/student/profile/documents/$DOCUMENT_ID" \
        -H "Authorization: Bearer $TOKEN")
    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    print_result "DELETE /student/profile/documents/$DOCUMENT_ID" "$STATUS_CODE" 200
fi

# Summary
echo ""
echo "========================================"
echo "Test Summary"
echo "========================================"
echo "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
if [ $FAILED_TESTS -gt 0 ]; then
    echo -e "${RED}Failed: $FAILED_TESTS${NC}"
else
    echo -e "Failed: $FAILED_TESTS"
fi
echo "========================================"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED! Student module is working correctly.${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please review the errors above.${NC}"
    exit 1
fi
