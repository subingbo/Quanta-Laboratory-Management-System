#!/usr/bin/env bash
# ensure-cert.sh —— 「新增接入」备案生效后，自动签发 Let's Encrypt 证书并切到 HTTPS。
# 由 cron 每 30 分钟调用；失败后进入 6 小时冷却，避免撞 LE 的 invalid-authorization 限流。
# 想立刻重试：bash /opt/ruoyi/deploy/ensure-cert.sh --force   日志：/opt/ruoyi/logs/ensure-cert.log
#
# 注意：不要用「本机 curl 自己的域名」判断备案是否生效 —— 机器回环不经过阿里云入站拦截，
#       会假阳性（曾因此反复签发失败）。这里直接以 LE 的验证结果为准。
set -uo pipefail
DOMAIN="${DOMAIN:-www.quantacenter.com}"
ALT_DOMAIN="${ALT_DOMAIN:-quantacenter.com}"
WEBROOT=/var/www/certbot
DEPLOY_DIR="${DEPLOY_DIR:-/opt/ruoyi/deploy}"
LOGDIR=/opt/ruoyi/logs
CERT="/etc/letsencrypt/live/${DOMAIN}/fullchain.pem"
FAIL_TS="$LOGDIR/.cert-last-fail"
COOLDOWN=21600   # 6 小时
log() { echo "[$(date '+%F %T')] $*"; }

[ -d "$LOGDIR" ] || mkdir -p "$LOGDIR"

if [ -f "$CERT" ]; then
  log "证书已存在，续期交给 certbot.timer"
  exit 0
fi

if [ "${1:-}" != "--force" ] && [ -f "$FAIL_TS" ]; then
  last=$(cat "$FAIL_TS" 2>/dev/null || echo 0)
  now=$(date +%s)
  if [ $(( now - last )) -lt $COOLDOWN ]; then
    log "上次签发失败，冷却中（$(( (COOLDOWN - (now - last)) / 60 )) 分钟后可重试），本轮跳过"
    exit 0
  fi
fi

install -d -o www-data -g www-data "$WEBROOT/.well-known/acme-challenge"
log "尝试向 LE 申请证书（LE 能验证通过 = 阿里云备案拦截已解除）"
if certbot certonly --webroot -w "$WEBROOT" -d "$DOMAIN" -d "$ALT_DOMAIN" \
     --agree-tos --register-unsafely-without-email --non-interactive --keep-until-expiring \
     2>&1 | sed 's/^/    /'; then
  rm -f "$FAIL_TS"
else
  date +%s > "$FAIL_TS"
fi

if [ -f "$CERT" ]; then
  log "证书就绪 → 切换 Nginx 到 HTTPS"
  bash "$DEPLOY_DIR/apply-nginx.sh"
  log "完成：https://${DOMAIN}/admin/"
else
  log "仍被拦截（备案未生效），进入 ${COOLDOWN}s 冷却"
fi
