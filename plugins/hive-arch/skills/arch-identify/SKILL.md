---
name: arch-identify
description: Use when picking a software architecture style or pattern for a system — phrases like "what architecture should we use", "monolith vs microservices", "should we go event-driven", "is this a CQRS situation", "do we need a saga here", "should we use hexagonal architecture", "what's the right architectural style for this problem". Diagnostic mode that routes a stated problem to the relevant architecture skill or rules patterns out.
---

## One-line summary

Diagnose which architecture style or cross-cutting pattern fits a stated problem — or route to a simpler DDD / design-pattern skill when an architecture-level answer is overkill.

## When to use this skill

- Greenfield system, picking a macro architecture style (monolith / microservices / event-driven / serverless).
- Existing system facing scalability, integration, or transactional-consistency pressure and asking "what pattern fits".
- Architecture-review framing: "should we have used X instead of Y".

## When NOT to use this skill

- Domain modeling question → route to `ddd-design`.
- Code-level pattern question → route to `gof-identify`.
- Kotlin-specific pattern question → route to `kp-identify`.

## Core content

Walk the problem through these diagnostic questions, in this order:

**1. Is this about the system's macro shape?**
- *Default starting point, simple deployment, single codebase* → `arch-layered` and stay with a monolith. Don't reach for microservices until the monolith has a real seam.
- *Decouple domain core from frameworks / databases / IO so each can change independently* → `arch-hexagonal` (Ports & Adapters).
- *Loosely-coupled components communicating asynchronously through events* → `arch-event-driven`.
- *Independent deployable units, each owning its data, communicating over a network* → `arch-microservices` — but read the skill's "when NOT to" section first; it's the most over-applied style.

**2. Is this about data, transactions, or consistency across services / aggregates?**
- *Read load very different from write load; reads need denormalized views* → `arch-cqrs`.
- *Source-of-truth is "what happened", not "current state"; full audit history; temporal queries* → `arch-event-sourcing`. Pairs naturally with CQRS but doesn't require it.
- *A multi-step business transaction spans multiple services, no distributed transaction available* → `arch-saga`.

**3. Is this about failure handling between services?**
- *Downstream service degrades or fails; calling service must protect itself and recover gracefully* → `arch-circuit-breaker` (plus bulkhead and retry policies covered there).

**4. Is this about evolving a legacy system?**
- *Incremental migration from monolith to microservices, can't rewrite, must keep shipping* → `arch-strangler-fig`. Often combined with an Anticorruption Layer (see `ddd-context-mapping`).

**5. Is this about architecture quality and governance?**
- *How do we know the architecture is staying healthy as the code evolves* → `arch-fitness-functions`.
- *How do we record architectural decisions so future maintainers know why* → `arch-adr`.
- *Component-level design discipline (single responsibility, dependency direction, etc.)* → `arch-solid`.

**6. None of the above?**
- The problem may not need an architecture-level answer. Most often: a DDD modeling decision (`ddd-design`), a code-level pattern (`gof-identify`), or "just don't overengineer this — keep the monolith".

## Decision heuristics

- Default to *monolith first* until you have a concrete reason to distribute.
- Default to *layered* until you have a concrete reason to flip the dependency direction (which is what hexagonal does).
- Distribution is a cost, not a feature. Microservices, sagas, event-driven, CQRS all add operational complexity. Justify them with specific requirements.
- "We're building it that way because [respected company] does" is not a reason.

## Anti-patterns

- Picking microservices first and then trying to retrofit a domain model — leads to data-coupling and chatty services.
- Adopting CQRS because "it sounds advanced" with no actual read/write asymmetry — pure overhead.
- Reaching for sagas when a single transactional boundary would have done — the saga's compensating actions are real engineering cost.
- "Resume-driven development" — picking an architecture for its career signal rather than fit.

## See also

- All thirteen `arch-*` pattern skills.
- `ddd-design` and `ddd-bounded-context` — DDD often informs architecture-level decisions before any pattern is picked.
- `gof-identify` — when the question is code-level, not system-level.

## References

- Richards & Ford, *Fundamentals of Software Architecture* (O'Reilly, 2020) — Ch. 18, *Choosing the Appropriate Architecture Style*.
- Khononov, *Learning Domain-Driven Design* (O'Reilly, 2021) — Ch. 14–15, DDD ↔ microservices ↔ event-driven.
