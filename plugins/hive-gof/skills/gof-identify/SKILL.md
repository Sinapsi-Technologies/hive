---
name: gof-identify
description: Use when the user has a design problem and is asking which GoF pattern (if any) fits — phrases like "which design pattern should I use", "is this an Observer or a Mediator", "what pattern handles this", "I'm not sure if it's Strategy or State", "help me pick a pattern for this scenario". Diagnostic mode that walks the problem statement to a candidate pattern (or rules patterns out) and hands off to the relevant pattern skill.
---

## One-line summary

Diagnose which Gang-of-Four pattern fits a stated design problem — or rule patterns out and point at a simpler solution.

## When to use this skill

- The user has described a design problem and wants to know which pattern applies.
- The user is confused between two patterns that look similar (Strategy vs State, Decorator vs Proxy, Adapter vs Facade).
- The user is about to introduce a pattern but isn't sure it's the right one.

## When NOT to use this skill

- The user already knows the pattern they want — go directly to that pattern skill (e.g., `gof-observer`).
- The user wants to refactor existing code toward a known pattern → route to `gof-refactor-to-pattern`.
- The user wants to audit code for misused patterns → route to `gof-review`.

## Core content

Walk the problem with these diagnostic questions, in this order:

**1. What is varying?**
- *The algorithm itself, client-chosen at runtime*: `gof-strategy`.
- *The algorithm itself, changing with object lifecycle*: `gof-state`.
- *Steps within an algorithm, base class fixes the skeleton*: `gof-template-method`.
- *Behavior added/removed at runtime, in any combination*: `gof-decorator`.

**2. Is it a creation problem?**
- *Choosing which concrete class to instantiate*: `gof-factory`.
- *Assembling an object via many (often optional) parameters or staged steps*: `gof-builder`.
- *Creating a new instance by copying an existing one with a few fields changed*: `gof-prototype`.
- *Making sure there's only one instance*: `gof-singleton` — but check first whether language features (Kotlin `object`, DI scoping) make the pattern unnecessary.

**3. Is it a "wrap one object in another" problem?**
- *Add behavior*: `gof-decorator`.
- *Change interface*: `gof-adapter`.
- *Control access (lazy/remote/permission)*: `gof-proxy`.
- *Simplify a subsystem behind one entry point*: `gof-facade`.

**4. Is it a structural problem?**
- *Part-whole hierarchy, treat leaves and branches uniformly*: `gof-composite`.
- *Traverse a collection without exposing its internals*: `gof-iterator`.
- *Two orthogonal dimensions of variation (NxM subclass explosion otherwise)*: `gof-bridge`.

**5. Is it a behavioral / communication problem?**
- *One subject, many dependents that need updates*: `gof-observer`.
- *Encapsulate a request as an object (queue, log, undo)*: `gof-command`.
- *Add new operations over a stable hierarchy without modifying its element classes*: `gof-visitor` — but in Kotlin, prefer `sealed` + exhaustive `when`.

**6. None of the above?** The problem may not need a GoF pattern at all. Common cases:
- A plain function would solve it.
- The language has a feature that supersedes the pattern (Kotlin `object` for Singleton, `by` delegation for Decorator, function types for Strategy).
- The problem is in the domain, not in the design — route to `ddd-design` or similar.

## Decision heuristics

- Don't reach for a pattern when "a function" or "a language feature" would do.
- Strategy and State have identical structure — distinguish by intent (client-chosen vs lifecycle-driven).
- Decorator and Proxy have identical structure — distinguish by intent (add behavior vs control access).
- Adapter and Facade differ in scope — single class to a different interface vs subsystem to a simpler one.

## Anti-patterns

- "Let's introduce a pattern" before identifying the problem — premature abstraction.
- Naming things after patterns (`OrderManager`, `CustomerStrategy`) that don't actually use the pattern — pattern theatre.
- Choosing the pattern based on what the team is excited about rather than what the problem calls for.

## See also

- All thirteen pattern skills (`gof-strategy`, `gof-observer`, …).
- `gof-review` — when the question is "is this code using the pattern correctly?" rather than "which pattern fits?".
- `gof-refactor-to-pattern` — when the destination is known and the question is "how do I get there?".

## References

- *Head First Design Patterns* (2nd ed), Ch. 13 ("Patterns in the Real World") — discusses how to recognize which pattern applies in practice.
- *Design Patterns* (Gamma, Helm, Johnson, Vlissides, 1994) — the original pattern catalog and its "intent / motivation / applicability" framing.
