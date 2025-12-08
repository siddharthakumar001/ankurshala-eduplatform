#!/bin/bash

# Teacher Module Smoke Test Script
# Tests all teacher profile endpoints to ensure proper functionality

set -e

BASE_URL="http://localhost:8080/api"
TEACHER_EMAIL="teacher1@ankurshala.com"
TEACHER_PASSWORD="Maza@123"

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
echo "Teacher Module Smoke Test"
echo "========================================"
echo ""

# 1. Login as Teacher
echo "1. Authenticating as Teacher..."
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/signin" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$TEACHER_EMAIL\",\"password\":\"$TEACHER_PASSWORD\"}")

STATUS_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

print_result "Teacher Login" "$STATUS_CODE" 200

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

# 2. Get Teacher Profile
echo "2. Testing Profile Endpoints..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/teacher/profile" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /teacher/profile" "$STATUS_CODE" 200

# 3. Get Teacher Dashboard
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/teacher/dashboard" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /teacher/dashboard" "$STATUS_CODE" 200

# 4. Get Teacher Qualifications
echo ""
echo "3. Testing Qualifications Endpoints..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/teacher/profile/qualifications" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /teacher/profile/qualifications" "$STATUS_CODE" 200

# 5. Add Teacher Qualification
QUALIFICATION_PAYLOAD='{
  "degree": "Master of Science",
  "field": "Computer Science",
  "institution": "Test University",
  "yearOfCompletion": 2020,
  "grade": "First Class"
}'

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/teacher/profile/qualifications" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$QUALIFICATION_PAYLOAD")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')
print_result "POST /teacher/profile/qualifications" "$STATUS_CODE" 200

# Extract qualification ID if creation was successful
QUALIFICATION_ID=""
if [ "$STATUS_CODE" -eq 200 ]; then
    QUALIFICATION_ID=$(echo "$RESPONSE_BODY" | jq -r '.id')
fi

# 6. Get Teacher Experiences
echo ""
echo "4. Testing Experiences Endpoints..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/teacher/profile/experiences" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /teacher/profile/experiences" "$STATUS_CODE" 200

# 7. Add Teacher Experience
EXPERIENCE_PAYLOAD='{
  "jobTitle": "Senior Teacher",
  "organization": "Test School",
  "startDate": "2020-01-01",
  "endDate": "2023-12-31",
  "description": "Teaching Mathematics and Science"
}'

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/teacher/profile/experiences" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$EXPERIENCE_PAYLOAD")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')
print_result "POST /teacher/profile/experiences" "$STATUS_CODE" 200

# Extract experience ID if creation was successful
EXPERIENCE_ID=""
if [ "$STATUS_CODE" -eq 200 ]; then
    EXPERIENCE_ID=$(echo "$RESPONSE_BODY" | jq -r '.id')
fi

# 8. Get Teacher Certifications
echo ""
echo "5. Testing Certifications Endpoints..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/teacher/profile/certifications" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /teacher/profile/certifications" "$STATUS_CODE" 200

# 9. Add Teacher Certification
CERTIFICATION_PAYLOAD='{
  "certificationName": "TEFL Certification",
  "issuingOrganization": "International TEFL Academy",
  "issueDate": "2021-06-15",
  "expiryDate": "2026-06-15",
  "credentialId": "TEFL123456"
}'

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/teacher/profile/certifications" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$CERTIFICATION_PAYLOAD")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')
print_result "POST /teacher/profile/certifications" "$STATUS_CODE" 200

# Extract certification ID if creation was successful
CERTIFICATION_ID=""
if [ "$STATUS_CODE" -eq 200 ]; then
    CERTIFICATION_ID=$(echo "$RESPONSE_BODY" | jq -r '.id')
fi

# 10. Get Teacher Documents
echo ""
echo "6. Testing Documents Endpoints..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/teacher/profile/documents" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /teacher/profile/documents" "$STATUS_CODE" 200

# 11. Get Teacher Availability
echo ""
echo "7. Testing Availability Endpoints..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/teacher/profile/availability" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /teacher/profile/availability" "$STATUS_CODE" 200

# 12. Get Teacher Addresses
echo ""
echo "8. Testing Addresses Endpoints..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/teacher/profile/addresses" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /teacher/profile/addresses" "$STATUS_CODE" 200

# 13. Add Teacher Address
ADDRESS_PAYLOAD='{
  "addressLine1": "123 Main Street",
  "addressLine2": "Apt 4B",
  "city": "Mumbai",
  "state": "Maharashtra",
  "postalCode": "400001",
  "country": "India",
  "addressType": "CURRENT"
}'

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/teacher/profile/addresses" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$ADDRESS_PAYLOAD")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')
print_result "POST /teacher/profile/addresses" "$STATUS_CODE" 200

# Extract address ID if creation was successful
ADDRESS_ID=""
if [ "$STATUS_CODE" -eq 200 ]; then
    ADDRESS_ID=$(echo "$RESPONSE_BODY" | jq -r '.id')
fi

# 14. Get Teacher Bank Details
echo ""
echo "9. Testing Bank Details Endpoints..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/teacher/profile/bank-details" \
    -H "Authorization: Bearer $TOKEN")
STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
print_result "GET /teacher/profile/bank-details" "$STATUS_CODE" 200

# Cleanup: Delete created test data
echo ""
echo "10. Cleaning up test data..."

if [ -n "$QUALIFICATION_ID" ] && [ "$QUALIFICATION_ID" != "null" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/teacher/profile/qualifications/$QUALIFICATION_ID" \
        -H "Authorization: Bearer $TOKEN")
    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    print_result "DELETE /teacher/profile/qualifications/$QUALIFICATION_ID" "$STATUS_CODE" 200
fi

if [ -n "$EXPERIENCE_ID" ] && [ "$EXPERIENCE_ID" != "null" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/teacher/profile/experiences/$EXPERIENCE_ID" \
        -H "Authorization: Bearer $TOKEN")
    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    print_result "DELETE /teacher/profile/experiences/$EXPERIENCE_ID" "$STATUS_CODE" 200
fi

if [ -n "$CERTIFICATION_ID" ] && [ "$CERTIFICATION_ID" != "null" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/teacher/profile/certifications/$CERTIFICATION_ID" \
        -H "Authorization: Bearer $TOKEN")
    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    print_result "DELETE /teacher/profile/certifications/$CERTIFICATION_ID" "$STATUS_CODE" 200
fi

if [ -n "$ADDRESS_ID" ] && [ "$ADDRESS_ID" != "null" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/teacher/profile/addresses/$ADDRESS_ID" \
        -H "Authorization: Bearer $TOKEN")
    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    print_result "DELETE /teacher/profile/addresses/$ADDRESS_ID" "$STATUS_CODE" 200
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
    echo -e "${GREEN}🎉 ALL TESTS PASSED! Teacher module is working correctly.${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please review the errors above.${NC}"
    exit 1
fi
