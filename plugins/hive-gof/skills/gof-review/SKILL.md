---
name: gof-review
description: Use when reviewing code that claims to use GoF patterns — phrases like "is this Observer used correctly", "review this for design pattern smells", "this is named *Strategy but doesn't look right", "audit this Singleton". Walks the target code through the catalog of pattern misuses with severity-tagged findings and citations to the relevant pattern skill.
---

## One-line summary

Audit code that claims to (or appears to) use GoF design patterns — checking that the pattern's intent, structure, and trade-offs are honoured, not just its name.

## When to use this skill

- A pull request introduces a pattern and the reviewer wants a structured check.
- Onboarding into a codebase with `*Factory`, `*Strategy`, `*Adapter`, `*Manager` everywhere and wanting to know which are real uses and which are pattern theatre.
- The user suspects a pattern is being misapplied (over-engineered, misnamed, wrong intent).

## When NOT to use this skill

- The user wants to *introduce* a pattern → route to `gof-identify` or `gof-refactor-to-pattern`.
- The user has a domain modeling problem, not a code-level pattern question → route to `ddd-review` or `ddd-design`.

## Core content

Review the target file or module against this catalog of pattern misuses. For each finding, report severity (`blocker` / `major` / `minor`), location, and the pattern skill that explains the fix.

**Pattern theatre.** A class named `OrderManager`, `CustomerStrategy`, or `PaymentFactory` that doesn't actually implement the pattern — just borrows the name. Common in codebases that have read about patterns but not adopted them. Fix: rename to the domain verb (see `ddd-ubiquitous-language`) or actually apply the pattern.

**Singleton everywhere.** `getInstance()` calls scattered through the codebase, holding global state, fighting tests. Fix: introduce dependency injection; reserve Singleton for genuinely-one-of resources. See `gof-singleton`.

**Strategy with one strategy.** A Strategy interface with one implementation, no plan for a second. The indirection costs without benefit. Fix: inline the strategy until a second appears.

**God Facade.** A `Facade` class that has grown to know every subsystem detail and includes setters for everything. Fix: split into focused facades, or accept that the "facade" is now an application service.

**Decorator soup.** A chain of decorators where the call order matters but isn't enforced anywhere. Fix: document the required composition order, or restructure so the order is encoded in types.

**Observer leaks.** Observers that registered but never deregistered, holding the subject alive forever. Fix: explicit `detach()` discipline, weak references, or a real event bus.

**Adapter doing business logic.** An adapter that has grown beyond translation into actual computation. Fix: move the logic to where it belongs; keep the adapter to translation.

**Pattern-named class that wraps but doesn't fulfil the intent.** A `*Proxy` that adds behavior (that's Decorator). A `*Decorator` that changes the interface (that's Adapter). A `*Strategy` that holds lifecycle state (that's State). Fix: rename or restructure to match intent.

**Inheritance hierarchy where composition was the point.** A "Decorator" implemented as a subclass tree — defeats the pattern's main value (composition over inheritance). Fix: refactor to use composition.

## Decision heuristics

- Severity scales with reach: a misused pattern touched by many callers is worse than the same misuse in one place.
- Report findings, not opinions. Each finding cites a line range and a pattern skill.
- Don't pattern-name without showing the violation in the code.
- A pattern that is named correctly but applied unnecessarily (premature abstraction) is still a finding — call it out.

## Anti-patterns

- Rubber-stamping code that obviously uses a pattern incorrectly.
- Reviewing for naming alone — `OrderManager` may be fine if it's a coherent domain concept; the test is whether the code does what the name implies.
- Citing a pattern without showing the violation in the code.

## See also

- All thirteen pattern skills — findings cite the relevant one.
- `gof-identify` — for "which pattern should this be?" questions that come up during review.
- `ddd-review` — for domain-modeling review (orthogonal to design-pattern review).

## References

- *Head First Design Patterns* (2nd ed), Ch. 13 — patterns in the real world, including warnings about over-application.
- *Design Patterns* (GoF, 1994) — original "intent" sections of each pattern; misalignment with intent is the most common source of misuse.
