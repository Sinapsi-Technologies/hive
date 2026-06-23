# Hive

Hive is an open-source Java 21 toolkit for building hexagonal applications with strong boundaries and low ceremony.

It is not a framework and it does not try to own your application. Hive gives you small contracts, optional validation, optional technical ports, lightweight adapters, reusable ArchUnit rules, and a CLI that creates the boring files correctly.

The default path is simple first, modular later.

## Why Hive Exists

Hexagonal architecture is powerful, but many teams lose time deciding where every class should live, which package names are acceptable, how to keep the domain clean, and how to stop infrastructure from leaking inward.

Hive turns those decisions into a small, repeatable convention:

- domain code stays framework-free
- use cases represent application actions
- input ports expose use case contracts
- output ports model capabilities needed by the application
- adapters implement ports
- validation happens before a use case receives a command
- architecture rules are executable through ArchUnit
- scaffolding creates compile-ready code and Maven dependencies

Hive helps teams start clean without forcing a heavy platform.

## Modules

| Module | Purpose |
| --- | --- |
| `hive-core` | Framework-free core contracts: commands, results, use cases, events, mappers, domain identifiers, aggregates, and port direction markers. |
| `hive-port` | Optional framework-free technical output ports such as clock, ID generation, event publishing, and file storage. |
| `hive-adapter` | Lightweight default implementations for `hive-port`, useful for tests, demos, local development, and small applications. |
| `hive-validator` | Framework-free command validation contract, `ValidationProviders`, and `AbstractCommandFactory`. |
| `jakarta-hive-validator` | Jakarta Bean Validation adapter discovered through Java SPI. |
| `hive-archunit` | Reusable ArchUnit rules for Hive's package convention and dependency direction. |
| `hive-cli` | Picocli-based `hive` command for project initialization and scaffolding. |
| `examples/single-context-demo` | Minimal Spring Boot single-context example using the Hive package convention. |
| `examples/document-storage-demo` | Framework-free document storage example using `hive-port` and `hive-adapter`. |
| `examples/todo-list-demo` | Spring Boot example with a full slice: aggregate, entities, value objects, domain events, an outbound port + adapter, validated commands, and a REST controller. |

## Current Version

```text
0.1.0-SNAPSHOT
```

Maven group:

```text
io.sinapsi.hive
```

Java version:

```text
21
```

## Build

Build and verify the whole monorepo:

```bash
mvn clean verify
```

Install local snapshots:

```bash
mvn clean install
```

Build the CLI jar:

```bash
mvn -pl hive-cli -am package
```

The repository includes a launcher script:

```bash
./hive --help
```

## Quick Start

Create a project:

```bash
mkdir hive-demo
cd hive-demo
hive init
```

Generate a use case:

```bash
hive create usecase CreateUser
```

Generate a use case with a command factory:

```bash
hive create usecase CreateUser --factory
```

Generate an ArchUnit test:

```bash
hive create archtest
```

Check the project structure:

```bash
hive check
```

`hive init` creates:

```text
.hive-project
hive.yml
pom.xml
src/main/java
src/test/java
```

Default `hive.yml`:

```yaml
basePackage: com.example.app
layout: single
javaSourceRoot: src/main/java
testSourceRoot: src/test/java
```

The generated `pom.xml` is Java 21-ready and includes `hive-core`. Commands that generate code requiring extra modules update Maven too:

- `--factory` adds `hive-validator`
- `archtest` adds `hive-archunit` and `archunit-junit5`

Existing files are not overwritten unless `--force` is passed.

## Package Convention

Hive's current architecture convention is:

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

`application.ports.in` contains input ports and use case contracts.

Small command types may live close to input contracts. Larger command classes may be extracted to:

```text
application.ports.in.commands
```

`application.ports.out` contains output ports, which are interfaces describing capabilities required by the application layer.

`application.services` contains use case implementations.

`infrastructure.adapters.in` contains inbound adapters such as controllers, listeners, schedulers, and CLI handlers.

