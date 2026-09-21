# Remove First-Round Interview Time Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完整移除 Web 后台招新管理的一面预约时间功能。

**Architecture:** 从 API、页面编排、弹窗、表格、样式和测试六个位置同时清理该功能，保留其他招新业务及新生端进度展示。删除旧功能文档，避免接口契约继续被误用。

**Tech Stack:** Vue 3、Element Plus、Vitest、Vite

**Spec:** `docs/superpowers/specs/2026-09-21-remove-first-round-interview-time-design.md`

## Global Constraints

- 只修改 `Quanta-admin-web` 中后台一面预约时间相关代码。
- 不修改后端代码。
- 不修改新生端面试进度展示。
- 不影响简历、面评、录用、评分和邮件功能。

---

### Task 1: 移除 API 与数据字段

**Files:**
- Modify: `Quanta-admin-web/src/api/recruitment.js`
- Modify: `Quanta-admin-web/src/api/__tests__/recruitment.test.js`

- [ ] 删除 `firstRoundInterviewTime` 归一化和 `saveFirstRoundInterviewTime` 请求方法。
- [ ] 删除对应接口及字段测试。

### Task 2: 移除弹窗和页面编排

**Files:**
- Modify: `Quanta-admin-web/src/views/recruitment/components/ResumeDialog.vue`
- Modify: `Quanta-admin-web/src/views/recruitment/index.vue`
- Modify: `Quanta-admin-web/src/views/recruitment/__tests__/ResumeDialog.test.js`
- Modify: `Quanta-admin-web/src/views/recruitment/__tests__/recruitment.test.js`

- [ ] 删除预约时间选择、保存按钮、事件和提交状态。
- [ ] 删除页面保存方法、接口导入及相关测试。

### Task 3: 移除表格列与样式

**Files:**
- Modify: `Quanta-admin-web/src/views/recruitment/components/CandidateTable.vue`
- Modify: `Quanta-admin-web/src/views/recruitment/recruitment.css`
- Modify: `Quanta-admin-web/src/views/recruitment/__tests__/CandidateTable.test.js`

- [ ] 删除一面“面试时间”列、格式化逻辑和专用样式。
- [ ] 删除表格预约时间测试数据与断言。

### Task 4: 验证与提交

- [ ] 运行招新相关单元测试。
- [ ] 运行完整测试、构建、lint 和 `git diff --check`。
- [ ] 仅提交本次功能删除涉及的文件并推送当前分支。
