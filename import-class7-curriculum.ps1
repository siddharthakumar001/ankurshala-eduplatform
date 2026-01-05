# Class 7 Curriculum Import - PowerShell Script
# This script automates the import process for Class 7 curriculum data

param(
    [Parameter(Mandatory=$false)]
    [string]$AdminToken,
    
    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = "http://localhost:8080",
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun
)

# Color output functions
function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Cyan
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

function Write-Step {
    param([string]$Message)
    Write-Host "`n🚀 $Message" -ForegroundColor Magenta
}

# Banner
Write-Host "`n╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         Ankurshala Class 7 Curriculum Import Tool        ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# Check if admin token is provided
if ([string]::IsNullOrWhiteSpace($AdminToken)) {
    Write-Warning "Admin token not provided"
    $AdminToken = Read-Host "Please enter your admin JWT token"
}

# Check if Python is installed
Write-Step "Step 1: Checking prerequisites"
try {
    $pythonVersion = python --version 2>&1
    Write-Success "Python found: $pythonVersion"
} catch {
    Write-Error-Custom "Python not found. Please install Python 3.x"
    exit 1
}

# Check if required Python packages are installed
Write-Info "Checking Python packages..."
$packages = @("pandas", "openpyxl")
foreach ($package in $packages) {
    try {
        python -c "import $package" 2>&1 | Out-Null
        Write-Success "$package is installed"
    } catch {
        Write-Warning "$package not found, installing..."
        pip install $package
    }
}

# Generate XLSX file
Write-Step "Step 2: Generating XLSX file from curriculum data"
if (Test-Path "class-7-curriculum.csv") {
    Write-Info "Found class-7-curriculum.csv"
    
    if (Test-Path "create_class7_xlsx.py") {
        Write-Info "Running conversion script..."
        python create_class7_xlsx.py
        
        if (Test-Path "Class_7_Curriculum.xlsx") {
            Write-Success "XLSX file generated successfully"
        } else {
            Write-Error-Custom "Failed to generate XLSX file"
            exit 1
        }
    } else {
        Write-Error-Custom "create_class7_xlsx.py not found"
        exit 1
    }
} else {
    Write-Error-Custom "class-7-curriculum.csv not found"
    exit 1
}

# Upload curriculum file
Write-Step "Step 3: Uploading curriculum to Ankurshala"

$xlsxFile = "Class_7_Curriculum.xlsx"
$uploadUrl = "$BaseUrl/admin/content/import/curriculum"

if ($DryRun) {
    Write-Info "Running in DRY RUN mode (validation only, no database changes)"
    $dryRunParam = "true"
} else {
    Write-Info "Running in LIVE mode (will save to database)"
    $dryRunParam = "false"
}

Write-Info "Upload URL: $uploadUrl"
Write-Info "File: $xlsxFile"

