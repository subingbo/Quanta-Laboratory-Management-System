# Web Activity Signup Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add one unified, view-only activity signup page under the Web admin “塔员端” group for every backend management account.

**Architecture:** Reuse `/qt/activity/list` for activities and `/qt/signup/detailList` for registrants. Add an explicit backend-management authorization check so management roles can read all signups while freshman users remain self-scoped; expose one dynamic-menu component and retire the two legacy signup entries.

**Tech Stack:** Vue 3, Element Plus, Vitest, Spring Boot, Spring Security, JUnit 5, MyBatis, MySQL menu data.

**Spec:** `docs/superpowers/specs/2026-09-18-web-activity-signup-management-design.md`

## Global Constraints

- All backend management identities may view complete signup lists.
- Freshman identities may only read their own signup records.
- The feature is view/export only: no review, edit, delete, or approval controls.
- Keep a single “活动报名” sidebar entry under “塔员端”.

---

### Task 1: Backend management read scope

**Files:**
- Modify: `ruoyi-qt/src/main/java/com/ruoyi/qt/util/QtAuthUtils.java`
- Modify: `ruoyi-qt/src/main/java/com/ruoyi/qt/controller/QtActivitySignupController.java`
- Test: `ruoyi-qt/src/test/java/com/ruoyi/qt/util/QtAuthUtilsTest.java`

**Interfaces:**
- Produces: `QtAuthUtils.isBackofficeUser()` and signup list/detail behavior that returns all rows only for `admin`, `ceo`, `qt_mgmt`, or `qt_manager`.

- [ ] Add failing unit cases proving management roles are recognized and freshman users are not.
- [ ] Run `mvn -pl ruoyi-qt -am -Dtest=QtAuthUtilsTest -Dsurefire.failIfNoSpecifiedTests=false test` and confirm failure.
- [ ] Implement `isBackofficeUser()` and use it when deciding whether `/qt/signup/list`, `/detailList`, and signup detail are self-scoped.
- [ ] Run the same Maven test and confirm it passes.

### Task 2: Unified frontend API and page

**Files:**
- Create: `Quanta-admin-web/src/api/activity-signups.js`
- Create: `Quanta-admin-web/src/views/activity-signups/index.vue`
- Create: `Quanta-admin-web/src/views/activity-signups/activity-signups.css`
- Create: `Quanta-admin-web/src/views/activity-signups/__tests__/activity-signups.test.js`

**Interfaces:**
- Produces: `getActivities(params)` and `getActivitySignups(activityId, params)` returning normalized `{ rows, total }` data.

- [ ] Write failing Vitest cases for activity filters, opening a signup list, rendered registrant fields, empty/error states, and CSV export.
- [ ] Run `npm.cmd test -- src/views/activity-signups/__tests__/activity-signups.test.js` and confirm failure.
- [ ] Implement the API adapter and unified view using Element Plus table, filters, dialog, and existing `downloadCsv`.
- [ ] Re-run the focused test and confirm it passes.

### Task 3: Route and menu consolidation

**Files:**
- Modify: `Quanta-admin-web/src/router/component-map.js`
- Modify: `Quanta-admin-web/src/mock/data/routers.js`
- Modify: `Quanta-admin-web/src/router/__tests__/route-transformer.test.js`
- Modify: `Quanta-admin-web/src/mock/handlers/__tests__/auth.test.js`
- Modify: `sql/lab/lab_patch_admin.sql`
- Create: `sql/lab/lab_patch_activity_signup_menu.sql`

**Interfaces:**
- Consumes: component key `activity-signups/index`.
- Produces: one `/activity-signups` route with title `活动报名`, group `塔员端`, visible to management roles.

- [ ] Update route tests first so they require the new component and reject legacy signup entries.
- [ ] Run the focused router/mock tests and confirm failure.
- [ ] Add the component mapping, replace legacy mock routes, and add idempotent SQL that renames/repoints menu `4018`, hides legacy menu `4005`, and grants the menu to management roles.
- [ ] Re-run the focused tests and confirm they pass.

### Task 4: Full verification

**Files:**
- Modify only files required to correct verification failures caused by this feature.

**Interfaces:**
- Consumes: all preceding tasks.
- Produces: a tested Web build and backend permission behavior.

- [ ] Run `npm.cmd test -- --run`, `npm.cmd run lint`, and `npm.cmd run build` in `Quanta-admin-web`.
- [ ] Run `mvn -pl ruoyi-qt -am -DskipTests=false test` from the repository root.
- [ ] Run `git diff --check` and review the final diff for unrelated changes.
- [ ] Commit the implementation with a focused message.
