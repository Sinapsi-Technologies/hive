---
name: arch-adr
description: Use when recording an architectural decision so future maintainers know why — phrases like "ADR", "architecture decision record", "why did we pick X", "lightweight architecture documentation", "decision log", "should this be an ADR", "tribal knowledge", "we chose this two years ago and now nobody remembers why". Defines the ADR as a short, structured document capturing *what* was decided, *why*, *alternatives considered*, and *consequences* — written at the time of the decision and kept in the repo.
---

## One-line summary

A short, structured document capturing one architectural decision: *what* was decided, *why*, *what alternatives were rejected*, and *what consequences follow*. Written at the moment of decision, lives in the repo forever.

## When to use this skill

- Any architectural choice that future maintainers will second-guess if they don't know the reasoning. Examples: picking a database, picking a messaging system, choosing hexagonal vs layered, picking microservices over monolith.
- A decision whose alternatives were genuinely considered and rejected — capturing why is most valuable.
- Onboarding: new joiners can read the ADR set to understand how the architecture got to its current state.

## When NOT to use this skill

- Tactical code-level decisions (using `data class` vs `class`, file organization) — these are too granular; the cost-of-writing exceeds the benefit.
- Decisions that have no real alternatives (Java vs Python on a Python-only team is not a decision).
- After-the-fact "let me write an ADR for this decision we made two years ago and don't remember the reasoning for" — the value is in the timestamp; back-dating is worth less.

## Core content

**Standard ADR structure** (Michael Nygard's original template):

- **Title.** A short noun phrase. "Use PostgreSQL as the primary store" or "Adopt hexagonal architecture for the order service".
- **Status.** `proposed` / `accepted` / `deprecated` / `superseded by ADR-NNN`.
- **Context.** What's the situation, the forces, the constraints? Why is this decision needed *now*?
- **Decision.** What did we decide. Stated as a positive assertion ("We will…"), not as discussion ("We're thinking about…").
- **Consequences.** What follows from this decision — good and bad. Forces it commits us to. New problems it creates.
- *(Optional)* **Alternatives considered.** What else was on the table; why each was rejected.

**File layout.**

```
docs/adr/
├── 0001-use-postgresql.md
├── 0002-adopt-hexagonal-architecture.md
├── 0003-event-bus-via-rabbitmq.md
├── 0004-replace-rabbitmq-with-kafka.md  (supersedes 0003)
└── README.md      ← index of ADRs
```

Numbering is sequential and never reused. When an ADR is replaced, the new ADR references the old one and the old one's status becomes `superseded by ADR-NNN`. The old ADR stays in the tree — the history is the point.

**Length.** A single page. If an ADR is two pages, it's probably making two decisions and should be split.

**Tooling.** `adr-tools` (shell), `adr-manager`, MADR template, Log4brains — all generate / index ADRs. Useful at scale; markdown by hand works fine for small teams.

**Living document vs decision log.** ADRs are decision logs — *immutable* once accepted. The "living document" is something else (the README, the architecture diagram). Don't update an ADR's decision text; supersede it with a new ADR.

## Decision heuristics

- Write the ADR *before* implementing the decision. Forces clarity.
- Write the ADR even if the decision feels obvious — "obvious" often means "I'm not thinking carefully about it".
- Keep alternatives short but specific. "We considered Kafka but rejected it because we don't have streaming use cases yet."
- Numbers are sequential and immutable. Don't re-number. Don't reuse a deleted number.
- ADRs live in the repo, not a wiki. They're versioned, reviewed, and travel with the code.

## Anti-patterns

- **Architecture wiki page.** Drifts immediately. Doesn't survive team turnover. Reviewers don't read it.
- **ADRs written after the fact, for decisions nobody remembers.** Time-stamps matter. Backfilling has limited value.
- **ADRs that hide the alternatives section.** Most of the value is in *why we rejected X*. Without it, the ADR is just a declaration.
- **Updating an old ADR's decision text.** Breaks the historical record. Supersede instead.
- **Hundreds of ADRs for tactical decisions.** ADRs are *architectural*; not every file-organization choice deserves one. Keep the bar high enough that the set stays readable.

## See also

- `arch-fitness-functions` — fitness functions enforce *what*; ADRs document *why*. Pair them.
- All other `arch-*` skills — most architectural decisions deserve an ADR.

## References

- Michael Nygard, "Documenting Architecture Decisions" (2011 essay) — original ADR articulation.
- MADR template — modern markdown ADR template.
- `adr-tools` — Joel Parker Henderson's shell tool.
- Richards & Ford, *Fundamentals of Software Architecture*, Ch. 19 (Architecture Decisions).
