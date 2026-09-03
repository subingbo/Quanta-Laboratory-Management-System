# Quanta 后台管理系统第一阶段实施计划

对应规格：`docs/superpowers/specs/2026-08-24-quanta-admin-foundation-design.md`  
技术栈：Vue 3、Vite、JavaScript、Element Plus、Pinia、Vue Router、Axios、Vitest  
实施原则：小步提交、关键逻辑测试优先、Mock 与真实若依接口共用调用层

## 任务 1：建立工程与质量工具

**新增或修改文件**

- `.gitignore`
- `.env.development`
- `.env.production`
- `.env.example`
- `package.json`
- `vite.config.js`
- `eslint.config.js`
- `index.html`
- `src/main.js`
- `src/App.vue`
- `src/styles/reset.css`
- `src/styles/variables.css`
- `src/styles/global.css`

**实施步骤**

1. 创建 Vue 3 + Vite 基础配置，不移动或覆盖 OpenAPI 与设计文档。
2. 安装运行依赖：Vue、Vue Router、Pinia、Element Plus、Element Plus Icons、Axios 和 NProgress。
3. 安装开发依赖：Vite Vue 插件、Vitest、Vue Test Utils、jsdom、ESLint 及 Vue ESLint 插件。
4. 配置 `dev`、`build`、`preview`、`lint`、`test` 和 `test:run` 脚本。
5. 默认开发环境使用 `VITE_USE_MOCK=true`；生产示例保留 `VITE_API_BASE_URL`。
6. `.gitignore` 排除 `node_modules`、`dist`、本地 `.env`、覆盖率和 `.superpowers/brainstorm` 临时文件。
7. 运行 `npm run lint`、`npm run test:run` 和 `npm run build`，确认空工程健康。

**验收结果**

- `npm run dev` 可启动。
- 工程无默认 Vite 示例残留。
- 构建、静态检查和测试命令均可执行。

## 任务 2：实现若依请求协议与双模式数据层

**新增文件**

- `src/utils/token.js`
- `src/utils/request.js`
- `src/utils/unauthorized.js`
- `src/mock/index.js`
- `src/mock/handlers/auth.js`
- `src/mock/handlers/dashboard.js`
- `src/mock/data/accounts.js`
- `src/mock/data/routers.js`
- `src/api/auth.js`
- `src/api/dashboard.js`
- `src/utils/__tests__/request.test.js`

**测试优先步骤**

1. 先写测试：Mock 模式调用 `/login` 返回若依形状 `{ code, msg, token }`。
2. 先写测试：错误密码返回业务错误；失效 Token 返回 401 并触发统一未授权处理器。
3. 先写测试：Mock 关闭时，请求配置包含 `Authorization: Bearer <token>`。
4. 实现 Token 读写和清理，键名固定为 `quanta_admin_token`。
5. 实现 Axios 实例、请求头注入、若依响应规范化和错误规范化。
6. 实现可注册的 401 回调，避免请求层直接依赖 Router 或 Pinia。
7. 实现轻量 Mock Handler 路由匹配、200–500ms 延迟和一致的异常格式。
8. 实现 `login`、`getInfo`、`getRouters`、`logout` 和 `getDashboardStats` API 函数。
9. 运行 `npm run test:run -- src/utils/__tests__/request.test.js`。

**验收结果**

- 页面调用 API 时不需要判断 Mock/真实模式。
- 真实模式遵循若依 Bearer Token 和响应结构。
- 401 可由应用层统一清理会话。

## 任务 3：实现 Pinia 用户与应用状态

**新增文件**

- `src/stores/index.js`
- `src/stores/user.js`
- `src/stores/app.js`
- `src/stores/__tests__/user.test.js`

**测试优先步骤**

1. 写用户 Store 测试：登录后保存 Token；获取用户信息后保存 `user`、`roles`、`permissions`。
2. 写恢复测试：localStorage 中有 Token 时可恢复凭证，但不会伪造用户信息。
3. 写清理测试：退出或初始化失败时清空全部用户状态和 Token。
4. 实现 User Store 的 `login`、`fetchUserInfo`、`logout` 和 `reset`。
5. 实现 App Store 的侧栏折叠和全局界面状态。
6. 运行用户 Store 测试。

