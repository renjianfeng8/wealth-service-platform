# 停止本地开发服务
# 关闭所有 maven/npm 相关进程

Write-Host "正在停止本地服务..." -ForegroundColor Yellow

# 停止 Java 进程
$javaProcs = Get-Process -Name java -ErrorAction SilentlyContinue
if ($javaProcs) {
    $javaProcs | ForEach-Object { Write-Host "  停止 Java PID $($_.Id)" -ForegroundColor Gray }
    $javaProcs | Stop-Process -Force
} else {
    Write-Host "  未运行 Java 进程" -ForegroundColor Gray
}

# 停止 Node 进程（前端）
$nodeProcs = Get-Process -Name node -ErrorAction SilentlyContinue
if ($nodeProcs) {
    $nodeProcs | ForEach-Object { Write-Host "  停止 Node PID $($_.Id)" -ForegroundColor Gray }
    $nodeProcs | Stop-Process -Force -ErrorAction SilentlyContinue
} else {
    Write-Host "  未运行 Node 进程" -ForegroundColor Gray
}

Write-Host "全部服务已停止。" -ForegroundColor Green
