# Quanta Recruitment Role Workflow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build distinct management and manager recruitment workflows, role-correct dynamic menus, first-round decision confirmation, and second-round offer notification flow.

**Architecture:** Keep the table role-aware but API-free, let focused dialogs emit semantic events, and keep orchestration in the recruitment page. Extend the RuoYi-style Mock account and handler layer so front-end visibility and back-end authorization enforce the same role and department rules.

**Tech Stack:** Vue 3 Composition API, Element Plus 2.14, Pinia 4, Vue Router 5, JavaScript, Vitest, Vue Test Utils, Vite 8.

**Spec:** `docs/superpowers/specs/2026-08-26-quanta-recruitment-role-workflow-design.md`

## Global Constraints

- Member removal and password reset remain restricted to users with both the permission code and exact `ceo` role.
- Management can view feedback and decide results; managers can edit feedback but cannot decide results or offers.
- Non-CEO recruitment data is restricted to the signed-in user's department.
- First-round decisions only accept `PASS` and `FAIL`; `WAITING` is removed from the UI and Mock contract.
- When both choices pass round one, only the first choice advances.
- Manager menus show dashboard, members, recruitment, learning materials, and book borrows; they hide workstation reservations and clothing orders.
- Existing user-provided root files `apifox接口.openapi.json` and `接口文档.md` must not be modified or committed.

---

### Task 1: Role Accounts, Dynamic Menus, and Server-Side Permissions

**Files:**
- Modify: `src/mock/data/accounts.js`
- Modify: `src/mock/data/routers.js`
- Modify: `src/mock/handlers/auth.js`
- Modify: `src/mock/handlers/recruitment.js`
- Create: `src/mock/handlers/__tests__/auth.test.js`
- Modify: `src/mock/handlers/__tests__/recruitment.test.js`

**Interfaces:**
- Produces: Mock account `product_interviewer` with token `mock-token-product-interviewer` and exact role `manager`.
- Produces: `GET /getRouters` returns only routes whose `meta.permission` is granted.
- Produces: `isManagement(account)` and `isManager(account)` authorization helpers in the recruitment handler.
- Consumes: existing `mockRouters`, `findAccountByToken`, and RuoYi wildcard permission `*:*:*`.

- [ ] **Step 1: Write failing menu and permission tests**

```js
it('returns the confirmed manager menu', () => {
  const response = getRouters.handle(config('mock-token-product-interviewer'))
  expect(response.data.map((route) => route.name)).toEqual([
    'Dashboard', 'Members', 'Recruitment', 'LearningMaterials', 'BookBorrows',
  ])
})

it('lets managers edit feedback but rejects their result decisions', () => {
  const edit = saveEvaluation.handle(config('mock-token-product-interviewer', {
    data: { applicationId: 1001, roundId: 1, department: 'PRODUCT', content: '经理面评' },
  }))
  const decide = saveResult.handle(config('mock-token-product-interviewer', {
    data: { applicationId: 1001, roundId: 1, department: 'PRODUCT', resultStatus: 'PASS' },
  }))
  expect(edit.code).toBe(200)
  expect(decide.code).toBe(403)
})
```

- [ ] **Step 2: Run the focused tests and verify failure**

Run: `npm run test:run -- src/mock/handlers/__tests__/auth.test.js src/mock/handlers/__tests__/recruitment.test.js`

Expected: FAIL because `mock-token-product-interviewer` does not exist and `/getRouters` still returns every route.

- [ ] **Step 3: Add the manager account and correct management permissions**

Add this account shape to `mockAccounts`:

```js
{
  username: 'product_interviewer',
  password: 'quanta123',
  token: 'mock-token-product-interviewer',
  user: {
    userId: 21,
    userName: 'product_interviewer',
    nickName: '产品部经理',
    deptCode: 'PRODUCT',
    dept: { deptId: 101, deptName: '产品部', deptCode: 'PRODUCT' },
    memberDepartment: '产品部',
    memberTitle: '经理',
    memberCohort: '第21届',
  },
  roles: ['manager'],
  permissions: [
    'dashboard:view', 'member:list', 'recruitment:list',
    'recruitment:resume:view', 'recruitment:feedback:view',
    'recruitment:feedback:edit', 'borrow:list', 'material:list',
  ],
}
```

