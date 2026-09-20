# Web 端阿里云 RUM 与 Trace ID 接入操作手册

## 1. 文档目的

本手册用于 Quanta Web 项目的前端、后端和运维协作，目标是：

1. 在阿里云 ARMS 中创建 Web & H5 用户体验监控应用。
2. 将前端错误、页面性能、接口耗时与后端日志关联起来。
3. 通过 `X-Trace-Id` 快速定位一次具体请求。

本手册不需要、也禁止向前端提供阿里云 AccessKey、服务器密码、数据库密码或其他服务端密钥。

## 2. RUM 是什么

RUM（Real User Monitoring，真实用户监控）会在真实用户访问网站时采集页面性能、JavaScript 错误、资源加载失败、接口耗时、浏览器与网络环境等信息。

它与 Lighthouse 的区别是：Lighthouse 是人工发起的单次实验室测试；RUM 是网站上线后持续收集真实用户数据。

Quanta Web 接入后主要关注：

- Core Web Vitals：LCP、INP、CLS。
- 页面加载、白屏和长任务。
- JavaScript、Promise 和静态资源错误。
- API 成功率、状态码和耗时。
- Vue 单页应用的页面切换。
- 错误发生时对应的应用版本和 Trace ID。

## 3. 在阿里云创建 Web RUM 应用

操作人需要拥有阿里云 ARMS 用户体验监控的管理权限。

1. 登录阿里云控制台并进入 **应用实时监控服务 ARMS**。
2. 在左侧进入 **用户体验监控 → 应用列表**。
3. 在顶部选择网站实际使用的地域。建议与现有后端服务所在地域一致，便于统一管理。
4. 单击 **添加应用**。
5. 应用类型选择 **Web & H5**。
6. 应用名称建议填写 `Quanta-Web-Production`，描述填写 `Quanta 官网、新生端、塔员端和管理端 Web 监控`。
7. 创建后，在接入页面复制完整的 `endpoint`。

阿里云新版 Web SDK 0.1.x 及以后可以从 `endpoint` 中识别应用，`pid` 已不是必填项；如果控制台同时显示 `pid`，请一并保存，便于排查和后台识别。

官方文档：

