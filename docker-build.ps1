# ==============================================
# wealth-service-platform Docker Image Build Script
# Usage: ./docker-build.ps1
# ==============================================

$modules = @(
    @{Name="gateway"; Port=8080; Dockerfile="wealth-gateway/Dockerfile"},
    @{Name="service"; Port=8081; Dockerfile="wealth-service/Dockerfile"}
)

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$results = @()
$allSuccess = $true

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "  Wealth Service Platform - Docker Build" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

# Check Docker
Write-Host ""
Write-Host "[Check] Docker daemon ... " -NoNewline
$dockerOk = docker info 2>&1 | Select-String -Pattern "Server Version"
if (-not $dockerOk) {
    Write-Host "NOT RUNNING" -ForegroundColor Red
    Write-Host "  Please start Docker Desktop first." -ForegroundColor Red
    exit 1
}
Write-Host "OK" -ForegroundColor Green

# Step 1: Maven package
Write-Host ""
Write-Host "[Step 1/2] Maven packaging ..." -ForegroundColor Yellow
Set-Location $rootDir
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "  Maven packaging failed." -ForegroundColor Red
    exit 1
}
Write-Host "  Maven packaging done." -ForegroundColor Green

# Step 2: Docker build
Write-Host ""
Write-Host "[Step 2/2] Building Docker images ..." -ForegroundColor Yellow

foreach ($mod in $modules) {
    $name = $mod.Name
    $port = $mod.Port
    $dockerfile = $mod.Dockerfile
    $imageName = "wealth-$name`:1.0.0"
    $buildDir = (Join-Path $rootDir "wealth-$name")

    Write-Host ""
    Write-Host "  Building wealth-$name (port $port) ..." -ForegroundColor Yellow

    $output = & docker build -t $imageName -f $dockerfile $buildDir 2>&1
    $exitCode = $LASTEXITCODE

    if ($exitCode -eq 0) {
        Write-Host "  >> wealth-$name built OK" -ForegroundColor Green
        $results += @{Module="wealth-$name"; Port=$port; Status="OK"}
    } else {
        Write-Host "  >> wealth-$name BUILD FAILED" -ForegroundColor Red
        Write-Host "  Error output:" -ForegroundColor Red
        $output | ForEach-Object { Write-Host "    $_" }
        $results += @{Module="wealth-$name"; Port=$port; Status="FAILED"}
        $allSuccess = $false
    }
}

# Summary
Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "  Summary" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

$results | Format-Table -Property Module, Port, Status -AutoSize

if ($allSuccess) {
    Write-Host "  All $($results.Count) images built successfully." -ForegroundColor Green
} else {
    $failed = ($results | Where-Object { $_.Status -eq "FAILED" }).Count
    Write-Host "  $failed image(s) failed." -ForegroundColor Red
}

Write-Host ""
Write-Host "List built images:" -ForegroundColor Cyan
Write-Host "  docker images --filter reference='wealth-*'" -ForegroundColor Gray
