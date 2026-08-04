#!/bin/bash
# MySQL 恢复脚本
# 用法: ./scripts/db/restore-mysql.sh <backup-file>
# 示例: ./scripts/db/restore-mysql.sh backups/wealth_20260517_020000.sql.gz

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

if [ $# -lt 1 ]; then
    echo "Usage: $0 <backup-file>"
    echo "Example: $0 backups/wealth_20260517_020000.sql.gz"
    exit 1
fi

BACKUP_FILE="$1"
MYSQL_CONTAINER="wealth-mysql"
MYSQL_USER="root"
MYSQL_HOST="localhost"
PASSWORD="${MYSQL_ROOT_PASSWORD:-}"

if [ ! -f "$BACKUP_FILE" ]; then
    echo "ERROR: Backup file not found: $BACKUP_FILE"
    exit 1
fi

if [ -z "$PASSWORD" ]; then
    if [ -f "$ROOT/deploy/env/.env" ]; then
        PASSWORD=$(grep "^MYSQL_ROOT_PASSWORD=" "$ROOT/deploy/env/.env" | cut -d'=' -f2)
    fi
fi

if [ -z "$PASSWORD" ]; then
    echo "ERROR: MYSQL_ROOT_PASSWORD not set."
    exit 1
fi

FILE_SIZE=$(ls -lh "$BACKUP_FILE" | awk '{print $5}')
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Restoring from: $BACKUP_FILE (${FILE_SIZE})"
echo "WARNING: This will overwrite the current 'wealth' database!"
read -rp "Continue? [y/N] " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "Cancelled."
    exit 0
fi

# Decompress and restore
if [[ "$BACKUP_FILE" == *.gz ]]; then
    gunzip -c "$BACKUP_FILE" | docker exec -i "$MYSQL_CONTAINER" \
        mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$PASSWORD"
else
    docker exec -i "$MYSQL_CONTAINER" \
        mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$PASSWORD" < "$BACKUP_FILE"
fi

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Restore complete!"
