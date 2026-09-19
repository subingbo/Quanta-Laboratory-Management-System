# First-Round Interview Time Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a frontend-only workflow for selecting one shared first-round interview time per candidate and displaying it in the first-round recruitment table.

**Architecture:** Normalize all supported backend response shapes into `firstRoundInterviewTime` in the recruitment API module. Keep the picker inside `ResumeDialog`, emit a save event to the recruitment page, and let the page call one isolated API function before refreshing the list. `CandidateTable` remains display-only and conditionally adds the column for round one.

**Tech Stack:** Vue 3 Composition API, Element Plus, Vitest, Vue Test Utils, Vite

**Spec:** `docs/superpowers/specs/2026-09-19-first-round-interview-time-design.md`

## Global Constraints

- Only the Web admin frontend is modified; no backend source changes.
- One first-round time is shared by both department choices.
- Available slots are exactly `2026-09-22 18:30:00` and `2026-09-23 18:30:00`; both display through 22:30.
- Round two has no picker and no interview-time table column.
- A failed request must not be presented as a successful save.

---

### Task 1: Recruitment API contract and normalization

**Files:**
- Modify: `Quanta-admin-web/src/api/recruitment.js`
- Test: `Quanta-admin-web/src/api/__tests__/recruitment.test.js`

**Interfaces:**
- Produces: `firstRoundInterviewTime: string` on every mapped application.
- Produces: `saveFirstRoundInterviewTime(applicationId: number|string, interviewTime: string): Promise<object>`.

- [ ] **Step 1: Write failing mapping and request-contract tests**

Add assertions that `mapApplication()` accepts both `firstRoundInterviewTime` and legacy `interviewTime`, and add a mocked request assertion for:

```js
saveFirstRoundInterviewTime(8, '2026-09-22 18:30:00')
// PUT /qt/interview/admin/applications/8/rounds/1/interview-time
// body: { interviewTime: '2026-09-22 18:30:00' }
```

- [ ] **Step 2: Run the focused API test and verify failure**

Run: `npm run test:run -- src/api/__tests__/recruitment.test.js`

Expected: FAIL because the normalized field and save function do not exist.

- [ ] **Step 3: Implement the normalized field and save function**

Use the response precedence:

```js
firstRoundInterviewTime:
  application.firstRoundInterviewTime ||
  application.interviewTime ||
  application.tracks?.find((track) => track.rounds?.[1]?.interviewTime)?.rounds?.[1]?.interviewTime ||
  ''
```

Add:

```js
export function saveFirstRoundInterviewTime(applicationId, interviewTime) {
  return request({
    url: `/qt/interview/admin/applications/${applicationId}/rounds/1/interview-time`,
    method: 'put',
    data: { interviewTime },
  })
}
```

- [ ] **Step 4: Re-run the focused API test**

Run: `npm run test:run -- src/api/__tests__/recruitment.test.js`

Expected: PASS.

### Task 2: First-round picker and table presentation

**Files:**
- Modify: `Quanta-admin-web/src/views/recruitment/components/ResumeDialog.vue`
- Modify: `Quanta-admin-web/src/views/recruitment/components/CandidateTable.vue`
- Modify: `Quanta-admin-web/src/views/recruitment/recruitment.css`
- Test: `Quanta-admin-web/src/views/recruitment/__tests__/ResumeDialog.test.js`
- Test: `Quanta-admin-web/src/views/recruitment/__tests__/CandidateTable.test.js`

**Interfaces:**
- `ResumeDialog` consumes `roundId`, `submitting`, and `application.firstRoundInterviewTime`.
- `ResumeDialog` emits `save-interview-time` with the selected backend timestamp.
- `CandidateTable` consumes `row.firstRoundInterviewTime` and `roundId`.

- [ ] **Step 1: Write failing component tests**

Cover these exact behaviours:

```js
expect(firstRoundWrapper.find('[data-test="interview-time-picker"]').exists()).toBe(true)
expect(secondRoundWrapper.find('[data-test="interview-time-picker"]').exists()).toBe(false)
expect(wrapper.emitted('save-interview-time')).toEqual([['2026-09-22 18:30:00']])
expect(firstRoundTable.text()).toContain('09月22日 18:30–22:30')
expect(secondRoundTable.text()).not.toContain('面试时间')
```

- [ ] **Step 2: Run focused component tests and verify failure**

Run: `npm run test:run -- src/views/recruitment/__tests__/ResumeDialog.test.js src/views/recruitment/__tests__/CandidateTable.test.js`

Expected: FAIL because the picker, event, and column do not exist.

- [ ] **Step 3: Implement the picker**

In `ResumeDialog.vue`, define two constant slot objects with backend values and user-facing labels, synchronize a local selected value whenever the dialog/application changes, and render an `ElRadioGroup` only when `roundId === 1`. Disable the save button until a slot is selected and while `submitting` is true.

- [ ] **Step 4: Implement first-round-only table formatting**

Add a conditional column in `CandidateTable.vue`. Convert supported timestamp forms beginning with `2026-09-22` or `2026-09-23` to the corresponding full label. Show `待安排` for empty values and retain the original value as a safe fallback for an unknown timestamp.

- [ ] **Step 5: Add scoped visual styles**

Style the picker as two responsive cards using the existing orange accent. Keep desktop table width usable and stack the radio cards on narrow screens without changing other dialogs.

- [ ] **Step 6: Re-run focused component tests**

Run: `npm run test:run -- src/views/recruitment/__tests__/ResumeDialog.test.js src/views/recruitment/__tests__/CandidateTable.test.js`

Expected: PASS.

### Task 3: Page orchestration and regression validation

**Files:**
- Modify: `Quanta-admin-web/src/views/recruitment/index.vue`
- Test: `Quanta-admin-web/src/views/recruitment/__tests__/recruitment.test.js`

**Interfaces:**
- Consumes: `saveFirstRoundInterviewTime(applicationId, interviewTime)` from Task 1.
- Consumes: `save-interview-time` from `ResumeDialog` in Task 2.

- [ ] **Step 1: Add the page save-state test**

Assert that opening a resume passes `roundId=1`, submitting calls the API with the current application ID and selected timestamp, success closes the dialog and refreshes the list, and failure leaves the dialog open.

- [ ] **Step 2: Run the page test and verify failure**

Run: `npm run test:run -- src/views/recruitment/__tests__/recruitment.test.js`

Expected: FAIL because page-level save orchestration is absent.

- [ ] **Step 3: Implement page save orchestration**

Add `submitting` to the `resume` reactive state, pass `roundId` and `submitting` to `ResumeDialog`, and handle `@save-interview-time`. On success show `面试时间已保存`, close the dialog, and call `loadList()`. On error use `error.message || '面试时间保存失败，请稍后重试'` and keep the dialog open.

- [ ] **Step 4: Run focused tests**

Run all API and recruitment component/page tests changed by this plan.

Expected: PASS.

- [ ] **Step 5: Run the complete frontend checks**

Run:

```text
npm run test:run
npm run build
npm run lint
```

Expected: all tests and build pass; lint has no new errors or warnings.

- [ ] **Step 6: Review the final diff**

Run `git diff --check` and inspect only the files listed in this plan. Confirm no backend files or unrelated untracked artifacts are included.
