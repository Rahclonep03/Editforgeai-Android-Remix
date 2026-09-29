# Add user content ownership clause to Terms & Conditions

## Goal
Insert a new clause into `/terms` stating that users may only upload material they own or have rights to, and that EDITFORGE AI and its owners are not responsible for content uploaded without the user's ownership or rights.

## What will change

1. **Update `src/routes/terms.tsx`**
   - Add a new numbered section after the existing "Rights in audio and content" section (becoming section 4, and renumbering the following sections).
   - Section title: "User content and ownership".
   - Body points:
     - Users must only upload audio, masters, stems or other content that they created themselves, or for which they have obtained all necessary rights, licences, clearances and permissions.
     - Users may not upload content that infringes copyright, trade marks, publicity rights, privacy rights or any other third-party rights.
     - EDITFORGE AI, Cloneproof Ent and its owner are not responsible for, and do not endorse, any content uploaded by users.
     - Users bear full legal and financial responsibility for any claim, takedown notice, damages or penalty arising from content they upload without proper rights.
     - We reserve the right to remove infringing or unauthorised content and suspend or terminate the account of any user who repeatedly uploads material they do not own or have rights to use.
   - Renumber subsequent sections accordingly (Acceptable use becomes 5, Service availability becomes 6, etc.).

2. **Use existing constants**
   - Reference `SELLER_NAME` from `src/components/LegalLayout.tsx` for the ownership entity ("Cloneproof Ent, a sole proprietorship owned by Rahman Hameed du Plessis"), so the clause updates automatically if the seller name changes.

## Out of scope
- No changes to Refund Policy, Privacy Notice, or other legal pages.
- No changes to pricing, billing, or app functionality.

## Verification
- Typecheck the project.
- Build the project.
- Visit `/terms` and confirm the new clause renders correctly and section numbers are sequential.
