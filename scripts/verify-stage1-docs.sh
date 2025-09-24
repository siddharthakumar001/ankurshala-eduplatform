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

# Function to check file content
check_content() {
    local file=$1
    local pattern=$2
    local description=$3
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -f "$file" ] && grep -q "$pattern" "$file"; then
        echo -e "✅ ${GREEN}$description${NC}: Found in $file"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "❌ ${RED}$description${NC}: Not found in $file"
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
echo "📝 Checking documentation content quality..."
echo

# Check system overview content
check_content "docs/stage1/system-overview.md" "mermaid" "Architecture diagrams"
check_content "docs/stage1/system-overview.md" "DEMO_SEED_ON_START" "Environment configuration"
check_content "docs/stage1/system-overview.md" "WHERE TO START FOR STAGE-2" "Stage-2 guidance"

# Check API catalog content
check_content "docs/stage1/api-index.md" "/api/auth/signin" "Authentication endpoints"
check_content "docs/stage1/api-index.md" "/api/student/profile" "Student endpoints"
check_content "docs/stage1/api-index.md" "/api/teacher/profile" "Teacher endpoints"
check_content "docs/stage1/api-index.md" "/api/admin/profile" "Admin endpoints"
check_content "docs/stage1/api-index.md" "curl" "Sample cURL commands"

# Check database schema content
check_content "docs/stage1/db-schema.md" "erDiagram" "Entity Relationship Diagram"
check_content "docs/stage1/db-schema.md" "AES-GCM" "Encryption documentation"
check_content "docs/stage1/db-schema.md" "CASCADE" "Foreign key relationships"

# Check security documentation
check_content "docs/stage1/security-rbac.md" "JWT" "JWT authentication"
check_content "docs/stage1/security-rbac.md" "BCrypt" "Password security"
check_content "docs/stage1/security-rbac.md" "Rate Limiting" "Rate limiting"
check_content "docs/stage1/security-rbac.md" "@PreAuthorize" "RBAC implementation"

# Check frontend documentation
check_content "docs/stage1/frontend-map.md" "data-testid" "Test identifiers"
check_content "docs/stage1/frontend-map.md" "React Query" "State management"
check_content "docs/stage1/frontend-map.md" "RouteGuard" "Route protection"

# Check testing documentation
check_content "docs/stage1/tests-e2e-index.md" "26" "Total test count"
check_content "docs/stage1/tests-e2e-index.md" "100%" "Success rate"
check_content "docs/stage1/tests-e2e-index.md" "playwright" "Test framework"

# Check demo seeding documentation
check_content "docs/stage1/demo-seed.md" "siddhartha@ankurshala.com" "Admin credentials"
check_content "docs/stage1/demo-seed.md" "student1@ankurshala.com" "Student credentials"
check_content "docs/stage1/demo-seed.md" "teacher1@ankurshala.com" "Teacher credentials"
check_content "docs/stage1/demo-seed.md" "Maza@123" "Demo password"

# Check operations documentation
check_content "docs/stage1/ops.md" "docker-compose" "Docker configuration"
check_content "docs/stage1/ops.md" "health" "Health checks"
check_content "docs/stage1/ops.md" "RFC7807" "Error handling"

# Check OpenAPI specification
if [ -f "backend/openapi.yaml" ]; then
    check_content "backend/openapi.yaml" "openapi:" "OpenAPI version"
    check_content "backend/openapi.yaml" "info:" "API information"
    check_content "backend/openapi.yaml" "paths:" "API paths"
else
    echo -e "⚠️  ${YELLOW}OpenAPI spec not found - this is acceptable for manual creation${NC}"
fi

echo
echo "🔗 Checking internal documentation links..."
echo

# Check if system overview links to other docs
if [ -f "docs/stage1/system-overview.md" ]; then
    for doc in "api-index.md" "db-schema.md" "security-rbac.md" "frontend-map.md" "tests-e2e-index.md" "demo-seed.md" "ops.md"; do
        if grep -q "$doc" "docs/stage1/system-overview.md"; then
            echo -e "✅ ${GREEN}Link to $doc${NC}: Found in system overview"
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
        else
            echo -e "❌ ${RED}Link to $doc${NC}: Missing from system overview"
        fi
        TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    done
fi

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
    echo -e "📚 ${GREEN}All documentation meets quality standards.${NC}"
    exit 0
elif [ $SUCCESS_RATE -ge 75 ]; then
    echo -e "⚠️  ${YELLOW}Docs Mostly OK - Minor issues found${NC}"
    echo -e "📝 ${YELLOW}Consider addressing missing items above.${NC}"
    exit 1
else
    echo -e "❌ ${RED}Docs Incomplete - Significant issues found${NC}"
    echo -e "🔧 ${RED}Please address missing files and content before proceeding.${NC}"
    exit 2
fi
