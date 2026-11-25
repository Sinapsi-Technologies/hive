---
name: arch-microservices
description: Use when designing or reviewing a microservices architecture — phrases like "microservices", "should we split this service", "service boundaries", "bounded context per service", "API gateway", "backend for frontend", "BFF", "service mesh", "microservices vs monolith", "database per service", "data ownership". Defines microservices as independently deployable services aligned with business capabilities — and the discipline of resisting the style until the monolith proves inadequate.
---

## One-line summary

Decompose the system into independently deployable services, each owning its data and aligned with a business capability. Communicate over the network. Pay the operational tax in exchange for autonomy and independent scaling.

## When to use this skill

- Multiple teams stepping on each other in a single codebase / deployment.
- Different parts of the system need *independent scaling characteristics* (one is CPU-bound, another I/O-bound, a third bursty).
- Different parts have *different release cadences* and one team's deploy must not block another's.
- Different parts have *different reliability profiles* (a critical payment path; a non-critical analytics dashboard).

## When NOT to use this skill

- One team, one product, no scaling pressure — you don't have the problems microservices solve. Keep the monolith.
- Domain not understood well enough to draw service boundaries — premature splits are very expensive to reverse.
- No operational maturity (no CI/CD, no observability, no on-call) — microservices amplify operational pain.
- Resume-driven development. Stop.

## Core content

**Service boundaries follow business capabilities, not technical layers.** A `customer-service`, `orders-service`, `pricing-service` is correct. A `database-service`, `api-service`, `cache-service` is anti-pattern. Boundaries are typically aligned with DDD bounded contexts (see `ddd-bounded-context`).

**Each service owns its data.** No shared database. If service B needs data from service A, B either calls A's API or A publishes events that B subscribes to. Sharing a database underneath two "microservices" makes them a distributed monolith — the worst of both worlds.

**Communication patterns.**

- **Synchronous (REST, gRPC).** Direct, easy to reason about, latency-coupled. Use for query-heavy patterns where the caller needs an immediate answer.
- **Asynchronous (events, see `arch-event-driven`).** Decoupled in time, eventually consistent. Use for workflows that span services.

Most production systems mix both.

**Edge patterns.**

- **API gateway** — single entry point for external clients; handles auth, rate-limiting, routing, response composition. Decouples clients from internal service topology.
- **Backend for Frontend (BFF)** — a gateway tailored per client type (mobile, web, partner API). Avoids the "one gateway must satisfy every client" trap.
- **Service mesh** (Istio, Linkerd) — sidecar proxies handling traffic, mTLS, retries, observability. Moves cross-cutting infrastructure out of services. Justified at scale; overkill below ~10 services.

**Anti-pattern alert: distributed monolith.** Services deployed independently but coupled tightly (shared database, synchronous chains, lock-step releases). Carries all the operational cost of microservices with none of the autonomy benefit. Hardest to escape.

## Decision heuristics

- **Default to monolith.** Microservices solve specific problems (team autonomy, independent scaling, fault isolation). If you don't have those problems, you don't need this style.
- Service boundaries align with bounded contexts. Use `ddd-bounded-context` first; let the contexts inform the splits, not the other way around.
- A service that owns no data of its own is a smell — usually it's an adapter or a coordinator that should live elsewhere.
- Synchronous chains of more than 2-3 services for a single user request indicate broken decomposition. Reach for events or rethink the boundary.
- BFF when client types diverge significantly; gateway with content negotiation when they don't.

## Anti-patterns

- **Distributed monolith.** Services that must release together. Worst of both worlds.
- **Nanoservices.** Services so small they ship per-class. Coordination cost exceeds any decomposition benefit.
- **Shared database.** Two services reading and writing the same tables — they're not separate.
- **Synchronous chains.** Service A calls B calls C calls D for one user request. Each adds latency and failure surface; the chain fails when any link does.
- **Choosing microservices because Netflix does** — Netflix solved Netflix's problems; your problems are different.

## See also

- `ddd-bounded-context` — service boundaries should follow bounded contexts.
- `arch-event-driven` — the standard async communication style.
- `arch-saga` — coordinating distributed transactions.
- `arch-circuit-breaker` — failure isolation between services.
- `arch-strangler-fig` — migrating from monolith to microservices.

## References

- Richards & Ford, *Fundamentals of Software Architecture*, Ch. 17 (Microservices).
- Sam Newman, *Building Microservices* (O'Reilly).
- Sam Newman, *Monolith to Microservices* (O'Reilly).
- Khononov, *Learning DDD*, Ch. 14 (Microservices).
