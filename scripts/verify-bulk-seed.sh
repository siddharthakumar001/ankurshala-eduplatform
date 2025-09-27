#!/bin/bash

# Bulk seed verification script
# Tests the bulk seeding functionality and verifies idempotency

set -e

BASE_URL="http://localhost:8080"
ADMIN_TOKEN="${ADMIN_TOKEN:-}"

echo "🔍 Bulk Seed Verification Script"
echo "================================"

# Check if backend is running
echo "Checking backend health..."
if ! curl -fsS "${BASE_URL}/api/actuator/health" >/dev/null; then
    echo "❌ Backend is not running or not healthy"
    exit 1
fi
echo "✅ Backend is healthy"

# Check seeding endpoint status
echo "Checking seeding endpoint status..."
STATUS_RESPONSE=$(curl -s "${BASE_URL}/api/admin/dev-seed/status")
echo "Status response: $STATUS_RESPONSE"

# Check if we can run seeding
CAN_RUN=$(echo "$STATUS_RESPONSE" | jq -r '.canRun')
if [ "$CAN_RUN" != "true" ]; then
    echo "❌ Bulk seeding is not allowed in current environment"
    echo "Status: $STATUS_RESPONSE"
    exit 1
fi
echo "✅ Bulk seeding is allowed"

# First run - should create users
echo ""
echo "🌱 Running first bulk seed..."
FIRST_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/admin/dev-seed/bulk" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{}')

echo "First run response:"
echo "$FIRST_RESPONSE" | jq .

# Check if first run was successful
FIRST_ERRORS=$(echo "$FIRST_RESPONSE" | jq -r '.errors | length')
if [ "$FIRST_ERRORS" -gt 0 ]; then
    echo "❌ First run had errors:"
    echo "$FIRST_RESPONSE" | jq -r '.errors[]'
    exit 1
fi

# Verify counts
STUDENTS_CREATED=$(echo "$FIRST_RESPONSE" | jq -r '.summary.students.created')
TEACHERS_CREATED=$(echo "$FIRST_RESPONSE" | jq -r '.summary.teachers.created')
ADMINS_CREATED=$(echo "$FIRST_RESPONSE" | jq -r '.summary.admins.created')

echo "✅ First run results:"
echo "   Students created: $STUDENTS_CREATED"
echo "   Teachers created: $TEACHERS_CREATED"
echo "   Admins created: $ADMINS_CREATED"

# Verify expected counts
if [ "$STUDENTS_CREATED" -ne 15 ]; then
    echo "❌ Expected 15 students created, got $STUDENTS_CREATED"
    exit 1
fi

if [ "$TEACHERS_CREATED" -ne 15 ]; then
    echo "❌ Expected 15 teachers created, got $TEACHERS_CREATED"
    exit 1
fi

if [ "$ADMINS_CREATED" -ne 3 ]; then
    echo "❌ Expected 3 admins created, got $ADMINS_CREATED"
    exit 1
fi

# Second run - should update existing users (idempotency test)
echo ""
echo "🔄 Running second bulk seed (idempotency test)..."
SECOND_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/admin/dev-seed/bulk" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{}')

echo "Second run response:"
echo "$SECOND_RESPONSE" | jq .

# Check if second run was successful
SECOND_ERRORS=$(echo "$SECOND_RESPONSE" | jq -r '.errors | length')
if [ "$SECOND_ERRORS" -gt 0 ]; then
    echo "❌ Second run had errors:"
    echo "$SECOND_RESPONSE" | jq -r '.errors[]'
    exit 1
fi

# Verify idempotency
STUDENTS_UPDATED=$(echo "$SECOND_RESPONSE" | jq -r '.summary.students.updated')
TEACHERS_UPDATED=$(echo "$SECOND_RESPONSE" | jq -r '.summary.teachers.updated')
ADMINS_UPDATED=$(echo "$SECOND_RESPONSE" | jq -r '.summary.admins.updated')

STUDENTS_CREATED_SECOND=$(echo "$SECOND_RESPONSE" | jq -r '.summary.students.created')
TEACHERS_CREATED_SECOND=$(echo "$SECOND_RESPONSE" | jq -r '.summary.teachers.created')
ADMINS_CREATED_SECOND=$(echo "$SECOND_RESPONSE" | jq -r '.summary.admins.created')

echo "✅ Second run results (idempotency test):"
echo "   Students updated: $STUDENTS_UPDATED (created: $STUDENTS_CREATED_SECOND)"
echo "   Teachers updated: $TEACHERS_UPDATED (created: $TEACHERS_CREATED_SECOND)"
echo "   Admins updated: $ADMINS_UPDATED (created: $ADMINS_CREATED_SECOND)"

# Verify no new users were created in second run
if [ "$STUDENTS_CREATED_SECOND" -ne 0 ]; then
    echo "❌ Expected 0 students created in second run, got $STUDENTS_CREATED_SECOND"
    exit 1
fi

if [ "$TEACHERS_CREATED_SECOND" -ne 0 ]; then
    echo "❌ Expected 0 teachers created in second run, got $TEACHERS_CREATED_SECOND"
    exit 1
fi

if [ "$ADMINS_CREATED_SECOND" -ne 0 ]; then
    echo "❌ Expected 0 admins created in second run, got $ADMINS_CREATED_SECOND"
    exit 1
fi

# Verify all users were updated in second run
if [ "$STUDENTS_UPDATED" -ne 15 ]; then
    echo "❌ Expected 15 students updated in second run, got $STUDENTS_UPDATED"
    exit 1
fi

if [ "$TEACHERS_UPDATED" -ne 15 ]; then
    echo "❌ Expected 15 teachers updated in second run, got $TEACHERS_UPDATED"
    exit 1
fi

if [ "$ADMINS_UPDATED" -ne 3 ]; then
    echo "❌ Expected 3 admins updated in second run, got $ADMINS_UPDATED"
    exit 1
fi

echo ""
echo "🎉 All tests passed!"
echo "✅ Bulk seeding works correctly"
echo "✅ Idempotency verified - no duplicates created"
echo "✅ All 15 students, 15 teachers, and 3 admins seeded successfully"

