# Lecture Signups and Book Borrow Records Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the lecture signup list with CSV export, the read-only book borrow list, and the recruitment export tooltip.

**Architecture:** Follow the existing API → RuoYi-style Mock handler → dynamic route component → compact Element Plus table pattern. Keep each business page independent; use a focused CSV utility for the lecture export.

**Tech Stack:** Vue 3, Element Plus, JavaScript, Vite, Vitest

**Spec:** `docs/superpowers/specs/2026-08-26-lecture-book-records-design.md`

## Global Constraints

- Preserve dynamic route permissions `signup:list` and `borrow:list`.
- Lecture export downloads the currently loaded rows as UTF-8 CSV.
- Book records are read-only and use `GET /system/borrow/detailList` without `userId`.
- Tooltip text must be exactly `导出的内容是当前页面筛选结果`.

---

### Task 1: API and Mock contracts

**Files:**
- Create: `src/api/lecture-signups.js`, `src/api/book-borrows.js`
- Create: `src/mock/data/lecture-signups.js`, `src/mock/data/book-borrows.js`
- Create: `src/mock/handlers/lecture-signups.js`, `src/mock/handlers/book-borrows.js`
- Modify: `src/mock/index.js`
- Test: `src/api/__tests__/lecture-book-records.test.js`, `src/mock/handlers/__tests__/lecture-book-records.test.js`

**Interfaces:**
- Produces: `getLectureRegistrations(): Promise<{rows,total,quota}>`
- Produces: `getBookBorrowRecords(params?): Promise<{rows,total}>`

- [ ] Write failing API and permission tests for management access and missing permission rejection.
- [ ] Run the focused tests and confirm failure.
- [ ] Add Mock records matching the designs and RuoYi response handlers.
- [ ] Run focused tests and confirm pass.

### Task 2: Dynamic route pages

**Files:**
- Create: `src/views/lecture-signups/index.vue`, `src/views/lecture-signups/lecture-signups.css`
- Create: `src/views/book-borrows/index.vue`, `src/views/book-borrows/book-borrows.css`
- Create: `src/utils/csv.js`
- Modify: `src/mock/data/routers.js`, `src/router/component-map.js`
- Test: `src/views/lecture-signups/__tests__/lecture-signups.test.js`, `src/views/book-borrows/__tests__/book-borrows.test.js`, `src/router/__tests__/route-transformer.test.js`

**Interfaces:**
- Consumes: Task 1 API functions.
- Produces: `downloadCsv(filename, columns, rows)` and two lazy route components.

- [ ] Write failing rendering, CSV and component-map tests.
- [ ] Implement the compact lecture table, `10 / 50` badge, error retry and CSV export.
- [ ] Implement the compact book table, type/status tags, date mapping and overdue row class.
- [ ] Replace both placeholder component keys and run focused tests.

### Task 3: Recruitment export tooltip

**Files:**
- Modify: `src/views/recruitment/index.vue`
- Test: `src/views/recruitment/__tests__/recruitment.test.js`

**Interfaces:**
- Wraps the existing permission button without changing `exportList()`.

- [ ] Add a failing assertion for the exact tooltip content.
- [ ] Wrap the non-board export control in `ElTooltip` with top placement.
- [ ] Run recruitment tests and confirm pass.

### Task 4: Documentation and verification

**Files:**
- Modify: `README.md`

- [ ] Record the temporary lecture endpoint and real book endpoint.
- [ ] Run `npm run test:run`, expecting all tests to pass.
- [ ] Run `npm run lint`, expecting no errors.
- [ ] Run `npm run build`, expecting a successful production build.
- [ ] Browser-check both pages, CSV download, overdue styling, and the tooltip.
- [ ] Commit only project changes; leave user-provided API documents untouched.