- [接入 Web & H5 应用](https://help.aliyun.com/zh/arms/user-experience-monitoring/access-web-h5-applications)
- [Web & H5 SDK 配置参考](https://help.aliyun.com/zh/arms/user-experience-monitoring/web-h5-sdk-configuration-reference)
- [Web & H5 应用快速入门](https://help.aliyun.com/zh/arms/user-experience-monitoring/web-h5-app-quickstart)
- [打通前后端链路](https://help.aliyun.com/zh/arms/user-experience-monitoring/use-cases/trace-associated-with-rum-monitoring)

> 注意：ARMS 用户体验监控是计费服务。启用前请在控制台确认免费额度、计费方式、数据保存周期和告警费用。

## 4. 创建完成后需要发给前端的内容

请将以下信息通过团队内部安全渠道发给前端：

| 名称 | 示例/说明 | 是否必需 |
| --- | --- | --- |
| ARMS RUM endpoint | 控制台生成的完整上报地址，不要手工删改查询参数 | 必需 |
| pid | 控制台显示的应用 ID；新版 SDK 通常可不传 | 可选 |
| 环境名称 | 生产环境固定使用 `prod` | 必需 |
| 发布版本 | 推荐使用发布日期加 Git 短提交号，例如 `2026.09.20-a1b2c3d` | 必需 |
| 初始采样率 | 建议先用 `0.1`，即采集约 10% 会话 | 必需 |

前端将使用下列构建环境变量：

```dotenv
VITE_ARMS_RUM_ENDPOINT=控制台复制的完整endpoint
VITE_APP_ENV=prod
VITE_APP_VERSION=当前发布版本
VITE_ARMS_RUM_SAMPLE_RATE=0.1
```

这些配置是浏览器运行时能够看到的公开接入配置，不是服务器密钥。任何以 `AccessKey`、`Secret`、`Password`、`Token` 命名的阿里云或后端凭据都不得写入 `VITE_` 环境变量，也不得提交到 GitHub。

## 5. Trace ID 前后端约定

### 5.1 请求头

前端每次 API 请求都会生成一个新的 32 位十六进制标识，并放入：

```http
X-Trace-Id: 87c4db2f6b574ad6aafe6b715741d408
```

后端必须按 HTTP 请求头大小写不敏感的规则读取 `X-Trace-Id`。

### 5.2 后端处理规则

建议后端在最外层过滤器或拦截器中完成以下流程：

1. 读取请求头 `X-Trace-Id`。
2. 如果请求头为空，则由后端生成一个新的 Trace ID。
3. 将 Trace ID 写入日志上下文，例如 SLF4J MDC 的 `traceId`。
4. 所有日志格式都输出该字段。
5. 在响应头中返回同一个 `X-Trace-Id`。
6. 请求结束后必须在 `finally` 中清理日志上下文，避免线程池复用导致不同请求串号。

Spring Boot / SLF4J MDC 逻辑示例：

```java
String traceId = request.getHeader("X-Trace-Id");
if (traceId == null || !traceId.matches("^[a-fA-F0-9]{32}$")) {
    traceId = UUID.randomUUID().toString().replace("-", "");
}

try {
    MDC.put("traceId", traceId);
    response.setHeader("X-Trace-Id", traceId);
    filterChain.doFilter(request, response);
} finally {
    MDC.remove("traceId");
}
```

日志格式示例：

```text
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [traceId=%X{traceId}] %logger - %msg%n
```

### 5.3 日志查询

用户反馈接口失败时，前端可以提供 Trace ID。后端应能够直接按完整值搜索：

```text
traceId=87c4db2f6b574ad6aafe6b715741d408
```

不要把 Trace ID 当作身份认证信息。它只用于关联日志，不能代替 Token、Session 或权限校验。

## 6. CORS 配置

如果 Web 域名和 API 域名不同，后端或网关需要允许前端发送该请求头，并允许浏览器读取响应头：

```http
Access-Control-Allow-Headers: Authorization, Content-Type, X-Trace-Id
Access-Control-Expose-Headers: X-Trace-Id
```

现有 CORS 白名单不能因为加入 Trace ID 而改成不受限制的 `*`。生产环境仍应只允许 Quanta 的正式域名和明确的测试域名。

## 7. RUM 隐私与安全要求

RUM 上报前必须过滤或禁止采集以下内容：

- `Authorization`、Cookie、Token 和验证码。
- 登录密码、修改密码表单和邮箱验证码。
- 新生姓名、学号、手机号、邮箱等可识别个人身份的信息。
- 新生报名表正文、简历 PDF、证件照和上传文件地址中的临时签名参数。
- 邮件正文、群二维码内容和后台管理备注。
- API 请求体、响应体以及 URL 中的敏感查询参数。

建议仅记录接口的规范化路径，例如 `/qt/interview/admin/applications/:id`，不要记录真实候选人 ID 或带签名的完整下载链接。

## 8. 推荐的生产配置

- `env`：`prod`。
- `spaMode`：Vue Router 使用 history 路由时设置为 `history`；无法确认时可使用 `auto`。
- `version`：每次发布必须变化，建议与 Git 提交或发布流水线版本一致。
- `sessionConfig.sampleRate`：初次上线使用 `0.1`，观察费用和数据量后再调整。
- Source Map：用于还原压缩后的错误堆栈，但不要公开部署 `.map` 文件；应使用受控上传方式交给监控平台。
- 告警：先配置 JavaScript 错误率、API 失败率、LCP 和异常突增告警，避免一次设置过多低价值告警。

## 9. 联调验收清单

### 9.1 Trace ID

- 浏览器开发者工具的 Network 中，每个 API 请求都能看到 `X-Trace-Id`。
- 连续两个请求的 Trace ID 不相同。
- 后端响应头返回相同的 `X-Trace-Id`。
- 后端日志能够使用该 ID 搜索到同一次请求的入口、业务和异常日志。
- 跨域请求没有因为自定义请求头触发 CORS 失败。

### 9.2 RUM

- 正式域名访问后，ARMS 控制台出现 PV、会话和性能数据。
- Vue 页面切换能够产生正确的页面视图数据。
- 人工触发一条无敏感信息的测试异常后，ARMS 能看到错误、页面、版本和浏览器环境。
- API 监控能看到成功率和耗时，但看不到 Token、请求体和响应体。
- 抽查上报数据，确认不存在姓名、学号、手机号、邮箱、简历或证件照信息。
- 监控采样率、数据量和费用符合预期。

## 10. 前后端交付边界

后端/运维需要交付给前端：

1. ARMS RUM 完整 `endpoint`。
2. 应用 `pid`（控制台有则提供）。
3. 确认后的采样率和部署版本规则。
4. 后端已支持 `X-Trace-Id` 日志关联和 CORS 的确认结果。

前端收到以上信息后再启用 ARMS SDK，并将 Trace ID 与慢请求、错误事件进行关联。在参数未齐全前，前端只启用 Trace ID，不加载 ARMS SDK。
