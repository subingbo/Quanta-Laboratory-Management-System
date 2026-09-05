# Uni-app Real Backend Smoke Test Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the WeChat mini-program build use the local Spring Boot backend and real login flow by default while preserving an explicit opt-in login mock.

**Architecture:** A focused runtime configuration module owns API URL and mock-flag parsing. The existing request and authentication modules consume those exported values; all other feature-specific mock stores remain unchanged.

**Tech Stack:** Vue 3, uni-app, TypeScript, Vite 5, Vitest 2, WeChat mini-program build

**Spec:** `docs/superpowers/specs/2026-09-05-uniapp-real-backend-smoke-test-design.md`

## Global Constraints

- Default backend URL is exactly `http://127.0.0.1:8080`.
- Login mock is enabled only when `VITE_USE_MOCK` is exactly `true`.
- Existing feature-specific mocks are preserved.
- Network failures never fall back to mock login.
- No account, password, or token is committed to source code.

---

### Task 1: Runtime configuration

**Files:**
- Create: `Quanta-uniapp/src/config/runtime.test.ts`
- Create: `Quanta-uniapp/src/config/runtime.ts`

**Interfaces:**
- Consumes: Vite `import.meta.env.VITE_API_BASE_URL` and `import.meta.env.VITE_USE_MOCK`.
- Produces: `resolveApiBaseUrl(value?: string): string`, `resolveUseMock(value?: string): boolean`, `API_BASE_URL: string`, and `USE_LOGIN_MOCK: boolean`.

- [ ] **Step 1: Write the failing configuration tests**

```ts
import { describe, expect, it } from 'vitest'
import { resolveApiBaseUrl, resolveUseMock } from './runtime'

describe('runtime configuration', () => {
  it('uses the local backend when the URL is missing or blank', () => {
    expect(resolveApiBaseUrl()).toBe('http://127.0.0.1:8080')
    expect(resolveApiBaseUrl('   ')).toBe('http://127.0.0.1:8080')
  })

  it('removes trailing slashes from a configured backend URL', () => {
    expect(resolveApiBaseUrl('http://192.168.1.20:8080/')).toBe('http://192.168.1.20:8080')
  })

  it('enables mock login only for the explicit true value', () => {
    expect(resolveUseMock('true')).toBe(true)
    expect(resolveUseMock('false')).toBe(false)
    expect(resolveUseMock()).toBe(false)
  })
})
```

- [ ] **Step 2: Run the focused test and verify failure**

Run: `npm test -- --run src/config/runtime.test.ts`

Expected: FAIL because `./runtime` does not exist.

- [ ] **Step 3: Implement the configuration module**

```ts
const DEFAULT_API_BASE_URL = 'http://127.0.0.1:8080'

export const resolveApiBaseUrl = (value?: string): string => {
  const configured = value?.trim()
  return configured ? configured.replace(/\/+$/, '') : DEFAULT_API_BASE_URL
}

export const resolveUseMock = (value?: string): boolean => value === 'true'

export const API_BASE_URL = resolveApiBaseUrl(import.meta.env.VITE_API_BASE_URL)
export const USE_LOGIN_MOCK = resolveUseMock(import.meta.env.VITE_USE_MOCK)
```

- [ ] **Step 4: Run the focused test and verify success**

Run: `npm test -- --run src/config/runtime.test.ts`

Expected: 3 tests PASS.

- [ ] **Step 5: Commit the configuration module**

```bash
git add Quanta-uniapp/src/config/runtime.ts Quanta-uniapp/src/config/runtime.test.ts
git commit -m "feat: configure uniapp backend runtime"
```

### Task 2: Real login wiring and smoke validation

**Files:**
- Modify: `Quanta-uniapp/src/utils/request.ts:1-10`
- Modify: `Quanta-uniapp/src/api/auth.ts:1-16`

**Interfaces:**
- Consumes: `API_BASE_URL` and `USE_LOGIN_MOCK` from `src/config/runtime.ts`.
- Produces: requests targeting `${API_BASE_URL}${url}` and `loginApi` using the real `/login` endpoint unless mock login is explicitly enabled.

- [ ] **Step 1: Replace the hard-coded Apifox URL**

Add:

```ts
import { API_BASE_URL } from '../config/runtime'
```

Remove the local `BASE_URL` constant and change request URL composition to:

```ts
url: `${API_BASE_URL}${url}`,
```

- [ ] **Step 2: Replace the implicit development mock condition**

Add:

```ts
import { USE_LOGIN_MOCK } from '../config/runtime'
```

Change:

```ts
if (import.meta.env.DEV) {
```

to:

```ts
if (USE_LOGIN_MOCK) {
```

- [ ] **Step 3: Run the complete unit test suite**

Run: `npm test`

Expected: all existing and new tests PASS.

- [ ] **Step 4: Build the WeChat mini-program**

Run: `npm run build:mp-weixin`

Expected: exit code 0 and output in `Quanta-uniapp/dist/build/mp-weixin`.

- [ ] **Step 5: Verify generated request configuration**

Run: `rg -n "127\\.0\\.0\\.1:8080|8176579-7935747-default" dist/build/mp-weixin`

Expected: the local backend URL is present and the old Apifox mock URL is absent.

- [ ] **Step 6: Import into WeChat Developer Tools**

Open `D:\\微信web开发者工具\\微信开发者工具.exe` with project directory `C:\Users\LXZ\Desktop\Quanta-System\Quanta-uniapp\dist\build\mp-weixin` and confirm the project loads without compile errors.

- [ ] **Step 7: Commit the real login wiring**

```bash
git add Quanta-uniapp/src/utils/request.ts Quanta-uniapp/src/api/auth.ts
git commit -m "feat: connect uniapp login to local backend"
```

