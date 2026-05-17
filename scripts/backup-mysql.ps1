# MySQL 备份脚本 (Windows PowerShell)
# 用法: .\scripts\backup-mysql.ps1 [-BackupDir <path>]
# 默认备份到 .\backups\

param(
    [string]$BackupDir = (Join-Path (Split-Path $PSScriptRoot -Parent) "backups")
)

$ErrorActionPreference = "Stop"
$Container = "wealth-mysql"
$Database = "wealth"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$BackupFile = Join-Path $BackupDir "wealth_$Timestamp.sql.gz"
$RetentionDays = 7

# Read password from .env
$EnvFile = Join-Path (Split-Path $PSScriptRoot -Parent) ".env"
$Password = $env:MYSQL_ROOT_PASSWORD
if (-not $Password -and (Test-Path $EnvFile)) {
    $match = Select-String "^MYSQL_ROOT_PASSWORD=(.+)" $EnvFile
    if ($match) { $Password = $match.Matches.Groups[1].Value }
}
if (-not $Password) {
    Write-Error "MYSQL_ROOT_PASSWORD not set."
    exit 1
}

# Create backup dir
New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null

Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Starting MySQL backup..." -ForegroundColor Cyan

# Run mysqldump inside MySQL container and pipe to gzip
$result = docker exec $Container mysqldump -h localhost -u root -p"$Password" `
    --single-transaction --routines --triggers --events `
    --databases $Database 2>"$BackupDir\last_error.log"

if ($LASTEXITCODE -eq 0) {
    $result | gzip > $BackupFile
    $size = (Get-Item $BackupFile).Length / 1MB
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Backup complete: $BackupFile ($([math]::Round($size, 2)) MB)" -ForegroundColor Green
    Remove-Item "$BackupDir\last_error.log" -ErrorAction SilentlyContinue

    # Cleanup: delete backups older than RetentionDays
    $oldFiles = Get-ChildItem $BackupDir -Filter "wealth_*.sql.gz" | Where-Object {
        $_.LastWriteTime -lt (Get-Date).AddDays(-$RetentionDays)
    }
    foreach ($f in $oldFiles) {
        Remove-Item $f.FullName -Force
        Write-Host "  Purged: $($f.Name)"
    }
} else {
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Backup FAILED!" -ForegroundColor Red
    Get-Content "$BackupDir\last_error.log"
    exit 1
}
