#!/usr/bin/env bash
# Quanta 后端启动脚本（本机/云主机通用）。
# 读取同目录 .env 或系统环境变量；未设置则用回环默认值。
# 说明：application-druid.yml 里 URL/账号/密码都支持 ${MYSQL_*}；
#       Redis database 索引与 JDBC 细节参数在这里用 SPRING_ARGS 覆盖，
#       与云托管部署保持完全一致的行为。
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
[[ -f "$HERE/.env" ]] && set -a && . "$HERE/.env" && set +a

JAR="${JAR:-$HERE/ruoyi-admin.jar}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USERNAME="${MYSQL_USERNAME:-quanta}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:?set MYSQL_PASSWORD in .env}"
REDIS_HOST="${REDIS_HOST:-127.0.0.1}"
REDIS_PORT="${REDIS_PORT:-6379}"
REDIS_PASSWORD="${REDIS_PASSWORD:?set REDIS_PASSWORD in .env}"
APP_HOME="${APP_HOME:-$HERE}"
UPLOAD_PATH="${UPLOAD_PATH:-$APP_HOME/uploadPath}"
# JWT 签名密钥。application.yml 写的是 ${TOKEN_SECRET:}，这里导出环境变量即可被读取，
# 不走 --token.secret= 命令行参数，避免密钥出现在 ps 输出里被同机其他进程看到。
TOKEN_SECRET="${TOKEN_SECRET:?set TOKEN_SECRET in .env (generate: openssl rand -base64 48)}"
# 招新小程序若与后端同 db index 会串，保留 db1；如需换再改这里
SPRING_ARGS="${SPRING_ARGS:---spring.data.redis.database=1}"

mkdir -p "$UPLOAD_PATH" "$APP_HOME/logs"

# 若你的 MySQL 未启用 TLS，allowPublicKeyRetrieval 必须开
JDBC_URL="jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/ry-vue?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_general_ci&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8"

export MYSQL_HOST MYSQL_PORT MYSQL_USERNAME MYSQL_PASSWORD REDIS_HOST TOKEN_SECRET

# 2C2G 小机器内存预算：MySQL(容器,含128M缓冲池) ≈ 400M / Redis ≤128M / JVM 堆 512M+元空间 ≈ 750M
# 若以后升回 4G，可把 -Xmx 调到 1024m
exec java \
  -Xms256m -Xmx512m -XX:MaxMetaspaceSize=256m \
  -jar "$JAR" \
  --ruoyi.profile="$UPLOAD_PATH" \
  --spring.data.redis.host="$REDIS_HOST" \
  --spring.data.redis.port="$REDIS_PORT" \
  --spring.data.redis.password="$REDIS_PASSWORD" \
  --spring.datasource.druid.master.url="$JDBC_URL" \
  $SPRING_ARGS
