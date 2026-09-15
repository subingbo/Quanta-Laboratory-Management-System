#!/usr/bin/env bash
# Quanta 后端启动脚本（本机/云主机通用）。
# 读取同目录 .env 或系统环境变量；未设置则用回环默认值。
# 说明：application-druid.yml 里 URL/账号/密码都支持 ${MYSQL_*}；
#       Redis database 索引与 JDBC 细节参数在这里用 SPRING_ARGS 覆盖，
#       与云托管部署保持完全一致的行为。
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
[[ -f "$HERE/.env" ]] && set -a && . "$HERE/.env" && set +a
# Windows 编辑的 .env 会带 CR，否则会出现 druid,prod\r 这种非法 profile
strip_cr() { printf '%s' "${1//$'\r'/}"; }
JAR="$(strip_cr "${JAR:-$HERE/ruoyi-admin.jar}")"
MYSQL_HOST="$(strip_cr "${MYSQL_HOST:-127.0.0.1}")"
MYSQL_PORT="$(strip_cr "${MYSQL_PORT:-3306}")"
MYSQL_USERNAME="$(strip_cr "${MYSQL_USERNAME:-quanta}")"
MYSQL_PASSWORD="$(strip_cr "${MYSQL_PASSWORD:?set MYSQL_PASSWORD in .env}")"
REDIS_HOST="$(strip_cr "${REDIS_HOST:-127.0.0.1}")"
REDIS_PORT="$(strip_cr "${REDIS_PORT:-6379}")"
REDIS_PASSWORD="$(strip_cr "${REDIS_PASSWORD:?set REDIS_PASSWORD in .env}")"
APP_HOME="$(strip_cr "${APP_HOME:-$HERE}")"
UPLOAD_PATH="$(strip_cr "${UPLOAD_PATH:-$APP_HOME/uploadPath}")"
TOKEN_SECRET="$(strip_cr "${TOKEN_SECRET:?set TOKEN_SECRET in .env (generate: openssl rand -base64 48)}")"
SPRING_PROFILES_ACTIVE="$(strip_cr "${SPRING_PROFILES_ACTIVE:-druid,prod}")"
CORS_ALLOWED_ORIGINS="$(strip_cr "${CORS_ALLOWED_ORIGINS:-https://www.quantacenter.com}")"
SPRING_ARGS="$(strip_cr "${SPRING_ARGS:---spring.data.redis.database=1}")"

mkdir -p "$UPLOAD_PATH" "$APP_HOME/logs"

# 若你的 MySQL 未启用 TLS，allowPublicKeyRetrieval 必须开
JDBC_URL="jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/ry-vue?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_general_ci&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8"

export MYSQL_HOST MYSQL_PORT MYSQL_USERNAME MYSQL_PASSWORD REDIS_HOST TOKEN_SECRET
export SPRING_PROFILES_ACTIVE CORS_ALLOWED_ORIGINS

# 2C2G 小机器内存预算：MySQL(容器,含128M缓冲池) ≈ 400M / Redis ≤128M / JVM 堆 512M+元空间 ≈ 750M
# 若以后升回 4G，可把 -Xmx 调到 1024m
exec java \
  -Xms256m -Xmx512m -XX:MaxMetaspaceSize=256m \
  -jar "$JAR" \
  --spring.profiles.active="$SPRING_PROFILES_ACTIVE" \
  --ruoyi.profile="$UPLOAD_PATH" \
  --spring.data.redis.host="$REDIS_HOST" \
  --spring.data.redis.port="$REDIS_PORT" \
  --spring.data.redis.password="$REDIS_PASSWORD" \
  --spring.datasource.druid.master.url="$JDBC_URL" \
  $SPRING_ARGS
