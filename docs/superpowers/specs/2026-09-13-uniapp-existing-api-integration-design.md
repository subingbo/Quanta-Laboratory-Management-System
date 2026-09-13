# Uni-app Existing API Integration Design

## Goal

Connect every Quanta uni-app business flow that already has a backend endpoint to the local Spring Boot service. Do not add backend endpoints or database tables in this change. Features without a usable endpoint remain visibly unavailable or read-only and are listed as follow-up gaps.

## Scope

### Recruitment

- Load the current user's application from `GET /qt/interview/my`.
- Submit or update an application through multipart `POST /qt/interview/apply`.
- Load interview progress from `GET /qt/interview/myResults` and map backend round and department values into the existing process timeline.
- Do not persist application or process state in uni storage.
- Keep second-interview accept/decline controls unavailable because no candidate-facing endpoint exists.

### Activities

- Load published activities from `GET /system/activity/list` and select the relevant `LECTURE` or `SHARING` activity.
- Load the current user's signups from `GET /system/signup/detailList`.
- Submit a signup through `POST /system/signup`.
- Preserve the current signup-state calculation and page appearance using backend data.
- Do not synthesize guest records when the backend returns none; the sharing-page guest section uses an explicit empty state.

### Member identity, directory, and home

- Load the authenticated profile from `GET /getInfo` and normalize the returned `SysUser` into the existing session profile.
- Load the member directory from `GET /qt/member/list`.
- Load member-home activity cards from `GET /system/activity/list`.
- Keep the decorative home banner local because it is a bundled presentation asset, not mutable business state.
- Disable business-card persistence and opening when no backend endpoint supplies card data.

### Workstations

- Load workstations from `GET /system/workstation/list`.
- Load reservations from `GET /system/reservation/detailList` and map overlaps into the three existing time slots.
- Create one backend reservation for every selected slot through `POST /system/reservation`.
- Reload server state after submission so the UI never treats local cache as authoritative.
- Do not offer self-service cancellation because the available generic update endpoint requires management permission.

### Library

- Load books from `GET /system/book/list`.
- Create borrow records through `POST /system/borrow`, calculating the due date with the existing client rule.
- Load the current user's records from `GET /system/borrow/detailList`.
- Do not offer member-side return confirmation because the available generic update endpoint requires management permission.

### Clothing

- Load the first active clothing item from `GET /system/item/list` and parse its color and size JSON fields.
- Create a server-side `DRAFT` order through `POST /system/order` from the current selection.
- Load the current user's orders from `GET /system/order/detailList`.
- Keep payment proof submission unavailable because the current page has no payment-upload interaction; the backend upload and submitted-order endpoints remain usable by a future UI change.

## Architecture

Create focused modules under `Quanta-uniapp/src/api/` for recruitment, activities, members, workstations, library, clothing, and services. Each module owns backend transport types and maps responses into the existing page view models. Pure filtering, date, validation, status, and display helpers stay in `src/utils`; local-storage-backed business stores are removed from page imports.

The shared request layer continues to add the bearer token and handle 401/403/business errors. It gains multipart upload support for the interview photo and an optional query type suitable for paginated list endpoints.

Pages retain their current layout. Each asynchronous write uses a submitting guard, shows the backend error message when available, and reloads authoritative data after success. Empty server datasets render existing or new compact empty states instead of falling back to mock records.

## Missing backend capabilities

The following are deliberately not implemented in this change:

- Candidate-facing accept or decline of a second-interview invitation.
- Activity guest/speaker records for the elite sharing page.
- Member business-card read and write, biography, active-talent flag, and card-availability flag.
- Member-home banner/content management.
- Member self-service workstation reservation cancellation.
- Member self-service book return confirmation.
- A dedicated member-facing notification feed contract for the home bell.

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
- Add source-level integration assertions that production pages no longer import business mock stores.
- Run `npm test` and `npm run build:mp-weixin`.
- Run relevant Maven tests and package the backend only if an existing backend issue must be fixed; no backend feature code is planned.
- With the local backend running, log in as `qt_fresh` and `qt_member`, exercise each available read/write flow, and confirm the requests reach port 8080.