Remove `member:retain` and `recruitment:feedback:edit` from non-CEO management accounts, add `recruitment:offer` to department management accounts, and retain all CEO permissions except edit feedback. Keep CEO member permissions unchanged. This makes non-CEO management fully read-only on both current and historical member cohorts.

Move the `LearningMaterials` route before `BookBorrows` in `mockRouters` so the filtered manager menu matches the confirmed design order.

- [ ] **Step 4: Filter Mock dynamic routes by permission**

```js
function hasPermission(account, permission) {
  return account.permissions.includes('*:*:*') || account.permissions.includes(permission)
}

const routes = mockRouters.filter((route) => hasPermission(account, route.meta.permission))
return { code: 200, msg: '操作成功', data: routes }
```

In recruitment handlers, require exact `manager` for evaluation writes and `management` or `ceo` for decisions/offers. Always call `allowedDepartment` for non-CEO writes.

- [ ] **Step 5: Run focused tests and commit**

Run: `npm run test:run -- src/mock/handlers/__tests__/auth.test.js src/mock/handlers/__tests__/recruitment.test.js src/router/__tests__/guard.test.js`

Expected: PASS.

```bash
git add src/mock/data/accounts.js src/mock/data/routers.js src/mock/handlers/auth.js src/mock/handlers/recruitment.js src/mock/handlers/__tests__/auth.test.js src/mock/handlers/__tests__/recruitment.test.js
git commit -m "feat: add recruitment role permissions"
```

### Task 2: Role-Specific Candidate Table and Compact Styling

**Files:**
- Modify: `src/views/recruitment/components/CandidateTable.vue`
- Modify: `src/views/recruitment/recruitment.css`
- Modify: `src/views/recruitment/__tests__/CandidateTable.test.js`

**Interfaces:**
- Produces events: `resume(row)`, `view-feedback(row)`, `edit-feedback(row)`, and `offer(row)`.
- Consumes exact roles: `management`, `ceo`, `manager` from `useUserStore()`.
- Consumes existing permissions through `PermissionButton`.

- [ ] **Step 1: Add failing role-matrix tests**

Create a `mountTable({ roles, permissions, roundId })` helper and assert:

```js
expect(managementRoundOne.text()).toContain('阅览简历')
expect(managementRoundOne.text()).toContain('查看面评')
expect(managementRoundOne.text()).not.toContain('编辑面评')
expect(managementRoundOne.text()).not.toContain('评定')

expect(managerRoundOne.text()).toContain('阅览简历')
expect(managerRoundOne.text()).toContain('编辑面评')
expect(managerRoundOne.text()).not.toContain('查看面评')

expect(managementRoundTwo.text()).toContain('是否录用')
expect(managementRoundTwo.text()).not.toContain('阅览简历')
expect(managerRoundTwo.text()).toContain('编辑面评')
expect(managerRoundTwo.text()).not.toContain('是否录用')
```

- [ ] **Step 2: Run the CandidateTable test and verify failure**

Run: `npm run test:run -- src/views/recruitment/__tests__/CandidateTable.test.js`

Expected: FAIL because the current table always renders resume, edit feedback, decision, and conditional offer actions.

- [ ] **Step 3: Implement the role matrix**

```js
const isManagement = computed(() => roles.value.includes('management') || roles.value.includes('ceo'))
const isManager = computed(() => roles.value.includes('manager'))
```

Render management round one actions as resume plus `view-feedback`; manager round one as resume plus `edit-feedback`; management round two as only `offer`; manager round two as only `edit-feedback`. Remove the `decision` event and the old `canOffer()` display gate.

- [ ] **Step 4: Apply the compact visual rules**

```css
.candidate-choice__department {
  padding: 2px 6px;
  font-size: 10px;
  line-height: 1.4;
}

.recruitment-table__link.is-underlined.el-button {
  text-decoration: underline;
  text-decoration-color: currentcolor;
  text-underline-offset: 2px;
}
```

Apply `is-underlined` to 阅览简历、查看面评、编辑面评、是否录用.

- [ ] **Step 5: Run the test and commit**

