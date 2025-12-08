#!/bin/bash

#############################################
# Admin Content Data Integrity Test
# Tests hierarchical data integrity constraints
#############################################

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
BACKEND_URL="http://localhost:8080/api"
ADMIN_EMAIL="siddhartha@ankurshala.com"
ADMIN_PASSWORD="Maza@123"

# Counters
TESTS_PASSED=0
TESTS_FAILED=0

echo "========================================"
echo "Admin Content Data Integrity Test"
echo "========================================"
echo ""

# Login as Admin
echo "1. Authenticating as Admin..."
LOGIN_RESPONSE=$(curl -s -X POST "$BACKEND_URL/auth/signin" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}")

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken')
if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo -e "${RED}Failed to login. Exiting...${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Admin authenticated successfully${NC}"
echo ""

# Test 1: Try to create grade with invalid number (below 7)
echo "2. Testing Grade Validation (Grade < 7 should fail)..."
RESPONSE=$(curl -s -X POST "$BACKEND_URL/admin/content/grades" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "6",
      "displayName": "Grade 6",
      "boardId": 1,
      "active": true
    }')

if echo "$RESPONSE" | grep -q "Grade must be between 7 and 12"; then
    echo -e "${GREEN}✓ Grade validation working - rejected grade 6${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Grade validation failed - should reject grade 6${NC}"
    echo "Response: $RESPONSE"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

# Test 2: Try to create grade with invalid number (above 12)
echo "3. Testing Grade Validation (Grade > 12 should fail)..."
RESPONSE=$(curl -s -X POST "$BACKEND_URL/admin/content/grades" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "13",
      "displayName": "Grade 13",
      "boardId": 1,
      "active": true
    }')

if echo "$RESPONSE" | grep -q "Grade must be between 7 and 12"; then
    echo -e "${GREEN}✓ Grade validation working - rejected grade 13${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Grade validation failed - should reject grade 13${NC}"
    echo "Response: $RESPONSE"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

# Test 3: Create valid grade (7-12)
echo "4. Creating valid grade (9)..."
RESPONSE=$(curl -s -X POST "$BACKEND_URL/admin/content/grades" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "9",
      "displayName": "Grade 9",
      "boardId": 1,
      "active": true
    }')

GRADE_ID=$(echo "$RESPONSE" | jq -r '.data.id // .id')
if [ -n "$GRADE_ID" ] && [ "$GRADE_ID" != "null" ]; then
    echo -e "${GREEN}✓ Valid grade created successfully (ID: $GRADE_ID)${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${YELLOW}⚠ Grade might already exist or creation failed${NC}"
    # Try to get existing grade
    EXISTING_GRADE=$(curl -s "$BACKEND_URL/admin/content/grades/by-board?board=CBSE" \
        -H "Authorization: Bearer $TOKEN" | jq '.data[] | select(.name=="9") | .id')
    if [ -n "$EXISTING_GRADE" ]; then
        GRADE_ID=$EXISTING_GRADE
        echo -e "${GREEN}✓ Using existing grade (ID: $GRADE_ID)${NC}"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi
fi

# Test 4: Try to create subject without grade (should fail)
echo "5. Testing Subject Validation (missing grade should fail)..."
RESPONSE=$(curl -s -X POST "$BACKEND_URL/admin/content/subjects" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "Test Subject",
      "boardId": 1,
      "active": true
    }')

if echo "$RESPONSE" | grep -q "Grade ID is required\|gradeId"; then
    echo -e "${GREEN}✓ Subject validation working - rejected missing grade${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Subject validation failed - should require grade${NC}"
    echo "Response: $RESPONSE"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

# Test 5: Create subject with board + grade
echo "6. Creating subject with proper hierarchy..."
# Use timestamp to ensure unique name
SUBJECT_NAME="IntegTest_Subject_$(date +%s)"
RESPONSE=$(curl -s -X POST "$BACKEND_URL/admin/content/subjects" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"$SUBJECT_NAME\",
      \"boardId\": 1,
      \"gradeId\": $GRADE_ID,
      \"active\": true
    }")

SUBJECT_ID=$(echo "$RESPONSE" | jq -r '.data.id // .id')
if [ -n "$SUBJECT_ID" ] && [ "$SUBJECT_ID" != "null" ]; then
    echo -e "${GREEN}✓ Subject created with proper hierarchy (ID: $SUBJECT_ID)${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Failed to create subject${NC}"
    echo "Response: $RESPONSE"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

# Test 6: Try to create chapter without subject (should fail)
echo "7. Testing Chapter Validation (missing subject should fail)..."
RESPONSE=$(curl -s -X POST "$BACKEND_URL/admin/content/chapters" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"Test Chapter\",
      \"boardId\": 1,
      \"gradeId\": $GRADE_ID,
      \"active\": true
    }")

if echo "$RESPONSE" | grep -q "Subject ID is required\|subjectId"; then
    echo -e "${GREEN}✓ Chapter validation working - rejected missing subject${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Chapter validation failed - should require subject${NC}"
    echo "Response: $RESPONSE"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

