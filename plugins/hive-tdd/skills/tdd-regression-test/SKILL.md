---
name: tdd-regression-test
description: >-
  Use when fixing a bug or regression and you need a failing test that reproduces it before changing production code. Defines the Regression Test workflow: reproduce the defect, prove the test fails for the right reason, fix the code, and keep the test as a permanent guard.
---

## One-line summary

Every bug fix gets a test. Write the test that *would have caught the bug*, fail it (proving it characterizes the bug), then fix the code. The test stays forever as the guarantee that this specific failure won't return silently.

## When to use this skill

- A bug was reported and you're about to fix it.
- You discovered a defect during refactoring and you're about to correct it.
- A near-miss: code that *would* have been wrong but you caught it before shipping — write the test anyway, for the next person.
- Characterizing a piece of legacy code where the existing behavior should be preserved even though it's not specified anywhere.

## When NOT to use this skill

- Trivial cosmetic fixes (typo in a comment, formatting) — no behavior is changing.
- You've just deleted a feature; the bug fix is moot because the buggy code no longer exists.
- The bug is in throwaway code that won't be maintained.

## Core content

The discipline (in order):

1. **Reproduce the bug as a failing test.** Smallest possible test that demonstrates the wrong behavior. Run it; confirm it fails the way the bug report describes.
2. **Fix the production code.** Just enough to make the new test pass.
3. **Confirm the existing tests still pass.** Your fix must not break anything else.
4. **Commit the test and the fix together.** The test is now a permanent guard against the bug recurring.

The test does three things at once:
- **Proves you understand the bug** (you reproduced it).
- **Proves your fix actually fixes it** (the test goes green).
- **Prevents regression** (the test runs forever).

**The "would-have-caught-it" framing.** A regression test is what the test suite *should have already had* before the bug shipped. Writing it now closes the original gap — you're not adding a new test for a new feature; you're filling a hole the original test suite had.

**Bug report → test naming.** Use the bug ID or a short description as the test name: `test_negative_total_when_refund_exceeds_payment_REGRESSION` or `test_BUG_4471_handles_zero_quantity_lines`. Future readers see immediately why the test exists.

**For legacy code (no tests existed).** When you discover a bug in code without a test suite, the regression test doubles as a *characterization test* — it documents what the code *should* do, even when nothing else does. Build up the test suite one bug at a time.

**For race conditions and flaky bugs.** Try to write a test that reproduces the race deterministically (often requires injecting clocks, executors, or test dispatchers — see `kp-testing-coroutines`). If you genuinely cannot, document the bug and the suspected fix; mark the absence of a test as technical debt.

**The Boy Scout corollary.** When fixing a bug, also write tests for the *adjacent* behaviors you noticed weren't covered. The bug is the prompt; the test suite's gap is the lesson.

## Decision heuristics

- The test goes in first, fails as expected, then the fix lands. Don't write the fix and then "add a test" — at best that produces a test that passes against the fix you already wrote.
- Keep regression tests *small*. They shouldn't be wholesale recreations of the failing user journey; just the minimal trigger and the minimal assertion.
- Tag regression tests so the team can see how many exist. Many = recurring bugs in a hot area; investigate the design.
- If you can't write a test for the bug, name the obstacle. "Race condition, no deterministic repro yet" is more useful than silence.

## Anti-patterns

- **No regression test on bug fix.** The bug returns six months later; nobody knows it was ever fixed.
- **Regression test that doesn't actually test the bug.** Written to silence reviewers; passes regardless of whether the fix is correct.
- **Massive regression tests that recreate the entire UI flow.** Slow; brittle; the next refactor breaks them. Keep regressions small and unit-shaped where possible.
- **Treating regression tests as second-class citizens.** They're some of the highest-value tests in the suite — they're each a piece of incident history made executable.

## See also

- `tdd-learning-test` — similar shape (a test that catches future breakage); different purpose (library behavior, not your code's bugs).
- `tdd-test-isolation` — flaky regression tests are worse than no regression tests.
- `tdd-anti-patterns` — "no test on bug fix" is the canonical anti-pattern here.

## References

- Kent Beck, *Test-Driven Development By Example*, Ch. 27 — "Regression Test" entry.
- Michael Feathers, *Working Effectively With Legacy Code* — characterization tests for systems without tests; the bigger-picture version of this skill.
