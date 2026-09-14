# Quanta 管理端 Web 接口 Mock 清单

更新日期：2026-09-13  
适用项目：`Quanta-admin-web`

## 使用规则

- Web 端默认连接真实后端 `http://127.0.0.1:8080`。
- 只有经过真实联调确认暂不可用的接口，才能标记为“Mock”并加入代码白名单。
- `/login`、`/getInfo`、`/getRouters`、`/logout` 禁止使用局部 Mock。
- 401、403、参数错误和普通 500 不自动回退 Mock。
- 写接口未经安全联调时标记为“待复测”，不能为了页面看似成功而直接使用 Mock。

## 当前使用 Mock 的接口

真实后端的届次接口当前返回空数据，已加入显式 Mock 白名单。动态菜单尚未配置 Quanta Web 页面，因此路由层也启用了明确登记的本地菜单兜底。

| 模块 | 方法 | 路径 | 当前问题 | Mock 原因 | 临时行为 | 复测条件 | 状态 | 最近确认 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 动态菜单 | `GET` | `/getRouters` | 返回若依系统菜单，未返回 Quanta Web 页面 | 联调库尚未执行对应菜单初始化 | 认证和权限仍取真实后端，仅在响应中没有任何受支持页面时使用本地 Web 路由目录，并按真实权限过滤 | 后端返回 `dashboard/index` 等 Quanta 页面组件 | Mock | 2026-09-13 |
| 成员届次 | `GET` | `/qt/member/cohorts` | 业务码 200，但 `currentCohort` 为 `null` 且 `cohorts` 为空 | 联调库缺少届次数据 | 返回与真实成员字段兼容的本地届次，成员列表仍请求真实后端 | 后端返回当前届次和非空届次列表 | Mock | 2026-09-13 |

## 已验证的真实接口

以下接口已使用 `admin / admin123` 在本机后端完成读取冒烟，业务码均为 200：

- `POST /login`
- `GET /getInfo`
- `GET /getRouters`
- `GET /dashboard/stats`
- `GET /qt/member/cohorts`
- `GET /qt/member/list`
- `GET /qt/interview/admin/statistics`
- `GET /qt/interview/admin/applications`
- `GET /qt/interview/admin/evaluations`
- `GET /qt/activity/lecture/registrations`
- `GET /qt/activity/sharing/registrations`
- `GET /qt/materials`
- `GET /system/reservation/detailList`
- `GET /system/borrow/detailList`
- `GET /system/order/detailList`

## 待复测但暂不使用 Mock

| 模块 | 方法 | 路径 | 待确认事项 | 状态 | 最近确认 |
| --- | --- | --- | --- | --- | --- |
| 成员管理 | `PUT` | `/system/user/resetPwd` | 前端缺少明确的新密码来源，不能安全执行 | 待复测 | 2026-09-13 |
| 招新管理 | `POST` | `/qt/interview/result` | 需确认一面 Pass / Out 的正式管理端契约 | 待复测 | 2026-09-13 |
| 招新管理 | `GET` | `/qt/interview/admin/applications` | 需用多轮次数据确认各志愿分轮状态字段 | 待复测 | 2026-09-13 |
| 招新管理 | `GET` | `/qt/interview/admin/statistics` | 需确认一面通过、二面通过、确认加入的精确字段含义 | 待复测 | 2026-09-13 |
| 文件下载 | `POST/GET` | 成员模板、招新导出、学习资料下载 | 需在存在可下载文件时确认文件名响应头 | 待复测 | 2026-09-13 |

## 状态维护

- `Mock`：真实接口已确认缺失或阻塞，代码白名单中存在对应规则。
- `待复测`：尚未安全完成联调，不进入 Mock 白名单。
- `已切真实`：真实接口验证通过，不在 Mock 白名单中。
