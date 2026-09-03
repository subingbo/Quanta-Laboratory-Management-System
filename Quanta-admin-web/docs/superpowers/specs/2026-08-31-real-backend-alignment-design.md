# Quanta 管理端真实后端适配设计

## 目标

在继续启用 `VITE_USE_MOCK=true` 的前提下，将管理端的角色、权限码、请求参数、响应映射和文件下载行为与后端《前端对接说明.md》（2026-08-30）对齐。页面继续使用稳定的前端视图模型，因此切换 Mock 与真实后端时不需要修改页面组件。

## 范围

本次包含：

1. 以后端角色 key 和权限码作为前端唯一标准。
2. 同步更新 Mock 账号、路由和处理器的角色及权限码。
3. 修正成员届次、留任、招新、学习资料等接口的数据映射。
4. 为成员导入模板、招新导出和学习资料下载增加统一 Blob 下载能力。
5. 更新相关单元测试并执行完整测试与生产构建。

本次不包含：

1. 不关闭 Mock，不修改 `.env.development` 的 `VITE_USE_MOCK=true`。
2. 不改变已确认的页面视觉设计。
3. 不擅自确定重置密码的默认密码。
4. 不执行后端数据库补丁或启用换届定时任务。

## 设计选择

采用“后端口径为标准、API 边界统一转换”的方案。

不保留一套长期的新旧权限别名。页面、Mock 和测试统一使用真实后端权限码，避免联调时按钮无故消失。接口返回值在 `src/api` 中转换为页面现有视图模型，页面组件不直接依赖后端原始结构。

## 角色与权限

### 角色

前端使用以下后端角色 key：

| 角色 | key | 前端判断 |
|---|---|---|
| CEO | `ceo` | 拥有 CEO 操作能力 |
| 超级管理员 | `admin` | 在业务权限判断中等同 CEO |
| 管理层 | `qt_mgmt` | 招新管理、成员只读等管理功能 |
| 经理层 | `qt_manager` | 仅本部门二面面评等经理功能 |

### 权限码

| 前端能力 | 后端权限码 |
|---|---|
| 控制台统计 | `qt:dashboard:stats` |
| 成员列表 | `qt:member:list` |
| 成员留任 | `qt:member:retain` |
| 删除成员 | `system:user:remove` |
| 重置密码 | `system:user:resetPwd` |
| 导入成员/模板 | `system:user:import` |
| 招新列表/详情/查看面评 | `qt:interview:admin:list` |
| 编辑面评/一面评定 | `qt:interview:admin:evaluate` |
| 二面录用 | `qt:interview:admin:offer` |
| 招新导出 | `qt:interview:admin:export` |
| 活动报名名单 | `qt:activity:registrations` |
| 工位预约记录 | `system:reservation:list` |
| 图书借阅记录 | `system:borrow:list` |
| 塔服订单列表 | `system:order:list` |
| 确认收款 | `system:order:approve` |
| 学习资料列表 | `qt:material:list`（若后端实际下发不同，以真实 `getInfo.permissions` 为准） |
| 上传资料 | `qt:material:add` |
| 修改资料 | `qt:material:edit` |
| 删除资料 | `qt:material:remove` |

权限组件需要支持 `admin` 的超级管理员语义。除 `*:*:*` 权限外，角色判断中的 `admin` 也应满足 CEO 要求。

## API 边界与数据流

### 成员管理

`GET /qt/member/cohorts` 返回：

```js
{
  data: {
    currentCohort: { cohortId, cohortName, isCurrent },
    cohorts: [{ id, name, isCurrent }]
  }
}
```

API 层转换为页面需要的：

```js
[
  { value: String(id), label: name, isCurrent: Boolean(isCurrent) }
]
```

`PUT /qt/member/{userId}/retain` 本次只发送：

```js
{ retain: true }
```

由后端使用当前届并在必要时创建下一届。API 函数保留未来传入 `sourceCohortId`、`targetCohortId` 的扩展位置。

