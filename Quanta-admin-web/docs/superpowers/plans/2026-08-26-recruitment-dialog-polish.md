# Recruitment Dialog Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Polish recruitment result styling and build a two-tab second-round offer dialog matching the approved design.

**Architecture:** Keep the current recruitment page orchestration and API contracts. Extend the existing dialog components with explicit semantic classes and a local tab state, while the page container loads and passes second-round evaluations through the existing evaluation API.

**Tech Stack:** Vue 3 Composition API, Element Plus, JavaScript, Vitest, Vue Test Utils, CSS

**Spec:** `docs/superpowers/specs/2026-08-26-recruitment-dialog-polish-design.md`

## Global Constraints

- Table Pass/Out remain plain text with `#05C860` and `#FF0404`.
- Dialog Pass/Out and confirm/cancel controls use the same green/red colors with white text.
- Preserve all existing permission logic, department scoping, API paths, and request payload shapes.
- Use existing `getEvaluations` and `sendOffer`; add no dependency.

---

### Task 1: Lock the approved action order, copy, and style hooks with tests

**Files:**
- Modify: `src/views/recruitment/__tests__/FeedbackDialog.test.js`
- Modify: `src/views/recruitment/__tests__/OfferFlow.test.js`
- Modify: `src/views/recruitment/__tests__/CandidateTable.test.js`

**Interfaces:**
- Consumes: existing `select-result`, `confirm`, and `update:decision` component events.
- Produces: executable expectations for `.feedback-dialog__result--pass`, `.offer-confirm-dialog__cancel`, `.offer-dialog__tab`, and plain result classes.

- [ ] **Step 1: Add a failing feedback action-order test**

```js
const actions = wrapper.findAll('[data-test^="feedback-result-"]')
expect(actions.map((button) => button.text())).toEqual(['Pass', 'Out'])
expect(actions[0].classes()).toContain('feedback-dialog__result--pass')
expect(actions[1].classes()).toContain('feedback-dialog__result--out')
```

- [ ] **Step 2: Add failing offer confirmation copy and class tests**

```js
expect(wrapper.text()).toContain('确定向该同学(陈思思)发送二面通过的录用通知？')
expect(wrapper.get('[data-test="cancel-offer-step"]').classes()).toContain('offer-confirm-dialog__cancel')
expect(wrapper.get('[data-test="confirm-offer-step"]').classes()).toContain('offer-confirm-dialog__confirm')
```

- [ ] **Step 3: Add failing offer tab tests**

```js
expect(wrapper.get('[data-test="offer-tab-decision"]').classes()).toContain('is-active')
await wrapper.get('[data-test="offer-tab-feedback"]').trigger('click')
expect(wrapper.text()).toContain('产品部经理')
expect(wrapper.find('[data-test="submit-offer"]').exists()).toBe(false)
```

- [ ] **Step 4: Run focused tests and verify failure**

Run: `npm run test:run -- src/views/recruitment/__tests__/FeedbackDialog.test.js src/views/recruitment/__tests__/OfferFlow.test.js src/views/recruitment/__tests__/CandidateTable.test.js`

Expected: FAIL because the new hooks, copy, and tabs do not exist.

### Task 2: Implement first-round result styling and feedback button order

**Files:**
- Modify: `src/views/recruitment/components/FeedbackDialog.vue`
- Modify: `src/views/recruitment/recruitment.css`
- Test: `src/views/recruitment/__tests__/FeedbackDialog.test.js`
- Test: `src/views/recruitment/__tests__/CandidateTable.test.js`

**Interfaces:**
- Consumes: `select-result` with `PASS` or `FAIL`.
- Produces: Pass-first result controls and semantic classes used by CSS/tests.

- [ ] **Step 1: Render Pass before Out with semantic classes**

```vue
<ElButton data-test="feedback-result-pass" class="feedback-dialog__result feedback-dialog__result--pass" @click="emit('select-result', 'PASS')">Pass</ElButton>
<ElButton data-test="feedback-result-out" class="feedback-dialog__result feedback-dialog__result--out" @click="emit('select-result', 'FAIL')">Out</ElButton>
```

- [ ] **Step 2: Add exact plain-result and dialog-button tokens**

```css
.candidate-choice__status.status-tag--success.is-plain-result { color: #05c860; }
.candidate-choice__status.status-tag--danger.is-plain-result { color: #ff0404; }
.candidate-choice__status.is-plain-result { font-family: Inter, sans-serif; font-size: 12px; font-weight: 500; line-height: 19.5px; }
.feedback-dialog__result--pass.el-button { color: #fff; background: #05c860; border-color: #05c860; }
.feedback-dialog__result--out.el-button { color: #fff; background: #ff0404; border-color: #ff0404; }
```

- [ ] **Step 3: Run focused tests**

Run: `npm run test:run -- src/views/recruitment/__tests__/FeedbackDialog.test.js src/views/recruitment/__tests__/CandidateTable.test.js`

