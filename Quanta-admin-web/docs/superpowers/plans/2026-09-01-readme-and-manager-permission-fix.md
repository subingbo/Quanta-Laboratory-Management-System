# README and Manager Permission Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restrict manager feedback editing to recruitment round two and bring the project README in line with the current implementation and backend-integration status.

**Architecture:** Keep the existing role, permission-button, Mock-handler, and dynamic-route architecture unchanged. Tighten only the round-aware rendering condition in `CandidateTable.vue`, lock it with component tests, then replace stale README claims using the existing backend open-items document as the single source for unresolved contracts.

**Tech Stack:** Vue 3, Pinia, Element Plus, Vitest, Vue Test Utils, ESLint, Vite

**Spec:** `docs/superpowers/specs/2026-09-01-readme-and-manager-permission-fix-design.md`

## Global Constraints

- Keep `VITE_USE_MOCK=true` in development.
- Managers may edit feedback only for their department in round two.
- Managers may view a department candidate's resume in round one, but may not view feedback, edit feedback, decide Pass/Out, or send offers there.
- Management and CEO recruitment behavior remains unchanged.
- Do not change unconfirmed backend request contracts.
- Do not modify user-owned untracked interface documents or Word files.

---

### Task 1: Lock the Manager Round-One Permission Rule with Tests

**Files:**
- Modify: `src/views/recruitment/__tests__/CandidateTable.test.js`

**Interfaces:**
- Consumes: `CandidateTable` props `{ rows, roundId }` and Pinia user state `{ roles, permissions }`.
- Produces: regression coverage requiring round-one managers to see only `阅览简历` and round-two managers to see `编辑面评`.

- [ ] **Step 1: Change the manager round-one expectation to the confirmed rule**

Replace the existing manager first-round assertion with:

```js
it('keeps manager first-round actions read-only', () => {
  const wrapper = mountTable({
    roles: ['qt_manager'],
    permissions: ['qt:interview:admin:list', 'qt:interview:admin:evaluate'],
  })

  expect(wrapper.text()).toContain('阅览简历')
  expect(wrapper.text()).not.toContain('编辑面评')
  expect(wrapper.text()).not.toContain('已评')
  expect(wrapper.text()).not.toContain('查看面评')
  expect(wrapper.text()).not.toContain('是否录用')
})
```

- [ ] **Step 2: Add an evaluated manager candidate fixture for the round-two marker**

In the round-two manager test, clone the candidate and add an evaluation owned by user `21`:

```js
const evaluatedCandidate = structuredClone(candidate)
evaluatedCandidate.choices[0].rounds[2].evaluations = [
  { interviewerId: 21, content: '二面面评' },
]
```

Allow `mountTable` to accept `rows`, pass `[evaluatedCandidate]`, and assert both `编辑面评` and `已评` are present in round two.

- [ ] **Step 3: Run the focused test and verify it fails before implementation**

Run:

```bash
npx vitest run src/views/recruitment/__tests__/CandidateTable.test.js --maxWorkers=1
```

Expected: the manager first-round test fails because `CandidateTable.vue` still renders `编辑面评` when `roundId === 1`.

---

### Task 2: Restrict Manager Feedback UI to Round Two

**Files:**
- Modify: `src/views/recruitment/components/CandidateTable.vue`
- Test: `src/views/recruitment/__tests__/CandidateTable.test.js`

**Interfaces:**
- Consumes: numeric `roundId`, `isManager`, department-filtered `availableTracks(row)`, and `hasOwnEvaluation(row)`.
- Produces: round-aware manager edit and reviewed-state visibility without altering emitted event names or backend request shapes.

- [ ] **Step 1: Tighten the edit-feedback visibility condition**

Change the edit button condition to:

```vue
v-if="roundId === 2 && isManager && availableTracks(row).length"
```

- [ ] **Step 2: Tighten the reviewed marker visibility condition**

Change the marker condition to:

```vue
v-if="roundId === 2 && isManager && hasOwnEvaluation(row)"
```

- [ ] **Step 3: Run the focused test and verify it passes**

Run:

```bash
npx vitest run src/views/recruitment/__tests__/CandidateTable.test.js --maxWorkers=1
```

Expected: all `CandidateTable` tests pass.

---

### Task 3: Replace Stale README Integration Claims

**Files:**
- Modify: `README.md`
- Reference: `docs/backend-integration-open-items.md`

**Interfaces:**
- Consumes: current source routes, Mock accounts, API modules, confirmed permission names, and backend open items.
- Produces: an onboarding document that distinguishes implemented frontend behavior, Mock-only verification, and unresolved backend contracts.

- [ ] **Step 1: Update the implemented-scope introduction and project tree**

List the active pages: login, dashboard, members, recruitment, lecture signups, sharing signups, workstation records, book-borrow records, clothing orders, learning materials, and error pages. State that route guards, dynamic routes, department scope, and button permissions are implemented.

- [ ] **Step 2: Correct account and role descriptions**

Document these exact rules:

```text
CEO: full management flow, cross-department recruitment, member import/delete/reset.
qt_mgmt: department-scoped recruitment decisions and read-only member list.
qt_manager: department-scoped round-one resume access and round-two feedback editing only.
admin: current Mock treats admin as the CEO-equivalent super administrator.
viewer: read-only pages allowed by its Mock routes and permissions.
```

- [ ] **Step 3: Correct interface and permission documentation**

Describe current frontend modules without claiming now-implemented backend endpoints are missing. Use confirmed permissions `qt:material:list`, `qt:material:add`, `qt:material:remove`, `system:order:list`, and `system:order:approve`. Link all uncertain contracts to `docs/backend-integration-open-items.md` rather than duplicating outdated gaps.

- [ ] **Step 4: Add a clear integration-status section**

State that development still runs with Mock enabled, real-backend end-to-end verification has not occurred, and the remaining confirmation categories are reset password, first-round result endpoint, round-specific choice status, dashboard statistics, material list permission, download headers, database/test accounts, annual rollover, and backend host/CORS.

- [ ] **Step 5: Check README for stale phrases**

Run:

```bash
rg -n "一面/二面编辑面评|后端尚未提供|接口尚缺失|material:write|order:confirm|高风险业务按钮不默认开放" README.md
```

Expected: no matches.

---

### Task 4: Complete Project Verification

**Files:**
- Verify only; no source file is expected to change.

**Interfaces:**
- Consumes: the completed component, tests, and README.
- Produces: evidence that the targeted fix did not regress the application.

- [ ] **Step 1: Run the complete unit-test suite**

Run:

```bash
npm run test:run
```

Expected: all test files and tests pass.

- [ ] **Step 2: Run ESLint**

Run:

```bash
npm run lint
```

Expected: exit code 0 with no lint errors.

- [ ] **Step 3: Run the production build**

Run:

```bash
npm run build
```

Expected: Vite completes the production build successfully.

- [ ] **Step 4: Review repository scope**

Run:

```bash
git status --short
git diff --check
```

Expected: only the planned tracked files are modified; existing user-owned untracked documents remain untouched; `git diff --check` reports no whitespace errors.

- [ ] **Step 5: Report the remaining backend confirmations**

Return the nine current items from `docs/backend-integration-open-items.md`, grouped by P0, P1, and P2, and distinguish confirmed frontend fixes from work blocked on backend answers.

