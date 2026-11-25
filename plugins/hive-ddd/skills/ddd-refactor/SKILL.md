---
name: ddd-refactor
description: Use when the user wants to transform existing code toward DDD — phrases like "refactor this to use aggregates", "extract a value object from this primitive", "split this god aggregate", "make this domain model less anemic", "introduce a bounded context boundary here". Sequences refactor steps and points at the pattern skill that explains the target shape. Do NOT use for code review (route to ddd-review) or initial design (route to ddd-design).
---

## One-line summary

Sequence a refactor toward DDD: identify the smell, name the target pattern, propose small behavior-preserving steps in order.

## When to use this skill

- Existing code that the user already considers a problem.
- The user knows roughly which direction to refactor in.
- Migration of a legacy module toward a DDD model.

## When NOT to use this skill

- The user just wants a review and isn't ready to refactor → route to `ddd-review`.
- The user is starting a new domain model from scratch → route to `ddd-design`.

## Core content

The general pattern: name the smell, name the target shape, write small steps that preserve behavior, run tests between each step, commit after each.

Common refactors and their step sequences:

**Anemic model → behavioral entity.** (1) Pick one operation currently done by a service. (2) Move it onto the entity, keeping the same signature. (3) Have the service delegate to the new entity method. (4) Inline the service call where it is the only caller. Repeat per operation.

**Primitive obsession → value object.** (1) Introduce the new value object type with a private constructor and a validating factory. (2) Add a single conversion point at the boundary (input parsing). (3) Replace one usage at a time, deepest-first. (4) Stop allowing the primitive at the public API. See `ddd-value-object`.

**God aggregate → multiple aggregates.** (1) List the entities currently inside it. (2) For each pair, ask "must these change in the same transaction?" — group by yes-answers. (3) Identify the root of each group. (4) Replace cross-group object references with ID references. (5) Move cross-group consistency to events. See `ddd-aggregate`.

**Leaky repository → domain repository interface.** (1) Define a domain-layer interface with methods named in the ubiquitous language. (2) Implement it in the infra layer, wrapping the existing persistence. (3) Switch one caller at a time. (4) Delete the leaked types from the domain layer. See `ddd-repository`.

**Big ball of mud → first bounded context.** (1) Pick the most painful seam. (2) Draw the *intended* boundary on paper. (3) Introduce an anticorruption layer at the seam. (4) Move code across the seam one type at a time. See `ddd-context-mapping` for the seam's pattern.

## Decision heuristics

- Never combine a refactor commit with a feature commit. They review separately, they revert separately.
- Each step must keep behavior identical. The test suite is your seatbelt; if it doesn't exist, write characterization tests first.
- Stop after each step long enough to run tests and commit.
- Big-bang rewrites lose users' trust and almost always grow scope. Prefer ten small refactors over one large one.

## Anti-patterns

- Big-bang rewrite with no incremental milestones.
- Refactoring without a test net.
- Renaming classes "to sound DDD" without changing their structure or responsibilities.
- Refactoring and adding features in the same change.

## See also

- `ddd-anti-patterns` — the smells that motivate each refactor.
- `ddd-aggregate`, `ddd-value-object`, `ddd-bounded-context`, `ddd-repository` — target shapes.
- `ddd-deeper-insight` — what to do after the obvious refactors are done.

## References

- Evans, *Domain-Driven Design* (2003), Part III (Chs 8–13).
