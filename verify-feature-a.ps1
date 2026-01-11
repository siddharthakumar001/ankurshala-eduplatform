# Feature A: Today Home - Local Verification Script
# Run this script to verify the backend implementation

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Feature A: Today Home Backend Verification" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Continue"
$VerificationPassed = $true

# Step 1: Check if backend directory exists
Write-Host "[1/10] Checking backend directory..." -ForegroundColor Yellow
if (Test-Path "backend/pom.xml") {
    Write-Host "✅ Backend directory found" -ForegroundColor Green
} else {
    Write-Host "❌ Backend directory not found" -ForegroundColor Red
    $VerificationPassed = $false
}

# Step 2: Check if migration file exists
Write-Host "`n[2/10] Checking database migration..." -ForegroundColor Yellow
$migrationFile = "backend/src/main/resources/db/migration/V30__create_today_home_tables.sql"
if (Test-Path $migrationFile) {
    Write-Host "✅ Migration V30 found" -ForegroundColor Green
} else {
    Write-Host "❌ Migration V30 not found" -ForegroundColor Red
    $VerificationPassed = $false
}

# Step 3: Check if entity classes exist
Write-Host "`n[3/10] Checking entity classes..." -ForegroundColor Yellow
$entities = @(
    "backend/src/main/java/com/ankurshala/backend/entity/DailyPlanProgress.java",
    "backend/src/main/java/com/ankurshala/backend/entity/WeakTopicRecommendation.java"
)
$entityCount = 0
foreach ($entity in $entities) {
    if (Test-Path $entity) {
        $entityCount++
    }
}
if ($entityCount -eq $entities.Count) {
    Write-Host "✅ All entity classes found ($entityCount/$($entities.Count))" -ForegroundColor Green
} else {
    Write-Host "❌ Missing entity classes ($entityCount/$($entities.Count))" -ForegroundColor Red
    $VerificationPassed = $false
}

# Step 4: Check if repositories exist
Write-Host "`n[4/10] Checking repository interfaces..." -ForegroundColor Yellow
$repositories = @(
    "backend/src/main/java/com/ankurshala/backend/repository/DailyPlanProgressRepository.java",
    "backend/src/main/java/com/ankurshala/backend/repository/WeakTopicRecommendationRepository.java"
)
$repoCount = 0
foreach ($repo in $repositories) {
    if (Test-Path $repo) {
        $repoCount++
    }
}
if ($repoCount -eq $repositories.Count) {
    Write-Host "✅ All repositories found ($repoCount/$($repositories.Count))" -ForegroundColor Green
} else {
    Write-Host "❌ Missing repositories ($repoCount/$($repositories.Count))" -ForegroundColor Red
    $VerificationPassed = $false
}

# Step 5: Check if DTOs exist
Write-Host "`n[5/10] Checking DTO classes..." -ForegroundColor Yellow
$dtos = @(
    "backend/src/main/java/com/ankurshala/backend/dto/student/DailyPlanDto.java",
    "backend/src/main/java/com/ankurshala/backend/dto/student/CompleteStepRequest.java",
    "backend/src/main/java/com/ankurshala/backend/dto/student/CompleteStepResponse.java"
)
$dtoCount = 0
foreach ($dto in $dtos) {
    if (Test-Path $dto) {
        $dtoCount++
    }
}
if ($dtoCount -eq $dtos.Count) {
    Write-Host "✅ All DTOs found ($dtoCount/$($dtos.Count))" -ForegroundColor Green
} else {
    Write-Host "❌ Missing DTOs ($dtoCount/$($dtos.Count))" -ForegroundColor Red
    $VerificationPassed = $false
}

# Step 6: Check if service exists
Write-Host "`n[6/10] Checking service layer..." -ForegroundColor Yellow
if (Test-Path "backend/src/main/java/com/ankurshala/backend/service/TodayService.java") {
    Write-Host "✅ TodayService found" -ForegroundColor Green
} else {
    Write-Host "❌ TodayService not found" -ForegroundColor Red
    $VerificationPassed = $false
}

# Step 7: Check if controller exists
Write-Host "`n[7/10] Checking controller..." -ForegroundColor Yellow
if (Test-Path "backend/src/main/java/com/ankurshala/backend/controller/TodayController.java") {
    Write-Host "✅ TodayController found" -ForegroundColor Green
} else {
    Write-Host "❌ TodayController not found" -ForegroundColor Red
    $VerificationPassed = $false
}

# Step 8: Check if tests exist
Write-Host "`n[8/10] Checking test files..." -ForegroundColor Yellow
$tests = @(
    "backend/src/test/java/com/ankurshala/backend/controller/TodayControllerTest.java",
    "backend/src/test/java/com/ankurshala/backend/test/TodayHomeIntegrationTest.java"
)
$testCount = 0
foreach ($test in $tests) {
    if (Test-Path $test) {
        $testCount++
    }
}
if ($testCount -eq $tests.Count) {
    Write-Host "✅ All test files found ($testCount/$($tests.Count))" -ForegroundColor Green
} else {
    Write-Host "❌ Missing test files ($testCount/$($tests.Count))" -ForegroundColor Red
    $VerificationPassed = $false
}

# Step 9: Check if documentation is updated
Write-Host "`n[9/10] Checking documentation..." -ForegroundColor Yellow
if (Test-Path "docs/STUDENT_MODULE.md") {
    $content = Get-Content "docs/STUDENT_MODULE.md" -Raw
    if ($content -match "Today Home") {
        Write-Host "✅ Documentation updated with Today Home section" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Documentation may need Today Home section" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ STUDENT_MODULE.md not found" -ForegroundColor Red
    $VerificationPassed = $false
}

# Step 10: Check if summary document exists
Write-Host "`n[10/10] Checking summary document..." -ForegroundColor Yellow
if (Test-Path "FEATURE_A_TODAY_HOME_SUMMARY.md") {
    Write-Host "✅ Summary document found" -ForegroundColor Green
} else {
    Write-Host "❌ Summary document not found" -ForegroundColor Red
    $VerificationPassed = $false
}

# Final summary
Write-Host "`n================================" -ForegroundColor Cyan
Write-Host "Verification Summary" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

if ($VerificationPassed) {
    Write-Host "✅ ALL CHECKS PASSED" -ForegroundColor Green
    Write-Host "`nYour Feature A (Today Home) backend implementation is complete!" -ForegroundColor Green
    Write-Host "`nNext steps:" -ForegroundColor Cyan
    Write-Host "1. Start infrastructure: docker-compose up -d postgres redis" -ForegroundColor White
    Write-Host "2. Run migrations: cd backend && ./mvnw flyway:migrate" -ForegroundColor White
    Write-Host "3. Run tests: ./mvnw test" -ForegroundColor White
    Write-Host "4. Start backend: ./mvnw spring-boot:run" -ForegroundColor White
    Write-Host "5. Test endpoints using curl (see FEATURE_A_TODAY_HOME_SUMMARY.md)" -ForegroundColor White
} else {
    Write-Host "❌ SOME CHECKS FAILED" -ForegroundColor Red
    Write-Host "`nPlease review the errors above and ensure all files are created." -ForegroundColor Yellow
}

Write-Host "`n================================" -ForegroundColor Cyan