try {
    Write-Info "Sending request..."
    
    $response = curl.exe -X POST "$uploadUrl" `
        -H "Authorization: Bearer $AdminToken" `
        -F "file=@$xlsxFile" `
        -F "dryRun=$dryRunParam" `
        -s
    
    $result = $response | ConvertFrom-Json
    
    if ($result.jobId) {
        Write-Success "Upload initiated successfully!"
        Write-Host "`n📊 Import Job Details:" -ForegroundColor Yellow
        Write-Host "   Job ID:    $($result.jobId)" -ForegroundColor White
        Write-Host "   Status:    $($result.status)" -ForegroundColor White
        Write-Host "   File:      $($result.fileName)" -ForegroundColor White
        Write-Host "   Size:      $($result.fileSize) bytes" -ForegroundColor White
        Write-Host "   Dry Run:   $($result.dryRun)" -ForegroundColor White
        
        $jobId = $result.jobId
        
        # Check job status
        Write-Step "Step 4: Checking import status"
        Start-Sleep -Seconds 2
        
        $maxAttempts = 10
        $attempt = 0
        
        while ($attempt -lt $maxAttempts) {
            $attempt++
            Write-Info "Checking status... (attempt $attempt/$maxAttempts)"
            
            $statusResponse = curl.exe -X GET "$BaseUrl/admin/content/import/jobs/$jobId" `
                -H "Authorization: Bearer $AdminToken" `
                -s
            
            $statusResult = $statusResponse | ConvertFrom-Json
            
            Write-Host "   Status: $($statusResult.status)" -ForegroundColor Cyan
            
            if ($statusResult.status -eq "SUCCEEDED") {
                Write-Success "Import completed successfully!"
                
                if ($statusResult.stats) {
                    Write-Host "`n📈 Import Statistics:" -ForegroundColor Yellow
                    $stats = $statusResult.stats | ConvertFrom-Json
                    foreach ($key in $stats.PSObject.Properties.Name) {
                        Write-Host "   $key : $($stats.$key)" -ForegroundColor White
                    }
                }
                
                break
            } elseif ($statusResult.status -eq "FAILED") {
                Write-Error-Custom "Import failed!"
                if ($statusResult.errorMessage) {
                    Write-Host "   Error: $($statusResult.errorMessage)" -ForegroundColor Red
                }
                exit 1
            } elseif ($statusResult.status -eq "RUNNING") {
                Write-Info "Import still running..."
                Start-Sleep -Seconds 3
            } else {
                Write-Info "Status: $($statusResult.status)"
                Start-Sleep -Seconds 2
            }
        }
        
        if ($attempt -ge $maxAttempts) {
            Write-Warning "Timed out waiting for import to complete"
            Write-Info "Check status manually using: GET $BaseUrl/admin/content/import/jobs/$jobId"
        }
        
    } else {
        Write-Error-Custom "Upload failed"
        Write-Host $response -ForegroundColor Red
        exit 1
    }
    
} catch {
    Write-Error-Custom "Request failed: $($_.Exception.Message)"
    exit 1
}

# Verification steps
Write-Step "Step 5: Verification suggestions"

Write-Info "You can verify the import using these commands:"
Write-Host ""
Write-Host "# Check boards" -ForegroundColor Gray
Write-Host "curl.exe -X GET `"$BaseUrl/api/boards`" -H `"Authorization: Bearer $AdminToken`"" -ForegroundColor White
Write-Host ""
Write-Host "# Check Grade 7 subjects" -ForegroundColor Gray
Write-Host "curl.exe -X GET `"$BaseUrl/api/grades/7/subjects`" -H `"Authorization: Bearer $AdminToken`"" -ForegroundColor White
Write-Host ""
Write-Host "# Check all Grade 7 topics" -ForegroundColor Gray
Write-Host "curl.exe -X GET `"$BaseUrl/api/topics?grade=7`" -H `"Authorization: Bearer $AdminToken`"" -ForegroundColor White

# Summary
Write-Host "`n╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║                    Import Complete! 🎉                    ║" -ForegroundColor Green
Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Green

Write-Host "`n📚 What was imported:" -ForegroundColor Yellow
Write-Host "   • CBSE Board - Class 7 curriculum (4 subjects, 12 chapters)" -ForegroundColor White
Write-Host "   • Bihar Board - Class 7 curriculum (3 subjects, 3 chapters)" -ForegroundColor White
Write-Host "   • ~58 individual topics with cross-grade relationships" -ForegroundColor White
Write-Host "   • Bilingual content (English + Hindi)" -ForegroundColor White

Write-Host "`n🎯 Next Steps:" -ForegroundColor Yellow
Write-Host "   1. Review imported data in admin dashboard" -ForegroundColor White
Write-Host "   2. Configure teacher expertise for Class 7 topics" -ForegroundColor White
Write-Host "   3. Enable student browsing and search" -ForegroundColor White
Write-Host "   4. Test topic-based booking system" -ForegroundColor White

Write-Host "`n✨ For detailed information, see: CLASS_7_IMPORT_GUIDE.md`n" -ForegroundColor Cyan