Run: `npm run test:run -- src/views/recruitment/__tests__/CandidateTable.test.js`

Expected: PASS.

```bash
git add src/views/recruitment/components/CandidateTable.vue src/views/recruitment/recruitment.css src/views/recruitment/__tests__/CandidateTable.test.js
git commit -m "feat: render recruitment actions by role"
```

### Task 3: Read-Only Management Feedback and Editable Manager Feedback

**Files:**
- Modify: `src/views/recruitment/components/FeedbackDialog.vue`
- Create: `src/views/recruitment/__tests__/FeedbackDialog.test.js`
- Modify: `src/views/recruitment/index.vue`

**Interfaces:**
- `FeedbackDialog` consumes `mode: 'view' | 'edit'` and existing candidate, department, options, evaluations, content, loading, and submitting props.
- `FeedbackDialog` produces `select-result('PASS' | 'FAIL')` only in view mode and `submit()` only in edit mode.
- Recruitment page produces `openViewFeedback(row)` and `openEditFeedback(row)` orchestration functions.

- [ ] **Step 1: Write failing dialog mode tests**

```js
it('renders read-only feedback with Pass and Out', () => {
  const wrapper = mountDialog({ mode: 'view' })
  expect(wrapper.find('textarea').exists()).toBe(false)
  expect(wrapper.text()).toContain('Pass')
  expect(wrapper.text()).toContain('Out')
  expect(wrapper.text()).not.toContain('Waiting')
})

it('renders editable feedback without result controls', () => {
  const wrapper = mountDialog({ mode: 'edit' })
  expect(wrapper.find('textarea').exists()).toBe(true)
  expect(wrapper.text()).toContain('提交')
  expect(wrapper.text()).not.toContain('Pass')
})
```

- [ ] **Step 2: Run the test and verify failure**

Run: `npm run test:run -- src/views/recruitment/__tests__/FeedbackDialog.test.js`

Expected: FAIL because `mode` and `select-result` do not exist.

- [ ] **Step 3: Implement view/edit branches**

Use `v-if="mode === 'edit'"` for the textarea and submit button. In view mode, render:

```vue
<ElButton type="danger" size="small" @click="emit('select-result', 'FAIL')">Out</ElButton>
<ElButton type="success" size="small" @click="emit('select-result', 'PASS')">Pass</ElButton>
```

Keep the selector enabled only for CEO in view mode; lock it for managers and department management.

- [ ] **Step 4: Split page opening functions**

```js
async function openViewFeedback(row) {
  feedback.mode = 'view'
  await prepareFeedback(row)
}

async function openEditFeedback(row) {
  feedback.mode = 'edit'
  await prepareFeedback(row)
}
```

Only populate `feedback.content` from the signed-in interviewer's evaluation in edit mode. Wire the table's two semantic events to these functions.

- [ ] **Step 5: Run tests and commit**

Run: `npm run test:run -- src/views/recruitment/__tests__/FeedbackDialog.test.js src/views/recruitment/__tests__/recruitment.test.js`

Expected: PASS.

```bash
git add src/views/recruitment/components/FeedbackDialog.vue src/views/recruitment/index.vue src/views/recruitment/__tests__/FeedbackDialog.test.js src/views/recruitment/__tests__/recruitment.test.js
git commit -m "feat: split recruitment feedback modes"
```

### Task 4: First-Round Result Confirmation

**Files:**
- Modify: `src/views/recruitment/components/DecisionDialog.vue`
- Create: `src/views/recruitment/__tests__/DecisionDialog.test.js`
- Modify: `src/views/recruitment/index.vue`
- Modify: `src/mock/handlers/recruitment.js`
- Modify: `src/mock/handlers/__tests__/recruitment.test.js`

**Interfaces:**
- `DecisionDialog` consumes `result: 'PASS' | 'FAIL'`, locked `department`, and `options`.
- `DecisionDialog` produces a parameterless `confirm()` event.
- Recruitment page `selectFirstRoundResult(status)` transitions from feedback to confirmation and `submitDecision()` persists the stored status.

- [ ] **Step 1: Write failing confirmation tests**

