---
name: tdd-triangulate
description: >-
  Use when generalizing a fake implementation by adding a second test that forces the real algorithm — phrases like "triangulate", "force generalization", "second test", "the fake passes the first test but I need to make it real", "two examples force the abstraction". Defines Beck's Triangulation technique: add a test the fake cannot pass; the resulting red bar forces a genuine generalization.
---

## One-line summary

When a faked implementation passes the current test but you're not sure of the general algorithm, add a *second* test with different inputs that the fake cannot satisfy. The red bar forces you to write the real algorithm.

## When to use this skill

- You faked-it (`tdd-fake-it`) and now need to generalize but the right code isn't yet clear to you.
- You can see one case but not the pattern; a second example will make the pattern visible.
- You want to bound the design conservatively: only generalize what *two* concrete examples demand.

## When NOT to use this skill

- The general algorithm is already obvious after the first test → just write it (`tdd-obvious-implementation`). Triangulation is for when you can't see the pattern, not for ceremony.
- You're triangulating with five tests "for completeness" when two examples already settle the design — that's not triangulation, that's wishful test-writing.

## Core content

The move:

1. After `fake-it`:
   ```kotlin
   assertEquals(120, factorial(5))     // passes
   fun factorial(n: Int) = 120
   ```
2. Add a second test the fake can't satisfy:
   ```kotlin
   assertEquals(6, factorial(3))       // FAILS — fake returns 120
   ```
3. Now write the genuine generalization that satisfies both:
   ```kotlin
   fun factorial(n: Int): Int = if (n <= 1) 1 else n * factorial(n - 1)
   ```

Two concrete examples force the algorithm into the open. With one example, multiple implementations could pass (the fake, a hash table lookup, the real algorithm). With two well-chosen examples, the algorithm becomes the natural answer.

**Choosing the second test.** The second example should be:
- *Different enough* from the first to rule out the fake.
- *Not so different* that you're testing a separate behavior. Stay on the same axis.
- Specific enough that one obvious implementation satisfies both. The simplest implementation should be the right one.

**Beck's preference.** Beck himself prefers `obvious-implementation` whenever the pattern is clear after the first test — triangulation is the safety net, not the goal. Triangulation is "when you can't see how to generalize, let two examples show you".

**The "rule of three" reading.** Some teachers conflate this with "wait until you have three concrete cases before generalizing". Beck's version is two — and the second exists specifically to force the abstraction. You don't write the second test to wait; you write it to push.

## Decision heuristics

- One fake-then-triangulate pair is usually enough. If you need three or four examples to see the pattern, you're either modelling at the wrong abstraction or the algorithm genuinely is data-table-shaped (in which case, model it that way).
- Pick the second test for *information value* — what case will most ruthlessly rule out the fake?
- Once the algorithm is general, you don't need to keep both tests forever. The second one was scaffolding; if the first test is more representative, the second can be deleted (or kept as a regression test if it covers a real edge case).

## Anti-patterns

- **Triangulating with similar examples.** Two tests that the same fake passes don't force anything. The fake survives and you've added noise.
- **Triangulating forever.** Adding tests one at a time, each forcing a tiny generalization. At some point, the algorithm is general; stop.
- **Triangulating when the algorithm is obvious.** Wasted cycle; type the real code instead.
- **Calling everything triangulation.** "I have multiple tests" is not the same as "I added a test to force generalization of a fake".

## See also

- `tdd-fake-it` — the technique triangulation completes.
- `tdd-obvious-implementation` — when you can see the pattern after the first test, skip triangulation.
- `tdd-red-green-refactor` — the cycle this lives inside.

## References

- Kent Beck, *Test-Driven Development By Example*, Ch. 26 — the "Triangulation" entry.
