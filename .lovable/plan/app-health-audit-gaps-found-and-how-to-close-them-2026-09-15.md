# App health audit — gaps found and how to close them

I reviewed the whole app: payments, credits, generation, retention, admin and database. Nothing is broken right now, and there is no way for anyone to get free credits or spend someone else's. The gaps below are about money going the wrong way in edge cases, and about jobs that can silently get stuck.

## What is already solid

- Credits can only be spent through the database itself, always checked against the real balance — no way to fake it from the browser.
- Admin actions all re-check that the caller is an admin, and every privileged action is logged.
- The scheduled clean-up endpoint rejects anyone without the secret, and deletes the right files.
- Payment messages are signature-checked and recorded once, so a repeat cannot double-credit.

## Issues to fix (highest impact first)

### 1. A refund or chargeback does not take anything back
If a customer is refunded or reverses a charge, the app never hears about it — they keep the plan and the credits. Same for a failed renewal payment: the plan can stay active indefinitely.

Fix: handle refund/adjustment, failed payment, paused and resumed events; revoke or downgrade accordingly, and log it.

### 2. A payment message can be lost forever
Right now a message is marked "handled" before it is actually applied. If applying it fails once, the payment provider retries, the app sees "already handled" and skips it — so that customer's plan or credits never arrive.

Fix: mark applied separately from received, and only skip messages that actually completed. Add an admin view of received-but-not-applied messages.

### 3. Changes made in the provider's own billing portal can be missed
If a customer cancels or changes plan through the hosted portal, the message may arrive without an identifying tag, and the app drops it silently. Their account then shows the old plan.

Fix: when the tag is missing, look the customer up by their stored provider customer/subscription ID instead of discarding the message.

### 4. Credits are charged before the work succeeds, with no refund
If a render fails, the export is marked failed but the credits stay spent. Users pay and get nothing.

Fix: automatically return the credits when a job fails, logged as a refund line in credit history, and do the same when an admin cancels a stuck job.

### 5. Jobs can get stuck forever
There are currently 7 jobs sitting at "queued" and 3 at "processing", the oldest since 19 August. Nothing ever times them out. Also, if the source audio fails to load, the whole run throws before any job is marked failed.

Fix: wrap the run so any early failure marks its jobs failed and refunds; add a timeout sweep (part of the existing daily job) that fails anything stuck beyond a threshold and refunds it; give users a retry button on failed exports. Clear the existing backlog as a one-off.

### 6. Lapsed plans are never demoted
The daily sweep only ends plans marked cancelled. A plan that simply stops paying stays on the paid tier.

Fix: also demote plans whose paid period ended and whose payment is failing.

### 7. The price-lookup endpoint is open to anyone
It requires no sign-in and forwards requests to the payment provider using the app's own keys. Someone could hammer it.

Fix: require a signed-in user, and cache results so repeated lookups do not hit the provider.

### 8. Out-of-order messages can overwrite newer data
Two small follow-up updates write without checking whether a newer message already landed.

Fix: only apply when the message is newer than what is stored.

### 9. Minor
- Failed file deletions during clean-up are ignored, so a file can survive while the record says it is gone — log and retry.
- Expiry warning emails are still switched off (no sender configured).

## Technical notes

- `src/lib/paddle-fulfillment.ts`: add cases for `adjustment.created/updated`, `transaction.payment_failed`, `subscription.past_due/paused/resumed`; add `resolveUserId(data)` fallback that queries `subscriptions.paddle_subscription_id` then `paddle_customers.customer_id` when `customData.userId` is absent; guard the trailing `.update()` calls with an event-time comparison.
- Migration: add `applied_at timestamptz` to `billing_events`; add `billing_refund_credits(_user_id, _amount, _reason)` and a `credit_refund` transaction reason; add `billing_revoke_for_refund(...)`. All service_role only.
- `src/routes/api/public/payments/webhook.ts`: skip only when `applied_at is not null`; set `applied_at` after `applyVerifiedEvent` succeeds; return non-2xx on apply failure so the provider retries.
- `src/lib/services/api.ts`: wrap `run`, `alternative`, `batchAlternate` in try/catch that marks jobs failed and calls the refund RPC; export a `retryExport` user-facing path.
- `src/routes/api/public/retention-sweep.ts`: add a stuck-job sweep (queued/processing older than 30 min → failed + refund) and extend the lapsed-plan query to `past_due` past period end.
- `src/utils/payments.functions.ts`: add `requireSupabaseAuth` to `resolvePaddlePrice` plus an in-memory price-ID cache.
- `src/routes/_authenticated/admin.tsx`: Webhooks tab gains an "unapplied" filter.
- One-off SQL to fail and refund the existing 10 stuck exports.

## Testing

1. Simulate a refund event in test mode → plan and credits are revoked, logged.
2. Simulate a failed renewal → account flagged past due, then demoted after period end.
3. Force a render failure → export shows failed and credits are returned in credit history.
4. Leave a job queued past the timeout → daily sweep fails and refunds it.
5. Replay a webhook twice → applied once only.
6. Call the price endpoint signed out → rejected.
7. Cancel via the hosted portal → account reflects it without a tag present.
