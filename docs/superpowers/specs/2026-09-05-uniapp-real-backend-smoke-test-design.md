# Uni-app 真实后端基础联调设计

## 目标

让 `Quanta-uniapp` 的开发构建可配置地访问本地 Spring Boot 后端，并用真实 `/login` 完成基础登录链路验证。本轮不将活动、图书、工位、名片等页面的本地 Mock 全部改造为真实接口。

## 配置设计

- 请求基础地址改为读取 `VITE_API_BASE_URL`。
- 开发环境默认值为 `http://127.0.0.1:8080`，用于微信开发者工具联调。
- 登录 Mock 改由 `VITE_USE_MOCK=true` 显式开启；默认关闭并请求真实 `/login`。
- 保留现有 Mock 实现，便于以后纯前端演示，不做删除。
- 真机联调时可将 `VITE_API_BASE_URL` 换成电脑的局域网 IP；本轮以微信开发者工具为主。

## 数据流

1. 用户在小程序登录页输入种子账号、密码和验证码。
2. `loginApi` 通过统一请求封装访问 `${VITE_API_BASE_URL}/login`。
3. Spring Boot 使用 MySQL 验证用户，并通过 Redis 处理验证码和 Token。
4. 小程序保存后端返回的真实 Token，后续请求携带 `Authorization: Bearer <token>`。

## 错误处理

- 保留现有 HTTP 状态码、业务 `code` 和 401/403 处理逻辑。
- 后端不可达时显示网络异常，不自动回退到 Mock，避免假登录掩盖联调故障。
- 不在源码中写入账号、密码或 Token。

## 验证标准

- MySQL 和 Redis 容器保持 `healthy`。
- Spring Boot `GET /captchaImage` 返回业务码 200。
- `Quanta-uniapp` 单元测试通过。
- `npm run build:mp-weixin` 成功生成微信小程序产物。
- 微信开发者工具可导入构建产物，登录请求指向 `127.0.0.1:8080`，而非 Apifox Mock。

