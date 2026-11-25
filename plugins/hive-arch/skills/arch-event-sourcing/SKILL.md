---
name: arch-event-sourcing
description: Use when persisting state as a sequence of events — phrases like "event sourcing", "event store", "append-only log of facts", "replay events to rebuild state", "audit log as source of truth", "temporal queries", "snapshot", "should we event-source this aggregate". Defines event sourcing as storing the history of what happened as the canonical state, with current state derived by replay.
---

## One-line summary

Persist the sequence of state-changing events as the source of truth. Current state is derived by replaying events. The event log is append-only, immutable, and authoritative.

## When to use this skill

- Audit / regulatory requirements that demand a complete history of changes.
- Business that genuinely thinks in terms of "what happened" (banking transactions, claim adjustments, supply-chain events).
- Need to answer temporal queries ("what was the state on date X").
- Multiple projections of the same data needed and easier to materialize from events than to maintain by hand.
- Pairs naturally with CQRS (see `arch-cqrs`) when reads need different shapes than the canonical event stream.

## When NOT to use this skill

- The domain doesn't think in events — forcing the model into event shape is more cost than benefit.
- Current state is sufficient and history is rarely queried — CRUD is simpler.
- Team unfamiliar with eventual consistency, replay semantics, or event versioning — operational pain is severe.
- "Event sourcing" because microservices are trendy and we read about it somewhere — that's not a reason.

## Core content

**The event store.** An append-only log of events, each tagged with an aggregate ID, a sequence number, and a timestamp. Writing is an `append(events)`; reading for a specific aggregate is `eventsFor(aggregateId)`.

**Rebuilding state.** An aggregate's current state is the fold over its events:

```
state = events.fold(initial) { acc, event -> apply(acc, event) }
```

To handle a new command: load the events, replay to get the current state, run the command, append new events. The state in memory is transient; the events on disk are the truth.

**Snapshots.** For long-lived aggregates (years of events), replaying from the start gets slow. A snapshot is a periodic save of state-at-event-N. Loading uses the snapshot plus events after N. Snapshots are an optimization, not the source of truth — the events still are.

**Event versioning.** Events live forever. Their schemas must evolve forward-compatibly. Tactics:

- **Additive changes only** — new fields are optional, never required.
- **Upcasters** — code that transforms old event versions into the current shape on read.
- **Weak schema** (JSON with permissive parsing) — flexibility at the cost of compile-time safety.

**CQRS pairing.** Event-sourced writes naturally produce events that project into read models. Most production event-sourced systems use CQRS for queries because the event stream is awkward to query directly.

**Operational considerations.**

- Event store availability is critical — it's the source of truth.
- Tools (replay, projection rebuild, schema migration) need to exist before you commit; building them mid-project is painful.
- Storage grows monotonically. Plan for it.
- Aggregates with millions of events without snapshots will struggle.

## Decision heuristics

- Event-source aggregates where audit / temporal queries / multiple projections are genuinely required. Don't blanket-apply.
- Snapshot interval is a tuning knob — start with 100-500 events; adjust based on replay performance.
- Treat event schemas as a public API — version them; never break them.
- Build the operational toolkit (replay, rebuild, migrate) *before* you depend on event sourcing in production.

## Anti-patterns

- **Event-sourcing the whole system reflexively.** Most subdomains don't need it. Apply selectively — typically only the core domain with strong audit needs.
- **Mutable events.** Editing past events to "fix" history. Defeats the entire pattern. To "correct", append a compensating event.
- **Events as DTOs.** Events with no semantic meaning beyond "fields changed". Should be domain events: past-tense, business-meaningful.
- **Replaying without snapshots on hot aggregates.** Every command takes seconds to seconds-many.
- **Schema breakage without upcasters.** Old events become unreadable. The history is lost in practice.

## See also

- `arch-cqrs` — read models from event projections.
- `arch-event-driven` — events as cross-context communication (often the same events).
- `ddd-domain-event` — the tactical-level event concept.
- `ddd-aggregate` — event-sourced aggregates produce events as part of command handling.

## References

- Khononov, *Learning DDD*, Ch. 7 (Event-Sourced Domain Model).
- Vaughn Vernon, *Implementing Domain-Driven Design* — Ch. 8 has the canonical Java treatment.
- Greg Young, talks and essays on event sourcing (foundational).
