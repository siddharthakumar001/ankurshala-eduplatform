#!/bin/bash
# Azure Container Registry Setup Script

set -Eeuo pipefail

# Colors
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log(){ case "$1" in INFO) echo -e "${BLUE}ℹ️  $2${NC}";; WARN) echo -e "${YELLOW}⚠️  $2${NC}";;
FAIL) echo -e "${RED}❌ $2${NC}";; PASS) echo -e "${GREEN}✅ $2${NC}";; esac; }

# Configuration
RESOURCE_GROUP="ankurshala-rg"
ACR_NAME="ankurshala"
LOCATION="eastus"
SKU="Basic"

usage() {
    echo "Usage: $0 [setup|login|test]"
    echo "  setup - Create Azure Container Registry"
    echo "  login - Login to ACR"
    echo "  test  - Test ACR connection"
    exit 1
}

setup_acr() {
    log INFO "Setting up Azure Container Registry..."
    
    # Create resource group if it doesn't exist
    if ! az group show --name "$RESOURCE_GROUP" &>/dev/null; then
        log INFO "Creating resource group: $RESOURCE_GROUP"
        az group create --name "$RESOURCE_GROUP" --location "$LOCATION"
    else
        log INFO "Resource group $RESOURCE_GROUP already exists"
    fi
    
    # Create ACR if it doesn't exist
    if ! az acr show --name "$ACR_NAME" --resource-group "$RESOURCE_GROUP" &>/dev/null; then
        log INFO "Creating Azure Container Registry: $ACR_NAME"
        az acr create \
            --resource-group "$RESOURCE_GROUP" \
            --name "$ACR_NAME" \
            --sku "$SKU" \
            --admin-enabled true
    else
        log INFO "Azure Container Registry $ACR_NAME already exists"
    fi
    
    # Get credentials
    log INFO "Retrieving ACR credentials..."
    ACR_USERNAME=$(az acr credential show --name "$ACR_NAME" --query "username" -o tsv)
    ACR_PASSWORD=$(az acr credential show --name "$ACR_NAME" --query "passwords[0].value" -o tsv)
    
    echo
    log PASS "Azure Container Registry setup completed!"
    echo
    echo "📋 ACR Details:"
    echo "  Registry URL: $ACR_NAME.azurecr.io"
    echo "  Username: $ACR_USERNAME"
    echo "  Password: $ACR_PASSWORD"
    echo
    echo "🔐 Add these to your GitHub Secrets:"
    echo "  ACR_USERNAME: $ACR_USERNAME"
    echo "  ACR_PASSWORD: $ACR_PASSWORD"
    echo
    echo "🔧 Add these to your server environment:"
    echo "  ACR_USERNAME: $ACR_USERNAME"
    echo "  ACR_PASSWORD: $ACR_PASSWORD"
}

login_acr() {
    log INFO "Logging into Azure Container Registry..."
    
    if [ -z "${ACR_USERNAME:-}" ] || [ -z "${ACR_PASSWORD:-}" ]; then
        log FAIL "ACR_USERNAME and ACR_PASSWORD environment variables must be set"
        exit 1
    fi
    
    echo "$ACR_PASSWORD" | docker login "$ACR_NAME.azurecr.io" --username "$ACR_USERNAME" --password-stdin
    
    log PASS "Successfully logged into ACR"
}

test_acr() {
    log INFO "Testing Azure Container Registry connection..."
    
    # Test login
    if ! docker login "$ACR_NAME.azurecr.io" --username "$ACR_USERNAME" --password "$ACR_PASSWORD" &>/dev/null; then
        log FAIL "Failed to login to ACR"
        exit 1
    fi
    
    # Test push/pull
    log INFO "Testing push/pull with hello-world image..."
    docker pull hello-world
    docker tag hello-world "$ACR_NAME.azurecr.io/hello-world:test"
    docker push "$ACR_NAME.azurecr.io/hello-world:test"
    docker rmi "$ACR_NAME.azurecr.io/hello-world:test"
    
    log PASS "ACR connection test successful"
}

# Main script
case "${1:-}" in
    setup) setup_acr ;;
    login) login_acr ;;
    test) test_acr ;;
    *) usage ;;
esac
