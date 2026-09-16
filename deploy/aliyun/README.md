# Quanta 后端 → 阿里云 ECS 迁移文档

> 用于把当前部署在 **腾讯云 CloudBase 云托管** 的 `quanta-system` / `quanta-mysql` / `quanta-redis`，迁到一台 **阿里云 ECS** 上自管。
> 本目录 (`deploy/aliyun/`) 是配套的脚手架。当前**已完成脚手架，未执行迁移**（按你的要求，现在不迁）。真要迁时照本文从上到下走即可。
>
> 参考：本项目迁移前的云托管部署过程见 `docs/云托管部署文档.md`；paramiko 上传/凭据/白名单思路来自 `ihavc-dev/docs/opencode-aliyun-mcp-ecs-deploy.md`。

---

## 0. 本目录都有啥

```
deploy/aliyun/
├── .env.example          # 口令 / ECS 连接信息 模板 → 复制成 .env
├── docker-compose.yml    # 起 MySQL8(自动建 ry-vue) + Redis7(带密码)
├── provision.sh          # ECS 一键装 Docker + JDK17 + 起依赖栈（幂等）
├── run.sh                # 后端启动（注入 DB/Redis/上传路径/SPRING_ARGS）
├── ruoyi.service         # systemd 单元
├── nginx/                # 拆分式配置：common(upstream/map) + http + https + redirect + ip(8081兜底)
├── apply-nginx.sh        # 落 nginx 配置；证书已签发则自动切 HTTPS
├── ensure-cert.sh        # 备案生效后自动签 Let's Encrypt 证书（cron 每 30 分钟）
├── upload.py             # paramiko 助手：sync / start / verify
└── .gitignore            # .env 和 *.jar 不入库
```

> `docker-compose.yml` 直接挂载 `cloudrun/quanta-mysql/initdb/`，与云托管用**同一批** schema/种子/关验证码脚本（顺序 01→07），迁过去库结构完全一致，不用维护两套 SQL。

---

## 1. 准备

1. 阿里云控制台开一台 **ECS**（Ubuntu 22.04 LTS，≥2C4G，系统盘 ≥60G）。
2. **安全组**：放行 `22`(SSH)、`80/443`(Nginx)。**不要**对公网开 `3306/6379/8080`。
3. 大陆节点：域名 **ICP 备案**，否则 80/443 被拦。
4. （可选）按参考文档配 `aliyun-ops` MCP（RAM 用户 + 白名单策略，不授 Delete），用于在 opencode 里直接管这台 ECS。纯运维用 SSH 也能完成，不强依赖。
5. 本机建一个备案好的域名 A 记录 → ECS 公网 IP，例如 `api.your-domain.com`。

## 2. 本地配置脚手架

```bash
cd D:\RuoYi-Vue-Qt\deploy\aliyun
cp .env.example .env      # 填 MySQL/Redis 口令、ECS_HOST/USER/PASSWORD、APP_HOME、TOKEN_SECRET
pip install paramiko      # upload.py 依赖
```
> 生产环境强烈建议把 `.env` 里的默认口令 `ChangeMe_*` 全部换掉；SSH 若改用密钥，`.env` 填 `ECS_KEY_FILE`（可留空 `ECS_PASSWORD`）。

**先确认 jar 能编译**（JDK17 + Maven）：
```bash
cd D:\RuoYi-Vue-Qt && mvn clean package -DskipTests
# 产物 ruoyi-admin/target/ruoyi-admin.jar，upload.py sync 会自动带上
```
> Windows OpenSSH 的 `scp` 无法非交互填密码，所以统一用 `upload.py`（paramiko SFTP）。

## 3. 上传 → 预置 → 启动

按顺序，三条子命令搞定：

```bash
python upload.py sync        # 传 run.sh / unit / nginx / compose / .env / initdb / jar
python upload.py provision   # ECS 装 Docker+JDK17、建 ruoyi 用户、起 MySQL+Redis(MySQL 首启自动导库)
# 等待 MySQL healthcheck ready（约 30~90s，见下 §4 校验），再:
python upload.py start       # systemctl 起 ruoyi
python upload.py verify      # 服务器本机 curl /captchaImage
```

`sync` 默认落盘 `ruoyi-admin.jar` **和** `initdb/*.sql` 到 `$APP_HOME/deploy`；`docker compose up -d` 后，MySQL 容器按数字序执行 initdb：`01_ry_vue → 02_quartz → 03_lab_full → 04_lab_init(补主字段列) → 05_signup_remark → 06_member_role → 07_disable_captcha`。

## 4. 校验（分层：DB → 应用 → 反代 → 前端）

