# Ankurshala - Docker Health Check and Start Script
# Run this script after Docker Desktop is started

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   Ankurshala Health Check & Startup" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Check if Docker is running
Write-Host "🔍 Step 1: Checking Docker status..." -ForegroundColor Yellow
try {
    docker info 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Docker is running`n" -ForegroundColor Green
    } else {
        Write-Host "❌ Docker is not running!" -ForegroundColor Red
        Write-Host "Please start Docker Desktop first, then run this script again.`n" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "❌ Docker is not accessible!" -ForegroundColor Red
    Write-Host "Please start Docker Desktop first, then run this script again.`n" -ForegroundColor Yellow
    exit 1
}

# Step 2: Check container status
Write-Host "🔍 Step 2: Checking container status..." -ForegroundColor Yellow
$containers = docker ps -a --filter "name=ankurshala" --format "{{.Names}}" 2>$null
if ($containers) {
    Write-Host "Found existing containers`n" -ForegroundColor Green
} else {
    Write-Host "No existing containers found`n" -ForegroundColor Gray
}

# Step 3: Start/Restart containers
Write-Host "🚀 Step 3: Starting all services..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Services started successfully!`n" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to start services!`n" -ForegroundColor Red
    Write-Host "Run 'docker-compose logs' to see error details`n" -ForegroundColor Yellow
    exit 1
}

# Step 4: Wait for services to be ready
Write-Host "⏳ Step 4: Waiting for services to initialize (15 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Step 5: Check container health
Write-Host "`n📊 Step 5: Container Status:" -ForegroundColor Yellow
docker ps --filter "name=ankurshala" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Step 6: Test connectivity
Write-Host "`n🌐 Step 6: Testing service connectivity..." -ForegroundColor Yellow

Write-Host "`nTesting Frontend (http://localhost:3000)..." -ForegroundColor Gray
try {
    $frontend = Invoke-WebRequest -Uri "http://localhost:3000" -Method Head -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($frontend.StatusCode -eq 200 -or $frontend.StatusCode -eq 307) {
        Write-Host "✅ Frontend is accessible" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Frontend returned status: $($frontend.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Frontend is not responding yet. It may still be starting..." -ForegroundColor Red
}

Write-Host "`nTesting Backend (http://localhost:8080/api/actuator/health)..." -ForegroundColor Gray
try {
    $backend = Invoke-WebRequest -Uri "http://localhost:8080/api/actuator/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($backend.StatusCode -eq 200) {
        Write-Host "✅ Backend is accessible and healthy" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Backend returned status: $($backend.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Backend is not responding yet. It may still be starting..." -ForegroundColor Red
    Write-Host "   Backend typically takes 30-60 seconds to fully start" -ForegroundColor Gray
}

# Final Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   Summary" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "🌐 Service URLs:" -ForegroundColor Green
Write-Host "  Frontend:  http://localhost:3000" -ForegroundColor White
Write-Host "  Backend:   http://localhost:8080/api" -ForegroundColor White
Write-Host "  API Health: http://localhost:8080/api/actuator/health" -ForegroundColor White
Write-Host "  MailHog:   http://localhost:8025" -ForegroundColor White
Write-Host "  Grafana:   http://localhost:3001" -ForegroundColor White

Write-Host "`n💡 Useful Commands:" -ForegroundColor Yellow
Write-Host "  Check status:       docker-compose ps" -ForegroundColor Gray
Write-Host "  View all logs:      docker-compose logs -f" -ForegroundColor Gray
Write-Host "  View backend logs:  docker-compose logs -f backend" -ForegroundColor Gray
Write-Host "  View frontend logs: docker-compose logs -f frontend" -ForegroundColor Gray
Write-Host "  Restart backend:    docker-compose restart backend" -ForegroundColor Gray
Write-Host "  Stop all:           docker-compose down" -ForegroundColor Gray

Write-Host "`n📝 Note: If services are not accessible yet, wait 30-60 seconds" -ForegroundColor Yellow
Write-Host "   for the backend to fully initialize, then refresh your browser.`n" -ForegroundColor Yellow
