#!/usr/bin/env pwsh
# run-e2e-local.ps1
# One-command script to run full local E2E testing

param(
    [switch]$SkipBackendStart,
    [switch]$SkipFrontendStart,
    [switch]$SkipTests,
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Ankurshala E2E Local Testing" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$BACKEND_DIR = "backend"
$FRONTEND_DIR = "frontend"
$BACKEND_PORT = 8080
$FRONTEND_PORT = 3000
$BACKEND_HEALTH = "http://localhost:$BACKEND_PORT/api/actuator/health"
$FRONTEND_HEALTH = "http://localhost:$FRONTEND_PORT"
$MAX_WAIT_SECONDS = 120

# Helper function to check if port is in use
function Test-PortInUse {
    param([int]$Port)
    
    $connections = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object { $_.LocalPort -eq $Port }
    return $connections.Count -gt 0
}

# Helper function to wait for service health
function Wait-ForService {
    param(
        [string]$Url,
        [string]$ServiceName,
        [int]$MaxSeconds = 120
    )
    
    Write-Host "Waiting for $ServiceName to be ready..." -ForegroundColor Yellow
    $elapsed = 0
    
    while ($elapsed -lt $MaxSeconds) {
        try {
            $response = Invoke-WebRequest -Uri $Url -Method Get -TimeoutSec 5 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 204) {
                Write-Host "✓ $ServiceName is ready!" -ForegroundColor Green
                return $true
            }
        }
        catch {
            # Service not ready yet
        }
        
        Start-Sleep -Seconds 2
        $elapsed += 2
        Write-Host "." -NoNewline
    }
    
    Write-Host ""
    Write-Host "✗ $ServiceName failed to start within $MaxSeconds seconds" -ForegroundColor Red
    return $false
}

# Step 1: Check prerequisites
Write-Host "Step 1: Checking prerequisites..." -ForegroundColor Cyan

# Check if in project root
if (-not (Test-Path "backend") -or -not (Test-Path "frontend")) {
    Write-Host "✗ Error: Must run from project root directory" -ForegroundColor Red
    exit 1
}

# Check Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✓ Java: $javaVersion" -ForegroundColor Green
}
catch {
    Write-Host "✗ Java not found. Please install Java 17+" -ForegroundColor Red
    exit 1
}

# Check Node
try {
    $nodeVersion = node --version
    Write-Host "✓ Node: $nodeVersion" -ForegroundColor Green
}
catch {
    Write-Host "✗ Node.js not found. Please install Node.js 18+" -ForegroundColor Red
    exit 1
}

# Check PostgreSQL (optional - assume Docker or external)
Write-Host "✓ Assuming PostgreSQL is running (Docker or external)" -ForegroundColor Green

Write-Host ""

# Step 2: Start Backend
if (-not $SkipBackendStart) {
    Write-Host "Step 2: Starting backend..." -ForegroundColor Cyan
    
    if (Test-PortInUse -Port $BACKEND_PORT) {
        Write-Host "✓ Backend already running on port $BACKEND_PORT" -ForegroundColor Yellow
    }
    else {
        Write-Host "Starting Spring Boot backend..." -ForegroundColor Yellow
        
        Push-Location $BACKEND_DIR
        
        # Set E2E environment variables
        $env:SEED_E2E_DATA = "true"
        $env:AI_DEV_MODE = "true"
        $env:SPRING_PROFILES_ACTIVE = "local"
        
        # Start backend in background
        $backendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", ".\mvnw.cmd spring-boot:run" -PassThru -WindowStyle Minimized
        
        Pop-Location
        
        # Wait for backend health
        if (-not (Wait-ForService -Url $BACKEND_HEALTH -ServiceName "Backend" -MaxSeconds $MAX_WAIT_SECONDS)) {
            Write-Host "✗ Backend failed to start. Check logs in backend/logs/" -ForegroundColor Red
            exit 1
        }
        
        Write-Host "✓ E2E test data seeded automatically" -ForegroundColor Green
        Write-Host "✓ DEV AI Provider enabled (no API costs)" -ForegroundColor Green
    }
}
else {
    Write-Host "Step 2: Skipping backend start (--SkipBackendStart)" -ForegroundColor Yellow
}

