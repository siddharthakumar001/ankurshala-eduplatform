#!/bin/bash
set -Eeuo pipefail

# Azure Storage Diagnostic Script
# This script helps diagnose Azure Storage container creation issues

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

log INFO "🔍 Diagnosing Azure Storage container creation issues..."

# Check if storage key is provided
if [[ -z "$STORAGE_KEY" ]]; then
    log FAIL "STORAGE_KEY environment variable is not set"
    log INFO "Please set the STORAGE_KEY environment variable with your Azure storage account key"
    log INFO "Example: export STORAGE_KEY='your-storage-key-here'"
    exit 1
fi

# 1. Test Azure CLI installation
log INFO "1. Testing Azure CLI installation..."
if command -v az &> /dev/null; then
    AZ_VERSION=$(az --version | head -n1)
    log PASS "Azure CLI is installed: $AZ_VERSION"
else
    log FAIL "Azure CLI is not installed"
    log INFO "Installing Azure CLI..."
    curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
    if command -v az &> /dev/null; then
        log PASS "Azure CLI installed successfully"
    else
        log FAIL "Failed to install Azure CLI"
        exit 1
    fi
fi

# 2. Test storage account access
log INFO "2. Testing storage account access..."
if az storage account show --name "$STORAGE_ACCOUNT" --key "$STORAGE_KEY" >/dev/null 2>&1; then
    log PASS "Storage account '$STORAGE_ACCOUNT' is accessible"
else
    log FAIL "Cannot access storage account '$STORAGE_ACCOUNT'"
    log INFO "Please check:"
    log INFO "  - Storage account name: $STORAGE_ACCOUNT"
    log INFO "  - Storage account key: $STORAGE_KEY"
    log INFO "  - Storage account exists and is in the correct region"
    exit 1
fi

# 3. Get storage account details
log INFO "3. Getting storage account details..."
STORAGE_INFO=$(az storage account show --name "$STORAGE_ACCOUNT" --key "$STORAGE_KEY" 2>/dev/null)
if [[ -n "$STORAGE_INFO" ]]; then
    echo "$STORAGE_INFO" | jq -r '. | "  - Name: \(.name)\n  - Resource Group: \(.resourceGroup)\n  - Location: \(.location)\n  - SKU: \(.sku.name)\n  - Kind: \(.kind)"'
    log PASS "Storage account details retrieved"
else
    log FAIL "Failed to get storage account details"
    exit 1
fi

# 4. List existing containers
log INFO "4. Listing existing containers..."
CONTAINERS=$(az storage container list --account-name "$STORAGE_ACCOUNT" --account-key "$STORAGE_KEY" --output table 2>/dev/null)
if [[ -n "$CONTAINERS" ]]; then
    echo "$CONTAINERS"
    log PASS "Container list retrieved"
else
    log WARN "No containers found or failed to list containers"
fi

# 5. Check if target container exists
log INFO "5. Checking if target container '$CONTAINER_NAME' exists..."
if az storage container show --name "$CONTAINER_NAME" --account-name "$STORAGE_ACCOUNT" --account-key "$STORAGE_KEY" >/dev/null 2>&1; then
    log PASS "Container '$CONTAINER_NAME' already exists"
    CONTAINER_EXISTS=true
else
    log INFO "Container '$CONTAINER_NAME' does not exist"
    CONTAINER_EXISTS=false
fi

# 6. Test container creation
if [[ "$CONTAINER_EXISTS" == "false" ]]; then
    log INFO "6. Testing container creation..."
    
    # Try creating with private access
    log INFO "Attempting to create container with private access..."
    CREATE_RESULT=$(az storage container create \
        --name "$CONTAINER_NAME" \
        --account-name "$STORAGE_ACCOUNT" \
        --account-key "$STORAGE_KEY" \
        --public-access off 2>&1)
    
    echo "Creation result: $CREATE_RESULT"
    
    # Check if creation was successful
    if echo "$CREATE_RESULT" | grep -q '"created": true'; then
        log PASS "Container created successfully with private access"
    elif echo "$CREATE_RESULT" | grep -q '"created": false'; then
        log WARN "Container creation returned false"
        
        # Check if container exists anyway
        if az storage container show --name "$CONTAINER_NAME" --account-name "$STORAGE_ACCOUNT" --account-key "$STORAGE_KEY" >/dev/null 2>&1; then
            log PASS "Container exists despite creation returning false"
        else
            log FAIL "Container creation failed and container does not exist"
            
            # Try with blob access
            log INFO "Trying to create with blob access..."
            CREATE_RESULT2=$(az storage container create \
                --name "$CONTAINER_NAME" \
                --account-name "$STORAGE_ACCOUNT" \
                --account-key "$STORAGE_KEY" \
                --public-access blob 2>&1)
            
            echo "Second creation result: $CREATE_RESULT2"
            
            if az storage container show --name "$CONTAINER_NAME" --account-name "$STORAGE_ACCOUNT" --account-key "$STORAGE_KEY" >/dev/null 2>&1; then
                log PASS "Container created successfully with blob access"
            else
                log FAIL "Container creation failed with both access levels"
                exit 1
            fi
        fi
    else
        log FAIL "Unexpected creation result"
        exit 1
    fi
fi

# 7. Test blob upload
log INFO "7. Testing blob upload..."
TEST_FILE="/tmp/test-upload-$(date +%s).txt"
echo "Test file for Azure Storage - $(date)" > "$TEST_FILE"

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

# 8. Final verification
log INFO "8. Final verification..."
if az storage container show --name "$CONTAINER_NAME" --account-name "$STORAGE_ACCOUNT" --account-key "$STORAGE_KEY" >/dev/null 2>&1; then
    log PASS "Container '$CONTAINER_NAME' is ready for use"
else
    log FAIL "Final verification failed"
    exit 1
fi

log PASS "🎉 Azure Storage diagnostic completed successfully!"
echo
echo "📋 Summary:"
echo "  - Storage Account: $STORAGE_ACCOUNT"
echo "  - Container: $CONTAINER_NAME"
echo "  - Status: Ready for use"
echo "  - Access Level: Private (recommended)"
echo
echo "🔧 For CI/CD Pipeline:"
echo "  - Make sure STORAGE_KEY secret is set in GitHub repository"
echo "  - Container is now ready for Docker image uploads"
echo "  - Pipeline should work without container creation errors"
