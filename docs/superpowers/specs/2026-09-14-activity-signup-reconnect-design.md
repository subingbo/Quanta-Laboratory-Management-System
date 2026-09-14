# Uni-app Activity Signup Reconnection Design

## Goal

Reconnect freshman lecture and elite-sharing signup state and submission to the local Spring Boot backend after the new database patch restored the activity-signup API.

## Verified contract

- `GET /system/signup/detailList` now returns business code `200` for `qt_fresh` after applying `sql/lab/lab_patch_signup_remark.sql`.
- `POST /system/signup` binds the authenticated user, rejects duplicate signup, and creates an `APPLIED` record.
- Activity content continues to come from `GET /system/activity/list`.

## Frontend behavior

- `src/api/activity.ts` owns signup transport and maps backend rows to `LocalActivitySignup`.
- Both freshman activity pages load the selected activity first, then load the current user's signup for that activity.
- Submitting awaits the backend POST and reloads authoritative signup state before displaying success.
- Network or business errors remain visible to the user and never fall back to local signup storage.
- Elite-sharing guest cards remain mock-backed because no guest endpoint exists.

## Retained mocks

Workstation booking, applicant invitation response, elite-sharing guests, business cards, the home banner, member-side cancellation/return confirmation, clothing price/payment UI, signup counts, and activity time-of-day remain unchanged until their backend contracts are usable.

## Verification

- Unit-test signup mapping.
- Assert activity pages use the real signup API and no longer import local signup persistence helpers.
- Run the complete Vitest suite and the WeChat mini-program build.
- Recheck `GET /system/signup/detailList` against the running local backend.
