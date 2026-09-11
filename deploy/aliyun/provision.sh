#!/usr/bin/env bash
# 阿里云 ECS 一键预置脚本（作为 root 或 sudo 用户执行）：
# 安装 Docker + docker compose plugin + OpenJDK 17；建 ruoyi 用户；启 MySQL/Redis 栈。
# 幂等：已装的会跳过。
set -euo pipefail
DEPLOY_DIR="${DEPLOY_DIR:-/opt/ruoyi}"

echo "==> apt update"
apt-get update

echo "==> install base tools"
DEBIAN_FRONTEND=noninteractive apt-get install -y curl ca-certificates gnupg rsync unzip

if ! command -v docker >/dev/null 2>&1; then
  echo "==> install Docker (official repo)"
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  chmod a+r /etc/apt/keyrings/docker.asc
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" \
     > /etc/apt/sources.list.d/docker.list
  apt-get update
  DEBIAN_FRONTEND=noninteractive apt-get install -y docker-ce docker-ce-cli containerd.io \
     docker-buildx-plugin docker-compose-plugin
  systemctl enable --now docker
fi

if ! java -version 2>&1 | grep -q 'version "17'; then
  echo "==> install OpenJDK 17"
  DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-17-jre-headless
fi

if ! id -u ruoyi >/dev/null 2>&1; then
  echo "==> create service user ruoyi"
  adduser --system --group --home "$DEPLOY_DIR" --shell /usr/sbin/nologin ruoyi
fi

echo "==> deps up (mysql + redis)"
cd "$DEPLOY_DIR/deploy"
# docker compose 会读同目录 .env
[ -f .env ] || { echo "!! 未找到 .env, 先复制 .env.example 并填值再执行"; exit 1; }
docker compose up -d

echo "==> done. 接下来："
echo "  1) 放置 ruoyi-admin.jar 到 $DEPLOY_DIR/ruoyi-admin.jar"
echo "  2) cp deploy/ruoyi.service /etc/systemd/system/ && systemctl daemon-reload && systemctl enable --now ruoyi"
echo "  3) cp deploy/nginx/quanta.conf 到 /etc/nginx/sites-available/ (server_name 改真实域名) && ln -sf .../quanta .../sites-enabled/quanta && nginx -s reload"
