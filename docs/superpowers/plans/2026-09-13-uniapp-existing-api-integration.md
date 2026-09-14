# Uni-app Existing API Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace every mock-backed Quanta uni-app operation that already has a usable backend endpoint with real authenticated requests while retaining mocks only for documented endpoint gaps.

**Architecture:** Add domain-focused API modules that own transport types, pure response mappers, and network calls. Existing pages consume their current view models from these modules, so layout stays stable while the server becomes authoritative. Missing capabilities remain in the existing mock modules and are imported only at the exact unsupported interaction.

**Tech Stack:** Vue 3, uni-app, TypeScript, `uni.request`, `uni.uploadFile`, Vitest, Spring Boot/RuoYi HTTP APIs

**Spec:** `docs/superpowers/specs/2026-09-13-uniapp-existing-api-integration-design.md`

## Global Constraints

- Do not add or change backend endpoints, database tables, or SQL migrations.
- Existing endpoint reads and writes must never fall back to mock records after a network or business failure.
- Missing capabilities retain their current local mock behavior.
- Preserve page layout and all existing navigation/safe-area fixes.
- Preserve the user's uncommitted `Quanta-uniapp/src/manifest.json` and `.local/` changes.
- Business modules always use `VITE_API_BASE_URL`; `VITE_USE_MOCK` remains login-only.

---

### Task 1: Shared API contracts, image URLs, upload transport, and real profile

**Files:**
- Create: `Quanta-uniapp/src/api/contracts.ts`
- Create: `Quanta-uniapp/src/api/mappers.ts`
- Create: `Quanta-uniapp/src/api/mappers.test.ts`
- Modify: `Quanta-uniapp/src/utils/request.ts`
- Modify: `Quanta-uniapp/src/api/user.ts`
- Modify: `Quanta-uniapp/src/types/auth.ts`
- Modify: `Quanta-uniapp/src/hooks/useLogin.ts`
- Modify: `Quanta-uniapp/src/pages/login/freshman.vue`
- Modify: `Quanta-uniapp/src/pages/login/tower.vue`

**Interfaces:**
- Produces: `AjaxResponse<T>`, `TableResponse<T>`, `resolveApiAssetUrl(path)`, `getCurrentProfileApi()`, and `completeLogin(...): Promise<UserProfile>`.
- Consumes: `request<T>()`, `API_BASE_URL`, and the stored bearer token.

- [ ] **Step 1: Write failing mapper tests**

```ts
expect(resolveApiAssetUrl('/profile/upload/a.png')).toBe(`${API_BASE_URL}/profile/upload/a.png`)
expect(mapSysUserProfile({ userId: 103, userName: 'qt_member', nickName: '塔员小王', memberDepartment: 'FRONTEND', memberCohort: '21st' }, 'tower')).toMatchObject({ id: '103', account: 'qt_member', name: '塔员小王', department: '全栈(前端)', batch: '21st' })
```

- [ ] **Step 2: Run the focused test and verify failure**

Run: `npm test -- src/api/mappers.test.ts`

Expected: FAIL because the mapper module does not exist.

- [ ] **Step 3: Implement contracts and transport helpers**

