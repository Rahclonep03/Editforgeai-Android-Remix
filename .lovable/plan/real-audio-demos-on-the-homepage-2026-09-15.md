# Real audio demos on the homepage

Replace the three "Demo placeholder" cards with real, playable clips made by EDITFORGE from one of your own masters.

## Source track

Your project **"-iR&B inspired Rah- by rahclonep"** (6:29 master, analysed 14 Sep) already has finished 60s, 30s and 15s edits. Those three files become the homepage demos.

## What changes on the homepage

- Each of the three cards gets a working play/pause button with real audio, a live progress indicator moving across the waveform, and the true clip length.
- The waveform drawn on each card is generated from the actual edit, so what you see matches what you hear.
- Only one clip plays at a time; starting another stops the first. Nothing autoplays.
- The "Demo placeholder" badges and the "coming soon" buttons are removed. The credit line becomes "Demo edits created with EDITFORGE from an original master, used with permission."
- The editing rationale text under each card stays, lightly adjusted to describe this track.

## How the audio gets published

Your project audio lives in private storage behind expiring links, which can't be used on a public page. So the three edits are exported once, converted to compressed MP3 (fast loading, roughly a few hundred KB each), and published to the site's permanent asset storage. The originals in your account are untouched.

Files are loaded only when a visitor presses play, so the page stays fast.

## Not included

- The product-walkthrough video placeholder stays as it is — that needs a recording from you.
- No pricing, account, payment or legal changes.

## Technical details

- Pull the three `exports.file_path` WAVs for project `5060505c-9724-4cb9-90af-40905092b7aa` (types `edit_60`, `edit_30`, `edit_15`) from storage with service-role access into `/tmp`.
- Transcode each to MP3 (ffmpeg, mono-safe stereo, ~128 kbps) and compute a fixed-length peak array per clip with ffmpeg/python; store peaks inline in the route module.
- Upload the three MP3s via `lovable-assets create`, commit the `.asset.json` pointers under `src/assets/demos/`, and reference `asset.url` in the homepage.
- New `src/components/AudioDemoCard.tsx`: single `<audio preload="none">` per card, shared "currently playing" state lifted into the demos section, `timeupdate`-driven progress passed into the existing `Waveform` component, accessible play/pause labels, and no motion when `prefers-reduced-motion` is set.
- Keep existing design tokens, card layout and head/JSON-LD metadata in `src/routes/index.tsx` unchanged apart from the card contents.
