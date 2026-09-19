#!/usr/bin/env bash
# 阿里云 ECS 一键预置脚本（作为 root 或 sudo 用户执行）：
# 安装 Docker + docker compose plugin + OpenJDK 17；建 ruoyi 用户；启 MySQL/Redis 栈。
# 幂等：已装的会跳过。
set -euo pipefail
DEPLOY_DIR="${DEPLOY_DIR:-/opt/ruoyi}"

echo "==> apt update"
apt-get update

echo "==> install base tools"
DEBIAN_FRONTEND=noninteractive apt-get install -y curl ca-certificates gnupg rsync unzip nginx

# ---- swap：2G 保险垫（4G 已够用，仍保留；swappiness=10），幂等 ----
if ! swapon --show=TYPE | grep -q partition; then
  if [ ! -f /swapfile ]; then
    echo "==> create 2G swapfile"
    fallocate -l 2G /swapfile || { dd if=/dev/zero of=/swapfile bs=1M count=2048; }
    chmod 600 /swapfile
    mkswap /swapfile
  fi
  swapon /swapfile 2>/dev/null || true
  grep -q '^/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi
sysctl -w vm.swappiness=10 >/dev/null
grep -q '^vm.swappiness' /etc/sysctl.d/99-ruoyi.conf 2>/dev/null || \
  echo 'vm.swappiness=10' > /etc/sysctl.d/99-ruoyi.conf
free -h

if ! command -v docker >/dev/null 2>&1; then
  echo "==> install Docker (Ubuntu 自带源；大陆 ECS 访问 download.docker.com 会被重置)"
  DEBIAN_FRONTEND=noninteractive apt-get install -y docker.io docker-compose-v2
  systemctl enable --now docker
fi

# ---- Docker Hub 镜像加速：大陆 ECS 直连 registry-1.docker.io 经常超时（幂等，不覆盖已有配置）
if [ ! -f /etc/docker/daemon.json ]; then
  echo "==> config docker registry-mirrors"
  mkdir -p /etc/docker
  cat > /etc/docker/daemon.json <<'JSON'
{
  "registry-mirrors": ["https://docker.m.daocloud.io", "https://docker.1ms.run"],
  "live-restore": true
}
JSON
  systemctl restart docker
fi

if ! java -version 2>&1 | grep -q 'version "17'; then
  echo "==> install OpenJDK 17"
  DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-17-jre-headless
fi

if ! id -u ruoyi >/dev/null 2>&1; then
  echo "==> create service user ruoyi"
  adduser --system --group --home "$DEPLOY_DIR" --shell /usr/sbin/nologin ruoyi
fi

# logback.xml 里日志路径硬编码为 /home/ruoyi/logs（若依上游默认值），服务用户必须能写
mkdir -p /home/ruoyi/logs
chown -R ruoyi:ruoyi /home/ruoyi

echo "==> deps up (mysql + redis)"
cd "$DEPLOY_DIR/deploy"
# docker compose 会读同目录 .env
[ -f .env ] || { echo "!! 未找到 .env, 先复制 .env.example 并填值再执行"; exit 1; }
docker compose up -d

echo "==> done. 接下来："
echo "  1) 放置 ruoyi-admin.jar 到 $DEPLOY_DIR/ruoyi-admin.jar"
echo "  2) cp deploy/ruoyi.service /etc/systemd/system/ && systemctl daemon-reload && systemctl enable --now ruoyi"
echo "  3) bash $DEPLOY_DIR/deploy/apply-nginx.sh   # 落 nginx 配置；证书就绪后自动切 HTTPS"
