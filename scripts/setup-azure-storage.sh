#!/bin/bash
# Azure Storage-based CI/CD Setup Script (Security Compliant)

set -Eeuo pipefail

# Colors
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log(){ case "$1" in INFO) echo -e "${BLUE}ℹ️  $2${NC}";; WARN) echo -e "${YELLOW}⚠️  $2${NC}";;
FAIL) echo -e "${RED}❌ $2${NC}";; PASS) echo -e "${GREEN}✅ $2${NC}";; esac; }

# Configuration - Use environment variables
STORAGE_ACCOUNT="${STORAGE_ACCOUNT:-ankurimage}"
STORAGE_KEY="${STORAGE_KEY:-}"
CONTAINER_NAME="${CONTAINER_NAME:-docker-images}"
VM_USER="${VM_USER:-AnkurshalaVM}"
VM_IP="${VM_IP:-74.225.207.72}"
VM_PASSWORD="${VM_PASSWORD:-}"

usage() {
    echo "Usage: $0 [setup-storage|test-storage|deploy]"
    echo "  setup-storage - Set up Azure Storage container for Docker images"
    echo "  test-storage  - Test Azure Storage connectivity"
    echo "  deploy        - Deploy using Azure Storage"
    echo
    echo "Environment Variables Required:"
    echo "  STORAGE_ACCOUNT - Azure Storage Account name"
    echo "  STORAGE_KEY     - Azure Storage Account key"
    echo "  VM_PASSWORD     - VM password for deployment"
    echo
    echo "Example:"
    echo "  export STORAGE_KEY='your-storage-key'"
    echo "  export VM_PASSWORD='your-vm-password'"
    echo "  $0 setup-storage"
    exit 1
}

check_environment() {
    if [[ -z "$STORAGE_KEY" ]]; then
        log FAIL "STORAGE_KEY environment variable is required"
        usage
    fi
    
    if [[ -z "$VM_PASSWORD" ]]; then
        log FAIL "VM_PASSWORD environment variable is required"
        usage
    fi
}

setup_storage() {
    log INFO "Setting up Azure Storage for Docker images..."
    
    check_environment
    
    # Install Azure CLI if not present
    if ! command -v az &> /dev/null; then
        log INFO "Installing Azure CLI..."
        curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
    fi
    
    # Login to Azure (you'll need to complete this manually)
    log INFO "Please complete Azure login:"
    az login
    
    # Create storage container
    log INFO "Creating storage container: $CONTAINER_NAME"
    az storage container create \
        --name "$CONTAINER_NAME" \
        --account-name "$STORAGE_ACCOUNT" \
        --account-key "$STORAGE_KEY" \
        --public-access blob
    
    log PASS "Azure Storage setup completed!"
    echo
    echo "📋 Storage Details:"
    echo "  Storage Account: $STORAGE_ACCOUNT"
    echo "  Container: $CONTAINER_NAME"
}

test_storage() {
    log INFO "Testing Azure Storage connectivity..."
    
    check_environment
    
    # Test container access
    az storage blob list \
        --container-name "$CONTAINER_NAME" \
        --account-name "$STORAGE_ACCOUNT" \
        --account-key "$STORAGE_KEY" \
        --output table
    
    log PASS "Azure Storage test successful"
}

# Enhanced deployment script with Azure Storage support
deploy_with_storage() {
    log INFO "Starting deployment with Azure Storage support..."
    
    check_environment
    
    # Create deployment script
    cat > deploy-with-azure-storage.sh << 'EOF'
#!/bin/bash
set -Eeuo pipefail

# Colors
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log(){ case "$1" in INFO) echo -e "${BLUE}ℹ️  $2${NC}";; WARN) echo -e "${YELLOW}⚠️  $2${NC}";;
FAIL) echo -e "${RED}❌ $2${NC}";; PASS) echo -e "${GREEN}✅ $2${NC}";; esac; }

# Configuration - Use environment variables
STORAGE_ACCOUNT="${STORAGE_ACCOUNT:-ankurimage}"
STORAGE_KEY="${STORAGE_KEY:-}"
CONTAINER_NAME="${CONTAINER_NAME:-docker-images}"
VM_USER="${VM_USER:-AnkurshalaVM}"
VM_IP="${VM_IP:-74.225.207.72}"

# Resource management functions
check_disk_space() {
    local required_gb="${1:-5}"
    local available_gb=$(df / | awk 'NR==2 {print int($4/1024/1024)}')
    
    log INFO "Checking disk space: ${available_gb}GB available, ${required_gb}GB required"
    
    if [ "$available_gb" -lt "$required_gb" ]; then
        log WARN "Insufficient disk space. Available: ${available_gb}GB, Required: ${required_gb}GB"
        return 1
    fi
    return 0
}

