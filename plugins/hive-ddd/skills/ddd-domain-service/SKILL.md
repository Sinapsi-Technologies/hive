---
name: ddd-domain-service
description: Use when behavior doesn't naturally belong to a single entity or value object — phrases like "domain service", "this logic spans multiple aggregates", "where does this go", "the entity is starting to know too much", "we need a calculator/coordinator that isn't a thing". Defines when to introduce a stateless domain service vs forcing behavior onto an entity. Distinguishes domain services from application/infrastructure services.
---

## One-line summary

A domain service is a stateless operation in the domain layer that doesn't naturally sit on a single entity or value object — usually because it coordinates multiple aggregates or involves a domain computation that isn't anchored to one thing.

## When to use this skill

- A cohesive domain operation involves multiple aggregates (e.g., transferring money between two `Account` aggregates).
- A calculation involves domain types but isn't naturally a method on any of them (e.g., currency conversion, given exchange rates).
- Forcing the behavior onto one of the participating entities would make that entity "know too much" about the others.

## When NOT to use this skill

- The logic fits cleanly on a single entity — put it there.
- The work is application-layer orchestration (handling a use case, opening a transaction, calling repositories) — that is an application service, not a domain service.
- The work is infrastructure (HTTP calls, database access, message sending) — that is an infrastructure service.

## Core content

Three kinds of services often coexist in a DDD codebase. Keep them distinct:

**Domain service.** Lives in the domain layer. Operates on domain types only. Stateless. Expresses a verb in the ubiquitous language — `TransferService.transfer(fromAccount, toAccount, amount)`, not `AccountUtils.handleTransfer(...)`.

**Application service.** Orchestrates a use case. Opens transactions, calls repositories, invokes domain services, dispatches events. Stateless. Lives in the application layer.

**Infrastructure service.** Talks to the outside world — sends HTTP, writes to a database, publishes to a queue. Lives in the infrastructure layer.

A domain service is named for what it *does* in domain terms. `TransferService` and `ExchangeRateConverter` are good names. `TransferManager` and `TransferHelper` are smells — they hide the verb behind technical noise.

## Decision heuristics

- Prefer entity behavior. Reach for a domain service only when the operation involves multiple aggregates or has no natural home.
- If a domain service has state, it is almost certainly misclassified — make it a domain entity, a value object, or move the state into one of the participating aggregates.
- If a domain service grows to dozens of methods, it is probably a "transaction script" in disguise — split it or push behavior back onto entities.

## Anti-patterns

- `*Service` suffix on every class — the domain becomes a collection of transaction scripts over data DTOs (see anemic-domain-model in `ddd-anti-patterns`).
- Putting infrastructure (database, HTTP) inside a domain service — couples the domain to deployment concerns.
- Domain services that mutate the entities they are given without going through the entity's methods — bypasses invariants.
- Stateful "services" that hold caches, sessions, or other process state.

## See also

- `ddd-entity` — first preference is to put behavior here.
- `ddd-aggregate` — domain services often coordinate across aggregates.
- `ddd-anti-patterns` — anemic-model and service-explosion smells.

## References

- Evans, *Domain-Driven Design* (2003), Ch. 5 (A Model Expressed in Software).
