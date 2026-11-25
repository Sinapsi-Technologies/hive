---
name: ddd-deeper-insight
description: Use when the model is "working but feels off" and the user wants to deepen it — phrases like "supple design", "refactor toward deeper insight", "implicit concept", "the model isn't quite right yet", "what would a better model look like", "intention-revealing interfaces", "side-effect-free functions", "assertions in the domain", "conceptual contours". Covers Evans's Part III techniques for evolving a model past its first draft.
---

## One-line summary

Past the first model, deeper insight comes from naming the concepts the model hides and shaping interfaces so they reveal intent and resist misuse.

## When to use this skill

- The model "works" but feels awkward — code that explains itself in comments, methods that take many parameters, names that almost-but-not-quite fit.
- Ongoing modeling sessions where the language is sharpening week by week.
- A team that has used DDD for a while and wants to move past the obvious patterns toward supple design.

## When NOT to use this skill

- Initial design from scratch → use `ddd-design`.
- Clear, named smells with known fixes → use `ddd-review` and `ddd-refactor` directly.

## Core content

Evans's Part III techniques, distilled. Apply them one at a time when the model invites them — none of these are mandatory.

**Breakthroughs are gradual.** The first version of a model is rarely the best one. Keep modeling after it "works". The team's understanding improves; the model should improve with it.

**Make implicit concepts explicit.** When a rule is hidden in code comments or scattered if-statements, name it as a domain concept. A `Policy`, a `Specification`, a `Rate`, an `EligibilityRule`. The name itself is part of the ubiquitous language. Conversely: when a comment is explaining what code "really means", the missing name is the comment's subject.

**Intention-revealing interfaces.** Name methods by *what* they accomplish in domain terms, not *how* they implement it. `Account.withdraw(amount)` reveals intent; `Account.modifyBalance(-amount)` describes a mechanism.

**Side-effect-free functions.** Push as much logic as possible into pure functions that return new value objects rather than mutating state. The non-mutating parts of a model are the easiest to reason about and the easiest to compose.

**Assertions.** Encode invariants in code — preconditions on constructors, postconditions on methods, invariants on the entity's state — not just in docs. If invariants live only in prose, they will drift.

**Conceptual contours.** Split classes along the natural seams of domain concepts, not technical convenience. If two pieces of behavior change for different reasons or evolve at different rates, they belong in different places.

**Standalone classes.** Minimize a class's outgoing dependencies. Coupling is a modeling smell — heavily-coupled classes usually contain multiple concepts that the model has not yet separated.

**Closure of operations.** Operations whose inputs and outputs share a type compose cleanly. `Money.add(Money) → Money` composes; `Money.add(Amount) → BigDecimal` does not. Closure makes the model algebraic — operations chain naturally.

## Decision heuristics

- When a comment explains what code "really means", the concept the comment names is probably missing from the model.
- Pure functions and immutable value objects make the model easier to reason about; reach for them by default and accept mutability only when the entity's lifecycle demands it.
- Coupling is a modeling smell, not just a code-quality smell — if a class depends on many others, the modeling responsibility is too broad.
- Don't refactor for the sake of patterns. Apply these techniques when the domain asks for them, not because the book lists them.

## Anti-patterns

- Renaming classes "to sound DDD-ish" without changing structure or responsibilities.
- Introducing a `Policy` or `Specification` class because the book mentions them, not because the domain calls for one.
- "Closure of operations" theatre — forcing operations to share types when they naturally don't.
- Refactoring without an end state in mind — supple design is a direction, not a checklist.

## See also

- `ddd-refactor` — the operational counterpart; sequences of small steps.
- `ddd-value-object` — closure of operations and immutability find their natural home here.
- `ddd-ubiquitous-language` — naming the implicit concept *is* growing the language.

## References

- Evans, *Domain-Driven Design* (2003), Part III, Chs 8–13 (Breakthrough, Making Implicit Concepts Explicit, Supple Design, Analysis Patterns, Design Patterns, Refactoring Toward Deeper Insight).
