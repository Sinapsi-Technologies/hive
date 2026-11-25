---
name: tdd-one-step-test
description: >-
  Use when picking which test to write next — phrases like "what test should I write next", "I have a backlog of tests in my head", "which TODO test first", "one step test", "what teaches me the most", "small enough to be implementable now". Defines Beck's One Step Test: of the tests you could write, pick the one that teaches you something new AND can be implemented in one step from where you are.
---

## One-line summary

Of all the tests you could write next, pick the one that teaches you something new about the design *and* is small enough that you can implement it in one cycle from where the code is now.

## When to use this skill

- You have a list of TODO tests (in code comments, in your head, on paper) and need to pick the next one.
- You're at the start of a new feature and uncertain which behavior to drive out first.
- You finished a cycle and the next test isn't obvious — multiple candidates feel equally valid.

## When NOT to use this skill

- You have exactly one TODO test left — write it.
- You're early in exploring a domain and don't yet have a list of candidate tests — write whatever feels concrete; the list emerges as you go.
- You're stuck on a test that's too big — that's `tdd-child-test`, not "pick a different test".

## Core content

Two criteria, equally important:

1. **The test must teach you something new.** It should expose a behavior the existing tests don't cover. Tests that re-prove what existing tests already prove are noise. The first test in a new direction is usually the high-information one.

2. **The test must be implementable in one step.** "One step" is rough — a few minutes of work from the current state. If implementing the test requires writing 200 lines of supporting code first, the test is too big; pick a smaller one and come back to this later.

**The pick.** Of your candidate tests, score each on *information value* (how much it teaches) and *implementation distance* (how close it is to current code). Pick the one with the best combination — usually that's the smallest test that still moves the design forward.

**The honest part.** Many TDD walkthroughs in books and talks pick the "right" next test as if it were obvious. In practice, it's genuinely a judgment call. Beck's framing of "one step" is the discipline that prevents you from picking tests that require giant leaps.

**Example progression.** Building a money library:
- Candidate tests: `Money(5, USD) * 2 == Money(10, USD)`, `Money(5, USD) + Money(10, USD) == Money(15, USD)`, `Money(5, USD) + Money(10, EUR) == ?`.
- The multiplication test is the smallest one-step: it teaches the basic multiplication contract and you can fake-then-generalize quickly.
- Addition same-currency is the next one-step: incremental from multiplication.
- Cross-currency addition is *not* one-step from where you are; defer it.

**Starter Test (related).** The very first test for a new feature should also be one-step from "nothing exists". A simple, narrow first test gets the harness running and proves the wiring. Don't make the starter test ambitious.

## Decision heuristics

- Smaller is better when in doubt. A small test you can complete teaches more than a big test you can't.
- Picking a high-information test that's *just* implementable from current state often takes you further than a low-information test that's trivially easy.
- Keep the TODO list visible. Mid-cycle, when a new candidate test occurs to you, jot it down — don't reorganize the cycle to chase it.
- If you keep picking the "same kind" of test (all addition tests, all multiplication tests), you're probably leaving an axis of variation untouched. Switch axes.

## Anti-patterns

- **Picking the most ambitious test.** Often unfinishable, breaks the cycle.
- **Picking the easiest test.** Trivial extensions of existing behavior teach nothing.
- **Writing all the candidate tests at once.** Multiple red tests at the same time means you can't tell which one your code is failing.
- **Not maintaining a TODO list.** You forget candidate tests as you go and the design slips.

## See also

- `tdd-child-test` — when the test you picked turns out to be too big mid-cycle.
- `tdd-red-green-refactor` — the cycle this skill picks the next iteration of.
- `tdd-identify` — when stuck on "which test", the diagnostic.

## References

- Kent Beck, *Test-Driven Development By Example*, Ch. 26 — the "One Step Test" and "Starter Test" entries.
