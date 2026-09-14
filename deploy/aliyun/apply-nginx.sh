#!/usr/bin/env bash
# 幂等地把 deploy/nginx/*.conf 落到 nginx 并按「证书是否已签发」决定 HTTP / HTTPS 形态。
# 用法：bash /opt/ruoyi/deploy/apply-nginx.sh
set -euo pipefail
DOMAIN="${DOMAIN:-www.quantacenter.com}"
DEPLOY_DIR="${DEPLOY_DIR:-/opt/ruoyi/deploy}"
AVAIL=/etc/nginx/sites-available
ENABLE=/etc/nginx/sites-enabled
CERT="/etc/letsencrypt/live/${DOMAIN}/fullchain.pem"

echo "==> copy nginx configs"
install -d "$AVAIL"
for f in "$DEPLOY_DIR"/nginx/*.conf; do
  install -m 0644 "$f" "$AVAIL/$(basename "$f")"
done

# 老版单文件配置（quanta.conf）已拆分为 common/http/https/redirect/ip，清掉残留软链
rm -f "$ENABLE/quanta.conf" "$AVAIL/quanta.conf"

link()  { ln -sfn "$AVAIL/$1" "$ENABLE/$1"; echo "  + $1"; }
unlink() { rm -f "$ENABLE/$1" && echo "  - $1" || true; }

echo "==> enabled set"
link quanta-common.conf
link quanta-ip.conf

if [ -f "$CERT" ]; then
  echo "==> 证书已存在：启用 HTTPS + 80 强跳"
  link quanta-https.conf
  link quanta-apex.conf
  link quanta-redirect.conf
  unlink quanta-http.conf
else
  echo "==> 尚无证书：启用 HTTP 站点（等备案生效后 ensure-cert.sh 会自动切 HTTPS）"
  link quanta-http.conf
  unlink quanta-https.conf
  unlink quanta-apex.conf
  unlink quanta-redirect.conf
fi

install -d -o www-data -g www-data /var/www/certbot
nginx -t
systemctl reload nginx
echo "==> done"