重置密码接口暂不改动产品行为。现有请求继续保留，并在最终不确定项清单中注明后端文档要求 `{ userId, password }`，等待双方确认默认密码或后端生成临时密码的方案。

### 招新管理

真实列表返回的 `firstChoice`、`secondChoice`、对应状态和投递字段，在 API 层转换为页面现有 `choices[]` 模型。转换逻辑允许 Mock 已有的 `tracks[]` 继续工作，确保两种数据源输出相同视图模型。

详情接口的 `data.application` 与 `data.profile` 合并后再交给简历弹窗。面评列表同时接受 AjaxResult 的 `data` 数组和可能的分页 `rows`。

二面录用请求使用后端字段：

```js
{
  applicationId,
  department,
  decision,
  notice,
  roundId: 2
}
```

一面评定继续调用当前 `/qt/interview/result`，但使用真实权限 `qt:interview:admin:evaluate`。后端说明中“请改用 admin 接口”没有给出独立的一面 Pass/Out 路径，因此将其列为实际联调确认项。

### 学习资料

API 层将后端字段映射为页面字段：

| 后端 | 页面模型 |
|---|---|
| `uploaderName` | `uploader` |
| `uploadTime` 或 `createTime` | `uploadedAt` |
| `downloadUrl` | `downloadUrl` |

上传仍允许仅传文件，后端使用 `visibility=MEMBER` 默认值。页面增加文件下载入口；下载前由后端校验 `ALL`、`MEMBER`、`DEPT` 可见范围。

### 文件流

增加统一下载工具，职责为：

1. Axios 请求设置 `responseType: 'blob'`。
2. 从 `Content-Disposition` 解析 UTF-8 或普通文件名。
3. 使用 `URL.createObjectURL` 和隐藏 `<a>` 触发下载。
4. 下载结束后撤销 Object URL。
5. 若后端以 Blob 返回 JSON 错误，解析并转为现有请求错误。

成员模板和招新导出必须使用该工具；学习资料下载使用后端提供的下载路径。

## 环境与 Mock 兼容

`.env.development` 保持：

```env
VITE_USE_MOCK=true
```

本次不启动真实联调。Mock 账号、动态路由、处理器和测试夹具改为真实角色及权限码，使 Mock 成为真实后端合同的本地替身，而不是另一套权限系统。

真实联调时需要同时设置：

```env
VITE_USE_MOCK=false
VITE_API_BASE_URL=http://localhost:8080
```

## 错误处理

继续使用现有 RuoYi 响应规范：AjaxResult 非 `code=200` 时抛出业务错误，TableDataInfo 从根级读取 `rows/total`，登录从根级读取 `token`。

文件流错误需要额外解析 Blob 中的 JSON。权限不足由后端返回 403，前端按钮隐藏只用于用户体验，不作为安全边界。

## 测试策略

1. API 测试覆盖届次对象到数组的转换、扁平招新记录到 `choices[]` 的转换、详情合并、录用 `notice` 字段及资料字段映射。
2. 权限测试覆盖 `ceo`、`admin`、`qt_mgmt`、`qt_manager` 与真实权限码。
3. Mock 处理器测试改用真实权限码，验证 CEO 高风险操作和部门角色边界。
4. 文件下载工具测试覆盖文件名解析、Blob 保存和 JSON 错误。
5. 运行全部 Vitest 测试与 `npm run build`。

## 完成标准

1. 开发环境仍使用 Mock，现有页面可以正常浏览和操作。
2. 前端不再以旧 Mock 权限码作为页面功能判断依据。
3. 后端文档中已明确的请求参数和响应字段已在 API 层适配。
4. 三类文件流可以触发真实浏览器下载。
5. 全部测试与生产构建通过。
6. 输出一份“仍需后端确认/真实联调验证”的清单，至少包含重置密码、一面评定路径、学习资料列表权限码和后端 SQL/定时任务部署状态。

