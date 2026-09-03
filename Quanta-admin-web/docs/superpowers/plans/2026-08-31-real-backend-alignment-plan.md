# Quanta 管理端真实后端适配 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在保持开发环境 Mock 开启的同时，使前端角色、权限码、请求参数、响应映射和文件下载行为符合真实若依后端合同。

**Architecture:** 后端字段只在 `src/api` 和下载工具边界出现，页面继续消费稳定的视图模型。前端、Mock、测试统一使用真实后端角色和权限码；文件流经独立下载工具处理，普通 JSON 请求继续走现有 `request()`。

**Tech Stack:** Vue 3、Pinia、Vue Router、Axios、Element Plus、Vitest、Vue Test Utils、Vite

**Spec:** `docs/superpowers/specs/2026-08-31-real-backend-alignment-design.md`

**Execution status:** Completed on 2026-08-31. Verification: 38 test files / 87 tests passed, ESLint passed, production build passed.

## Global Constraints

- 保持 `.env.development` 中 `VITE_USE_MOCK=true`。
- 不改变现有页面视觉设计和交互文案。
- 不给重置密码接口添加未经确认的默认密码。
- 页面和 Mock 统一使用后端角色 `ceo`、`admin`、`qt_mgmt`、`qt_manager`。
- 页面和 Mock 统一使用后端权限码，不保留长期旧权限别名。
- 所有真实后端响应先在 API 层转换，再传给页面。
- 每项任务先写失败测试，再做最小实现。

---

### Task 1: 统一角色、权限码与 Mock 身份合同

**Files:**
- Modify: `src/composables/usePermission.js`
- Modify: `src/components/PermissionButton.vue`
- Modify: `src/mock/data/accounts.js`
- Modify: `src/mock/data/routers.js`
- Modify: `src/mock/handlers/*.js`
- Modify: `src/views/members/index.vue`
- Modify: `src/views/members/components/MemberTable.vue`
- Modify: `src/views/recruitment/index.vue`
- Modify: `src/views/recruitment/components/CandidateTable.vue`
- Modify: `src/views/learning-materials/index.vue`
- Modify: `src/views/clothing-orders/index.vue`
- Modify: `src/composables/__tests__/usePermission.test.js`
- Modify: `src/views/members/__tests__/*.test.js`
- Modify: `src/views/recruitment/__tests__/*.test.js`
- Modify: `src/views/learning-materials/__tests__/learning-materials.test.js`
- Modify: `src/views/clothing-orders/__tests__/clothing-orders.test.js`
- Modify: `src/mock/handlers/__tests__/*.test.js`

**Interfaces:**
- Consumes: `getInfo()` 返回的 `roles: string[]` 与 `permissions: string[]`
- Produces: `isCeoRole(role)`、`hasBusinessRole(required, roles)`；页面按钮直接使用后端权限码

- [ ] **Step 1: 写角色兼容的失败测试**

```js
import { describe, expect, it } from 'vitest'
import { hasBusinessRole, isCeoRole } from '../usePermission'

describe('backend role keys', () => {
  it('treats admin as CEO and accepts backend management roles', () => {
    expect(isCeoRole('ceo')).toBe(true)
    expect(isCeoRole('admin')).toBe(true)
    expect(hasBusinessRole('ceo', ['admin'])).toBe(true)
    expect(hasBusinessRole('qt_mgmt', ['qt_mgmt'])).toBe(true)
    expect(hasBusinessRole('qt_manager', ['qt_manager'])).toBe(true)
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `npm run test:run -- src/composables/__tests__/usePermission.test.js`

Expected: FAIL，提示 `isCeoRole` 或 `hasBusinessRole` 未导出。

- [ ] **Step 3: 实现统一角色判断**

```js
export function isCeoRole(role) {
  return role === 'ceo' || role === 'admin'
}