`infrastructure.adapters.out` contains outbound adapters such as persistence, messaging, file, HTTP, and external service implementations.

`configurations` and `infrastructure.configs` are composition packages. They wire services, ports, and adapters.

## Generated Use Case

```bash
hive create usecase CreateUser
```

Generates:

```text
src/main/java/com/example/app/application/ports/in/commands/CreateUserCommand.java
src/main/java/com/example/app/application/ports/in/CreateUserUseCase.java
src/main/java/com/example/app/application/services/CreateUserService.java
```

With:

```bash
hive create usecase CreateUser --factory
```

Hive also generates:

```text
src/main/java/com/example/app/application/ports/in/commands/CreateUserCommandFactory.java
```

The factory extends:

```java
io.sinapsi.hive.factory.AbstractCommandFactory
```

and validates through:

```java
ValidationProviders.get()
```

## Optional Modular Layout

Hive supports modular-style scaffolding without requiring Spring Modulith or any module framework.

Create a module:

```bash
hive create module customer
```

Create a use case inside a module:

```bash
hive create usecase customer RegisterCustomer --factory
```

The generated module lives under:

```text
<base-package>.modules.<module-name>
```

with the same internal Hive package convention:

```text
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

Modular support is intentionally optional. Start with one context when the application is small; split into modules when boundaries become valuable.

## Core

Dependency:

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>hive-core</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Package root:

```text
io.sinapsi.hive.core
```

Main contracts:

```text
command.Command
domain.Identifier
domain.AggregateId
domain.AggregateRoot
event.Event
event.DomainEvent
event.IntegrationEvent
event.EventHandler
mapper.Mapper
mapper.BiMapper
port.Port
port.InputPort
port.OutputPort
result.Result
result.Unit
usecase.UseCase
```

`hive-core` has no Spring, Jakarta, JPA, validation runtime, or infrastructure dependency.

Only direction markers live in core:

```text
Port
InputPort
OutputPort
```

Technical ports live in `hive-port`.

## Ports And Adapters

`hive-port` provides optional technical output ports:

```text
ClockPort
IdGeneratorPort<T>
PublishEventPort<E>
FileStoragePort
```

Dependency:

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>hive-port</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

`hive-adapter` provides small default implementations:

```text
ClockPort         -> SystemClockAdapter
IdGeneratorPort   -> UuidGeneratorAdapter
PublishEventPort  -> InMemoryEventPublisherAdapter
FileStoragePort   -> LocalFileStorageAdapter
```

Dependency:

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>hive-adapter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Use these adapters for tests, examples, local development, or small applications. Replace them with project-specific infrastructure when needed.

### Scaffolding Ports And Adapters

Generate an output port under `application.ports.out`:

```bash
hive create port SaveUser
```

Generate an outbound adapter under `infrastructure.adapters.out` implementing it:

```bash
hive create adapter InMemorySaveUser --port SaveUser
```

Both accept an optional leading module name (`hive create port customer SaveCustomer`) and `--force`. The generated port extends `OutputPort`; the adapter implements the named port. A name is given its conventional suffix automatically (`SaveUser` becomes `SaveUserPort`, `InMemorySaveUser` becomes `InMemorySaveUserAdapter`).

## Validation

`hive-validator` keeps validation outside commands and use cases.

Dependency:

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>hive-validator</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Recommended flow:

1. A request enters through an inbound adapter.
2. A command factory creates the command.
3. The factory validates the command.
4. The use case receives a valid command.

Example:

```java
public final class CreateUserCommandFactory
        extends AbstractCommandFactory<CreateUserCommand> {

    public CreateUserCommand create(String name, String email) {
        return validate(new CreateUserCommand(name, email));
    }
}
```

`AbstractCommandFactory` calls:

```java
ValidationProviders.get().commandValidator()
```

If no provider is found, Hive uses a no-op validator.

For Jakarta Bean Validation support, add:

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>jakarta-hive-validator</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

`jakarta-hive-validator` is discovered through Java SPI. Application code should keep using `AbstractCommandFactory`; it should not instantiate the Jakarta provider directly.

## ArchUnit

`hive-archunit` exposes reusable rules through:

```java
io.sinapsi.hive.archunit.HexagonalRules
```

Dependency:

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>hive-archunit</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>com.tngtech.archunit</groupId>
  <artifactId>archunit-junit5</artifactId>
  <version>1.3.0</version>
  <scope>test</scope>
</dependency>
```

