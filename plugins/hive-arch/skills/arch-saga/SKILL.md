---
name: arch-saga
description: Use when a business transaction spans multiple services or aggregates and no distributed-transaction primitive is available — phrases like "saga pattern", "distributed transaction", "compensating action", "choreographed saga", "orchestrated saga", "process manager", "should we use a saga here", "how do we roll back across services". Defines a saga as a sequence of local transactions tied together by compensating actions, with two implementation styles (choreography and orchestration).
---

## One-line summary

Execute a multi-step business workflow as a sequence of local transactions, each in its own service / aggregate. If a step fails, run *compensating* actions to undo the prior successful steps. There's no rollback — only forward-going compensation.

## When to use this skill

- A business process spans multiple services or multiple aggregates, and they cannot share a single transaction.
- Some steps are external (third-party API, payment processor) and cannot be transactional.
- Long-running workflows where holding a distributed lock is unrealistic.
- The business has a clear concept of "if step X fails, run step X' to undo".

## When NOT to use this skill

- Everything happens inside a single service / single aggregate — use a local database transaction.
- The steps are idempotent and order-independent — you may not need saga's coordination at all.
- "Atomic" rollback semantics are genuinely required (rare in distributed systems) — saga doesn't give you those; it gives you eventual consistency with compensation.

## Core content

**Two styles** with different coordination shapes:

**Choreographed saga.** Each service listens for events and decides when to act. Service A completes step 1 and emits `Step1Completed`; service B picks it up, completes step 2, emits `Step2Completed`; and so on. Compensation is also emitted as events (`Step1Compensated`).

- Pros: decentralized, no single point of failure, scales horizontally.
- Cons: the full workflow is implicit — nobody owns it; debugging a stuck saga is hard; adding a new step touches multiple services.

**Orchestrated saga (process manager).** A central orchestrator drives the workflow. It sends commands to each service in turn, listens for their responses, and decides the next step (forward or compensate).

- Pros: the workflow is explicit, in one place, easy to reason about and test.
- Cons: the orchestrator is a coordination component that must be highly available; centralization is a real cost.

**Compensating actions, not rollbacks.**

- A rollback (database transaction) restores state as if nothing happened. A compensating action *undoes the business effect*. Refunding a payment is a compensation; rolling back the payment doesn't exist if it already went through.
- Compensations must be idempotent and safe to call after partial completion.
- Some actions cannot be compensated (sent an email, called an external irreversible API). Design those steps to come *last*, so a failure earlier in the saga never reaches them.

**Idempotency and at-least-once delivery.** Sagas live in an event-driven world. Messages can be redelivered. Every step and every compensation must be idempotent.

**Saga state.** The orchestrator (or, in choreography, distributed across services) tracks where each saga instance is. Persist this state — saga crashes happen, and you need to resume.

## Decision heuristics

- ≤3 steps with stable participants → choreography.
- 4+ steps, or steps may be added later, or compensation logic is non-trivial → orchestration.
- Order the steps so the irreversible step is last.
- Build idempotency into every step from day one.
- Persist orchestrator state; reload on restart.
- Use a workflow engine (Temporal, Camunda, Conductor) instead of rolling your own orchestrator when complexity warrants — but use them deliberately; they're real dependencies.

## Anti-patterns

- **"Saga" that's really a chained synchronous RPC.** Service A calls B calls C, with no event broker. That's a distributed call chain, not a saga; failures don't compensate, they just propagate.
- **Compensating action that's not actually a compensation.** Marking a record "cancelled" instead of refunding the user. Half-undoing.
- **Non-idempotent steps.** Same message redelivered → step runs twice → user charged twice.
- **Irreversible step in the middle.** Step 3 sends an unrecallable email; steps 4-5 fail; you can't undo the email. Put it last.
- **Saga of 20 steps.** Probably a workflow engine, not a hand-rolled saga.

## See also

- `arch-event-driven` — saga events flow through the broker.
- `arch-microservices` — sagas are the standard answer to "how do we transact across services".
- `arch-event-sourcing` — saga state is often event-sourced.
- `arch-circuit-breaker` — saga steps that call other services should protect themselves.
- `ddd-domain-event` — saga steps produce / consume domain events.

## References

- Hector Garcia-Molina & Kenneth Salem, "Sagas" (1987) — original paper.
- Khononov, *Learning DDD*, sections on sagas in microservices / event-driven chapters.
- Bellemare, *Building Event-Driven Microservices*, Ch. 8 (Building Workflows with Microservices).
