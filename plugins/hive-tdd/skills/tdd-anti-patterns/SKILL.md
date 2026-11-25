---
name: tdd-anti-patterns
description: Use when auditing tests for TDD smells — phrases like "test smells", "flaky tests", "slow tests", "brittle tests", "test anti-patterns", "tests testing the framework", "over-mocking", "mystery guest", "fragile tests", "test breaks on every refactor". Catalogs the most common test-suite anti-patterns with symptoms, why each is wrong, and the fix.
---

## One-line summary

A reference catalog of test-suite anti-patterns. Each smell has a symptom you can spot, an explanation of why it's wrong, and a pointer to the TDD technique that fixes it.

## When to use this skill

- Code review of test code.
- A test suite has grown ugly and someone has to triage what to fix first.
- Onboarding: explaining "we don't do X here, and here's why".
- Diagnosing CI pain (slow, flaky, hard to debug).

## When NOT to use this skill

- You haven't written the tests yet — use the per-technique skills directly.
- A specific test is failing for legitimate reasons — that's debugging, not auditing.

## Core content

**Test interdependence.** Tests pass when run alone but fail when run together. *Symptom:* CI fails on the test that "always passes locally". *Fix:* `tdd-test-isolation`.

**Slow suite.** Single test takes longer than ~100 ms; whole suite takes hours. *Symptom:* developers stop running tests locally. *Fix:* push real I/O, real time, real network out behind doubles (`tdd-mock-object`); use in-memory fakes for unit tests.

**Flaky tests.** Same test fails sometimes, passes sometimes, with no code change. *Symptom:* "rerun the build" culture. *Causes:* race conditions, real clocks, non-deterministic order, network. *Fix:* inject the clock, inject the executor, seed randomness, isolate (`tdd-test-isolation`).

**Mystery Guest.** Test depends on data it didn't put there — a database row, a file on disk. *Symptom:* `select * from users where id = 42` in the test assertion; no record of how 42 got there. *Fix:* the test sets up its own fixture.

**Over-mocking.** Tests verify which collaborators were called rather than what behavior was produced. *Symptom:* `verify(mock, times(2))` everywhere; benign refactor breaks ten tests. *Fix:* mock only at the boundary (`tdd-mock-object`); use real objects inside your domain.

**Testing the framework.** A test that verifies, e.g., that `@Transactional` rolls back, or that Jackson serializes a `LocalDate`. *Symptom:* the test would pass even if the team's code didn't exist. *Fix:* delete it; the framework already tests itself.

**Testing implementation, not behavior.** A test asserts internal state (private field values, method call counts) rather than observable outcomes. *Symptom:* refactor that doesn't change behavior breaks dozens of tests. *Fix:* assert on outputs, return values, side effects to externals — not internals.

**Mocking what you don't own.** A test directly mocks a third-party SDK's class. *Symptom:* SDK upgrade silently breaks the mock's behavior; tests pass; production breaks. *Fix:* wrap the SDK behind a small interface *you* own; mock the interface; add a `tdd-learning-test` for the SDK.

**No test on bug fix.** A bug is fixed without a regression test. *Symptom:* the bug returns weeks later. *Fix:* `tdd-regression-test` — every bug fix gets a test.

**Massive tests.** A single test sets up 50 lines of fixture, exercises 10 behaviors, and asserts 20 outcomes. *Symptom:* when it fails, nobody knows why. *Fix:* split into small focused tests; one assertion per test as the default.

**Test code without code review.** Test code is treated as second-class — no review, no refactoring, no naming discipline. *Symptom:* test suite is messier than production code. *Fix:* treat test code like production code. The test suite is your design's executable specification.

**Mocking your own data classes.** `mock<Order>()` when you could `Order(...)`. *Symptom:* mocks need to be told every property they have. *Fix:* construct real value objects.

**Assertions hidden in helper methods.** A test calls `verifyEverything(...)`; the actual assertions live three layers deep. *Symptom:* failure message is "AssertionError" with no context. *Fix:* assertions belong in the test method; helpers should set up state, not assert it.

**Random sleeps for synchronization.** `Thread.sleep(2000)` to "wait for the async thing to complete". *Symptom:* slow tests + flaky tests + neither problem actually solved. *Fix:* use proper synchronization primitives, await futures, use `kp-testing-coroutines` virtual time for suspending code.

**No assertion message.** `assertEquals(expected, actual)` with no third-argument message. *Symptom:* CI failure shows two numbers, no clue what was being checked. *Fix:* assertion messages or per-line description.

## Decision heuristics

- Severity scales with reach: a slow test in CI affects every developer; a single brittle test in one file is a small fix.
- Rerunning a flaky test until it passes is not a fix. Find the source of nondeterminism.
- Mocks added "to make the test fast" are the most common over-mocking on-ramp. Question every new mock.
- Tests as documentation: the test's name and shape should tell you what the code does. If they don't, the test is broken regardless of pass/fail.

## Anti-patterns about catching anti-patterns

- Treating every long test as bad — sometimes integration tests are legitimately longer.
- Demanding zero mocks — boundary mocks are correct and necessary.
- Refactoring tests "for cleanliness" without re-running them. Test code is code; the same discipline applies.

## See also

- All eleven other `tdd-*` skills — each fix references the relevant technique.

## References

- Kent Beck, *Test-Driven Development By Example*, Ch. 27 — many of the test anti-patterns are mentioned alongside the patterns.
- Gerard Meszaros, *xUnit Test Patterns* — exhaustive smell catalog (the canonical reference).
- Robert C. Martin, *Clean Code*, Ch. 9 — clean tests; FIRST principles (Fast, Independent, Repeatable, Self-validating, Timely).