cleanup_docker_resources() {
    log INFO "Cleaning up Docker resources..."
    
    # Remove unused images (keep last 3 versions)
    docker images --format "table {{.Repository}}:{{.Tag}}\t{{.CreatedAt}}" | \
        grep -E "(ankurshala|none)" | \
        tail -n +4 | \
        awk '{print $1}' | \
        head -n -3 | \
        xargs -r docker rmi || true
    
    # Clean up build cache
    docker builder prune -f || true
    
    # Clean up unused containers and networks
    docker system prune -f || true
    
    log PASS "Docker cleanup completed"
}

ensure_resources() {
    log INFO "Ensuring sufficient resources for deployment..."
    
    if ! check_disk_space 8; then
        log WARN "Disk space insufficient, cleaning up..."
        cleanup_docker_resources
        
        if ! check_disk_space 5; then
            log FAIL "Still insufficient disk space after cleanup"
            exit 1
        fi
    fi
    
    log PASS "Resource check passed"
}

# Pull image from Azure Storage
pull_from_storage() {
    local service="$1"
    local tag="$2"
    
    log INFO "Pulling ${service} image from Azure Storage: ${tag}"
    
    # Download image from Azure Storage
    local blob_name="${service}-${tag}.tar"
    local local_file="/tmp/${blob_name}"
    
    # Download blob
    az storage blob download \
        --container-name "$CONTAINER_NAME" \
        --name "$blob_name" \
        --file "$local_file" \
        --account-name "$STORAGE_ACCOUNT" \
        --account-key "$STORAGE_KEY" || {
        log WARN "Failed to pull from Azure Storage, falling back to local build"
        return 1
    }
    
    # Load image into Docker
    docker load -i "$local_file"
    rm -f "$local_file"
    
    log PASS "Successfully pulled ${service} from Azure Storage"
    return 0
}

# Push image to Azure Storage
push_to_storage() {
    local service="$1"
    local tag="$2"
    
    log INFO "Pushing ${service} image to Azure Storage: ${tag}"
    
    # Save image to tar file
    local blob_name="${service}-${tag}.tar"
    local local_file="/tmp/${blob_name}"
    
    docker save "ankurshala/${service}:${tag}" -o "$local_file"
    
    # Upload to Azure Storage
    az storage blob upload \
        --container-name "$CONTAINER_NAME" \
        --name "$blob_name" \
        --file "$local_file" \
        --account-name "$STORAGE_ACCOUNT" \
        --account-key "$STORAGE_KEY" || {
        log FAIL "Failed to push to Azure Storage"
        rm -f "$local_file"
        exit 1
    }
    
    rm -f "$local_file"
    log PASS "Successfully pushed ${service} to Azure Storage"
}

# Main deployment logic
main() {
    log INFO "🚀 Starting AnkurShala Azure Storage Deployment"
    echo "================================================================="
    
    # Ensure resources
    ensure_resources
    
    # Get current SHAs
    FE_SHA=$(git rev-parse --short HEAD:frontend 2>/dev/null || git rev-parse --short HEAD)
    BE_SHA=$(git rev-parse --short HEAD:backend  2>/dev/null || git rev-parse --short HEAD)
    
    FRONTEND_TAG="prod-${FE_SHA}"
    BACKEND_TAG="prod-${BE_SHA}"
    
    log INFO "Detected SHAs → frontend:$FE_SHA backend:$BE_SHA"
    
    # Try to pull from Azure Storage first
    if pull_from_storage "frontend" "$FRONTEND_TAG"; then
        log PASS "Using frontend image from Azure Storage"
    else
        log INFO "Building frontend locally..."
        docker compose -f docker-compose.prod.yml --env-file .env-prod build --no-cache frontend
        push_to_storage "frontend" "$FRONTEND_TAG"
    fi
    
    if pull_from_storage "backend" "$BACKEND_TAG"; then
        log PASS "Using backend image from Azure Storage"
    else
        log INFO "Building backend locally..."
        docker compose -f docker-compose.prod.yml --env-file .env-prod build --no-cache backend
        push_to_storage "backend" "$BACKEND_TAG"
    fi
    
    # Deploy services
    log INFO "Deploying services..."
    docker compose -f docker-compose.prod.yml --env-file .env-prod up -d
    
    # Wait for health checks
    log INFO "Waiting for services to be healthy..."
    sleep 30
    
    # Verify deployment
    if curl -f https://ankurshala.com/health; then
        log PASS "🎉 Deployment successful!"
    else
        log FAIL "Deployment verification failed"
        exit 1
    fi
    
    # Cleanup
    cleanup_docker_resources
}

main "$@"
EOF

    chmod +x deploy-with-azure-storage.sh
    
    log PASS "Azure Storage deployment script created!"
    echo
    echo "📋 Next steps:"
    echo "1. Complete Azure login: az login"
    echo "2. Set up storage: ./scripts/setup-azure-storage.sh setup-storage"
    echo "3. Deploy: ./deploy-with-azure-storage.sh"
}

# Main script
case "${1:-}" in
    setup-storage) setup_storage ;;
    test-storage) test_storage ;;
    deploy) deploy_with_storage ;;
    *) usage ;;
esac