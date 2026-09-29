# Replace the product-demo video placeholder with the real walkthrough

## Goal
Swap the placeholder in the homepage's "See one master become a complete delivery pack." section for the real YouTube walkthrough video (https://youtu.be/cDliUz6duLM) so visitors can watch it directly on the page.

## Verified facts
- The link resolves to a real, public video: "EditForgeAi Demo Video 01" on the RahTheGamerX channel, ID `cDliUz6duLM`.
- The homepage section to change is the `#product-demo` section in `src/routes/index.tsx`, which currently shows a 16:9 placeholder panel with a disabled "Watch the 60-second walkthrough" button.

## Changes
1. **Embed the real video** in the `#product-demo` section of `src/routes/index.tsx`:
   - Replace the placeholder panel with a responsive 16:9 YouTube embed using the privacy-enhanced `youtube-nocookie.com` domain, `allowfullscreen`, and an accessible `title` ("EDITFORGE AI demo walkthrough").
   - Lazy-load the iframe (`loading="lazy"`) so it doesn't slow the page.
   - Remove the placeholder copy ("will appear here when the final recording is supplied") and the now-redundant disabled button, since the video itself is playable inline.
   - Keep the existing heading, "Product walkthrough" label, and the three outcome callouts unchanged.
2. **Update section copy**: change the small "60-second walkthrough placeholder" label to a caption that describes the video (e.g. watch one master become a full delivery pack) — no invented claims.
3. **Head metadata**: add the video thumbnail (`https://i.ytimg.com/vi/cDliUz6duLM/hqdefault.jpg`) as `og:image` and `twitter:image` on the homepage, since the page now renders this cover content.

## What stays untouched
- Hero, audio demo cards, FAQ, trust strip, footer, all other routes.
- No authentication, pricing, legal, or backend changes.

## Validation
- Check the build log for errors.
- Playwright: confirm the iframe renders in the `#product-demo` section on desktop and mobile viewports, no console errors, and the section still anchors correctly from "Watch how it works".
