# Admin dashboard expansion plan

## Goal
Turn the existing read-only `/admin` page into an operational control centre where you (and future staff admins) can diagnose and fix common problems without touching the database or code.

## What already exists
- `/admin` route with basic stats (users, projects, exports) and recent lists.
- `AccountApi.isAdmin()` checks the `user_roles` table for the `admin` role.
- `AdminApi.overview()` reads `profiles`, `projects`, `exports`.
- Database already has: `exports` (status, error, provider_job_id), `billing_events`, `credit_transactions`, `subscriptions`, `profiles`, `projects` with expiry/retention fields, `user_roles`.
- Admin read access is already allowed via `private.has_role(auth.uid(), 'admin')` on most tables.

## What we will build

### 1. Admin access model
- Keep the existing `admin` role as the single privileged role.
- Add an admin action to promote another signed-in user to `admin` by email, gated so it can only be run by an existing admin.
- No separate "support" role for this phase; all controls live under `admin`.

### 2. Job control panel
- List exports with filters: all / queued / processing / complete / failed.
- Show key fields: user email, project, type, status, error, created/updated time.
- **Retry failed job**: re-queue a failed export, clear `error`, set status back to `queued`.
- **Cancel stuck job**: mark a long-running `processing` export as `failed` with a reason so the user sees a clean state.
- Server functions use the service-role client because admins act on other users’ data.

### 3. Webhook event log
- List `billing_events` with environment, event type, status (success/failed/pending), price, amount, timestamp.
- Show raw payload on demand (collapsed).
- **Replay failed event**: for events that failed to apply, re-run the same handler logic idempotently so duplicate processing cannot double-credit.

### 4. User management
- Searchable user list with email, display name, plan, credits, created date.
- Drill-down to a user detail view:
  - profile + subscription status
  - recent projects
  - recent exports
  - credit history
- **Adjust credits**: add or remove credits with a reason, logged to `credit_transactions`.
- **Extend retention**: push a project’s `expires_at` forward by a chosen number of days.
- **Manage subscription**: view Paddle subscription row; link to Paddle dashboard for manual changes (no direct mutation of provider state from the app).

### 5. Credit & payment audit
- Combined view of `credit_transactions` and `billing_events` for a user or globally.
- Filters: date range, type, environment.
- Export to CSV (optional, if low effort).

### 6. Navigation
- Add an "Admin" link to the app shell for users with the admin role.
- Keep the route under `/_authenticated/admin`.

## Files to create / change
- `src/lib/services/api.ts`: expand `AdminApi` with server-function wrappers for job retry/cancel, webhook replay, user search/detail, credit adjustment, retention extension.
- `src/utils/admin.functions.ts` (new): server functions using `requireSupabaseAuth` + admin role check, privileged DB writes via `supabaseAdmin`.
- `src/routes/_authenticated/admin.tsx`: expand into tabbed sections (Overview, Jobs, Webhooks, Users, Audit).
- `src/components/AppShell.tsx`: conditionally show Admin link for admins.
- Database: no new tables required; may add a small migration if we need an indexed `admin_actions` audit log for manual credit/retention changes (optional, can reuse `credit_transactions` for credits).

## Out of scope
- Real-time notifications or Slack alerts.
- Impersonating a user’s session.
- Directly cancelling Paddle subscriptions from the app (Paddle portal is the source of truth).

## Testing plan (preview)
1. Sign in as an admin, open `/admin`, confirm tabs load.
2. Upload and analyse a track, then trigger a generation; mark one export `failed` in the DB and use Retry to re-queue it.
3. Use Cancel on a fake `processing` row older than 10 minutes.
4. Send a test Paddle webhook with a bad signature and confirm it appears in the webhook log; replay a verified event and confirm idempotency (no duplicate billing event row).
5. Pick a non-admin test user, grant 10 credits, verify balance and `credit_transactions`; then deduct 5 and verify.
6. Extend a project’s expiry and confirm the new date appears in the vault.
7. Promote a second account to admin and confirm the Admin link appears for that account.

## Risks / guardrails
- All admin server functions must re-verify the caller is an admin on every call, not just the UI.
- Credit adjustments must be atomic and logged; never allow negative balances.
- Webhook replay must be idempotent (keyed on `provider_event_id`).
- Retention extension only moves `expires_at` forward, never backward.
