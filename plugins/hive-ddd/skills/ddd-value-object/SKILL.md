---
name: ddd-value-object
description: Use when the user is modeling a descriptive concept with no identity — phrases like "value object", "Money", "Address", "DateRange", "primitive obsession", "wrap this primitive into a type", "two instances with the same attributes are the same". Distinguishes from entities; pairs with ddd-entity.
---

## One-line summary

A value object is a concept defined entirely by its attributes — no identity, immutable, equal by value.

## When to use this skill

- Modeling `Money`, `Address`, `DateRange`, `EmailAddress`, `ProductCode`, `Percentage`, `PhoneNumber`.
- Replacing a primitive (`String`, `Long`, `BigDecimal`) that carries invariants the type system doesn't enforce.
- Distinguishing two concepts that share an underlying primitive type (`CustomerId` vs `OrderId` — both UUIDs, never interchangeable).
- Encapsulating domain operations that belong with a concept: `Money.add(Money)`, `Address.normalize()`, `DateRange.contains(Instant)`.

## When NOT to use this skill

- The concept has a lifecycle or identity → use `ddd-entity`.
- The wrap would add neither invariants nor behavior — wrapping for the sake of wrapping is ceremony.
- A primitive is used in only one place and never crosses boundaries — the cost of the wrap may exceed the benefit.

## Core content

Value objects are *immutable*: once constructed, their fields never change. Operations on them return new value objects rather than mutating the receiver. They are *equal by value*: two value objects with the same attribute values are interchangeable.

They are also a natural home for *domain operations*. `Money` should have an `add` method that refuses to add USD to EUR. `Address` might have a `normalize` method that returns a canonical form. `DateRange` should know whether an `Instant` falls inside it. Putting these operations on the value object — rather than on a `MoneyUtils` static helper — gives the domain a richer vocabulary.

Validation belongs in construction. A `EmailAddress("notanemail")` should fail at the point of construction, not later when something tries to use it. This is what "making invalid states unrepresentable" means.

## Decision heuristics

- Wrap a primitive when (a) it has invariants the language doesn't express, (b) it has operations specific to the domain, or (c) two domain concepts share a primitive type and should not be interchangeable.
- Prefer immutable value objects even in languages that allow mutable ones.
- If you are about to write `MoneyUtils.add(money1, money2)`, stop — the operation belongs on `Money`.

## Anti-patterns

- Mutable value objects — setters break equality semantics and invite shared-state bugs.
- Value objects with identity fields — that is an entity in disguise.
- Treating every primitive as in need of wrapping — over-engineering. Wrap when it carries domain meaning, not by reflex.
- Validation that throws generic `IllegalArgumentException` with no message — define expressive domain exceptions or use a result type.

## See also

- `ddd-entity` — the opposite case; concepts with identity.
- `ddd-anti-patterns` — primitive obsession, mutable value objects.
- `ddd-kotlin-idioms` — how to express value objects idiomatically in Kotlin (`data class`, `value class`).

## References

- Evans, *Domain-Driven Design* (2003), Ch. 5 (A Model Expressed in Software).
