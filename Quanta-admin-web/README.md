# Quanta 后台管理系统

面向 Quanta 社团的 Vue 3 后台管理前端。项目已经完成主要管理页面、若依登录鉴权、动态菜单、路由守卫、按钮级权限、部门数据范围和本地 Mock；开发环境可以在后端未启动时独立演示。

## 当前已实现

- 登录、验证码兼容、Token 持久化、用户信息和动态路由加载
- 控制台统计卡片
- 成员查询、届次切换、部门/角色筛选、导入、删除、重置密码和留任确认
- 招新看板、一面/二面名单、双志愿状态、简历阅览、面评、Pass/Out、录用通知和名单导出
- 宣讲会报名名单和精英分享会报名名单
- 工位预约记录、图书借阅记录和塔服订单管理
- 学习资料上传、下载、列表和删除
- 403、404 页面及未知后端组件的安全回退

当前页面样式以产品设计稿为准，表格、按钮、状态标签和弹窗已经统一为较紧凑的后台视觉。

## 技术栈

- Vue 3 + Vite + JavaScript
- Element Plus
- Pinia
- Vue Router
- Axios
- Vitest + Vue Test Utils
- ESLint

## 本地启动

```bash
npm install
npm run dev
```

开发环境默认启用本地 Mock，不依赖后端即可运行。

### Mock 账号

所有账号密码均为 `quanta123`。

| 账号 | 业务角色 | 当前能力 |
| --- | --- | --- |
| `ceo` | CEO | 跨部门招新；成员导入、删除、重置密码和留任；全部管理端功能 |
| `product_manager` | 产品部管理层 `qt_mgmt` | 查看成员；管理本部门招新评定和录用；活动、记录、资料和订单管理 |
| `design_manager` | 设计部管理层 `qt_mgmt` | 查看成员；管理本部门招新评定和录用；活动、记录、资料和订单管理 |
| `product_interviewer` | 产品部经理层 `qt_manager` | 查看成员；一面阅览本部门简历；仅在二面编辑本部门面评；查看学习资料和图书借阅 |
| `admin` | 超级管理员 | Mock 中按 CEO 等价角色处理，并拥有 `*:*:*` |
| `viewer` | 只读用户 | 仅查看动态菜单和权限允许的页面，无管理按钮 |

角色规则：

- CEO 和 `admin` 可以执行成员删除、密码重置等高风险操作，并可跨部门处理招新。
- 管理层 `qt_mgmt` 可以查看成员，但不能修改成员；招新操作严格限制在本部门。
- 经理层 `qt_manager` 在一面只能阅览本部门候选人简历，在二面只能编辑本部门面评，不能评定 Pass/Out 或发送录用通知。
- 经理层不显示工位预约和塔服订购；其余菜单由后端 `getRouters` 和权限码共同决定。

## 环境变量

复制 `.env.example` 或修改对应环境文件：

```dotenv
VITE_USE_MOCK=true
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=Quanta 后台管理系统
```

- `.env.development` 当前保持 `VITE_USE_MOCK=true`，API 地址为空。
- 开始联调时设置 `VITE_USE_MOCK=false`，并填写后端地址或配置 Vite 代理。
- `.env.production` 默认关闭 Mock，部署前必须配置真实 API 地址。

## 权限与路由

前端使用若依返回的 `roles`、`permissions` 和动态路由进行三层控制：

1. 路由守卫负责登录状态、动态路由加载和 403/404 跳转。
2. 动态菜单根据 `getRouters` 和路由权限决定页面入口。
3. `PermissionButton`、`v-permission` 和业务角色判断控制按钮显示。

后端仍必须重复校验角色、权限和部门数据范围；隐藏按钮不等于安全授权。超级权限 `*:*:*` 可通过通用权限判断。

## 前端当前调用的接口

以下是源码中已经定义并由页面调用的接口。开发环境启用 Mock 时由本地处理器响应；关闭 Mock 后请求会发送给真实后端。

### 登录与路由

- `POST /login`：登录并取得 Token
- `GET /captchaImage`：获取验证码
- `GET /getInfo`：获取用户、角色、权限和部门信息
- `GET /getRouters`：获取后端动态菜单
- `POST /logout`：退出登录

请求头使用 `Authorization: Bearer <token>`。

### 控制台

- `GET /dashboard/stats`：获取控制台统计数据

### 成员管理

