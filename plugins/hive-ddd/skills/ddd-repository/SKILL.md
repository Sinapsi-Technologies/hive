---
name: ddd-repository
description: Use when designing how aggregates are persisted and retrieved — phrases like "repository", "how should I load this aggregate", "JPA repository vs DDD repository", "leaky repository", "finder methods", "the repository is doing too much". Defines a repository as the abstraction that gives the illusion of an in-memory collection of aggregates. One repository per aggregate root.
---

## One-line summary

A repository hides persistence behind the illusion of an in-memory collection of aggregates — domain code does not know whether storage is SQL, key-value, or in-memory.

## When to use this skill

- Loading or saving aggregates by identity.
- Replacing ad-hoc DAOs that leak persistence types into the domain.
- Defining a small set of domain-meaningful queries (`findActiveByCustomer`, not `findByStatusEqualsAndCustomerIdEquals`).

## When NOT to use this skill

- Value objects — they have no identity to look up by.
- Cross-aggregate queries for display purposes — build a read model, not a repository method.
- Reporting / analytics — those want a query layer optimized for reads, not a domain repository.

## Core content

A repository serves *one aggregate root*. Its interface lives in the *domain layer*; its implementation lives in the *infrastructure layer*. Domain code depends only on the interface; infrastructure code wires the implementation.

The interface speaks the ubiquitous language. Methods are named for what the business does, not for the persistence mechanism. `findActiveByCustomer` is good. `findByStatusEqualsAndCustomerIdEquals` is a SQL query name in a domain-shaped wrapper — the abstraction has leaked.

A repository deals in *whole aggregates*. It does not return half an aggregate, or an aggregate's internal entity, or a database row. The aggregate root is what comes back; callers operate through it.

When a query is for *display* — showing the user a list of orders, summarizing customer activity, building a dashboard — it usually does not belong in the repository. Build a separate read model with its own query interface, optimized for the view.

## Decision heuristics

- One repository per aggregate root. Not one per entity.
- If a method exposes persistence types (`Query`, `Predicate`, `Specification` in the JPA sense, `EntityManager`), the abstraction has leaked — push those down.
- If a method returns a row, a DTO, or a partial entity, it is not a domain repository operation. Move it to a read model.
- If the repository grows beyond ~5–8 methods, consider whether some of those are really read-model queries in disguise.

## Anti-patterns

- Repositories returning rows, DTOs, or persistence types.
- One repository per entity, exposing the aggregate's internals.
- Arbitrary finder methods (`findByXAndYAndZAndW`) instead of a small, named, domain-meaningful set.
- Using a framework's generic `JpaRepository` / `CrudRepository` *as* the domain repository — that exposes every CRUD operation indiscriminately and ties the domain to JPA.

## See also

- `ddd-aggregate` — what a repository operates on.
- `ddd-factory` — for creating the aggregate; repository handles its persisted lifecycle.
- `ddd-anti-patterns` — leaky repository and CRUD-named methods.

## References

- Evans, *Domain-Driven Design* (2003), Ch. 6 (The Life Cycle of a Domain Object).
