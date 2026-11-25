---
name: ddd-entity
description: Use when the user is modeling something whose identity matters beyond its attributes — phrases like "entity", "this thing has an ID", "two of these are the same if their IDs match", "tracked over time", "lifecycle". Distinguishes entities from value objects by identity continuity. Pairs with ddd-value-object (the opposite distinction).
---

## One-line summary

An entity is a domain object identified by *who it is*, not by *what it contains* — its identity is stable across attribute changes and across time.

## When to use this skill

- Modeling something tracked over time: `Customer`, `Order`, `Account`, `Shipment`, `Subscription`.
- Two instances with identical attributes are still distinct things ("two customers named John Smith are not the same customer").
- A concept has a lifecycle — created, modified, archived, deleted — and the same conceptual instance persists across those states.

## When NOT to use this skill

- The concept is fully described by its attributes with no identity → use `ddd-value-object`.
- The "identity" is just a database autoincrement with no domain meaning → that suggests the concept might actually be a value object that the persistence layer is identifying for storage reasons.
- The concept exists only inside a single aggregate and is never referenced from outside → it may still be an entity, but reconsider whether it needs to be modeled separately at all.

## Core content

Identity is the distinguishing feature. An entity's identity is *assigned* (often as a UUID, a business identifier like a customer number, or a natural key) and *stable* — it does not change when attributes change. Equality is by identity: two entities are equal if and only if their identifiers match, regardless of every other field.

Entities have lifecycles. They are created (often via a factory), modified through methods that maintain invariants, possibly archived, and possibly deleted. Throughout that lifecycle the identity remains the same.

Entities expose *behavior*, not just data. A `Customer` entity might have methods like `changeAddress`, `closeAccount`, `deactivate`. Each of those methods is responsible for enforcing the entity's invariants — the customer's state is never inconsistent.

## Decision heuristics

- Entity if you need to track its history or refer to "the same one" later.
- Value object if you would happily swap one instance for another with the same attributes.
- If a candidate concept is an entity but has no behavior beyond getters/setters, push back: either the entity is anemic (add behavior), or it is misclassified (it might really be a value object).
- Identity is assigned, not derived from attributes. If you find yourself deriving identity from fields, you are likely modeling a value object.

## Anti-patterns

- Modeling every domain object as an entity, including ones that have no inherent identity ("entity explosion").
- Entity with no behavior — an "anemic entity" that is just fields with getters. See `ddd-anti-patterns`.
- Replacing identity equality with attribute equality (a language adapter pitfall — Kotlin `data class` does this by default).
- Treating a database row's autoincrement ID as if it were the domain's identity.

## See also

- `ddd-value-object` — the opposite case; concepts without identity.
- `ddd-aggregate` — entities often serve as aggregate roots.
- `ddd-factory` — for creating entities with non-trivial construction.
- `ddd-repository` — for loading and saving entities by identity.
- `ddd-anti-patterns` — anemic entities, primitive obsession in identifiers.

## References

- Evans, *Domain-Driven Design* (2003), Ch. 5 (A Model Expressed in Software).
