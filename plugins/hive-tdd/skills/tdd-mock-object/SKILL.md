---
name: tdd-mock-object
description: Use when substituting a real collaborator with a test double — phrases like "mock object", "test double", "stub", "fake", "spy", "Mockito", "MockK", "should I mock this", "database in tests", "fake the time", "fake the HTTP client". Defines test doubles in their five flavors (dummy / fake / stub / spy / mock) with picking criteria, and the strong warnings about over-mocking.
---

## One-line summary

Use a test double to replace a real collaborator that's too slow, too external, or too non-deterministic to call from a test. Reach for it reluctantly — over-mocking couples tests to implementation rather than behavior.

## When to use this skill

- A test would otherwise touch a database, a network, a file system, a message broker, or any other "outside the JVM" resource.
- A test would otherwise depend on system time, randomness, or other non-deterministic sources.
- A test would otherwise call a third-party API that's slow / paid / rate-limited.
- You want to verify that a collaborator was called correctly (e.g., a notifier was triggered).

## When NOT to use this skill

- The dependency is fast, deterministic, and in-process — just use the real thing.
- You're mocking a class *you own* whose behavior is the thing being tested. That's testing the mock, not the code.
- You're mocking a value object or data structure — overkill; construct the real one.
- You're mocking everything in sight and your tests are now mostly mock setup. The design needs simplification before more mocks.

## Core content

**Five flavors** (Meszaros's taxonomy, widely used):

- **Dummy.** A placeholder passed where the real value isn't used. (`null`, `Object()`, `Any()` in Kotlin.)
- **Fake.** A working but simplified implementation. An in-memory `Repository` that holds a `MutableMap<Id, Entity>` is a fake.
- **Stub.** Returns canned answers; doesn't verify how it's called. "When asked `findById(42)`, return this preset User."
- **Spy.** A stub that records how it was called, so the test can assert on the calls.
- **Mock.** Set up with expectations *before* the test runs; the test fails if the expected calls don't happen. Library-based tooling: Mockito, MockK, jMock.

In casual conversation everyone says "mock" for all five. In careful conversation the distinctions matter: stubs are about *input*; mocks are about *output verification*.

**Libraries** (JVM): Mockito (Java standard), MockK (Kotlin-idiomatic, handles `final` classes and coroutines), jMock (older, strict). Pick MockK for Kotlin projects; Mockito works for Java.

**The biggest risk: over-mocking.** Tests that mock every collaborator have two problems:
- They test *implementation*, not *behavior*. A refactor that changes which collaborator gets called fails the test even though the externally observable behavior is identical.
- The test becomes mostly mock setup. Reading it is harder than reading the production code.

**Beck's "don't mock what you don't own" rule.** Mock the *boundary* — your code's interface to the outside world. Don't mock the outside world itself. The boundary is *yours*; you can fake it consistently. The outside world is *theirs*; your mock will drift from real behavior and you won't notice until production.

**Pair mocks with contract tests.** If you fake a `PaymentGateway` for unit tests, also write a contract test that runs against both the fake and the real gateway, asserting they behave identically on the cases that matter. Without contract tests, the fake drifts and lies.

## Decision heuristics

- Reluctance is the right default. Real objects > fakes > stubs > spies > mocks. Use the highest fidelity that's practical.
- Mock at the *boundary*. Inside your domain, prefer real objects.
- For time: inject a `Clock`. For randomness: inject a `Random`. For HTTP: inject a small interface you own, not the third-party client directly.
- Resist the temptation to verify "call count". `verify(mock, times(3))` is almost always testing implementation, not behavior.
- If a test has more lines of mock setup than lines of assertion, the design is asking to change.

## Anti-patterns

- **Mocking the database.** Common; almost always wrong. Use an in-memory implementation (H2, Testcontainers with a real database, or a fake repository).
- **Mocking value objects / data classes.** Construct the real one with `data class Money(BigDecimal, Currency)`; don't `mock<Money>()`.
- **Mocking the framework.** Tests verify that Spring's `@Transactional` was called. You're testing Spring, not your code.
- **Brittle call-count verifications.** `verify(repo, times(2))` failing after a benign refactor that called the repo once instead of twice with no behavioral difference.
- **No contract tests on fakes.** The fake's behavior drifts from the real thing; tests pass; production breaks.
- **Mocking what you don't own (third-party SDK directly).** Wrap their SDK behind a small interface *you* own and mock that.

## See also

- `tdd-test-isolation` — mocks are one technique for achieving isolation.
- `tdd-anti-patterns` — over-mocking and brittle verification are smells in the catalog.
- `kp-testing-coroutines` — `TestDispatcher` is a mock-like substitute for the dispatcher; same disciplines apply.
- `gof-kotlin-idioms` — Kotlin's MockK supports `final` classes and suspending functions.

## References

- Kent Beck, *Test-Driven Development By Example*, Ch. 27 — "Mock Object" entry plus the Self Shunt variant.
- Gerard Meszaros, *xUnit Test Patterns* — exhaustive test-double taxonomy.
- Steve Freeman & Nat Pryce, *Growing Object-Oriented Software, Guided by Tests* — "mock roles, not objects" — the canonical guidance on doing it right.
