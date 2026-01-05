# AnkurShala - Docker Startup Script
# This script builds and starts all services in Docker containers

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  AnkurShala - Container Deployment" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Clean up any existing containers
Write-Host "🧹 Step 1: Cleaning up existing containers..." -ForegroundColor Yellow
docker-compose down -v
Write-Host "✅ Cleanup complete" -ForegroundColor Green
Write-Host ""

# Step 2: Build backend (this takes the longest)
Write-Host "🔨 Step 2: Building backend image..." -ForegroundColor Yellow
Write-Host "   This will take 5-10 minutes on first run..." -ForegroundColor Gray
docker-compose build backend
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Backend build failed!" -ForegroundColor Red
    Write-Host "   Check the error messages above" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Backend built successfully" -ForegroundColor Green
Write-Host ""

# Step 3: Build frontend
Write-Host "🔨 Step 3: Building frontend image..." -ForegroundColor Yellow
Write-Host "   This will take 3-5 minutes on first run..." -ForegroundColor Gray
docker-compose build frontend
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Frontend build failed!" -ForegroundColor Red
    Write-Host "   Check the error messages above" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Frontend built successfully" -ForegroundColor Green
Write-Host ""

# Step 4: Start all services
Write-Host "🚀 Step 4: Starting all services..." -ForegroundColor Yellow
docker-compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start services!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ All services started" -ForegroundColor Green
Write-Host ""

# Step 5: Wait for services to be healthy
Write-Host "⏳ Step 5: Waiting for services to be healthy..." -ForegroundColor Yellow
Write-Host "   This may take 30-60 seconds..." -ForegroundColor Gray
Start-Sleep -Seconds 30
Write-Host ""

# Step 6: Show service status
Write-Host "📊 Service Status:" -ForegroundColor Cyan
docker-compose ps
Write-Host ""

# Step 7: Check backend health
Write-Host "🏥 Checking backend health..." -ForegroundColor Yellow
$maxAttempts = 12
$attempt = 0
$backendHealthy = $false

while ($attempt -lt $maxAttempts -and -not $backendHealthy) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/actuator/health" -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $backendHealthy = $true
            Write-Host "✅ Backend is healthy!" -ForegroundColor Green
        }
    } catch {
        $attempt++
        if ($attempt -lt $maxAttempts) {
            Write-Host "   Attempt $attempt/$maxAttempts - Backend not ready yet, waiting..." -ForegroundColor Gray
            Start-Sleep -Seconds 10
        }
    }
}

if (-not $backendHealthy) {
    Write-Host "⚠️  Backend health check timed out" -ForegroundColor Yellow
    Write-Host "   Check logs with: docker-compose logs backend" -ForegroundColor Gray
}

Write-Host ""
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  ✅ Deployment Complete!" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "🌐 Access Points:" -ForegroundColor Cyan
Write-Host "   Frontend:  http://localhost:3000" -ForegroundColor White
Write-Host "   Backend:   http://localhost:8080/api" -ForegroundColor White
Write-Host "   Health:    http://localhost:8080/api/actuator/health" -ForegroundColor White
Write-Host "   MailHog:   http://localhost:8025" -ForegroundColor White
Write-Host ""
Write-Host "🔑 Demo Credentials:" -ForegroundColor Cyan
Write-Host "   Admin:    siddhartha@ankurshala.com / Maza@123" -ForegroundColor White
Write-Host "   Student:  student1@ankurshala.com / Maza@123" -ForegroundColor White
Write-Host "   Teacher:  teacher1@ankurshala.com / Maza@123" -ForegroundColor White
Write-Host ""
Write-Host "📋 Useful Commands:" -ForegroundColor Cyan
Write-Host "   View logs:     docker-compose logs -f" -ForegroundColor White
Write-Host "   Stop all:      docker-compose down" -ForegroundColor White
Write-Host "   Restart:       docker-compose restart [service]" -ForegroundColor White
Write-Host "   Status:        docker-compose ps" -ForegroundColor White
Write-Host ""
