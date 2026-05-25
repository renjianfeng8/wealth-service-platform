# 生产事故分析: ERR_CONNECTION_CLOSED 连接间歇性断开

## 1. 事故概要

- **时间**: 持续性问题
- **现象**: 访问 `rjfwealth.cn` 正常操作数分钟后，静态资源和 API 同时报 `net::ERR_CONNECTION_CLOSED`
- **影响范围**: 所有用户
- **根因**: 服务器内存超卖(3.6GB × 14容器) → 系统内存耗尽 → Docker DNS 超时 → Nginx 全部上游断连

## 2. 故障链

```
物理内存 3.6GB < 容器需求 ~4.5GB
  → 内存压力 → 内核重度 swapping
  → Docker DNS (127.0.0.11) 响应超时
  → Nginx 变量 proxy_pass 依赖每个请求 DNS 解析
  → 全部上游 (frontend/front-user/gateway) 不可达
  → 浏览器 → Nginx → "Connection Refused" → ERR_CONNECTION_CLOSED
  → 容器 OOM → ExitOnOutOfMemoryError → 进程自杀 → Docker 重启
  → 释放内存 → 恢复 → 用户刷新可用 → 循环
```

## 3. 直接原因

**P0** — docker-compose 未设置任何 `mem_limit`，容器间无内存隔离，单容器可耗尽全机内存

**P1** — Nginx 变量 `proxy_pass` + `resolver 127.0.0.11` 导致 DNS 成为单点故障

## 4. 已执行修复

- [x] docker-compose.yml: 全容器添加 `mem_limit` + `memswap_limit`
- [x] docker-compose.yml: 关键容器设置 `oom_score_adj` (nginx:-1000, gateway:-800, mysql:-800)
- [x] docker-compose.yml: Nacos JVM 从 256m 降至 128m
- [x] docker-compose.yml: Redis mem_limit 96m
- [x] docker-compose.yml: 前端容器 48m
- [x] docker-compose.yml: Prometheus 保留时间从 30d → 14d, 大小限制 1GB
- [x] docker-compose.yml: 日志从 100m×5 → 50m×3
- [x] docker-compose.yml: MySQL max-connections 从 200 → 100, innodb-buffer-pool-size 256M
- [x] docker-compose.yml: Sentinel Dashboard 空值（禁用连接尝试）
- [x] docker-compose.yml: Seata 禁用 (`seata.enabled=false`)
- [x] nginx.conf: `worker_processes auto`, `worker_connections 2048`
- [x] nginx.conf: `proxy_http_version 1.1` + `proxy_set_header Connection ""` 全部 location
- [x] nginx.conf: `keepalive_timeout 65s`, `keepalive_requests 1000`
- [x] nginx.conf: upstream 块替代变量 `proxy_pass`（连接池 + 复用）
- [x] nginx.conf: `proxy_next_upstream error timeout http_500 http_502 http_503`
- [x] nginx.conf: 静态资源缓存（7 天 + nginx proxy_cache）
- [x] nginx.conf: SSE regex 优先于通用 API regex（路由冲突修复）
- [x] nginx.conf: 通用安全头（X-Content-Type-Options, X-Frame-Options）

## 5. 待确认/未完成

- [ ] Nacos 配置 `wealth-shared.yaml`: `management.tracing.sampling.probability` 从 0.1 → 0（无 Zipkin）
- [ ] 代码层面: `MarketDataSimulationService.selectList(null)` 需加 LIMIT
- [ ] 代码层面: Product 端口从 8084 → 8086（8086 已被其他占用？确认）
- [ ] 服务器 sysctl: 优化内核参数（见下方）

## 6. 验证命令

