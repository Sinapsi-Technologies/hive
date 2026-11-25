---
name: ddd-anti-patterns
description: Use when identifying DDD anti-patterns and smells in code or design — phrases like "anemic domain model", "primitive obsession", "leaky repository", "smart UI", "transaction script", "god aggregate", "big ball of mud". Catalogs the common DDD anti-patterns with symptoms, why each is wrong, and the pattern skill that explains the fix.
---

## One-line summary

A reference catalog of the most common DDD anti-patterns, each with symptoms and a link to the pattern skill that explains the fix.

## When to use this skill

- Code review walks (`ddd-review` cites this for each finding).
- Spotting smells during a refactor (`ddd-refactor` uses this catalog as the smell-to-target-shape lookup).
- Debating "is this really DDD or just CRUD-with-pattern-names".
- Onboarding teammates — a single page that names the things to avoid.

## When NOT to use this skill

- You already know which pattern applies — go directly to it.
- The question is about *what* to build, not *what not* to build → use `ddd-design`.

## Core content

The catalog. Each entry: name, symptoms, why it is wrong, fix.

**Anemic Domain Model.** Entities are data bags — fields with getters and setters, no behavior. All the logic lives in `*Service` classes. Wrong because the model carries no domain knowledge; services become transaction scripts over DTOs. Fix: push behavior onto entities. See `ddd-entity`, `ddd-domain-service`.

**Primitive Obsession.** Domain concepts encoded as raw `String`, `Long`, `BigDecimal`, etc. — `String customerId`, `Long amount`, `String currency`. Wrong because invariants aren't enforced, two concepts sharing a primitive type become interchangeable, and operations get scattered into static helpers. Fix: extract value objects. See `ddd-value-object`.

**God Aggregate.** One aggregate that owns half the model. Symptoms: loading it pulls in hundreds of rows; lock contention spikes; transactions become unwieldy. Wrong because aggregate boundaries should be transactional boundaries. Fix: split by what must change together. See `ddd-aggregate`.

**Leaky Repository.** The repository exposes persistence types (`Query`, `Predicate`, `EntityManager`, framework `JpaRepository`). Wrong because the domain layer now depends on persistence concerns and can't be tested in isolation. Fix: domain-language methods on a domain-layer interface, with infrastructure-layer implementations. See `ddd-repository`.

**Transaction Script masquerading as DDD.** Services full of procedural logic over data DTOs, with classes named `*Manager`, `*Helper`, `*Processor`. Wrong because there is no actual domain layer — the names create the illusion of DDD. Fix: behavior on entities; `*Service` only when truly stateless and cross-aggregate. See `ddd-domain-service`.

**Smart UI.** Business rules embedded in controllers, views, or front-end code. The "domain" is whatever happens to be in the database. Wrong because the rules are inaccessible to anything that isn't the UI. Fix: extract a real domain layer. See `ddd-design`.

**Big Ball of Mud.** No boundaries at all. Everything reaches into everything. Wrong because change becomes impossible to localize. Fix: draw a context map first; integrate against the mud through an anticorruption layer; do not extend it. See `ddd-bounded-context`, `ddd-context-mapping`.

**Cross-aggregate object references.** `Order` holds an `Customer` *object* reference (not just an ID). Wrong because it forces eager loading, ties the two aggregates' lifecycles, and breaks the transactional boundary. Fix: hold an ID, look up via repository when needed. See `ddd-aggregate`.

**`Manager` / `Service` / `Helper` suffix syndrome.** Names like `OrderManager`, `PaymentHelper`, `AccountProcessor` hide the domain verb behind technical noise. Wrong because the ubiquitous language is supposed to live in the code. Fix: name for the verb the business uses. See `ddd-ubiquitous-language`, `ddd-domain-service`.

## Decision heuristics

- If you can name a smell but can't name the target pattern, the smell is real but the fix is unclear. Discuss with the team and pick a target shape *first*, then refactor.
- Severity scales with reach: a leaky abstraction touched by ten callers is worse than the same leak in one place.
- Some anti-patterns are *acceptable in generic subdomains* (CRUD over a config table doesn't need a rich model). Don't apply DDD's full ceremony uniformly. See `ddd-subdomain-classification`.

## Anti-patterns about anti-patterns

- Treating every long class as anemic — anemia is about *behavior*, not length.
- Chasing patterns for the sake of patterns instead of fitness for purpose.
- Calling working, well-tested code "wrong" because it doesn't match a textbook layout.
- Renaming a `Manager` to a `Service` and calling it a refactor.

## See also

- Every pattern skill in `ddd-core` — each smell links to one or more of them.
- `ddd-review` — uses this catalog as its checklist.
- `ddd-refactor` — uses this catalog as its smell-to-target lookup.

## References

- Evans, *Domain-Driven Design* (2003), Parts II and III.
- Fowler, *Anemic Domain Model* (essay, 2003) — the canonical statement of that anti-pattern.
