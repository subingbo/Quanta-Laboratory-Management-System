# Quanta 成员管理模块实施计划

对应规格：`docs/superpowers/specs/2026-08-24-quanta-member-management-design.md`  
实施原则：Mock 与真实若依接口共用适配层；先测试数据与状态变化，再实现页面；小步提交并做真实浏览器验收

## 任务 1：建立成员模型、Mock 数据与查询适配

**新增或修改文件**

- `src/api/members.js`
- `src/mock/data/members.js`
- `src/mock/handlers/members.js`
- `src/mock/index.js`
- `src/api/__tests__/members.test.js`
- `src/mock/handlers/__tests__/members.test.js`

**步骤**

1. 定义若依 `SysUser` 到稳定成员模型的转换函数。
2. 建立第 18–21 届、部门、三级角色和多种成员状态的 Mock 数据。
3. 扩展 Mock 路由匹配，使带路径参数的删除与留任接口可被处理。
4. 实现届次元数据和成员列表查询，支持分页、姓名、部门、角色和届次组合筛选。
5. 实现删除、重置密码、导入和留任 Mock；留任需幂等地更新原届并同步下一届。
6. 先写并运行 API 映射、组合筛选和留任事务测试。

## 任务 2：实现成员 API 业务适配层

**新增或修改文件**

- `src/api/members.js`
- `src/api/__tests__/members.test.js`

**步骤**

1. 实现 `getMemberCohorts`、`getMemberList`、`removeMember`、`resetMemberPassword`、`importMembers`、`downloadMemberTemplate` 和 `retainMember`。
2. 将页面查询模型映射为若依参数，将 `TableDataInfo` 映射为稳定列表结果。
3. 上传使用 `FormData`；保留 `updateSupport` 参数。
4. 为现有接口路径和推荐 `/qt/member/*` 契约写断言，避免以后页面散落路径。

## 任务 3：实现查询工具栏与届次页签

**新增文件**

- `src/views/members/components/MemberFilters.vue`
- `src/views/members/components/CohortTabs.vue`
- `src/views/members/__tests__/MemberFilters.test.js`

**步骤**

1. 实现姓名输入、清空和 300ms 防抖事件。
2. 实现部门与三级角色下拉筛选，并提供清除筛选。
3. 实现动态届次胶囊页签和“当前”标识。
4. 保证筛选控件具备可访问名称，支持键盘操作。
5. 测试防抖、筛选事件和届次切换。

## 任务 4：实现成员表格与按钮权限

**新增文件**

- `src/views/members/components/MemberTable.vue`
- `src/views/members/__tests__/MemberTable.test.js`

**步骤**

1. 实现姓名、部门、角色、届数、联系电话、加入时间、状态和操作列。
2. 通过 `StatusTag` 映射在职、已卸任、已留任和待确认状态。
3. 当前届显示删除和重置密码；历史届仅在 `canRetain` 时显示是否留任。
4. 操作按钮接入 `member:remove`、`member:resetPwd`、`member:retain` 权限。
5. 实现加载、空状态、固定操作列与横向滚动。
6. 测试当前/历史届差异、状态显示和只读权限。

## 任务 5：实现名单导入弹窗

**新增文件**

- `src/views/members/components/ImportMembersDialog.vue`
- `src/views/members/__tests__/ImportMembersDialog.test.js`

**步骤**

1. 使用 Element Plus Upload 实现单个 Excel 文件选择和拖拽。
2. 在前端校验 `.xls/.xlsx` 与文件大小，错误文件不进入提交。
3. 提供“覆盖已存在成员”开关，默认关闭。
4. 提交期间锁定弹窗；成功后清空并触发列表刷新。
5. 模板下载保留入口，Mock 明确提示模板待提供。
6. 测试格式、大小、提交、清理和失败反馈。

## 任务 6：组装成员管理页面与业务流程

**新增或修改文件**

- `src/views/members/index.vue`
- `src/views/members/members.css`
- `src/views/members/__tests__/members.test.js`

**步骤**

1. 组装工具栏、页签、表格、分页与导入弹窗。
2. 使用请求序号避免较慢的旧查询覆盖最新筛选结果。
3. 实现删除确认、分页回退与成功/失败提示。
4. 实现重置密码提交锁和成功/失败提示。
5. 实现留任确认；成功后同时刷新届次和列表，并切换/保留合理上下文。
6. 实现加载、查询失败重试、空数据清除条件。
7. 测试关键业务流程与异常分支。

## 任务 7：接入动态路由与权限账号

**修改文件**

- `src/router/component-map.js`
- `src/mock/data/routers.js`
- `src/mock/data/accounts.js`
- `src/router/__tests__/route-transformer.test.js`

**步骤**

1. 把 `/members` 的组件从占位页切换为 `members/index`。
2. 将组件加入安全映射白名单。
3. 保持 Viewer 仅有 `member:list`；Admin 继续使用超级权限。
4. 验证刷新 `/members` 后动态路由能够恢复。

## 任务 8：样式、全量验证与浏览器验收

**修改文件**

- `README.md`
- 必要的样式与测试修复文件

**步骤**

1. 对照设计稿校准卡片、工具栏、胶囊页签、表格、状态标签和弹窗。
2. 在 1366、1440 和 1920 宽度验证布局与表格横向滚动。
3. 运行 `npm run lint`。
4. 运行 `npm run test:run`。
5. 运行 `npm run build`。
6. 使用管理员账号验收筛选、删除取消、重置、导入校验和留任同步。
7. 使用 Viewer 验收只读按钮权限，并刷新动态路由。
8. 检查控制台无未处理异常和关键警告。
9. 更新 README 中的成员模块、Mock 行为和待后端确认接口。

## 完成标准

- 成员管理页面达到设计稿主要视觉和交互要求。
- 无后端时可通过 Mock 连续演示全部成员流程。
- 有后端时页面不变，仅需调整成员 API 适配层。
- 留任跨届同步幂等，年度自动规则明确由后端负责。
- 页面权限、按钮权限、动态路由和刷新恢复均有测试保障。
