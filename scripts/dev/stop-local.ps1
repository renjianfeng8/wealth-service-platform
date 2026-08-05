# 停止本地开发服务 - 只关闭本项目进程（按端口定位）
# 停掉监听 8080(gateway) / 8081(service) / 3000(front) 的进程，不误杀其他 Java/Node

Write-Host "正在停止本地服务..." -ForegroundColor Yellow

$ports = @(8080, 8081, 3000)
$stopped = $false

foreach ($port in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conns) {
        foreach ($c in $conns) {
            try {
                Stop-Process -Id $c.OwningProcess -Force -ErrorAction Stop
                Write-Host "  已停止 PID $($c.OwningProcess) (端口 $port)" -ForegroundColor Gray
                $stopped = $true
            } catch {
                Write-Host "  停止端口 $port 进程失败: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
    } else {
        Write-Host "  端口 $port 无监听进程" -ForegroundColor Gray
    }
}

if (-not $stopped) {
    Write-Host "  未发现本项目服务在运行" -ForegroundColor Gray
}

Write-Host "全部服务已停止。" -ForegroundColor Green
