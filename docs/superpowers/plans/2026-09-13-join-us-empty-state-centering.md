# Join Us Empty State Centering Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Center the unsubmitted-resume prompt card horizontally and vertically within the Join Us content area below the title and tab navigation.

**Architecture:** Preserve the existing page and component hierarchy. Change only the empty-state Flex alignment contract in `process.vue`, with a source-level Vitest regression test that protects the responsive centering rule.

**Tech Stack:** Vue 3, uni-app, scoped CSS, Vitest, Weixin Mini Program build output

**Spec:** `docs/superpowers/specs/2026-09-13-join-us-empty-state-centering-design.md`

## Global Constraints

- Keep the existing prompt card content, size, colors, shadow, and interaction unchanged.
- Keep the title bar and the “流程 / 投递” tab bar at the top using their existing layout.
- Do not modify submitted-process behavior, modal behavior, resume submission, or recruitment state logic.
- Use the existing Flex height chain; do not introduce viewport-height calculations.

---

### Task 1: Center the unsubmitted prompt card

**Files:**
- Create: `Quanta-uniapp/src/pages/freshman/join-us/process.layout.test.ts`
- Modify: `Quanta-uniapp/src/pages/freshman/join-us/process.vue`

**Interfaces:**
- Consumes: The existing `.process-page` and `.tab-content` Flex height chain.
- Produces: An `.empty-state` layout rule with `justify-content:center` and without a fixed `padding-top` offset.

- [ ] **Step 1: Write the failing layout regression test**

```ts
import { readFileSync } from 'node:fs'
import { describe, expect, it } from 'vitest'

const source = readFileSync(new URL('./process.vue', import.meta.url), 'utf8')
const emptyStateRule = source.match(/\.empty-state\{([^}]*)\}/)?.[1] ?? ''

describe('Join Us empty state layout', () => {
  it('centers the prompt in the remaining content area without a fixed top offset', () => {
    expect(emptyStateRule).toContain('align-items:center')
    expect(emptyStateRule).toContain('justify-content:center')
    expect(emptyStateRule).not.toContain('padding-top:')
  })
})
```

- [ ] **Step 2: Run the focused test and verify the old layout fails**

Run:

```powershell
cd Quanta-uniapp
npm test -- src/pages/freshman/join-us/process.layout.test.ts
```

Expected: FAIL because the existing rule contains `justify-content:flex-start` and `padding-top:128rpx`.

- [ ] **Step 3: Apply the minimal CSS change**

Replace the existing `.empty-state` rule with:

```css
.empty-state{flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;box-sizing:border-box}
```

- [ ] **Step 4: Run focused and full tests**

Run:

```powershell
cd Quanta-uniapp
npm test -- src/pages/freshman/join-us/process.layout.test.ts
npm test
```

Expected: The focused test and all existing tests PASS.

- [ ] **Step 5: Verify the Weixin build output**

Keep `npm run dev:mp-weixin` running with:

```powershell
$env:VITE_API_BASE_URL = "http://192.168.20.119:8080"
$env:VITE_USE_MOCK = "false"
npm run dev:mp-weixin
```

Confirm the watcher reports `Build complete`, then inspect `dist/dev/mp-weixin/pages/freshman/join-us/process.wxss` and verify the generated `.empty-state` rule contains `justify-content:center` and no `padding-top`.

- [ ] **Step 6: Review and commit only the implementation files**

Run:

```powershell
git diff --check -- Quanta-uniapp/src/pages/freshman/join-us/process.vue Quanta-uniapp/src/pages/freshman/join-us/process.layout.test.ts
git add -- Quanta-uniapp/src/pages/freshman/join-us/process.vue Quanta-uniapp/src/pages/freshman/join-us/process.layout.test.ts
git commit -m "fix: center join us empty state"
```

Expected: The commit includes only the Vue file and its new layout regression test; existing `manifest.json` and `.local/` changes remain untouched.
