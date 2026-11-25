---
name: ddd-domain-event
description: Use when modeling significant domain occurrences — phrases like "domain event", "OrderPlaced", "PaymentSettled", "something the business cares happened", "decouple these two aggregates", "eventual consistency across aggregates". Defines domain events, their naming (past tense, business-meaningful), and how they coordinate work across aggregates without violating aggregate boundaries.
---

## One-line summary

A domain event is an immutable record that a business-significant thing happened — past tense, named in the ubiquitous language, carrying the data downstream listeners need to react.

## When to use this skill

- Crossing aggregate boundaries — two aggregates must coordinate, but they live in separate transactions.
- Informing other bounded contexts that a noteworthy thing happened in this one.
- Building read models, projections, or downstream integrations (audit logs, analytics, search indexes).
- Signalling a lifecycle transition the business itself talks about: an order is placed, a payment settles, a refund is issued, a shipment leaves the warehouse.

## When NOT to use this skill

- The state change is internal to one aggregate and has no listeners — just call the method directly.
- The "event" is really a command (an imperative request to do something). Commands and events are different: `PlaceOrder` is a command, `OrderPlaced` is an event.
- The interaction must be synchronous within a single transaction — that is not an event, it is a method call.

## Core content

A domain event is named in the past tense: `OrderPlaced`, `PaymentSettled`, `RefundIssued`, `ShipmentDispatched`. The name itself is a piece of the ubiquitous language — if domain experts say "we settle a payment", the event is `PaymentSettled`.

Events are *immutable* and *self-describing*. Once published, an event never changes; subscribers may store, replay, or forward it. The event carries IDs and the minimum data subscribers need to react — typically aggregate identifiers, timestamps, and a few salient fields.

Events are published *after* the emitting aggregate's state mutation succeeds. They are typically published transactionally with that mutation (outbox pattern, transactional event bus) so that subscribers see an event if and only if the state change actually committed.

Subscribers live in *other* aggregates or *other* contexts. They react asynchronously. Within-aggregate state changes don't need events — events exist precisely to cross boundaries.

Domain events are useful independent of CQRS or event sourcing. You can ship them with a plain relational store, an outbox table, and a publisher; you do not need to event-source anything.

## Decision heuristics

- If domain experts use a noun for "what happened" (a settlement, an approval, a delivery), it is probably an event worth modeling.
- If the only consumer of the event is the same aggregate that emitted it, drop the event and just call the method.
- Past tense is mandatory. If you find yourself writing `UpdateOrderEvent`, you are modeling a command, not an event.

## Anti-patterns

- Imperative event names (`UpdateOrder`, `ProcessRefund`) — those are commands.
- Fat events carrying entire entity graphs — subscribers shouldn't need to chase references through events.
- Using events for synchronous control flow within one transaction — events are explicitly for crossing boundaries.
- Publishing events before the state change commits — subscribers see phantom events when the transaction rolls back.

## See also

- `ddd-aggregate` — events are the canonical way to coordinate across aggregate boundaries.
- `ddd-context-mapping` — domain events often serve as the published language between contexts.
- `ddd-deeper-insight` — making implicit "what happened" concepts explicit as events is a classic Part III move.

## References

- Evans, *Domain-Driven Design* (2003), Ch. 5 mentions events briefly in the context of Domain Service; the canonical treatment came later in Vernon, *Implementing Domain-Driven Design* (2013).
