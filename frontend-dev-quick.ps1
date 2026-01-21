# Quick Frontend Development Script
# This script provides instant updates without rebuilding the container

Write-Host "`n=== Quick Frontend Development Mode ===" -ForegroundColor Cyan
Write-Host "`nThis mode uses hot-reload for instant updates without rebuilding!`n" -ForegroundColor Green

# Check if frontend container exists
$containerExists = docker ps -a --filter "name=ankurshala_frontend_local" --format "{{.Names}}"

if ($containerExists) {
    Write-Host "Restarting existing frontend container..." -ForegroundColor Yellow
    docker-compose restart frontend
    
    Write-Host "`nFrontend restarted! Waiting for it to be ready (10 seconds)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
} else {
    Write-Host "Starting frontend container for the first time..." -ForegroundColor Yellow
    Write-Host "Note: First-time setup will install dependencies (~2-3 minutes)`n" -ForegroundColor Gray
    
    # Start only frontend and its dependencies
    docker-compose up -d backend frontend
    
    Write-Host "`nWaiting for services to initialize (30 seconds)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
}

# Show status
Write-Host "`n=== Container Status ===" -ForegroundColor Cyan
docker-compose ps frontend

Write-Host "`n=== Development Mode Active! ===" -ForegroundColor Green
Write-Host "`n Your Frontend is now running in HOT-RELOAD mode:" -ForegroundColor White
Write-Host "   URL: http://localhost:3000`n" -ForegroundColor Green

Write-Host " How it works:" -ForegroundColor Cyan
Write-Host "   - Edit your code in ./frontend/" -ForegroundColor White
Write-Host "   - Changes will appear in browser INSTANTLY" -ForegroundColor White
Write-Host "   - No rebuild or restart needed!" -ForegroundColor White
Write-Host "   - Volume mounting keeps your code synced`n" -ForegroundColor White

Write-Host " View Live Logs:" -ForegroundColor Yellow
Write-Host "   docker-compose logs -f frontend`n" -ForegroundColor Cyan

Write-Host " Need to reinstall dependencies?" -ForegroundColor Yellow
Write-Host "   1. Stop container: docker-compose stop frontend" -ForegroundColor Cyan
Write-Host "   2. Remove volume: docker volume rm ankurshala-eduplatform_node_modules" -ForegroundColor Cyan
Write-Host "   3. Restart: docker-compose up -d frontend`n" -ForegroundColor Cyan

Write-Host "Happy coding! Your changes will reflect immediately." -ForegroundColor Green
