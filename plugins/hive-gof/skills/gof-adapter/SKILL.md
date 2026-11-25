---
name: gof-adapter
description: Use when an incompatible interface needs to look like an expected one — phrases like "Adapter pattern", "the third-party library's API doesn't match what I need", "wrap this legacy class to look like the new interface", "interface mismatch", "translate between two APIs". Defines Adapter as wrapping one interface to satisfy another, so client code can use otherwise incompatible classes through a uniform interface.
---

## One-line summary

Convert one interface into another the client expects — letting classes work together that couldn't otherwise because of incompatible interfaces.

## When to use this skill

- Integrating a third-party library that has a different interface than what your code expects.
- Migrating from an old API to a new one — write an adapter that exposes the old interface in terms of the new (or vice versa) and switch incrementally.
- Two systems must talk and you can't change either — adapter sits between them.
- The classic Adapter example: an electrical plug adapter that lets a US plug fit a European socket; the underlying current is the same, only the interface differs.

## When NOT to use this skill

- The two interfaces serve genuinely different conceptual purposes — an adapter would be doing translation work that hides a real domain decision; surface the decision instead.
- Only one method needs adapting and the host class can change to call the source directly — adapter is overkill.
- The wrapper *adds behavior* rather than *changes interface* — that's `gof-decorator`.
- The wrapper *hides complexity* of a subsystem — that's `gof-facade`.

## Core content

Two structural variants:

**Object Adapter** (the common one). The adapter holds a reference to an instance of the adaptee and implements the target interface by delegating to it. Composition-based. Works in any OO language.

**Class Adapter**. The adapter inherits from both the target and the adaptee (requires multiple inheritance, so it's rare outside C++). Subclass-based.

The motivating example in HFDP is the Iterator / Enumeration translation: legacy code returns `Enumeration`; modern code expects `Iterator`. An `EnumerationIterator` adapter wraps the legacy and exposes the modern interface.

Two-way adapters exist (an adapter that satisfies both interfaces and translates in either direction) but they double the surface and rarely earn their weight.

## Decision heuristics

- Build an adapter at the boundary between *your code* and *foreign code* — never deep inside your domain.
- Keep the adapter narrow: implement only the target methods your code actually uses, not the entire interface.
- Adapter is a translation layer; if the adapter starts containing business logic, the business logic belongs elsewhere.

## Anti-patterns

- "Adapter as integration layer" — an adapter that has grown to handle authentication, retry, caching, and logging. Split each concern out.
- Adapters that silently swallow exceptions from the adaptee — the adapter must translate failures too.
- Adapters around your own code — if both sides are yours, change one to match instead of adapting.

## See also

- `gof-decorator` — same structural shape, different intent (Decorator adds behavior; Adapter changes interface).
- `gof-facade` — when wrapping a *subsystem* (multiple classes) into a simpler interface, not a single class to a different interface.
- `gof-proxy` — when controlling access to an object, not changing its interface.
- DDD `ddd-context-mapping` — the Anticorruption Layer is an Adapter at the bounded-context boundary.
- `gof-kotlin-idioms` — Kotlin extension functions can act as one-off adapters for single methods without introducing a class.

## References

- *Head First Design Patterns* (2nd ed), Ch. 7 ("Being Adaptive: the Adapter and Facade Patterns") — canonical chapter, includes the duck-to-turkey adapter example.
