---
name: arch-event-driven
description: Use when designing a system around asynchronous events — phrases like "event-driven architecture", "EDA", "event broker", "Kafka", "publish-subscribe at scale", "event-first design", "choreography vs orchestration", "event broker vs message queue", "broker topology vs mediator topology". Defines event-driven as a macro architectural style where components emit and react to events, not direct calls.
---

## One-line summary

Components communicate by emitting and reacting to events through a broker, not by calling each other directly. The producer doesn't know who consumes; the consumer doesn't know who produced.

## When to use this skill

- Loosely-coupled components that must scale independently.
- Real-time data processing, streaming, or analytics pipelines.
- Business workflows where the steps are naturally "X happened, now Y reacts".
- Systems where downstream consumers are added or removed without the producer knowing.

## When NOT to use this skill

- Strict transactional consistency requirements — events are eventually consistent.
- Synchronous request-response is the natural communication shape — forcing async adds latency and complexity.
- Small system with a handful of components that all live in one process — events are overkill; direct calls are simpler.

## Core content

**Two topologies** (Richards & Ford):

- **Broker topology** — components emit events to topics; subscribers consume; no central coordinator. Best for choreographed workflows where each step is a small reaction.
- **Mediator topology** — a central mediator orchestrates the workflow, sending commands to event processors. Best when the workflow has a defined sequence and someone has to know the whole flow.

**Events vs commands.**

- An **event** describes something that has happened, in past tense (`OrderPlaced`, `PaymentSettled`). The producer asserts a fact; subscribers decide whether to react. *Domain events* (see `ddd-domain-event`) are this concept at the model level; in EDA, the same events flow through the broker.
- A **command** is an imperative request to do something (`PlaceOrder`, `SettlePayment`). It addresses one specific handler. Events fan out; commands target one.

**Choreography vs orchestration.**

- **Choreography** — components react to events independently; the overall workflow emerges from their local decisions. Decentralized, scales well, hard to see the whole picture.
- **Orchestration** — a coordinator (mediator, saga, workflow engine) tells each component what to do next. Centralized, easier to reason about, single point of complexity.

**Broker choices.**

- **Topics with persistent log** (Kafka): events kept for a retention period; subscribers can replay. Strong for analytics, audit, late-joining consumers.
- **Queues with delivery guarantees** (RabbitMQ, SQS): events delivered once, then gone. Strong for work-queue patterns.
- **In-process bus** (Spring `ApplicationEventPublisher`): events stay inside a single JVM; cheap; appropriate for intra-monolith decoupling.

**Schema discipline.** Event schemas are the public API of an event-driven system. Treat them with the same versioning rigour as any external contract: backwards-compatible changes only; new fields are added with defaults; deprecated fields stay around until consumers migrate.

## Decision heuristics

- Choreography for workflows with ≤4 steps and stable participant set. Orchestration when steps multiply or compensation is non-trivial.
- Choose the broker by what you actually need: replay → log-based; one-shot delivery → queue; intra-process → in-memory bus.
- Event-driven within a monolith is also valid (in-process events) — you don't need Kafka to do EDA.
- Pair with a schema registry from day one. Event-driven systems rot fastest when schemas drift uncontrolled.

## Anti-patterns

- **"Event-driven" with commands disguised as events** (`UpdateOrderEvent`) — that's still RPC, just with extra latency. Past-tense names are mandatory.
- **Event-as-method-call.** A producer emitting an event and immediately listening for a specific consumer's response — synchronous RPC pretending to be async. Use a real request-response if that's what you need.
- **Schema chaos.** Producers changing event payloads without coordination. Consumers break silently.
- **No replay strategy.** Critical event lost, no way to recover. Choose broker retention deliberately.
- **Choreography for a 12-step workflow.** Nobody can reason about the whole thing. Promote to orchestration.

## See also

- `arch-microservices` — event-driven is one common communication style for microservices (the other is REST/gRPC).
- `arch-saga` — orchestrating distributed transactions via events / commands.
- `arch-event-sourcing` — when events are the *source of truth* for state, not just a communication mechanism.
- `arch-cqrs` — often paired with EDA: writes generate events; reads project from event-derived materialized views.
- `ddd-domain-event` — the model-level event concept that often becomes the wire-level event.

## References

- Richards & Ford, *Fundamentals of Software Architecture*, Ch. 14 (Event-Driven Architecture Style).
- Adam Bellemare, *Building Event-Driven Microservices* (O'Reilly).
- Khononov, *Learning DDD*, Ch. 15 (Event-Driven Architecture).
