#!/bin/bash
set -Eeuo pipefail

# Azure Storage Fix Script for AnkurShala CI/CD Pipeline
# This script fixes Azure Storage container creation issues

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    case "$1" in
        INFO) echo -e "${BLUE}ℹ️  $2${NC}";;
        WARN) echo -e "${YELLOW}⚠️  $2${NC}";;
        FAIL) echo -e "${RED}❌ $2${NC}";;
        PASS) echo -e "${GREEN}✅ $2${NC}";;
    esac
}

# Configuration
STORAGE_ACCOUNT="${STORAGE_ACCOUNT:-ankurimage}"
CONTAINER_NAME="${CONTAINER_NAME:-docker-images}"
STORAGE_KEY="${STORAGE_KEY:-}"

log INFO "🔧 Fixing Azure Storage container creation issues..."

# Check if storage key is provided
if [[ -z "$STORAGE_KEY" ]]; then
    log FAIL "STORAGE_KEY environment variable is not set"
    log INFO "Please set the STORAGE_KEY environment variable with your Azure storage account key"
    log INFO "You can get the key from Azure Portal > Storage Account > Access Keys"
    exit 1
fi

# Install Azure CLI if not present
if ! command -v az &> /dev/null; then
    log INFO "Installing Azure CLI..."
    curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
    log PASS "Azure CLI installed"
fi

# Test Azure CLI authentication
log INFO "Testing Azure CLI authentication..."
if az storage account show --name "$STORAGE_ACCOUNT" --key "$STORAGE_KEY" >/dev/null 2>&1; then
    log PASS "Azure CLI authentication successful"
else
    log FAIL "Azure CLI authentication failed"
    log INFO "Please check your storage account name and key"
    exit 1
fi

# Check storage account status
log INFO "Checking storage account status..."
STORAGE_INFO=$(az storage account show --name "$STORAGE_ACCOUNT" --key "$STORAGE_KEY" 2>/dev/null || echo "ERROR")
if [[ "$STORAGE_INFO" == "ERROR" ]]; then
    log FAIL "Cannot access storage account '$STORAGE_ACCOUNT'"
    exit 1
else
    log PASS "Storage account '$STORAGE_ACCOUNT' is accessible"
fi

# List existing containers
log INFO "Listing existing containers..."
az storage container list \
    --account-name "$STORAGE_ACCOUNT" \
    --account-key "$STORAGE_KEY" \
    --output table

# Check if container already exists
log INFO "Checking if container '$CONTAINER_NAME' exists..."
if az storage container show \
    --name "$CONTAINER_NAME" \
    --account-name "$STORAGE_ACCOUNT" \
    --account-key "$STORAGE_KEY" >/dev/null 2>&1; then
    log PASS "Container '$CONTAINER_NAME' already exists"
else
    log INFO "Container '$CONTAINER_NAME' does not exist, creating..."
    
    # Create container with proper permissions
    log INFO "Creating container with private access..."
    if az storage container create \
        --name "$CONTAINER_NAME" \
        --account-name "$STORAGE_ACCOUNT" \
        --account-key "$STORAGE_KEY" \
        --public-access off >/dev/null 2>&1; then
        log PASS "Container '$CONTAINER_NAME' created successfully with private access"
    else
        log WARN "Failed to create container with private access, trying with blob access..."
        if az storage container create \
            --name "$CONTAINER_NAME" \
            --account-name "$STORAGE_ACCOUNT" \
            --account-key "$STORAGE_KEY" \
            --public-access blob >/dev/null 2>&1; then
            log PASS "Container '$CONTAINER_NAME' created successfully with blob access"
        else
            log FAIL "Failed to create container '$CONTAINER_NAME'"
            exit 1
        fi
    fi
fi

# Verify container creation
log INFO "Verifying container creation..."
if az storage container show \
    --name "$CONTAINER_NAME" \
    --account-name "$STORAGE_ACCOUNT" \
    --account-key "$STORAGE_KEY" >/dev/null 2>&1; then
    log PASS "Container '$CONTAINER_NAME' is ready for use"
else
    log FAIL "Container verification failed"
    exit 1
fi

# Test blob upload
log INFO "Testing blob upload..."
TEST_FILE="/tmp/test-upload.txt"
echo "Test file for Azure Storage" > "$TEST_FILE"

if az storage blob upload \
    --container-name "$CONTAINER_NAME" \
    --name "test-upload.txt" \
    --file "$TEST_FILE" \
    --account-name "$STORAGE_ACCOUNT" \
    --account-key "$STORAGE_KEY" >/dev/null 2>&1; then
    log PASS "Blob upload test successful"
    
    # Clean up test file
    az storage blob delete \
        --container-name "$CONTAINER_NAME" \
        --name "test-upload.txt" \
        --account-name "$STORAGE_ACCOUNT" \
        --account-key "$STORAGE_KEY" >/dev/null 2>&1
    rm -f "$TEST_FILE"
    log PASS "Test file cleaned up"
else
    log FAIL "Blob upload test failed"
    rm -f "$TEST_FILE"
    exit 1
fi

# Display container information
log INFO "Container information:"
az storage container show \
    --name "$CONTAINER_NAME" \
    --account-name "$STORAGE_ACCOUNT" \
    --account-key "$STORAGE_KEY" \
    --output table

log PASS "🎉 Azure Storage container setup completed successfully!"
echo
echo "📋 Container Details:"
echo "  - Storage Account: $STORAGE_ACCOUNT"
echo "  - Container Name: $CONTAINER_NAME"
echo "  - Access Level: Private (recommended for security)"
echo "  - Status: Ready for use"
echo
echo "🔧 For CI/CD Pipeline:"
echo "  - Make sure STORAGE_KEY secret is set in GitHub repository"
echo "  - Container is now ready for Docker image uploads"
echo "  - Pipeline should work without container creation errors"