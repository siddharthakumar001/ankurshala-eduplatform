# Check status of import job
# Usage: .\check-job-status.ps1 -JobId 123

param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$Token = "",
    [Parameter(Mandatory=$true)]
    [int]$JobId
)

# Try to read token from file if not provided
if ([string]::IsNullOrEmpty($Token) -and (Test-Path "token.txt")) {
    $Token = Get-Content "token.txt" -Raw
    $Token = $Token.Trim()
}

if ([string]::IsNullOrEmpty($Token)) {
    Write-Host "Error: No token provided" -ForegroundColor Red
    Write-Host "Run .\get-token.ps1 first or provide -Token parameter" -ForegroundColor Yellow
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $Token"
}

try {
    $response = Invoke-RestMethod -Uri "$BackendUrl/api/admin/content/import/jobs/$JobId" -Headers $headers
    
    Write-Host "Job Status:" -ForegroundColor Cyan
    Write-Host ""
    $response | ConvertTo-Json -Depth 10 | Write-Host
    
    if ($response.status -eq "SUCCEEDED") {
        Write-Host ""
        Write-Host "Import completed successfully!" -ForegroundColor Green
        if ($response.stats) {
            Write-Host ""
            Write-Host "Statistics:" -ForegroundColor Yellow
            $stats = $response.stats | ConvertFrom-Json
            $stats | Format-List
        }
    } elseif ($response.status -eq "FAILED") {
        Write-Host ""
        Write-Host "Import failed!" -ForegroundColor Red
        if ($response.errorMessage) {
            Write-Host "Error: $($response.errorMessage)" -ForegroundColor Red
        }
    } elseif ($response.status -eq "RUNNING") {
        Write-Host ""
        Write-Host "Import is still running..." -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "Error checking job status:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
    }
    exit 1
}

