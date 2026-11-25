---
name: ddd-context-mapping
description: Use when mapping relationships between bounded contexts — phrases like "context map", "how do these two services talk", "should we use an anticorruption layer", "shared kernel vs separate ways", "upstream/downstream", "what integration pattern fits here". Catalogs the seven relationship patterns (Shared Kernel, Customer-Supplier, Conformist, Anticorruption Layer, Open Host Service, Published Language, Separate Ways) and the signals that pick one.
---

## One-line summary

A context map documents how bounded contexts relate, and picks an integration pattern that translates models across the seam.

## When to use this skill

- Integrating with another team's domain model.
- Deciding on an integration pattern for a new system-to-system seam.
- A legacy system the team must consume but cannot change.
- The user asks about *how* contexts talk, not *whether* they should be split.

## When NOT to use this skill

- Within-context modeling → `ddd-bounded-context` or tactical pattern skills.
- Drawing the boundary itself → `ddd-bounded-context`.
- Choosing what to invest in across subdomains → `ddd-subdomain-classification`.

## Core content

The seven patterns, with picking criteria:

**Shared Kernel.** Two contexts maintain a small, explicitly shared subset of the model (e.g., a tiny shared library of value objects). Cheap when the teams collaborate closely. Brittle when they don't — every change requires both teams' agreement.

**Customer–Supplier.** Downstream depends on upstream; upstream commits to prioritizing downstream's needs in its planning. Works when the upstream team is willing to be accountable to the downstream.

**Conformist.** Downstream accepts upstream's model unchanged and conforms to it. Use only when you have no influence on the upstream and conforming costs less than translating. It is a deliberate strategic choice, not laziness.

**Anticorruption Layer (ACL).** A translation layer the downstream owns, isolating its own model from a foreign or unstable upstream. Use when conforming would damage your model — typically when integrating with a legacy system or a third-party API.

**Open Host Service (OHS).** Upstream offers a stable protocol designed for multiple downstream consumers. Use when N+ downstreams need the same upstream — pays the cost of a stable interface once.

**Published Language.** A formal, well-documented interchange format (a schema, a protocol) that several contexts agree to. Often paired with Open Host Service. The format is part of the contract, not implementation detail.

**Separate Ways.** Two contexts refuse to integrate; small overlaps are duplicated. Use when integration cost exceeds duplication cost — the right answer more often than teams admit.

There is also an anti-pattern often included in the same catalog:

**Big Ball of Mud.** No boundaries at all. Recognize it, draw a context map around the mud to contain it, integrate into it (typically via an ACL), do not extend it.

## Decision heuristics

- Integrate against the upstream's *stable interface*, not its internals.
- Build an ACL whenever the upstream model is foreign, unstable, or owned by a team you can't influence.
- Separate Ways is a real option; the right answer is sometimes "we don't integrate".
- Conformist is a strategic choice. Choose it deliberately, not by default.

## Anti-patterns

- Calling the legacy system you depend on "the model" — it isn't, it's an upstream context.
- Building an ACL with no clear translation rules — it becomes a passthrough.
- Sharing a kernel between teams that don't collaborate; the kernel rots.
- Declaring "Open Host Service" without actually stabilizing the interface.

## See also

- `ddd-bounded-context` — where the boundary is, before mapping the relationships.
- `ddd-anti-patterns` — Big Ball of Mud and other integration anti-patterns.

## References

- Evans, *Domain-Driven Design* (2003), Ch. 14 (Maintaining Model Integrity).
