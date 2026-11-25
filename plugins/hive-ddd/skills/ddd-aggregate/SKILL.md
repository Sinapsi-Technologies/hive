---
name: ddd-aggregate
description: Use when designing or critiquing aggregate boundaries — phrases like "aggregate", "aggregate root", "should these be one entity or two", "consistency boundary", "transactional consistency", "this aggregate is huge", "should I reference by ID or by object". Defines aggregates as transactional consistency boundaries and the rules for entity references within and across them.
---

## One-line summary

An aggregate is a cluster of entities and value objects treated as a single unit for data changes — a transactional consistency boundary with one root entity that owns it.

## When to use this skill

- Deciding which entities belong together as one unit.
- Choosing between "one big object" and "several related objects".
- Reference rules: "can `Order` hold a `Customer` object, or just a `customerId`?".
- Performance and locking concerns that trace back to aggregate size.

## When NOT to use this skill

- Within a single entity's behavior — that is `ddd-entity` territory.
- Cross-context relationships → `ddd-context-mapping`.
- Eventual consistency across aggregates via events → `ddd-domain-event`.

## Core content

An aggregate has one *root* entity. External code may hold a reference only to the root. Objects inside the aggregate may reference each other freely. Other aggregates are referenced *only by ID*, never by object reference.

Within one aggregate, invariants are enforced synchronously and transactionally. If a rule must always hold across two entities, those entities belong to the same aggregate. If the rule can lag (eventual consistency is acceptable), the entities can live in separate aggregates and coordinate through domain events.

**One transaction touches one aggregate.** This is the canonical rule. Loading two aggregates and modifying both in one transaction is a smell — it usually means the aggregate boundaries are wrong, or the work should be split into a command + an event-driven follow-up.

The aggregate root is the only entry point. Code outside the aggregate calls methods on the root; the root delegates to internal entities as needed. The root is responsible for ensuring the aggregate's invariants hold after every operation.

## Decision heuristics

- Prefer small aggregates over large ones. When unsure, split.
- If two entities don't need to change in the same transaction, they belong to different aggregates.
- If two entities share an invariant that must always hold synchronously, they belong to the same aggregate.
- Reference *other* aggregates by ID only. Holding a `Customer` object reference inside `Order` forces eager loading and transactional headaches.

## Anti-patterns

- God aggregates that own half the domain model — every change loads the world, locks become contention, the database transaction balloons.
- Cross-aggregate object references — pull half the model into memory and tangle persistence.
- Reaching into an aggregate's internals by passing the root around and letting callers traverse its children.
- "Repository per entity" instead of "repository per aggregate root" — exposes aggregate internals to callers.

## See also

- `ddd-entity` — entities are the building blocks of aggregates.
- `ddd-value-object` — value objects nest freely inside aggregates.
- `ddd-repository` — one repository per aggregate root.
- `ddd-domain-event` — cross-aggregate consistency goes through events.
- `ddd-anti-patterns` — god aggregates and cross-aggregate references.

## References

- Evans, *Domain-Driven Design* (2003), Ch. 6 (The Life Cycle of a Domain Object).
