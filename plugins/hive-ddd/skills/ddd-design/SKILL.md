---
name: ddd-design
description: Use when the user wants to design a new domain model from scratch — phrases like "help me model X", "what's the bounded context for Y", "design a domain for this feature", "we need a domain model". Walks the user through ubiquitous language, bounded context, aggregate identification, and then routes to the relevant pattern skills. Do NOT use for code generation (route to ddd-implement) or refactoring existing code (route to ddd-refactor).
---

## One-line summary

Guide a user from a feature brief to a draft domain model: ubiquitous language → bounded context → aggregates → key events.

## When to use this skill

- A new feature with no model yet.
- Greenfield project starting a domain layer.
- An existing system being extended into a new business area where no model exists.
- The user explicitly asks to "design", "model", or "architect" a domain.

## When NOT to use this skill

- Generating code for an already-decided pattern → route to `ddd-implement`.
- Transforming existing code → route to `ddd-refactor`.
- Auditing code for DDD violations → route to `ddd-review`.

## Core content

Design is a four-step walk. Most of the value lives in the first two steps; the latter two follow once the language and boundaries are clear.

**Step 1 — Elicit the ubiquitous language.** Ask the user (or a domain expert) to describe the feature in their own words. Capture nouns and verbs verbatim. Push back on jargon that sounds technical ("entity", "record", "DTO") and on overloaded English words. See `ddd-ubiquitous-language` for the discipline.

**Step 2 — Sketch the bounded context.** Where does this model end? What model are we *not* responsible for? Name the upstream and downstream contexts. See `ddd-bounded-context` and, for the relationships, `ddd-context-mapping`.

**Step 3 — Identify aggregates.** From the captured nouns, group those that change together transactionally. Each cluster has one root; external code references only the root. See `ddd-aggregate`.

**Step 4 — Name candidate domain events.** Past-tense things the business cares happened (`OrderPlaced`, `RefundIssued`). These reveal the seams between aggregates and contexts. See `ddd-domain-event`.

## Decision heuristics

- Spend roughly 80% of the conversation on language and boundaries (steps 1–2). If the model feels wrong later, it is almost always because step 1 was rushed.
- If the user resists naming things precisely, that *is* the work — push back politely.
- Stop at the sketch. Do not produce code in this skill; hand off to `ddd-implement` when the user asks for it.

## Anti-patterns

- Jumping straight to entities and attributes before the language is settled.
- "Let's just put it in one big context" — saves a conversation now, costs a context split later.
- Designing the database schema in parallel — schema thinking corrupts the model.
- Producing a class diagram before any verb has been spoken.

## See also

- `ddd-ubiquitous-language` — the first deliverable of every design session.
- `ddd-bounded-context` — where the model ends.
- `ddd-context-mapping` — relationships to neighbouring models.
- `ddd-aggregate` — clustering entities by transactional consistency.
- `ddd-domain-event` — the seams that connect aggregates and contexts.
- `ddd-subdomain-classification` — is this even worth modeling deeply?

## References

- Evans, *Domain-Driven Design* (2003), Part I (Chs 1–3) and Part IV (Chs 14–15).
