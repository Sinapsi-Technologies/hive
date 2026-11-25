---
name: ddd-review
description: Use when the user asks to review code for DDD smells, PR-style — phrases like "is this DDD-clean", "review this for domain modeling issues", "what's wrong with this aggregate", "audit this module for DDD". Scans a file, diff, or module against the DDD anti-pattern catalog and reports specific violations with citations to the relevant pattern skill. Do NOT use when the user wants new code (route to ddd-implement) or wants to design from scratch (route to ddd-design).
---

## One-line summary

Walk a target file, diff, or module through the DDD anti-pattern checklist and report violations with severity, location, and a fix direction.

## When to use this skill

- Pull-request style review framed around DDD ("is this DDD-clean?", "review this aggregate").
- A specific module the user suspects has model rot.
- Code authored in a hurry that is now being hardened.
- Onboarding a teammate to a domain model — "what's wrong with our current code?".

## When NOT to use this skill

- The user wants new code → route to `ddd-implement`.
- The user wants to redesign from scratch → route to `ddd-design`.
- The user already knows the target shape and wants the path to it → route to `ddd-refactor`.

## Core content

Run the target code through six checks. For each finding, report severity (`blocker` / `major` / `minor`), the offending location, and the pattern skill that explains the fix.

**1. Does it speak the ubiquitous language?** Class and method names should be nouns and verbs from the business, not CRUD (`createX`, `updateY`) or technical jargon (`Manager`, `Helper`). A model that reads like a database table is a smell.

**2. Are aggregate boundaries respected?** Aggregates reference *other* aggregates by ID only, never by object reference. A `class Order { val customer: Customer }` field is almost always a `blocker`-level finding.

**3. Anemic model check.** Where does the behavior live? If it is all in `*Service` classes and the entity is fields-with-getters, the entity is anemic. Push behavior down onto the entity / aggregate root.

**4. Primitive obsession.** Look for `String customerId`, `Long amount`, `String email`. Each is a missed value object. Severity scales with how often the primitive flows through method signatures.

**5. Leaky repository.** Domain code should not import JPA, JDBC, HTTP clients, or framework types. If the domain layer mentions `EntityManager`, `Predicate`, or `ResultSet`, the abstraction has leaked.

**6. Factory vs constructor.** If a constructor takes nine parameters or does substantial work, a factory is hiding. Conversely, a `FooFactory` whose single method `create(...)` does nothing the constructor couldn't do is over-engineering.

## Decision heuristics

- Report findings, not opinions. Each finding cites a line range and a pattern skill.
- Severities: `blocker` (model is wrong, will mislead other code), `major` (real smell, refactor soon), `minor` (style or naming, fix in passing).
- Don't pattern-name without showing the violation. "This is anemic" is not a finding; "the `Order` entity has 14 fields and zero methods that change them; `OrderService.process()` does all the state changes" is.

## Anti-patterns

- Rubber-stamping ("looks good") on code that obviously violates the catalog.
- Reviewing for style instead of model — bracket placement is not a DDD concern.
- Citing patterns without code references.
- Treating every long file as a smell — length alone is not a violation.

## See also

- `ddd-anti-patterns` — the catalog this skill checks against.
- Every pattern skill in `ddd-core` — findings cite the relevant one.

## References

- Evans, *Domain-Driven Design* (2003), Ch. 4 (Isolating the Domain), Chs 5–6 (building blocks), Part III (smells that motivate deeper insight).
