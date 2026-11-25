---
name: arch-hexagonal
description: >-
  Use when designing or reviewing ports and adapters, especially in a Hive Java project — phrases like "hexagonal architecture", "ports and adapters", "clean architecture", "domain should not know about Spring", "input ports", "output ports", "primary vs secondary adapters". Defines the invariant dependency rules and the Hive profile: a pure domain, application-owned ports and use cases, and infrastructure adapters.
---

## One-line summary

Keep the domain at the centre and free of framework and infrastructure dependencies. In the Hive profile, the application layer owns the input and output ports, while infrastructure adapters invoke or implement them. Dependencies point inward toward application and domain.

## When to use this skill

- The domain is complex and you want it testable in isolation from frameworks.
- You expect to swap infrastructure (database, message broker, HTTP framework) over the system's life.
- The domain logic must survive multiple delivery mechanisms (REST API, message handler, scheduled job all calling the same domain).
- DDD codebase that wants a clear technical architecture (`arch-hexagonal` is the conventional architectural shape for DDD).

## When NOT to use this skill

- Simple CRUD with no real domain logic — layered architecture (`arch-layered`) is enough. Hexagonal's ceremony costs more than it saves.
- Tiny scripts or short-lived prototypes.
- Teams unfamiliar with dependency inversion who would build "fake hexagonal" — folders renamed without actually inverting the dependency direction.

## Core content

The Hive profile has four collaborating parts:

- **Domain.** Pure business concepts, invariants, entities, value objects, aggregates, domain services and domain events. It knows nothing about Spring, JPA, HTTP, Kafka, application services or infrastructure.
- **Application.** Use cases that coordinate domain behavior, transaction boundaries and external capabilities. It may depend on the domain but not on infrastructure.
- **Ports.** Contracts owned by the application boundary:
  - **Input ports** in `application.ports.in` expose use cases to driving actors.
  - **Output ports** in `application.ports.out` describe capabilities the application needs from external systems, such as persistence, messaging, clocks and ID generation.
- **Adapters.** Technology-specific boundary implementations:
  - **Inbound adapters** translate transport models and invoke input ports.
  - **Outbound adapters** implement output ports using databases, brokers, remote APIs or other infrastructure.

The **dependency rule** is:

```text
inbound adapter  ──> application input port ──> application service ──> domain
outbound adapter ──> application output port <── application service
```

The domain imports neither application nor infrastructure. The application imports the domain and its own port contracts, never concrete adapters. Infrastructure depends on the inner contracts. Wiring happens at the composition root.

**Why it's worth it.** Tests for the domain run instantly with in-memory adapters. Changing the database doesn't touch the domain. A second delivery mechanism (message handler vs REST) reuses the same use cases. The model survives even when half the infrastructure is replaced.

### Hive contracts

When the `hive-core` skill is available, use it as the authoritative source for the concrete Java contracts and the version actually resolved by the application. In particular, it defines the applicable `UseCase`, `Command`, `InputPort`, `OutputPort`, `Result`, view, event and mapper conventions. Do not duplicate or guess their signatures here.

Inbound adapters convert DTOs or transport models before calling an input port. Application services coordinate the use case and call output ports. Outbound adapters translate between infrastructure representations and the application or domain models at the boundary.

### Other hexagonal variants

Some ports-and-adapters implementations place ports directly in a domain module or use a single undivided application core. Onion Architecture and Clean Architecture also use different ring and layer names. These variants share dependency inversion but not necessarily the Hive package layout.

For a Hive project, the Hive profile above takes precedence: do not move `application.ports.in` or `application.ports.out` into the domain merely to match another hexagonal variant.

## Decision heuristics

- Prefer a real domain model. If the application is only simple CRUD, verify that the additional boundaries provide enough value to justify their cost.
- Domain code does not depend on Spring annotations, JPA annotations, transport types or infrastructure libraries. It may use deliberately adopted domain abstractions from the resolved Hive contracts; verify those contracts instead of treating every library dependency as equivalent to framework leakage.
- In Hive, ports live in the application layer and adapters live in infrastructure. The build system and architecture tests should enforce the dependency direction.
- Name ports after application capabilities and business intent, not after a framework, protocol or persistence API.
- Treat the repository's established package conventions and the resolved Hive contracts as evidence. Do not relocate existing boundaries mechanically without assessing compatibility and migration impact.
- Be wary of inventing more than the necessary number of ports — every interface is a cost.

## Anti-patterns

- **Hexagonal in name only.** Folders renamed `domain/` and `infrastructure/` but the domain still imports Spring or JPA. Cargo cult.
- **Wrong profile.** Moving Hive input or output ports into the domain because another hexagonal variant does so. This breaks the project convention without improving dependency inversion.
- **Anemic ports.** Output ports that are 1:1 with JPA's `JpaRepository` interface — the abstraction adds no abstraction.
- **God adapter.** One adapter implementing six ports. Split by responsibility.
- **Bidirectional dependency at the seam.** Domain or application code imports an adapter type by name. Defeats inversion.
- **Adapter doing business logic.** Logic that belongs in the domain leaking into the controller or the repository implementation.
- **Transport leakage.** Input ports, commands or domain types depend on HTTP, serialization, messaging or persistence DTOs.

## See also

- `arch-layered` — the alternative shape; simpler, no inversion.
- `arch-solid` — Dependency Inversion Principle is the foundational idea.
- `hive-core` — authoritative Java contracts and package conventions for the Hive profile, when installed.
- `ddd-context-mapping` — an Anticorruption Layer can be expressed through an application-boundary port and adapter.
- `ddd-repository` — DDD repositories are output ports in hexagonal terms.
- `arch-fitness-functions` — enforcing the dependency direction in CI.

## References

- Alistair Cockburn, *Hexagonal Architecture* (2005 original essay).
- Tom Hombergs, *Get Your Hands Dirty on Clean Architecture* — practical hexagonal-with-Java reference.
- Robert C. Martin, *Clean Architecture* (Pearson, 2017) — the "Clean Architecture" variant.
- Davi Vieira, *Designing Hexagonal Architecture with Java* (Packt) — domain / application / framework hexagon vocabulary.
