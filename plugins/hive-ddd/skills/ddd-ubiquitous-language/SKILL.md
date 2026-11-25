---
name: ddd-ubiquitous-language
description: Use when establishing or auditing a domain's shared vocabulary — phrases like "ubiquitous language", "what should we call this concept", "the team uses different words for the same thing", "the code uses different names than the business". Defines what a ubiquitous language is, how to grow one, and the smell tests that reveal a missing one. Do NOT use for code generation; this is a process/language skill.
---

## One-line summary

Establish a single vocabulary shared by domain experts, developers, code, and tests — within one bounded context.

## When to use this skill

- A new project starting to model a domain.
- Persistent terminology drift between business and engineering.
- The code's names don't match how domain experts talk.
- Translation tables sit between conversation and code ("when the business says 'invoice', we mean `BillingDocument`").

## When NOT to use this skill

- Code-level pattern questions → delegate to tactical pattern skills.
- Cross-context terminology translation → route to `ddd-context-mapping`.
- Naming conventions for variables and functions in general → that is a code-style concern, not DDD.

## Core content

A ubiquitous language is the shared, precise vocabulary that domain experts and engineers use to discuss a model — and that appears verbatim in the code, the tests, the database, and the UI. It is not a glossary the team agrees to consult; it is the language they actually speak and write.

**How to grow one.** Listen to domain experts and capture their phrases without paraphrasing. When the experts say "settle a payment", the code has a method called `settle`, not `processPayment`. When experts disagree about a term, push them to reconcile — that disagreement is hiding a real distinction (often two concepts crammed into one word).

**Per bounded context.** A ubiquitous language is consistent *within one bounded context*. Two contexts may use the same word with different meanings, and that is fine — context mapping handles the translation. The mistake is trying to enforce one global enterprise-wide language.

**The reading-aloud test.** Take a piece of code (a method body, a test name, a class hierarchy) and read it aloud to a domain expert. If they recognize what it does, the language is working. If they ask "what does that mean?", the language has drifted from the model.

## Decision heuristics

- Apply *inside* a bounded context. Resist the urge to unify language across an enterprise.
- Push back when developers invent names ("Manager", "Helper", "Processor") instead of asking experts what to call the concept.
- Kill synonyms aggressively. "Customer", "Client", and "Account" should not all coexist meaning the same thing in one context.

## Anti-patterns

- Developers inventing names because asking the expert "takes too long".
- Tolerating synonyms because each team prefers a different one.
- Translation tables between conversation and code — a sign that the model is misaligned.
- Maintaining a glossary as a separate document instead of the code being the glossary.

## See also

- `ddd-bounded-context` — one language per context; the boundary makes a coherent language possible.
- `ddd-design` — language is the first deliverable of any design session.
- `ddd-context-mapping` — translating language across contexts.

## References

- Evans, *Domain-Driven Design* (2003), Ch. 2 (Communication and the Use of Language).
