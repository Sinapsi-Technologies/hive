---
name: tdd-child-test
description: >-
  Use when a red test is too big to make green in one cycle — phrases like "child test", "this test is too big", "I can't implement this in one step", "subdivide the test", "smaller test to learn first", "back away from a too-big bite". Defines Beck's Child Test: when a test is too ambitious, set it aside; write a smaller test you can complete; return to the parent test with new understanding.
---

## One-line summary

A red test you can't make green in one cycle is too big. Set it aside; write a smaller "child" test that drives out a piece of the behavior; come back to the parent test with the new code as a stepping stone.

## When to use this skill

- A test is red and you've been stuck for more than ~15 minutes.
- You start writing the production code and realize you need to build a substantial piece of infrastructure first.
- The test touches multiple unfamiliar concepts at once and you can't unstick one from the other.
- You're typing in fits and starts, undoing, retyping — the cycle has lost its rhythm.

## When NOT to use this skill

- The test is the right size but you don't know how to implement it → that's a `tdd-fake-it` or `tdd-triangulate` moment, not a child-test one.
- The test feels uncomfortable but you can see the implementation in your head → push through; don't subdivide reflexively.

## Core content

The move:

1. You wrote a test. It's red.
2. You start implementing. Five minutes in, you realize you need to build three other things first — a parser, a state machine, a coordinator.
3. **Stop.** Leave the parent test red (or comment it out, or `@Disabled`). You'll come back.
4. Identify the smallest sub-behavior you can test independently. Often that's "the parser correctly handles one specific input". Or "the state machine transitions from A to B on event X".
5. Write a child test for that smaller behavior. Make it green. Refactor.
6. Repeat — child of child if needed.
7. When you have enough pieces in place, come back to the parent test. With the children green, the parent is now implementable in one step.

**The discipline.** Resist the urge to keep grinding on the parent test. Five hours of struggle versus five 20-minute child tests — the second produces working code, learning, and momentum; the first produces frustration and partial implementations.

**Parent test management.** While the parent is parked:
- Leave it in the codebase, ideally as a comment with a TODO, or marked `@Disabled` / `@Ignore` with a reason.
- Don't delete it — you'll forget what you were trying to do.
- When you come back, check: does the parent test still describe the right behavior? Often the children have changed your understanding enough that the parent needs a small rewrite.

**Relationship to one-step-test.** `one-step-test` is "pick a test that's implementable from current state". `child-test` is "you picked one, it turned out too big — back off and pick a smaller one". Same family.

## Decision heuristics

- The signal is loss of rhythm. If you're not flowing through red → green → refactor in minutes, the test is probably too big.
- The smaller child should still be *meaningful*. Pure scaffolding tests with no domain content add noise.
- Multiple levels of children are fine for genuinely complex behaviors. Treat each as its own complete cycle.
- The parent test is the contract you're building toward. Don't let the children drift away from it.

## Anti-patterns

- **Pushing through a too-big test for hours.** The cycle is dead; the design is suffering.
- **Disabling the parent and never coming back.** A disabled test is a lie. Either re-enable it or delete it intentionally.
- **Subdividing reflexively.** Every test gets broken into ten pieces because "smaller is better". That's not TDD; that's micro-testing.
- **Children that test the framework, not the domain.** "Test that List.add() works" isn't a useful child test.

## See also

- `tdd-one-step-test` — the partner skill: picking a test that's the right size from the start.
- `tdd-red-green-refactor` — the cycle this is for getting unstuck inside.
- `tdd-identify` — when the parent test is too big, this is one of the routings.

## References

- Kent Beck, *Test-Driven Development By Example*, Ch. 26 — the "Child Test" entry.
