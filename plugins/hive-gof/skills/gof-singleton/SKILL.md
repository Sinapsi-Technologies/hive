---
name: gof-singleton
description: >-
  Use when discussing single-instance objects — phrases like "Singleton pattern", "only one instance", "global access point", "make this a singleton", "thread-safe singleton". Defines the classical Singleton AND explains why it has aged poorly: most legitimate uses are better served by language-level constructs (Kotlin's `object`, Spring beans, dependency injection) and Singleton is widely considered an anti-pattern in 2026 codebases.
---

## One-line summary

The Singleton ensures a class has only one instance and provides a global point of access to it — a pattern that solved real problems in 1994 and has aged into one of the most warned-against patterns in modern code.

## When to use this skill

- Reviewing or removing existing Singleton usage in legacy code.
- Discussing whether something "should be a singleton" — usually the answer is "no, but here's why and what to do instead".
- Implementing the pattern when you have a *genuine* reason: an external resource where multiple instances would be incorrect (a hardware device interface, a single connection pool that's owned by the application, a logging sink).

## When NOT to use this skill

- "I just want one of these" — that is not a justification. Use `object` (Kotlin), a Spring `@Bean`, or any DI container.
- Caching — use a real cache.
- Configuration — use injected configuration.
- Logging — use the standard logging facade.
- Anything that needs to be tested — Singletons fight tests.

## Core content

Classical Singleton:
1. Private constructor.
2. Static `getInstance()` method that returns the one instance.
3. Lazy or eager initialization.
4. Thread-safety considerations (double-checked locking, holder-class idiom, eager init).

**Why it has aged poorly:**

- **Globalness.** A Singleton is a global variable in a tuxedo. It carries every problem global state has: hidden dependencies, hard-to-test code, lifecycle nightmares in tests, ordering bugs.
- **Identity confusion.** "Only one instance" is rarely actually true — once you have multiple classloaders, multiple JVMs, multi-tenancy, or test isolation, the "one" becomes "many" and the contract breaks silently.
- **Replaceable by language features.** Kotlin's `object` declaration gives you a thread-safe single instance with no boilerplate. DI containers give you scoped beans with explicit lifecycle.

**When it's actually justified.** A genuine external-resource adapter where two instances would conflict — but even then, prefer to scope the single instance to a DI container, not the JVM globally.

## Decision heuristics

- Default position: do not introduce a Singleton in new code. If you think you need one, ask: is this really about *identity*, or is this about *convenient access*?
- If "convenient access", use DI.
- If "true single instance for a real reason", scope it explicitly (Kotlin `object`, Spring `@Bean(SCOPE_SINGLETON)` inside a context, not statically global).

## Anti-patterns

- Singleton-as-service-locator — the Singleton holds a registry of other Singletons. Service locator is its own anti-pattern; doubling down with Singleton compounds it.
- Singleton-as-global-state — `AppContext.getInstance().getCurrentUser()`. Hidden dependency, tests cry.
- Trying to "unit test the Singleton" by adding a reset method — the moment you need a reset method, the abstraction is wrong.
- Thread-unsafe lazy init that "usually works" — race conditions on first access are real and rare and hellish to debug.

## See also

- `gof-factory` — Singleton is often combined with a factory; the factory's static method returns the single instance.
- `gof-kotlin-idioms` — Kotlin's `object` keyword and DI containers cover virtually all legitimate Singleton use cases; the adapter shows when (rarely) to fall back to classical Singleton.
- `gof-anti-patterns`-style flagging applies — treat new-code Singletons as a smell first, justify them second.

## References

- *Head First Design Patterns* (2nd ed), Ch. 5 ("One-of-a-Kind Objects: the Singleton Pattern") — the canonical exposition, including its honest treatment of the pattern's problems. Worth reading specifically for the "Confessions of a Singleton" section.
