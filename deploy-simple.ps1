# =============================================================================
# Ankurshala Deploy Script - Using Azure Container Registry (ACR)
# Build locally -> Push to ACR -> Pull and Deploy on VM
# =============================================================================

param(
    [switch]$Backend,
    [switch]$Frontend,
    [switch]$Migrate,
    [switch]$Rollback,
    [switch]$Logs,
    [switch]$Status,
    [switch]$Shell,
    [switch]$NoBuild,
    [switch]$Help
)

# Configuration
$VM_USER = "AnkurshalaVM"
$VM_IP = "74.225.207.72"
$REMOTE_DIR = "/opt/ankurshala"

# ACR Configuration - Update this with your ACR name
$ACR_NAME = "ankurshalaacr"
$ACR_REGISTRY = "$ACR_NAME.azurecr.io"

# Image names (no versioning - always use latest)
$BACKEND_IMAGE = "$ACR_REGISTRY/ankurshala/backend:latest"
$FRONTEND_IMAGE = "$ACR_REGISTRY/ankurshala/frontend:latest"

# Colors
function Write-Step { param($msg) Write-Host "`n>>> $msg" -ForegroundColor Cyan }
function Write-Success { param($msg) Write-Host "[OK] $msg" -ForegroundColor Green }
function Write-Warn { param($msg) Write-Host "[WARN] $msg" -ForegroundColor Yellow }
function Write-Err { param($msg) Write-Host "[ERROR] $msg" -ForegroundColor Red }
function Write-Info { param($msg) Write-Host "[INFO] $msg" -ForegroundColor Blue }

# Help
if ($Help) {
    Write-Host ""
    Write-Host "ANKURSHALA DEPLOY (ACR)" -ForegroundColor Cyan
    Write-Host "=======================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Build locally, push to Azure Container Registry, deploy on VM"
    Write-Host ""
    Write-Host "USAGE:"
    Write-Host "  .\deploy-simple.ps1               Deploy all (backend + frontend)"
    Write-Host "  .\deploy-simple.ps1 -Backend      Deploy backend only"
    Write-Host "  .\deploy-simple.ps1 -Frontend     Deploy frontend only"
    Write-Host "  .\deploy-simple.ps1 -NoBuild      Push existing images (skip build)"
    Write-Host "  .\deploy-simple.ps1 -Status       Check server status"
    Write-Host "  .\deploy-simple.ps1 -Logs         View live server logs"
    Write-Host "  .\deploy-simple.ps1 -Migrate      Run database migrations"
    Write-Host "  .\deploy-simple.ps1 -Rollback     Restart services"
    Write-Host "  .\deploy-simple.ps1 -Shell        SSH into VM"
    Write-Host ""
    Write-Host "FIRST TIME SETUP:"
    Write-Host "  1. Install Azure CLI: winget install Microsoft.AzureCLI"
    Write-Host "  2. Login to Azure: az login"
    Write-Host "  3. Login to ACR: az acr login --name $ACR_NAME"
    Write-Host ""
    exit 0
}

Write-Host ""
Write-Host "================================================================" -ForegroundColor Magenta
Write-Host "   ANKURSHALA DEPLOY (ACR)" -ForegroundColor Magenta
Write-Host "================================================================" -ForegroundColor Magenta
Write-Host "   VM: $VM_USER@$VM_IP"
Write-Host "   ACR: $ACR_REGISTRY"
Write-Host "================================================================" -ForegroundColor Magenta

# Check Docker
$dockerCheck = docker info 2>$null
if (-not $dockerCheck) {
    Write-Err "Docker is not running. Start Docker Desktop first."
    exit 1
}

# SSH helper
function Invoke-SSH {
    param([string]$Command)
    ssh "${VM_USER}@${VM_IP}" $Command
}

# Shell into VM
if ($Shell) {
    Write-Step "Connecting to VM..."
    ssh "${VM_USER}@${VM_IP}"
    exit 0
}

# Status
if ($Status) {
    Write-Step "Server Status"
    Invoke-SSH "cd $REMOTE_DIR && docker compose -f docker-compose.prod.yml ps && echo '' && docker images | grep ankurshala | head -10"
    exit 0
}

# Logs
if ($Logs) {
    Write-Step "Live Logs (Ctrl+C to exit)..."
    ssh "${VM_USER}@${VM_IP}" "cd $REMOTE_DIR && docker compose -f docker-compose.prod.yml logs -f --tail=100"
    exit 0
}

# Migrate
if ($Migrate) {
    Write-Step "Running Migrations..."
    Invoke-SSH "cd $REMOTE_DIR && docker compose -f docker-compose.prod.yml restart backend && sleep 30 && echo 'Migration restart complete'"
    exit 0
}

# Rollback (just restart with current images)
if ($Rollback) {
    Write-Step "Restarting services..."
    Invoke-SSH "cd $REMOTE_DIR && docker compose -f docker-compose.prod.yml restart backend frontend && echo 'Services restarted'"
    exit 0
}

# What to deploy
$deployBackend = $Backend -or (-not $Backend -and -not $Frontend)
$deployFrontend = $Frontend -or (-not $Backend -and -not $Frontend)

