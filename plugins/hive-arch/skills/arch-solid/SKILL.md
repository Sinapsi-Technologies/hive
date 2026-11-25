---
name: arch-solid
description: Use when discussing object-oriented design principles or component-level structure — phrases like "SOLID", "Single Responsibility", "Open-Closed", "Liskov Substitution", "Interface Segregation", "Dependency Inversion", "SRP", "OCP", "LSP", "ISP", "DIP". Defines the five SOLID principles, with the honest caveats Uncle Bob's original treatment glosses over and the cases where they shouldn't be applied dogmatically.
---

## One-line summary

The five principles that shape healthy object-oriented and component design — applied with judgment, not dogma. Each principle has a specific motivation and a corresponding overuse anti-pattern.

## When to use this skill

- Code review where structure / responsibility / coupling is the concern.
- Component design discussion: should this be one class or two; should this interface be split.
- Onboarding: explaining the underlying principles that motivate many GoF patterns.
- Architecture discussions where the dependency direction matters (DIP underpins hexagonal architecture).

## When NOT to use this skill

- Tactical fixes for individual smells → the per-pattern skills (`gof-*`, `ddd-anti-patterns`) are more concrete.
- Dogmatic application as a checklist ("does this violate any SOLID principle?") — that's how SOLID becomes anti-pattern. Each principle is a *direction*, not a binary.

## Core content

**S — Single Responsibility Principle.** "A class should have one reason to change." Uncle Bob's gloss: "responsibility = reason to change", not "responsibility = task". The principle is about the *change axis* — different stakeholders should not need to modify the same class for unrelated reasons. Overapplied: one class per method, files everywhere, indirection without benefit.

**O — Open-Closed Principle.** Open to extension, closed to modification — add new behaviour through new code (subclasses, strategies, plug-ins) without modifying existing tested code. Realized through abstraction over varying axes (see `gof-strategy`, `gof-template-method`). Overapplied: every class is `abstract` "in case we need to extend it later" — premature flexibility.

**L — Liskov Substitution Principle.** A subtype must be substitutable for its base type without breaking callers. If `Square` extends `Rectangle` and breaks `setWidth(w); setHeight(h)` assumptions, the inheritance is wrong even if both types have width and height. Often violated by inheriting "is-a" relationships that aren't truly behavioural sub-types.

**I — Interface Segregation Principle.** Clients shouldn't be forced to depend on methods they don't use. Many small focused interfaces beat one fat interface that forces every implementer to handle methods it doesn't care about. Overapplied: interface-per-method, friction without benefit.

**D — Dependency Inversion Principle.** High-level modules don't depend on low-level modules; both depend on abstractions. Abstractions don't depend on details; details depend on abstractions. The foundational idea behind hexagonal architecture (`hive`) — the domain owns the interface, the infrastructure implements it.

**Where SOLID is misused.**

- As a binary pass/fail rather than a direction.
- As justification for excessive abstraction ("we need an interface here in case we ever swap implementations" — premature).
- As the only criterion: code can satisfy SOLID and still be unreadable, slow, or wrong.
- As a hiring filter: "do you know SOLID" reduces it to vocabulary check, missing the judgment that makes the principles useful.

## Decision heuristics

- SOLID is a vocabulary for talking about coupling, cohesion, and change. The principles are *defaults*, not rules. Apply with judgment about the specific code.
- DIP is the most consequential at architecture scale (hexagonal, clean architecture). The others operate at class / module scale.
- LSP violations are bugs (substituted subtype breaks callers). Other SOLID violations are design smells — less urgent, still worth fixing when the cost is low.
- Don't introduce interfaces for SOLID's sake. The cost of indirection is real. Wait until the second implementation appears (or is genuinely imminent).

## Anti-patterns

- **SOLID theatre.** Adding abstractions, splitting classes, introducing interfaces — all to "satisfy SOLID" — without changing what the code actually does. Pattern-name without substance.
- **One-class-one-method.** Splitting classes so aggressively that the codebase becomes a maze of tiny indirections. SRP exaggerated past usefulness.
- **Interface for everything.** Every class behind an interface that has exactly one implementation. ISP overapplied.
- **Inheritance for code reuse.** Subclassing a base class to reuse 30% of its methods, breaking LSP because the subclass doesn't truly behave as the base does.
- **SOLID as a checklist.** "Does this PR violate SOLID?" misses the question of whether the violation matters. Some do; many don't.

## References

- Robert C. Martin, *Clean Architecture* (Pearson, 2017) — Chs 7–11 cover SOLID.
- Robert C. Martin, *Agile Software Development, Principles, Patterns, and Practices* (2002) — earlier SOLID treatment.
- The principles are widely written about; Uncle Bob's framing is canonical but not the only valid take.