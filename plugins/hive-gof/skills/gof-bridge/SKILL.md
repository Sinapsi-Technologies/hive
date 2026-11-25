---
name: gof-bridge
description: Use when an abstraction and its implementation must vary independently — phrases like "Bridge pattern", "I have N shapes times M renderers and don't want N*M subclasses", "decouple the abstraction from its implementation", "two orthogonal dimensions of variation". Defines Bridge as separating an abstraction from its implementation so the two can vary independently — a cousin of Adapter, but designed up-front rather than retrofitted.
---

## One-line summary

Decouple an abstraction from its implementation so that the two can vary independently — replacing a combinatorial subclass explosion with composition along two orthogonal dimensions.

## When to use this skill

- Two orthogonal dimensions of variation: N shapes × M renderers, or N drawing tools × M rendering backends. Without Bridge, you get an N×M subclass explosion.
- You expect both the abstraction (e.g., `Shape`) and the implementation (e.g., `Renderer`) to evolve independently over time, possibly maintained by different teams.
- You want to switch implementations at runtime — choose a different renderer per platform, per environment, per configuration.

## When NOT to use this skill

- Only one of the two dimensions actually varies — the other has only one implementation and no second is in sight. Bridge is over-engineering.
- The "two dimensions" aren't truly orthogonal — they interact in ways that force coupling. Bridge's value depends on independence.
- You're retrofitting two existing incompatible interfaces — that's `gof-adapter`, not Bridge. Bridge is designed up front.

## Core content

Two parallel hierarchies:
- **Abstraction** — the client-facing concept (`Shape`, `RemoteControl`, `Notification`). Holds a reference to an Implementor.
- **Implementor** — the underlying interface (`Renderer`, `Device`, `DeliveryChannel`). Concrete implementors plug in at runtime.

The Abstraction delegates concrete work to the Implementor it holds. The two hierarchies grow independently: adding a new Shape doesn't require new Renderer subclasses, and vice versa.

```
abstract class Shape(val renderer: Renderer) {
    abstract fun draw()
}

interface Renderer { fun renderCircle(x, y, r); fun renderSquare(x, y, s) }

class Circle(renderer: Renderer, val x, y, r): Shape(renderer) {
    override fun draw() = renderer.renderCircle(x, y, r)
}
class VectorRenderer: Renderer { ... }
class RasterRenderer: Renderer { ... }
```

Adding a new Shape (`Triangle`) requires defining how it draws via *any* `Renderer`. Adding a new `Renderer` (`SVGRenderer`) makes every existing Shape work in SVG for free.

**Bridge vs Adapter.** Same composition structure, different intent and timing:
- *Adapter* retrofits an incompatible interface — applied after the fact, often at a system boundary.
- *Bridge* is a deliberate design choice to decouple abstraction from implementation — designed up front to allow both to vary.

## Decision heuristics

- Bridge pays off when both dimensions have ≥2 variants and you expect both to grow.
- If only the implementation varies but the abstraction is fixed, you have `gof-strategy`, not Bridge.
- Inject the Implementor via constructor (or DI) so it can be swapped without modifying the Abstraction.

## Anti-patterns

- Bridge introduced when only one side actually varies — abstract-class-with-one-subclass plus interface-with-one-implementation is pure ceremony.
- Bridge where the Implementor leaks back into the Abstraction's API — defeats the decoupling.
- Hardcoding `Implementor` selection inside the Abstraction's constructor — preserves the coupling Bridge was supposed to break.

## See also

- `gof-adapter` — same composition structure, retrofitted to existing incompatible interfaces.
- `gof-strategy` — Bridge with only one dimension of variation (the implementation).
- `gof-kotlin-idioms` — Bridge in Kotlin is often just composition with an interface parameter, no abstract class needed.

## References

- *Design Patterns* (GoF, 1994), Ch. 4 — Bridge. (Not in HFDP 2nd ed.)
- *Kotlin Design Patterns and Best Practices* (Alexey Soshin), Ch. 3 — Bridge.
