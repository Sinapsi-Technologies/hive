# Hive

[![CI](https://github.com/Sinapsi-Technologies/hive/actions/workflows/ci.yml/badge.svg)](https://github.com/Sinapsi-Technologies/hive/actions/workflows/ci.yml)
[![License: Apache-2.0 / AGPL-3.0](https://img.shields.io/badge/License-Apache--2.0%20%2F%20AGPL--3.0-blue.svg)](#license)

Hive is an open-source **Java 21 toolkit for building hexagonal (ports & adapters) applications** with strong boundaries and low ceremony.

It is **not a framework** and it does not try to own your application. Hive gives you small contracts, optional validation, optional technical ports, lightweight adapters, **reusable ArchUnit rules**, a **CLI** that creates the boring files correctly, and a **Claude Code plugin** that drives it all.

> The default path is *simple first, modular later*.

---

## Why Hive exists

Hexagonal architecture is powerful, but teams lose time on the same questions: where does this class go? which package names are acceptable? how do I keep the domain clean and stop infrastructure from leaking inward?

Hive turns those decisions into a small, **repeatable, executable** convention:

- domain code stays framework-free;
- use cases represent application actions, exposed through input ports;
- output ports model capabilities the application needs; adapters implement them;
- validation happens before a use case receives a command;
- the architecture rules run in CI through ArchUnit — they don't live only in someone's head;
- scaffolding creates compile-ready code and updates Maven for you.

## Highlights

- **`hive` CLI** — `init`, `check`, deterministic `create` generators for application and domain building blocks, and `c4 generate`. It is idempotent, machine-readable with `--json`, and leaves `// TODO:` markers at the spots meant to be filled in.
- **Executable architecture** — `hive-archunit` ships the hexagonal rules as ArchUnit tests; `hive create archtest` wires them into your build.
- **Visible architecture** — `hive c4 generate` reads your hexagonal packages and produces styled C4 diagrams (PlantUML → SVG) plus a static HTML page, so the structure you are building is something you can actually see.
- **Framework-free core** — `hive-core` has no Spring, Jakarta, JPA, or persistence dependency.
- **Optional batteries** — technical ports (clock, id, events, file storage), default adapters, and Jakarta Bean Validation, all opt-in.
- **Claude Code plugin** — `/hive:new` to bootstrap a project and a `hive` skill to add or change a slice in natural language: the CLI builds the structure, Claude fills the logic, ArchUnit + `mvn verify` validate.

## Modules

| Module | Purpose |
| --- | --- |
| `hive-core` | Framework-free core contracts: commands, results, use cases, events, mappers, domain identifiers, aggregates, and port direction markers. |
| `hive-port` | Optional framework-free technical output ports: clock, id generation, event publishing, file storage. |
| `hive-adapter` | Lightweight default implementations of `hive-port`, useful for tests, demos, and small applications. |
| `hive-validator` | Framework-free command validation contract, `ValidationProviders`, and `AbstractCommandFactory`. |
| `jakarta-hive-validator` | Jakarta Bean Validation provider, discovered through Java SPI. |
| `hive-archunit` | Reusable ArchUnit rules for the Hive package convention and dependency direction. |
| `hive-cli` | Picocli-based `hive` command for project initialization and scaffolding. |
| `examples/todo-list-demo` | Full slice: aggregate, entities, value objects, domain events, an outbound port + adapter, validated commands, and a REST controller. |

## Coordinates

```text
group:   io.sinapsi.hive
version: 0.1.0-SNAPSHOT
java:    21
```

---

## Build

```bash
mvn clean verify        # build + tests + architecture rules for the whole monorepo
mvn clean install       # also install the snapshots into your local ~/.m2
mvn -pl hive-cli -am package   # build the CLI jar
```

The repository ships a launcher script:

```bash
./hive --help
```

To use `hive` from anywhere, alias it or symlink it onto your `PATH` — see [hive-cli/README.md](hive-cli/README.md).

## Quick start (CLI)

```bash
mkdir hive-demo && cd hive-demo
hive init                              # .hive-project, hive.yml, pom.xml, source roots
hive create id UserId
hive create vo Email --type String --not-blank
hive create entity User --id UserId --field email:Email
hive create usecase CreateUser --factory
hive create port SaveUser
hive create adapter InMemorySaveUser --port SaveUser
hive create archtest
hive check
```

`hive init` writes a default `hive.yml`:

```yaml
basePackage: com.example.app
layout: single
javaSourceRoot: src/main/java
testSourceRoot: src/test/java
```

The generated `pom.xml` is Java 21-ready and includes `hive-core`. Commands that need extra modules update Maven for you: `--factory` adds `hive-validator`; `archtest` adds `hive-archunit` and `archunit-junit5`. Existing files are never overwritten unless `--force` is passed.

### `--json` and `// TODO:` markers

Every `init`, `create`, and `check` command accepts `--json`, printing a machine-readable result instead of prose:

```bash
hive create usecase CreateUser --json
```

```json
{
  "command": "create usecase",
  "name": "CreateUser",
  "created": [
    "src/main/java/com/example/app/application/ports/in/commands/CreateUserCommand.java",
    "src/main/java/com/example/app/application/ports/in/CreateUserUseCase.java",
    "src/main/java/com/example/app/application/services/CreateUserService.java"
  ]
}
```

Generated files carry `// TODO:` markers at the points meant to be filled in (command fields, port methods, use-case logic, adapter implementations). Together with `--json`, this is built for a tool-driven workflow: a generator runs the deterministic CLI for the structure, reads the created paths, fills the marked spots, then lets `mvn verify` and the ArchUnit rules validate the result.

### Deterministic domain generation

Hive can also materialize domain primitives without inferring business semantics:

```bash
hive create vo Email --type String --not-blank --pattern ".+@.+"
hive create vo Money --field amount:BigDecimal --field currency:String --factory
hive create vo Quantity --type Integer --min 1
hive create id CustomerId
hive create entity OrderLine --id OrderLineId --field quantity:Quantity
hive create aggregate Order --id OrderId --field status:OrderStatus
hive create enum OrderStatus --value DRAFT --value CONFIRMED --value CANCELLED
hive create event OrderCreated --field orderId:OrderId --field occurredAt:Instant
hive create exception OrderAlreadyConfirmed
hive create domainservice Pricing
hive create snapshot OrderSnapshot --field id:OrderId --field status:OrderStatus
```

These generators write framework-free Java 21 domain code under `domain/valueobjects`, `domain/entities`, `domain/aggregates`, `domain/events`, and `domain/exceptions`. Identifiers and aggregates reuse the `hive-core` `AggregateId` / `AggregateRoot` contracts; value-object validation is generated as constructor checks, not Jakarta annotations.

For non-domain boilerplate, Hive also exposes small deterministic Java generators:

```bash
hive create record CustomerResponse --field id:UUID --field tags:List<String>
hive create class CustomerDto --field name:String --field email:String --getters --setters --all-args-constructor
hive create command CreateOrder --field customerId:CustomerId
hive create port LoadOrder --method "Optional<Order> load(OrderId id)"
hive create adapter OrderPersistence --port LoadOrderPort --port SaveOrderPort
```

Blueprint files can batch the same generators:

```bash
hive generate .hive/model/order.yml
hive generate --all
hive inspect config --json
```

---

## Visual architecture with C4

Hive can generate [C4](https://c4model.com/) diagrams directly from your hexagonal package structure — no extra modelling, no separate source of truth that drifts from the code:

```bash
hive c4 generate --module todo --render --site
```

Generated output:

```text
docs/architecture/
  c3-todo.puml      # C4-PlantUML component diagram (styled with the Hive palette)
  c3-todo.svg       # rendered diagram (when PlantUML is available)
  index.html        # a static, dependency-free architecture page
```

The flagship is the **C3 component diagram**, where Hive's hexagon becomes visible at a glance:

```text
Inbound Adapter → Input Port → Application Service → Domain → Output Port ← Outbound Adapter
```

The CLI understands Hive's own convention, so the diagram is deterministic and stays clean: it draws the building blocks and the relationships that matter (an inbound adapter *calls* its input ports, a service *implements* a use case and *uses* the domain and its output ports, an outbound adapter *implements* an output port) instead of a noisy complete graph.

Rendering is optional and degrades gracefully — if PlantUML is not installed, the `.puml` files are still written and the command tells you how to enable rendering. Configure it in `hive.yml`:

```yaml
c4:
  plantumlPath: plantuml      # or a path to plantuml.jar
  defaultRenderFormat: svg    # svg | png
  generateSite: true          # always build index.html
  theme: hive
```

`hive c4 generate --render --site --open` then renders the diagrams, builds the page, and opens it. The result is meant to be shared — in READMEs, pull requests, onboarding docs, and architecture reviews.

---

## Working with Claude Code

Hive ships as a **Claude Code plugin** (`plugins/hive`). The repository itself is a plugin marketplace:

```text
/plugin marketplace add Sinapsi-Technologies/hive
/plugin install hive@sinapsi
```

It provides:

- **`/hive:new [basePackage]`** — bootstraps a project (resolves the CLI, `hive init`, a starter slice, `mvn verify`) and leaves you with a green build in seconds.
- **the `hive` skill** — triggers when you ask to add or change a use case / port / adapter / module. It runs `hive create … --json`, reads the created paths, fills the `// TODO:` markers respecting the Hive conventions, and verifies with `mvn verify`.

The division of labour is the point:

```text
description → hive CLI (structure)  →  Claude (logic)  →  ArchUnit + mvn verify (guarantee)
              deterministic, 0 tokens    only where needed   the architecture can't drift
```

> `/plugin` runs in the standalone Claude Code (terminal, desktop, or IDE app). Installing the plugin doesn't require the `hive` CLI, but the CLI must be available for the commands to do their work.

---

## Package convention

```text
<base-package>
  commons
  configurations

  application
    ports
      in
        commands
      out
    services

  domain
    valueobjects
    aggregates
    events
    entities
    services
    snapshots
    exceptions

  infrastructure
    configs
    adapters
      in
      out
```

- `application.ports.in` — input ports and use case contracts. Small commands may live here; larger ones under `application.ports.in.commands`.
- `application.ports.out` — output ports: interfaces describing capabilities the application needs.
- `application.services` — use case implementations.
- `infrastructure.adapters.in` — inbound adapters (controllers, listeners, schedulers, CLI handlers).
- `infrastructure.adapters.out` — outbound adapters (persistence, messaging, file, HTTP, external services).
- `configurations` / `infrastructure.configs` — composition roots that wire services, ports, and adapters.

### Optional modular layout

`hive create module <name>` generates the same internal convention under `<base-package>.modules.<name>`, without requiring Spring Modulith or any module framework. Start with one context; split into modules when the boundaries become valuable.

---

## Core

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>hive-core</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Package root `io.sinapsi.hive.core`. Main contracts:

```text
command.Command
domain.Identifier  domain.AggregateId  domain.AggregateRoot
event.Event  event.DomainEvent  event.IntegrationEvent  event.EventHandler
mapper.Mapper  mapper.BiMapper
port.Port  port.InputPort  port.OutputPort
result.Result  result.Unit
usecase.UseCase
```

`hive-core` has no Spring, Jakarta, JPA, validation runtime, or infrastructure dependency. Only the direction markers (`Port`, `InputPort`, `OutputPort`) live in core; technical ports live in `hive-port`.

## Ports and adapters

`hive-port` provides optional technical output ports — `ClockPort`, `IdGeneratorPort<T>`, `PublishEventPort<E>`, `FileStoragePort` — and `hive-adapter` ships default implementations:

```text
ClockPort         -> SystemClockAdapter
IdGeneratorPort   -> UuidGeneratorAdapter
PublishEventPort  -> InMemoryEventPublisherAdapter
FileStoragePort   -> LocalFileStorageAdapter
```

Use them for tests, demos, and small apps; replace them with project-specific infrastructure when needed.

## Validation

`hive-validator` keeps validation out of commands and use cases. The recommended flow:

1. a request enters through an inbound adapter;
2. a command factory builds the command;
3. the factory validates it;
4. the use case receives a valid command.

```java
public final class CreateUserCommandFactory extends AbstractCommandFactory<CreateUserCommand> {
    public CreateUserCommand create(String name, String email) {
        return validate(new CreateUserCommand(name, email));
    }
}
```

`AbstractCommandFactory` validates through `ValidationProviders.get().commandValidator()`. With no provider on the classpath it uses a no-op validator. Add `jakarta-hive-validator` (discovered via Java SPI) for Jakarta Bean Validation — keep using `AbstractCommandFactory`; don't instantiate the provider directly.

## ArchUnit

`hive-archunit` exposes the rules through `io.sinapsi.hive.archunit.HexagonalRules`. `hive create archtest` generates a test bound to your base package:

```java
@AnalyzeClasses(packages = "com.example.app")
class ArchitectureTest {
    @ArchTest
    static final ArchRule hiveBaseRules = HexagonalRules.allBaseRules("com.example.app");
}
```

Base rules:

- `domainShouldNotDependOnFrameworks`
- `domainShouldNotDependOnApplication`
- `domainShouldNotDependOnInfrastructure`
- `applicationShouldNotDependOnInfrastructure`
- `applicationShouldNotDependOnInboundAdapters`
- `applicationShouldNotDependOnOutboundAdapters`
- `inboundAdaptersShouldNotDependOnOutboundAdapters`
- `mappersShouldNotBeInDomain`
- `commandsShouldResideInAllowedPlaces`
- `noStandaloneApplicationCommandPackageShouldBeUsed`
- `useCasesShouldResideInsideInputPorts`
- `outputPortsShouldResideInsideOutputPortsPackage`
- `servicesShouldNotResideInDomain`
- `noCyclesBetweenMainLayers`
- `allBaseRules`

---

## CLI reference

```bash
hive init [--readme] [--force] [--json]
hive check [--json]
hive create usecase <Name> [--factory] [--force] [--json]
hive create usecase <module> <Name> [--factory] [--force] [--json]
hive create port <Name> [--force] [--json]
hive create adapter <Name> --port <Port> [--force] [--json]
hive create vo <Name> [--type <Type>] [--not-null] [--not-blank] [--min <n>] [--max <n>] [--min-length <n>] [--max-length <n>] [--pattern <regex>] [--force] [--json]
hive create id <Name> [--type UUID|Long|String] [--force] [--json]
hive create entity <Name> --id <IdType> [--field name:Type]... [--force] [--json]
hive create aggregate <Name> --id <IdType> [--field name:Type]... [--force] [--json]
hive create enum <Name> --value <VALUE>... [--force] [--json]
hive create event <Name> [--field name:Type]... [--force] [--json]
hive create exception <Name> [--message <message>] [--force] [--json]
hive create domainservice <Name> [--force] [--json]
hive create snapshot <Name> [--field name:Type]... [--force] [--json]
hive create record <Name> [--field name:Type]... [--force] [--json]
hive create class <Name> [--field name:Type]... [--getters] [--setters] [--constructor] [--all-args-constructor] [--force] [--json]
hive create command <Name> [--field name:Type]... [--force] [--json]
hive create module <name> [--json]
hive create archtest [--force] [--json]
hive generate <file> [--force] [--json]
hive generate --all [--force] [--json]
hive inspect config|model|generated [--json]
hive c4 generate [--module <name>] [--level <level>] [--output <path>] [--format <format>] [--render [svg|png]] [--site] [--open] [--force] [--json]
```

The CLI locates the project by walking upward until it finds `.hive-project`, reads `hive.yml`, writes deterministic files, refuses to overwrite without `--force`, and updates Maven for generated code. See [hive-cli/README.md](hive-cli/README.md) for the full guide.

## Examples

```bash
mvn clean verify   # builds and tests every module, including the examples
```

- **`todo-list-demo`** — a complete slice: a `TodoList` aggregate with entities, value objects, and domain events; an outbound repository port and adapter; validated commands behind a REST controller; the Hive clock and event-publishing ports wired to their default adapters. Scaffolded with the `hive` CLI, then implemented and enforced by the ArchUnit rules. See [examples/todo-list-demo/README.md](examples/todo-list-demo/README.md).

> ⚠️ **`.gitignore` gotcha.** A common IntelliJ rule, `out/` (no leading slash), ignores **every** directory named `out` at any depth — including the hexagonal `application/ports/out/` and `infrastructure/adapters/out/` packages. New, untracked files there silently get dropped by `git clean` or the IDE. Anchor the rule to the repository root instead: `/out/`. Hive's own `.gitignore` is already fixed this way.

---

## Releasing

A push of a `v*` tag runs [`.github/workflows/release.yml`](.github/workflows/release.yml), which:

1. derives the release version from the tag (`v0.1.0` → `0.1.0`);
2. builds, GPG-signs, and publishes the six libraries (+ parent POM) to **Maven Central** via the Central Publishing Plugin (the `release` Maven profile);
3. attaches the executable `hive-cli.jar` to a **GitHub Release**.

The `io.sinapsi` namespace is verified on the Central Portal with a DNS TXT record on `sinapsi.io`. Examples and the CLI are excluded from Central; the CLI is distributed as a release asset.

## Design rules

- Strong boundaries, low ceremony. Core stays framework-free. Modular support is optional.
- A use case represents one action; an input port exposes one use case operation.
- Commands are immutable input models. They may declare validation annotations and helper conversions, but must not validate themselves, create aggregates, or hold business logic.
- Output ports model capabilities, not technologies. Adapters implement ports.
- Domain code must not depend on Spring, JPA, HTTP, persistence, messaging, validation frameworks, or infrastructure adapters.

## Roadmap

- publish to Maven Central and cut the first release
- richer CLI templates and configurable dependency versions
- optional Spring Boot starter and Spring adapters (events, clock, transactions)
- more adapter implementations
- migration helpers for existing projects

## License

Hive uses module-level licensing:

- `hive-cli` is licensed under AGPL-3.0 — see [hive-cli/LICENSE](hive-cli/LICENSE).
- All other modules are licensed under Apache License 2.0 — see [LICENSE](LICENSE).

Generated output from `hive-cli` is not automatically subject to AGPL-3.0 merely
because it was produced by the CLI. See [hive-cli/LICENSE](hive-cli/LICENSE) for
the generated-output notice.