Expected: PASS.

### Task 3: Implement the second-round confirmation and two-tab notification dialog

**Files:**
- Modify: `src/views/recruitment/components/OfferConfirmDialog.vue`
- Modify: `src/views/recruitment/components/OfferDialog.vue`
- Modify: `src/views/recruitment/recruitment.css`
- Test: `src/views/recruitment/__tests__/OfferFlow.test.js`

**Interfaces:**
- Consumes: `candidate`, `department`, `options`, `decision`, `content`, and new `evaluations`/`evaluationsLoading` props.
- Produces: `update:decision`, `update:content`, and `submit`, with tab changes remaining component-local.

- [ ] **Step 1: Update confirmation copy and action classes**

```vue
<p>确定向该同学({{ candidateName }})发送二面通过的录用通知？</p>
<ElButton data-test="cancel-offer-step" class="offer-confirm-dialog__cancel">取消</ElButton>
<ElButton data-test="confirm-offer-step" class="offer-confirm-dialog__confirm">确认</ElButton>
```

- [ ] **Step 2: Add local decision/feedback tabs to OfferDialog**

```js
const activeTab = ref('decision')
watch(() => props.modelValue, (value) => { if (value) activeTab.value = 'decision' })
```

```vue
<button data-test="offer-tab-decision" :class="{ 'is-active': activeTab === 'decision' }" @click="activeTab = 'decision'">是否录用</button>
<button data-test="offer-tab-feedback" :class="{ 'is-active': activeTab === 'feedback' }" @click="activeTab = 'feedback'">查看面评</button>
```

- [ ] **Step 3: Render read-only evaluation cards and empty/loading states**

```vue
<div v-if="evaluationsLoading" v-loading="true" class="offer-dialog__feedback-state" />
<article v-for="evaluation in evaluations" :key="evaluation.evaluationId" class="offer-dialog__evaluation">
  <span>{{ evaluation.interviewerName }}</span><p>{{ evaluation.content }}</p>
</article>
<ElEmpty v-if="!evaluationsLoading && !evaluations.length" description="暂无二面面评" :image-size="52" />
```

- [ ] **Step 4: Style tabs, compact decision buttons, forms, and actions**

```css
.offer-dialog__tab.is-active::after { background: #fdaf32; }
.offer-dialog__decision.is-pass { background: #05c860; }
.offer-dialog__decision.is-out { background: #ff0404; }
.offer-confirm-dialog__cancel.el-button { color: #fff; background: #ff0404; border-color: #ff0404; }
.offer-confirm-dialog__confirm.el-button { color: #fff; background: #05c860; border-color: #05c860; }
```

- [ ] **Step 5: Run offer flow tests**

Run: `npm run test:run -- src/views/recruitment/__tests__/OfferFlow.test.js`

Expected: PASS.

### Task 4: Load and pass second-round evaluations from the page container

**Files:**
- Modify: `src/views/recruitment/index.vue`
- Test: `src/views/recruitment/__tests__/recruitment.test.js`

**Interfaces:**
- Consumes: `getEvaluations({ applicationId, roundId: 2, department })`.
- Produces: `offer.evaluations` and `offer.evaluationsLoading` props for `OfferDialog`.

- [ ] **Step 1: Extend offer state**

```js
const offer = reactive({
  visible: false,
  evaluationsLoading: false,
  evaluations: [],
})
```

- [ ] **Step 2: Load evaluations before showing the notification editor**

```js
async function loadOfferEvaluations() {
  offer.evaluationsLoading = true
  try {
    offer.evaluations = await getEvaluations({ applicationId: offer.row.applicationId, roundId: 2, department: offer.department })
  } finally {
    offer.evaluationsLoading = false
  }
}
```

- [ ] **Step 3: Pass evaluation props to OfferDialog**

```vue
<OfferDialog :evaluations="offer.evaluations" :evaluations-loading="offer.evaluationsLoading" />
```

- [ ] **Step 4: Run recruitment page tests**

Run: `npm run test:run -- src/views/recruitment/__tests__/recruitment.test.js`

Expected: PASS.

### Task 5: Verify the complete change

**Files:**
- Verify: `src/views/recruitment/**`

**Interfaces:**
- Consumes: completed components and page orchestration.
- Produces: validated production-ready recruitment flow.

- [ ] **Step 1: Run the complete test suite**

Run: `npm run test:run`

Expected: all tests pass.

- [ ] **Step 2: Run lint**

Run: `npm run lint`

Expected: exit code 0.

- [ ] **Step 3: Run production build**

Run: `npm run build`

Expected: exit code 0.

- [ ] **Step 4: Browser-check both workflows**

Open the management role recruitment page, verify the first-round table and feedback result buttons, then verify the second-round confirmation, decision tab, feedback tab, Pass/Out template switch, and send action.

- [ ] **Step 5: Commit implementation**

```bash
git add src/views/recruitment
git commit -m "feat: polish recruitment result dialogs"
```
