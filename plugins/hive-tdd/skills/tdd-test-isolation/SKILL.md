---
name: tdd-test-isolation
description: >-
  Use when ensuring each test runs independently — phrases like "test isolation", "tests pass alone but fail together", "shared state between tests", "test interdependence", "test ordering", "fresh fixture", "no shared mutable state in tests", "flaky test order". Defines the discipline of making every test independent: each test owns its own setup and tears it down; no test depends on another test's side effects.
---

## One-line summary

Each test should run independently. Passing or failing must not depend on whether another test ran first, ran in the same JVM, ran on the same machine, or ran at all. Independence is the prerequisite for everything else in TDD.

## When to use this skill

- Tests pass when run alone but fail when run as a suite (or vice versa).
- Test ordering matters (running in CI vs locally produces different results).
- A test mutates global state, a singleton, a database table, or a file that another test reads.
- Time-dependent tests pass at 11:59 PM but fail at 12:01 AM.

## When NOT to use this skill

- An integration test that legitimately needs a shared, pre-populated environment (rare; usually a sign that the test should be smaller).
- Performance / load tests where ordering and shared state are part of the test setup by design.

## Core content

**Each test owns its fixture.** Setup creates everything the test needs; teardown undoes it. The test passes deterministically regardless of what else has run.

**Common sources of test interdependence:**

- **Global mutable state.** Static fields, singletons, `object` declarations (Kotlin) that hold state across tests.
- **Filesystem.** Tests writing to fixed paths; the second test reads stale data from the first.
- **Database.** Shared schemas without transactional rollback; data from test A pollutes test B.
- **Time.** `LocalDateTime.now()` baked into assertions; tests pass at one time of day and fail at another.
- **Random.** Tests using non-seeded random; same test sometimes passes, sometimes fails.
- **External services.** Test calling a real API; works locally, fails when the service is down.
- **Test order assumption.** Test B implicitly relies on test A having created a record; reorder tests → B fails.

**Fixes.**

- **Fresh fixture per test.** Setup creates new instances; teardown disposes. Most test frameworks make this idiomatic (`@BeforeEach` in JUnit 5, `init` blocks in Kotest, `setUp` in pytest).
- **Transactional rollback.** Wrap each test in a database transaction that rolls back at end. Used in Spring Boot tests; data never persists across tests.
- **Inject the clock.** Tests pass a fixed `Clock` instead of using `Instant.now()`. Production injects the system clock.
- **Seed the random.** Tests use a deterministic seed; same input → same output.
- **In-memory implementations.** For databases, message queues, etc., use in-memory variants for unit tests. Real ones only in integration tests.
- **No globally mutated singletons.** If a singleton holds state, make it injectable so tests can substitute.

**Per-test vs per-class vs per-suite setup.** JUnit's `@BeforeAll` (class-level setup) and `@BeforeEach` (test-level setup) are not equivalent — class-level state persists across tests within the class. Use class-level only for *immutable* shared resources (a precomputed lookup table). Anything mutable goes in test-level setup.

## Decision heuristics

- If a test fails only when run with others, isolation is broken. Don't paper over it with `@TestMethodOrder` — fix the dependence.
- Time and randomness in tests are isolation bugs waiting to happen. Inject both.
- Prefer in-memory implementations for unit tests; reserve the real database / queue / cache for integration tests that explicitly accept the slower runtime.
- The test runner should be able to run any test, any order, in parallel, and produce the same results.

## Anti-patterns

- **Pinning test order.** "Tests run in alphabetical order, so we name them `test01_setup`, `test02_use_setup`, `test03_cleanup`." Defeats isolation.
- **Shared mutable singletons in tests.** A static counter incremented across tests; the third run sees a value that depends on the previous runs.
- **`Thread.sleep(...)` for synchronization.** Flaky on slow machines, slow on fast ones, broken on all of them.
- **Tests that connect to a shared dev database.** Two engineers running tests at the same time clobber each other.
- **System.now() in assertions.** Time-of-day dependent.

## See also

- `tdd-mock-object` — substitute slow / external dependencies with test doubles for isolation.
- `tdd-anti-patterns` — most isolation failures are catalogued there too.
- `kp-testing-coroutines` — virtual time in coroutine tests addresses the clock-isolation problem for suspending code.

## References

- Kent Beck, *Test-Driven Development By Example*, Ch. 27 — "Isolated Test" and related entries.
- Gerard Meszaros, *xUnit Test Patterns* — exhaustive treatment of test isolation, with anti-pattern catalogue.