# Test 7: Create chapter with full hierarchy
if [ -n "$SUBJECT_ID" ] && [ "$SUBJECT_ID" != "null" ]; then
    echo "8. Creating chapter with proper hierarchy..."
    CHAPTER_NAME="IntegTest_Chapter_$(date +%s)"
    RESPONSE=$(curl -s -X POST "$BACKEND_URL/admin/content/chapters" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
          \"name\": \"$CHAPTER_NAME\",
          \"boardId\": 1,
          \"gradeId\": $GRADE_ID,
          \"subjectId\": $SUBJECT_ID,
          \"active\": true
        }")

    CHAPTER_ID=$(echo "$RESPONSE" | jq -r '.data.id // .id')
    if [ -n "$CHAPTER_ID" ] && [ "$CHAPTER_ID" != "null" ]; then
        echo -e "${GREEN}✓ Chapter created with proper hierarchy (ID: $CHAPTER_ID)${NC}"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo -e "${RED}✗ Failed to create chapter${NC}"
        echo "Response: $RESPONSE"
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi
fi

# Test 8: Try to create topic without chapter (should fail)
echo "9. Testing Topic Validation (missing chapter should fail)..."
RESPONSE=$(curl -s -X POST "$BACKEND_URL/admin/content/topics" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"title\": \"Test Topic\",
      \"boardId\": 1,
      \"gradeId\": $GRADE_ID,
      \"subjectId\": $SUBJECT_ID,
      \"active\": true
    }")

if echo "$RESPONSE" | grep -q "Chapter ID is required\|chapterId"; then
    echo -e "${GREEN}✓ Topic validation working - rejected missing chapter${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Topic validation failed - should require chapter${NC}"
    echo "Response: $RESPONSE"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi

# Test 9: Create topic with full hierarchy
if [ -n "$CHAPTER_ID" ] && [ "$CHAPTER_ID" != "null" ]; then
    echo "10. Creating topic with proper hierarchy..."
    TOPIC_TITLE="IntegTest_Topic_$(date +%s)"
    RESPONSE=$(curl -s -X POST "$BACKEND_URL/admin/content/topics" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
          \"title\": \"$TOPIC_TITLE\",
          \"description\": \"Test topic description\",
          \"boardId\": 1,
          \"gradeId\": $GRADE_ID,
          \"subjectId\": $SUBJECT_ID,
          \"chapterId\": $CHAPTER_ID,
          \"active\": true
        }")

    TOPIC_ID=$(echo "$RESPONSE" | jq -r '.data.id // .id')
    if [ -n "$TOPIC_ID" ] && [ "$TOPIC_ID" != "null" ]; then
        echo -e "${GREEN}✓ Topic created with proper hierarchy (ID: $TOPIC_ID)${NC}"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo -e "${RED}✗ Failed to create topic${NC}"
        echo "Response: $RESPONSE"
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi
fi

# Test 10: Try to create topic note without hierarchy (should fail)
if [ -n "$TOPIC_ID" ] && [ "$TOPIC_ID" != "null" ]; then
    echo "11. Testing Topic Note Validation (missing hierarchy should fail)..."
    RESPONSE=$(curl -s -X POST "$BACKEND_URL/admin/content/topic-notes" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
          \"title\": \"Test Note\",
          \"content\": \"Test note content\",
          \"topicId\": $TOPIC_ID,
          \"active\": true
        }")

    if echo "$RESPONSE" | grep -q "Board ID is required\|Grade ID is required\|Subject ID is required\|Chapter ID is required"; then
        echo -e "${GREEN}✓ Topic Note validation working - rejected incomplete hierarchy${NC}"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo -e "${RED}✗ Topic Note validation failed - should require full hierarchy${NC}"
        echo "Response: $RESPONSE"
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi

    # Test 11: Create topic note with full hierarchy
    echo "12. Creating topic note with proper hierarchy..."
    NOTE_TITLE="IntegTest_Note_$(date +%s)"
    RESPONSE=$(curl -s -X POST "$BACKEND_URL/admin/content/topic-notes" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
          \"title\": \"$NOTE_TITLE\",
          \"content\": \"Test note content for integration testing\",
          \"topicId\": $TOPIC_ID,
          \"boardId\": 1,
          \"gradeId\": $GRADE_ID,
          \"subjectId\": $SUBJECT_ID,
          \"chapterId\": $CHAPTER_ID,
          \"active\": true
        }")

    NOTE_ID=$(echo "$RESPONSE" | jq -r '.data.id // .id')
    if [ -n "$NOTE_ID" ] && [ "$NOTE_ID" != "null" ]; then
        echo -e "${GREEN}✓ Topic Note created with proper hierarchy (ID: $NOTE_ID)${NC}"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo -e "${RED}✗ Failed to create topic note${NC}"
        echo "Response: $RESPONSE"
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi
fi

# Summary
echo ""
echo "========================================"
echo "Test Summary"
echo "========================================"
echo "Total Tests: $((TESTS_PASSED + TESTS_FAILED))"
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
if [ $TESTS_FAILED -gt 0 ]; then
    echo -e "${RED}Failed: $TESTS_FAILED${NC}"
else
    echo -e "Failed: $TESTS_FAILED"
fi
echo "========================================"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL DATA INTEGRITY TESTS PASSED!${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please review the errors above.${NC}"
    exit 1
fi
