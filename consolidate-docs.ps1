# Documentation Consolidation Script
# Consolidates all documentation to maximum 10 files

$ErrorActionPreference = "Stop"
$docsPath = "c:\Users\SiddharthaSingh\Documents\workspace\Ankurshala\ankurshala-eduplatform\docs"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Documentation Consolidation Started" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Count current files
$currentFiles = Get-ChildItem -Path $docsPath -Filter "*.md" | Measure-Object
Write-Host "Current documentation files: $($currentFiles.Count)`n" -ForegroundColor Yellow

# Files to delete (already consolidated)
$filesToDelete = @(
    "BOOKING_SYSTEM_IMPLEMENTATION_SUMMARY.md",
    "E2E_TESTING_GUIDE.md",
    "MONITORING_IMPLEMENTATION.md",
    "PAYMENT_INTEGRATION_IMPLEMENTATION.md",
    "RATE_LIMITING_IMPLEMENTATION.md"
)

Write-Host "Deleting redundant files..." -ForegroundColor Yellow
foreach ($file in $filesToDelete) {
    $filePath = Join-Path -Path $docsPath -ChildPath $file
    if (Test-Path $filePath) {
        Remove-Item -Path $filePath -Force
        Write-Host "  ✓ Deleted: $file" -ForegroundColor Green
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Consolidation Complete!" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Count final files
$finalFiles = Get-ChildItem -Path $docsPath -Filter "*.md"
Write-Host "Final documentation files: $($finalFiles.Count)`n" -ForegroundColor Green

Write-Host "Remaining files:" -ForegroundColor Cyan
$finalFiles | ForEach-Object { Write-Host "  - $($_.Name)" -ForegroundColor White }

Write-Host "" -ForegroundColor Green
Write-Host "Target achieved: $($finalFiles.Count) files (target: 10 or less)" -ForegroundColor Green
Write-Host "" -ForegroundColor Green
