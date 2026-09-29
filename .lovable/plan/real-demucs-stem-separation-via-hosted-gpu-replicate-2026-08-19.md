# Real Demucs stem separation via hosted GPU (Replicate)

Everything runs over the internet from inside the app. No GPU box, no self-hosted Python service. The app sends the track to a hosted Demucs model, waits, and pulls the four real stems back into your storage.

## How it works

```text
Browser (Generate page)
  -> server function: submit stem job
       - signed URL for the uploaded track
       - creates Replicate prediction (htdemucs)
       - marks export rows "processing"
  -> server function: poll job status (client polls every few seconds)
       - on success: downloads the 4 stem files, uploads them to your
         audio bucket, sets file_path + status "complete" per export
       - on failure: status "failed" + message, credits refunded
```

Trims, stings, custom edits and the full mix keep using the existing fast in-browser renderer — only stem separation goes to the GPU.

## Stem names change

The four stem deliverables become Demucs' native output, which is the highest-quality result with no extra processing:

- Vocals
- Drums
- Bass
- Other

This replaces the current Vocals / Kick / Snare / Samples labels, and the phase-cancellation approximations are removed for these four. The "Instrumental" deliverable becomes a real instrumental (drums + bass + other summed) instead of a centre-cancel trick.

## Job behaviour

- Export rows show Queued -> Processing -> Complete, with live status in the Generate page and project workspace.
- If the GPU job errors, times out, or the Replicate account has no credit, the export flips to Failed with a clear message and the credits spent on that deliverable are refunded.
- No silent fallback to the old DSP approximations.

## What you need to provide

A Replicate account with billing enabled. I'll open the connector card in chat so you can link it — no key pasting into code.

## Technical notes

- New connector: Replicate, linked via `standard_connectors--connect`. Calls go through the Lovable connector gateway from server code only; keys never reach the browser.
- New files:
  - `src/lib/services/stems.functions.ts` — `submitStemJob` and `pollStemJob` server functions, both behind `requireSupabaseAuth`. They read `LOVABLE_API_KEY` / `LOVABLE_CONNECTOR_REPLICATE_API_KEY` inside the handler.
  - `src/lib/services/stems.server.ts` — Replicate create/poll helpers plus stem download and upload into the `audio` bucket via `supabaseAdmin`, verifying the caller owns the project first.
- Migration: add `provider_job_id` and `provider` columns to `exports` so a prediction can be resumed across page reloads; refund logic uses the existing credits column on `profiles`.
- `src/lib/services/types.ts`: rename the four stem types to `stem_vocals`, `stem_drums`, `stem_bass`, `stem_other` with updated labels/descriptions; keep `MAX_STEMS = 4`.
- `src/lib/services/render-engine.ts`: drop the DSP stem branches; keep trims, fades and WAV encoding.
- `src/lib/services/api.ts`: `GenerateApi.run` splits deliverables into local renders (unchanged path) and stem jobs (submit + poll loop driven from the client, updating the same `exports` rows).
- Stems come back as WAV and are stored at `{user}/{project}/{track}_stem_vocals.wav` etc., so the existing preview player and download links work unchanged.
- Replicate prediction URLs expire in ~1 hour, so stems are persisted to storage immediately on completion.
