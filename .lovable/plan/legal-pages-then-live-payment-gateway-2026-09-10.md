# Legal pages, then live payment gateway

Two stages: publish the policies the payment provider requires, then switch the checkout from test mode to real cards.

## Stage 1 — Legal pages

Three new public pages, linked from the footer and from the checkout areas (Billing and Buy Credits):

**Terms & Conditions** (`/terms`)
- Your legal name as the seller, and that the customer contracts with you.
- Acceptance by continued use; account accuracy and credential responsibility.
- What EDITFORGE AI provides: analysis and generated audio deliverables, credits, tier retention windows (7 / 40 / 120 days).
- Acceptable use: no unlawful use, fraud, infringing uploads, scraping or interference. You must own or have rights to the audio you upload.
- Ownership of the platform stays with you; customers keep rights to their own audio and their generated deliverables.
- Files are deleted at the retention date — downloading in time is the customer's responsibility.
- No guarantee of uninterrupted or error-free service; generated output may vary.
- Card security clause, in your words: the cardholder is responsible for keeping their full card details private, including CVV and expiry, and for having their own bank verification in place to confirm or decline transactions made with their card.
- Payment and billing handled by Paddle, with the required reseller wording: "Our order process is conducted by our online reseller Paddle.com. Paddle.com is the Merchant of Record for all our orders. Paddle provides all customer service inquiries and handles returns."
- Suspension or termination for breach, non-payment or fraud risk.
- Liability cap, warranty disclaimer, governing law (South Africa).

**Refund Policy** (`/refunds`)
- 14-day window from the order date for unused credits and unused subscription time.
- Credits already spent on analysis or generated deliverables are not refundable, since the work has been delivered.
- Refunds are handled by Paddle at paddle.net, plus your support email.
- Cancelling keeps access until the paid period ends; no partial-period refunds.

**Privacy Notice** (`/privacy`)
- Your legal name as data controller.
- Data collected: email and login details, uploaded audio and its analysis metadata, generated files, credit and payment records, basic usage and device data.
- Purposes and legal bases: running the account and service, processing payments, security and fraud prevention, support, service improvement.
- Sharing: hosting and backend provider, Paddle as merchant of record for payments, tax and invoicing, authorities where legally required.
- Retention: audio deleted at the project expiry date; analysis metadata and billing records kept longer for accounts and legal reasons.
- User rights under POPIA/GDPR, security measures, cookie note, and your support email for requests.

Contact email used across all three: Rahclonep06@gmail.com

## Stage 2 — Real payment gateway

The checkout, webhook and credit granting are already built and working in test mode; going live is configuration plus a few connections:

- Add checkboxes/links so the legal pages are visible at checkout, as the provider's review requires.
- Confirm the live products and prices exist (Pro $29/mo, Premier $99/mo, credit packs 15 / 50 / 150) and that the live webhook is registered.
- Verify the live webhook signature secret is in place so real payments grant credits and tiers.
- The test-mode strip disappears automatically once the live token is in use on the published site.

Then, in the payments area, you complete: identity verification, business details and payout account, and submit the site for provider review. That approval step is theirs, not something I can do from here.

## Testing

Before going live: run a test purchase of Pro and a credit pack with card 4242 4242 4242 4242, confirm the tier and credits land, then cancel from the portal and confirm access stays until the period end.

## Note

These pages are written to meet the payment provider's requirements. Review the wording once they're up and tell me anything to adjust — the only parts that can't be removed are the merchant-of-record disclosure and the minimum 14-day refund window.
