---
name: ddd-event-storming
description: Use when running or designing an EventStorming workshop — phrases like "EventStorming", "event storming", "domain discovery workshop", "sticky notes on a wall", "big-picture EventStorming", "process modeling EventStorming", "software design EventStorming", "pivotal event", "hotspot", "should we do an EventStorming session". Defines EventStorming as Alberto Brandolini's lightweight collaborative workshop format for exploring a domain by mapping it as a sequence of past-tense events.
---

## One-line summary

A collaborative workshop format invented by Alberto Brandolini: domain experts and engineers map a business process as a left-to-right sequence of past-tense events on a wall (physical or virtual), surfacing commands, actors, policies, read models, and hotspots along the way.

## When to use this skill

- Greenfield project — discovering the domain and its bounded contexts before writing code.
- Existing system that nobody fully understands — reconstructing the domain knowledge that's gone tribal.
- Cross-team alignment — bringing domain experts and engineers into the same conversation.
- Identifying bounded context boundaries — events that don't fit a single context usually mark a boundary.
- Designing a single feature or process — process-modeling EventStorming zooms in on one workflow.

## When NOT to use this skill

- Tactical code-level question — EventStorming is a domain-discovery workshop, not a code design tool.
- The domain is genuinely well-understood and stable — the workshop pays off when there's confusion or fresh perspective needed; not when everyone already agrees.
- Without a domain expert participating — running EventStorming with only engineers reproduces what engineers already think, not what the business actually does.

## Core content

**Three levels of EventStorming** (Brandolini):

- **Big Picture.** The whole business process across departments / contexts. Output: a wall covered in events, the chronological story of "what happens" end-to-end. Typically half a day to a day.
- **Process Modeling.** Zoom into one process. Add commands (what triggers events), actors (who issues commands), policies (rules), read models (data needed to decide), external systems. Typically a few hours per process.
- **Software Design.** Even finer zoom. Map events to aggregates. Decide which events update which aggregates. The output feeds directly into the tactical model — entities, value objects, repositories.

**The vocabulary** (sticky note colours by convention):

- **Domain events** (orange) — past-tense, business-meaningful. "OrderPlaced", "RefundIssued".
- **Commands** (blue) — what triggers an event. "PlaceOrder", "IssueRefund".
- **Actors** (yellow) — who issues commands. "Customer", "Operator".
- **Policies** (purple) — rules that link events to commands. "Whenever payment fails, retry within 24 hours."
- **Read models** (green) — what data is needed to decide. "Available stock view".
- **External systems** (pink) — outside the model. "Payment provider", "Email service".
- **Hotspots** (red) — questions, conflicts, areas of confusion. The most valuable category — every red note is a learning opportunity.

**The rhythm.**

1. **Chaotic exploration.** Everyone writes events on stickies and slaps them on the wall. No structure, no order.
2. **Timeline.** Re-arrange events left-to-right chronologically. Duplicates collapse; gaps surface.
3. **Pivotal events.** Identify the most important state transitions. These often mark bounded context boundaries.
4. **Walk the timeline.** Talk through the story from start to finish. Surface contradictions; refine events; spawn hotspots.
5. **Add commands, actors, policies, read models.** Build up the surrounding structure.

**Outputs to take back.** Photo (or virtual export) of the wall. Hotspot list. Initial guess at bounded contexts. Candidate aggregates and the events they emit. Open questions for follow-up.

## Decision heuristics

- Domain experts are mandatory. If they're not in the room, postpone the session.
- Optimize for *participation*, not *correctness*. Events that are slightly wrong now will be corrected as the timeline is walked.
- Hotspots are the most important output. Every red note is a learning opportunity; chase them down post-workshop.
- Big-picture first, then drill into specific processes only after the wider context is mapped. Going straight to process modeling without the big picture means picking the wrong process to model.
- For remote teams, virtual whiteboards (Miro, Mural, FigJam) work — but in-person sessions are noticeably more productive when feasible.

## Anti-patterns

- **EventStorming with no domain expert.** Engineering ideas masquerading as domain understanding.
- **One person at the wall, everyone else watching.** No collaboration → no shared understanding.
- **Imperative-tense events.** "UpdateOrder" is a command, not an event. Past tense is mandatory — the workshop's vocabulary is precise about this.
- **Software Design EventStorming before Big Picture.** Modelling aggregates without understanding the business flow. Premature.
- **Wall left rolled up after the session.** The artefacts have to translate into code, tickets, or follow-up sessions. Otherwise the workshop becomes "team-building" with no follow-through.

## See also

- `ddd-design` — EventStorming is one of the most common discovery techniques `ddd-design` will reach for.
- `ddd-bounded-context` — pivotal events surface context boundaries.
- `ddd-ubiquitous-language` — the events become vocabulary in the language.
- `ddd-domain-event` — the tactical-level codification of events the workshop surfaced.

## References

- Alberto Brandolini, *Introducing EventStorming* (Leanpub, work-in-progress) — the canonical reference.
- Vlad Khononov, *Learning Domain-Driven Design*, Ch. 12 (EventStorming).
- Brandolini's talks and blog posts (`ziobrando.blogspot.com`).
