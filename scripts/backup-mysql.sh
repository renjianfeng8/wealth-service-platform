#!/bin/bash
# MySQL 定时备份脚本
# 用法: ./scripts/backup-mysql.sh [backup-dir]
# 默认备份到 ./backups/

set -euo pipefail

BACKUP_DIR="${1:-$(cd "$(dirname "$0")/.." && pwd)/backups}"
MYSQL_CONTAINER="wealth-mysql"
MYSQL_USER="root"
MYSQL_DB="wealth"
MYSQL_HOST="localhost"
RETENTION_DAYS=7
RETENTION_WEEKLY=4   # 保留 4 个周日全量
RETENTION_MONTHLY=3  # 保留 3 个 1 号全量
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DOW=$(date +%u)       # 1=Mon..7=Sun
DOM=$(date +%d)       # 01..31
WEEK_TAG=$([ "$DOW" = "7" ] && echo "_Sun" || echo "")
MONTH_TAG=$([ "$DOM" = "01" ] && echo "_day01" || echo "")
PASSWORD="${MYSQL_ROOT_PASSWORD:-}"

mkdir -p "$BACKUP_DIR"

if [ -z "$PASSWORD" ]; then
    if [ -f .env ]; then
        PASSWORD=$(grep "^MYSQL_ROOT_PASSWORD=" .env | cut -d'=' -f2)
    fi
fi

if [ -z "$PASSWORD" ]; then
    echo "ERROR: MYSQL_ROOT_PASSWORD not set. Set env var or add to .env file."
    exit 1
fi

BACKUP_FILE="${BACKUP_DIR}/wealth_${TIMESTAMP}${WEEK_TAG}${MONTH_TAG}.sql.gz"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Starting MySQL backup..."

docker exec "$MYSQL_CONTAINER" \
    mysqldump \
    -h "$MYSQL_HOST" \
    -u "$MYSQL_USER" \
    -p"$PASSWORD" \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    --databases "$MYSQL_DB" \
    2>"${BACKUP_DIR}/last_error.log" \
| gzip > "$BACKUP_FILE"

if [ ${PIPESTATUS[0]} -eq 0 ]; then
    FILE_SIZE=$(ls -lh "$BACKUP_FILE" | awk '{print $5}')
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Backup complete: $BACKUP_FILE (${FILE_SIZE})"
    rm -f "${BACKUP_DIR}/last_error.log"
else
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Backup FAILED!"
    cat "${BACKUP_DIR}/last_error.log"
    exit 1
fi

# === 保留策略 ===

# 日备份保留 7 天（排除周/月档）
find "$BACKUP_DIR" -name "wealth_*.sql.gz" -mtime +$RETENTION_DAYS -not -name "*_Sun.sql.gz" -not -name "*_day01.sql.gz" -delete 2>/dev/null || true

# 周备份（周日）保留 4 周
if [ "$DOW" = "7" ]; then
    find "$BACKUP_DIR" -name "wealth_*_Sun.sql.gz" -mtime +$((RETENTION_WEEKLY * 7)) -delete 2>/dev/null || true
fi

# 月备份（1 号）保留 3 个月
if [ "$DOM" = "01" ]; then
    find "$BACKUP_DIR" -name "wealth_*_day01.sql.gz" -mtime +$((RETENTION_MONTHLY * 31)) -delete 2>/dev/null || true
fi

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Retention cleanup complete."
