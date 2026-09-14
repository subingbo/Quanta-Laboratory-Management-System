#!/usr/bin/env python3
"""
Quanta 部署助手（paramiko SSH/SFTP）——参考 ihavc-dev/docs 里的做法：
Windows OpenSSH 的 scp 不能非交互传密码，所以统一用 paramiko。

子命令：
  sync       把本地文件同步到 ECS（run.sh / *.service / *.conf / docker-compose / .env / initdb / jar）
  provision  在 ECS 上执行 provision.sh (装 Docker/JDK/起 mysql+redis)
  start      在 ECS 上重启 ruoyi systemd 服务
  verify     在 ECS 上 curl 本地 /captchaImage 检查

前置：
  pip install paramiko
  cd deploy/aliyun && cp .env.example .env   # 填 ECS 主机/密码/DB 口令
"""
import argparse
import os
import posixpath
import socket
import sys
import shlex
from pathlib import Path

HERE = Path(__file__).resolve().parent
REPO = HERE.parent.parent      # 项目根

def load_env(path: Path) -> dict:
    env = {}
    if path.exists():
        for line in path.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, _, v = line.partition("=")
            env[k.strip()] = v.strip()
    return env

def must(cond, msg):
    if not cond:
        print(f"!! {msg}", file=sys.stderr)
        sys.exit(2)

try:
    import paramiko
except ImportError:
    print("请先 `pip install paramiko`", file=sys.stderr); sys.exit(2)

class ECS:
    def __init__(self, env):
        must(env.get("ECS_HOST"), "ECS_HOST 未在 .env 中配置")
        host = env["ECS_HOST"]
        port = int(env.get("ECS_PORT", "22"))
        user = env.get("ECS_USER", "root")
        pw   = env.get("ECS_PASSWORD")
        key  = env.get("ECS_KEY_FILE")          # 可选：改用密钥认证
        app_home = env.get("APP_HOME", "/opt/ruoyi")
        cli = paramiko.SSHClient()
        cli.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        sock = socket.socket(); sock.settimeout(5)
        try: sock.connect((host, port))
        except Exception as e:
            print(f"!! 无法连接 {host}:{port} → {e}", file=sys.stderr); sys.exit(3)
        sock.close()
        kw = {"hostname": host, "port": port, "username": user, "timeout": 20,
              "allow_agent": False, "look_for_keys": False}
        if key:
            kw["key_filename"] = str(Path(key).expanduser())
            # passphrase 也可在 .env 里加 ECS_KEY_PASSPHRASE
            if env.get("ECS_KEY_PASSPHRASE"):
                kw["passphrase"] = env["ECS_KEY_PASSPHRASE"]
        else:
            must(pw, "请在 .env 里给 ECS_PASSWORD 或 ECS_KEY_FILE")
            kw["password"] = pw
        cli.connect(**kw)
        self.cli = cli
        self.app_home = app_home
        self.deploy_dir = posixpath.join(app_home, "deploy")
        self.initdb_local = REPO / "cloudrun/quanta-mysql/initdb"
        # 上传完成后不删除的本地文件清单
        self.files = {
            HERE / "docker-compose.yml":  posixpath.join(self.deploy_dir, "docker-compose.yml"),
            HERE / ".env":                 posixpath.join(self.deploy_dir, ".env"),
            HERE / "run.sh":               posixpath.join(app_home,        "run.sh"),
            HERE / "provision.sh":         posixpath.join(self.deploy_dir, "provision.sh"),
            HERE / "ruoyi.service":        posixpath.join(self.deploy_dir, "ruoyi.service"),
        }
        # nginx 配置已拆成多份（common/http/https/redirect/ip），全部同步
        for conf in sorted((HERE / "nginx").glob("*.conf")):
            self.files[conf] = posixpath.join(self.deploy_dir, "nginx", conf.name)
        for sh in ("apply-nginx.sh", "ensure-cert.sh"):
            self.files[HERE / sh] = posixpath.join(self.deploy_dir, sh)
        # 大 jar（若不存在会报错）
        jar = REPO / "ruoyi-admin/target/ruoyi-admin.jar"
        if not jar.exists():
            jar = HERE / "ruoyi-admin.jar"     # 允许手动放
        if jar.exists():
            self.files[jar] = posixpath.join(app_home, "ruoyi-admin.jar")

    def run(self, cmd: str, check: bool = True):
        _, out, err = self.cli.exec_command(cmd, timeout=600)
        o = out.read().decode(errors="replace")
        e = err.read().decode(errors="replace")
        code = out.channel.recv_exit_status()
        if code != 0 and check:
            print(f"$ {cmd}\n[exit {code}]\nSTDERR:\n{e}", file=sys.stderr)
            sys.exit(code)
        if o: print(o.rstrip())
        return code, o, e

    def ensure_dirs(self):
        self.run(f"mkdir -p {shlex.quote(self.deploy_dir + '/nginx')} "
                 f"{shlex.quote(self.deploy_dir + '/initdb')} "
                 f"{shlex.quote(self.app_home + '/logs')} "
                 f"{shlex.quote(self.app_home + '/uploadPath')}")

    def upload(self, lpath: Path, rpath: str):
        sftp = self.cli.open_sftp()
        try:
            print(f"  ↑ {lpath.name}  →  {rpath}")
            # paramiko 没有 progress；大文件给个尺寸提示
            size = lpath.stat().st_size
            print(f"    size: {size/1024/1024:.1f} MiB" if size > 1_000_000 else f"    size: {size} B")
            sftp.put(str(lpath), rpath)
            # 保留可执行位
            if lpath.suffix == ".sh" or rpath.endswith("/ruoyi-admin.jar"):
                sftp.chmod(rpath, 0o755 if lpath.suffix == ".sh" else 0o644)
        finally:
            sftp.close()

    def close(self):
        try: self.cli.close()
        except Exception: pass

