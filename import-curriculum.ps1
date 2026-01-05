# PowerShell script to import curriculum data from Excel file
# Usage: .\import-curriculum.ps1 -BackendUrl "http://localhost:8080" -Token "YOUR_JWT_TOKEN" -File "CBSE Bihar Board Curriculum Class 7-12.xlsx"

param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$Token = "",
    [string]$File = "CBSE Bihar Board Curriculum Class 7-12.xlsx",
    [switch]$DryRun = $false
)

# Check if file exists
if (-not (Test-Path $File)) {
    Write-Host "Error: File not found: $File" -ForegroundColor Red
    exit 1
}

# Check if backend is running
Write-Host "Checking if backend is running..." -ForegroundColor Yellow
try {
    $healthCheck = Invoke-WebRequest -Uri "$BackendUrl/api/actuator/health" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Host "Backend is running!" -ForegroundColor Green
} catch {
    Write-Host "Error: Backend is not running at $BackendUrl" -ForegroundColor Red
    Write-Host "Please start the backend first." -ForegroundColor Yellow
    exit 1
}

# Try to read token from file if not provided
if ([string]::IsNullOrEmpty($Token) -and (Test-Path "token.txt")) {
    $Token = Get-Content "token.txt" -Raw
    $Token = $Token.Trim()
    Write-Host "Using token from token.txt" -ForegroundColor Yellow
}

# If still no token, provide instructions
if ([string]::IsNullOrEmpty($Token)) {
    Write-Host "No token provided. You need to authenticate first." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To get a token, run:" -ForegroundColor Cyan
    Write-Host "  .\get-token.ps1" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Or use this script with -Token parameter:" -ForegroundColor Cyan
    Write-Host "  .\import-curriculum.ps1 -Token `"YOUR_JWT_TOKEN`"" -ForegroundColor Cyan
    exit 1
}

# Prepare headers
$headers = @{
    "Authorization" = "Bearer $Token"
}

# Get file info
$filePath = Resolve-Path $File
$fileName = Split-Path $File -Leaf
$fileSize = (Get-Item $filePath).Length

Write-Host ""
Write-Host "Uploading file: $fileName" -ForegroundColor Yellow
Write-Host "File size: $([math]::Round($fileSize/1KB, 2)) KB" -ForegroundColor Yellow
Write-Host "Dry Run: $DryRun" -ForegroundColor Yellow
Write-Host ""

try {
    # Try to use curl.exe (actual curl binary, not PowerShell alias)
    $curlExe = Get-Command curl.exe -ErrorAction SilentlyContinue
    
    if ($curlExe) {
        Write-Host "Using curl.exe to upload file..." -ForegroundColor Yellow
        $dryRunParam = if ($DryRun) { "true" } else { "false" }
        
        # Use curl.exe with proper escaping
        $filePathEscaped = $filePath -replace '"', '""'
        $headersEscaped = "Authorization: Bearer $Token"
        
        $responseJson = & curl.exe -X POST "$BackendUrl/api/admin/content/import/curriculum?dryRun=$dryRunParam" `
            -H $headersEscaped `
            -F "file=@$filePathEscaped" `
            -F "dryRun=$dryRunParam" `
            -s -S
        
        $exitCode = $LASTEXITCODE
        if ($exitCode -eq 0) {
            $response = $responseJson | ConvertFrom-Json
        } else {
            $errorMsg = "curl failed with exit code " + $exitCode + ": " + $responseJson
            throw $errorMsg
        }
    } else {
        # Use PowerShell's Invoke-RestMethod with multipart form data
        Write-Host "Using PowerShell to upload file..." -ForegroundColor Yellow
        
        # For PowerShell 5.1, we need to manually construct multipart form data
        # For PowerShell 6+, we can use -Form parameter
        $psVersion = $PSVersionTable.PSVersion.Major
        
        if ($psVersion -ge 6) {
            # PowerShell 6+ supports -Form parameter
            $form = @{
                file = Get-Item $filePath
                dryRun = $DryRun.ToString().ToLower()
            }
            
            $uri = "$BackendUrl/api/admin/content/import/curriculum"
            $response = Invoke-RestMethod -Uri $uri -Method Post -Headers $headers -Form $form
        } else {
            # PowerShell 5.1 - provide curl command as alternative
            Write-Host "PowerShell 5.1 detected. curl.exe not found." -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Please install curl.exe or use this command manually:" -ForegroundColor Cyan
            $dryRunParam = if ($DryRun) { "true" } else { "false" }
            Write-Host "curl.exe -X POST `"$BackendUrl/admin/content/import/curriculum?dryRun=$dryRunParam`" -H `"Authorization: Bearer $Token`" -F `"file=@$filePath`" -F `"dryRun=$dryRunParam`"" -ForegroundColor White
            exit 1
        }
    }
    
    Write-Host ""
    Write-Host "Upload successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10 | Write-Host
    
    $jobId = $response.jobId
    if ($jobId) {
        Write-Host ""
        Write-Host "Job ID: $jobId" -ForegroundColor Green
        Write-Host ""
        Write-Host "To check status, run:" -ForegroundColor Yellow
        Write-Host "  .\check-job-status.ps1 -JobId $jobId" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Or use curl:" -ForegroundColor Yellow
        Write-Host "  curl `"$BackendUrl/admin/content/import/jobs/$jobId`" -H `"Authorization: Bearer $Token`"" -ForegroundColor Cyan
    }
    
} catch {
    Write-Host ""
    Write-Host "Error uploading file:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
        try {
            $errorObj = $_.ErrorDetails.Message | ConvertFrom-Json
            Write-Host ""
            Write-Host "Error details:" -ForegroundColor Yellow
            $errorObj | ConvertTo-Json -Depth 10 | Write-Host
        } catch {
            # Not JSON, just display as is
        }
    }
    if ($_.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    exit 1
}
