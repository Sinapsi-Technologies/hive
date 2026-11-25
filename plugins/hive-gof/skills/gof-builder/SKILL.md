---
name: gof-builder
description: Use when constructing an object requires many parameters or optional configuration steps — phrases like "Builder pattern", "telescoping constructors", "I have a constructor with 12 parameters half of which are optional", "fluent API for construction", "step-by-step object construction", "type-safe builder", "DSL builder". Defines Builder as separating construction of a complex object from its representation, letting the same construction process create different representations.
---

## One-line summary

Separate the construction of a complex object from its representation — letting the same construction process produce different results, and replacing "telescoping constructors" with a fluent, readable assembly.

## When to use this skill

- An object has many constructor parameters (more than ~4), several of which are optional.
- Construction proceeds in steps (build the burger, then add toppings, then wrap it) and the steps may vary.
- You want callers to read like a recipe rather than fight a constructor signature.
- The Kotlin DSL idiom — `html { head { … } body { … } }` — is a builder.

## When NOT to use this skill

- The constructor takes 1–3 parameters and a single call is clearly readable.
- The language already gives you a better tool — Kotlin's *named arguments + default parameter values* eliminate Builder for most cases. Reach for Builder only when the construction is genuinely *staged* (intermediate validation, type-safe step ordering).
- You're building a single immutable value object — `data class` with named args is almost always enough.

## When to choose Builder over Factory (`gof-factory`)

- Factory hides *which concrete type* to instantiate.
- Builder hides *how to assemble* an instance whose type is known.

They compose: a Factory may return a Builder; a Builder may use a Factory for sub-parts.

## Core content

Two common shapes:

**Fluent builder.** A `Builder` class accumulates state via chained setter methods and ends with `build()`:

```
Burger.builder()
  .bun(Brioche)
  .patty(Beef)
  .topping(Cheese).topping(Bacon)
  .build()
```

Each setter returns `this` (or `Builder`) for chaining. `build()` validates and returns the finished object. The target type should have a private constructor so callers must go through the builder.

**Type-safe DSL builder** (Kotlin idiom, but the concept is older). A function takes a lambda with receiver; inside the lambda, methods of a builder are in scope. The Kotlin standard library's `buildString { append("hello"); append(" world") }` is the simplest example. HTML-style builders take this further.

A common mistake is implementing Builder for an object that didn't actually need it — Builder is justified when construction is *complex enough that a single constructor call obscures intent*.

## Decision heuristics

- Count the constructor parameters. Below ~4, Builder is over-engineering — use named args.
- Look for optional parameters with sensible defaults. If most parameters have defaults, named args + defaults beat Builder for readability.
- If construction requires multiple stages where stage N depends on stage N-1, Builder (especially a type-state-based one) earns its weight.
- If the target object should be immutable, `build()` must produce a fully-validated value and the builder itself should be discarded.

## Anti-patterns

- Builder where named arguments would do — ceremony with no benefit.
- Builders that allow `build()` to return an invalid object — defeats the safety purpose.
- "Builder" classes that hold mutable references back to the built object and continue to mutate it after `build()`.
- Sharing one builder instance across threads — builders are usually stateful and not safe.

## See also

- `gof-factory` — Builder hides assembly; Factory hides type choice. They compose.
- `gof-prototype` — alternative to Builder when an existing instance is the easier starting point.
- `gof-kotlin-idioms` — named arguments + default values replace Builder in most Kotlin code; type-safe builder DSLs are the principal exception.

## References

- *Design Patterns* (GoF, 1994), Ch. 3 — Builder. (Not in HFDP 2nd ed; covered there only in passing.)
- *Kotlin Design Patterns and Best Practices* (Alexey Soshin), Ch. 2 — Builder, with explicit comparison to Kotlin's named arguments.
