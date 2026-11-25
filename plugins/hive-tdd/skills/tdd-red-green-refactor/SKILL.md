---
name: tdd-red-green-refactor
description: Use when establishing TDD as a discipline — phrases like "red green refactor", "TDD cycle", "test first", "should I do TDD here", "TDD discipline", "what's the TDD loop", "when should I refactor". Defines the canonical TDD cycle (red → green → refactor), what each phase actually requires, the honest scope of when TDD applies, and the cases where it shouldn't.
---

## One-line summary

The TDD cycle: write a failing test (red), write the minimum code to pass it (green), refactor while staying green. Repeat one tiny step at a time. The discipline is in the *order* and the *minimum* — both are easy to skip.

## When to use this skill

- Building new behavior in code where you can articulate the desired outcome as a test.
- Bug fixing — write the regression test first (see `tdd-regression-test`), then fix.
- Designing an API from the consumer's side — the first test forces you to consider what calling code wants.
- Working in a codebase with a healthy test suite where TDD adds little friction.

## When NOT to use this skill

- Pure exploration / spike work where you don't yet know the shape of what you're building. Spike first, then test-drive the rewrite.
- Throwaway scripts, one-shot utilities, or rapid prototyping that won't be maintained.
- UI tweaks where the cycle of "write a test, change a pixel, see the test pass" is more pain than benefit.
- Codebases with no existing test infrastructure where setting up the harness for one feature is disproportionate. Add tests; don't pretend you're doing TDD if you can't.

## Core content

**Red — write a failing test.** Write a small test for behavior that doesn't exist yet (or behavior you're about to add to). Run it. Confirm it fails *for the right reason* — compile error or assertion failure on the expected line, not some random NullPointerException three layers deep.

**Green — make the test pass, minimum required.** Write just enough production code to flip the test green. *Minimum* is load-bearing — type whatever sins are necessary in the moment (`return "hardcoded"` is fine). The minimum is the cheapest signal that the test is correctly wired.

**Refactor — clean up while staying green.** With a passing test net, restructure the code. Extract methods, rename variables, remove duplication. Run the test after each small change. If it goes red, your refactor was wrong; undo and try smaller.

**Why the order matters.**

- *Red first* forces you to write a test that fails. If you write production code first, you may not actually need the test you write later.
- *Minimum green* is the loop's discipline. Without it, you over-engineer; the loop stops being tight.
- *Refactor with the net* is when design improves. Without the test, you fear changing code. With it, you can experiment.

**Cycle length.** A complete cycle should take minutes, not hours. If a cycle takes longer than ~30 minutes, the test was too big — reach for `tdd-child-test`.

**TDD is not "writing tests".** Tests-after is testing. Tests-first is design. The order changes how the code emerges; both produce tests but only one drives design.

## Decision heuristics

- Run tests *every* cycle step. Don't batch test runs — fast feedback is the point.
- The first test you write for a new feature should be small and run quickly. Big or slow first tests kill the cycle.
- Refactor only on green. Refactoring on red is "changing code while broken" — no longer TDD.
- If you find yourself writing production code before the test, the test is going to be biased toward what you already wrote. Stop and rewind.

## Anti-patterns

- **Tests written after the code.** Often called "TDD" by people who haven't actually done TDD. Doesn't drive design.
- **Big tests covering large surfaces.** Slow, hard to make green, low information when they fail.
- **Refactoring on red.** "I'm fixing the design and also the test will be green when I'm done." Conflates two activities; both go wrong.
- **Skipping the minimum-green step.** Going from red straight to a "real" implementation. Often introduces complexity the test doesn't actually require.
- **TDD as religion.** Insisting on TDD for code that doesn't benefit (one-shot scripts, exploratory spikes). The discipline costs are real; pay them where they help.

## See also

- `tdd-identify` — when stuck inside the cycle, route to the right technique.
- `tdd-fake-it`, `tdd-obvious-implementation`, `tdd-triangulate` — three ways to do the "green" step.
- `tdd-child-test`, `tdd-one-step-test` — when the red test is too big or you can't pick.
- `tdd-anti-patterns` — the smells that mean TDD has gone wrong.
- Superpowers' `superpowers:test-driven-development` — workflow-level trigger.

## References

- Kent Beck, *Test-Driven Development By Example* (Addison-Wesley, 2002) — the canonical reference. Part I (Money example) walks through dozens of red-green-refactor cycles.
- Beck's "TDD by Example" talks (frequently delivered) — distilled versions of the same material.