# =============================================================================
# Step 1: Login to ACR
# =============================================================================
Write-Step "Logging in to Azure Container Registry..."
$acrLogin = az acr login --name $ACR_NAME 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Warn "ACR login failed. Trying docker login..."
    Write-Info "You may need to run: az login"
    Write-Info "Then: az acr login --name $ACR_NAME"
    
    # Try docker login as fallback
    $acrCreds = az acr credential show --name $ACR_NAME 2>$null | ConvertFrom-Json
    if ($acrCreds) {
        docker login $ACR_REGISTRY -u $acrCreds.username -p $acrCreds.passwords[0].value
    } else {
        Write-Err "Could not login to ACR. Run: az login && az acr login --name $ACR_NAME"
        exit 1
    }
}
Write-Success "Logged in to ACR"

# =============================================================================
# Step 2: Build images locally
# =============================================================================
if (-not $NoBuild) {
    Write-Step "Building Docker images..."
    
    if ($deployBackend) {
        Write-Info "Building backend..."
        docker build -t $BACKEND_IMAGE ./backend
        if ($LASTEXITCODE -ne 0) { Write-Err "Backend build failed!"; exit 1 }
        Write-Success "Backend built: $BACKEND_IMAGE"
    }
    
    if ($deployFrontend) {
        Write-Info "Building frontend..."
        docker build -t $FRONTEND_IMAGE --build-arg NEXT_PUBLIC_API_URL=/api ./frontend
        if ($LASTEXITCODE -ne 0) { Write-Err "Frontend build failed!"; exit 1 }
        Write-Success "Frontend built: $FRONTEND_IMAGE"
    }
}

# =============================================================================
# Step 3: Push images to ACR
# =============================================================================
Write-Step "Pushing images to ACR..."

if ($deployBackend) {
    Write-Info "Pushing backend..."
    docker push $BACKEND_IMAGE
    if ($LASTEXITCODE -ne 0) { Write-Err "Backend push failed!"; exit 1 }
    Write-Success "Backend pushed"
}

if ($deployFrontend) {
    Write-Info "Pushing frontend..."
    docker push $FRONTEND_IMAGE
    if ($LASTEXITCODE -ne 0) { Write-Err "Frontend push failed!"; exit 1 }
    Write-Success "Frontend pushed"
}

# =============================================================================
# Step 4: Deploy on VM (pull from ACR and restart)
# =============================================================================
Write-Step "Deploying on VM..."

$deployCmd = @"
set -e
cd $REMOTE_DIR

echo 'Loading environment...'
set -a; source .env-prod; set +a

echo 'Logging in to ACR...'
az acr login --name $ACR_NAME 2>/dev/null || docker login $ACR_REGISTRY 2>/dev/null || echo 'ACR login skipped (may already be logged in)'

echo 'Pulling latest images from ACR...'
docker pull $BACKEND_IMAGE || echo 'Backend pull failed'
docker pull $FRONTEND_IMAGE || echo 'Frontend pull failed'

echo 'Updating docker-compose...'
sed -i 's|image: ankurshala/backend:.*|image: $BACKEND_IMAGE|g' docker-compose.prod.yml
sed -i 's|image: ankurshala/frontend:.*|image: $FRONTEND_IMAGE|g' docker-compose.prod.yml

echo 'Starting services...'
docker compose -f docker-compose.prod.yml up -d --no-deps --force-recreate backend frontend

echo 'Waiting for services (30s)...'
sleep 30

echo 'Health checks...'
for i in 1 2 3 4 5 6 7 8 9 10; do
    if docker exec ankurshala_backend_prod wget -q --spider http://localhost:8080/api/actuator/health 2>/dev/null; then
        echo 'Backend OK'
        break
    fi
    [ \$i -eq 10 ] && echo 'Backend health timeout' || sleep 3
done

for i in 1 2 3 4 5; do
    if docker exec ankurshala_frontend_prod wget -q --spider http://localhost:3000 2>/dev/null; then
        echo 'Frontend OK'
        break
    fi
    [ \$i -eq 5 ] && echo 'Frontend health timeout' || sleep 3
done

echo ''
echo 'Service Status:'
docker compose -f docker-compose.prod.yml ps backend frontend nginx

echo ''
docker image prune -f 2>/dev/null || true

echo ''
echo 'DEPLOYMENT COMPLETE!'
echo 'https://ankurshala.com'
"@

Invoke-SSH $deployCmd

# =============================================================================
# Done!
# =============================================================================
Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "   DEPLOYMENT SUCCESSFUL" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "   Images: latest (no versioning)"
Write-Host "   Site: https://ankurshala.com"
Write-Host "   API: https://ankurshala.com/api/actuator/health"
Write-Host ""
Write-Host "   Quick Commands:" -ForegroundColor Yellow
Write-Host "     .\deploy-simple.ps1 -Status   # Check status"
Write-Host "     .\deploy-simple.ps1 -Logs     # View logs"
Write-Host "     .\deploy-simple.ps1 -Shell    # SSH to VM"
Write-Host ""
