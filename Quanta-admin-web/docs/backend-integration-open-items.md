# 后端联调剩余确认清单

更新日期：2026-08-31

当前前端已按《前端对接说明》完成角色、权限码、成员届次、留任、招新、文件流和学习资料字段适配；开发环境仍保持 `VITE_USE_MOCK=true`。以下事项无法仅凭现有文档确定，需要联调前由后端确认。

## P0：会阻塞具体操作

### 1. 重置密码的 password 从哪里产生

- 后端合同：`PUT /system/user/resetPwd`，body 为 `{ userId, password }`。
- 当前前端：仍只发送 `{ userId }`，未擅自添加默认密码。
- 需要确认：固定初始密码、后端生成临时密码，还是由 CEO 在弹窗中输入新密码；如果后端生成，需要明确响应是否返回临时密码。

### 2. 一面 Pass / Out 的最终管理端路径

- 文档一处说明 `/qt/interview/result` 已恢复 `qt:interview:admin:evaluate` 权限，另一处又要求改用 admin 接口。
- 现有 admin 接口中，`/qt/interview/admin/evaluations` 是面评内容，`/qt/interview/admin/offers` 是二面录用，没有单独列出一面结果接口。
- 当前前端：一面仍调用 `POST /qt/interview/result`。
- 需要确认：该路径是否是正式方案；若不是，请提供一面 Pass / Out 的方法、路径和 body。

### 3. 招新列表是否返回各轮次独立状态

- 文档只明确 `firstChoiceStatus`、`secondChoiceStatus`，但页面需要同时还原一面和二面状态。
- 当前前端：优先读取可选的 `firstChoiceFirstRoundStatus`、`firstChoiceSecondRoundStatus` 等字段；缺失时回退到志愿总状态，并维持“两志愿均通过时默认第一志愿进入二面”的规则。
- 需要确认：按 `round/roundId` 查询时两个 status 字段代表指定轮次，还是代表最终状态；详情是否能返回每个志愿的分轮结果。

## P1：不阻塞 Mock，但会影响真实权限或数据准确性

### 4. 招新看板字段和设计稿四项指标不完全对应

- 后端提供：`submittedCount`、`processingCount`、`offeredCount`、`rejectedCount`、`pendingEvalCount`、`pendingOfferCount`、`todayResumeCount`。
- 当前页面需要：投递总数、一面通过、二面通过、确认加入。
- 当前前端临时映射：`submittedCount -> 投递总数`、`processingCount -> 一面通过`、`offeredCount -> 二面通过`；确认加入仅在后端返回 `joinedCount` 时展示。
- 需要确认：`processingCount` 是否等于一面通过人数；是否补充 `firstPassedCount`、`secondPassedCount`、`joinedCount`，或允许页面改用后端现有指标。

### 5. 学习资料列表的菜单权限码

- 文档写明 `GET /qt/materials` 登录即可，且“有管理 list 权限可看全部”，但未在接口表中明确 list 权限码。
- 当前前端与 Mock 暂用 `qt:material:list` 控制管理端菜单和列表入口；新增、删除已使用明确的 `qt:material:add`、`qt:material:remove`。
- 需要确认：真实 `getInfo.permissions` 和 `getRouters` 是否会返回 `qt:material:list`。

### 6. 文件流响应头

- 涉及成员模板、招新导出、学习资料下载。
- 前端已支持 Blob、UTF-8 `Content-Disposition` 文件名和后端 JSON 错误 Blob。
- 需要确认：响应是否设置 `Content-Disposition`；跨域时是否暴露该响应头（`Access-Control-Expose-Headers: Content-Disposition`）。

### 7. 数据库补丁和测试账号是否就绪

- 需要确认生产/联调库已执行 `sql/lab/lab_patch_admin.sql`。
- `qt_mgmt`、`qt_manager` 测试账号需要绑定对应角色和 `memberDepartment`，否则部门隔离接口无法验证。

## P2：上线运维确认

### 8. 每年 8 月 1 日换届任务

- 后端已实现 `qtCohortJob.rollover`，说明中标记为默认暂停。
- 需要确认：由谁启用、首次执行时间、服务器时区、重复执行幂等性和错误回滚方式。

### 9. 真实后端地址

- 当前 `.env.development` 保持 `VITE_USE_MOCK=true`，`VITE_API_BASE_URL` 为空。
- 开始联调时需要提供实际 host，并确认开发环境是否通过 Vite 代理访问 `http://<host>:8080`，还是直接配置完整地址及 CORS。
