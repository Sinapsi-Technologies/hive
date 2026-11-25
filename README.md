# Hive

[![CI](https://github.com/Sinapsi-Technologies/hive/actions/workflows/ci.yml/badge.svg)](https://github.com/Sinapsi-Technologies/hive/actions/workflows/ci.yml)
[![License: Apache-2.0 / AGPL-3.0](https://img.shields.io/badge/License-Apache--2.0%20%2F%20AGPL--3.0-blue.svg)](#license)

Hive is an open-source **Java 21 toolkit for building hexagonal (ports & adapters) applications** with strong boundaries and low ceremony.

It is **not a framework** and it does not try to own your application. Hive gives you small contracts, optional validation, optional technical ports, lightweight adapters, **reusable ArchUnit rules**, and an **MCP server** that exposes deterministic project operations to coding agents.

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

- **Hive MCP server** — project selection, indexing, domain-model validation, deterministic generation, dependency inspection, OSV scanning, and architecture verification.
- **Executable architecture** — `hive-archunit` ships the hexagonal rules as ArchUnit tests; MCP generation can add the architecture test scaffold.
- **Visible architecture** — the MCP project index exposes package, type, call, and dependency relations that can be rendered as focused graph views.
- **Framework-free core** — `hive-core` has no Spring, Jakarta, JPA, or persistence dependency.
- **Optional batteries** — technical ports (clock, id, events, file storage), default adapters, and Jakarta Bean Validation, all opt-in.
- **Claude Code plugin** — an MCP-first `hive` skill that orchestrates project analysis and generation through `mcp__hive__*` tools.

## Modules

| Module | Purpose |
| --- | --- |
| `hive-core` | Framework-free core contracts: commands, results, use cases, events, mappers, domain identifiers, aggregates, and port direction markers. |
| `hive-port` | Optional framework-free technical output ports: clock, id generation, event publishing, file storage. |
| `hive-adapter` | Lightweight default implementations of `hive-port`, useful for tests, demos, and small applications. |
| `hive-validator` | Framework-free command validation contract, `ValidationProviders`, and `AbstractCommandFactory`. |
| `jakarta-hive-validator` | Jakarta Bean Validation provider, discovered through Java SPI. |
| `hive-archunit` | Reusable ArchUnit rules for the Hive package convention and dependency direction. |
| `hive-mcp` | Spring Boot and Spring AI MCP server for analysis, generation, dependencies, and validation. |
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
mvn spotless:check      # verify Java formatting
mvn spotless:apply      # apply the configured Java formatter
```

Java sources are formatted with Spotless and Google Java Format (AOSP style).
Formatting is intentionally explicit: normal builds do not rewrite source files.

## Quick start (MCP)

```bash
mvn -q -pl hive-mcp spring-boot:run \\
  -Dspring-boot.run.arguments="--server.port=9090 --hive.project-root=/path/to/project"
```

Connect the MCP client to `http://127.0.0.1:9090/mcp`, then use
`hive_select_project`, `hive_init`, and the specialized `hive_create_*` tools.

`hive_init` writes a default `hive.yml`:

```yaml
basePackage: com.example.app
layout: single
javaSourceRoot: src/main/java
testSourceRoot: src/test/java
```

The generated `pom.xml` is Java 21-ready and includes `hive-core`. MCP generation tools return exact paths and do not overwrite existing files unless `force=true` is requested.

### Structured MCP results and `// LLM:` guidance

Every Hive MCP tool returns structured data instead of command-line prose:

```text
hive_create_usecase
```

```json
{
  "created": [
    "src/main/java/com/example/app/application/ports/in/CreateUserUseCase.java",
    "src/main/java/com/example/app/application/services/CreateUserService.java"
  ]
}
```

Generated files may carry `// TODO:` markers. When domain guidance is known,
`implementationGuidance` emits `// LLM:` comments for the next implementation
pass. The MCP server creates the deterministic structure; the agent fills the
business logic and verifies it with tests and ArchUnit.

### Deterministic domain generation

Hive MCP can also materialize domain primitives without inferring business semantics:

```text
hive_create_value_object
hive_create_id
hive_create_entity
hive_create_aggregate
hive_create_enum
hive_create_event
hive_create_exception
hive_create_domain_service
hive_create_snapshot
```

These generators write framework-free Java 21 domain code under `domain/valueobjects`, `domain/entities`, `domain/aggregates`, `domain/events`, and `domain/exceptions`. Identifiers and aggregates reuse the `hive-core` `AggregateId` / `AggregateRoot` contracts; value-object validation is generated as constructor checks, not Jakarta annotations.

For non-domain boilerplate, Hive also exposes deterministic MCP generators:

```text
hive_create_record
hive_create_class
hive_create_command
hive_create_port
hive_create_adapter
```

Structured domain models can batch the same generation decisions:

```text
hive_validate_domain_model
hive_save_domain_model
hive_create_*
hive_inspect
```

The MCP domain-model schema is the authoritative contract for requirements-driven generation.

---

## Visual architecture with the project graph

Hive MCP indexes the source graph and exposes targeted relations for visualisation:

```text
hive_analyze_project
hive_get_symbol_relations
hive_get_used_by
hive_get_source_context
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

The Hive project index understands the package convention, so an agent can draw the relationships that matter (an inbound adapter *calls* its input ports, a service *implements* a use case and *uses* the domain and its output ports, an outbound adapter *implements* an output port) instead of a noisy complete graph.

Rendering is optional and degrades gracefully — if PlantUML is not installed, the `.puml` files are still written and the command tells you how to enable rendering. Configure it in `hive.yml`:

```yaml
c4:
  plantumlPath: plantuml      # or a path to plantuml.jar
  defaultRenderFormat: svg    # svg | png
  generateSite: true          # always build index.html
  theme: hive
```

The indexed graph can be rendered as focused Mermaid, Graphviz, or C4-like views for READMEs, pull requests, onboarding docs, and architecture reviews.

---

## Working with Claude Code

Hive ships as a **Claude Code plugin** (`plugins/hive`). The repository itself is a plugin marketplace:

```text
/plugin marketplace add Sinapsi-Technologies/hive
/plugin install hive@sinapsi
```

It provides:

- **the `hive` skill** — uses the connected MCP server to select a project, analyze requirements, generate a slice, and verify the result.

The division of labour is the point:

```text
requirements → Hive MCP (structure) → Claude (logic) → ArchUnit + mvn verify
               deterministic tools     only where needed
```

> `/plugin` runs in the standalone Claude Code (terminal, desktop, or IDE app). The Hive MCP server must be connected before using the skill.

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
- `infrastructure.adapters.in` — inbound adapters (controllers, listeners, schedulers, command-line handlers).
- `infrastructure.adapters.out` — outbound adapters (persistence, messaging, file, HTTP, external services).
- `configurations` / `infrastructure.configs` — composition roots that wire services, ports, and adapters.

### Optional modular layout

`hive_create_module` generates the same internal convention under `<base-package>.modules.<name>`, without requiring Spring Modulith or any module framework. Start with one context; split into modules when the boundaries become valuable.

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
public interface CreateUserUseCase
        extends UseCase<CreateUserUseCase.CreateUserCommand, Result<Unit>> {

    record CreateUserCommand(String name, String email) implements Command {}

    final class CommandFactory extends AbstractCommandFactory<CreateUserCommand> {
        private CommandFactory() {}

        public static Result<CreateUserCommand> create(String name, String email) {
            return new CommandFactory().validate(new CreateUserCommand(name, email));
        }
    }
}
```

Generated commands are immutable records nested in the use case, with an optional
`CommandFactory` next to them. Factories return `Result<Command>` and convert expected
validation violations into `Failure` with `VALIDATION_FAILED` and all field details.

```java
var result = CreateUserUseCase.CommandFactory.create(name, email)
        .flatMap(createUser::handle);
```

Invalid input skips the use case. Failure transformations preserve the complete
`ResultError(code, message, details)`, and `fold` receives that error as one argument.

`AbstractCommandFactory` validates through `ValidationProviders.get().commandValidator()`. With no provider on the classpath it uses a no-op validator. Add `jakarta-hive-validator` (discovered via Java SPI) for Jakarta Bean Validation — keep using `AbstractCommandFactory`; don't instantiate the provider directly.

## ArchUnit

`hive-archunit` exposes the rules through `io.sinapsi.hive.archunit.HexagonalRules`. The MCP architecture-test generator creates a test bound to your base package:

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

## Examples

```bash
mvn clean verify   # builds and tests every module, including the examples
```

- **`todo-list-demo`** — a complete slice: a `TodoList` aggregate with entities, value objects, and domain events; an outbound repository port and adapter; validated commands behind a REST controller; the Hive clock and event-publishing ports wired to their default adapters. Generated and validated through Hive MCP. See [examples/todo-list-demo/README.md](examples/todo-list-demo/README.md).

> ⚠️ **`.gitignore` gotcha.** A common IntelliJ rule, `out/` (no leading slash), ignores **every** directory named `out` at any depth — including the hexagonal `application/ports/out/` and `infrastructure/adapters/out/` packages. New, untracked files there silently get dropped by `git clean` or the IDE. Anchor the rule to the repository root instead: `/out/`. Hive's own `.gitignore` is already fixed this way.

---

## Releasing

A push of a release tag such as `v0.1.0` runs [`.github/workflows/release.yml`](.github/workflows/release.yml). It sets every module to the tag version, builds and tests the reactor, attaches sources and Javadoc, GPG-signs the artifacts, and publishes to **Maven Central**. The workflow waits for Central to confirm publication.

Published coordinates use `io.sinapsi.hive`: `hive-toolkit` (parent POM), `hive-core`, `hive-archunit`, `hive-port`, `hive-adapter`, `hive-validator`, and `jakarta-hive-validator`. The example and executable `hive-mcp` server are built and tested but excluded from the Central bundle.

Before the first release:

1. Verify ownership of the `io.sinapsi` namespace in the [Central Portal](https://central.sonatype.com) using the DNS TXT record for `sinapsi.io`, and grant the publishing account access to it.
2. Generate a [Central Portal user token](https://central.sonatype.org/publish/generate-portal-token/).
3. Configure these GitHub repository secrets:

| Secret | Value |
| --- | --- |
| `MAVEN_CENTRAL_USERNAME` | User token username (not the login email) |
| `MAVEN_CENTRAL_PASSWORD` | User token password |
| `GPG_PRIVATE_KEY` | ASCII-armored signing private key (`gpg --armor --export-secret-keys <key-id>`) |
| `MAVEN_GPG_PASSPHRASE` | Signing key passphrase |

Publish the corresponding GPG public key to a public keyserver, as described in the [Central signing requirements](https://central.sonatype.org/publish/requirements/gpg/).

To release the committed project, push a new, unused version tag:

```bash
git tag v0.1.0
git push origin v0.1.0
```

For a local release, import the signing key into GPG and export `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, and `MAVEN_GPG_PASSPHRASE` in your shell. Use the committed settings template, which reads the token from environment variables:

```bash
mvn -B -ntp org.codehaus.mojo:versions-maven-plugin:2.18.0:set \
  -DnewVersion=0.1.0 -DprocessAllModules=true -DgenerateBackupPoms=false
mvn -B -ntp -s .mvn/central-settings.xml -Prelease clean deploy
```

The `release` profile rejects SNAPSHOT project versions and dependencies. To inspect the signed bundle locally without uploading, use the same deploy command with `-DskipUpload=true`; the ZIP is written under `target/central-publishing/`. This still requires a non-SNAPSHOT version and a GPG signing key. Published versions on Central cannot be overwritten.

## Design rules

- Strong boundaries, low ceremony. Core stays framework-free. Modular support is optional.
- A use case represents one action; an input port exposes one use case operation.
- Commands are immutable input models. They may declare validation annotations and helper conversions, but must not validate themselves, create aggregates, or hold business logic.
- Output ports model capabilities, not technologies. Adapters implement ports.
- Domain code must not depend on Spring, JPA, HTTP, persistence, messaging, validation frameworks, or infrastructure adapters.

## Roadmap

- publish to Maven Central and cut the first release
- richer MCP templates and configurable dependency versions
- optional Spring Boot starter and Spring adapters (events, clock, transactions)
- more adapter implementations
- migration helpers for existing projects

## License

Hive uses module-level licensing:

- All modules are licensed under Apache License 2.0 — see [LICENSE](LICENSE).