```ts
export interface AjaxResponse<T = unknown> { code: number; msg: string; data: T }
export interface TableResponse<T> { code: number; msg: string; rows: T[]; total: number }
export const resolveApiAssetUrl = (path = '') => /^(https?:|data:|\/static\/)/.test(path) ? path : `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
```

Allow `request` data to be any serializable value and export the unauthorized handler behavior unchanged. Add `getCurrentProfileApi()` for `GET /getInfo`. Make `completeLogin` save the initial token, fetch the profile, update the session, and fall back only to the authenticated account identity if the profile request fails.

- [ ] **Step 4: Await profile completion in both login pages and run tests**

Run: `npm test -- src/api/mappers.test.ts`

Expected: PASS.

- [ ] **Step 5: Commit the shared foundation**

```bash
git add Quanta-uniapp/src/api Quanta-uniapp/src/utils/request.ts Quanta-uniapp/src/types/auth.ts Quanta-uniapp/src/hooks/useLogin.ts Quanta-uniapp/src/pages/login/freshman.vue Quanta-uniapp/src/pages/login/tower.vue
git commit -m "feat: add uniapp api integration foundation"
```

### Task 2: Recruitment application and process

**Files:**
- Create: `Quanta-uniapp/src/api/recruitment.ts`
- Create: `Quanta-uniapp/src/api/recruitment.test.ts`
- Modify: `Quanta-uniapp/src/pages/freshman/join-us/submit.vue`
- Modify: `Quanta-uniapp/src/pages/freshman/join-us/process.vue`

**Interfaces:**
- Produces: `getMyApplication()`, `submitApplication(form)`, `getMyInterviewProcess()`, `mapInterviewProcess(application, results)`.
- Consumes: `GET /qt/interview/my`, multipart `POST /qt/interview/apply`, `GET /qt/interview/myResults`, and recruitment presentation helpers from `mockRecruitment.ts` only for unsupported invitation responses and static labels.

- [ ] **Step 1: Write failing department, gender, application, and process mapper tests**

```ts
expect(toDepartmentCode('全栈(前端)')).toBe('FRONTEND')
expect(toGenderCode('女')).toBe('1')
expect(mapInterviewProcess(application, [{ roundId: 1, department: 'FRONTEND', resultStatus: 'PASS' }])[0].stages[0].status).toBe('passed')
```

- [ ] **Step 2: Run the focused tests and verify failure**

Run: `npm test -- src/api/recruitment.test.ts`

Expected: FAIL because recruitment API functions do not exist.

- [ ] **Step 3: Implement real recruitment transport**

Use `uni.uploadFile` with the bearer token when the photo is a local temporary file. Send ordinary fields through `formData`; when editing with an existing HTTP image, send `photoUrl` through `uni.request`-compatible multipart upload only if a file is present, otherwise submit a request that preserves the existing backend photo path. Reject non-200 HTTP or business codes with the backend message.

- [ ] **Step 4: Replace application local-storage reads and writes**

On page load, call `getMyApplication()`. On submission, await `submitApplication`, guard duplicate taps, set the submitted state only after success, and reload the server application. Draft-only editing may remain in memory for the current page session but must not set the submitted flag.

- [ ] **Step 5: Replace process local storage with server results**

On mount, load application and results in parallel. Map `PASS`, `FAIL`, `PENDING`, and `WAITING` into the existing timeline. Keep the local accept/decline mock only when the timeline renders an invitation state not represented by a server mutation endpoint.

- [ ] **Step 6: Run tests and commit**

Run: `npm test -- src/api/recruitment.test.ts`

```bash
git add Quanta-uniapp/src/api/recruitment.ts Quanta-uniapp/src/api/recruitment.test.ts Quanta-uniapp/src/pages/freshman/join-us
git commit -m "feat: connect recruitment to backend"
```

### Task 3: Activities and notifications

**Files:**
- Create: `Quanta-uniapp/src/api/activity.ts`
- Create: `Quanta-uniapp/src/api/activity.test.ts`
- Create: `Quanta-uniapp/src/api/notice.ts`
- Modify: `Quanta-uniapp/src/pages/freshman/talk/talk.vue`
- Modify: `Quanta-uniapp/src/pages/freshman/elite-share/elite-share.vue`
- Modify: `Quanta-uniapp/src/pages/member/home/home.vue`
- Modify: `Quanta-uniapp/src/pages/member/function/function.vue`

**Interfaces:**
- Produces: `getPublishedActivities()`, `getActivityByKind(kind)`, `getMyActivitySignup(activityId)`, `signupActivity(activityId, remark)`, `getTopNotices()`, `markNoticeRead(id)`.
- Consumes: `/system/activity/list`, `/system/signup/detailList`, `/system/signup`, `/system/notice/listTop`, and `/system/notice/markRead`.

- [ ] **Step 1: Write failing activity mapper and selector tests**

```ts
expect(selectActivity(rows, 'LECTURE')?.title).toContain('宣讲')
expect(mapSignup({ activityId: 9, status: 'APPLIED', signupTime: '2026-09-13' }).activityId).toBe(9)
```

Cover the current seed-data fallback: if `activityType` is `GENERAL`, `宣讲` selects lecture and `分享` selects sharing without introducing mock activity data.

- [ ] **Step 2: Implement API calls and update both freshman activity pages**

Initialize pages with an empty/loading model, then hydrate with a real activity and real signup. Keep only elite guest speaker cards from `getMockEliteShareGuests()` because that endpoint is missing. Await signup POST and reload the signup instead of incrementing a local authoritative count.

- [ ] **Step 3: Connect member activity cards and notice actions**

Map published activities to the existing member-home cards. Load notice count and show the recent notices in a compact modal on bell/announcement click; opening a notice calls `markNoticeRead` and refreshes the count.

- [ ] **Step 4: Run tests and commit**

Run: `npm test -- src/api/activity.test.ts`

```bash
git add Quanta-uniapp/src/api/activity* Quanta-uniapp/src/api/notice.ts Quanta-uniapp/src/pages/freshman/talk Quanta-uniapp/src/pages/freshman/elite-share Quanta-uniapp/src/pages/member/home Quanta-uniapp/src/pages/member/function
git commit -m "feat: connect activities and notices to backend"
```

### Task 4: Member directory and profile

**Files:**
- Create: `Quanta-uniapp/src/api/member.ts`
- Create: `Quanta-uniapp/src/api/member.test.ts`
- Modify: `Quanta-uniapp/src/pages/member/contact/contact.vue`
- Modify: `Quanta-uniapp/src/pages/member/profile/profile.vue`

**Interfaces:**
- Produces: `getMemberDirectory(params?)`, `mapLabMember(row)`, `getCurrentProfileApi()` from Task 1.
- Consumes: `GET /qt/member/list` and `GET /getInfo`.

- [ ] **Step 1: Write failing member mapping tests**

```ts
expect(mapLabMember({ userId: 103, nickName: '塔员小王', memberNo: 'Q101', memberDepartment: 'FRONTEND', memberTitle: '成员', memberCohort: '21st' })).toMatchObject({ name: '塔员小王', code: 'Q101', department: '全栈(前端)', batch: '21st' })
```

- [ ] **Step 2: Implement member API and update pages**

Directory rows come only from `/qt/member/list`; derive available cohort tabs from returned rows. Because card metadata is absent, merge only the current local business-card flag by member number so the documented missing card capability stays mocked. Profile page refreshes from `/getInfo` and stores the normalized server profile.

- [ ] **Step 3: Run tests and commit**

Run: `npm test -- src/api/member.test.ts`

```bash
git add Quanta-uniapp/src/api/member* Quanta-uniapp/src/pages/member/contact/contact.vue Quanta-uniapp/src/pages/member/profile/profile.vue
git commit -m "feat: connect member data to backend"
```

### Task 5: Workstation availability and reservation

**Files:**
- Create: `Quanta-uniapp/src/api/workstation.ts`
- Create: `Quanta-uniapp/src/api/workstation.test.ts`
- Modify: `Quanta-uniapp/src/pages/member/workstation/workstation.vue`

**Interfaces:**
- Produces: `getWorkstationDay(dateKey)`, `reserveWorkstation(workstationId, dateKey, periods)`, `Workstation`, `WorkstationDay`.
- Consumes: `/system/workstation/list`, `/system/reservation/detailList`, `/system/reservation`, and `WORKSTATION_PERIODS`.

- [ ] **Step 1: Write failing reservation overlap tests**

```ts
expect(mapWorkstationDay('2026-09-14', stations, reservations, 103).workstations[0].slots[0].status).toBe('mine')
expect(toReservationPayload(7, '2026-09-14', 'morning')).toMatchObject({ workstationId: 7, reserveStart: '2026-09-14 07:00:00', reserveEnd: '2026-09-14 12:00:00', status: 'PENDING' })
```

- [ ] **Step 2: Implement real list, mapping, and reservation calls**

Assign layout IDs (`seat-1`, `seat-2`, ...) separately from numeric `workstationId`. Mark disabled stations unavailable. Map approved/pending overlapping reservations to `booked` or `mine`; because the current backend serializes reservation timestamps as dates, treat a date-only reservation conservatively as all-day and record that contract limitation in the final gap report.

- [ ] **Step 3: Replace workstation page imports and reload on write**

The page passes `selectedSeat.workstationId`, awaits one POST per unique selected period, and reloads. Keep `releaseWorkstationReservation` only in the unsupported mock cancellation path in My Services.

- [ ] **Step 4: Run tests and commit**

Run: `npm test -- src/api/workstation.test.ts`

```bash
git add Quanta-uniapp/src/api/workstation* Quanta-uniapp/src/pages/member/workstation/workstation.vue
git commit -m "feat: connect workstation booking to backend"
```

### Task 6: Library and member service records

**Files:**
- Create: `Quanta-uniapp/src/api/library.ts`
- Create: `Quanta-uniapp/src/api/library.test.ts`
- Create: `Quanta-uniapp/src/api/memberServices.ts`
- Create: `Quanta-uniapp/src/api/memberServices.test.ts`
- Modify: `Quanta-uniapp/src/pages/member/library/library.vue`
- Modify: `Quanta-uniapp/src/pages/member/services/services.vue`

**Interfaces:**
- Produces: `getLibraryBooks()`, `borrowBook(bookId)`, `getMemberServices()` and existing page view-model types.
- Consumes: `/system/book/list`, `/system/borrow`, `/system/borrow/detailList`, `/system/reservation/detailList`, `/system/order/detailList`.

- [ ] **Step 1: Write failing book and service-record mapper tests**

```ts
expect(mapBook({ bookId: 8, isbn: '9787', bookName: '设计模式', locationDesc: '书架C-02', availableCount: 1, status: '0' })).toMatchObject({ id: '8', cabinet: '书架C-02', status: 'available' })
expect(mapBorrowRecord({ borrowId: 3, bookName: '设计模式', status: 'BORROWED' }).status).toBe('borrowed')
```

- [ ] **Step 2: Implement real library operations**

Use `bookType === 'TEXTBOOK'` as textbook; otherwise map to general. Use `availableCount > 0 && status === '0'` as available. Send backend-formatted due time and reload the catalog after borrowing.

- [ ] **Step 3: Implement real My Services aggregation**

Fetch reservation, borrow, and order detail lists in parallel, map them to existing components, and keep existing cancel/return mocks isolated. Never remove or mutate a real server row locally when an unsupported action is invoked.

- [ ] **Step 4: Run tests and commit**

Run: `npm test -- src/api/library.test.ts src/api/memberServices.test.ts`

```bash
git add Quanta-uniapp/src/api/library* Quanta-uniapp/src/api/memberServices* Quanta-uniapp/src/pages/member/library/library.vue Quanta-uniapp/src/pages/member/services/services.vue
git commit -m "feat: connect library and services to backend"
```

### Task 7: Clothing catalog and draft orders

**Files:**
- Create: `Quanta-uniapp/src/api/clothing.ts`
- Create: `Quanta-uniapp/src/api/clothing.test.ts`
- Modify: `Quanta-uniapp/src/pages/member/shirt-order/shirt-order.vue`

**Interfaces:**
- Produces: `getShirtProduct()`, `createDraftShirtOrder(selection)`, `parseOptionArray(value)`.
- Consumes: `/system/item/list`, `/system/order`, shared image URL resolution, and `validateShirtSelection()` from the retained member utility.

- [ ] **Step 1: Write failing clothing mapper tests**

```ts
expect(parseOptionArray('["黑色","白色"]')).toEqual(['黑色', '白色'])
expect(mapClothingItem({ itemId: 2, effectImageUrl: '/profile/a.png', colorOptionsJson: '["黑色"]', sizeOptionsJson: '["L"]' })).toMatchObject({ itemId: 2, colors: ['黑色'], sizes: ['L'] })
```

- [ ] **Step 2: Implement item loading and draft creation**

Select the first active item, parse options safely, resolve its image, and keep the existing mock price only because the backend item entity has no price field. Generate `orderNo` as `QT` plus timestamp and a four-digit random suffix; POST quantity `1`, status `DRAFT`, selected color/size, and mapped unit/total price.

- [ ] **Step 3: Update page, run tests, and commit**

Run: `npm test -- src/api/clothing.test.ts`

```bash
git add Quanta-uniapp/src/api/clothing* Quanta-uniapp/src/pages/member/shirt-order/shirt-order.vue
git commit -m "feat: connect clothing orders to backend"
```

### Task 8: Integration guard, full verification, and gap report

**Files:**
- Create: `Quanta-uniapp/src/api/backendIntegration.test.ts`
- Modify: `docs/前端对接说明.md`

**Interfaces:**
- Consumes: all production page source files and API modules.
- Produces: a regression guard and an exact missing/limited-contract checklist.

- [ ] **Step 1: Add source-level integration assertions**

```ts
expect(read('pages/freshman/join-us/submit.vue')).toContain("api/recruitment")
expect(read('pages/member/library/library.vue')).not.toContain("libraryMock")
expect(read('pages/member/business-card/edit.vue')).toContain("memberMock")
```

- [ ] **Step 2: Run the complete frontend test suite**

Run: `npm test`

Expected: all tests pass.

- [ ] **Step 3: Build the WeChat mini program**

Run: `npm run build:mp-weixin`

Expected: exit code 0 and output under `Quanta-uniapp/dist/build/mp-weixin`.

- [ ] **Step 4: Verify live authenticated reads**

Log in with `qt_fresh / admin123` and verify `/getInfo`, `/qt/interview/my`, `/qt/interview/myResults`, `/system/activity/list`, and `/system/signup/detailList`. Log in with `qt_member / admin123` and verify `/getInfo`, `/qt/member/list`, `/system/workstation/list`, `/system/reservation/detailList`, `/system/book/list`, `/system/borrow/detailList`, `/system/item/list`, `/system/order/detailList`, and `/system/notice/listTop` all return business code 200.

- [ ] **Step 5: Update documentation and commit**

Document the retained mocks and existing-contract limitations: interview invitation response, activity guests, business cards, banners, cancellation, return confirmation, clothing price/payment UI, activity signup count, and date-only workstation/activity timestamps.

```bash
git add Quanta-uniapp/src/api/backendIntegration.test.ts docs/前端对接说明.md
git commit -m "test: verify uniapp backend integration"
```

- [ ] **Step 6: Inspect final worktree scope**

Run: `git status --short` and `git diff --check HEAD~8..HEAD`.

Expected: only the user's original `manifest.json` and `.local/` remain uncommitted; no whitespace errors.

## Live-contract verification adjustments

- Activity content, signup state, and signup submission are server-backed after `lab_patch_signup_remark.sql` repaired the missing column on 2026-09-14.
- Workstation booking stays mocked because member-scoped reservation results omit other users' occupancy and reservation timestamps serialize without the time slots required by the UI.
- Learning materials were additionally connected through `GET /qt/materials` and authenticated material download.
