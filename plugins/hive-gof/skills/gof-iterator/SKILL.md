---
name: gof-iterator
description: Use when traversing a collection without exposing its internals — phrases like "Iterator pattern", "the menus use different internal structures but the diner shouldn't care", "encapsulate iteration", "hide whether it's an array, list, or hash map". Defines Iterator as providing a uniform way to access elements of a collection sequentially without revealing the collection's underlying representation.
---

## One-line summary

Provide a way to access the elements of an aggregate object sequentially without exposing its underlying representation.

## When to use this skill

- Multiple aggregate types must be traversed uniformly even though they use different internal structures (one uses an array, another a hash map, a third a linked list).
- A method consumes a collection but should not depend on which concrete collection type it is.
- Implementing a custom collection that needs to be traversable in client code (a tree, a graph, a paginated remote dataset).

## When NOT to use this skill

- The language already gives you iteration over standard collections (every modern language does) and you're not designing a custom collection — just use the language's `for-each` / iteration.
- The "iteration" is really a transform or filter pipeline — that's a higher-level concept; reach for the language's streams / sequences.
- You need *random access*, not sequential access — Iterator is sequential by definition.

## Core content

The pattern separates two concerns:

- The **Aggregate** owns the data and knows its internal structure.
- The **Iterator** knows how to walk that structure one element at a time.

The aggregate provides a method (typically `iterator()`) that returns a fresh Iterator. The Iterator implements the standard interface (`hasNext()`, `next()`, occasionally `remove()`).

Multiple iterators may exist over the same aggregate at the same time, each tracking its own position — that's why iteration state belongs to the Iterator, not the aggregate.

The pattern is so foundational that virtually every modern language has it built into the language and standard library. Kotlin, Java, C#, Python, Swift, Rust, JavaScript — all have `Iterator` or `Iterable` interfaces and `for` loops that consume them.

## Decision heuristics

- For built-in collections, never roll your own Iterator — the language provides one.
- When designing a custom collection or stream, implement the language's standard Iterator interface so client code can use idiomatic iteration (Kotlin `for` over `Iterable`, Java `for-each`, Python `for x in y`).
- For a one-time traversal that filters or transforms, prefer a stream / sequence over a stateful iterator.

## Anti-patterns

- Exposing the underlying collection (`getInternalList()`) and letting callers iterate it directly — defeats encapsulation; callers depend on the concrete type.
- Iterators that mutate the aggregate during traversal in ways the aggregate didn't sanction — usually causes concurrent-modification bugs.
- Custom iterators that don't match the language's standard interface — forces client code into bespoke patterns.

## See also

- `gof-composite` — Iterator is the natural way to walk a Composite structure.
- `gof-kotlin-idioms` — `Sequence`, `Iterable`, the `Iterator<T>` interface, and `iterator { yield(x) }` builders are first-class; rolling your own iterator class is rarely the right move.

## References

- *Head First Design Patterns* (2nd ed), Ch. 9 ("Well-Managed Collections: the Iterator and Composite Patterns") — the canonical chapter, framed around two restaurants merging menus stored in different data structures.
