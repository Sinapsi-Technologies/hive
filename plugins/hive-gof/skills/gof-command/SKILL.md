---
name: gof-command
description: Use when encapsulating a request as an object — phrases like "Command pattern", "undo/redo", "queue this operation", "remote control with arbitrary buttons", "macro of operations", "the invoker shouldn't know what the operation does". Defines Command as wrapping a request (target + action + arguments) into an object that can be parameterized, queued, logged, or undone.
---

## One-line summary

Encapsulate a request as an object — separating *who triggers* the action from *what gets done* — so requests can be parameterized, queued, logged, undone, or composed.

## When to use this skill

- An invoker (UI button, scheduler, queue handler) must trigger actions but should not know what they do.
- Operations need to be queued, scheduled, retried, or logged.
- Undo/redo functionality: each command knows how to execute and how to unexecute itself.
- Building a macro recorder — a command that holds a list of commands and runs them in sequence.
- Decoupling the timing of "what should happen" from "when it actually happens".

## When NOT to use this skill

- A direct method call is sufficient and there's no decoupling need.
- The "command" is a single function with no arguments and no lifecycle — that's just a function reference.
- The system already has a job queue / message bus that handles this concern.

## Core content

A Command bundles together:
- The **receiver** (the object the action is performed on).
- The **action** (the operation to invoke).
- The **arguments** (any data the action needs).

The **invoker** holds a Command reference and calls `execute()` without knowing what the command does internally. Concrete commands implement `execute()` and (optionally) `undo()`.

The canonical example: a TV remote where each button holds a Command. The remote doesn't know whether a button turns on a light, opens a garage door, or starts the stereo — each button just calls `execute()` on its command.

Composition: a `MacroCommand` holds a list of `Command`s and executes them in order. Undoing a macro reverses the order.

## Decision heuristics

- If you need undo/redo, Command is your starting point.
- If you need scheduling, retrying, or remote execution of actions, Command is the natural unit of transport.
- Each Command should be small and have one clear purpose. Large multi-step commands belong in a `MacroCommand`.
- If `execute()` and `undo()` have to maintain state about the previous state for undo to work, the Command must capture that state at execute time (memento-style).

## Anti-patterns

- Commands that mutate global state and have no `undo()` — defeats the pattern's main value.
- Command classes with a single `execute()` and no other reason for existing — a function reference would do.
- Putting business logic inside the Command instead of delegating to the receiver — Commands are *requests*, not implementations.
- "Just one big GodCommand with a switch over a type code" — that's a Simple Factory hiding inside a Command class; split the variants into separate command types.

## See also

- `gof-strategy` — both wrap behavior, but Strategy is for *swapping algorithms*; Command is for *queueing/scheduling/undoing requests*.
- `gof-observer` — when commands are triggered by events.
- `gof-kotlin-idioms` — Kotlin function types (`() -> Unit`) and lambda expressions cover the simplest Command cases without an interface.

## References

- *Head First Design Patterns* (2nd ed), Ch. 6 ("Encapsulating Invocation: the Command Pattern") — canonical exposition, framed around a programmable home remote control.
