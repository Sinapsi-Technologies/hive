---
name: gof-observer
description: Use when one object's state change must notify many dependent objects — phrases like "Observer pattern", "publish-subscribe", "listeners", "the weather station needs to notify multiple displays", "broadcast a change", "one-to-many notification". Defines Observer as a subject maintaining a list of observers and notifying them on state changes, without the subject knowing observer concrete types.
---

## One-line summary

Define a one-to-many dependency: when one object (the subject) changes state, all its dependents (observers) are notified automatically — without the subject knowing their concrete types.

## When to use this skill

- One object holds canonical state and several others must stay synchronized with it (data → multiple views, model → multiple listeners).
- Event-style coupling: "when X happens, do these N things, and the list of things grows over time".
- Decoupling a publisher from a set of subscribers that the publisher should not know about.

## When NOT to use this skill

- Only one downstream consumer exists — a direct call is simpler.
- The "notification" must be synchronous and block the subject's progress until subscribers complete — Observer's typical implementation is push-style and assumes subscribers are independent.
- You need event sourcing, replay, or persistence — that's a real event bus / message broker, not the in-process Observer pattern.

## Core content

The subject knows only a `notify()` method on an `Observer` interface. Concrete observers register and deregister via `attach()` / `detach()`. When the subject's state changes, it iterates its observer list and calls `notify()` on each.

Push vs pull:
- **Push** — subject passes the changed data into `notify(data)`. Simple; subjects know what observers want.
- **Pull** — subject passes itself; observer queries back for the data it cares about. Looser coupling; observers pull only what they need.

Most modern frameworks (RxJava, Kotlin Flow, EventBus libraries, JavaFX bindings, even DOM event listeners) implement variants of Observer.

## Decision heuristics

- If observers care about the same data, push is fine; if they care about different subsets of the subject's state, pull is better.
- Keep the observer interface narrow (one method) — fat observer interfaces become a maintenance burden.
- For more than a handful of subscribers or for cross-process coupling, reach for a real event bus (Kafka, RabbitMQ, Spring `ApplicationEventPublisher`) rather than rolling Observer by hand.

## Anti-patterns

- Memory leaks from observers that never deregister (the subject holds them alive forever) — make deregistration explicit and tested.
- Observers that mutate the subject during `notify()` — causes re-entrancy bugs and observer-list invalidation.
- Order-dependent observers — Observer guarantees notification, not order; if order matters, you need a different pattern.

## See also

- `gof-command` — when the "notification" needs to be queueable, undoable, or parameterized.
- `gof-kotlin-idioms` — in Kotlin, `Flow`, `SharedFlow`, and `StateFlow` are first-class Observer-shaped primitives; rolling your own Observer is rarely warranted.

## References

- *Head First Design Patterns* (2nd ed), Ch. 2 ("Keeping your Objects in the Know: the Observer Pattern") — the canonical chapter, framed around a weather monitoring station.
