# Uni-app Existing API Integration Design

## Goal

Connect every Quanta uni-app business flow that already has a backend endpoint to the local Spring Boot service. Do not add backend endpoints or database tables in this change. Features without a usable endpoint keep their current mock-backed behavior and are listed as follow-up gaps.

## Scope

### Recruitment

- Load the current user's application from `GET /qt/interview/my`.
- Submit or update an application through multipart `POST /qt/interview/apply`.
- Load interview progress from `GET /qt/interview/myResults` and map backend round and department values into the existing process timeline.
- Do not persist application or process state in uni storage.
- Keep the current mock-backed second-interview accept/decline controls because no candidate-facing endpoint exists. Server results remain authoritative and the local choice only affects the unsupported invitation response state.

### Activities

- Load published activities from `GET /system/activity/list` and select the relevant `LECTURE` or `SHARING` activity.
- Load signup state from `GET /system/signup/detailList` and submit through `POST /system/signup`; the missing `qt_activity_signup.remark` column was repaired by the 2026-09-14 backend patch.
- Keep the current mock guest records because no activity guest endpoint exists.

### Member identity, directory, and home

- Load the authenticated profile from `GET /getInfo` and normalize the returned `SysUser` into the existing session profile.
- Load the member directory from `GET /qt/member/list`.
- Load member-home activity cards from `GET /system/activity/list`.
- Keep the decorative home banner local because it is a bundled presentation asset, not mutable business state.
- Keep the current local business-card persistence and opening behavior because no backend endpoint supplies card data.

### Notifications

- Load the five most recent notices and unread count from `GET /system/notice/listTop` for the member-home bell and member function page.
- Mark a notice as read through `POST /system/notice/markRead` when the user opens it.
- Keep notice presentation inside the existing pages; no new navigation hierarchy is required.

### Workstations

- Keep the workstation booking page mock-backed. A normal member can only list their own reservations, so the client cannot determine which stations are occupied by others.
- The reservation contract also serializes `reserveStart` and `reserveEnd` as date-only values, so it cannot represent the page's morning, afternoon, and evening slots safely.

### Library

- Load books from `GET /system/book/list`.
- Create borrow records through `POST /system/borrow`, calculating the due date with the existing client rule.
- Load the current user's records from `GET /system/borrow/detailList`.
- Keep the current mock-backed member-side return confirmation behavior because the available generic update endpoint requires management permission. Real borrow records remain unchanged on the server.

### Clothing

- Load the first active clothing item from `GET /system/item/list` and parse its color and size JSON fields.
- Create a server-side `DRAFT` order through `POST /system/order` from the current selection.
- Load the current user's orders from `GET /system/order/detailList`.
- Keep payment proof submission unavailable because the current page has no payment-upload interaction; the backend upload and submitted-order endpoints remain usable by a future UI change.

## Architecture

Create focused modules under `Quanta-uniapp/src/api/` for recruitment, activities, members, workstations, library, clothing, and services. Each module owns backend transport types and maps responses into the existing page view models. Pure filtering, date, validation, status, and display helpers stay in `src/utils`; local-storage-backed business stores are removed from page imports.

The shared request layer continues to add the bearer token and handle 401/403/business errors. It gains multipart upload support for the interview photo and an optional query type suitable for paginated list endpoints.

Pages retain their current layout. Each asynchronous write uses a submitting guard, shows the backend error message when available, and reloads authoritative data after success. Existing endpoints never fall back to mock data; mocks remain only for the explicitly listed capabilities that have no usable endpoint.

## Missing backend capabilities

The following keep their existing mock-backed behavior and are deliberately not implemented on the backend in this change:

- Candidate-facing accept or decline of a second-interview invitation.
- Activity guest/speaker records for the elite sharing page.
- Member business-card read and write, biography, active-talent flag, and card-availability flag.
- Member-home banner/content management.
- Workstation availability and booking until the member-visible occupancy and time-slot contract is complete.
- Member self-service workstation reservation cancellation.
- Member self-service book return confirmation.

## Data and compatibility rules

- Department values map between backend codes (`PRODUCT`, `DESIGN`, `FRONTEND`, `BACKEND`, `ANDROID`) and the existing Chinese labels.
- Gender values map between backend codes (`0`, `1`, `2`) and Chinese labels.
- Relative backend image paths are resolved against `VITE_API_BASE_URL`; existing absolute URLs and bundled `/static/` assets are preserved.
- Paginated calls request a sufficiently large explicit page size where the current UI has no server pagination control.
- `VITE_USE_MOCK` remains a login-only development switch; business modules always call the configured backend.
- Existing user changes in `Quanta-uniapp/src/manifest.json` and `.local/` are not modified.

## Testing and verification

- Add unit tests for every backend-to-view-model mapper and status conversion.
- Keep existing pure-rule tests passing.
- Add source-level integration assertions that server-backed operations no longer call mock stores; imports are allowed only for the explicitly listed missing capabilities.
- Run `npm test` and `npm run build:mp-weixin`.
- Run relevant Maven tests and package the backend only if an existing backend issue must be fixed; no backend feature code is planned.
- With the local backend running, log in as `qt_fresh` and `qt_member`, exercise each available read/write flow, and confirm the requests reach port 8080.
