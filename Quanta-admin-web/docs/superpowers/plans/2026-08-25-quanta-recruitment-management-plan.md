# Quanta 招新管理模块实施计划

对应规格：`docs/superpowers/specs/2026-08-25-quanta-recruitment-management-design.md`  
实施原则：页面只依赖稳定 API 适配层；Mock 复刻后端权限与状态机；先验证数据规则，再组装页面；保持紧凑业务页面密度

## 任务 1：补充 CEO 与部门管理权限基础

**新增或修改文件**

- `src/mock/data/accounts.js`
- `src/stores/user.js`
- `src/composables/usePermission.js`
- `src/components/PermissionButton.vue`
- `src/stores/__tests__/user.test.js`
- `src/composables/__tests__/usePermission.test.js`

**步骤**

1. 增加 CEO、产品部管理层和设计部管理层 Mock 账号；账号同时保存稳定部门编码和显示名称。
2. 保留若依 `/getInfo` 的 `roles`、`permissions` 和 `user.dept` 结构，增加用户部门计算属性。
3. 支持“权限标识 + 角色”组合判断，使 CEO 专属按钮不能被普通管理层看到。
4. 超级管理员跨部门业务策略保持未启用；不把技术管理员自动映射为 CEO。
5. 测试 CEO、部门管理层和只读账号的权限差异。

## 任务 2：建立招新模型、状态机与 Mock 数据

**新增或修改文件**

- `src/mock/data/recruitment.js`
- `src/mock/handlers/recruitment.js`
- `src/mock/index.js`
- `src/mock/handlers/__tests__/recruitment.test.js`

**步骤**

1. 建立候选人、申请、两个志愿轨道、两轮结果、多面试官面评和最终状态数据。
2. 使用 `applicationId + department + roundId` 唯一定位评审轨道。
3. 实现第一志愿优先晋级：双 Pass 仅第一志愿 `advanced = true`；第一志愿 Out、第二志愿 Pass 时晋级第二志愿。
4. 第二志愿未晋级时保留一面 Pass 历史，不自动递补。
5. 实现按登录用户部门过滤列表；CEO 可跨部门，普通管理层只返回本人部门轨道。
6. 对详情、面评和结果提交执行 Mock 后端权限与部门校验，越权返回 `403`。
7. 先测试双志愿组合、晋级结果、部门越权、多面试官评价不互相覆盖和统计去重。

## 任务 3：实现招新 API 适配层

**新增文件**

- `src/api/recruitment.js`
- `src/api/__tests__/recruitment.test.js`

**步骤**

1. 定义页面稳定模型和后端枚举映射，统一中文部门显示。
2. 实现统计、候选人列表、简历详情、面评列表、面评保存、轮次结果、录用通知和导出函数。
3. 真实接口已存在的结果提交适配到 `POST /qt/interview/result`。
4. 缺失接口暂使用规格中建议路径，集中在 API 文件中，避免页面散落 URL。
5. 测试查询参数、部门编码、轮次字段、列表适配和错误透传。

## 任务 4：实现招聘看板、Tab 与候选人表格

**新增文件**

- `src/views/recruitment/components/RecruitmentTabs.vue`
- `src/views/recruitment/components/RecruitmentBoard.vue`
- `src/views/recruitment/components/CandidateTable.vue`
- `src/views/recruitment/__tests__/RecruitmentBoard.test.js`
- `src/views/recruitment/__tests__/CandidateTable.test.js`

**步骤**

1. 实现招聘看板、一面、二面紧凑胶囊 Tab。
2. 实现四项去重统计卡并对齐设计稿视觉。
3. 实现候选人表格：姓名/学号、专业/班级、双志愿、投递时间、状态和操作。
4. 在志愿标签旁展示待评、Pass、Out、Waiting、已晋级或保留结果。
5. 二面表格只展示 `advanced = true` 的唯一晋级轨道。
6. 普通管理层只能操作本人部门志愿；其他志愿只读展示。
7. 测试 Tab、状态映射、晋级过滤、部门操作范围和按钮权限。

## 任务 5：实现简历、面评、评定和录用弹窗

**新增文件**

