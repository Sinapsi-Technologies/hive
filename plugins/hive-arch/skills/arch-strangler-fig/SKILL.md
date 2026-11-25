---
name: arch-strangler-fig
description: Use when migrating a legacy system incrementally — phrases like "strangler fig", "strangler pattern", "incremental migration", "monolith to microservices", "rewrite without big bang", "replace legacy in place", "facade in front of the legacy system", "anticorruption layer". Defines Fowler's Strangler Fig as the migration discipline of gradually replacing legacy functionality behind a stable facade, never doing a big-bang rewrite.
---

## One-line summary

Wrap the legacy system in a facade. Route new functionality and gradually-rewritten existing functionality to new services behind that facade. When all functionality has migrated, the legacy system has been "strangled" and can be retired.

## When to use this skill

- Migrating a monolith to microservices over months or years.
- Replacing a legacy system that's too critical to take offline for a rewrite.
- Decomposing a system whose internals are poorly understood — you migrate one behaviour at a time and rebuild understanding as you go.
- Any time someone proposes a "big bang rewrite". This is the safer alternative.

## When NOT to use this skill

- The legacy system is small enough that a direct rewrite is genuinely shorter and lower risk.
- Functionality is so tangled that no clean facade can be drawn — first invest in a refactor that exposes seams.
- Strangulation is being used as cover for an indefinite migration that nobody intends to finish — the legacy never goes away.

## Core content

The discipline:

1. **Pick a seam.** Identify a piece of functionality that can be intercepted at a clear boundary (HTTP endpoint, message handler, scheduled job).
2. **Build the facade.** A proxy / gateway / dispatcher routes traffic for that functionality. Initially, all traffic goes to the legacy.
3. **Build the replacement.** A new service implements the same external contract for that functionality.
4. **Switch a percentage.** Route 1% / 10% / 100% to the new service. Compare outputs (shadow traffic, parallel run).
5. **Decommission.** Once all traffic is on the new service for that functionality, delete the legacy code path.
6. **Repeat.** Move to the next piece of functionality.

**Pairs with Anticorruption Layer (ACL).** The legacy system's model is almost always different from the new service's. The ACL translates between them so the new service doesn't inherit the legacy's data shapes or vocabulary. See `ddd-context-mapping` for the ACL pattern.

**Crucial: each migration step must be releasable independently.** If a migration step requires "rewrite for 6 months, then switch", you've built a parallel system, not a strangler. Real strangulation ships small migrated slices weekly.

**Common facade implementations.**

- **API gateway** (Kong, Istio, custom proxy) routes by path / header.
- **Database view** that abstracts whether reads come from the old or new store.
- **Event bridge** for event-shaped systems — old and new services consume the same events, route on a feature flag.
- **Custom dispatcher** in code for embedded legacy (a coordinator class that decides "old or new path").

**Schemas during migration.** The new service typically owns its data. The legacy still has the canonical store. Strategies:

- **Read-from-legacy, write-to-both.** The new service reads from the legacy for now; writes go to both stores. Once the new store is canonical, flip the read direction.
- **Replicate from legacy to new.** Change data capture (CDC) pushes legacy changes into the new store. New service reads only from the new store.

## Decision heuristics

- Always start by picking the *simplest* functional slice — proves the migration mechanism works without committing to the riskiest bits first.
- Parallel-run new and old for a window before cutting over. Compare results; investigate divergences.
- Have a written kill-the-legacy date and milestone plan. Without it, the migration runs forever and you maintain both systems for 5 years.
- Decommissioning is part of the work, not an afterthought. Until the legacy code is *deleted*, the migration isn't done.

## Anti-patterns

- **Big-bang rewrite disguised as strangulation.** "We're using the strangler pattern" but the plan is "rewrite for 18 months in parallel, then switch". That's a rewrite with extra branding.
- **Indefinite strangulation.** The new service handles 30% of traffic for years; nobody completes the migration; both systems are maintained forever.
- **Strangling without an ACL.** The new service inherits the legacy's data shapes verbatim. The legacy's bad design propagates into the replacement.
- **Slicing by technical layer.** Migrating "the database" or "the API" without migrating coherent functionality — produces a half-migrated mess.

## See also

- `ddd-context-mapping` — Anticorruption Layer is the canonical companion pattern.
- `arch-microservices` — strangulation is the standard path from monolith to microservices.
- `arch-event-driven` — bridges between old and new often happen via events.
- `arch-fitness-functions` — automate "is the legacy code path actually used" checks.

## References

- Martin Fowler, "StranglerFigApplication" (2004 essay) — original articulation.
- Sam Newman, *Monolith to Microservices* (O'Reilly) — full chapter on strangler-pattern migrations.
