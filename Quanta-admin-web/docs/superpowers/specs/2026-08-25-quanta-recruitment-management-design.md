# Quanta 招新管理模块设计规格

## 1. 目标与范围

本阶段在现有 Vue 3、Vite、Element Plus、Pinia、动态路由和 Mock/API 双数据源基础上，实现可完整演示的招新管理模块。模块包含招聘看板、一面名单、二面名单、简历预览、面评编辑、部门独立评定、录用通知和导出入口。

后端尚未上线，页面优先使用 Mock 数据完成流程。所有业务请求统一经 `src/api/recruitment.js`，后续只替换请求目标和响应适配，不改页面组件。

本规格同时记录后端接口缺口和部门数据权限要求。宣讲会、精英分享会等其他模块不在本次范围。

## 2. 已确认的核心规则

### 2.1 双志愿独立评审

每份报名申请只有一条候选人记录，但第一志愿和第二志愿是两条独立的部门评审轨道。每条轨道分别保存一面、二面和最终录用状态。

- 只有一个志愿一面通过时，该志愿对应部门进入二面。
- 两个志愿均通过时，默认只让第一志愿进入二面；第二志愿保留“一面 Pass”结果，但不创建二面评审轨道。
- 第一志愿一面 Out、第二志愿一面 Pass 时，第二志愿进入二面。
- 一面阶段只有两个志愿均 Out 时，候选人才无法进入二面。
- 二面继续按最终晋级部门进行独立评审，同一候选人在本阶段最多只有一条二面轨道。
- 未晋级的第二志愿保持一面结果不变，作为历史评审记录；本阶段不自动触发递补，也不再影响二面的最终录用结果。
- 二面通过的轨道进入待录用状态。
- 招聘看板人数按唯一 `applicationId` 去重，不按轨道重复计数。

### 2.2 部门数据范围

非 CEO 管理层和经理层都严格限制在本人所属部门。经理层只能编辑该轨道的面评；管理层只能查看面评并提交评定或录用通知。

用户部门来自若依 `/getInfo` 返回的 `user.dept` 或后端新增的稳定部门编码字段。业务判断使用稳定编码，不使用中文显示名。当前面试接口的部门枚举为 `BACKEND`、`PRODUCT`、`DESIGN`、`FRONTEND`、`ANDROID`，页面通过字典映射显示中文。

普通管理层进入一面或二面页面时：

- 后端列表接口只返回至少有一个志愿匹配本人部门的申请。
- 表格保留两个志愿作为上下文，但只有本人部门对应志愿可操作。
- 所有面评、评定和录用弹窗都锁定本人部门轨道，不允许切换到其他部门。
- 前端再次检查部门匹配，隐藏不允许的操作入口。
- 后端必须根据登录身份重新校验部门；不得信任前端提交的 `department`。
- 越权查看或提交统一返回 `403`，前端提示“无权操作其他部门的招新数据”。

CEO 可处理跨部门业务。若依超级管理员 `admin` 是否拥有跨部门业务权限暂不纳入本阶段授权模型；不能仅因为持有 `*:*:*` 就在前端默认展示跨部门高风险业务操作。

## 3. 权限模型

权限标识决定“能否执行某类操作”，部门编码决定“能操作哪些数据”。前端权限只用于改善交互，后端鉴权才是安全边界。

### 3.1 招新权限

| 权限标识 | 用途 | 经理层 | 管理层 | CEO |
| --- | --- | --- | --- | --- |
| `recruitment:list` | 查看招新模块和名单 | 本部门 | 本部门 | 全部门 |
| `recruitment:resume:view` | 查看候选人简历 | 本部门一面 | 本部门一面 | 全部门一面 |
| `recruitment:feedback:view` | 查看面评 | 本部门 | 本部门 | 全部门 |
| `recruitment:feedback:edit` | 新增或编辑本人面评 | 本部门 | 无 | 无 |
| `recruitment:decision` | 提交一面 Pass/Out | 无 | 本部门 | 全部门 |
| `recruitment:offer` | 二面结果与录用/淘汰通知 | 无 | 本部门 | 全部门 |
| `recruitment:export` | 导出当前名单 | 无 | 本部门 | 全部门 |

“已评”只对经理层显示，表示当前登录用户已经为当前轮次、当前部门轨道提交面评。已有面评允许再次编辑。

### 3.2 成员管理高风险权限修正

