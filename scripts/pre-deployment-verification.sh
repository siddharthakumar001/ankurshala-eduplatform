#!/bin/bash

echo "🔍 Pre-Deployment Verification for AnkurShala Production"
echo "======================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASSED=0
FAILED=0

check_status() {
    local test_name=$1
    local status=$2
    local message=$3
    
    if [ "$status" = "0" ]; then
        echo -e "✅ ${GREEN}$test_name${NC}: $message"
        PASSED=$((PASSED + 1))
    else
        echo -e "❌ ${RED}$test_name${NC}: $message"
        FAILED=$((FAILED + 1))
    fi
}

echo "🔧 Checking configuration files..."

# Check .env.prod exists and has required variables
if [ -f ".env.prod" ]; then
    required_vars=("SPRING_PROFILES_ACTIVE" "DB_HOST" "DB_NAME" "JWT_SECRET" "REDIS_HOST" "NEXT_PUBLIC_API_URL")
    missing_vars=()
    
    for var in "${required_vars[@]}"; do
        if ! grep -q "^${var}=" .env.prod; then
            missing_vars+=("$var")
        fi
    done
    
    if [ ${#missing_vars[@]} -eq 0 ]; then
        check_status "Environment Variables" 0 "All required variables present in .env.prod"
    else
        check_status "Environment Variables" 1 "Missing variables: ${missing_vars[*]}"
    fi
else
    check_status "Environment File" 1 ".env.prod file not found"
fi

# Check Docker Compose production file
if [ -f "docker-compose.prod.yml" ]; then
    check_status "Production Compose" 0 "docker-compose.prod.yml exists"
else
    check_status "Production Compose" 1 "docker-compose.prod.yml missing"
fi

# Check Nginx configuration
if [ -d "nginx" ] && [ -f "nginx/nginx.conf" ] && [ -f "nginx/conf.d/ankurshala.conf" ]; then
    check_status "Nginx Config" 0 "Nginx configuration files present"
else
    check_status "Nginx Config" 1 "Nginx configuration files missing"
fi

# Check SSL directory (for certificates)
if [ -d "nginx/ssl" ]; then
    check_status "SSL Directory" 0 "SSL directory exists"
else
    echo -e "⚠️  ${YELLOW}SSL Directory${NC}: Creating nginx/ssl directory"
    mkdir -p nginx/ssl
    check_status "SSL Directory" 0 "SSL directory created"
fi

# Check deployment scripts
if [ -f "scripts/deploy-production.sh" ] && [ -x "scripts/deploy-production.sh" ]; then
    check_status "Deployment Script" 0 "Production deployment script ready"
else
    if [ -f "scripts/deploy-production.sh" ]; then
        chmod +x scripts/deploy-production.sh
        check_status "Deployment Script" 0 "Made deployment script executable"
    else
        check_status "Deployment Script" 1 "scripts/deploy-production.sh missing"
    fi
fi

# Check SSL setup script
if [ -f "scripts/setup-ssl.sh" ] && [ -x "scripts/setup-ssl.sh" ]; then
    check_status "SSL Setup Script" 0 "SSL setup script ready"
else
    if [ -f "scripts/setup-ssl.sh" ]; then
        chmod +x scripts/setup-ssl.sh
        check_status "SSL Setup Script" 0 "Made SSL setup script executable"
    else
        check_status "SSL Setup Script" 1 "scripts/setup-ssl.sh missing"
    fi
fi

echo ""
echo "🌐 Checking domain and DNS..."

# Check if domain is configured in .env.prod
if grep -q "ankurshala.com" .env.prod; then
    check_status "Domain Configuration" 0 "Domain ankurshala.com configured"
    
    # Test DNS resolution (if available)
    if command -v nslookup &> /dev/null; then
        if nslookup ankurshala.com > /dev/null 2>&1; then
            check_status "DNS Resolution" 0 "ankurshala.com resolves"
        else
            check_status "DNS Resolution" 1 "ankurshala.com does not resolve - check DNS settings"
        fi
    fi
else
    check_status "Domain Configuration" 1 "Domain not configured in .env.prod"
fi

echo ""
echo "🐳 Checking Docker requirements..."

# Check Docker and Docker Compose
if command -v docker &> /dev/null; then
    check_status "Docker" 0 "Docker is installed"
    
    # Check if Docker daemon is running
    if docker info > /dev/null 2>&1; then
        check_status "Docker Daemon" 0 "Docker daemon is running"
    else
        check_status "Docker Daemon" 1 "Docker daemon is not running"
    fi
else
    check_status "Docker" 1 "Docker is not installed"
fi

if command -v docker-compose &> /dev/null; then
    check_status "Docker Compose" 0 "Docker Compose is installed"
else
    check_status "Docker Compose" 1 "Docker Compose is not installed"
fi

echo ""
echo "📊 Pre-Deployment Summary"
echo "========================="
echo -e "✅ Passed checks: ${GREEN}$PASSED${NC}"
echo -e "❌ Failed checks: ${RED}$FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo ""
    echo -e "🎉 ${GREEN}All pre-deployment checks PASSED!${NC}"
    echo -e "🚀 ${GREEN}Ready for production deployment${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Run: ./scripts/setup-ssl.sh"
    echo "2. Run: ./scripts/deploy-production.sh"
    echo ""
    exit 0
else
    echo ""
    echo -e "⚠️  ${YELLOW}Please address the failed checks before deployment${NC}"
    echo ""
    exit 1
fi