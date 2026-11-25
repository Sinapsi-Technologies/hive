---
name: arch-cqrs
description: Use when separating read and write models — phrases like "CQRS", "Command Query Responsibility Segregation", "read model vs write model", "denormalized read side", "materialized view", "different scaling for reads and writes", "command bus", "query bus", "should we use CQRS here". Defines CQRS as keeping reads and writes on separate models, with picking criteria that resist over-application.
---

## One-line summary

Separate the model used for *commands* (state-changing operations) from the model used for *queries* (read-only views). Each can evolve, scale, and persist independently.

## When to use this skill

- Read load is dramatically asymmetric with write load — many queries per write, or vice versa.
- Different read use cases need different shapes of the same data (dashboard, search, detail page) — one canonical read model would be a compromise.
- The natural write model (e.g., an aggregate with rich invariants) is awkward to query for views.
- The write side benefits from being event-sourced, but the read side needs fast SQL queries — CQRS is the bridge.

## When NOT to use this skill

- Reads and writes are roughly balanced and use the same shape — CQRS is pure overhead.
- The team is unfamiliar with eventual consistency — CQRS introduces it by design.
- The system is small and a normal repository pattern would do — over-engineering.
- "CQRS" used to justify having two database tables for the same data without a real driver — that's just data duplication.

## Core content

**Two models, one underlying truth.**

- **Write model** — receives commands (`PlaceOrder`, `CancelShipment`). Validates, enforces invariants, persists. Often an aggregate (`ddd-aggregate`). Optimized for consistency, not for reads.
- **Read model** — receives queries. Returns DTOs shaped for specific consumers. May be denormalized, joined-ahead, cached, indexed for search. No invariants enforced — it's a projection.

**How they sync.** Writes generate events (often domain events; see `ddd-domain-event`). Read models are projections — handlers consume events and update materialized views. The read side is always slightly behind the write side. That's *eventual consistency*; embrace it deliberately.

**Storage flavors.**

- **Same database, two models.** Simplest. Write model and read model are different tables (or even different schemas) in the same database. Read model populated by triggers, scheduled jobs, or in-process projection.
- **Different databases.** Write to a transactional store (Postgres for aggregates); project into a query store (Elasticsearch for search; Redis for hot lookups; a separate Postgres schema for dashboards). Real operational complexity; justify with real read requirements.

**CQRS does NOT require event sourcing.** They're often paired but independent. You can do CQRS over a normal CRUD write model that emits events as it commits — see `arch-event-sourcing` for the case where events *are* the source of truth.

**The eventual consistency conversation.** Read models lag. If your domain experts say "the user expects to see their change immediately on the dashboard", you have a choice: tighten the projection latency, or accept "your change is being processed" UI messaging. Don't pretend the gap doesn't exist.

## Decision heuristics

- Use when there's *measurable* read/write asymmetry or *materially different shapes* between writes and reads. Not because "CQRS sounds advanced".
- Default to single-database CQRS first (two models in one Postgres). Different databases only when the read store's characteristics (search, geospatial, in-memory) genuinely differ from the write store's.
- Pair with event-driven only when downstream consumers exist for the events; otherwise simpler projections work fine.
- The read model is a *projection*, not a source of truth. If you find yourself reading and then writing back through the read model, you've broken CQRS.

## Anti-patterns

- **"CQRS" with one model.** Two folders named `commands/` and `queries/` but they read from and write to the same table without separation. Theatre.
- **Read model becomes write-through.** Code writes directly to the read store, bypassing the write model. The two diverge silently.
- **Eventual consistency hidden from the UI.** User makes a change; UI shows stale data; user thinks the change failed. Communicate the delay.
- **Heavy read model migrations.** Every schema change requires a full reprojection, which is slow and risky. Version your read models and run side-by-side migrations.
- **Adopted for write/read parity (no asymmetry).** Pure overhead with no benefit.

## See also

- `arch-event-driven` — events typically propagate writes to read-model projections.
- `arch-event-sourcing` — when the write side stores events as the source of truth, CQRS becomes the natural read side.
- `ddd-aggregate` — the canonical shape of a CQRS write model.
- `ddd-repository` — read-side reads are usually *not* through the domain repository; they're separate query objects.

## References

- Khononov, *Learning DDD*, Ch. 8 (CQRS).
- Greg Young, "CQRS Documents by Greg Young" — original CQRS articulation.
- Vaughn Vernon, *Implementing Domain-Driven Design* — Ch. 4 includes a CQRS treatment.