```js
expect(wrapper.text()).toContain('一面结果评定')
expect(wrapper.text()).toContain('Pass')
expect(wrapper.find('[aria-label="评定部门"]').attributes('disabled')).toBeDefined()
expect(wrapper.text()).not.toContain('Waiting')
await wrapper.get('[data-test="confirm-decision"]').trigger('click')
expect(wrapper.emitted('confirm')).toHaveLength(1)
```

Add a Mock assertion that `WAITING` returns code 400.

- [ ] **Step 2: Run tests and verify failure**

Run: `npm run test:run -- src/views/recruitment/__tests__/DecisionDialog.test.js src/mock/handlers/__tests__/recruitment.test.js`

Expected: FAIL because the dialog still owns three result buttons and the Mock accepts `WAITING`.

- [ ] **Step 3: Convert DecisionDialog to confirmation-only**

Render the candidate, a result label, disabled `ElSelect`, Cancel, and one Confirm button with `data-test="confirm-decision"`. Remove `canSwitch` and all result-specific footer buttons.

- [ ] **Step 4: Implement page transition and tighten the Mock contract**

```js
function selectFirstRoundResult(status) {
  decision.row = feedback.row
  decision.department = feedback.department
  decision.options = feedback.options.filter((option) => option.value === feedback.department)
  decision.result = status
  feedback.visible = false
  decision.visible = true
}
```

Change Mock validation to `['PASS', 'FAIL'].includes(resultStatus)` and submit `decision.result` only after confirmation.

- [ ] **Step 5: Run tests and commit**

Run: `npm run test:run -- src/views/recruitment/__tests__/DecisionDialog.test.js src/mock/handlers/__tests__/recruitment.test.js src/views/recruitment/__tests__/recruitment.test.js`

Expected: PASS.

```bash
git add src/views/recruitment/components/DecisionDialog.vue src/views/recruitment/index.vue src/mock/handlers/recruitment.js src/views/recruitment/__tests__/DecisionDialog.test.js src/mock/handlers/__tests__/recruitment.test.js
git commit -m "feat: confirm first round decisions"
```

### Task 5: Second-Round Confirmation and Atomic Offer Notification

**Files:**
- Create: `src/views/recruitment/components/OfferConfirmDialog.vue`
- Modify: `src/views/recruitment/components/OfferDialog.vue`
- Create: `src/views/recruitment/__tests__/OfferFlow.test.js`
- Modify: `src/views/recruitment/index.vue`
- Modify: `src/mock/handlers/recruitment.js`
- Modify: `src/mock/handlers/__tests__/recruitment.test.js`
- Modify: `src/api/__tests__/recruitment.test.js`

**Interfaces:**
- `OfferConfirmDialog` consumes candidate name, locked department, and one selected option; produces `confirm()`.
- `OfferDialog` consumes locked `department`, `options`, `decision`, and editable `content`; produces decision/content updates and `submit()`.
- `POST /qt/interview/admin/offers` consumes `{ applicationId, department, decision: 'PASS' | 'FAIL', content }` and atomically updates round-two status plus application status in Mock mode.

- [ ] **Step 1: Write failing two-step dialog tests**

```js
it('confirms before opening the notification editor', async () => {
  await openOfferFor(candidate)
  expect(wrapper.text()).toContain('二面结果评定')
  expect(wrapper.text()).not.toContain('通知内容')
  await wrapper.get('[data-test="confirm-offer-step"]').trigger('click')
  expect(wrapper.text()).toContain('通知内容')
})

it('switches notification copy for Pass and Out', async () => {
  await wrapper.get('[data-test="offer-out"]').trigger('click')
  expect(wrapper.find('textarea').element.value).toContain('很遗憾')
  await wrapper.get('[data-test="offer-pass"]').trigger('click')
  expect(wrapper.find('textarea').element.value).toContain('祝贺')
})
```

Add a handler test asserting a department management token can submit its own department offer, cannot submit another department, and an Out submission sets both `track.rounds[2].status === 'FAIL'` and `applicationStatus === 'REJECTED'`.

- [ ] **Step 2: Run tests and verify failure**

Run: `npm run test:run -- src/views/recruitment/__tests__/OfferFlow.test.js src/mock/handlers/__tests__/recruitment.test.js src/api/__tests__/recruitment.test.js`

