# 本地开发启动脚本 - 一键启动全套服务（单窗口后台运行）
# 自动停止旧进程 → 后台启动 gateway + service + front → 等待就绪并提示结果
# 查看日志: Get-Content logs\startup\service.log -Tail 50
# 停止服务: .\scripts\dev\stop-local.ps1
# 注意: 本脚本假设项目路径不含空格（cmd 重定向简化处理）

param(
    [ValidateSet("all", "gateway", "service", "front")]
    [string]$Target = "all"
)

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$logDir = Join-Path $root "logs\startup"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

# 将 .env 文件转为 cmd 的 set 命令（值需不含空格/&/|/引号等特殊字符）
function Load-EnvCmd($file) {
    $sets = Get-Content $file | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $eq = $line.IndexOf("=")
            if ($eq -gt 0) {
                $key = $line.Substring(0, $eq).Trim()
                $val = $line.Substring($eq + 1).Trim().Trim('"', "'")
                if (-not [string]::IsNullOrEmpty($val)) {
                    # 必须用引号包裹，否则 set VAR=value && 会因 && 前空格把尾随空格存进变量值
                    "set `"$key=$val`""
                }
            }
        }
    }
    return ($sets -join " && ")
}

function Start-Bg($name, $cmdLine) {
    $logOut = Join-Path $logDir "$name.log"
    $full = "$cmdLine > $logOut 2>&1"
    $p = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $full -WindowStyle Hidden -PassThru
    Write-Host "  [$name] 后台启动中 (PID $($p.Id))" -ForegroundColor Yellow
    Write-Host "    日志: logs\startup\$name.log" -ForegroundColor Gray
}

# 停掉监听指定端口的旧进程（释放端口）
function Stop-OldPort($port) {
    $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    foreach ($c in $conns) {
        try {
            Stop-Process -Id $c.OwningProcess -Force -ErrorAction Stop
            Write-Host "  已停止旧进程 (PID $($c.OwningProcess), 端口 $port)" -ForegroundColor Gray
        } catch {
            Write-Host "  停止端口 $port 进程失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

# 轮询等待指定端口开始监听
function Wait-Port($port, $timeoutSec) {
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    while ((Get-Date) -lt $deadline) {
        $c = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($c) { return $true }
        Start-Sleep -Seconds 2
    }
    return $false
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  理财服务平台 - 一键启动全套服务" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "第 1 步: 停止旧服务 (释放端口)..." -ForegroundColor Cyan
if ($Target -eq "all" -or $Target -eq "gateway") { Stop-OldPort 8080 }
if ($Target -eq "all" -or $Target -eq "service") { Stop-OldPort 8081 }
if ($Target -eq "all" -or $Target -eq "front")   { Stop-OldPort 3000 }
Write-Host ""

Write-Host "第 2 步: 后台启动服务..." -ForegroundColor Cyan
if ($Target -eq "all" -or $Target -eq "gateway") {
    $envCmd = Load-EnvCmd "$root\backend\wealth-gateway\.env"
    $cmdLine = "cd /d $root"
    if ($envCmd) { $cmdLine += " && $envCmd" }
    $cmdLine += " && mvn spring-boot:run -pl backend/wealth-gateway"
    Start-Bg "gateway" $cmdLine
}
if ($Target -eq "all" -or $Target -eq "service") {
    $envCmd = Load-EnvCmd "$root\backend\wealth-service\.env"
    $cmdLine = "cd /d $root"
    if ($envCmd) { $cmdLine += " && $envCmd" }
    $cmdLine += " && mvn spring-boot:run -pl backend/wealth-service"
    Start-Bg "service" $cmdLine
}
if ($Target -eq "all" -or $Target -eq "front") {
    Start-Bg "front" "cd /d $root\front && npm run dev"
}
Write-Host ""

Write-Host "第 3 步: 等待服务就绪..." -ForegroundColor Cyan
if ($Target -eq "all" -or $Target -eq "gateway") {
    $ok = Wait-Port 8080 120
    if ($ok) { Write-Host "  [gateway]  就绪 OK  http://localhost:8080" -ForegroundColor Green }
    else     { Write-Host "  [gateway]  超时未就绪 XX  查看 logs\startup\gateway.log" -ForegroundColor Red }
}
if ($Target -eq "all" -or $Target -eq "service") {
    $ok = Wait-Port 8081 180
    if ($ok) { Write-Host "  [service]  就绪 OK  http://localhost:8081" -ForegroundColor Green }
    else     { Write-Host "  [service]  超时未就绪 XX  查看 logs\startup\service.log" -ForegroundColor Red }
}
if ($Target -eq "all" -or $Target -eq "front") {
    $ok = Wait-Port 3000 60
    if ($ok) { Write-Host "  [front]  就绪 OK  http://localhost:3000" -ForegroundColor Green }
    else     { Write-Host "  [front]  超时未就绪 XX  查看 logs\startup\front.log" -ForegroundColor Red }
}

Write-Host ""
Write-Host "启动完成。访问: http://localhost:3000" -ForegroundColor Green
Write-Host "查看日志: Get-Content logs\startup\service.log -Tail 50" -ForegroundColor Gray
Write-Host "停止服务: .\scripts\dev\stop-local.ps1" -ForegroundColor Gray
Write-Host ""
Read-Host "按回车键关闭本窗口（服务仍在后台运行）"