以下权限只授予 CEO：

- `member:import`：导入成员名单。
- `member:remove`：删除成员。
- `member:resetPwd`：重置成员密码。

其他管理层和普通成员不展示对应按钮，直接调用接口时后端同样必须拒绝。现有成员管理页面仍使用权限标识判断，不在组件内硬编码职位名称。

## 4. 页面与交互设计

### 4.1 招聘看板

顶部使用紧凑胶囊 Tab：招聘看板、一面、二面。看板展示投递总数、一面通过人数、二面通过人数和确认加入人数。统计卡沿用设计稿的四列布局，字号和控件密度遵循已确认的紧凑业务页面规范。

### 4.2 一面名单

表格保留设计稿字段：姓名/学号、专业/班级、第一志愿、第二志愿、投递时间、操作。志愿标签旁显示当前轮次状态：待评、Pass 或 Out。

操作入口：

- “阅览简历”打开简历预览弹窗。
- 经理层显示带下划线的“编辑面评”，已提交时同时显示绿色“已评”。
- 管理层显示带下划线的“查看面评”；弹窗内只读展示历史面评，底部 Pass/Out 进入锁定志愿的二次确认。

### 4.3 二面名单

二面只展示按照第一志愿优先规则真正晋级的候选人轨道。经理层操作列只有带下划线的“编辑面评”；管理层操作列只有带下划线的“是否录用”。

“是否录用”先打开锁定志愿的确认弹窗，确认后进入通知编辑弹窗。Pass/Out 会切换通知模板，发送时必须原子保存二面结果和通知内容。

### 4.4 弹窗

- 简历预览：展示姓名、学号、专业班级、两个志愿、邮箱、个人简介、编程经历、Quanta 认知和照片。
- 面评编辑：标题明确显示轮次和部门，例如“一面面评｜产品部｜陈思思”；包含面评文本和提交按钮。
- 查看面评：按面试官展示多条历史评价，普通管理层只看本人部门，CEO 可查看全部部门。
- 评定确认：明确提示当前轮次、候选人和部门轨道，避免误操作另一个志愿。
- 录用通知：显示最终部门、接收人和通知内容，发送前二次确认。

## 5. 前端架构

### 5.1 文件边界

- `src/api/recruitment.js`：列表、详情、面评、结果、统计、通知和导出 API 适配。
- `src/mock/data/recruitment.js`：候选人、志愿轨道、面评和看板 Mock 数据。
- `src/mock/handlers/recruitment.js`：Mock 查询、部门数据范围、状态流转和权限校验。
- `src/views/recruitment/index.vue`：页面级查询状态、Tab 和弹窗编排。
- `src/views/recruitment/components/RecruitmentTabs.vue`：三类 Tab。
- `src/views/recruitment/components/RecruitmentBoard.vue`：统计卡。
- `src/views/recruitment/components/CandidateTable.vue`：一面、二面复用表格。
- `src/views/recruitment/components/ResumeDialog.vue`：简历详情。
- `src/views/recruitment/components/FeedbackDialog.vue`：面评编辑与查看。
- `src/views/recruitment/components/OfferConfirmDialog.vue`：二面志愿锁定确认。
- `src/views/recruitment/components/OfferDialog.vue`：录用通知。
- `src/views/recruitment/recruitment.css`：紧凑业务页面样式。

### 5.2 状态模型

候选人以申请为根对象，每个志愿转成独立轨道：

```js
{
  applicationId,
  candidate,
  tracks: [
    {
      choiceOrder: 1,
      department: 'PRODUCT',
      rounds: {
        1: { status: 'PENDING', evaluations: [] },
        2: { status: 'PENDING', evaluations: [], advanced: false }
      }
    }
  ]
}
```

页面通过 `applicationId + department + roundId` 唯一定位一个评审轨道。`advanced` 明确标识该志愿是否真正进入二面，避免把保留的一面 Pass 误当成二面名单。面试官评价与部门最终结果分开保存，避免多位面试官覆盖同一个 `feedback` 字段。

## 6. 现有接口映射

可直接或经过适配使用的接口：

- `GET /getInfo`：获取角色、权限和用户部门。
- `POST /qt/interview/result`：提交部门轮次结果；必须补充权限和部门数据范围校验。
- `GET /qt/interview/myResults`：仅适合应聘者查询自己的结果，不能替代管理端结果列表。
- `GET /qt/interview/my`：仅适合应聘者查询自己的申请，不能替代管理端简历详情。

