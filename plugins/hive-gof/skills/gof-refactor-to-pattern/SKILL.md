---
name: gof-refactor-to-pattern
description: Use when refactoring existing code toward a known GoF pattern — phrases like "refactor this to use Strategy", "extract a Decorator from these flags", "this switch should be State", "replace conditional with polymorphism", "introduce a Factory here". Sequences small behavior-preserving refactor steps and links to the destination pattern skill.
---

## One-line summary

Take existing code and step-by-step move it toward a target GoF pattern — small commits, behavior preserved at every step, tests green between each.

## When to use this skill

- The user knows the destination pattern and wants the path.
- A code smell has a known pattern-shaped fix (big switch on enum → State; primitive flag explosion → Decorator).
- Migration from procedural / transaction-script code to OO with patterns.

## When NOT to use this skill

- The user doesn't know which pattern fits — route to `gof-identify`.
- The user wants a review, not a refactor — route to `gof-review`.
- The user wants the pattern in *new* code → route to `gof-implement` style (here, that maps to `gof-kotlin-idioms` for direct code).

## Core content

General discipline: never combine a refactor with a feature change in one commit. Each refactor step must keep behavior identical and pass tests; if there are no tests, write characterization tests first.

Common refactors and their step sequences:

**Big switch on type code → Strategy.**
1. Identify the varying behavior (the method body inside each `switch` case).
2. Extract each branch into its own object implementing a common `Strategy` interface.
3. Replace the switch with a map / `when` that selects the strategy.
4. Inline the lookup until clients hold strategy references directly.

**Subclass-per-variant → Strategy or Composition.**
1. Identify the single dimension that varies between subclasses.
2. Extract that dimension as a Strategy interface.
3. Collapse the subclasses back into one class that composes a Strategy.

**Conditional behavior accumulating in one class → Decorator.**
1. Identify the orthogonal extensions hiding in `if (hasX)` checks.
2. Extract each extension into a Decorator class wrapping the base.
3. Compose decorators in the construction site instead of toggling flags.

**Status enum + switch in many methods → State.**
1. Draw the state diagram explicitly.
2. Create a State class per state.
3. Move the per-state behavior from each `switch` into the corresponding State.
4. The context delegates to the current State; states drive transitions by setting `context.state = NextState`.

**Multiple `new ConcreteX()` decisions across client code → Factory.**
1. Centralize the choice in one method (Simple Factory).
2. If the choice depends on a subclass's identity, promote to Factory Method.
3. If matched families must be created together, promote to Abstract Factory.

**Direct calls between many subsystems → Facade.**
1. Identify the common sequences callers use.
2. Define a Facade class whose methods are the high-level intents.
3. Migrate callers to the Facade; keep the subsystem accessible to power users.

**Static `getInstance()` everywhere → Dependency Injection.**
1. Identify the Singleton's callers.
2. Add a constructor parameter (or field) for the dependency.
3. Wire the dependency once at the application's composition root.
4. Delete `getInstance()`.

## Decision heuristics

- Each refactor step is small, reversible, and committable.
- Run tests between each step. If tests don't exist, write characterization tests first (record-and-replay style).
- Refactor and feature changes never share a commit.
- If the refactor balloons, split it into multiple PRs — refactor first, then feature.

## Anti-patterns

- Big-bang rewrites in the name of "now we'll do it right".
- Refactoring without tests.
- Renaming classes to pattern names without restructuring.
- Refactor + feature in the same commit — review and revert become hard.

## See also

- All thirteen pattern skills (the destinations).
- `gof-identify` — when destination is unknown.
- `gof-review` — to catch smells worth refactoring.
- `ddd-refactor` — the DDD-flavored counterpart.

## References

- *Head First Design Patterns* (2nd ed), Ch. 13 (patterns in the real world).
- Joshua Kerievsky, *Refactoring to Patterns* (2004) — book-length treatment of the same idea; complements this skill.
