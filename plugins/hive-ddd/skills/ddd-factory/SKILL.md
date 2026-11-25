---
name: ddd-factory
description: Use when creation of an aggregate or complex value object requires non-trivial work — phrases like "factory", "the constructor is doing too much", "construction has business meaning", "how do I create this aggregate", "constructor with twelve parameters". Defines when to introduce a factory and when a constructor or static creation method suffices.
---

## One-line summary

A factory encapsulates the knowledge needed to create a complete, valid aggregate or value object when construction itself is complex or has domain meaning.

## When to use this skill

- Creation requires assembling multiple sub-objects (a new `Order` is born with line items, an initial status, a created-at timestamp).
- Creation must enforce invariants beyond simple field validation (a new `Subscription` must reference a valid `Plan` and a non-cancelled `Customer`).
- The act of creation has a domain-meaningful name in the ubiquitous language (`Order.placeFor(customer, items)`, `Account.openFor(applicant)`) — not just `new Order(...)`.

## When NOT to use this skill

- Simple objects whose constructor fully captures the work.
- A static creation method on the entity itself is sufficient — promote to a separate factory class only when the creation logic doesn't belong on the resulting object, or when multiple aggregates collaborate during creation.
- The "factory" you are tempted to write would just call `new` — that is ceremony, not encapsulation.

## Core content

Three common shapes, from least to most ceremony:

**Static creation method on the entity.** `Order.placeFor(customer, items)`. Preferred when the creation logic naturally belongs on the resulting type. Names itself in the ubiquitous language. Encapsulates whatever construction work is needed without inventing a new class.

**Dedicated factory class.** A `SubscriptionFactory` with a method `createFor(applicant, plan, paymentMethod)`. Use when creation involves orchestration that doesn't belong on the resulting entity — for example, calling other aggregates, looking up reference data, or coordinating multiple inputs.

**Builder.** A fluent builder for incremental construction. Use sparingly; builders shine when an aggregate has many optional fields and validation must run at the final `build()` step. Make sure the builder cannot hand back an invalid intermediate state — `build()` returns a fully validated aggregate or fails.

A factory's responsibility ends at returning a valid aggregate. Persistence is the *repository's* job. Mixing creation and persistence inside one factory blurs the layers.

## Decision heuristics

- Start with a constructor or a static method. Promote to a factory class only when the work is too complex for the entity to own or involves multiple collaborators.
- The factory's method names should belong to the ubiquitous language — `placeOrder`, `openAccount`, `issueRefund` — not `create`, `make`, `instantiate`.
- If a factory needs to read or persist data, you are mixing concerns — the factory probably belongs in the application layer, or it should accept already-loaded inputs.

## Anti-patterns

- Factories that also persist — confuses creation with lifecycle ownership.
- A `FooFactory` class per entity, reflexively, even when its `create()` does nothing the constructor couldn't.
- "FactoryFactory" indirection — almost always a sign of an over-engineered codebase.
- Builders that ship intermediate state — callers can hold a half-built `Order` and call methods on it.

## See also

- `ddd-entity` — what the factory typically creates.
- `ddd-aggregate` — factories produce whole aggregates, not half-aggregates.
- `ddd-repository` — the boundary; factory creates, repository persists.

## References

- Evans, *Domain-Driven Design* (2003), Ch. 6 (The Life Cycle of a Domain Object).
