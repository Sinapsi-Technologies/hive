---
name: gof-template-method
description: Use when an algorithm has a fixed skeleton with variable steps — phrases like "Template Method pattern", "the steps are the same but one varies", "Coffee and Tea both brew via boil-brew-pour-add-condiments but differ in two steps", "hook method", "the Hollywood principle don't call us we'll call you". Defines Template Method as a base class that defines an algorithm's skeleton in a final method, deferring some steps to subclasses.
---

## One-line summary

Define the skeleton of an algorithm in a base method, deferring some steps to subclasses — letting subclasses redefine certain steps without changing the algorithm's structure.

## When to use this skill

- Two or more classes follow the same overall procedure but differ in one or two steps (Beck's "Coffee and Tea both brew" example).
- A workflow has a fixed sequence but specific stages vary by use case (e.g., a parsing pipeline where the tokenize step differs per format).
- Cross-cutting framework hooks: a base class provides lifecycle methods, subclasses override the ones they care about (Spring's `JdbcTemplate`, JUnit's `setUp` / `tearDown`).

## When NOT to use this skill

- The variation is *the algorithm itself*, not steps within a skeleton — that's `gof-strategy`.
- Only one step varies and the variation is small — a function parameter or lambda is simpler than a subclass.
- The base class becomes an inheritance choke point that prevents the subclass from doing anything outside the template — that's a sign the abstraction is too rigid.

## Core content

A `TemplateMethod` is a (typically `final` or non-overridable) method on the base class that calls a sequence of steps, some implemented in the base, others abstract or "hook" methods that subclasses override.

```
base.run():
    step1()         # final, implemented in base
    step2Abstract() # abstract — subclass must provide
    step3Hook()    # default no-op — subclass may override
    step4()         # final, implemented in base
```

Three kinds of steps:
- **Concrete** — fully implemented in the base class.
- **Abstract** — subclass must implement.
- **Hook** — default behavior (often a no-op or sensible default); subclass may override.

The "Hollywood Principle" — "don't call us, we'll call you" — is the framework-level expression of this pattern. The base class calls the subclass's overrides at the right moments; the subclass doesn't call the base class to drive the workflow.

## Decision heuristics

- Use when the *skeleton* is shared and the *steps* vary. If only one step varies, consider a function parameter instead of a subclass.
- Make the template method `final` (or non-overridable) so subclasses can't break the workflow.
- Distinguish abstract steps (must override) from hook steps (may override) — and document which is which.
- If subclasses keep needing to override more and more steps to do non-trivial work, the template is too rigid.

## Anti-patterns

- Template Method where the "template" is a giant method with many overridable steps — the rigidity that makes Template Method valuable disappears.
- Overriding the template method itself in a subclass — defeats the purpose.
- Calling `super.step1()` from a subclass's overridden step — couples subclass to base internals.

## See also

- `gof-strategy` — when the *whole algorithm* varies, not just steps within it.
- `gof-factory` — Factory Method *is* an instance of Template Method (the algorithm defers the choice of concrete type to a subclass).
- `gof-kotlin-idioms` — Kotlin's `open` / `final` defaults make Template Method explicit, and higher-order functions often beat subclass-based templates for one or two varying steps.

## References

- *Head First Design Patterns* (2nd ed), Ch. 8 ("Encapsulating Algorithms: the Template Method Pattern") — canonical exposition with the Coffee/Tea example.