```bash
# ===== 部署后验证 =====
# 1. 重新部署
docker-compose down
docker-compose up -d

# 2. 确认所有容器运行
docker ps | grep wealth-

# 3. 确认内存限制生效
docker stats --no-stream | grep -E "(wealth-|CONTAINER)"

# 4. 等待 30s 后检查健康状态
for c in nacos mysql nginx gateway system user product trade message search front front-user; do
  echo "=== $c health ==="
  docker inspect wealth-$c --format='{{json .State.Health.Status}}'
done

# 5. 确认 Nginx 配置加载无误
docker exec wealth-nginx nginx -t

# 6. 冒烟测试 - 静态资源
curl -sI https://rjfwealth.cn/ | head -5
curl -sI https://rjfwealth.cn/user-portal/ | head -5

# 7. 冒烟测试 - API
curl -s -X POST https://rjfwealth.cn/api/v1/system/umsAdmin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | head -c 200

# 8. 监控端点
curl -s http://localhost:9090/api/v1/query?query=up | head -c 500
```

## 7. 监控命令（日常巡检）

```bash
# ===== 内存监控 =====
# 容器内存排行
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.CPUPerc}}" | sort -k2 -h

# 系统内存
free -h
cat /proc/meminfo | head -10

# ===== OOM 检查 =====
# 检查 OOM Killer 日志
dmesg | grep -i "killed process" | tail -10
journalctl -k --no-pager | grep -i "oom\|out of memory" | tail -20

# 检查容器重启历史
docker ps --filter "status=exited" --format "{{.Names}} exited {{.Status}}"
docker inspect $(docker ps -aq) --format '{{.Name}} {{.State.Status}} {{.State.FinishedAt}}' | grep -v "running"

# ===== 连接检查 =====
# Nginx 连接状态
docker exec wealth-nginx cat /var/log/nginx/access.log | tail -50

# TIME_WAIT 统计
ss -s
ss -tan | awk '{print $1}' | sort | uniq -c
netstat -tan | grep TIME_WAIT | wc -l

# 端口使用
ss -tlnp | head -20

# ===== 网络 =====
# Docker DNS 检查
docker exec wealth-nginx nslookup gateway 2>&1

# ===== 磁盘 =====
df -h
du -sh /var/lib/docker/overlay2/
```

## 8. 内核参数优化

部署后需在宿主机 `/etc/sysctl.conf` 添加：

```bash
# 内存 - 降低 swappiness，减少非必要 swap
vm.swappiness = 10
vm.vfs_cache_pressure = 200
vm.min_free_kbytes = 65536

# 网络 - TIME_WAIT 快速回收 + 端口范围扩大
net.ipv4.tcp_fin_timeout = 30
net.ipv4.tcp_tw_reuse = 1
net.ipv4.ip_local_port_range = 10240 65535
net.core.somaxconn = 4096
net.ipv4.tcp_max_syn_backlog = 4096
net.ipv4.tcp_keepalive_time = 300
net.ipv4.tcp_keepalive_intvl = 30
net.ipv4.tcp_keepalive_probes = 3

# 文件句柄
fs.file-max = 100000
```

生效: `sysctl -p`

## 9. 长期加固建议

### 优先级 P0（立即需要）
- 升级云服务器内存至 8GB（约 ¥50/月）— **彻底解决根因**
- 或移除 Prometheus + Grafana（约释放 300MB）改为外部托管

### 优先级 P1（本周内）
- Gateway 端口复用从 Netty 默认改为优化值: 设置 `spring.cloud.gateway.httpclient.pool.maxConnections=500`
- Product: `MarketDataSimulationService` 初始加载加 LIMIT 1000，或分页加载
- Product: SSE 广播频率从 2s 改为 5s，减少 GC 压力

### 优先级 P2（下个迭代）
- 添加 Zipkin 容器用于链路追踪（已配置 tracing endpoint）
- 统一 Redis 连接池配置到 Nacos（目前分散在各模块 yml）
- 前端 Docker 镜像添加自定义 nginx.conf（启用 gzip + 缓存）
- 添加容器级监控告警（cAdvisor + 内存阈值告警）
- 实施健康检查的自动恢复：`restart: always` + `start_period` 充足

### P3（长期架构）
- 合并 message + search 为轻量服务（降低容器数）
- 考虑使用 GraalVM Native Image 减少 Java 服务内存占用
- 数据库读写分离（读库副本分担 MySQL 压力）
