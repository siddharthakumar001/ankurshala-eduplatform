# Helper script to get JWT token for API authentication
# Usage: .\get-token.ps1 -BackendUrl "http://localhost:8080" -Email "siddhartha@ankurshala.com" -Password "Maza@123"

param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$Email = "siddhartha@ankurshala.com",
    [string]$Password = "Maza@123"
)

Write-Host "Authenticating with backend..." -ForegroundColor Yellow

$body = @{
    email = $Email
    password = $Password
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BackendUrl/api/auth/signin" -Method Post -Body $body -ContentType "application/json"
    
    $token = $response.data.accessToken
    if ($token) {
        Write-Host ""
        Write-Host "Authentication successful!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Token:" -ForegroundColor Cyan
        Write-Host $token -ForegroundColor White
        Write-Host ""
        Write-Host "Copy this token and use it with the import script:" -ForegroundColor Yellow
        Write-Host "  .\import-curriculum.ps1 -Token `"$token`"" -ForegroundColor Cyan
        
        # Save token to a file for convenience
        $token | Out-File -FilePath "token.txt" -Encoding ASCII -NoNewline
        Write-Host ""
        Write-Host "Token also saved to token.txt" -ForegroundColor Green
    } else {
        Write-Host "Error: No token in response" -ForegroundColor Red
        Write-Host $response | ConvertTo-Json -Depth 10
    }
} catch {
    Write-Host "Error authenticating:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
    }
    exit 1
}