- `src/views/recruitment/components/ResumeDialog.vue`
- `src/views/recruitment/components/FeedbackDialog.vue`
- `src/views/recruitment/components/DecisionDialog.vue`
- `src/views/recruitment/components/OfferDialog.vue`
- `src/views/recruitment/__tests__/FeedbackDialog.test.js`
- `src/views/recruitment/__tests__/OfferDialog.test.js`

**步骤**

1. 简历弹窗展示申请资料、双志愿、个人简介、编程经历和照片占位。
2. 面评弹窗标题固定显示轮次、候选人和当前部门；普通管理层不能切换部门。
3. 支持编辑本人历史面评，保留其他面试官评价。
4. 提交后显示绿色“已评”，已有面评可再次编辑。
5. Pass、Out、Waiting 写入前显示明确的轨道确认信息。
6. CEO 录用弹窗支持接收人、通知内容、Pass/Out 模板和二次确认。
7. 请求失败保留输入，请求中锁定重复提交。

## 任务 6：组装招新页面并接入动态路由

**新增或修改文件**

- `src/views/recruitment/index.vue`
- `src/views/recruitment/recruitment.css`
- `src/views/recruitment/__tests__/recruitment.test.js`
- `src/router/component-map.js`
- `src/mock/data/routers.js`
- `src/router/__tests__/route-transformer.test.js`

**步骤**

1. 组装 Tab、统计、列表和所有弹窗，管理加载、错误、空状态和刷新。
2. 使用请求序号防止旧请求覆盖最新 Tab 或部门结果。
3. 一面评定成功后重新计算唯一二面晋级轨道并刷新统计。
4. 二面 Pass/Out 后刷新当前轨道和最终状态。
5. 把 `/recruitment` 从占位页切换到 `recruitment/index` 并加入安全组件映射。
6. 验证无 `recruitment:list` 权限时路由守卫拒绝进入，刷新页面后动态路由可恢复。

## 任务 7：把成员三项高风险操作收紧为 CEO 专属

**修改文件**

- `src/views/members/index.vue`
- `src/views/members/components/MemberTable.vue`
- `src/views/members/__tests__/members.test.js`
- `src/views/members/__tests__/MemberTable.test.js`
- `src/mock/handlers/members.js`
- `src/mock/handlers/__tests__/members.test.js`

**步骤**

1. 导入名单入口同时要求 `member:import` 和 `ceo` 角色。
2. 删除按钮同时要求 `member:remove` 和 `ceo` 角色。
3. 重置密码按钮同时要求 `member:resetPwd` 和 `ceo` 角色。
4. Mock 后端对三个接口重复执行 CEO 角色校验，不能只依赖前端隐藏。
5. 测试普通管理层即使伪造请求也收到 `403`，CEO 可正常操作。

## 任务 8：全量验证与真实浏览器验收

**修改文件**

- `README.md`
- 必要的样式和测试修复文件

**步骤**

1. 更新 README 的 Mock 账号、招新规则、权限边界和待后端接口。
2. 运行 `npm run lint`。
3. 运行 `npm run test:run`。
4. 运行 `npm run build`。
5. 使用产品部和设计部管理层账号分别验收部门隔离、双志愿面评和越权隐藏。
6. 使用 CEO 账号验收跨部门查看、录用通知及成员导入/删除/重置密码。
7. 验收双 Pass 仅第一志愿进入二面，第一志愿 Out、第二志愿 Pass 时晋级第二志愿。
8. 在主流桌面宽度检查紧凑字号、表格滚动、弹窗和状态标签。
9. 检查浏览器控制台无未处理异常和关键警告。
10. 输出可点击的本地预览地址和页面截图供用户验收。

## 完成标准

- 招聘看板、一面、二面和全部设计稿弹窗可在无后端环境连续演示。
- 双志愿第一志愿优先规则、部门数据范围和 CEO 权限均有测试保护。
- 页面不直接依赖 Mock 数据；真实后端上线后仅调整 API 适配层。
- 动态路由、路由守卫、按钮权限和 Mock 后端鉴权同时生效。
- 成员管理导入、删除和重置密码只允许 CEO。
- Lint、单元测试、构建和浏览器验收全部通过。
