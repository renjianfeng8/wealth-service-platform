# 启动 Docker 编排栈（mysql / redis / nginx / gateway / wealth-service / frontend / 监控）
# 用法: .\deploy\up.ps1                     # 启动全部
#       .\deploy\up.ps1 mysql redis nginx   # 仅启动指定服务
param(
    [string[]]$Services = @()
)

$compose = Join-Path $PSScriptRoot "docker-compose.yml"
$envFile = Join-Path $PSScriptRoot "env\.env"

docker compose `
    --project-directory $PSScriptRoot `
    -f $compose `
    --env-file $envFile `
    up -d $Services

if ($LASTEXITCODE -eq 0) {
    Write-Host "Docker stack started." -ForegroundColor Green
} else {
    Write-Host "Docker stack failed to start (exit $LASTEXITCODE)." -ForegroundColor Red
}
