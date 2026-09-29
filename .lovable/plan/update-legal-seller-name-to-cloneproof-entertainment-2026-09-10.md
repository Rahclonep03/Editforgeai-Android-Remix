# Update legal seller name to Cloneproof Entertainment

## Goal
Replace the placeholder seller identity on the public legal pages with the user's actual business identity: **Cloneproof Entertainment**, a sole proprietorship owned by Rahman Hameed du Plessis. Keep the product brand (EDITFORGE AI) in the footer copyright.

## What will change

1. **Seller name constant**
   - In `src/components/LegalLayout.tsx`, change `SELLER_NAME` from the placeholder to:
     `Cloneproof Entertainment, a sole proprietorship owned by Rahman Hameed du Plessis`
   - This single constant feeds `src/routes/terms.tsx`, `src/routes/refunds.tsx`, and `src/routes/privacy.tsx`, so all three pages update automatically.

2. **Search for stray references**
   - Search the codebase for the old placeholder seller string and any other hard-coded seller names (e.g. in `src/routes/about.tsx`, `src/routes/index.tsx`, `src/routes/_authenticated/billing.tsx`, `src/routes/_authenticated/credits.tsx`).
   - Replace any that represent the legal seller with the new constant or wording. Leave product/brand references as EDITFORGE AI.

3. **Footer copyright**
   - Keep `© {new Date().getFullYear()} EDITFORGE AI` in `LegalFooter`. No change.

4. **Verification**
   - Run `bunx tsgo --noEmit` to confirm type safety.
   - Run the production build check.
   - Visit `/terms`, `/refunds`, and `/privacy` to confirm the new seller name renders correctly.

## Out of scope
- No changes to refund policy wording, Paddle disclosures, support email, or business logic.
- No live payment gateway activation; that remains pending Paddle seller verification after the legal name is correct.

## How to test in the preview
1. Open the preview and navigate to `/terms`, `/refunds`, and `/privacy`.
2. Confirm each page identifies the seller as **Cloneproof Entertainment, a sole proprietorship owned by Rahman Hameed du Plessis**.
3. Confirm the footer still shows `© 2026 EDITFORGE AI`.
4. Confirm no build/typecheck errors.