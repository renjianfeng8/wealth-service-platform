# 本地开发启动脚本
# 自动加载 .env 环境变量，每个服务启动在独立的终端窗口

param(
    [ValidateSet("all", "gateway", "service", "front")]
    [string]$Target = "all"
)

function Load-EnvString($file) {
    $lines = Get-Content $file | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $eq = $line.IndexOf("=")
            if ($eq -gt 0) {
                $key = $line.Substring(0, $eq).Trim()
                $val = $line.Substring($eq + 1).Trim().Trim('"', "'")
                # 如果值包含空格或特殊字符，用引号包裹
                if ($val -match "[\s;]") { $val = "'$val'" }
                "`$env:$key = $val"
            }
        }
    }
    return $lines -join "; "
}

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  理财服务平台 - 本地开发启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($Target -eq "all" -or $Target -eq "gateway") {
    Write-Host "[gateway] 启动 wealth-gateway (端口 8080)..." -ForegroundColor Yellow
    $envStr = Load-EnvString "$root\backend\wealth-gateway\.env"
    $cmd = "cd '$root'; $envStr; mvn spring-boot:run -pl backend/wealth-gateway"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd
}

if ($Target -eq "all" -or $Target -eq "service") {
    Write-Host "[service] 启动 wealth-service (端口 8081)..." -ForegroundColor Yellow
    $envStr = Load-EnvString "$root\backend\wealth-service\.env"
    $cmd = "cd '$root'; $envStr; mvn spring-boot:run -pl backend/wealth-service"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd
}

if ($Target -eq "all" -or $Target -eq "front") {
    Write-Host "[front] 启动前端开发服务器 (端口 3000)..." -ForegroundColor Yellow
    $cmd = "cd '$root\front'; npm run dev"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd
}

if ($Target -eq "all") {
    Write-Host ""
    Write-Host "全部服务已启动，每个服务在独立窗口中运行。" -ForegroundColor Green
    Write-Host "关闭窗口即可停止对应服务。" -ForegroundColor Green
}
