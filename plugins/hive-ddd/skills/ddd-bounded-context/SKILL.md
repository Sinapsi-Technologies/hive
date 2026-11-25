---
name: ddd-bounded-context
description: Use when drawing or auditing model boundaries — phrases like "bounded context", "where do we draw the line between X and Y", "this model is getting muddled", "should this be one service or two", "the same word means different things in different places". Defines what a bounded context is, what it isn't, and the signals that say a boundary should exist. Pairs with ddd-context-mapping for the relationships across boundaries.
---

## One-line summary

A bounded context is the explicit boundary within which a single model — and the ubiquitous language that names it — is consistent and complete.

## When to use this skill

- The same noun means two different things in different parts of the system ("customer" in sales vs "customer" in support).
- A model is growing tangled and individual changes ripple in unexpected directions.
- Team boundaries don't match code boundaries.
- The team is debating "should this be its own service?".

## When NOT to use this skill

- Within-context refactoring — delegate to tactical pattern skills.
- Questions about *how* contexts relate to each other → route to `ddd-context-mapping`.
- Allocating modeling effort across the business → route to `ddd-subdomain-classification`.

## Core content

A bounded context is conceptual, not deployment-bound. It is the scope within which a model holds together. It can be a module, a package, a microservice, or a whole system — what matters is that *inside* the boundary, every term has one meaning and every rule is consistent.

**Signals that a boundary is needed.**
- The same word means two different things in two different parts of the system.
- Two rules of the model contradict each other depending on which area of code is running.
- Two teams keep stepping on each other's changes to "the same" model.
- The UI for one user group looks nothing like the UI for another, but they hit the same domain code.

**Context vs microservice.** A bounded context is a *model* boundary; a microservice is a *deployment* boundary. They often coincide but don't have to. A single service can host two contexts (kept clean by module boundaries). Two services can implement the same context (rare, usually a mistake).

**Naming the boundary.** Give each context a name that reflects its business purpose: *Sales*, *Billing*, *Shipping*, *Identity*. Avoid technical names (*Backend*, *Core*). The name should make sense to a domain expert.

## Decision heuristics

- Prefer fewer, larger contexts at the start of a project. Splitting is cheap; merging is expensive.
- Split when terms collide irreconcilably, when teams need autonomy, or when the model rules contradict.
- Do not assume one bounded context per microservice. The mapping is decided by the model, not the deployment topology.

## Anti-patterns

- Declaring a bounded context per database table — performative, no model boundary actually drawn.
- Ignoring boundaries until the model breaks — by which point the cleanup is a major project.
- Treating "bounded context" as a synonym for "microservice".
- Drawing a boundary without naming the ubiquitous language inside it.

## See also

- `ddd-ubiquitous-language` — what lives inside the boundary.
- `ddd-context-mapping` — how contexts relate to each other.
- `ddd-subdomain-classification` — which contexts deserve the most effort.

## References

- Evans, *Domain-Driven Design* (2003), Ch. 14 (Maintaining Model Integrity).
