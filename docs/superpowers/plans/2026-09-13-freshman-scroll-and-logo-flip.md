# Freshman Scroll and Logo Flip Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the bottom content fully reachable above the floating freshman navigation and add a reversible 3D slogan flip to the freshman-home logo.

**Architecture:** Keep the shared floating navigation unchanged. Add scrollable trailing space inside the function-page content, increase native page trailing space on the home page, and turn the existing logo circle into a state-driven two-sided CSS card.

**Tech Stack:** Vue 3, uni-app, scoped CSS, Vitest, Weixin Mini Program WXML/WXSS

**Spec:** `docs/superpowers/specs/2026-09-13-freshman-scroll-and-logo-flip-design.md`

## Global Constraints

- Preserve the existing bottom navigation position and appearance.
- Preserve the existing logo size, position, front image, and outer orange glow.
- Use the exact slogan `nothing but profesSiOnal`, with `S` and `O` accented in orange.
- Do not add image dependencies or change authentication and navigation logic.

---

### Task 1: Add reachable bottom scroll space

**Files:**
- Create: `Quanta-uniapp/src/pages/freshman/freshman.layout.test.ts`
- Modify: `Quanta-uniapp/src/pages/freshman/function.vue`
- Modify: `Quanta-uniapp/src/pages/freshman/home.vue`

**Interfaces:**
- Consumes: Existing `.circle-list`, `.freshman-home`, and shared floating navigation dimensions.
- Produces: `320rpx` plus safe-area trailing space on both freshman content pages.

- [ ] **Step 1: Add a failing source-layout test**

Create assertions that `.circle-list` ends with `calc(320rpx + env(safe-area-inset-bottom))` bottom padding and `.freshman-home` uses the same bottom padding.

```ts
const functionSource = readFileSync(new URL('./function.vue', import.meta.url), 'utf8')
const homeSource = readFileSync(new URL('./home.vue', import.meta.url), 'utf8')
const circleListRule = functionSource.match(/\.circle-list\s*\{([^}]*)\}/)?.[1] ?? ''
const homeRule = homeSource.match(/\.freshman-home\s*\{([^}]*)\}/)?.[1] ?? ''

expect(circleListRule).toContain('padding: 50rpx 0 calc(320rpx + env(safe-area-inset-bottom))')
expect(homeRule).toContain('padding-bottom: calc(320rpx + env(safe-area-inset-bottom))')
```

- [ ] **Step 2: Run the focused test and verify failure**

Run `npm test -- src/pages/freshman/freshman.layout.test.ts`; expect failure because current bottom space is `40rpx` for the circle list and `220rpx` for the home page.

- [ ] **Step 3: Apply minimal spacing changes**

Set `.circle-list` to `padding:50rpx 0 calc(320rpx + env(safe-area-inset-bottom))` and `.freshman-home` to `padding-bottom:calc(320rpx + env(safe-area-inset-bottom))`.

- [ ] **Step 4: Run the focused test**

Run `npm test -- src/pages/freshman/freshman.layout.test.ts`; expect PASS.

### Task 2: Add the reversible logo flip

**Files:**
- Modify: `Quanta-uniapp/src/pages/freshman/home.vue`
- Modify: `Quanta-uniapp/src/pages/freshman/freshman.layout.test.ts`

**Interfaces:**
- Consumes: Existing `brandMarkSrc` and the current `366rpx` logo circle.
- Produces: `logoFlipped: Ref<boolean>`, `toggleLogo(): void`, front/back logo faces, and a 650ms Y-axis flip.

- [ ] **Step 1: Extend the test with failing flip assertions**

Assert the page contains a clickable `.logo-circle`, `logoFlipped`, `toggleLogo`, two `.logo-face` elements, the exact slogan segments, `transform-style:preserve-3d`, hidden backfaces, `transition:transform .65s`, and `rotateY(180deg)`.

```ts
expect(homeSource).toContain("const logoFlipped = ref(false)")
expect(homeSource).toContain('const toggleLogo = () =>')
expect(homeSource).toContain('@click="toggleLogo"')
expect(homeSource.match(/class="logo-face/g)).toHaveLength(2)
expect(homeSource).toContain('nothing but')
expect(homeSource).toContain('profes')
expect(homeSource).toContain('>S</text>')
expect(homeSource).toContain('>O</text>')
expect(homeSource).toContain('transform-style: preserve-3d')
expect(homeSource).toContain('backface-visibility: hidden')
expect(homeSource).toContain('transition: transform 0.65s')
expect(homeSource).toContain('transform: rotateY(180deg)')
```

- [ ] **Step 2: Run the focused test and verify failure**

Run `npm test -- src/pages/freshman/freshman.layout.test.ts`; expect failure because the current logo is a single static image.

- [ ] **Step 3: Implement the minimal flip**

Add `const logoFlipped = ref(false)` and a toggle handler. Replace the single-face logo markup with front and back faces. Style the back with a dark gradient, orange glow and orange `S`/`O` accents while retaining the existing circle dimensions.

- [ ] **Step 4: Verify tests and Weixin output**

Run `npm test` and expect all tests to pass. Confirm the running `npm run dev:mp-weixin` watcher reports `Build complete`; inspect generated WXML/WXSS for the two faces, slogan, bottom spacing and flip transforms.

- [ ] **Step 5: Commit only scoped files**

Commit the plan separately, then commit `function.vue`, `home.vue`, and `freshman.layout.test.ts` without staging `manifest.json` or `.local/`.
