# Freshman Application Deadline Notice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Display a responsive, non-dismissible deadline reminder at the top of the freshman application form.

**Architecture:** Keep the reminder as static semantic markup inside `ApplicationForm.vue`, before all form fields. Add styles to the existing freshman recruitment stylesheet and a focused component test; do not add time calculations or alter submit behavior.

**Tech Stack:** Vue 3, CSS, Vitest, Vue Test Utils, Vite

**Spec:** `docs/superpowers/specs/2026-09-20-freshman-application-deadline-notice-design.md`

## Global Constraints

- Display `2026年9月21日 24:00` exactly.
- Explain that saving a draft does not complete registration.
- Do not disable submission based on browser time.
- Do not change validation, draft saving, uploading, or submission behavior.
- Keep desktop and mobile layouts responsive.

---

### Task 1: Deadline notice component markup and regression test

**Files:**
- Modify: `Quanta-admin-web/src/views/freshman/recruitment/components/ApplicationForm.vue`
- Create: `Quanta-admin-web/src/views/freshman/recruitment/components/__tests__/ApplicationForm.test.js`

**Interfaces:**
- Produces: `[data-testid="application-deadline-notice"]` inside the form before the first field.

- [ ] **Step 1: Write the failing component test**

Mount `ApplicationForm` and assert:

```js
const notice = wrapper.get('[data-testid="application-deadline-notice"]')
expect(notice.text()).toContain('2026年9月21日 24:00')
expect(notice.text()).toContain('保存草稿不代表报名成功')
expect(notice.element.compareDocumentPosition(wrapper.get('[name="realName"]').element))
  .toBe(Node.DOCUMENT_POSITION_FOLLOWING)
```

- [ ] **Step 2: Run the test and verify failure**

Run: `npm run test:run -- src/views/freshman/recruitment/components/__tests__/ApplicationForm.test.js`

Expected: FAIL because the notice does not exist.

- [ ] **Step 3: Add semantic notice markup**

Add a full-width `<aside>` as the first form child. Include an accessible clock icon, a strong deadline line, and the explanatory line. The notice has no close button and emits no events.

- [ ] **Step 4: Re-run the focused test**

Run the same test command.

Expected: PASS.

### Task 2: Responsive styling and complete validation

**Files:**
- Modify: `Quanta-admin-web/src/views/freshman/recruitment/recruitment.css`

**Interfaces:**
- Consumes: `.application-form__deadline` markup from Task 1.

- [ ] **Step 1: Add desktop styles**

Make the notice span both grid columns, use the existing portal orange tokens, and arrange the icon and copy horizontally with a subtle border and background.

- [ ] **Step 2: Add narrow-screen styles**

At the existing mobile breakpoint, reduce padding and align the notice to the top so long Chinese text wraps naturally without horizontal scrolling.

- [ ] **Step 3: Run full checks**

Run:

```text
npm run test:run
npm run build
npm run lint
```

Expected: all tests and build pass; lint has no new warnings beyond the existing `NoticeDialog.vue` `vue/no-v-html` warning.

- [ ] **Step 4: Review the diff**

Run `git diff --check` and confirm no backend code, build output, screenshots, or local tool directories are included.
