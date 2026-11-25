---
name: gof-factory
description: Use when object creation is non-trivial enough to deserve its own abstraction — phrases like "Factory pattern", "Factory Method", "Abstract Factory", "Simple Factory", "I keep `new`-ing different concrete types", "creation logic is leaking into client code", "the constructor takes too much and is doing too much". Covers Simple Factory, Factory Method, and Abstract Factory together, with picking criteria for choosing between them.
---

## One-line summary

Centralize the choice of which concrete class to instantiate, hiding it from clients — in three flavors of increasing scope: Simple Factory (one method picks a type), Factory Method (subclasses pick a type), Abstract Factory (a family of related types).

## When to use this skill

- Client code is full of `new ConcreteX()` decisions and the choice depends on input data, configuration, or environment.
- Two parallel hierarchies need to pick matched implementations together (e.g., a `NYStylePizzaStore` should produce `NYStyleDough`, `NYStyleSauce` — that's Abstract Factory).
- Subclasses should decide *which* concrete type to instantiate while sharing the surrounding workflow — Factory Method.
- Construction itself has invariants that the constructor alone can't enforce.

## When NOT to use this skill

- Only one concrete type exists; just call its constructor.
- A simple top-level function (`fun parsePayment(json): Payment`) is enough — naming a class `*Factory` for a single function adds nothing.
- Construction is genuinely trivial and adding indirection just because "factories are best practice" is ceremony.

## Core content

Three increasingly-scoped flavors, each solving a more specific problem:

**Simple Factory** (not strictly a GoF pattern, but the entry point). A single method or class that takes a discriminator (a string, an enum, a flag) and returns the right concrete instance. Centralizes `new`. Works when the discriminator is known at the call site.

**Factory Method**. Defers the choice of concrete class to a subclass. The base class implements a workflow that includes `factoryMethod()` (abstract); each subclass overrides the method to return the appropriate concrete type. Use when the surrounding workflow is shared but the type to produce varies by subclass.

**Abstract Factory**. A factory that produces *families* of related products. The factory interface declares one method per product type (e.g., `createDough`, `createSauce`); concrete factories produce matched-style implementations. Use when types must be created in matching sets.

## Decision heuristics

- Start with Simple Factory. Promote to Factory Method only when a workflow inheritance hierarchy genuinely exists. Promote to Abstract Factory only when product families must be kept consistent (NY-style with NY-style; Chicago-style with Chicago-style; never mix).
- A single static creator method on the target class itself (e.g., `Pizza.of(type)`) is often enough — don't reflexively introduce a separate factory class.
- Factories that also persist or mutate global state are doing too much — keep creation pure.

## Anti-patterns

- Factory class per type (`OrderFactory`, `CustomerFactory`, `AddressFactory`) where the constructor would have sufficed — pattern theatre.
- "FactoryFactory" — almost always a sign of over-engineering.
- Abstract Factory used when there is only one concrete factory ever — the abstraction has no plurality to serve.

## See also

- `gof-singleton` — factories are often singletons; consider the trade-offs.
- `gof-strategy` — factories often *produce* strategies; the two compose.
- `gof-kotlin-idioms` — Kotlin's `companion object` factory methods, sealed-class factories with `when` matching, and constructor function expressions cover most cases without separate factory classes.

## References

- *Head First Design Patterns* (2nd ed), Ch. 4 ("Baking with OO Goodness: the Factory Pattern") — Simple Factory, Factory Method, and Abstract Factory, framed around the pizza store example.