- `GET /qt/member/list`：按届次、关键词、部门和角色查询成员
- `GET /qt/member/cohorts`：获取届次页签
- `PUT /qt/member/{userId}/retain`：确认成员留任并同步下一届
- `POST /system/user/importData`：导入成员名单
- `POST /system/user/importTemplate`：下载导入模板
- `DELETE /system/user/{userIds}`：删除成员，仅 CEO/超级管理员
- `PUT /system/user/resetPwd`：重置成员密码，仅 CEO/超级管理员

相关权限：`qt:member:list`、`qt:member:retain`、`system:user:import`、`system:user:remove`、`system:user:resetPwd`。

### 招新管理

- `GET /qt/interview/admin/statistics`：获取招新统计
- `GET /qt/interview/admin/applications`：获取当前轮次候选人列表
- `GET /qt/interview/admin/applications/{applicationId}`：获取候选人和简历详情
- `GET /qt/interview/admin/evaluations`：查看指定志愿、轮次的面评
- `POST /qt/interview/admin/evaluations`：经理层提交本部门二面面评
- `POST /qt/interview/result`：提交一面 Pass/Out；正式路径仍需后端最终确认
- `POST /qt/interview/admin/offers`：保存二面录用结果和通知内容
- `POST /qt/interview/admin/applications/export`：导出当前筛选结果

相关权限：`qt:interview:admin:list`、`qt:interview:admin:evaluate`、`qt:interview:admin:offer`、`qt:interview:admin:export`。

业务规则：两个志愿分别评审；两者一面都 Pass 时默认第一志愿晋级，第一志愿 Out 且第二志愿 Pass 时第二志愿晋级。管理层只能处理本部门，CEO/超级管理员可跨部门。

### 活动报名

- `GET /qt/activity/lecture/registrations`：获取宣讲会报名名单和名额
- `GET /qt/activity/sharing/registrations`：获取精英分享会报名名单和名额

相关权限：`qt:activity:registrations`。

### 工位、图书与塔服

- `GET /system/reservation/detailList`：获取工位预约记录，只展示记录
- `GET /system/borrow/detailList`：获取图书借阅记录，只展示记录
- `GET /system/order/detailList`：获取塔服订单
- `PUT /system/order/{orderId}/approve`：确认塔服订单收款

相关权限：`system:reservation:list`、`system:borrow:list`、`system:order:list`、`system:order:approve`。

### 学习资料

- `GET /qt/materials`：获取学习资料列表
- `POST /qt/materials`：上传学习资料
- `GET /qt/materials/{materialId}/download`：下载学习资料
- `DELETE /qt/materials/{materialId}`：删除学习资料

相关权限：`qt:material:list`、`qt:material:add`、`qt:material:remove`。经理层只有只读权限。

## 后端联调状态

前端接口结构已按最新《接口文档》和《前端对接说明》进行适配，但真实后端地址尚未提供，因此还没有完成端到端联调。当前仍需后端确认：

1. 重置密码中的 `password` 由谁产生。
2. 一面 Pass/Out 的正式管理端路径。
3. 两个志愿的一面、二面独立状态字段及查询语义。
4. 招新看板统计字段与设计稿指标的映射。
5. 学习资料列表权限是否为 `qt:material:list`。
6. 模板、导出和资料下载的 `Content-Disposition` 及跨域暴露。
7. 数据库补丁和部门角色测试账号是否就绪。
8. 每年 8 月 1 日换届任务的启用、时区、幂等和回滚方案。
9. 联调后端地址，以及使用开发代理还是直接跨域访问。

详细问题、当前前端行为和后端需要回答的内容见 [后端联调剩余确认清单](docs/backend-integration-open-items.md)。这些事项不影响 Mock 演示，但会影响真实接口联调或上线准确性。

## 质量检查

```bash
npm run test:run
npm run lint
npm run build
```

## 项目结构

```text
src/
  api/          页面使用的请求函数和字段映射
  components/   通用组件与权限按钮
  composables/  权限组合逻辑
  directives/   v-permission
  layout/       后台公共布局与菜单
  mock/         本地若依风格 Mock、账号和业务数据
  router/       静态路由、动态路由转换和守卫
  stores/       Pinia 用户与应用状态
  styles/       全局视觉与布局
  views/        登录、控制台和各业务页面
docs/
  backend-integration-open-items.md  后端联调剩余确认项
  superpowers/                      已确认设计规格和实施计划
```
