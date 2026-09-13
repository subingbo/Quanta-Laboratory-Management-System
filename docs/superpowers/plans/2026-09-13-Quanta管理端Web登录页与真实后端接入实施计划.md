# Quanta 管理端 Web 登录页与真实后端接入实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 美化 Quanta 管理端 Web 登录页，并让开发环境默认连接真实后端，仅让明确登记的问题接口使用 Mock。

**Architecture:** 统一请求层先判断全量 Mock 开关，再判断显式的“方法 + 路径”Mock 白名单，其余请求全部进入真实后端。认证接口设置硬性保护，禁止进入局部 Mock。登录页继续复用现有 Vue/Element Plus 表单逻辑，只更换品牌资源、开发提示和响应式视觉。

**Tech Stack:** Vue 3、Vite、Pinia、Axios、Element Plus、Vitest

**Spec:** `docs/superpowers/specs/2026-09-13-quanta-admin-web-login-and-backend-integration-design.md`

## 全局约束

- 开发后端地址固定为 `http://127.0.0.1:8080`。
- 登录、用户信息、权限路由和退出接口不得使用局部 Mock。
- 真实接口的 401、403、404、500 和业务错误不得自动回退 Mock。
- Mock 接口必须显式登记，并同步记录在 `docs/Quanta管理端Web接口Mock清单.md`。
- 新增文档使用中文文件名，并在名称中明确标识 Web 端。

---

### Task 1：建立真实优先的请求路由

**Files:**
- Create: `Quanta-admin-web/src/config/mock-api.js`
- Modify: `Quanta-admin-web/src/utils/request.js`
- Modify: `Quanta-admin-web/.env.development`
- Test: `Quanta-admin-web/src/utils/__tests__/request.test.js`

**Interfaces:**
- Produces: `shouldUseMock(config): boolean`，按全量开关和显式白名单选择请求目标。
- Consumes: 现有 `mockRequest(config)` 与 Axios `service.request(config)`。

- [ ] **Step 1: 写失败测试**

覆盖默认真实请求、白名单 Mock、认证路径禁用 Mock、真实错误不回退四种情况。

- [ ] **Step 2: 验证测试失败**

Run: `npm test -- src/utils/__tests__/request.test.js`

- [ ] **Step 3: 实现白名单与路由选择**

`mock-api.js` 导出规范化匹配函数和认证路径集合；`request.js` 每次请求只计算一次数据源，不在 catch 中自动降级。

- [ ] **Step 4: 设置开发环境默认真实后端**

```env
VITE_USE_MOCK=false
VITE_API_BASE_URL=http://127.0.0.1:8080
VITE_APP_TITLE=Quanta 后台管理系统
```

- [ ] **Step 5: 运行测试并提交**

Run: `npm test -- src/utils/__tests__/request.test.js`

### Task 2：创建并维护 Web Mock 接口清单

**Files:**
- Create: `docs/Quanta管理端Web接口Mock清单.md`
- Modify: `Quanta-admin-web/src/config/mock-api.js`

**Interfaces:**
- Produces: 文档清单与代码白名单一一对应。
- Consumes: 实际接口冒烟测试结果。

- [ ] **Step 1: 创建中文清单文件**

清单表格包含模块、方法、路径、问题、Mock 原因、临时行为、复测条件、状态和确认日期，并声明认证接口禁止登记。

- [ ] **Step 2: 对已知开放问题逐项实测**

使用真实管理员 token 验证主要查询接口；只有能稳定复现缺失或阻塞问题的接口才加入白名单。

- [ ] **Step 3: 校验清单与配置一致并提交**

代码中的每一条白名单都有同路径文档记录，文档中的 `Mock` 状态项也都存在于代码配置。

### Task 3：重做登录页品牌视觉

**Files:**
- Create: `Quanta-admin-web/src/assets/quanta-logo.jpg`
- Modify: `Quanta-admin-web/src/views/login/index.vue`
- Modify: `Quanta-admin-web/src/views/login/login.css`
- Test: `Quanta-admin-web/src/views/login/__tests__/login.test.js`

**Interfaces:**
- Consumes: 用户提供的 Quanta Logo、现有登录 store、验证码 API 和 Element Plus 表单。
- Produces: 桌面与移动端登录页、真实账号提示和开发数据源标识。

- [ ] **Step 1: 更新失败测试**

断言正式 Logo、`admin / admin123`、真实后端状态文本存在，旧 Mock 账号提示消失，并保留提交与验证码行为。

- [ ] **Step 2: 验证测试失败**

Run: `npm test -- src/views/login/__tests__/login.test.js`

- [ ] **Step 3: 添加 Logo 并更新模板**

复制原始 Logo 到项目资源目录，通过 ES 模块导入；保留无障碍替代文本，开发状态从环境变量计算。

- [ ] **Step 4: 实现深色科技风样式**

使用深蓝黑品牌面板、橙色光晕、细网格装饰、浅色登录卡片与清晰焦点态；在 `960px` 以下切换为单栏布局。

- [ ] **Step 5: 运行测试并提交**

Run: `npm test -- src/views/login/__tests__/login.test.js`

### Task 4：真实后端冒烟、构建与视觉验证

**Files:**
- Modify: `docs/Quanta管理端Web接口Mock清单.md`

**Interfaces:**
- Consumes: `admin / admin123` 和运行中的 `http://127.0.0.1:8080`。
- Produces: 可登录、可浏览主要页面且已记录问题接口的 Web 管理端。

- [ ] **Step 1: 运行完整质量检查**

Run: `npm test`

Run: `npm run lint`

Run: `npm run build`

- [ ] **Step 2: 启动真实后端模式前端并登录**

Run: `npm run dev`

使用 `admin / admin123`，验证登录、`/getInfo`、`/getRouters` 与首页加载。

- [ ] **Step 3: 逐页冒烟主要业务接口**

访问成员、招新、工位、读书角、活动报名、学习资料和塔服订单页面；把确认异常项加入中文 Web 清单和代码白名单。

- [ ] **Step 4: 检查响应式视觉与控制台**

验证桌面和移动端登录页无溢出，Logo 清晰，网络请求符合清单，控制台没有未处理异常。

- [ ] **Step 5: 最终提交**

仅提交本计划涉及的 Web 前端、测试和中文文档文件，不包含工作区已有的其他改动。
