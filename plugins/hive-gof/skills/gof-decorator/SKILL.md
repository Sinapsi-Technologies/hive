---
name: gof-decorator
description: Use when adding behavior to an object dynamically without subclassing — phrases like "Decorator pattern", "wrap this with extra behavior", "the Java I/O Streams pattern", "compose responsibilities at runtime", "I need 16 variants of Beverage and don't want 16 subclasses", "add features without modifying the underlying class". Defines Decorator as wrapping an object in another object that adds responsibility while preserving the original's interface.
---

## One-line summary

Wrap an object in another object that shares its interface and adds responsibility — at runtime, in any combination, without modifying the original.

## When to use this skill

- A class needs N orthogonal extensions (a coffee gets milk, sugar, foam, syrup … any combination). Subclassing yields N! variants; Decorator yields N small wrappers.
- Java I/O Streams (`BufferedReader(InputStreamReader(FileInputStream(...)))`) is the textbook real-world example.
- Adding cross-cutting concerns (logging, caching, validation, rate-limiting) around an existing object without touching its code.

## When NOT to use this skill

- The extensions interact non-orthogonally (sugar must be added before milk, etc.) — Decorator gives unordered composition; if order matters, you have hidden coupling.
- Only one extension exists and no second is in sight — subclassing or a flag is simpler.
- The wrapping changes the *interface*, not just behavior — that's `gof-adapter`.

## Core content

Two parts of the structure:
- A common interface (or abstract class) shared by the "core" object and every decorator.
- Each decorator holds a reference to a "wrapped" instance of the same interface and forwards calls to it, optionally adding work before or after.

Composition reads outside-in: `Foam(Milk(Espresso))` is "an espresso, then milk, then foam". The outermost decorator is the one the client uses.

The "open–closed principle" is the standard motivation — classes should be open to extension but closed to modification, and Decorator gives you extension via composition rather than subclassing.

## Decision heuristics

- If you would otherwise write `if (hasMilk) … if (hasSugar) … if (hasFoam) …` inside the core class, Decorator splits each concern into its own type.
- Each decorator should add *one* concern. If a decorator does two things, split it.
- The order of wrapping is the order of execution from the outside in — choose it deliberately and test for it.

## Anti-patterns

- "Decorator soup" — many tiny decorators with overlapping concerns, hard to reason about the actual call chain.
- Decorators that change the contract (return `null` where the wrapped object would not) — silently breaks callers.
- Using inheritance to add the behavior anyway because "the decorator is just a subclass with extra stuff" — defeats the point.

## Caveat: a decorator is not the wrapped type

A decorator implements the *interface* of what it wraps, not the concrete *class*. So `is` / `instanceof` checks against the wrapped concrete type will return false, even though the decorator behaves like it:

```
val loggedRepo: Repository = LoggingRepository(DefaultRepository())
loggedRepo is Repository           // true
loggedRepo is DefaultRepository    // false — even though it wraps one
loggedRepo is LoggingRepository    // true
```

This bites people doing runtime type checks "through" a decorator chain. If you find yourself wanting to ask "is the underlying object of type X?", the abstraction has already leaked — either treat the chain through the interface, or restructure.

## See also

- `gof-adapter` — when the wrapper changes the *interface*, not just behavior.
- `gof-proxy` — same structural shape; different intent (Proxy controls access; Decorator adds behavior).
- `gof-composite` — Decorator and Composite are structurally adjacent; both wrap; both recursively forward.
- `gof-kotlin-idioms` — Kotlin's `by` keyword (class delegation) makes decorators a one-liner.

## References

- *Head First Design Patterns* (2nd ed), Ch. 3 ("Decorating Objects: the Decorator Pattern") — canonical Starbuzz coffee example and Java I/O walkthrough.
- *Kotlin Design Patterns and Best Practices* (Alexey Soshin), Ch. 3 — Decorator, including the "Caveats of the Decorator design pattern" section that motivates the `is`-check caveat above.
