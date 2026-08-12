# Quanta 前端模块

这是 Quanta 移动端前端，和 `ruoyi-ui` 后台管理前端相互独立。

## 开发启动

```bash
cd ruoyi-qt-ui
npm install
npm run dev
```

默认开发端口是 `81`，开发代理会把 `/dev-api` 转发到 `http://localhost:8080`。

如需换后端地址，修改 `.env.development`：

```ini
VUE_APP_BASE_API=/dev-api
VUE_APP_API_TARGET=http://localhost:8080
```

## 生产构建

```bash
npm run build
```

构建产物输出到 `dist/`。

## 已对接接口

- 登录与用户信息：`/login`（支持 `loginType` 新生/塔员校验，返回 `isQuantaMember`）、`/captchaImage`、`/getInfo`
- 新生注册：`/register`（仅新生；需学号/班级；`sys.account.registerUser=true`）
- 个人资料：`/system/user/profile`、`/system/user/profile/updatePwd`
- 活动与报名：`/system/activity/*`、`/system/signup/*`
- 工位预约：`/system/workstation/*`、`/system/reservation/*`
- 图书借阅：`/system/book/*`、`/system/borrow/*`
- 塔服订购：`/system/item/*`、`/system/payment-config/*`、`/system/order/*`
- 招新投递与面试：`/qt/interview/*`（含结果写入 `/qt/interview/result`）
- 实验室名单：`/qt/member/list`
