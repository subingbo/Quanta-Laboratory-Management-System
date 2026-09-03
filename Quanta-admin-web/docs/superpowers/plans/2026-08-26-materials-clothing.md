# Materials and Clothing Orders Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build permission-aware learning material management and clothing order management pages.

**Architecture:** Use separate API/Mock modules and lazy-loaded Vue pages. Enforce write permissions in both UI controls and Mock handlers; keep unavailable backend operations behind documented temporary contracts.

**Tech Stack:** Vue 3, Element Plus, JavaScript, Vite, Vitest

**Spec:** `docs/superpowers/specs/2026-08-26-materials-clothing-design.md`

## Global Constraints

- Managers can read materials but cannot upload or delete.
- Managers cannot see clothing orders.
- Management uses `material:write` and `order:confirm` for mutations.
- Material upload accepts supported document files up to 50MB.

---

### Task 1: Permissions, API and Mock

**Files:**
- Create: `src/api/materials.js`, `src/api/clothing-orders.js`
- Create: `src/mock/data/materials.js`, `src/mock/data/clothing-orders.js`
- Create: `src/mock/handlers/materials.js`, `src/mock/handlers/clothing-orders.js`
- Modify: `src/mock/data/accounts.js`, `src/mock/index.js`
- Test: `src/api/__tests__/materials-clothing.test.js`, `src/mock/handlers/__tests__/materials-clothing.test.js`

**Interfaces:**
- `getMaterials()`, `uploadMaterial(file)`, `removeMaterial(materialId)`
- `getClothingOrders(params)`, `approveClothingOrder(orderId)`

- [ ] Add permission tests proving management write access and manager read-only/no-order access.
- [ ] Implement RuoYi-style handlers, including 50MB validation and status mutation.
- [ ] Run focused API/handler tests.

### Task 2: Learning materials page

**Files:**
- Create: `src/views/learning-materials/index.vue`, `src/views/learning-materials/learning-materials.css`
- Modify: `src/mock/data/routers.js`, `src/router/component-map.js`
- Test: `src/views/learning-materials/__tests__/learning-materials.test.js`

**Interfaces:**
- Consumes Task 1 material APIs and `PermissionButton`.

- [ ] Test management controls and manager read-only rendering.
- [ ] Implement upload dragger, validation feedback, compact table and delete confirmation.
- [ ] Connect `learning-materials/index` to the existing dynamic route.
- [ ] Run focused page tests.

### Task 3: Clothing orders page

**Files:**
- Create: `src/views/clothing-orders/index.vue`, `src/views/clothing-orders/clothing-orders.css`
- Create: `src/views/clothing-orders/components/PaymentProofDialog.vue`
- Modify: `src/mock/data/routers.js`, `src/router/component-map.js`
- Test: `src/views/clothing-orders/__tests__/clothing-orders.test.js`

**Interfaces:**
- Consumes Task 1 order APIs and emits no external state.

- [ ] Test status filtering, proof dialog, and receipt confirmation.
- [ ] Implement compact order table, state tags, status select, proof preview and confirmation prompt.
- [ ] Connect `clothing-orders/index` to the existing dynamic route.
- [ ] Run focused page tests.

### Task 4: Verification

**Files:**
- Modify: `README.md`, `src/router/__tests__/route-transformer.test.js`

- [ ] Document real and temporary contracts and add component-map assertions.
- [ ] Run `npm run test:run`, `npm run lint`, and `npm run build`.
- [ ] Browser-check manager read-only materials and management order actions.
- [ ] Commit only project files; preserve user API documents.
