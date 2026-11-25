---
name: arch-layered
description: Use when discussing layered architecture (n-tier) — phrases like "layered architecture", "n-tier", "presentation / business / persistence layers", "should we go layered", "leaky layer", "sinkhole anti-pattern", "open vs closed layers". Defines the layered style with its strict top-down dependency rule, the open/closed layer concept, and the most common failure modes.
---

## One-line summary

Organize the system into horizontal layers (typically Presentation → Business → Persistence → Database). Dependencies flow strictly downward; upper layers depend on lower layers, never the reverse.

## When to use this skill

- A simple monolith with clear UI / business-logic / data separation.
- A team's first server-side architecture — layered is the easiest mental model.
- A system where the dominant change axis is "swap the UI" or "swap the database" (within reason).
- When constraints (regulatory, team-size, deployment) make microservices or event-driven overkill.

## When NOT to use this skill

- The domain model is rich enough that the *business layer* becomes the bottleneck and should drive every other layer — consider `arch-hexagonal` (which inverts the dependency direction to keep the domain at the centre).
- The system genuinely needs independent scaling, deployment, or data ownership per business capability — see `arch-microservices`.
- The dominant communication pattern is asynchronous and event-shaped — see `arch-event-driven`.

## Core content

The canonical four layers (variations exist):

- **Presentation** — UI, API controllers, command-line handlers.
- **Business** — domain logic, use cases, services.
- **Persistence** — repositories, ORM, data access.
- **Database** — physical store.

The **dependency rule**: each layer can call the layer immediately below it. Skipping a layer is allowed by some teams (open layers), forbidden by others (closed layers). The convention should be explicit and documented.

**Open vs closed layers.** Closed layers force every call to go through the next layer down — clear but verbose. Open layers allow a layer to be bypassed when it adds no value (e.g., a thin "shared utilities" layer between two real ones). Pick a convention; don't mix.

**Why it's the default.** Layered architecture is easy to teach, easy to onboard onto, easy to reason about for CRUD-shaped systems. It's the right starting point unless you have a clear reason for something else.

**Where it breaks.** When the business layer starts depending on the persistence layer's *types* (e.g., your domain entity is a JPA `@Entity`), the abstraction has inverted — the database tail is wagging the domain dog. That's the moment to look at `arch-hexagonal`.

## Decision heuristics

- Start with layered for any new monolith. It's the boring choice and that's a feature.
- Closed layers by default; promote a layer to "open" only when bypassing it is genuinely cleaner.
- The business layer must not import from the presentation layer. Detect violations in CI (see `arch-fitness-functions`).
- If the business layer keeps needing infrastructure types (database connections, HTTP clients), the architecture is asking to flip — consider hexagonal.

## Anti-patterns

- **Sinkhole anti-pattern.** A layer that exists only to pass calls through to the layer below with no transformation. Indicates either: (1) the layer doesn't belong there, or (2) the layer should be opened. Common in over-engineered "service" layers.
- **Leaky layer.** A lower layer's types (JPA entities, HTTP DTOs) appearing in upper layers. Couples the upper layer to the lower's implementation.
- **Bidirectional dependencies.** A lower layer calling up into an upper one — usually via a callback or a circular import. Defeats the layering.
- **Layered architecture without an architecture.** Calling something "layered" because the folders are named `presentation/`, `business/`, `persistence/` but with no enforced dependency direction. Naming theatre.

## See also

- `arch-hexagonal` — when the domain should be at the centre, not at a layer.
- `arch-fitness-functions` — enforcing layer boundaries in CI.
- `ddd-anti-patterns` — anemic domain model is a sign of business logic leaking into presentation/persistence.

## References

- Richards & Ford, *Fundamentals of Software Architecture*, Ch. 10 (Layered Architecture Style).
- Buschmann et al., *Pattern-Oriented Software Architecture, Vol. 1* — original layered-pattern reference.
