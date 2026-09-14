# Uni-app Activity Signup Reconnection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the remaining local activity-signup persistence with the now-working authenticated backend endpoints.

**Architecture:** Extend the existing activity API module with pure signup mapping plus GET/POST transport. Keep page presentation intact and let server state remain authoritative.

**Tech Stack:** Vue 3, uni-app, TypeScript, Vitest, Spring Boot HTTP API

**Spec:** `docs/superpowers/specs/2026-09-14-activity-signup-reconnect-design.md`

## Global Constraints

- Do not change page layout.
- Do not fall back to local signup storage after backend errors.
- Preserve `Quanta-uniapp/src/manifest.json` and `.local/`.
- Keep all other confirmed missing capabilities mocked.

---

### Task 1: Restore activity signup transport

**Files:**
- Modify: `Quanta-uniapp/src/api/activity.ts`
- Modify: `Quanta-uniapp/src/api/activity.test.ts`

**Interfaces:**
- Produces: `mapSignup(row)`, `getMyActivitySignup(activityId)`, `signupActivity(activityId, remark?)`.
- Consumes: `GET /system/signup/detailList` and `POST /system/signup`.

- [ ] Add a failing test that maps an `APPLIED` backend row into `LocalActivitySignup`.
- [ ] Implement the mapper and authenticated GET/POST calls.
- [ ] Run `npm test -- src/api/activity.test.ts` and confirm it passes.

### Task 2: Switch both pages and update the gap report

**Files:**
- Modify: `Quanta-uniapp/src/pages/freshman/talk/talk.vue`
- Modify: `Quanta-uniapp/src/pages/freshman/elite-share/elite-share.vue`
- Modify: `Quanta-uniapp/src/api/backendIntegration.test.ts`
- Modify: `docs/前端对接说明.md`

**Interfaces:**
- Consumes: `getMyActivitySignup(activityId)` and `signupActivity(activityId, remark?)`.

- [ ] Replace local signup reads/writes in both pages with the real API functions.
- [ ] Update the integration guard to reject local signup persistence imports.
- [ ] Move activity signup from the missing list to the real-interface list in the original integration document.
- [ ] Run `npm test` and `npm run build:mp-weixin`.
- [ ] Commit only intended files, leaving `manifest.json` and `.local/` untouched.
