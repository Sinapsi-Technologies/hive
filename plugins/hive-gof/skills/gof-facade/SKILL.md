---
name: gof-facade
description: Use when simplifying access to a complex subsystem — phrases like "Facade pattern", "home theater example", "make this subsystem easier to use", "hide the complexity behind one method", "high-level interface to many classes". Defines Facade as a single entry-point class that orchestrates a set of subsystem classes behind a simpler interface, without preventing direct subsystem access.
---

## One-line summary

Provide a unified, simplified interface to a set of interfaces in a subsystem — making the subsystem easier to use without preventing direct access for power users.

## When to use this skill

- A subsystem has many parts and most callers only want a small, common combination of operations on them.
- Onboarding new code into a complex codebase — Facade gives the new code one clean entry point.
- Wrapping a legacy or third-party library whose internal complexity should not leak everywhere.
- The canonical example: a "watch a movie" facade that turns on the TV, dims the lights, lowers the screen, switches the receiver to the right input, queues the popcorn maker, and starts the DVD — instead of having every caller orchestrate those steps.

## When NOT to use this skill

- You're trying to *replace* the subsystem rather than offer a simpler entry point — that's a refactor of the subsystem.
- You're trying to *change the interface* of a single class — that's `gof-adapter`.
- The subsystem is genuinely small and a Facade would only add a layer of indirection.
- Callers actually need the full control the subsystem offers; a Facade that hides too much forces them to bypass it.

## Core content

A Facade is a class that holds references to the subsystem classes it orchestrates and exposes a small set of high-level methods. Each method on the Facade composes multiple lower-level operations into a coherent unit of meaning at the level the caller cares about.

Key principle of Facade: it **does not prevent access to the subsystem**. Callers that need finer control can still reach in directly. The Facade is a convenience, not a wall.

Related principle that HFDP attaches to Facade: the **Principle of Least Knowledge** (the Law of Demeter). Facade methods talk to a small number of close collaborators rather than reaching deep into the subsystem from afar.

## Decision heuristics

- A Facade method should read like a unit of *user intent* ("watchMovie", "shutdownProperly", "settleOrder") — not like a thin pass-through to one subsystem call.
- Keep the Facade itself stateless when possible; it should be orchestration, not state ownership.
- If you find yourself adding many setters to the Facade, it has stopped being a Facade.

## Anti-patterns

- Facade that *forbids* direct subsystem access — a Facade is convenience, not a fortress.
- Facade with one method per subsystem method, named identically — that's a pass-through, not a Facade.
- Facade that grows to know every subsystem detail — at some point it stops being "high-level" and becomes a god class.
- Multiple Facades over the same subsystem, each adding a slightly different convenience — usually a sign that the underlying subsystem needs refactoring instead.

## See also

- `gof-adapter` — when wrapping *one class* to a *different interface*; Facade wraps *many classes* into a *simpler interface*.
- `gof-mediator` (not covered here, GoF original) — Facade is one-directional (caller → subsystem); Mediator is bidirectional coordination between peers.
- `gof-kotlin-idioms` — top-level functions and extension functions in Kotlin often serve as lightweight facades without needing a class.

## References

- *Head First Design Patterns* (2nd ed), Ch. 7 ("Being Adaptive: the Adapter and Facade Patterns") — the home theater example is the canonical illustration.
