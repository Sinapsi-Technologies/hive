---
name: arch-fitness-functions
description: Use when enforcing architectural characteristics through automation — phrases like "architecture fitness function", "evolutionary architecture", "architecture tests", "ArchUnit", "dependency rules in CI", "automated architecture checks", "guardrails", "drift detection". Defines fitness functions as automated checks that verify architectural characteristics on every change, catching drift before it accumulates.
---

## One-line summary

Encode architectural intent as automated checks that run on every change. If the architecture says "the domain layer doesn't depend on Spring", a fitness function fails the build when it does.

## When to use this skill

- An architecture decision matters and needs to *stay true* over time (dependency direction, layer boundaries, no cyclic dependencies, performance budgets).
- Teams are large enough that informal "we agreed not to do X" doesn't hold across all PRs.
- Migration projects (`arch-strangler-fig`) where you want to enforce that new code lives in the new shape and not the legacy.
- Any architectural decision where you'd otherwise rely on code review to catch drift.

## When NOT to use this skill

- Tiny project, single developer, no scaling problem.
- The architectural property is genuinely subjective and not automatable (taste, aesthetics, naming style on edge cases).
- Over-policing — every architectural rule has a cost; only enforce what you would block a PR over.

## Core content

A fitness function is a test that asserts an architectural property. Like unit tests, they:

- Run automatically in CI on every change.
- Fail the build if violated.
- Document the architectural intent in executable form.

**Categories of fitness function** (Ford, Parsons, Kua, *Building Evolutionary Architectures*):

- **Atomic vs holistic.** Atomic checks a single property (no cycles in this module). Holistic combines multiple properties (performance + correctness under load).
- **Triggered vs continual.** Triggered runs on each build (most useful). Continual runs in production (latency budgets, error rates).
- **Static vs dynamic.** Static analyses code (dependency direction). Dynamic runs the system (response-time check).

**Common static fitness functions** (these are the most accessible to start with):

- **Dependency direction.** "Module X must not depend on module Y." Enforced via build graph or a dedicated library (ArchUnit for JVM, ts-arch for TypeScript, dependency-cruiser for JS).
- **No cyclic dependencies.** Detects accidental cycles introduced by a misplaced import.
- **Naming conventions.** "Every class in `controller/` must end in `Controller`." Mostly cosmetic but cheap to enforce.
- **Public API stability.** "No new public methods on this interface without a corresponding test." Custom check.
- **Layer boundary violations.** "Code in `domain/` must not import from `infrastructure/`." The classic hexagonal check.

**Dynamic fitness functions** for production architecture:

- p99 latency under a threshold.
- Memory usage bounded.
- Specific business invariants holding (no negative balances; no duplicate orders).

**Example with ArchUnit** (JVM, the most mature ecosystem):

```kotlin
@ArchTest
val domainHasNoSpringDependencies =
    noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAPackage("org.springframework..")
```

## Decision heuristics

- Start with one or two fitness functions enforcing the architecture's most important property (typically dependency direction). Add as drift surfaces.
- Make fitness function failures *fail the build* — a warning that's been ignored for a year is not a fitness function.
- Treat fitness functions like tests: each has a clear failure message that explains *why* the rule exists, not just *what* it checks.
- Don't write a fitness function for a rule you wouldn't block a PR over.
- For migration projects, fitness functions can encode "new code goes in path X, not legacy path Y" — invaluable for keeping the migration honest.

## Anti-patterns

- **Fitness function that nobody owns.** Breaks → someone disables it → bypassed forever.
- **Over-policing.** Hundreds of fitness functions enforcing every preference. CI gets slow; developers learn to ignore failures.
- **Style as architecture.** "All method names must be camelCase" is a linter rule, not a fitness function. Don't dilute the term.
- **Documentation without automation.** Wiki page describing the architecture; no automated check; new joiners violate it within weeks.
- **One-time check.** Architecture audit done once, results filed away. Drift accumulates immediately after.

## See also

- `arch-layered` — layer boundaries are the canonical fitness-function use case.
- `arch-hexagonal` — dependency-direction-toward-domain is the canonical hexagonal fitness function.
- `arch-adr` — fitness functions encode the *what*; ADRs explain the *why*. Both belong together.

## References

- Neal Ford, Rebecca Parsons, Patrick Kua, *Building Evolutionary Architectures* (O'Reilly, 2nd ed) — the foundational reference.
- ArchUnit documentation (JVM).
- Richards & Ford, *Fundamentals of Software Architecture*, governance / fitness chapters.