`POST /qt/interview/result` 的 `department` 必须与登录用户部门一致，CEO 例外。后端应优先从登录上下文推导允许部门，不能直接接受任意部门值。

## 7. 后端接口缺口清单

### P0：页面核心流程

1. 管理端候选人分页列表：支持轮次、部门、结果状态、姓名/学号筛选，并在服务端应用部门数据范围。
2. 管理端候选人简历详情：返回申请、个人资料、照片访问地址和两条志愿轨道。
3. 按申请、轮次和部门查询结果：支持回显当前状态。
4. 多面试官面评列表：按 `applicationId + roundId + department` 查询。
5. 新增/编辑本人面评：面评记录包含面试官身份，不能覆盖其他人的评价。
6. 部门结果接口权限修正：为 `POST /qt/interview/result` 增加权限与部门校验。
7. 二面晋级轨道字段：列表或结果接口必须明确返回真正晋级的部门，不能只依赖一面 Pass 状态推断。

建议接口形态：

- `GET /qt/interview/admin/applications`
- `GET /qt/interview/admin/applications/{applicationId}`
- `GET /qt/interview/admin/applications/{applicationId}/results`
- `GET /qt/interview/admin/applications/{applicationId}/evaluations`
- `POST /qt/interview/admin/evaluations`
- `PUT /qt/interview/admin/evaluations/{evaluationId}`

### P1：完整业务闭环

8. 招聘看板统计：投递、一面通过、二面通过、确认加入，按申请去重并应用部门范围。
9. 最终录用或淘汰通知发送；必须与二面结果在同一事务中原子保存。
10. 确认加入状态更新。
11. 申请整体状态更新和最终录用部门锁定。
12. 一面、二面名单导出。

建议接口形态：

- `GET /qt/interview/admin/statistics`
- `POST /qt/interview/admin/offers`
- `PUT /qt/interview/admin/applications/{applicationId}/join-status`
- `PUT /qt/interview/admin/applications/{applicationId}/final-status`
- `GET /qt/interview/admin/applications/export`

### P2：配置能力

13. 面试轮次或场次配置。
14. 部门字典和展示名称接口，确保用户部门与志愿部门使用同一编码体系。

实际接口路径可由后端调整，前端只依赖 `src/api/recruitment.js` 暴露的稳定函数。

## 8. 错误和空状态

- 列表请求失败：页面保留重试入口，不展示陈旧成功提示。
- 详情或面评失败：弹窗内显示错误，不影响主表。
- 提交失败：保持用户输入，允许再次提交。
- `401`：沿用全局请求拦截跳转登录。
- `403`：展示明确的权限或部门越权提示。
- 空名单：根据当前 Tab 和部门显示针对性空状态。
- 重复提交：按钮在请求期间禁用，成功后刷新当前轨道和统计。

## 9. 测试与验收

单元与组件测试至少覆盖：

- 一面双志愿状态相互独立，单条 Out 不会阻止另一条 Pass 轨道晋级。
- 两条轨道一面均 Out 后无法进入二面。
- 只有一条轨道 Pass 时，该轨道进入二面。
- 两条轨道均 Pass 时只有第一志愿进入二面，第二志愿保留一面 Pass 且不自动递补。
- 第一志愿 Out、第二志愿 Pass 时第二志愿进入二面。
- 普通管理层只能看到和操作本人部门轨道。
- 修改请求中的部门不能绕过 Mock 后端校验。
- 当前面试官提交后显示“已评”，其他人的面评不被覆盖。
- CEO 专属成员导入、删除和重置密码权限。
- 无权限按钮不渲染，接口返回 `403` 时有正确提示。
- 看板统计按申请去重。
- 路由守卫继续阻止无 `recruitment:list` 权限的用户进入模块。

浏览器验收覆盖招聘看板、一面、二面、简历、编辑面评、Pass/Out 确认、录用通知、部门限制、紧凑字号和控制台零错误。

## 10. 本阶段明确不处理

- 超级管理员 `admin` 的跨部门业务权限策略。
- 后端真实邮件、短信或企业微信发送能力。
- 招聘场次排期和面试官排班。
- 宣讲会、精英分享会及其他业务模块。

这些事项不会阻塞 Mock 可演示版和现有真实接口的适配。
