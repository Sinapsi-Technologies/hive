---
name: tdd-fake-it
description: >-
  Use when the test is red and you can answer one specific case but don't yet know the general algorithm — phrases like "fake it till you make it", "return a constant", "hardcode the expected value", "I know the answer for this input but not the general case", "minimum code to pass the test". Defines Beck's "Fake It" technique: make the test green by returning the literal expected value, then let triangulation (or your understanding) force the generalization.
---

## One-line summary

When you know the test's expected output but not yet the general algorithm, return the literal expected value to make the test green. You've now bought yourself a green bar; generalize next.

## When to use this skill

- You can write the test, but the implementation isn't yet clear to you.
- The first time a particular kind of function is being implemented and you want to focus on the test infrastructure first.
- After a big change, when you want a small confidence-building win before tackling the real implementation.
- When the implementation feels like it might be wrong and you want to ground yourself in a passing test before exploring.

## When NOT to use this skill

- You already know exactly what to type → `tdd-obvious-implementation` is faster. Don't fake what's obvious.
- The test is wrong (testing the wrong thing) — faking won't expose that; rewrite the test.
- "Fake it forever" — `fake-it` is a step in a sequence. If you stop after the fake, you've left a hardcoded constant in your code.

## Core content

The move:

1. Test:
   ```kotlin
   assertEquals(120, factorial(5))
   ```
2. Fake implementation:
   ```kotlin
   fun factorial(n: Int): Int = 120
   ```
3. Test passes. *Green.*

Now you have a passing test and an obviously wrong implementation. The next step is *not* "leave it hardcoded forever". One of two things follows:

- **Generalize directly** if you now see the pattern. Replace the hardcoded constant with the real algorithm; the test stays green.
- **Triangulate** by adding a second test (`factorial(0)` or `factorial(6)`) the fake cannot pass; the second red test forces you to write a genuine generalization. See `tdd-triangulate`.

**Why this works.** Beck's claim: making the bar green dominates everything else in TDD. Even an obviously-wrong implementation that passes the test means the test infrastructure works, the wiring is correct, and you can now refactor toward the right answer with a safety net. The alternative — staring at a complex algorithm while the test is red — burns time and momentum without producing evidence of progress.

**`fake-it` ladder.** Sometimes the fake is more elaborate than a literal constant:

- Return the literal constant the test expects.
- Return the constant but assigned to a variable named for the concept.
- Compute the constant from the input (still wrong for other inputs).
- Generalize once you see how.

Each step is closer to the real implementation while keeping the test green.

## Decision heuristics

- Default position in TDD: **try `obvious-implementation` first**, fall back to `fake-it` when you don't know what to type.
- If your `fake-it` lives more than one or two cycles, you've stopped doing TDD and started writing fake code. Pair every fake with a triangulating test or a generalization step in the same session.
- Fake at the lowest level that makes the test pass. Faking deeper than needed obscures what the test is actually proving.

## Anti-patterns

- **Faking it forever.** The hardcoded value ships to production. Whoever wrote it has long forgotten. Bug surfaces months later.
- **Faking the wrong thing.** Returning `120` when the test expected `120` is fine; returning `null` and adjusting the test until both pass isn't faking — it's gaming the cycle.
- **Skipping the fake when triangulation would be slower.** Sometimes you really do know the general implementation; typing it in directly (`obvious-implementation`) saves a step.

## See also

- `tdd-triangulate` — the canonical follow-up: a second test forces the fake to become real.
- `tdd-obvious-implementation` — when the answer is clear, skip the fake.
- `tdd-red-green-refactor` — the cycle this technique belongs inside.

## References

- Kent Beck, *Test-Driven Development By Example*, Ch. 26 ("Test-Driven Development Patterns") and Ch. 27 ("Testing Patterns") — the "Fake It ('Til You Make It)" entry.
