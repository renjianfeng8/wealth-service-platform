# 停止 Docker 编排栈
# 用法: .\deploy\down.ps1                  # 停止全部
#       .\deploy\down.ps1 -v               # 停止并删除数据卷
param(
    [string[]]$ExtraArgs
)

$compose = Join-Path $PSScriptRoot "docker-compose.yml"
$envFile = Join-Path $PSScriptRoot "env\.env"

docker compose `
    --project-directory $PSScriptRoot `
    -f $compose `
    --env-file $envFile `
    down $ExtraArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host "Docker stack stopped." -ForegroundColor Green
} else {
    Write-Host "Docker stack failed to stop (exit $LASTEXITCODE)." -ForegroundColor Red
}
