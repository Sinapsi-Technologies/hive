---
name: ddd-implement
description: Use when the user wants concrete code that realizes a DDD pattern — phrases like "scaffold an entity for X", "write the Aggregate for Y", "give me the repository interface", "implement this value object". Drives the conversation through naming + invariants and then delegates code generation to a language adapter skill (e.g., ddd-kotlin-idioms for Kotlin). Do NOT use for refactoring (route to ddd-refactor), review (route to ddd-review), or design from scratch (route to ddd-design).
---

## One-line summary

Bridge a DDD pattern decision to concrete code by clarifying naming and invariants, then handing off to a language adapter skill.

## When to use this skill

- The user already knows which pattern they want (entity, value object, aggregate, domain event, repository, factory).
- The target language is known (or can be inferred from the project).
- The user wants idiomatic scaffolding, not just prose.

## When NOT to use this skill

- The user is still designing → route to `ddd-design`.
- The user has working code with smells → route to `ddd-refactor`.
- The user wants a critique of existing code → route to `ddd-review`.

## Core content

Three steps, in order. Do not skip step 2.

**Step 1 — Confirm the pattern.** Name explicitly: entity, value object, aggregate root, domain service, domain event, repository, or factory. If the user is unsure, ask — and route to the matching pattern skill for the criteria.

**Step 2 — Confirm invariants and ubiquitous-language names.** What must always be true? What is this thing called in the business? An entity without enforced invariants is a struct; a value object with the wrong name is a missed concept. Push back if the user offers "I'll figure that out later" — invariants belong in the constructor or factory, not in a TODO.

**Step 3 — Hand off to the language adapter.** Detect the target language from the project. If the project is Kotlin, route to `ddd-kotlin-idioms`. If no adapter exists for the detected language, produce pseudocode following the relevant pattern skill and note the gap so the user can author an adapter.

## Decision heuristics

- If invariants are unspecified, stop. Generating fields-and-getters is a disservice — it produces anemic code disguised as DDD.
- Prefer immutability for value objects and identity-only equality for entities, regardless of language. Adapter skills enforce this idiomatically.
- Repositories are interfaces in the domain layer; implementations live in infrastructure. The adapter places them correctly for the language's conventions.

## Anti-patterns

- Generating getters and setters with no behavior.
- Producing a "DDD" entity that is really a JPA `@Entity` with annotations and nothing else.
- Skipping invariant enforcement because "the application layer will validate".
- Using a language's data-class equivalent for an entity (collapses identity to attribute equality).

## See also

- `ddd-entity`, `ddd-value-object`, `ddd-aggregate`, `ddd-domain-event`, `ddd-repository`, `ddd-factory` — pattern primitives this skill realizes.
- `ddd-kotlin-idioms` — the Kotlin adapter for code output.

## References

- Evans, *Domain-Driven Design* (2003), Chs 5–6.
