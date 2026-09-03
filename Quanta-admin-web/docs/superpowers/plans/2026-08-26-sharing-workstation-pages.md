# Sharing and Workstation Pages Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the existing sharing-signup and workstation placeholders with tested read-only pages and apply the approved recruitment dialog polish.

**Architecture:** Add one API adapter, Mock dataset, Mock handler, view, stylesheet, and test boundary per business page. Reuse dynamic routing and the shared request layer; keep date/status formatting local to the workstation view and limit recruitment edits to copy and CSS tokens.

**Tech Stack:** Vue 3, Element Plus, JavaScript, Vite, Vitest, Vue Test Utils, Pinia, existing Mock adapter

**Spec:** `docs/superpowers/specs/2026-08-26-sharing-workstation-pages-design.md`

## Global Constraints

- Both business pages are read-only and expose no search, export, write, approval, or cancellation actions.
- Sharing count is `10 / 50`; workstation data comes from `GET /system/reservation/detailList` without `userId`.
- Management receives `activity:list` and `reservation:list`; manager keeps neither permission.
- Recruitment Pass/Out font size is `14px`; active underline is `44px × 3px` and `#FF6600`.
- Add no dependencies and preserve current dynamic route guard behavior.

---

### Task 1: API and Mock contracts

**Files:**
- Create: `src/api/sharing-signups.js`
- Create: `src/api/workstations.js`
- Create: `src/api/__tests__/business-records.test.js`
- Create: `src/mock/data/sharing-signups.js`
- Create: `src/mock/data/workstations.js`
- Create: `src/mock/handlers/sharing-signups.js`
- Create: `src/mock/handlers/workstations.js`
- Create: `src/mock/handlers/__tests__/business-records.test.js`
- Modify: `src/mock/index.js`

**Interfaces:**
- Produces: `getSharingRegistrations(): Promise<{ rows, total, quota }>` and `getReservationRecords(params?): Promise<{ rows, total }>`.
- Mock endpoints: `GET /qt/activity/sharing/registrations` and `GET /system/reservation/detailList`.

- [ ] **Step 1: Write failing API and handler tests**

```js
expect(await getSharingRegistrations()).toMatchObject({ total: 10, quota: 50 })
expect((await getReservationRecords()).rows[0]).toHaveProperty('workstationCode')
```

- [ ] **Step 2: Run the tests and verify missing modules fail**

Run: `npm run test:run -- src/api/__tests__/business-records.test.js src/mock/handlers/__tests__/business-records.test.js`

- [ ] **Step 3: Implement adapters, ten sharing rows, eight reservation rows, authenticated permission-aware handlers, and register handlers**

```js
export async function getSharingRegistrations() {
  const payload = await request({ url: '/qt/activity/sharing/registrations', method: 'get' })
  return { rows: payload.rows || [], total: Number(payload.total || 0), quota: Number(payload.quota || 0) }
}
```

- [ ] **Step 4: Run focused tests**

Run: `npm run test:run -- src/api/__tests__/business-records.test.js src/mock/handlers/__tests__/business-records.test.js`

Expected: PASS.

### Task 2: Dynamic routes and permissions

**Files:**
- Modify: `src/mock/data/routers.js`
- Modify: `src/mock/data/accounts.js`
- Modify: `src/router/component-map.js`
- Modify: `src/router/__tests__/route-transformer.test.js`

**Interfaces:**
- Produces registered keys `sharing-signups/index` and `workstations/index` and management visibility through existing permission filtering.

- [ ] **Step 1: Extend route tests with both component keys**

```js
expect(isKnownComponent('sharing-signups/index')).toBe(true)
expect(isKnownComponent('workstations/index')).toBe(true)
```

- [ ] **Step 2: Run and verify failure**

Run: `npm run test:run -- src/router/__tests__/route-transformer.test.js`

- [ ] **Step 3: Map both route keys and add the two permissions to product/design management accounts only**

```js
'sharing-signups/index': () => import('@/views/sharing-signups/index.vue'),
'workstations/index': () => import('@/views/workstations/index.vue'),
```

- [ ] **Step 4: Run route and guard tests**

Run: `npm run test:run -- src/router/__tests__`

Expected: PASS.

### Task 3: Read-only sharing and workstation views

**Files:**
- Create: `src/views/sharing-signups/index.vue`
- Create: `src/views/sharing-signups/sharing-signups.css`
- Create: `src/views/sharing-signups/__tests__/sharing-signups.test.js`
- Create: `src/views/workstations/index.vue`
- Create: `src/views/workstations/workstations.css`
- Create: `src/views/workstations/__tests__/workstations.test.js`

**Interfaces:**
- Consumes Task 1 API adapters.
- Produces two route-level pages with loading, empty, failure/retry, compact tables, count badge, date/time and status formatting.

- [ ] **Step 1: Write failing page tests**

```js
expect(wrapper.text()).toContain('精英分享会报名名单')
expect(wrapper.text()).toContain('10 / 50')
expect(wrapper.text()).toContain('工位预约记录')
expect(wrapper.text()).toContain('已预约')
```

- [ ] **Step 2: Run and verify missing component failures**

Run: `npm run test:run -- src/views/sharing-signups/__tests__/sharing-signups.test.js src/views/workstations/__tests__/workstations.test.js`

- [ ] **Step 3: Implement compact Element Plus table pages and deterministic formatting helpers**

```js
function statusMeta(status) {
  return ({ PENDING: ['待审核', 'warning'], APPROVED: ['已预约', 'warning'], CANCELED: ['已取消', 'info'], FINISHED: ['已完成', 'success'] })[status] || [status || '-', 'info']
}
```

- [ ] **Step 4: Run page tests**

Run: `npm run test:run -- src/views/sharing-signups/__tests__/sharing-signups.test.js src/views/workstations/__tests__/workstations.test.js`

Expected: PASS.

### Task 4: Recruitment micro-adjustments

**Files:**
- Modify: `src/views/recruitment/components/OfferConfirmDialog.vue`
- Modify: `src/views/recruitment/recruitment.css`
- Modify: `src/views/recruitment/__tests__/OfferFlow.test.js`

**Interfaces:**
- Preserves all existing recruitment events and props.
- Produces exact updated copy and design tokens.

- [ ] **Step 1: Update the confirmation copy test**

```js
expect(wrapper.text()).toContain('确定向该同学(陈思思)发送二面是否通过的录用通知？')
```

- [ ] **Step 2: Update component copy and set result font to 14px plus centered 44px underline**

```css
.feedback-dialog__result.el-button, .offer-dialog__decision { font-size: 14px; }
.offer-dialog__tab::after { width: 44px; height: 3px; background: #ff6600; }
```

- [ ] **Step 3: Run recruitment tests**

Run: `npm run test:run -- src/views/recruitment/__tests__/FeedbackDialog.test.js src/views/recruitment/__tests__/OfferFlow.test.js`

Expected: PASS.

### Task 5: Complete verification

**Files:**
- Verify: all files above
- Modify: `README.md` with temporary sharing endpoint note

**Interfaces:**
- Produces verified, documented pages ready for backend replacement.

- [ ] **Step 1: Run all tests**

Run: `npm run test:run`

- [ ] **Step 2: Run lint and production build**

Run: `npm run lint` and `npm run build`

- [ ] **Step 3: Browser-check management navigation, both pages, recruitment copy/buttons/underline, and console errors**

- [ ] **Step 4: Commit implementation**

```bash
git add src README.md docs/superpowers/plans/2026-08-26-sharing-workstation-pages.md
git commit -m "feat: add sharing and workstation record pages"
```