def cmd_sync(args):
    env = load_env(HERE / ".env")
    e = ECS(env); e.ensure_dirs()
    print("[1/3] 上传配置文件与 jar")
    for lp, rp in e.files.items():
        must(lp.exists(), f"缺少本地文件: {lp}")
        e.upload(lp, rp)
    print("[2/3] 上传 initdb/*.sql")
    for sql in sorted(e.initdb_local.glob("*.sql")):
        e.upload(sql, f"{e.deploy_dir}/initdb/{sql.name}")
    print("[3/3] 修权")
    # run.sh 与 ruoyi.service 读的是 $APP_HOME/.env（compose 读 deploy/.env），两处都要有
    e.run(f"cp -f {shlex.quote(e.deploy_dir + '/.env')} {shlex.quote(e.app_home + '/.env')}")
    e.run(f"chmod 600 {shlex.quote(e.app_home + '/.env')} {shlex.quote(e.deploy_dir + '/.env')}")
    e.run(f"if id ruoyi >/dev/null 2>&1; then chown -R ruoyi:ruoyi {shlex.quote(e.app_home)}; fi")
    e.run(f"chmod +x {shlex.quote(e.app_home)}/run.sh {shlex.quote(e.deploy_dir)}/provision.sh "
          f"{shlex.quote(e.deploy_dir)}/apply-nginx.sh {shlex.quote(e.deploy_dir)}/ensure-cert.sh")
    e.close()
    print("OK sync done")


def cmd_web(args):
    """上传 admin-web 构建产物 dist/ → $APP_HOME/admin/"""
    env = load_env(HERE / ".env")
    dist = Path(args.dist) if args.dist else REPO / "Quanta-admin-web/dist"
    must(dist.exists(), f"找不到构建产物 {dist}（先在 Quanta-admin-web 执行 npm run build）")
    remote = posixpath.join(env.get("APP_HOME", "/opt/ruoyi"), "admin")
    e = ECS(env)
    e.run(f"mkdir -p {shlex.quote(remote)}")
    sftp = e.cli.open_sftp()
    n = 0
    for lp in sorted(dist.rglob("*")):
        if lp.is_dir():
            continue
        rp = posixpath.join(remote, lp.relative_to(dist).as_posix())
        d = posixpath.dirname(rp)
        try: sftp.stat(d)
        except IOError:
            e.run(f"mkdir -p {shlex.quote(d)}")
        sftp.put(str(lp), rp)
        n += 1
    sftp.close()
    e.close()
    print(f"OK uploaded {n} frontend files -> {remote}")

