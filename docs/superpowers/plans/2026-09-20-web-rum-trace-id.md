# Web RUM and Trace ID Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a unique, inspectable Trace ID to every Web API request and deliver a Chinese ARMS RUM handoff guide for backend and operations staff.

**Architecture:** A focused utility owns Trace ID generation and case-insensitive header handling. The existing request wrapper applies it before deciding between the real Axios service and the mock adapter, then attaches it to normalized errors. The RUM guide documents the external ARMS setup; the SDK remains disabled until a valid ARMS endpoint is supplied.

**Tech Stack:** Vue 3, Axios, Vitest, Vite, Alibaba Cloud ARMS RUM documentation

**Spec:** `docs/superpowers/specs/2026-09-20-web-rum-trace-id-design.md`

## Global Constraints

- Modify only the Web frontend and documentation; do not modify backend source.
- Use `X-Trace-Id` as the HTTP request header.
- Generate a new 32-character hexadecimal ID for every request unless the caller already supplied one.
- Apply the same behavior to real and mock requests.
- Do not enable the ARMS SDK without a real reporting endpoint.
- Never collect or document real passwords, tokens, AccessKeys, resume contents, or email verification codes.

---

### Task 1: Trace ID utility

**Files:**
- Create: `Quanta-admin-web/src/utils/trace-id.js`
- Create: `Quanta-admin-web/src/utils/__tests__/trace-id.test.js`

**Interfaces:**
- Produces: `TRACE_ID_HEADER = 'X-Trace-Id'`.
- Produces: `createTraceId(): string`.
- Produces: `getTraceId(configOrHeaders): string`.
- Produces: `withTraceId(config): object`.

- [ ] **Step 1: Write failing utility tests**

Test these exact properties:

```js
expect(createTraceId()).toMatch(/^[a-f0-9]{32}$/)
expect(createTraceId()).not.toBe(createTraceId())
expect(withTraceId({ headers: {} }).headers['X-Trace-Id']).toMatch(/^[a-f0-9]{32}$/)
expect(withTraceId({ headers: { 'x-trace-id': 'backend-test-id' } }).headers['x-trace-id']).toBe('backend-test-id')
expect(getTraceId({ headers: { 'X-TRACE-ID': 'case-insensitive' } })).toBe('case-insensitive')
```

- [ ] **Step 2: Run the utility test and verify failure**

Run: `npm run test:run -- src/utils/__tests__/trace-id.test.js`

Expected: FAIL because `trace-id.js` does not exist.

- [ ] **Step 3: Implement the utility**

Implement UUID generation with this precedence:

1. `globalThis.crypto.randomUUID()` and remove hyphens.
2. `globalThis.crypto.getRandomValues(new Uint8Array(16))` and encode each byte as two lowercase hexadecimal characters.
3. Combine the current timestamp with multiple `Math.random()` values, normalize to lowercase hexadecimal, and cut or pad to exactly 32 characters.

Header lookup must compare lowercase names so Axios headers and ordinary objects behave consistently. `withTraceId()` must return the original ID when one already exists.

- [ ] **Step 4: Run the utility test**

Run: `npm run test:run -- src/utils/__tests__/trace-id.test.js`

Expected: PASS.

### Task 2: Unified request integration

**Files:**
- Modify: `Quanta-admin-web/src/utils/request.js`
- Modify: `Quanta-admin-web/src/utils/__tests__/request.test.js`

**Interfaces:**
- Consumes: `withTraceId()` and `getTraceId()` from Task 1.
- Produces: every rejected request error has `error.traceId`.

- [ ] **Step 1: Write failing request-layer tests**

Extend the existing tests to verify that an invalid mock login rejection includes:

```js
await expect(login({ username: 'admin', password: 'wrong' })).rejects.toMatchObject({
  code: 500,
  traceId: expect.stringMatching(/^[a-f0-9]{32}$/),
})
```

Add a direct `withTraceId()`/request configuration assertion showing an explicitly supplied ID is preserved.

- [ ] **Step 2: Run the request test and verify failure**

Run: `npm run test:run -- src/utils/__tests__/request.test.js`

Expected: FAIL because standardized errors do not expose `traceId`.

- [ ] **Step 3: Integrate Trace ID before mock routing**

At the start of `request(config)`, create `requestConfig = withTraceId(config)`. Use `requestConfig` for `shouldUseMock`, `mockRequest`, and `service.request`. Add the same helper to the Axios request interceptor so any direct service use is protected.

In the catch block, read the ID from `rawError.config`, `rawError.response?.config`, or `requestConfig`, then assign it to the normalized error:

```js
error.traceId = getTraceId(rawError?.config) ||
  getTraceId(rawError?.response?.config) ||
  getTraceId(requestConfig)
```

Do not change existing authentication, response normalization, or mock-selection behavior.

- [ ] **Step 4: Run request and utility tests**

Run: `npm run test:run -- src/utils/__tests__/trace-id.test.js src/utils/__tests__/request.test.js`

Expected: PASS.

### Task 3: Chinese ARMS RUM and backend handoff guide

**Files:**
- Create: `docs/Web端阿里云RUM与TraceID接入操作手册.md`

**Interfaces:**
- Produces: a standalone document that backend or operations staff can follow without reading frontend source.

- [ ] **Step 1: Document current Alibaba Cloud setup steps**

Use only official Alibaba Cloud documentation links. Explain how to open ARMS → 用户体验监控 → 应用列表, choose the deployment region, create a Web & H5 application, and copy the generated `endpoint`. Note that current SDK 0.1.x and later can derive the application identity from the endpoint, so `pid` may be optional.

- [ ] **Step 2: Document the frontend handoff values**

Ask operations to provide the following named values, with the endpoint copied verbatim from ARMS and the release version taken from the deployment:

```text
VITE_ARMS_RUM_ENDPOINT
VITE_APP_ENV=prod
VITE_APP_VERSION
VITE_ARMS_RUM_SAMPLE_RATE=0.1
```

State that none of these values should be confused with Alibaba Cloud AccessKey credentials, and that AccessKeys must never enter frontend environment files.

- [ ] **Step 3: Document backend Trace ID handling**

Specify that the backend reads `X-Trace-Id`, generates one only when absent, writes it to the logging MDC/context, returns it as `X-Trace-Id`, and clears the context after the request. For cross-origin deployments, allow the request header and expose the response header through CORS.

- [ ] **Step 4: Document privacy and acceptance checks**

Exclude Authorization, Cookie, passwords, verification codes, uploaded resumes, photos, email content, and form bodies from RUM. Include checks for browser Network headers, backend log search, ARMS PV/error/performance data, SPA route changes, sampling, and source-map access policy.

### Task 4: Full frontend validation

**Files:**
- Review only the files created or modified in Tasks 1–3.

**Interfaces:**
- Consumes all deliverables from Tasks 1–3.

- [ ] **Step 1: Run all Web frontend tests**

Run: `npm run test:run`

Expected: all tests pass.

- [ ] **Step 2: Build the production frontend**

Run: `npm run build`

Expected: Vite production build succeeds.

- [ ] **Step 3: Run lint**

Run: `npm run lint`

Expected: no new errors or warnings compared with the existing baseline.

- [ ] **Step 4: Inspect the final diff**

Run `git diff --check` and confirm that no backend file, generated build output, local tool folder, or unrelated recruitment file is staged for this feature.
