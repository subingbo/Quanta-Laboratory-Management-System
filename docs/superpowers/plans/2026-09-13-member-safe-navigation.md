# Member Safe Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make every member-side header avoid iPhone cutouts and the WeChat menu capsule.

**Architecture:** A pure metrics helper validates device data and calculates pixel dimensions. A single Vue component renders brand and detail headers; member pages provide titles and action slots.

**Tech Stack:** Vue 3, uni-app, WeChat Mini Program, Vitest

**Spec:** `docs/superpowers/specs/2026-09-13-member-safe-navigation-design.md`

## Global Constraints

- Change member pages only; do not alter freshman pages.
- Preserve all existing button behavior and page styling outside the header.
- Use runtime pixel values for system dimensions and retain safe fallbacks.
- Do not add dependencies.

---

### Task 1: Navigation metrics

**Files:**
- Create: `Quanta-uniapp/src/utils/memberNavigation.js`
- Test: `Quanta-uniapp/src/utils/memberNavigation.test.ts`

**Interfaces:**
- Produces: `calculateMemberNavigationMetrics(windowInfo, menuRect)` returning `statusBarHeight`, `navigationHeight`, `totalHeight`, and `capsuleInsetRight`.
- Produces: `getMemberNavigationMetrics()` reading available uni-app APIs with fallback values.

- [ ] Write tests for ordinary iPhone, Dynamic Island, Android, and invalid capsule fallback.
- [ ] Run the focused test and verify it fails before implementation.
- [ ] Implement validation, safe-area handling, and metrics calculation.
- [ ] Run the focused test and verify it passes.
- [ ] Commit the helper and tests.

### Task 2: Shared header component

**Files:**
- Create: `Quanta-uniapp/src/component/MemberSafeHeader.vue`
- Create: `Quanta-uniapp/src/component/member-safe-header.layout.test.ts`

**Interfaces:**
- Consumes: `getMemberNavigationMetrics()` from Task 1.
- Produces: `MemberSafeHeader` props `mode`, `title`, `back`, `titleSize`; named slot `actions`; event `back`.

- [ ] Write static layout tests for dynamic height, capsule reservation, brand mode, detail mode, and centered title.
- [ ] Run the focused test and verify it fails.
- [ ] Implement the component with synchronous metrics and safe fallback.
- [ ] Run the focused test and verify it passes.
- [ ] Commit the component and test.

### Task 3: Migrate member pages

**Files:**
- Modify: `Quanta-uniapp/src/pages/member/home/home.vue`
- Modify: `Quanta-uniapp/src/pages/member/contact/contact.vue`
- Modify: `Quanta-uniapp/src/pages/member/function/function.vue`
- Modify: `Quanta-uniapp/src/pages/member/shirt-order/shirt-order.vue`
- Modify: `Quanta-uniapp/src/pages/member/business-card/view.vue`
- Modify: `Quanta-uniapp/src/pages/member/business-card/edit.vue`
- Modify: `Quanta-uniapp/src/pages/member/security/security.vue`
- Modify: `Quanta-uniapp/src/pages/member/services/services.vue`
- Modify: `Quanta-uniapp/src/pages/member/library/library.vue`
- Modify: `Quanta-uniapp/src/pages/member/workstation/workstation.vue`
- Create: `Quanta-uniapp/src/pages/member/member-navigation.layout.test.ts`

**Interfaces:**
- Consumes: `MemberSafeHeader` from Task 2.

- [ ] Write a static test requiring all scoped pages to use the shared component and forbidding their legacy header markup.
- [ ] Run the focused test and verify it fails.
- [ ] Migrate the three brand headers and preserve their action handlers.
- [ ] Migrate the seven detail headers and preserve their back handlers.
- [ ] Remove only obsolete header padding and styles, keeping body spacing visually equivalent.
- [ ] Run focused and full tests.
- [ ] Verify the mini-program watcher rebuilds and compiled output contains the shared header.
- [ ] Commit the page migration and tests.

### Task 4: Device preview verification

**Files:**
- No source changes expected.

- [ ] Reload the compiled project in WeChat Developer Tools.
- [ ] Confirm build output and source diff checks are clean.
- [ ] Report that freshman pages were intentionally unchanged.