Write-Host ""

# Step 3: Start Frontend
if (-not $SkipFrontendStart) {
    Write-Host "Step 3: Starting frontend..." -ForegroundColor Cyan
    
    if (Test-PortInUse -Port $FRONTEND_PORT) {
        Write-Host "✓ Frontend already running on port $FRONTEND_PORT" -ForegroundColor Yellow
    }
    else {
        Write-Host "Starting Next.js frontend..." -ForegroundColor Yellow
        
        Push-Location $FRONTEND_DIR
        
        # Install dependencies if needed
        if (-not (Test-Path "node_modules")) {
            Write-Host "Installing frontend dependencies..." -ForegroundColor Yellow
            npm install
        }
        
        # Start frontend in background
        $frontendProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "npm run dev" -PassThru -WindowStyle Minimized
        
        Pop-Location
        
        # Wait for frontend health
        if (-not (Wait-ForService -Url $FRONTEND_HEALTH -ServiceName "Frontend" -MaxSeconds $MAX_WAIT_SECONDS)) {
            Write-Host "✗ Frontend failed to start. Check terminal output." -ForegroundColor Red
            exit 1
        }
    }
}
else {
    Write-Host "Step 3: Skipping frontend start (--SkipFrontendStart)" -ForegroundColor Yellow
}

Write-Host ""

# Step 4: Display test credentials
Write-Host "Step 4: Test Credentials" -ForegroundColor Cyan
Write-Host "=========================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Student 1: student-e2e1@ankurshala.com / Test@123" -ForegroundColor Green
Write-Host "Student 2: student-e2e2@ankurshala.com / Test@123" -ForegroundColor Green
Write-Host "Admin:     admin-e2e@ankurshala.com / Test@123" -ForegroundColor Green
Write-Host "Teacher 1: teacher-e2e1@ankurshala.com / Test@123" -ForegroundColor Green
Write-Host "Teacher 2: teacher-e2e2@ankurshala.com / Test@123" -ForegroundColor Green
Write-Host ""

# Step 5: Run E2E Tests
if (-not $SkipTests) {
    Write-Host "Step 5: Running Playwright E2E tests..." -ForegroundColor Cyan
    
    Push-Location $FRONTEND_DIR
    
    # Install Playwright browsers if needed
    if (-not (Test-Path "$env:USERPROFILE\AppData\Local\ms-playwright")) {
        Write-Host "Installing Playwright browsers (first time only)..." -ForegroundColor Yellow
        npx playwright install
    }
    
    # Run tests
    Write-Host "Executing E2E test suite..." -ForegroundColor Yellow
    npx playwright test --project=chromium
    
    $testExitCode = $LASTEXITCODE
    
    Pop-Location
    
    if ($testExitCode -eq 0) {
        Write-Host ""
        Write-Host "=====================================" -ForegroundColor Green
        Write-Host "  ✓ All E2E Tests Passed!" -ForegroundColor Green
        Write-Host "=====================================" -ForegroundColor Green
    }
    else {
        Write-Host ""
        Write-Host "=====================================" -ForegroundColor Red
        Write-Host "  ✗ Some E2E Tests Failed" -ForegroundColor Red
        Write-Host "=====================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "View HTML report: cd frontend && npx playwright show-report" -ForegroundColor Yellow
    }
}
else {
    Write-Host "Step 5: Skipping E2E tests (--SkipTests)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  E2E Environment Ready!" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend:  http://localhost:$BACKEND_PORT" -ForegroundColor White
Write-Host "Frontend: http://localhost:$FRONTEND_PORT" -ForegroundColor White
Write-Host ""
Write-Host "To view test report: cd frontend && npx playwright show-report" -ForegroundColor Yellow
Write-Host "To stop services: Use Task Manager or: Get-Process -Name java,node | Stop-Process" -ForegroundColor Yellow
Write-Host ""

exit $testExitCode
