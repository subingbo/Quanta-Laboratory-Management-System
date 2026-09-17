# 招新面评线程对接说明（管理端）

给前端同学：后端已改成「每人一条、互不覆盖」的面评线程。本文档只说明管理端招新弹窗怎么接，**不要把写面评和 Pass/Out 绑在同一个权限码上**。不改小程序。

## 目标

- 面评弹窗改成聊天记录式线程：按时间正序展示该志愿下**全部塔员**的评论。
- 每条展示**登录用户名** `evaluatorUserName`（`sys_user.user_name`，全站唯一），不要用昵称 `evaluatorName` / `nick_name` 当身份。
- 当前登录用户只能编辑/保存**自己的那条**；别人的只读。
- 管理员和其他塔员都能看完整线程。
- Pass / Out、录用通知权限不变，仍只有管理层能改面试结果。

## 权限拆分（必读）

| 能力 | 接口 | 鉴权 | 谁能调 |
| --- | --- | --- | --- |
| 读面评线程 | `GET /qt/interview/admin/evaluations` | 登录且为塔员（`isQuantaMember=1`） | 全体塔员 + 管理员 |
| 写/更新自己的面评 | `POST /qt/interview/admin/evaluations` | 同上 | 全体塔员（一面、二面都可以） |
| 改自己已有面评 | `PUT /qt/interview/admin/evaluations/{evaluationId}` | 同上 | 只能改自己的行 |
| Pass / Out | `POST /qt/interview/result` | `qt:interview:admin:evaluate` | **仅管理层** |
| 录用 / 淘汰通知 | `POST /qt/interview/admin/offers` | `qt:interview:admin:offer` | **仅管理层** |

不要再用 `qt:interview:admin:evaluate` 控制「能不能写面评」。没有 evaluate 权限的塔员也可以 POST 面评；他们点 Pass/Out 会被 403。

## 列表：线程展示

`GET /qt/interview/admin/evaluations`

Query：

| 字段 | 说明 |
| --- | --- |
| `applicationId` | 申请 ID |
| `roundId` | 轮次，传 `1` / `2`（`round_no`）即可，后端会解析成真实 `round_id` |
| `department` | 志愿部门码，如 `BACKEND`。必须是该申请的第一或第二志愿 |

返回 `AjaxResult.data`：数组，已按 `createTime` **正序**（旧的在上、新的在下）。不再按当前账号部门裁掉别人的评论。

每条字段：

| 字段 | 用途 |
| --- | --- |
| `evaluationId` | 行 ID，编辑自己的那条时用 |
| `evaluatorUserId` | 作者用户 ID，用来判断「是不是我的」 |
| `evaluatorUserName` | **登录用户名，线程里显示这个** |
| `evaluatorName` | 昵称，仅辅助，不要当唯一身份 |
| `content` | 面评正文 |
| `createTime` | 创建时间，`yyyy-MM-dd HH:mm:ss`（同时有 `createdTime` 别名） |
| `updateTime` | 最后修改时间 |
| `department` / `roundId` / `applicationId` | 归属 |

当前页面对字段名的映射需要改：

- 现有 `interviewerName` → 改用 `evaluatorUserName`
- 现有 `interviewerId` → 改用 `evaluatorUserId`

UI 建议：

```
[evaluatorUserName]    createTime
content
```

当前用户那条后面给「编辑」；别人的没有编辑入口。找不到自己的行时，底部给空输入框用于新增。

## 写入：每人一条

`POST /qt/interview/admin/evaluations`

Body：

```json
{
  "applicationId": 12,
  "roundId": 1,
  "department": "BACKEND",
  "content": "表达清晰，基础扎实。"
}
```

规则：

- 后端按 `(applicationId, roundId, department, 当前登录 userId)` upsert **自己的行**，不会覆盖别人。
- 请求里带 `evaluatorUserId` 无效，一律写成当前登录人。
- `roundId` 传 `1`/`2` 即可。
- `department` 必须是候选人第一或第二志愿；不按「必须本部门」拦截。塔员可以给该候选人两个志愿都写面评。
- 经理层也可以写一面，不再限制只能评二面。

自己已有一条时，再次 POST 同一轮次+志愿会更新正文，而不是再插一行。

显式改某条（仍只能改自己的）：

`PUT /qt/interview/admin/evaluations/{evaluationId}`

```json
{ "content": "补充：项目经历一般。" }
```

改别人的行会返回业务错误：`无权修改他人面评`。

## Pass / Out / 录用（不要和线程绑在一起）

- 一面 Pass/Out：继续 `POST /qt/interview/result`，权限码 `qt:interview:admin:evaluate`。
- 二面录用/淘汰：继续 `POST /qt/interview/admin/offers`，权限码 `qt:interview:admin:offer`。
- 弹窗底部 Pass/Out 按钮继续按**原有角色权限**显示；有权写面评 ≠ 有权改结果。
- 面评线程区域所有塔员可见可写自己的评论；结果按钮与线程解耦。

## 验收

- 两个塔员对同一申请、同一轮次、同一志愿各写一条，列表里两条都在，作者显示各自 `user_name`，互不覆盖。
- 只能改自己的；点别人的保存应失败。
- 无 evaluate 权限的塔员能保存面评，点 Pass/Out 失败。
- 线程按时间从早到晚排。
