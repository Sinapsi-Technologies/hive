---
name: tdd-learning-test
description: >-
  Use when learning a third-party library by writing tests against it — phrases like "learning test", "characterization test against a library", "verify I understand this API", "what does this library do when I do X", "should we lock down this library's behavior", "library upgrade safety net". Defines Beck's Learning Test: write a small test that exercises the library's behavior you care about; commit it; run it against future versions to catch breaking changes.
---

## One-line summary

To learn how a third-party library behaves, write a small test that exercises the behavior you need. The test is faster than the docs, more accurate than the comments, and runs against future library versions to catch breaking changes for free.

## When to use this skill

- Adopting a new third-party library and unsure how a specific API behaves.
- Reading the library's source isn't an option (closed source, too large, or just slow).
- Anticipating a library upgrade: the existing learning tests will fail if the upgrade breaks something you depend on.
- Onboarding new team members: the learning tests teach what the library does in this context.

## When NOT to use this skill

- Testing your *own* code that uses the library — that's a regular unit test (probably with a mock of the library at the boundary).
- The library is so trivial that a one-line look at its API tells you everything.
- The behavior you need is genuinely documented and the docs are reliable. (Rare.)

## Core content

The technique is straightforward but easy to skip:

1. The library does *something* you need.
2. Instead of trusting the docs or guessing, write a test that exercises that behavior.
3. Make assertions about what the library returns / does.
4. Commit the test alongside your production code.

```kotlin
// Library: kotlinx.serialization
@Test fun `Json encodes BigDecimal as string by default — verify our assumption`() {
    val json = Json.encodeToString(BigDecimal("12.34"))
    assertEquals("\"12.34\"", json)   // Our code expects this; the test will scream if a future version changes it.
}
```

The test serves three purposes:
- **You learn the behavior** by writing the test.
- **The test documents your assumption** for the next reader.
- **The test catches breakage** when the library is upgraded.

**Why this beats reading docs.**

- Docs lie (or are out of date, or are written for a different version).
- The test exercises the *exact* behavior you care about with the *exact* inputs you'll use.
- The test runs in CI alongside the rest; if the library changes behavior, you find out immediately.

**Learning tests vs unit tests.** They look similar. The difference is the *target* of the test:
- A unit test asserts your code does the right thing.
- A learning test asserts the *library* does what you think it does. Your code may not even appear in it.

Keep them in a dedicated directory (`learning-tests/`, `library-contracts/`) so they're visible as a category.

**On library upgrades.** When you bump a library version, the learning tests are the first line of defense. If they fail, you have a concrete description of what changed. If they pass, you have evidence (not just hope) that the relevant behavior is preserved.

## Decision heuristics

- One learning test per library API you depend on for non-trivial behavior. Trivial APIs (`list.first()`) don't need them.
- The test should assert *your understanding*, not "the library exists". If the test would pass even if the library API behaved arbitrarily, it's not a learning test.
- Keep learning tests fast. They shouldn't slow the suite — they're tiny by nature.
- When a learning test fails on a library upgrade, that's the most useful signal you can get. Treat it as a contract change, not a flaky test.

## Anti-patterns

- **Skipping the learning test because "the docs say so"**. Docs lie. The test is cheap.
- **Learning tests deleted "for cleanup"**. Loss of safety net. Keep them.
- **Learning tests buried among unit tests**. Loses the "this is a library contract" signal. Separate them.
- **Learning tests for libraries you don't depend on**. Test what you use; don't audit the world.

## See also

- `tdd-mock-object` — a learning test plus a contract test together verify both your fake and the real library agree.
- `tdd-regression-test` — same shape (a test that catches future regressions), different purpose.
- `tdd-anti-patterns` — "testing the framework" is a sibling smell to learning tests; distinguish carefully.

## References

- Kent Beck, *Test-Driven Development By Example*, Ch. 27 — "Learning Test" entry.
- Jim Newkirk's project mentioned in Beck (running learning tests on each library upgrade) — the canonical real-world workflow.