def cmd_provision(args):
    env = load_env(HERE / ".env")
    e = ECS(env)
    e.run(f"bash {e.deploy_dir}/provision.sh")
    e.close()

def cmd_start(args):
    env = load_env(HERE / ".env")
    e = ECS(env)
    # 顺序无关的兜底：provision 之后 ruoyi 用户才存在，这里再修一次属主/权限
    e.run("id ruoyi >/dev/null 2>&1 && mkdir -p /home/ruoyi/logs && chown -R ruoyi:ruoyi /home/ruoyi || true")
    e.run(f"id ruoyi >/dev/null 2>&1 && chown -R ruoyi:ruoyi {shlex.quote(e.app_home)} "
          f"&& chmod 600 {shlex.quote(e.app_home + '/.env')} {shlex.quote(e.deploy_dir + '/.env')} || true")
    e.run("systemctl daemon-reload || true")
    e.run(f"test -f /etc/systemd/system/ruoyi.service && echo service_ok "
          f"|| cp {e.deploy_dir}/ruoyi.service /etc/systemd/system/ruoyi.service")
    e.run("systemctl enable --now ruoyi; systemctl restart ruoyi")
    e.run("sleep 6; systemctl status ruoyi --no-pager | head -n 20 || true", check=False)
    e.close()

def cmd_verify(args):
    env = load_env(HERE / ".env")
    e = ECS(env)
    code, out, err = e.run("set -o pipefail; curl -sSf --max-time 10 http://127.0.0.1:8080/captchaImage "
                           "-o /tmp/cap.json && head -c 220 /tmp/cap.json", check=False)
    print("--- exit", code); 
    if code == 0: print("OK backend")
    else:
        print("backend 未起来或路径不对。最近日志：")
        e.run("journalctl -u ruoyi -n 60 --no-pager || tail -n 80 " + e.app_home + "/logs/stderr.log",
              check=False)
    e.close()

def cmd_nginx(args):
    """把 deploy/nginx/*.conf 落到 nginx；有证书则自动切 HTTPS"""
    env = load_env(HERE / ".env")
    e = ECS(env)
    e.run(f"bash {e.deploy_dir}/apply-nginx.sh")
    e.close()

def cmd_cert(args):
    """跑一次 ensure-cert.sh（LE 能验证即说明备案拦截已解除，随后自动切 HTTPS）"""
    env = load_env(HERE / ".env")
    e = ECS(env)
    e.run(f"bash {e.deploy_dir}/ensure-cert.sh" + (" --force" if args.force else ""), check=False)
    e.close()

def main():
    p = argparse.ArgumentParser()
    sub = p.add_subparsers(dest="cmd", required=True)
    sub.add_parser("sync").set_defaults(func=cmd_sync)
    wp = sub.add_parser("web", help="上传 admin-web 的 dist 构建产物")
    wp.add_argument("--dist", default=None, help="默认 ../Quanta-admin-web/dist")
    wp.set_defaults(func=cmd_web)
    sub.add_parser("provision").set_defaults(func=cmd_provision)
    sub.add_parser("start").set_defaults(func=cmd_start)
    sub.add_parser("verify").set_defaults(func=cmd_verify)
    sub.add_parser("nginx", help="应用 Nginx 配置（证书就绪时自动切 HTTPS）").set_defaults(func=cmd_nginx)
    cp = sub.add_parser("cert", help="尝试签发证书：LE 验证得通就说明备案生效，随即自动切 HTTPS")
    cp.add_argument("--force", action="store_true", help="忽略 6 小时失败冷却，立即重试")
    cp.set_defaults(func=cmd_cert)
    args = p.parse_args()
    args.func(args)

if __name__ == "__main__":
    main()
