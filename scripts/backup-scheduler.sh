#!/bin/sh
# MySQL Backup Scheduler - runs inside mysql-backup container
# Connects directly to MySQL via Docker network (host: mysql)

set -e

PASSWORD="${MYSQL_ROOT_PASSWORD:-}"
BACKUP_DIR="/backups"
RETENTION_DAYS=7

if [ -z "$PASSWORD" ]; then
    echo "ERROR: MYSQL_ROOT_PASSWORD environment variable is not set."
    exit 1
fi


backup() {
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    SQL_FILE="${BACKUP_DIR}/wealth_${TIMESTAMP}.sql"
    GZ_FILE="${SQL_FILE}.gz"

    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Backup starting..."

    if mysqldump -h mysql -u root -p"$PASSWORD" \
        --single-transaction --routines --triggers --events \
        --ssl=0 \
        --databases wealth > "$SQL_FILE" 2>/tmp/last_error.log; then

        gzip -f "$SQL_FILE"
        SIZE=$(ls -lh "$GZ_FILE" | awk '{print $5}')
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] Backup complete: ${GZ_FILE} (${SIZE})"

        find "$BACKUP_DIR" -name "wealth_*.sql.gz" -mtime +${RETENTION_DAYS} -exec rm {} \; 2>/dev/null || true
    else
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] Backup FAILED!"
        cat /tmp/last_error.log 2>/dev/null || true
        rm -f "$SQL_FILE"
        return 1
    fi
}

setup_cron() {
    echo "0 2 * * * /scripts/backup-scheduler.sh backup" > /etc/crontabs/root
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Cron scheduled: daily at 02:00"
}

case "${1:-init}" in
    backup)
        backup
        ;;
    init|*)
        backup
        setup_cron
        echo "MySQL backup scheduler ready (daily at 02:00)"
        crond -f -l 2
        ;;
esac
