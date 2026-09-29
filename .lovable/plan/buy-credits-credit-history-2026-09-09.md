# Buy credits + credit history

Add a place in the account area to buy credit packs (simulated payment for now, no card step) and a history page that lists every credit added and spent.

## What you'll get

**Buy Credits page**
- Three packs: 25, 100 and 300 credits, each with a price shown.
- Pick a pack, confirm, and the credits land in your balance immediately.
- The counter in the top bar updates straight away.
- A clear note that payment is simulated while card payments are being set up.

**Credit History page**
- A table of every credit movement, newest first: date, what happened, amount (+ or -), and the balance after it.
- Entries cover pack purchases, plan changes that grant credits, unlocking a project's Core Bundle, single generations, alternates and batch alternates.
- Empty state when nothing has happened yet.

**Where they live**
- Both reachable from Billing, plus a "Credits" item in the side navigation.
- The existing "Top-Up Required" popup (shown when you try to analyse with fewer than 3 credits) will link straight to the Buy Credits page.

## Technical notes

Database (one migration):
- New table `public.credit_transactions`: `user_id`, `amount` (positive for added, negative for spent), `reason` (enum-style text: `purchase`, `plan_grant`, `core_bundle`, `generation`, `alternative`, `batch_alternate`), `description`, `project_id` (nullable), `balance_after`, `created_at`. GRANTs for `authenticated` (select/insert) and `service_role`; RLS scoped to `auth.uid() = user_id`, admins can read via `private.has_role`.
- New `purchase_credits(_amount int, _description text)` security-definer RPC: adds credits to `profiles.credit_balance` (and legacy `credits`), writes a ledger row, returns the new balance — atomic.
- Extend the existing `spend_credits`, `unlock_core_bundle` and `consume_bundle_alternate` functions to write a ledger row with the reason and resulting balance, so history is complete without duplicating logic in the frontend.

Frontend:
- `AccountApi.purchaseCredits(pack)` and `AccountApi.creditHistory()` in `src/lib/services/api.ts`; `CreditTransaction` type in `src/lib/services/types.ts`; pack definitions in `src/lib/services/credits.ts`.
- New routes `src/routes/_authenticated/credits.tsx` (buy) and `src/routes/_authenticated/credits.history.tsx` (history), following the existing AppShell + TanStack Query patterns, with their own `head()` metadata.
- Nav entry in `src/components/AppShell.tsx`; link from `src/components/CreditDialogs.tsx` top-up dialog and from Billing.
- Purchases use the existing confirmation-dialog pattern; success and failure use the existing toast/state handling. No fake success on failure.

No payment provider is wired up in this step; `purchaseCredits` is the single place to swap in real checkout later.