```bash
# 4.1 MySQL / initdb 是否成功
ssh root@<ECS_IP> "docker logs quanta-mysql --tail 40"
ssh root@<ECS_IP> "docker exec -it quanta-mysql mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" -D 'ry-vue' -e 'SHOW TABLES; SELECT config_value FROM sys_config WHERE config_key=\"sys.account.captchaEnabled\"'"

# 4.2 应用（本机 8080）
python upload.py verify
```
预期：
- 返回 `{"msg":"操作成功"...,"code":200,"captchaEnabled":false}` → **MySQL + Redis + Spring Boot 全链路通**。
- 若报 `Access denied for user quanta@127.0.0.1` → `.env` 里 `MYSQL_PASSWORD` 与 compose 首次启动的不一致；改回首次值或重建 mysql 容器。**因为 compose 里 `MYSQL_DATABASE` + `initdb` 只在数据目录为空时首次初始化**，改了账号口令不会同步；重置库：`cd .../deploy && docker compose down -v && docker compose up -d` 再 `upload.py start`。

```bash
# 4.3 Nginx + HTTPS（先手动装证书）
# 服务器 apt install -y certbot python3-certbot-nginx; certbot --nginx -d api.your-domain.com
# 把 server_name 改成你的域名 后再 cp / reload（见 §5.3）
curl -sS https://api.your-domain.com/captchaImage | head
```

## 5. 手动细节（可选）

- **5.1 想直接跑不用 systemd**：`cd /opt/ruoyi/ && set -a && . ./.env && set +a && bash run.sh`
- **5.2 systemd**：`upload.py start` 里已把 `deploy/ruoyi.service` 拷去 `/etc/systemd/system/ruoyi.service` 并 `enable --now`；改配置后 `systemctl daemon-reload && restart ruoyi`。
- **5.3 Nginx**：改好 `nginx/quanta-*.conf` 里的域名后 `python upload.py sync && python upload.py nginx`（内部执行 `apply-nginx.sh`：拷配置、按证书存在与否启用 HTTP 或 HTTPS 形态、`nginx -t` 后 reload）。备案「新增接入」生效后 `python upload.py cert` 或等 cron 自动签证书并切 HTTPS。
- **5.4 前端构建/部署**：
  - `Quanta-admin-web` 是统一 Web 门户，Vite `base` 固定为 `/`，构建产物整体同步到 `/opt/ruoyi/admin/`。
  - `/`、`/freshman/*`、`/member/*`、`/admin/*` 都由同一个 `index.html` 接管；不可再把根路径重定向到 `/admin/`。
  - Web 请求使用同源根路径。Nginx 已在 SPA fallback 前转发认证接口和 `/dashboard/`、`/qt/`、`/system/`、`/common/`、`/profile/`，不使用 `/prod-api` 前缀。
  - 应用配置前先运行 `nginx -t`；本仓库只提供配置，不会自动上线。
- **5.5 招新小程序**：`Quanta-uniapp/.env.production` 里 `VITE_API_BASE_URL` 改成 `https://api.your-domain.com`，重新 `build:mp-weixin`；到微信公众平台 →「开发-开发管理-服务器域名」把该域名加到 request / uploadFile / downloadFile 合法域名（**必须 HTTPS + 已备案**）。这一步和云托管时一样，只是换成你自己的域名。
- **5.6 CloudBase → ECS 迁移数据（如果云托管上已有真实业务数据要搬）**：见 §7（当前场景通常不需要，因为方案 A 里云托管数据无真实业务数据）。

## 6. 安全收尾（上线前必做，文档写死）

1. **恢复验证码**：删 `initdb/07_disable_captcha.sql`（或改成 `config_value='true'`）后重建 db / 执行 UPDATE 再 `docker restart quanta-mysql`。
2. `token.secret` —— **已改为强制外部注入**：`application.yml` 里是 `${TOKEN_SECRET:}`，`run.sh` 用 `${TOKEN_SECRET:?...}` 校验，因此**不设就直接启动失败**（不会再拿仓库里的公开默认值启动）。生成：`openssl rand -base64 48`，写进 `.env`。
   本地 IDE 直跑不设也行：`TokenService` 检测到缺失/过短会临时随机一把并打 WARN（代价是重启即全员掉线，别用于生产）。改这个值会让所有在线 token 失效。
3. Druid 监控台 —— **默认已关**：`statViewServlet.enabled: ${DRUID_STAT_ENABLED:false}`、`allow` 限 `127.0.0.1`、口令走环境变量；`SecurityConfig` 也已移除 `/druid/**` 的 permitAll，Nginx 再加一层 404。真要排查时：`.env` 临时设 `DRUID_STAT_ENABLED=true` + SSH 隧道看 `127.0.0.1:8080/druid`。
   生产 Swagger 仍建议关：`springdoc.swagger-ui.enabled:false`。
