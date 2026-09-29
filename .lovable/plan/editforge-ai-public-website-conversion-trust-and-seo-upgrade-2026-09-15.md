# EDITFORGE AI public website conversion, trust, and SEO upgrade

## Goal
Refocus the public website on independent producers and artists who want a complete, release-ready delivery pack from one finished master. Keep labels and managers as a secondary audience, preserve the existing dark navy/electric-pink waveform identity, and leave authentication, account functionality, pricing behavior, and legal wording intact.

## Homepage
- Refine the opening section around the single H1: **“One master in. Every release-ready version out.”**
- Add accurate supporting copy for WAV/MP3 uploads and the enabled outputs: full mix, 60s, 30s, 15s, sting, and custom edits.
- Keep **“Start free — 25 credits”** as the main action and add **“Watch how it works”** as an accessible in-page jump to the product demo.
- Keep the existing waveform-led product signal, but make its data deterministic to avoid server/browser rendering mismatches.
- Add the free-credit explanation and a real `/pricing` link labelled **“See exactly what credits cover.”**

## Tangible demo sections
- Place **“Hear the difference”** immediately after the opening section.
- Build three responsive audio-demo cards for the 60s radio, 30s hook-first, and 15s social edits, each with a waveform, duration, editing rationale, and play control.
- Because no licensed audio files are present, label every item clearly as a demo placeholder and keep playback controls honestly unavailable rather than simulating audio. Include **“Demo tracks are used with permission.”**
- Add the anchored product-demo section, **“See one master become a complete delivery pack.”**
- Use a polished 16:9 video placeholder with a clearly labelled unavailable walkthrough action until a real video exists, followed by the three requested outcome callouts: upload, review structure, export every version.

## Offer, audience, and trust
- Add **“What you get from one master”** with the six enabled delivery types.
- Explain the 3-credit Core Bundle accurately: analysis plus first 60s, 30s, and 15s edits; avoid claiming full mix, sting, or custom edit are included in those 3 credits.
- Add: **“25 free credits = enough to try several complete edit bundles. No card required.”**
- Reorder audience messaging so independent artists and producers lead, while labels/managers remain secondary.
- Add **“Built for the people who ship music”** with explicitly labelled Artist, Producer, and Label testimonial placeholders under an Early access/Beta treatment. No fabricated quotes, brands, numbers, or endorsements.
- Add a **“Your music stays yours”** trust strip using verified ownership, private-storage, and no-third-party-model-training language, linking to the existing Privacy Notice and Terms.

## FAQ and support
- Add an indexable homepage FAQ covering the seven requested questions.
- Use verified answers: WAV/MP3 up to 200 MB; users retain ownership; uploads are not used to train third-party models; cuts use analysed song structure and quiet musical boundaries; edits can be previewed; audio retention is 7/40/120 days by plan while analysis metadata remains; the Core Bundle costs 3 credits and additional generations generally cost 1 credit.
- Replace the public support address sitewide with **support@editforgeai.com**, including contact references displayed through the existing legal layout, without otherwise rewriting legal content.
- Add **Security & Rights** to the footer as a real internal link to `/terms`, as requested, and enlarge footer link targets for mobile accessibility.

## About and Pricing
- Preserve each page’s existing purpose and single H1.
- Refine their page titles and descriptions for search intent around AI music editing, release-ready audio versions, plan pricing, credits, and file retention.
- Keep all current pricing cards, prices, billing behavior, sign-in paths, and legal routes unchanged.

## SEO and structured data
- Add self-referencing canonical URLs for Home, About, and Pricing using `https://editforgeai.com`.
- Add unique Open Graph URL/title/description and Twitter metadata to those pages.
- Add `SoftwareApplication` structured data on Home and `Organization` structured data for EDITFORGE AI / Cloneproof Ent, using only verified facts and existing public URLs.
- Keep the homepage FAQ in rendered HTML for indexing; no separate `/faq` route is needed.
- Do not add unverified claims about speed, accuracy, adoption, security certification, or customers.

## Accessibility and validation
- Use semantic sections, headings, buttons, and links; preserve one H1 per page.
- Add visible keyboard focus, descriptive labels, disabled-state explanations for unavailable media, strong contrast, and stable responsive dimensions for waveforms/video.
- Respect reduced-motion preferences and avoid autoplay.
- Check Home, About, and Pricing at desktop and mobile sizes; verify in-page scrolling, keyboard use, no overflow/overlap, correct metadata/structured data, real internal links, and a clean runtime/build state.

## Technical details
- Reuse the existing `Waveform`, `Button`, design tokens, logo, and footer patterns; introduce small focused public-site components only where repetition warrants it.
- Keep all public visual colors token-based in the global design system.
- Do not modify authentication logic, protected account pages, payment checkout behavior, pricing data, database schema, or legal policy wording beyond the approved support-email substitution.
