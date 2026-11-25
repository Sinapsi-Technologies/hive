---
name: tdd-obvious-implementation
description: >-
  Use when the production code for a red test is immediately clear — phrases like "obvious implementation", "just type the code", "I know exactly what this should be", "skip the fake", "don't ceremoniously fake-it when you can see the answer". Defines Beck's Obvious Implementation technique: when you know what to type, type it directly. Faking and triangulating are for when you don't.
---

## One-line summary

When you know exactly what the production code should be, type it in. Don't fake-it. Don't triangulate. Run the test; if it goes green, move on. If it doesn't, back up to `fake-it`.

## When to use this skill

- The behavior is small and the algorithm is obvious (`max`, `min`, simple arithmetic, a known formula).
- You've done this exact thing many times; the muscle memory is reliable.
- The test is small enough that "just write the code" takes less time than the fake-then-generalize ladder.

## When NOT to use this skill

- You think the implementation is obvious but the test is going red on it. Your "obvious" is wrong; back up to `tdd-fake-it`.
- You can see *part* of the implementation but the edge cases are unclear. `fake-it` then triangulate is safer.
- You're tempted to write a "small obvious implementation" that's actually 50 lines. That's not obvious; that's a child test waiting to happen.

## Core content

The move:

```kotlin
// Test:
assertEquals(7, add(3, 4))

// Obvious implementation:
fun add(a: Int, b: Int) = a + b
```

That's the whole technique. No fake. No triangulation. Just type the code and watch the test go green.

**Why it's a named pattern, not just "doing the obvious".** Beck calls it out because the temptation in TDD is to *over-ceremonialize* — every test gets a fake-it pass, then a triangulation pass, then a generalization. For genuinely simple cases that's wasted cycles. `obvious-implementation` says: when you know what to type, type it. The ceremony exists for when you don't.

**Self-discipline.** The risk of obvious-implementation is overconfidence. You think it's obvious; you type 30 lines; the test fails for reasons you didn't anticipate. Beck's rule of thumb: if your "obvious" implementation produces an unexpected red bar, *back up immediately* to `fake-it`. Don't debug the obvious; the fact that the test went red means it wasn't obvious.

**Beck's decision tree:**
1. Type the obvious implementation.
2. If the test goes green, continue.
3. If it goes red, undo, switch to `fake-it`, then `tdd-triangulate` from there.

**What "obvious" should feel like.** Faster to type than to fake. Smaller than 5–10 lines. You can predict the test result before running it.

## Decision heuristics

- Default to this technique. It's the fastest path through the cycle when applicable.
- The moment the bar goes red unexpectedly, switch styles. Don't keep typing "more obvious code"; that's how 30-line wrong implementations happen.
- For complex algorithms (sorts, graph traversals, parsers), don't reach for obvious-implementation reflexively. The ceremony of fake-and-triangulate is cheap insurance.

## Anti-patterns

- **"Obviously implementing" a complex algorithm.** Beck explicitly warns against this. Use `fake-it` for anything non-trivial.
- **Refusing to back up after an unexpected red.** Pride dictates "I'll just fix this real quick". Twenty minutes later you've debugged the implementation instead of letting the cycle do its work.
- **Skipping the test entirely because the implementation is obvious.** That's not TDD; that's writing code. The test exists to prove the obvious *is* obvious.

## See also

- `tdd-fake-it` — fall back here when the "obvious" produces a red bar.
- `tdd-triangulate` — when you have to fake-it, triangulation completes the move.
- `tdd-red-green-refactor` — the cycle this lives inside.

## References

- Kent Beck, *Test-Driven Development By Example*, Ch. 26 — the "Obvious Implementation" entry, including Beck's own honest admission about when his obvious wasn't.
