---
name: gof-composite
description: Use when treating individual objects and groups of objects uniformly — phrases like "Composite pattern", "tree structure", "menus and submenus both behave like menu items", "I want to call `display()` on either a leaf or a branch and have it work", "part-whole hierarchy". Defines Composite as composing objects into tree structures so clients can treat individual leaves and composite branches uniformly through a shared interface.
---

## One-line summary

Compose objects into tree structures to represent part-whole hierarchies, letting clients treat individual objects (leaves) and compositions of objects (branches) uniformly.

## When to use this skill

- A part-whole hierarchy: file system (files and directories), menus and submenus, GUI components (button vs panel of components), organizational hierarchies.
- Client code needs to call the same operations on both leaves and branches and not care which it has.
- Recursive structures where the recursion shouldn't show in the calling code.

## When NOT to use this skill

- A flat collection — there's no hierarchy, just use a list.
- The operations are genuinely different for leaves vs. branches and forcing them through one interface awkwardly buries the distinction.
- Two-level structures with only one layer of grouping — sometimes a `List<Item>` is enough without inventing a Composite.

## Core content

The pattern introduces a common interface (`Component`) shared by leaves and composites. Both implement the operations the client cares about; composites implement them by delegating to their children.

```
Component { operation() }
  Leaf { operation()  /* does the work */ }
  Composite { children: List<Component>
              operation() { children.forEach { it.operation() } }
              add(c); remove(c); }
```

Trade-off about *transparency vs safety*:
- **Transparent Composite** — `Component` declares `add(child)`. Both leaves and composites have the method. Clients can treat them uniformly but `Leaf.add(child)` either no-ops or throws.
- **Safe Composite** — `add(child)` lives only on `Composite`. Type-safe but clients must check the type before adding.

HFDP favors transparent in its examples; in practice, safe is often cleaner unless uniform handling really matters.

## Decision heuristics

- Composite is worth its weight when *all the operations* meaningfully apply to both leaves and branches.
- If only one operation applies uniformly and the rest differ, the abstraction is forced — consider whether you need Composite at all.
- For very deep or very wide trees, watch out for stack-recursive operations exploding the stack — flatten with an Iterator if needed.

## Anti-patterns

- Composite with operations that only make sense on leaves, no-oped on composites (or vice versa) — uniformity is performative, not real.
- "Composite" that's really a list — no recursion, no branches with children. It's just a collection.
- Composite that exposes its children directly via `getChildren()` and lets callers mutate the tree externally — destroys invariants.

## See also

- `gof-iterator` — the natural way to walk a Composite tree.
- `gof-decorator` — same structural shape (both wrap), different intent (Decorator adds behavior; Composite represents part-whole).
- `gof-kotlin-idioms` — sealed class hierarchies (`sealed interface Component { class Leaf; class Branch(val children: List<Component>) }`) plus exhaustive `when` make Composite explicit and type-safe.

## References

- *Head First Design Patterns* (2nd ed), Ch. 9 ("Well-Managed Collections: the Iterator and Composite Patterns") — Composite is illustrated with menus-of-menus.
