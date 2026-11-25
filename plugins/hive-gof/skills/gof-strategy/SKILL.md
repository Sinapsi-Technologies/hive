---
name: gof-strategy
description: Use when behavior varies independently of the object that uses it — phrases like "Strategy pattern", "swap the algorithm at runtime", "this class has a giant switch over behavior types", "the duck class is hardcoded to one quacking style", "encapsulate the algorithm". Defines Strategy as encapsulating a family of interchangeable algorithms behind a common interface, lets clients vary the algorithm without subclassing.
---

## One-line summary

Encapsulate each member of a family of algorithms behind a common interface and let the client compose the one it needs at runtime — instead of hardcoding the algorithm or branching on a type flag.

## When to use this skill

- A class has a branching `switch`/`if` on a "type" field that selects between alternate algorithms (sort orders, pricing rules, validation strategies).
- Subclassing has been used to vary one piece of behavior, leading to a combinatorial explosion of classes.
- The set of algorithms is expected to grow.
- Two classes share most behavior but differ in a single dimension (e.g., `MallardDuck` and `RubberDuck` differ only in how they quack and fly).

## When NOT to use this skill

- Only one algorithm exists and no second one is in sight — adding the indirection is YAGNI.
- The varying behavior involves a sequence of steps with mostly identical structure — that's `gof-template-method`.
- The variation is *what gets composed* not *how it computes* — that's often `gof-decorator`.

## Core content

Strategy decouples *what the client does* from *how it does it*. The client holds a reference to a strategy object via a small interface; the concrete strategy plugs in at construction (or via a setter to change at runtime).

Composition over inheritance: instead of subclassing to vary one method, inject a small object that owns that method. Each strategy is independently testable and can be swapped without touching its host.

Pseudo-shape:
- Define an interface `Strategy { operate(input) }`.
- Concrete strategies implement it: `StrategyA`, `StrategyB`, …
- The context class holds a `Strategy` and delegates: `context.do(input) { strategy.operate(input) }`.

## Decision heuristics

- Apply when you genuinely have ≥2 algorithms today, or one today and a strong, near-term reason for a second.
- Prefer Strategy to subclass-per-algorithm explosion.
- If the strategies are stateless, they can be `object` instances (singletons) rather than allocated per-context.

## Anti-patterns

- Introducing Strategy for a single algorithm "in case we need more later" — the indirection cost shows up immediately, the benefit may never arrive.
- Strategy objects that carry too much state — Strategy is meant for *behavior*; if the strategy carries large per-call state, reconsider the boundary.
- Naming strategies for *how* they implement (`FastSortStrategy`, `SlowSortStrategy`) rather than *what they mean in the domain* (`PriorityFirstSort`, `ChronologicalSort`).

## See also

- `gof-state` — same structure, different intent (Strategy varies what's computed; State varies behavior across object lifecycle).
- `gof-template-method` — when the variation is the *steps within an algorithm*, not the whole algorithm.
- `gof-command` — for parameterizing operations rather than algorithms.
- `gof-kotlin-idioms` — in Kotlin, a strategy is often just a function-typed parameter `(T) -> R`; no interface needed.

## References

- *Head First Design Patterns* (2nd ed), Ch. 1 ("Welcome to Design Patterns") — the canonical Strategy chapter, framed around the Duck simulator example.
