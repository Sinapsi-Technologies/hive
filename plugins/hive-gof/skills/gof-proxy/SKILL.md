---
name: gof-proxy
description: Use when controlling access to an object — phrases like "Proxy pattern", "lazy initialization", "remote proxy", "virtual proxy", "protection proxy", "the client should not know they're talking to a stand-in", "wrap an expensive object with a placeholder". Defines Proxy as a stand-in for another object that controls access to it (creation timing, location, permission).
---

## One-line summary

Provide a surrogate or placeholder for another object to control access to it — without the client knowing it's not talking directly to the real object.

## When to use this skill

- The real object is expensive to create and should be deferred until actually used (**Virtual Proxy** — image loading, lazy initialization of heavy resources).
- The real object lives in a different process / on a different machine and the client interface should look local (**Remote Proxy** — RMI, gRPC stubs, RPC clients).
- Access to the real object should be checked against permissions or rate limits (**Protection Proxy**).
- The real object is hard to mock for tests, and a stand-in makes testing easier.

## When NOT to use this skill

- You want to *add behavior* to the wrapped object — that's `gof-decorator`, not Proxy. The structural shape is the same but the intent differs.
- You want to *change the interface* — that's `gof-adapter`.
- Lazy initialization of a simple field — most languages have lazy delegates (`by lazy` in Kotlin, `lazy` in C#) that handle it without a proxy class.

## Core content

A Proxy implements the same interface as the real object it stands in for. The client holds a reference to the proxy and calls methods on it; the proxy decides when and whether to forward the call to the real subject.

Three common kinds:

**Virtual Proxy.** Creates the real object on first use. Client gets immediate response for "cheap" operations (e.g., `getId()` returns a stored ID); the expensive object materializes only when `getData()` is actually called.

**Remote Proxy.** Local stand-in for a remote object. The proxy serializes arguments, sends them over the wire, deserializes the response, and returns it. The client writes code as if the remote object were local. (Caveat: pretending remote calls are local is also a known anti-pattern when callers forget about network failure, latency, and partial results — see "the eight fallacies of distributed computing".)

**Protection Proxy.** Wraps the real object with permission checks. Each method on the proxy first verifies the caller's permission, then forwards (or denies).

## Decision heuristics

- Virtual Proxy: justified when creation cost is significant *and* most callers won't actually need the real object. Otherwise just create it.
- Remote Proxy: rarely written by hand in 2026 — gRPC, OpenAPI, GraphQL all generate the proxy code. Understanding the pattern matters; hand-rolling it usually doesn't.
- Protection Proxy: be careful — security wrappers that can be bypassed by going around them aren't really protection. Enforce permissions at the boundary (controller / API layer), not via an in-process proxy.

## Anti-patterns

- Proxy that adds behavior — that's Decorator wearing a Proxy hat.
- "Smart Proxy" that grows behavior over time — at some point it isn't a proxy anymore; refactor.
- Hiding remote-call semantics so well that callers don't know they're remote — leads to N+1 network calls and surprised production incidents.

## See also

- `gof-decorator` — same structural shape, different intent (Decorator adds responsibility; Proxy controls access).
- `gof-adapter` — when changing the interface, not just controlling access.
- `gof-kotlin-idioms` — `by lazy` covers Virtual Proxy for fields; framework-generated stubs (gRPC, Retrofit) cover Remote Proxy. Rolling your own is rare and usually wrong.

## References

- *Head First Design Patterns* (2nd ed), Ch. 11 ("Controlling Object Access: the Proxy Pattern") — canonical chapter, includes Virtual, Remote (via Java RMI), and Protection Proxy variants.