**验收结果**

- 登录态只由 User Store 管理。
- 刷新时 Token 可恢复，用户和权限由 `/getInfo` 重新确认。

## 任务 4：实现若依动态路由转换与权限 Store

**新增文件**

- `src/router/component-map.js`
- `src/router/route-transformer.js`
- `src/stores/permission.js`
- `src/views/placeholder/index.vue`
- `src/views/error/ComponentError.vue`
- `src/router/__tests__/route-transformer.test.js`
- `src/stores/__tests__/permission.test.js`

**测试优先步骤**

1. 写测试：递归转换 `name/path/component/meta/children` 并保留 `hidden`、`redirect`、`alwaysShow`。
2. 写测试：`Layout`、控制台和占位页通过白名单映射为组件。
3. 写测试：未知组件映射到 ComponentError，不执行任意动态文件路径。
4. 写测试：Permission Store 只初始化一次，并能保存动态路由移除函数。
5. 实现组件映射表和纯函数式路由转换器。
6. 实现 Permission Store 的 `generateRoutes`、`installRoutes` 和 `resetRoutes`。
7. Mock `/getRouters` 返回设计稿全部菜单和自定义 `meta.group` 分组信息。
8. 运行路由转换与 Permission Store 测试。

**验收结果**

- 动态路由树可稳定转换并安全解析组件。
- 未开发菜单统一进入占位页。
- 退出登录后可完整移除动态路由。

## 任务 5：实现路由守卫与错误页面

**新增或修改文件**

- `src/router/index.js`
- `src/router/guard.js`
- `src/router/routes.js`
- `src/router/__tests__/guard.test.js`
- `src/views/error/403.vue`
- `src/views/error/404.vue`
- `src/main.js`

**测试优先步骤**

1. 写守卫测试：公开白名单无需 Token。
2. 写守卫测试：无 Token 访问受保护页跳转登录并保存站内 `redirect`。
3. 写守卫测试：有 Token 但无用户信息时依次获取用户与动态路由。
4. 写守卫测试：动态路由首次注入后重新进入原地址，不落入 404。
5. 写守卫测试：并发导航不会重复初始化；初始化失败会清理会话。
6. 写守卫测试：已登录访问登录页跳转控制台；无权限进入 403。
7. 实现静态路由、全局前置/后置守卫、NProgress 和路由标题。
8. 将请求层 401 处理器连接到 User Store、Permission Store 和 Router。
9. 实现 403、404 页面及返回控制台操作。
10. 运行守卫测试。

**验收结果**

- 登录、刷新恢复、首次动态注入、401、403 和 404 流程均可预测。
- 不存在重定向死循环或重复路由警告。

## 任务 6：实现按钮级权限基础设施

**新增文件**

- `src/composables/usePermission.js`
- `src/directives/permission.js`
- `src/directives/index.js`
- `src/components/PermissionButton.vue`
- `src/composables/__tests__/usePermission.test.js`
- `src/directives/__tests__/permission.test.js`

**测试优先步骤**

1. 写测试：拥有任一或全部权限时返回正确结果。
2. 写测试：若依超级权限 `*:*:*` 始终通过。
3. 写测试：无权限元素从 DOM 中移除。
4. 实现 `hasAnyPermission`、`hasAllPermissions` 和角色辅助判断。
5. 实现 `v-permission` 和 PermissionButton，默认隐藏无权操作。
6. 在占位页放置仅管理员可见的演示按钮，便于两个 Mock 账号验证差异。
7. 运行权限测试。

**验收结果**

- 业务页面可用指令、组合函数或组件三种方式复用同一权限规则。
- Admin 与 Viewer 账号呈现不同按钮。

## 任务 7：实现后台公共布局

**新增文件**

