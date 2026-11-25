---
name: ddd-subdomain-classification
description: Use when deciding where to spend modeling effort — phrases like "core domain", "supporting subdomain", "generic subdomain", "what's worth building vs buying", "where should the senior engineers work", "is this our competitive edge or just plumbing". Distinguishes Core (competitive edge), Supporting (custom but not differentiating), and Generic (commodity, buy/library) subdomains.
---

## One-line summary

Classify each subdomain as Core, Supporting, or Generic to direct modeling effort at the parts of the business that actually differentiate it.

## When to use this skill

- Scoping a roadmap and deciding which areas deserve deep design.
- Choosing build vs buy for a chunk of functionality.
- Allocating senior engineers across teams.
- Killing scope on a project that is over-investing in plumbing.

## When NOT to use this skill

- Within-subdomain modeling questions → tactical pattern skills.
- Drawing context boundaries → `ddd-bounded-context`.

## Core content

Three categories, named for the role they play in the business:

**Core domain.** The part of the business that makes the company different from its competitors. The reason customers pick this product over the next one. Senior modeling effort goes here. Do not outsource it, do not template it, do not "just use a library".

**Supporting subdomain.** Necessary, custom-ish, but not what the company wins on. Build it, but with restraint — the goal is "adequate for our needs", not "best in class".

**Generic subdomain.** Commodity. Authentication, basic billing, address validation, file storage, search-against-keyword. Buy it (Stripe, Auth0, Algolia), use a library, or take an off-the-shelf framework. Custom-building a generic subdomain is one of the most expensive mistakes in software.

**The test.** "If this subdomain disappeared tomorrow, would customers notice and care, and would competitors do this better than us?" Yes / yes → core. No / no → generic. Anything in between → supporting.

## Decision heuristics

- Ask "could we use Stripe / Auth0 / a SaaS / a framework default?" — yes means generic; build the integration, not the thing.
- Ask "if customers picked us over a competitor, what is the one feature they cited?" — that is core; staff it accordingly.
- Re-classify on every major roadmap review. Subdomains migrate over time (a feature that was generic becomes core, or vice versa).

## Anti-patterns

- Treating every subdomain as core — no one gets senior attention; the actual core stays under-invested.
- Over-engineering a generic subdomain (rebuilding auth from scratch, building a custom email system, writing a new search engine).
- Under-investing in the actual core because it "feels boring" or because the team prefers to work on the shiny generic plumbing.
- Outsourcing the core to a vendor — the company loses the thing that distinguishes it.

## See also

- `ddd-bounded-context` — the unit being classified.
- `ddd-design` — design effort flows from this classification.

## References

- Evans, *Domain-Driven Design* (2003), Ch. 15 (Distillation).
