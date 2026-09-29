# EDITFORGE AI — New neon theme + logo, then the two open jobs

## 1. New look and feel

Take the colours from the uploaded artwork — electric cyan, neon magenta/violet, and a deep
blue-black backdrop — and blend them with the dark studio base the app already uses.

New palette (replacing the current amber/gold accent):

- Background: deep blue-black, slightly cooler than today
- Primary: electric cyan (buttons, active menu item, credit counter)
- Accent / secondary highlight: neon magenta-violet
- Signature gradient: cyan → violet (used on the logo tile, headings, progress bars, waveform)
- Success / warning / danger: kept, retuned so they sit naturally next to the new colours
- Waveform: cyan with violet peaks

Everything is defined once in the shared style file, so every screen, button, badge and dialog
picks up the new colours automatically. Light/dark contrast is checked so text stays readable.

## 2. Logo

Use the headphone-character mark from the uploads (no wording in the image). The transparent
version is cleanest, so it gets recoloured to the neon cyan/violet treatment seen in the first
image and saved as the app logo.

Where it appears:
- Sidebar top (replacing the current waveform icon tile)
- Mobile top bar
- Landing page and About page headers
- Browser tab icon
- Social share image stays as-is unless you want it regenerated

The wordmark "EDITFORGE AI" stays as text next to the mark.

## 3. Screens touched

Only colours, logo and small polish — no behaviour changes:
sidebar and top bar, dashboard, projects, upload, analysis, generate, exports, billing,
settings, admin, landing, about, sign-in.

## 4. Then: the two remaining jobs

**Daily cleanup runs automatically.** The cleanup job already exists but nothing triggers it.
Schedule it to run once a day so audio past its expiry date is removed while the analysis
details (BPM, key, loudness, structure) stay in the database forever.

**Expiry warning emails.** Build the 48-hour and 24-hour warning emails with the new branding,
wired into the daily job and recorded per project so nobody gets the same warning twice.
Sending stays switched off until a sender domain is connected — until then each warning is
logged so you can see exactly who would have been emailed.

## Technical notes

- Palette rewritten in `src/styles.css` (OKLCH tokens, `--gradient-forge`, `--shadow-glow`,
  chart and sidebar tokens). No hardcoded colours in components.
- Logo: recolour the transparent PNG upload, store via `lovable-assets`, import the pointer in
  `AppShell`, landing, about; replace `public/favicon.ico`.
- Scheduling: `pg_cron` + `pg_net` daily call to the existing
  `/api/public/retention-sweep` route with its shared-secret header.
- Emails: template + send helper behind an `EMAIL_ENABLED` flag; sweep marks
  `warned_48h_at` / `warned_24h_at` for idempotency.
