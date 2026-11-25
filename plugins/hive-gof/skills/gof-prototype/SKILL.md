---
name: gof-prototype
description: Use when creating new instances by copying an existing one rather than constructing from scratch — phrases like "Prototype pattern", "clone this object", "copy an instance with one field changed", "registry of prototypes", "the constructor is expensive but I have a finished example". Defines Prototype as specifying the kinds of objects to create using a prototypical instance and creating new objects by copying that prototype.
---

## One-line summary

Create new objects by copying an existing instance (the prototype) rather than by calling a constructor — useful when construction is expensive, when you only want to vary a few fields, or when you have a registry of canned shapes.

## When to use this skill

- Object construction is expensive or has side effects that you don't want repeated.
- You have a "template" instance and want variations of it with a few fields changed.
- You need a registry of canned instances that consumers can clone and customize.
- The canonical Kotlin manifestation: `data class.copy(field = newValue)` is exactly this pattern.

## When NOT to use this skill

- Construction is cheap and stateless — just call the constructor.
- The "copy" must be a deep clone of a complex object graph — Prototype's contract is usually a *shallow* copy; deep cloning is its own problem.
- You're trying to dodge a hard construction problem by "copying" something that doesn't really exist yet — that's a smell.

## Core content

Two flavors:

**Simple Prototype.** The object has a `clone()` (or `copy()`) method that returns a new instance with the same field values. Callers obtain a base instance, copy it, and mutate the copy. In Kotlin, every `data class` provides `copy()` for free, optionally with named-argument overrides.

```
val baseRequest = HttpRequest(method = GET, url = "/users", headers = standardHeaders)
val authedRequest = baseRequest.copy(headers = standardHeaders + auth)
```

**Prototype Registry.** A central registry holds named prototype instances. Consumers ask the registry for a prototype by name (`"weekday-shift"`, `"weekend-shift"`) and get a clone they can customize. Useful when prototypes are configuration data rather than code.

The pattern's value is *making intent obvious*: "this new instance is *like* that existing one, with these differences" reads better than reconstructing every field.

## Decision heuristics

- In Kotlin, `data class.copy()` is the default Prototype. Reach for an explicit registry only when prototypes are named, configured, or numerous.
- For non-`data class` types, decide deliberately whether `copy()` should be a deep or shallow copy and document it.
- Beware of nested mutable state — `copy()` of a `data class` copies the references, not the referenced objects. Mutating a shared nested list breaks both the original and the copy.

## Anti-patterns

- `clone()` methods that are *almost* but not exactly a copy — accidentally produce subtly different instances. Either copy exactly or rename the method.
- Mutable prototypes shared across consumers without copying — the registry's instances drift over time.
- Implementing Prototype as a workaround for a missing constructor — fix the constructor instead.

## See also

- `gof-factory` — sometimes the right answer instead of Prototype, especially when each "variation" is really a different concrete type.
- `gof-builder` — when the variations are structural (different steps), not field-level overrides.
- `gof-kotlin-idioms` — `data class.copy()` is Prototype-as-language-feature.

## References

- *Design Patterns* (GoF, 1994), Ch. 3 — Prototype. (Not in HFDP 2nd ed.)
- *Kotlin Design Patterns and Best Practices* (Alexey Soshin), Ch. 2 — Prototype, framed around Kotlin's `copy()`.
