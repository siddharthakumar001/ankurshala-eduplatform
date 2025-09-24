#!/bin/bash

# scripts/verify-stage1-docs.sh
# Verification script for Stage-1 documentation bundle

echo "🔍 Verifying Stage-1 Documentation Bundle..."
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Track verification status
MISSING_FILES=()
PASSED_CHECKS=0
TOTAL_CHECKS=0

# Function to check file existence
check_file() {
    local file=$1
    local description=$2
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -f "$file" ]; then
        echo -e "✅ ${GREEN}$description${NC}: $file"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "❌ ${RED}$description${NC}: $file (MISSING)"
        MISSING_FILES+=("$file")
        return 1
    fi
}

echo "📋 Checking required documentation files..."
echo

# Core documentation files
check_file "docs/stage1/system-overview.md" "System Overview & Architecture"
check_file "docs/stage1/api-index.md" "API Catalog & Index"
check_file "docs/stage1/db-schema.md" "Database Schema & ERD"
check_file "docs/stage1/security-rbac.md" "Security & RBAC"
check_file "docs/stage1/frontend-map.md" "Frontend Component Map"
check_file "docs/stage1/tests-e2e-index.md" "E2E Testing Coverage"
check_file "docs/stage1/demo-seed.md" "Demo Data Seeding"
check_file "docs/stage1/ops.md" "Operations & Troubleshooting"

# OpenAPI specification
check_file "backend/openapi.yaml" "OpenAPI Specification"

echo
echo "📊 Verification Summary"
echo "======================"

if [ ${#MISSING_FILES[@]} -eq 0 ]; then
    echo -e "🎉 ${GREEN}All required files present!${NC}"
else
    echo -e "⚠️  ${YELLOW}Missing files:${NC}"
    for file in "${MISSING_FILES[@]}"; do
        echo "   - $file"
    done
fi

echo
echo -e "📈 ${GREEN}Passed checks: $PASSED_CHECKS/$TOTAL_CHECKS${NC}"

# Calculate success percentage
SUCCESS_RATE=$(( (PASSED_CHECKS * 100) / TOTAL_CHECKS ))
echo -e "📊 ${GREEN}Success rate: $SUCCESS_RATE%${NC}"

echo
if [ $SUCCESS_RATE -ge 90 ]; then
    echo -e "🎯 ${GREEN}Docs OK - Stage-1 documentation bundle is ready!${NC}"
    exit 0
else
    echo -e "❌ ${RED}Docs Incomplete - Issues found${NC}"
    exit 1
fi