- `src/layout/AppLayout.vue`
- `src/layout/components/AppSidebar.vue`
- `src/layout/components/SidebarMenuItem.vue`
- `src/layout/components/AppHeader.vue`
- `src/layout/components/AppMain.vue`
- `src/layout/components/UserPanel.vue`
- `src/components/PageCard.vue`
- `src/components/StatusTag.vue`
- `src/styles/layout.css`

**实施步骤**

1. 实现深蓝固定侧栏、品牌区、分组菜单和底部用户区。
2. 侧栏按动态路由的 `meta.group` 排列分组，过滤 `hidden` 菜单并显示徽标。
3. 支持多级菜单、当前路由激活态和侧栏折叠。
4. 实现白色顶部栏、动态标题和实时日期时间；组件卸载时清理定时器。
5. 实现浅灰主内容区、统一卡片、圆角、阴影和间距变量。
6. 为 1366px、1440px 和 1920px 宽度设置适配规则；窄窗口允许内容滚动。
7. 运行 Lint 和组件测试，检查无定时器泄漏。

**验收结果**

- 布局与设计稿视觉方向一致。
- 动态菜单与实际路由保持同步。
- 侧栏折叠后页面仍可正常操作。

## 任务 8：实现临时登录页

**新增文件**

- `src/views/login/index.vue`
- `src/views/login/login.css`
- `src/views/login/__tests__/login.test.js`

**测试优先步骤**

1. 写测试：空用户名/密码不能提交。
2. 写测试：提交期间按钮禁用，失败后密码清空但用户名保留。
3. 写测试：登录成功跳转安全的站内 redirect；外部 redirect 被拒绝并进入控制台。
4. 实现深蓝渐变背景、Quanta 品牌卡片、表单和可选验证码区域。
5. 接入 User Store 登录动作和 Element Plus 消息反馈。
6. 在页面底部仅开发环境提示两个 Mock 账号。
7. 运行登录页测试。

**验收结果**

- 两个 Mock 账号均能登录。
- 错误反馈、重复提交保护和回跳符合设计。

## 任务 9：实现控制台与占位页面

**新增或修改文件**

- `src/views/dashboard/index.vue`
- `src/views/dashboard/components/WelcomeBanner.vue`
- `src/views/dashboard/components/StatCard.vue`
- `src/views/dashboard/__tests__/dashboard.test.js`
- `src/views/placeholder/index.vue`

**测试优先步骤**

1. 写测试：四个统计卡正确展示 Mock 数据。
2. 写测试：单个数据缺失时展示 `--`，不阻断其他卡片。
3. 实现欢迎横幅、用户称呼、四张统计卡和设计稿颜色。
4. 控制台请求展示加载骨架，错误时提供重试入口。
5. 占位页展示当前菜单标题、所属分组和“后续批次开发”提示。
6. 运行控制台测试。

**验收结果**

- 控制台达到第一阶段设计稿还原目标。
- 所有未开发菜单均有一致且可返回的页面反馈。

## 任务 10：文档、全量验证与浏览器验收

**新增或修改文件**

- `README.md`
- `.env.example`
- 必要的测试修复文件

**实施步骤**

1. README 记录安装、启动、Mock 账号、环境变量、真实若依后端切换和目录说明。
2. 运行 `npm run lint`。
3. 运行 `npm run test:run`。
4. 运行 `npm run build`。
5. 启动本地服务，浏览器验证 Admin 登录、Viewer 登录、刷新恢复、退出、回跳、403、404、占位菜单和侧栏折叠。
6. 在 1366、1440 和 1920 宽度检查登录页、布局和控制台。
7. 检查浏览器控制台，无未处理异常和关键警告。
8. 对照规格逐项勾选验收标准，修复差异。
9. 提交最终第一阶段代码，并在交付说明中列出已完成项、Mock 账号和后续接口注意事项。

**最终完成标准**

- Mock 模式无需后端即可走通第一阶段全部流程。
- 路由守卫、动态路由、按钮权限和刷新恢复有自动化测试保障。
- 视觉结构与设计稿一致，常用桌面宽度可用。
- 真实后端上线后可以通过环境变量切换到若依 API。
