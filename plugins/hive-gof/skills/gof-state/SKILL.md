---
name: gof-state
description: Use when an object's behavior must change based on its internal state — phrases like "State pattern", "the gumball machine behaves differently in each state", "state machine", "huge switch over a status enum", "the same action does different things depending on the current state". Defines State as encapsulating each state of an object in its own class and letting the context object delegate behavior to whichever state it currently holds.
---

## One-line summary

Allow an object to alter its behavior when its internal state changes — making the object appear to change its class. Each state is encapsulated in its own class; the context delegates behavior to whichever state it currently holds.

## When to use this skill

- An object has multiple, well-defined states and its behavior depends meaningfully on which state it is in (gumball machine, order lifecycle, network connection, parser).
- The current implementation has a large `switch` or `if-else` over a status enum where each branch does substantially different work.
- The set of states is bounded and known; transitions between them are limited and rule-bound.

## When NOT to use this skill

- Only two states with trivially different behavior — a single boolean flag and a small `if` is simpler.
- The "state" is actually data, not behavior — that's a value object, not a state machine.
- The states are *not* mutually exclusive (multiple flags can be true simultaneously) — that's not a state machine; it's a feature flag matrix.

## Core content

The pattern has three parts:

- **Context** — the object whose behavior changes (the Gumball Machine). It holds a reference to a current `State` object and exposes operations that delegate to it.
- **State interface** — declares the operations that vary by state (`insertQuarter()`, `ejectQuarter()`, `turnCrank()`, `dispense()`).
- **Concrete states** — implement the State interface; each represents one specific state with its own behavior. State transitions are typically driven *by the state itself* — the current state, given an input, sets the context's next state.

The pattern can be implemented with the states *being* singletons (stateless) or with each state holding per-context state. Choose the simpler form first.

**State vs. Strategy.** Structurally identical: a context that delegates to a swappable behavior object. Different in *intent*:
- Strategy: the *client* chooses which algorithm at construction time.
- State: the *context* changes its own state internally as a consequence of operations.

## Decision heuristics

- Recognize the "big switch on status" smell — that's the canonical reason to reach for State.
- Define the state diagram first (which states exist, what transitions are legal). The state classes should mirror the diagram one-to-one.
- Centralize the transitions either in the context (simpler, but the context knows all the states) or in the state classes (more decentralized, but each state must know what comes next).

## Anti-patterns

- State explosion — every minor variant of behavior becomes a new state class. Reconsider whether the variations are truly different states or just parameters.
- States that mutate context state directly without going through the context's methods — couples states to the context's internals.
- Implementing State for a use case where the "states" don't actually have different behaviors — you're naming things after a pattern instead of using it.
- Forgetting to handle illegal transitions — a `Dispensed` state receiving an `insertQuarter()` call should respond explicitly, not silently no-op.

## See also

- `gof-strategy` — identical structure; different intent (and different *who chooses*).
- `gof-command` — when an operation triggers a state transition, the operation itself can be a Command.
- `gof-kotlin-idioms` — Kotlin sealed classes plus exhaustive `when` give compile-time-checked state machines without subclass-per-state.

## References

- *Head First Design Patterns* (2nd ed), Ch. 10 ("The State of Things: the State Pattern") — canonical exposition framed around a gumball machine with No-Quarter / Has-Quarter / Sold / Sold-Out states.
