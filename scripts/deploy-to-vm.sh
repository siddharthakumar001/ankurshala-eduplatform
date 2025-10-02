#!/bin/bash
# Production Deployment Script for AnkurShala VM (Security Compliant)
# This script handles resource management and deployment

set -Eeuo pipefail

# Colors
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log(){ case "$1" in INFO) echo -e "${BLUE}ℹ️  $2${NC}";; WARN) echo -e "${YELLOW}⚠️  $2${NC}";;
FAIL) echo -e "${RED}❌ $2${NC}";; PASS) echo -e "${GREEN}✅ $2${NC}";; esac; }

# Configuration - Use environment variables
VM_USER="${VM_USER:-AnkurshalaVM}"
VM_IP="${VM_IP:-74.225.207.72}"
VM_PASSWORD="${VM_PASSWORD:-}"
STORAGE_ACCOUNT="${STORAGE_ACCOUNT:-ankurimage}"
STORAGE_KEY="${STORAGE_KEY:-}"
CONTAINER_NAME="${CONTAINER_NAME:-docker-images}"

usage() {
    echo "Usage: $0 [deploy|cleanup|status|health]"
    echo "  deploy  - Deploy application to production VM"
    echo "  cleanup - Clean up resources on VM"
    echo "  status  - Check VM status"
    echo "  health  - Check application health"
    echo
    echo "Environment Variables Required:"
    echo "  VM_PASSWORD     - VM password for deployment"
    echo "  STORAGE_KEY     - Azure Storage Account key"
    echo
    echo "Example:"
    echo "  export VM_PASSWORD='your-vm-password'"
    echo "  export STORAGE_KEY='your-storage-key'"
    echo "  $0 deploy"
    exit 1
}

check_environment() {
    if [[ -z "$VM_PASSWORD" ]]; then
        log FAIL "VM_PASSWORD environment variable is required"
        usage
    fi
}

# SSH helper function
ssh_exec() {
    local command="$1"
    sshpass -p "$VM_PASSWORD" ssh -o StrictHostKeyChecking=no "$VM_USER@$VM_IP" "$command"
}

# Deploy to production VM
deploy() {
    log INFO "🚀 Starting deployment to production VM..."
    
    check_environment
    
    # Check VM connectivity
    if ! ping -c 1 "$VM_IP" &>/dev/null; then
        log FAIL "Cannot reach VM at $VM_IP"
        exit 1
    fi
    
    log INFO "VM connectivity verified"
    
    # Update code on VM
    log INFO "Updating code on VM..."
    ssh_exec "cd /opt/ankurshala && git pull origin ankurshala/prod-1.0"
    
    # Run cleanup first
    log INFO "Running cleanup on VM..."
    ssh_exec "cd /opt/ankurshala && ./scripts/deploy-production-enhanced.sh --cleanup-only"
    
    # Deploy application
    log INFO "Deploying application..."
    ssh_exec "cd /opt/ankurshala && ./scripts/deploy-production-enhanced.sh"
    
    # Wait for deployment
    log INFO "Waiting for deployment to complete..."
    sleep 60
    
    # Health check
    if curl -f "https://ankurshala.com/health" &>/dev/null; then
        log PASS "🎉 Deployment successful! Application is healthy."
    else
        log WARN "Health check failed, but deployment may still be in progress"
    fi
}

# Cleanup resources on VM
cleanup() {
    log INFO "🧹 Cleaning up resources on VM..."
    
    check_environment
    
    ssh_exec "cd /opt/ankurshala && ./scripts/deploy-production-enhanced.sh --cleanup-only"
    
    log PASS "Cleanup completed"
}

# Check VM status
status() {
    log INFO "📊 Checking VM status..."
    
    check_environment
    
    echo "VM Status:"
    ssh_exec "uptime && free -h && df -h /"
    
    echo
    echo "Docker Status:"
    ssh_exec "docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'"
    
    echo
    echo "Application Status:"
    ssh_exec "cd /opt/ankurshala && docker compose -f docker-compose.prod.yml --env-file .env-prod ps"
}

# Check application health
health() {
    log INFO "🏥 Checking application health..."
    
    # Check if application is responding
    if curl -f "https://ankurshala.com/health" &>/dev/null; then
        log PASS "Application is healthy and responding"
    else
        log FAIL "Application health check failed"
        exit 1
    fi
    
    # Check individual services
    log INFO "Checking individual services..."
    check_environment
    ssh_exec "cd /opt/ankurshala && docker compose -f docker-compose.prod.yml --env-file .env-prod ps --services --filter status=running"
}

# Install required tools
install_tools() {
    log INFO "Installing required tools..."
    
    # Install sshpass if not present
    if ! command -v sshpass &> /dev/null; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            brew install hudochenkov/sshpass/sshpass
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            sudo apt-get update && sudo apt-get install -y sshpass
        fi
    fi
    
    # Install Azure CLI if not present
    if ! command -v az &> /dev/null; then
        curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
    fi
    
    log PASS "Tools installation completed"
}

# Main script
case "${1:-}" in
    deploy) 
        install_tools
        deploy 
        ;;
    cleanup) 
        install_tools
        cleanup 
        ;;
    status) 
        install_tools
        status 
        ;;
    health) 
        health 
        ;;
    *) 
        usage 
        ;;
esac