export function hasBusinessRole(required, granted = []) {
  if (required === 'ceo') return granted.some(isCeoRole)
  return granted.includes(required) || granted.includes('admin')
}
```

`usePermission()` 的 `hasRole()` 改为调用 `hasBusinessRole()`；`PermissionButton.vue` 也统一使用 `hasRole()`，不再使用只做字符串相等判断的 `hasExactRole()`。其中 `admin` 可以满足任一业务角色，`ceo` 可以由 `ceo` 或 `admin` 满足。

- [ ] **Step 4: 将页面权限替换为真实后端权限码**

```text
member:import       -> system:user:import
member:remove       -> system:user:remove
member:resetPwd     -> system:user:resetPwd
member:retain       -> qt:member:retain
recruitment:list    -> qt:interview:admin:list
recruitment:feedback:edit -> qt:interview:admin:evaluate
recruitment:offer   -> qt:interview:admin:offer
recruitment:export  -> qt:interview:admin:export
material:write      -> qt:material:add / qt:material:remove
order:confirm       -> system:order:approve
```

页面角色判断替换为 `qt_mgmt`、`qt_manager`，CEO 使用统一 `isCeoRole`。

- [ ] **Step 5: 同步 Mock 账号、路由、处理器和测试夹具**

Mock 管理层账号使用 `roles: ['qt_mgmt']`，经理账号使用 `roles: ['qt_manager']`；CEO 继续使用 `ceo`；超级管理员继续使用 `admin` 与 `*:*:*`。Mock 处理器只接受新的后端权限码。

- [ ] **Step 6: 运行权限与页面测试**

Run: `npm run test:run -- src/composables/__tests__/usePermission.test.js src/views/members src/views/recruitment src/views/learning-materials src/views/clothing-orders src/mock/handlers`

Expected: PASS；CEO/admin 能看到高风险按钮，管理层和经理层只看到各自允许的操作。

- [ ] **Step 7: 提交任务**

```bash
git add src/composables src/components/PermissionButton.vue src/mock src/views/members src/views/recruitment src/views/learning-materials src/views/clothing-orders
git commit -m "refactor: align frontend roles and permissions"
```

### Task 2: 增加统一 Blob 文件下载能力

**Files:**
- Create: `src/utils/download.js`
- Create: `src/utils/__tests__/download.test.js`
- Modify: `src/utils/request.js`
- Modify: `src/api/members.js`
- Modify: `src/api/recruitment.js`
- Modify: `src/api/materials.js`
- Modify: `src/views/members/components/ImportMembersDialog.vue`
- Modify: `src/views/recruitment/index.vue`

**Interfaces:**
- Consumes: `service.request({ responseType: 'blob' })`、Mock 的普通 AjaxResult
- Produces: `requestBlob(config): Promise<{ blob: Blob, fileName: string }>`、`saveBlob(blob, fileName): void`

- [ ] **Step 1: 写文件名解析和保存的失败测试**

```js
import { describe, expect, it } from 'vitest'
import { parseContentDispositionFileName } from '../download'

