---
name: arch-circuit-breaker
description: Use when protecting a service from a degraded or failing dependency — phrases like "circuit breaker", "bulkhead", "retry policy", "exponential backoff", "fail fast", "fallback", "cascade failure", "open/closed/half-open state", "resilience4j", "Polly", "Hystrix". Defines the circuit breaker pattern and the related failure-isolation toolbox (bulkhead, retry-with-backoff, timeout, fallback).
---

## One-line summary

Stop calling a failing dependency for a while so it can recover and your service doesn't drown in timeouts. Combine with bulkheads (isolate resources per dependency), retries with backoff (handle transient blips), and timeouts (bound the failure window) for a complete resilience story.

## When to use this skill

- Service-to-service calls (REST, gRPC, message broker) where the downstream can fail or slow down.
- External dependencies (databases, third-party APIs, payment providers) that can degrade.
- Any system where one slow dependency can cascade: threads block on it, the thread pool fills, the service stops accepting requests.
- Anywhere you'd otherwise write "I'll just retry until it succeeds" — that's the cascade you're about to cause.

## When NOT to use this skill

- All dependencies are in-process — no network failure mode to protect against.
- A simple synchronous failure that should just propagate to the caller — circuit breaker hides the failure, which isn't always desirable.
- Total volume is low and a slow call won't cascade — the operational overhead exceeds the benefit.

## Core content

**Circuit breaker.** A state machine wrapping a dependency call:

- **Closed** — calls pass through normally. Failures are counted in a sliding window. When failures exceed the threshold, transition to *open*.
- **Open** — calls fail fast without touching the dependency. After a cool-down, transition to *half-open*.
- **Half-open** — a small number of probe calls pass through. If they succeed, transition to *closed*. If they fail, back to *open*.

The breaker prevents cascading failures by absorbing the failure locally. Callers fail fast (return a fallback or an error) instead of piling up on slow calls.

**Bulkhead.** Isolate resources per dependency so one bad neighbour can't sink the rest. Examples: separate thread pool per downstream service, separate connection pool per database. Origin: ship hulls partitioned so one breach doesn't sink the whole ship.

**Timeout.** Every remote call must have a timeout. Without one, a slow dependency exhausts the calling thread/coroutine and cascades. Set the timeout to a *finite, conservative* value — better to fail fast and retry than to wait forever.

**Retry with backoff and jitter.**

- **Retry**: try again on transient failure.
- **Backoff**: wait between retries (exponential is standard: 100ms, 200ms, 400ms, 800ms…).
- **Jitter**: randomize the wait so a thundering herd of retries doesn't synchronize and pummel a recovering dependency.

Retries should only fire on transient errors (timeouts, 5xx, connection refused). Never retry on 4xx — that's a permanent client error.

**Fallback.** When the dependency is unavailable, return something useful instead of an error. A cached value, a default, "service degraded; partial data". Fallback policy is a *product decision*, not a default — pick deliberately.

**Libraries.** Don't write this yourself. JVM: Resilience4j. .NET: Polly. Node: opossum. Each composes the patterns above into a configurable resilience pipeline.

## Decision heuristics

- Every remote call: timeout first, then circuit breaker, then bulkhead. Add retry where transients are expected.
- Failure threshold: low enough to react fast, high enough to absorb random blips. Start at 50% failure over 20 calls; tune from there.
- Cool-down: tens of seconds for a healthy dependency that occasionally hiccups; minutes for one that needs human intervention.
- Always jitter the retry. Synchronized retries cause thundering herd.
- Fallbacks are a *product* decision. The team's PM should know what "degraded mode" looks like.

## Anti-patterns

- **No timeout.** Hanging calls drown the thread pool. The single most common cause of cascading failure.
- **Retry without backoff.** Doubles the load on a struggling dependency immediately.
- **Retrying on 4xx.** Permanent errors retried into permanent thrashing.
- **Circuit-breaker thresholds tuned for the wrong axis.** Failure rate is per-window; raw failure count without window context is meaningless.
- **Circuit breaker with no fallback.** Open state means "fail fast", but if the caller has no plan B, you've just relabeled the failure.
- **Hand-rolled circuit breaker.** Resilience4j / Polly have edge cases worked out over years. Don't.

## See also

- `arch-microservices` — circuit breakers are essential between services.
- `arch-saga` — saga steps calling other services need this protection.
- `arch-event-driven` — different failure model; the breaker pattern is for synchronous calls.
- `kp-structured-concurrency` — Kotlin's `withTimeout` covers the timeout part for in-process coroutine code.

## References

- Michael Nygard, *Release It!* (Pragmatic Bookshelf) — the canonical resilience-patterns book; circuit breaker introduced here.
- Richards & Ford, *Fundamentals of Software Architecture*, microservices and distributed-architecture chapters.
- Resilience4j documentation — concrete implementation reference.
