---
name: tdd-identify
description: Use when stuck mid-TDD and unsure which Beck technique applies — phrases like "I have a red test and don't know how to make it green", "should I fake it or just write the code", "is this a triangulation moment", "this test is too big to implement at once", "should I mock this", "what's the next test I should write". Diagnostic mode that routes a stated TDD situation to the relevant tdd-* technique.
---

## One-line summary

Diagnose which TDD technique fits the current moment — the right Beck move depends on whether you know what to type, whether the test is too big, whether the dependency is too slow, or whether you've just found a bug.

## When to use this skill

- A test is red and you're not sure how to make it green.
- You can see the implementation but unsure whether to type it directly or fake-then-generalize.
- You're staring at the next test and can't decide which one to write.
- A test is hitting a slow / external dependency and you're not sure how to isolate.
- You just fixed a bug and aren't sure what kind of test to leave behind.

## When NOT to use this skill

- You haven't decided whether to use TDD at all → that's the workflow question, see `tdd-red-green-refactor` (and Superpowers' `test-driven-development` if installed).
- You know exactly which technique to apply → go directly to that skill.
- Domain-modeling question that's masquerading as a TDD question → route to `ddd-design`.

## Core content

Walk the stuck moment through these questions, in this order:

**1. Is the test red and you don't know what code to write?**
- *I know exactly what the production code looks like* → `tdd-obvious-implementation`. Just type it.
- *I know the answer for this specific input but not the general algorithm* → `tdd-fake-it`. Return a constant; let the next test force the generalization.
- *I've faked it and now need to generalize* → `tdd-triangulate`. Write a second test with different inputs that the fake can't satisfy.

**2. The test you're trying to write feels too big?**
- *I can't see how to write the production code for this test* → `tdd-child-test`. Set the big test aside; write a smaller test you can complete; come back to the big one with new understanding.
- *I've got a backlog of tests and don't know which to write next* → `tdd-one-step-test`. Pick the one that teaches you the most while still being implementable quickly.

**3. The test is touching something slow / external / unreliable?**
- *Database, network, file system, time* → `tdd-mock-object`. Use a test double; make sure you also test against the real thing somewhere.
- *Tests pass when run alone but fail when run together* → `tdd-test-isolation`. Each test must own its fixture; no shared mutable state.

**4. You're working with a third-party library you don't fully understand?**
- *I'm not sure how this library behaves; the docs aren't enough* → `tdd-learning-test`. Write tests against the library to discover its behavior AND catch breakage in upgrades.

**5. You just fixed a bug?**
- *Every bug gets a test* → `tdd-regression-test`. Write the test that would have caught the bug; commit it alongside the fix.

**6. Reviewing tests for smells?**
- Slow tests, flaky tests, interdependent tests, mocking what you don't own, testing the framework instead of your code → audit via `tdd-anti-patterns`.

## Decision heuristics

- Beck's own decision tree (paraphrased): "If you know what to type, type it (`obvious-implementation`). If you don't but you know the answer for one case, `fake-it`. If neither, `triangulate`. If you're still stuck, take a shower."
- Reach for `mock-object` reluctantly. Mocks couple tests to implementation; only mock at the boundary (external services, slow resources, time).
- When in doubt about test scale, `child-test` always — smaller tests teach faster.

## Anti-patterns

- Treating each technique as a "level you graduate from". `obvious-implementation` isn't *better* than `fake-it`; they're for different moments. Senior practitioners use all of them.
- Mocking everything by reflex. Mocks are a last resort, not a default.
- Skipping the `regression-test` step after a fix. The bug returns; you have nothing to catch it.

## See also

- All eleven other `tdd-*` skills in this plugin — each routing above points at one.
- Superpowers' `superpowers:test-driven-development` (if installed) — the workflow-level "use TDD now" trigger.

## References

- Kent Beck, *Test-Driven Development By Example* (Addison-Wesley, 2002) — Part III is the canonical pattern catalog.
- Beck's own "rules" essay and conference talks on TDD.