Recommended test:

```java
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.sinapsi.hive.archunit.HexagonalRules;

@AnalyzeClasses(packages = "com.example.app")
class ArchitectureTest {
    @ArchTest
    static final ArchRule hiveBaseRules =
            HexagonalRules.allBaseRules("com.example.app");
}
```

`hive create archtest` generates this test for the configured base package.

Available base rules:

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

The ArchUnit module intentionally does not include Spring-specific annotations, ACL-specific rules, or modular monolith dependency rules.

## CLI

Main commands:

```bash
hive init
hive check
hive create usecase CreateUser
hive create usecase CreateUser --factory
hive create port SaveUser
hive create adapter InMemorySaveUser --port SaveUser
hive create module customer
hive create usecase customer RegisterCustomer --factory
hive create archtest
```

Useful options:

```bash
hive init --readme
hive create usecase CreateUser --force
hive create usecase CreateUser --factory --force
hive create archtest --force
```

Every `init`, `create`, and `check` command also accepts `--json`, printing the created files (or the check result) as machine-readable JSON instead of prose:

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

Generated files carry `// TODO:` markers at the points meant to be filled in (command fields, port methods, use case logic, adapter implementations). Together with `--json`, this suits a tool-driven workflow: a generator runs the deterministic CLI for the structure, reads the created paths, and fills the marked spots — then `mvn verify` and the ArchUnit rules validate the result.

The CLI:

- locates projects by walking upward until `.hive-project`
- reads `hive.yml`
- creates deterministic files
- refuses to overwrite files unless `--force` is passed
- updates Maven dependencies for generated code
- keeps Picocli command classes thin and file generation in services

See [hive-cli/README.md](hive-cli/README.md) for the full CLI guide.

## Design Rules

- Strong boundaries, low ceremony.
- Core must remain framework-free.
- Modular support is optional.
- Use cases represent one action.
- Input ports should expose one use case operation.
- Commands are immutable input models.
- Commands may declare validation annotations.
- Commands must not validate themselves.
- Commands may expose helper methods that convert primitives to domain value objects.
- Commands must not create aggregates.
- Business logic belongs in domain or use case code, not in commands.
- Output ports model capabilities, not technologies.
- Adapters implement ports.
- Domain code must not depend on Spring, JPA, HTTP, persistence, messaging, validation frameworks, or infrastructure adapters.

## Examples

Run all examples and modules:

```bash
mvn clean verify
```

Example modules:

```text
examples/single-context-demo
examples/document-storage-demo
examples/todo-list-demo
```

The examples are intentionally small:

- `single-context-demo` shows a Spring Boot inbound adapter calling an input port implemented by an application service.
- `document-storage-demo` shows a framework-free use case wired with Hive's file storage and UUID adapters.
- `todo-list-demo` shows a complete slice — a `TodoList` aggregate with entities, value objects, and domain events, an outbound repository port and adapter, validated commands behind a REST controller, and the Hive clock and event-publishing ports wired to their default adapters. Scaffolded with the `hive` CLI, then implemented and checked by the ArchUnit rules.

They demonstrate how Hive contracts fit into real code without turning the toolkit into a framework.

## Roadmap

- publishable Maven coordinates and release workflow
- richer CLI templates
- CLI dependency version configuration
- optional Spring Boot starter
- Spring adapters for events, clock, and transactions
- more adapter implementations
- migration helpers for existing projects
- AI-assisted generation later

AI-assisted generation is intentionally not implemented in the current MVP.