describe('download helpers', () => {
  it('parses UTF-8 content disposition filenames', () => {
    const header = "attachment; filename*=UTF-8''%E6%88%90%E5%91%98.xlsx"
    expect(parseContentDispositionFileName(header)).toBe('成员.xlsx')
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `npm run test:run -- src/utils/__tests__/download.test.js`

Expected: FAIL，提示下载工具不存在。

- [ ] **Step 3: 实现下载工具**

```js
export function parseContentDispositionFileName(header = '') {
  const encoded = header.match(/filename\*=UTF-8''([^;]+)/i)?.[1]
  if (encoded) return decodeURIComponent(encoded)
  return header.match(/filename="?([^";]+)"?/i)?.[1] || ''
}

export function saveBlob(blob, fileName) {
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = fileName
  anchor.click()
  URL.revokeObjectURL(url)
}
```

`requestBlob()` 在真实请求模式直接使用 Axios `service`，处理 401 与 Blob JSON 错误；Mock 模式允许处理器返回可测试的 Blob 结果。

- [ ] **Step 4: 修改三类下载 API**

```js
export function downloadMemberTemplate() {
  return requestBlob({ url: '/system/user/importTemplate', method: 'post' }, '成员导入模板.xlsx')
}

export function exportRecruitmentList(params) {
  return requestBlob({ url: '/qt/interview/admin/applications/export', method: 'post', params }, '招新名单.xlsx')
}

export function downloadMaterial(material) {
  return requestBlob({ url: material.downloadUrl, method: 'get' }, material.fileName)
}
```

- [ ] **Step 5: 让模板和导出按钮真正保存文件**

下载成功后调用 `saveBlob(result.blob, result.fileName)`，成功提示改为“下载成功”；失败沿用页面错误提示。

- [ ] **Step 6: 运行下载及相关页面测试**

Run: `npm run test:run -- src/utils/__tests__/download.test.js src/views/members src/views/recruitment`

Expected: PASS，测试能观察到 Object URL 创建、隐藏链接点击和 URL 撤销。

- [ ] **Step 7: 提交任务**

```bash
git add src/utils src/api/members.js src/api/recruitment.js src/api/materials.js src/views/members/components/ImportMembersDialog.vue src/views/recruitment/index.vue
git commit -m "feat: add backend file download handling"
```

### Task 3: 对齐成员届次、成员列表与留任合同

**Files:**
- Modify: `src/api/members.js`
- Modify: `src/api/__tests__/members.test.js`
- Modify: `src/mock/handlers/members.js`
- Modify: `src/mock/data/members.js`
- Modify: `src/views/members/index.vue`

**Interfaces:**
- Consumes: `GET /qt/member/cohorts` 的 `{ data: { currentCohort, cohorts } }`
- Produces: `getMemberCohorts(): Promise<Array<{ value, label, isCurrent }>>`；`retainMember(userId, options?)`

- [ ] **Step 1: 写真实届次结构的失败测试**

```js
it('maps backend cohort payload to tab options', async () => {
  mockRequest.mockResolvedValue({
    code: 200,
    data: {
      currentCohort: { cohortId: 21, cohortName: '第21届', isCurrent: true },
      cohorts: [{ id: 21, name: '第21届', isCurrent: true }],
    },
  })
  await expect(getMemberCohorts()).resolves.toEqual([
    { value: '21', label: '第21届', isCurrent: true },
  ])
})
```

- [ ] **Step 2: 写留任请求失败测试**

验证 `retainMember(2002)` 请求 body 为 `{ retain: true }`，且可选 options 可以附加 `sourceCohortId`、`targetCohortId`。

- [ ] **Step 3: 运行成员 API 测试确认失败**

Run: `npm run test:run -- src/api/__tests__/members.test.js`

Expected: FAIL，当前实现将 `data` 对象当作数组，留任 body 仍为 `sourceCohort`。

- [ ] **Step 4: 实现届次映射和留任参数**

```js
export function mapCohorts(payload = {}) {
  const list = Array.isArray(payload) ? payload : payload.cohorts || []
  return list.map((item) => ({
    value: String(item.value ?? item.id ?? item.cohortId),
    label: item.label ?? item.name ?? item.cohortName,
    isCurrent: Boolean(item.isCurrent),
  }))
}

export function retainMember(userId, options = {}) {
  return request({
    url: `/qt/member/${userId}/retain`,
    method: 'put',
    data: { retain: true, ...options },
  })
}
```

- [ ] **Step 5: 更新 Mock 返回真实后端形状**

Mock `/qt/member/cohorts` 返回 `{ data: { currentCohort, cohorts } }`，Mock 留任处理器校验 `retain === true`，页面仍显示相同届次 tab。

- [ ] **Step 6: 运行成员模块测试**

Run: `npm run test:run -- src/api/__tests__/members.test.js src/views/members src/mock/handlers/__tests__/members.test.js`

Expected: PASS，当前届、历史届、留任弹窗和 Mock 幂等逻辑保持正常。

- [ ] **Step 7: 提交任务**

```bash
git add src/api/members.js src/api/__tests__/members.test.js src/mock/handlers/members.js src/mock/data/members.js src/views/members/index.vue
git commit -m "fix: align member cohort and retention contracts"
```

### Task 4: 对齐招新列表、详情、面评、录用与导出

**Files:**
- Modify: `src/api/recruitment.js`
- Modify: `src/api/__tests__/recruitment.test.js`
- Modify: `src/mock/data/recruitment.js`
- Modify: `src/mock/handlers/recruitment.js`
- Modify: `src/views/recruitment/index.vue`
- Modify: `src/views/recruitment/components/CandidateTable.vue`
- Modify: `src/views/recruitment/__tests__/*.test.js`

**Interfaces:**
- Consumes: 后端扁平申请记录、`data.application + data.profile`、二面 `notice` 请求字段
- Produces: 页面现有 `application.choices[]` 视图模型和真实录用请求

- [ ] **Step 1: 写扁平申请记录映射的失败测试**

```js
it('maps flat backend choices to page tracks', () => {
  const result = mapApplication({
    applicationId: 8,
    realName: '吴晓萌',
    firstChoice: 'DESIGN',
    secondChoice: 'PRODUCT',
    firstChoiceStatus: 'PASS',
    secondChoiceStatus: 'OUT',
    createTime: '2026-08-30 10:00:00',
  })
  expect(result.choices.map((item) => item.department)).toEqual(['DESIGN', 'PRODUCT'])
  expect(result.appliedAt).toBe('2026-08-30 10:00:00')
})
```

- [ ] **Step 2: 写详情合并与录用字段失败测试**

验证详情的 `application` 与 `profile` 合并；`sendOffer()` 将页面 `content` 转为后端 `notice` 并发送 `roundId: 2`。

- [ ] **Step 3: 运行招新 API 测试确认失败**

Run: `npm run test:run -- src/api/__tests__/recruitment.test.js`

Expected: FAIL，当前 mapper 只处理 `tracks[]`，详情未拆包，录用仍发送 `content`。

- [ ] **Step 4: 实现兼容真实后端与 Mock 的 mapper**

```js
function buildChoice(application, order) {
  const prefix = order === 1 ? 'firstChoice' : 'secondChoice'
  const department = application[prefix]
  if (!department) return null
  const status = application[`${prefix}Status`]
  const firstRoundStatus = application[`${prefix}FirstRoundStatus`] ?? status
  const secondRoundStatus = application[`${prefix}SecondRoundStatus`] ?? status
  return {
    choiceOrder: order,
    department,
    label: departmentLabels[department] || department,
    rounds: {
      1: { status: firstRoundStatus || 'PENDING' },
      2: { status: secondRoundStatus || 'PENDING', advanced: secondRoundStatus === 'PASS' },
    },
  }
}
```

若 `tracks[]` 已存在则保留并规范化；否则由扁平字段创建 choices。若后端暂未返回分轮状态字段，则回退到志愿总状态，并将“缺少独立一面/二面状态字段”写入最终待确认清单。`FAIL` 与后端文案 `OUT` 在 mapper 中统一为页面状态 `FAIL`。

- [ ] **Step 5: 修正详情、面评和录用请求**

```js
const payload = response.data || {}
return mapApplication({ ...(payload.application || payload), ...(payload.profile || {}) })
```

面评列表优先读取 `response.data`，其次读取 `response.rows`。录用请求显式构造 `{ applicationId, department, decision, notice, roundId: 2 }`。

- [ ] **Step 6: 更新 Mock 合同和招新页面测试**

Mock 列表至少增加一条扁平后端形状样例；保留双志愿、部门隔离和二面默认第一志愿规则的测试。页面测试角色与权限使用真实 key。

- [ ] **Step 7: 运行招新模块测试**

Run: `npm run test:run -- src/api/__tests__/recruitment.test.js src/views/recruitment src/mock/handlers/__tests__/recruitment.test.js`

Expected: PASS，一面/二面页面、面评弹窗、录用通知和导出按钮行为不退化。

- [ ] **Step 8: 提交任务**

```bash
git add src/api/recruitment.js src/api/__tests__/recruitment.test.js src/mock/data/recruitment.js src/mock/handlers/recruitment.js src/views/recruitment
git commit -m "fix: align recruitment admin API contracts"
```

### Task 5: 对齐学习资料字段、上传权限和下载入口

**Files:**
- Modify: `src/api/materials.js`
- Modify: `src/api/__tests__/materials-clothing.test.js`
- Modify: `src/mock/data/materials.js`
- Modify: `src/mock/handlers/materials.js`
- Modify: `src/views/learning-materials/index.vue`
- Modify: `src/views/learning-materials/__tests__/learning-materials.test.js`

**Interfaces:**
- Consumes: `uploaderName`、`uploadTime/createTime`、`downloadUrl`、后端可见性枚举
- Produces: `{ materialId, fileName, fileSize, category, visibility, uploader, uploadedAt, downloadUrl }`

- [ ] **Step 1: 写资料字段映射失败测试**

```js
it('maps backend material fields to the page model', async () => {
  const result = await getMaterials()
  expect(result.rows[0]).toMatchObject({
    uploader: '张晓雪',
    uploadedAt: '2026-08-30 09:00:00',
    downloadUrl: '/qt/materials/1/download',
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `npm run test:run -- src/api/__tests__/materials-clothing.test.js`

Expected: FAIL，当前 API 未做字段映射。

- [ ] **Step 3: 实现资料 mapper 和上传元数据**

```js
export function mapMaterial(item = {}) {
  return {
    ...item,
    uploader: item.uploaderName || item.uploader || '-',
    uploadedAt: item.uploadTime || item.createTime || item.uploadedAt || '-',
    downloadUrl: item.downloadUrl || `/qt/materials/${item.materialId}/download`,
  }
}
```

`uploadMaterial(file, { category = '', visibility = 'MEMBER' } = {})` 将 file、category、visibility 写入 FormData。

- [ ] **Step 4: 添加资料下载入口**

文件名改为可点击的下划线文字按钮，调用 Task 2 的 `downloadMaterial()`。视觉沿用项目现有链接样式，不增加新的卡片或工具栏。

- [ ] **Step 5: 修正可见性文案**

```js
const visibilityLabels = {
  ALL: '所有人可见',
  MEMBER: '仅塔员可见',
  DEPT: '仅本部门可见',
}
```

- [ ] **Step 6: 更新 Mock 与测试**

Mock 返回 `uploaderName`、`uploadTime` 和 `downloadUrl`，处理器使用 `qt:material:add`、`qt:material:remove`。测试覆盖管理者上传、上传者删除、普通用户只读和下载点击。

- [ ] **Step 7: 运行资料与塔服测试**

Run: `npm run test:run -- src/api/__tests__/materials-clothing.test.js src/views/learning-materials src/views/clothing-orders src/mock/handlers/__tests__/materials-clothing.test.js`

Expected: PASS；确认收款使用 `system:order:approve`，资料显示真实字段并可下载。

- [ ] **Step 8: 提交任务**

```bash
git add src/api/materials.js src/api/__tests__/materials-clothing.test.js src/mock/data/materials.js src/mock/handlers/materials.js src/views/learning-materials
git commit -m "fix: align material management contracts"
```

### Task 6: 全量验证并输出剩余联调清单

**Files:**
- Create: `docs/backend-integration-open-items.md`
- Modify only if required by failures: files changed in Tasks 1-5

**Interfaces:**
- Consumes: Tasks 1-5 的完整实现与测试结果
- Produces: 可构建的 Mock 管理端和明确的后端待确认清单

- [ ] **Step 1: 搜索遗留旧角色和权限码**

Run:

```bash
rg -n "management|manager|member:|recruitment:|material:write|order:confirm" src --glob "*.js" --glob "*.vue"
```

Expected: 仅允许出现在用户可见普通英文、历史说明或明确的迁移测试中；业务权限判断不再使用旧 key。

- [ ] **Step 2: 运行全部测试**

Run: `npm run test:run`

Expected: 全部 PASS，无未处理 Promise rejection。

- [ ] **Step 3: 运行代码检查和生产构建**

Run: `npm run lint`

Expected: PASS。

Run: `npm run build`

Expected: PASS，生成 `dist`，不提交 `dist`。

- [ ] **Step 4: 确认开发环境仍启用 Mock**

Run: `Get-Content .env.development`

Expected: `VITE_USE_MOCK=true`，没有被本次任务改为 false。

- [ ] **Step 5: 编写剩余联调清单**

`docs/backend-integration-open-items.md` 必须明确记录：

1. `/system/user/resetPwd` 是否必须由前端传 password，默认密码或临时密码如何确定。
2. 一面 Pass/Out 是否继续调用 `/qt/interview/result`，还是存在未写入说明的 admin 评定接口。
3. `getInfo.permissions` 中学习资料列表权限实际是否为 `qt:material:list`。
4. 招新扁平列表是否同时返回各轮次独立状态；若只返回一个状态，前端二面展示需要哪个字段。
5. 后端是否已执行 `lab_patch_admin.sql`，是否已为测试账号绑定角色和 `memberDepartment`。
6. `qtCohortJob.rollover` 在生产环境由谁启用、首次执行日期和回滚策略。
7. 文件流的 `Content-Disposition` 文件名和跨域暴露响应头是否已配置。

- [ ] **Step 6: 提交验证文档和必要修复**

```bash
git add docs/backend-integration-open-items.md src
git commit -m "docs: record remaining backend integration questions"
```