4. `.env` 里 MySQL root/Redis 强口令 + 别把 `.env` 上传/入 git（本目录已 ignore）。
5. Nginx 已加 `X-Content-Type-Options nosniff` + `X-Frame-Options SAMEORIGIN`；`/profile/**` 也单独带 nosniff 与 7 天缓存。
6. 备份：`docker exec quanta-mysql mysqldump -uroot -p"$ROOT_PW" --databases ry-vue --single-transaction --routines --triggers > ry-vue.sql`（cron 每日）。
7. SSH：安全组里 22 端口目前是 `0.0.0.0/0` + 密码登录（阿里云控制台已标黄）。建议限源 IP 或改密钥（`.env` 支持 `ECS_KEY_FILE`）。

### 6.1 缓存与上传限制（本轮新增的运维开关）

> 设计与验证的完整说明见 `docs/后端缓存与上传校验加固说明.md`（含「分页列表为何不能直接 @Cacheable」的三个硬伤、失效矩阵、实测数据与踩坑记录）。

| 开关 | 位置 | 说明 |
|---|---|---|
| `qt.cache.enabled` | `application.yml` | 业务查询缓存总开关，改 `false` 即刻全部直查，不必回滚代码 |
| Redis | `docker-compose.yml` | `maxmemory 256mb` + `volatile-lru`；业务缓存键全部带 TTL，改完需 `docker compose up -d redis`（会清登录态） |
| 缓存键 | Redis db1 | 前缀 `cache:`，排查用 `docker exec quanta-redis redis-cli -a "$REDIS_PASSWORD" -n 1 --scan --pattern 'cache:*'` |
| 图片上传 | `FileValidator.SIZE_IMAGE` = 5MB | 证件照/头像/收款码/效果图/支付凭证 |
| 文档上传 | `FileValidator.SIZE_DOCUMENT` = 20MB | 学习资料；同步受 `spring.servlet.multipart.max-file-size` 与 Nginx `client_max_body_size 25m` 约束 |

上传现在除后缀白名单外还会校验**文件头魔数**与**图片像素**（单边 ≤4096、总像素 ≤2000 万），改名绕过会被拒。

---

## 7. 关于"要不要真持久化"的补充（迁移到 ECS 后）

云托管里我们选了 **方案 A**（不挂 CFS、靠 initdb 自动重放等价）。迁到 ECS 后：
- MySQL 数据落在宿主机磁盘（Docker named volume `mysql_data`），**重启/重新部署都不丢**。
- 因此迁 ECS 之后，**不再需要** §8 那种 initdb 重放兜底；`07_disable_captcha.sql` 这种"测试便利脚本"也应一并下线。
- 备份恢复用 mysql 数据目录或 `mysqldump/导入`（见 §6.6）。这才是"数据要保住"的正解；云托管那套 initdb 只在无状态容器场景才划算。

---

## 8. 回滚 / 迁移到别处

- **停服**：`systemctl stop ruoyi; cd deploy && docker compose down`（数据在 volume 里，不会丢，除非带 `-v`）。
- **换到腾讯云数据库 MySQL**：只改 `ruoyi.service` 的 `.env` 里 `MYSQL_HOST/PORT/USER/PASSWORD`（走内网/VPC），重跑 `start` 即可；`quanta-system` 本身不动。
- **换到别的机器**：整台 `docker exec quanta-mysql mysqldump ... > dump.sql` + 拷 `uploadPath/` → 新主机 `docker compose up` 后 `mysql < dump.sql` + 放文件目录 + 改 `.env`。

---

## 9. 快速迁移清单版（TL;DR）

> 全部命令都在 Windows PowerShell、`cd` 到 `deploy/aliyun` 下执行；`python` = `py`；`.env` 先填好。

```bash
cp .env.example .env                # 填 ECS + 口令
mvn -f ../../ clean package -DskipTests   # 有改动才重打
python upload.py sync
python upload.py provision
python upload.py start
python upload.py verify
# Nginx + certbot 见 §4.3/§5.3/§5.4，域名备案见 §1.3
```

---

> ✍️ 本文件为迁移手册；实际迁移执行时按当次 `.env` 与目标主机状态微调。若以后真走这条路，本文 + 脚手架足够覆盖部署全流程（凭据/端口/反代/DB 建表/验证码/安全）且**只维护一份 SQL**（与云托管共用）。
