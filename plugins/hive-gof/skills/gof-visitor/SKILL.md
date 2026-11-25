---
name: gof-visitor
description: Use when adding new operations over an existing object hierarchy without modifying it — phrases like "Visitor pattern", "double dispatch", "I want to add operations to this AST/tree without changing the node classes", "separate algorithm from data structure". Defines Visitor as representing an operation to be performed on the elements of an object structure — letting you define a new operation without changing the classes of the elements on which it operates.
---

## One-line summary

Represent an operation to be performed on the elements of a stable object structure — letting you add new operations to the structure without modifying its element classes.

## When to use this skill

- A stable hierarchy of types (AST nodes, file-system entries, document elements) needs new operations added over time and you don't want to keep touching each element class.
- You need to perform different operations on the same hierarchy: a pretty-printer, a type-checker, a JSON serializer, a metrics-collector. Each is a separate Visitor.
- The classical GoF "double dispatch" use case — picking which method to call based on *two* dynamic types (the element type and the operation type).

## When NOT to use this skill

- The hierarchy is itself unstable — every new element type forces every Visitor to be updated. Visitor trades "adding operations is cheap" for "adding element types is expensive".
- The language already gives you exhaustive pattern matching (Kotlin `when` over `sealed` types, Rust `match`, Scala pattern matching). In those languages, you can add an operation as a function with an exhaustive `when` — without subclassing each element.
- You only have one operation to add and there's no reason to expect more. A method on the element is simpler.

## Core content

Two parallel pieces:

**Element hierarchy.** Each element class has an `accept(visitor)` method that calls back into the visitor with itself: `accept(v) { v.visitX(this) }`. The element does nothing else for the operation.

**Visitor interface.** Declares one method per concrete element type: `visitCircle(Circle)`, `visitSquare(Square)`, etc. Each concrete visitor (Pretty-Printer, Type-Checker, JSON-Serializer) implements all of them.

Adding a new *operation* means adding a new Visitor class. Existing element classes are untouched.

The double-dispatch trick: when client code calls `element.accept(visitor)`, the dispatch first selects which `accept` to run (based on the element's runtime type), and inside that, the dispatch selects which `visitX` to run (based on the visitor's runtime type). Two levels of polymorphism cooperate.

**In Kotlin, this is usually unnecessary.** A `sealed` hierarchy plus an exhaustive `when` gives you the same "add new operations without touching element classes" benefit, without the `accept` ceremony, and the compiler enforces that you handle every element type:

```
sealed class Shape
data class Circle(...): Shape()
data class Square(...): Shape()

fun area(s: Shape): Double = when (s) {
    is Circle -> ...
    is Square -> ...
}  // compile error if a new Shape variant isn't handled
```

The trade-off flips: with sealed `when`, *adding an operation* is cheap (a new function), and *adding an element* is loud (every existing `when` must be updated — but the compiler points each one out).

## Decision heuristics

- Choose Visitor when the language lacks exhaustive pattern matching over closed hierarchies.
- In Kotlin (and similar languages), prefer `sealed` + exhaustive `when` — you get the same benefit with less ceremony and compile-time exhaustiveness checks.
- Use Visitor when operations need to *cooperate* across element types (a tree-walker that maintains state as it descends) — visitors hold state across visits more cleanly than ad-hoc functions.

## Anti-patterns

- Visitor on an unstable hierarchy — every new element type forces N visitors to be updated and the compiler doesn't help in languages without explicit declaration coverage.
- Visitor with default no-op methods to "make it easier to add elements" — silently drops behavior for types nobody remembered to handle.
- Visitor in a language where exhaustive pattern matching is available — ceremony for no benefit.

## See also

- `gof-iterator` — Visitor walks a structure; an Iterator that yields elements + an exhaustive `when` is often the modern alternative.
- `gof-composite` — Visitor is often applied over Composites; the `accept` recursion descends the tree.
- `gof-kotlin-idioms` — sealed classes + `when` are the idiomatic substitute for Visitor in Kotlin.

## References

- *Design Patterns* (GoF, 1994), Ch. 5 — Visitor. (Not in HFDP 2nd ed.)
- *Kotlin Design Patterns and Best Practices* (Alexey Soshin), Ch. 4 — Visitor, with the sealed-class comparison.
