#!/usr/bin/env bash
# 备份数据库 MySQL ry-vue + /opt/ruoyi/uploadPath，保留最近 7 天
# 用法：
#   backup.sh              # 立即执行一次备份
#   backup.sh --install-cron
# 严禁 docker compose down -v（会删除数据卷，quanta-mysql 里的数据会全部丢失）
set -euo pipefail

APP_HOME="${APP_HOME:-/opt/ruoyi}"
BACKUP_DIR="${BACKUP_DIR:-$APP_HOME/backups}"
KEEP_DAYS="${KEEP_DAYS:-7}"
ENV_FILE="${ENV_FILE:-$APP_HOME/.env}"
LOG_FILE="${LOG_FILE:-$APP_HOME/logs/backup.log}"

strip_cr() { printf '%s' "${1//$'\r'/}"; }

load_env() {
  if [[ ! -f "$ENV_FILE" ]]; then
    echo "!! 找不到配置文件 $ENV_FILE" >&2
    exit 2
  fi
  set -a
  # shellcheck disable=SC1090
  . "$ENV_FILE"
  set +a
  MYSQL_ROOT_PASSWORD="$(strip_cr "${MYSQL_ROOT_PASSWORD:-}")"
  if [[ -z "$MYSQL_ROOT_PASSWORD" ]]; then
    echo "!! MYSQL_ROOT_PASSWORD 未设置" >&2
    exit 2
  fi
}

install_cron() {
  mkdir -p "$(dirname "$LOG_FILE")"
  local line="30 3 * * * /bin/bash ${APP_HOME}/deploy/backup.sh >> ${LOG_FILE} 2>&1"
  local tmp
  tmp="$(mktemp)"
  crontab -l 2>/dev/null | grep -v '/deploy/backup.sh' >"$tmp" || true
  echo "$line" >>"$tmp"
  crontab "$tmp"
  rm -f "$tmp"
  echo "OK cron installed: $line"
}

warn_disk() {
  local used
  used="$(df -P "$BACKUP_DIR" | awk 'NR==2 { gsub(/%/, "", $5); print $5 }')"
  if [[ -n "$used" && "$used" -gt 80 ]]; then
    echo "WARN 磁盘使用率 ${used}%，请及时清理旧备份" >&2
  fi
}

backup_mysql() {
  local stamp="$1"
  local out="$BACKUP_DIR/ry-vue-${stamp}.sql.gz"
  local tmp="${out}.tmp"
  if ! docker inspect -f '{{.State.Running}}' quanta-mysql 2>/dev/null | grep -q true; then
    echo "!! quanta-mysql 容器未在运行，跳过数据库备份" >&2
    return 1
  fi
  docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" quanta-mysql \
    mysqldump -uroot --single-transaction --routines --triggers --databases 'ry-vue' \
    | gzip -c >"$tmp"
  mv -f "$tmp" "$out"
  chmod 600 "$out"
  echo "OK mysql $(du -h "$out" | awk '{print $1}') $out"
}

backup_uploads() {
  local stamp="$1"
  local src="$APP_HOME/uploadPath"
  local out="$BACKUP_DIR/uploadPath-${stamp}.tar.gz"
  local tmp="${out}.tmp"
  if [[ ! -d "$src" ]]; then
    echo "!! 目录 $src 不存在，跳过上传文件备份" >&2
    return 0
  fi
  tar -C "$APP_HOME" -czf "$tmp" uploadPath
  mv -f "$tmp" "$out"
  chmod 600 "$out"
  echo "OK uploads $(du -h "$out" | awk '{print $1}') $out"
}

prune_old() {
  find "$BACKUP_DIR" -maxdepth 1 -type f \
    \( -name 'ry-vue-*.sql.gz' -o -name 'uploadPath-*.tar.gz' \) \
    -mtime "+${KEEP_DAYS}" -print -delete || true
  find "$BACKUP_DIR" -maxdepth 1 -type f -name '*.tmp' -mtime +1 -delete || true
}

main() {
  if [[ "${1:-}" == "--install-cron" ]]; then
    install_cron
    return 0
  fi
  mkdir -p "$BACKUP_DIR" "$(dirname "$LOG_FILE")"
  chown root:root "$BACKUP_DIR" 2>/dev/null || true
  chmod 700 "$BACKUP_DIR"
  load_env
  warn_disk
  local stamp
  stamp="$(date +%Y%m%d)"
  backup_mysql "$stamp"
  backup_uploads "$stamp"
  prune_old
  echo "OK backup ${stamp} keep ${KEEP_DAYS}d"
}

main "$@"
