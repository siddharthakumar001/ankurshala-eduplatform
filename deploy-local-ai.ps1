# Local Deployment Script for AnkurShala with AI Features
# This script sets up and deploys the application locally with AI capabilities

param(
    [switch]$SkipAzureSetup = $false,
    [switch]$BuildOnly = $false
)

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "AnkurShala Local Deployment with AI" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "Checking Docker..." -ForegroundColor Yellow
$dockerRunning = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker is not running. Please start Docker Desktop." -ForegroundColor Red
    exit 1
}
Write-Host "Docker is running" -ForegroundColor Green
Write-Host ""

# Check if .env file exists
if (-not (Test-Path ".env")) {
    Write-Host "Creating .env file from template..." -ForegroundColor Yellow
    if (Test-Path "env.example") {
        Copy-Item "env.example" ".env"
        Write-Host ".env file created. Please update it with your configuration." -ForegroundColor Yellow
    } else {
        Write-Host "WARNING: env.example not found. Creating basic .env file..." -ForegroundColor Yellow
        @"
# Database Configuration
DB_HOST=postgres
DB_PORT=5432
DB_NAME=ankurshala
DB_USERNAME=ankur
DB_PASSWORD=password

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT Configuration
JWT_SECRET=myVeryLongAndSecureSecretKeyForHS512AlgorithmThatIsAtLeast64CharactersLongAndSecure123456789

# AI Configuration
AI_ENABLED=false
AI_PROVIDER=openai
AI_DEV_MODE=true

# Azure OpenAI (if using)
AZURE_OPENAI_ENDPOINT=
AZURE_OPENAI_API_KEY=
AZURE_OPENAI_DEPLOYMENT_NAME=gpt-4o-mini

# Azure Speech (if using)
AZURE_SPEECH_KEY=
AZURE_SPEECH_REGION=
AZURE_SPEECH_LANGUAGE=en-IN
"@ | Out-File -FilePath ".env" -Encoding utf8
    }
    Write-Host ""
}

# Setup Azure services if requested
if (-not $SkipAzureSetup) {
    Write-Host "Do you want to set up Azure AI services? (y/n)" -ForegroundColor Yellow
    $setupAzure = Read-Host
    if ($setupAzure -eq "y" -or $setupAzure -eq "Y") {
        Write-Host "Running Azure setup script..." -ForegroundColor Yellow
        if (Test-Path "setup-azure-ai.ps1") {
            .\setup-azure-ai.ps1
            Write-Host ""
            Write-Host "Azure setup complete. Updating .env file..." -ForegroundColor Yellow
            if (Test-Path ".env.azure") {
                $azureEnv = Get-Content ".env.azure" -Raw
                Add-Content ".env" "`n# Azure Configuration`n$azureEnv"
                Write-Host "Azure configuration added to .env" -ForegroundColor Green
            }
        } else {
            Write-Host "WARNING: setup-azure-ai.ps1 not found. Skipping Azure setup." -ForegroundColor Yellow
        }
        Write-Host ""
    }
}

# Stop existing containers
Write-Host "Stopping existing containers..." -ForegroundColor Yellow
docker-compose down
Write-Host ""

# Build images
Write-Host "Building Docker images..." -ForegroundColor Yellow
docker-compose build --no-cache
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker build failed" -ForegroundColor Red
    exit 1
}
Write-Host "Build complete" -ForegroundColor Green
Write-Host ""

if ($BuildOnly) {
    Write-Host "Build only mode - exiting" -ForegroundColor Yellow
    exit 0
}

# Start services
Write-Host "Starting services..." -ForegroundColor Yellow
docker-compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to start services" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Wait for services to be healthy
Write-Host "Waiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Check service health
Write-Host "Checking service health..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$backendHealthy = $false

while ($attempt -lt $maxAttempts -and -not $backendHealthy) {
    Start-Sleep -Seconds 5
    $health = docker exec ankurshala_backend_local wget -q -O- http://localhost:8080/api/actuator/health 2>&1
    if ($LASTEXITCODE -eq 0) {
        $backendHealthy = $true
        Write-Host "Backend is healthy" -ForegroundColor Green
    } else {
        $attempt++
        Write-Host "Waiting for backend... ($attempt/$maxAttempts)" -ForegroundColor Yellow
    }
}

if (-not $backendHealthy) {
    Write-Host "WARNING: Backend health check failed. Services may still be starting." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Deployment Complete!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Services:" -ForegroundColor Yellow
Write-Host "  - Backend API: http://localhost:8080/api" -ForegroundColor White
Write-Host "  - Frontend: http://localhost:3000" -ForegroundColor White
Write-Host "  - Prometheus: http://localhost:9090" -ForegroundColor White
Write-Host "  - Grafana: http://localhost:3001 (admin/admin)" -ForegroundColor White
Write-Host "  - MailHog: http://localhost:8025" -ForegroundColor White
Write-Host ""
Write-Host "To view logs:" -ForegroundColor Yellow
Write-Host "  docker-compose logs -f backend" -ForegroundColor White
Write-Host "  docker-compose logs -f frontend" -ForegroundColor White
Write-Host ""
Write-Host "To stop services:" -ForegroundColor Yellow
Write-Host "  docker-compose down" -ForegroundColor White
Write-Host ""