Expected: FAIL because no confirmation component exists and offer submission is CEO-only and does not update round-two status.

- [ ] **Step 3: Implement OfferConfirmDialog and locked choice selectors**

The confirmation dialog uses width 520, title `二面结果评定`, a disabled `ElSelect`, Cancel, and Confirm with `data-test="confirm-offer-step"`. Replace the offer department input with a disabled `ElSelect` whose only option is the selected `第一志愿/第二志愿 · 部门` label.

- [ ] **Step 4: Implement the two-stage page state**

```js
function openOffer(row) {
  const options = trackOptions(row, 2)
  offer.row = row
  offer.options = options
  offer.department = options[0]?.value || ''
  offerConfirm.visible = true
}

function continueOffer() {
  offerConfirm.visible = false
  offer.decision = 'PASS'
  offer.content = offerTemplate(offer.row, 'PASS')
  offer.visible = true
}
```

- [ ] **Step 5: Make Mock offer submission atomic and department-scoped**

After role, permission, application, department, advanced-track, decision, and non-empty content checks:

```js
track.rounds[2].status = decision
recomputeAdvancement(application)
return {
  code: 200,
  msg: decision === 'PASS' ? '录用通知已发送' : '淘汰通知已发送',
  data: application,
}
```

- [ ] **Step 6: Run tests and commit**

Run: `npm run test:run -- src/views/recruitment/__tests__/OfferFlow.test.js src/mock/handlers/__tests__/recruitment.test.js src/api/__tests__/recruitment.test.js src/views/recruitment/__tests__/recruitment.test.js`

Expected: PASS.

```bash
git add src/views/recruitment/components/OfferConfirmDialog.vue src/views/recruitment/components/OfferDialog.vue src/views/recruitment/index.vue src/mock/handlers/recruitment.js src/views/recruitment/__tests__/OfferFlow.test.js src/mock/handlers/__tests__/recruitment.test.js src/api/__tests__/recruitment.test.js
git commit -m "feat: add second round offer workflow"
```

### Task 6: Documentation, Regression Tests, and Browser Acceptance

**Files:**
- Modify: `README.md`
- Modify: `docs/superpowers/specs/2026-08-25-quanta-recruitment-management-design.md`
- Verify: all modified source and test files from Tasks 1-5.

**Interfaces:**
- Documents Mock credentials and the missing atomic back-end offer-result contract.
- Produces browser screenshots for management first round, management second-round confirmation/notification, manager first round, and manager second round.

- [ ] **Step 1: Update user-facing development documentation**

Add `product_interviewer / quanta123` to the Mock account table. Describe the management/manager split and record that `/qt/interview/admin/offers` is a provisional Mock contract that must atomically save the second-round result and notification.

- [ ] **Step 2: Run all automated verification**

Run:

```bash
npm run test:run
npm run lint
npm run build
```

Expected: all tests pass, ESLint exits 0, and Vite completes a production build.

- [ ] **Step 3: Verify management in a browser**

Log in as `product_manager / quanta123` and confirm:

1. Members are read-only.
2. First round shows underlined 阅览简历 and 查看面评, with no standalone 评定.
3. 查看面评 is read-only and Pass/Out transitions to a locked confirmation.
4. Second round shows only 是否录用.
5. 是否录用 transitions from confirmation to Pass/Out notification editing.

- [ ] **Step 4: Verify manager in a browser**

Log in as `product_interviewer / quanta123` and confirm:

1. Sidebar shows 控制台、成员管理、招新管理、学习资料、图书借阅.
2. Sidebar omits 工位预约 and 塔服订购.
3. First round shows underlined 阅览简历 and 编辑面评.
4. Second round shows only underlined 编辑面评.
5. No decision or offer action is present.

- [ ] **Step 5: Check computed visual styles and console**

Assert department pill padding is `2px 6px`, specified action links have non-`none` text decoration, locked selectors are disabled, and the browser console reports zero errors and warnings.

- [ ] **Step 6: Commit documentation and final fixes**

```bash
git add README.md docs/superpowers/specs/2026-08-25-quanta-recruitment-management-design.md
git commit -m "docs: document recruitment role workflows"
```
