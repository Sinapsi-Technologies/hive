# Hive CLI

`hive-cli` is the Picocli-based scaffolding tool for Hive projects.

The CLI is intentionally small: it creates project markers, a minimal Maven `pom.xml`, basic source roots, simple use case files, optional command factories, optional module folders, ArchUnit test scaffolding, performs a lightweight structure check, and generates C4 architecture diagrams from the package structure.

It does not generate full applications, Spring Boot projects, validators, repositories, or adapter implementations. It creates the architectural structure and framework-free Java types that are deterministic; the developer or coding agent still decides the business behavior.

## Command Name

```bash
hive
```

The Java entry point is:

```text
io.sinapsi.hive.cli.Main
```

## Dependency

Inside this monorepo, the module artifact is:

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>hive-cli</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

For local development, build the project first:

```bash
mvn clean install
```

## Terminal Setup

The repository includes a bash launcher:

```text
./hive
```

The script resolves the repository directory and runs:

```bash
java -jar "$DIR/hive-cli/target/hive-cli.jar" "$@"
```

Build the CLI jar first:

```bash
mvn -pl hive-cli -am package
```

Make the script executable:

```bash
chmod +x ./hive
```

Run the CLI from the repository root:

```bash
./hive --help
```

To use `hive` as a command from any terminal, add a shell alias.

For zsh:

```bash
echo 'alias hive="/Users/cristian/Documents/code/sinapsi/hive/hive"' >> ~/.zshrc
source ~/.zshrc
```

For bash:

```bash
echo 'alias hive="/Users/cristian/Documents/code/sinapsi/hive/hive"' >> ~/.bashrc
source ~/.bashrc
```

Alternatively, create a symlink in a directory already on your `PATH`:

```bash
ln -sf /Users/cristian/Documents/code/sinapsi/hive/hive /usr/local/bin/hive
```

Verify the command:

```bash
hive --help
```

## Terminal Usage Example

Create a temporary project:

```bash
mkdir -p /tmp/hive-cli-demo
cd /tmp/hive-cli-demo
```

Initialize it:

```bash
hive init --readme
```

Create a use case:

```bash
hive create usecase CreateUser
```

Create domain primitives:

```bash
hive create id UserId
hive create vo Email --type String --not-blank
hive create entity User --id UserId --field email:Email
hive create port LoadUser --method "Optional<User> load(UserId id)"
```

Create a use case with a command factory:

```bash
hive create usecase CreateUser --factory
```

Create an ArchUnit architecture test:

```bash
hive create archtest
```

Create a module and a module use case:

```bash
hive create module customer
hive create usecase customer RegisterCustomer --factory
```

Check the project:

```bash
hive check
```

Inspect generated files:

```bash
find . -maxdepth 8 -type f | sort
```

Run from a nested directory. The CLI walks upward until it finds `.hive-project`:

```bash
cd src/main/java
hive check
```

Overwrite generated files when needed:

```bash
hive create usecase CreateUser --force
```

Remove the temporary project:

```bash
rm -rf /tmp/hive-cli-demo
```

## Project Marker

Hive CLI commands look for this marker file:

```text
.hive-project
```

`hive init` creates it with this content:

```text
hive-toolkit
```

Commands such as `hive check`, `hive create usecase`, and `hive create module` locate the project root by walking upward from the current directory until they find `.hive-project`.

## Configuration

`hive init` creates:

```text
hive.yml
```

Default content:

```yaml
basePackage: com.example.app
layout: single
javaSourceRoot: src/main/java
testSourceRoot: src/test/java
```

Fields:

```text
basePackage
  Java package root used for generated code.

layout
  Current values are convention-only. The generator accepts single or modular-style usage,
  but does not enforce the field yet.

javaSourceRoot
  Root directory for generated production Java sources.

testSourceRoot
  Root directory checked by hive check.
```

The current parser is intentionally minimal. It reads simple `key: value` lines and ignores blank lines and comments.

## Commands

### hive init

Initializes a Hive project in the current directory.

```bash
hive init
```

Creates:

```text
.hive-project
hive.yml
pom.xml
src/main/java
src/test/java
```

The generated `pom.xml` includes Java 21 compiler settings and the `hive-core` dependency because generated commands, use cases, and results import core Hive contracts.

Options:

```bash
hive init --force
```

Overwrites existing generated files.

```bash
hive init --readme
```

Creates a small `README.md` only when one does not already exist.

### hive check

Checks whether the current directory, or one of its parents, is a Hive project.

```bash
hive check
```

It verifies:

```text
.hive-project exists
hive.yml exists
pom.xml exists
javaSourceRoot exists
testSourceRoot exists
```

Exit codes:

```text
0  project structure looks good
1  project found, but expected files or directories are missing
2  no .hive-project marker was found
```

Warnings are printed to stderr.

### hive create usecase

Generates a simple single-context use case.

```bash
hive create usecase CreateUser
```

With the default config, this creates:

```text
src/main/java/com/example/app/application/ports/in/CreateUserUseCase.java
src/main/java/com/example/app/application/services/CreateUserService.java
```

The command also ensures `pom.xml` contains `hive-core` and `hive-validator`.

Generated input port with nested command:

```java
public interface CreateUserUseCase extends UseCase<CreateUserUseCase.CreateUserCommand, Result> {
    public final class CreateUserCommand implements Command {
        private CreateUserCommand() {
        }

        public static final class Factory extends AbstractCommandFactory<CreateUserCommand> {
            public CreateUserCommand create() {
                return validate(new CreateUserCommand());
            }
        }
    }
}
```

Generated service:

```java
public final class CreateUserService implements CreateUserUseCase {
    @Override
    public Result handle(CreateUserUseCase.CreateUserCommand input) {
        return new CreateUserResult();
    }

    private record CreateUserResult() implements Result {
    }
}
```

`CreateUserCommand` is an immutable final class with a private constructor, nested `Factory`, and record-style accessors for any fields. The nested Factory is the supported construction path, so application command validation cannot be bypassed by direct construction. Standalone commands created with `hive create command` continue to be generated as separate files under `application/ports/in/commands`, using the same immutable class shape.

The generated factory uses `hive-validator`; the CLI adds this dependency when it is missing:

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>hive-validator</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Use `--force` to overwrite existing generated files:

```bash
hive create usecase CreateUser --force
```

Without `--force`, the CLI refuses to overwrite existing files.

`--factory` and `--force` can be combined:

```bash
hive create usecase CreateUser --factory --force
```

Use repeated `--field name:Type` to populate the generated command record without changing the rest of the use-case structure:

```bash
hive create usecase CreateUser --field name:String --field email:Email
```

### hive create port

Generates an output port under `application.ports.out`.

```bash
hive create port SaveUser
```

With the default config, this creates:

```text
src/main/java/com/example/app/application/ports/out/SaveUserPort.java
```

```java
public interface SaveUserPort extends OutputPort {
}
```

The `Port` suffix is added automatically when missing, so `SaveUser` and `SaveUserPort` produce the same file. A leading module name and `--force` are supported.

Use repeated `--method` for deterministic output-port signatures:

```bash
hive create port LoadOrder --method "Optional<Order> load(OrderId id)"
hive create port SaveOrder --method "void save(Order order)"
```

Method parsing is intentionally limited to common Java signatures and simple generics such as `Optional<Order>`, `List<Order>`, `Set<String>`, and `Map<String, String>`. Unsupported signatures fail clearly instead of being guessed.

### hive create adapter

Generates an outbound adapter under `infrastructure.adapters.out` implementing an output port. The port is required via `--port`.

```bash
hive create adapter InMemorySaveUser --port SaveUser
hive create adapter OrderPersistence --group persistence --port LoadOrderPort --port SaveOrderPort
```

With the default config, this creates:

```text
src/main/java/com/example/app/infrastructure/adapters/out/InMemorySaveUserAdapter.java
```

```java
public final class InMemorySaveUserAdapter implements SaveUserPort {
}
```

The `Adapter` suffix is added automatically when missing. A leading module name and `--force` are supported.

Use `--group <group>` to place outbound adapters under a cohesive subpackage such as `infrastructure.adapters.out.persistence`, `infrastructure.adapters.out.payment`, or another project-specific integration concern. Without `--group`, adapters are generated directly under `infrastructure.adapters.out`.

Adapters can implement multiple output ports by repeating `--port`:

```bash
hive create adapter OrderPersistence --port LoadOrderPort --port SaveOrderPort
```

When the referenced generated ports already contain deterministic method declarations, the adapter emits matching method skeletons with TODO markers and neutral default returns. It never infers persistence technology or repository code.

### hive create vo

Generates a scalar-backed value object under `domain.valueobjects`.

```bash
hive create vo Email --type String --not-blank
hive create vo CustomerId --type UUID
```

With the default config, this creates:

```text
src/main/java/com/example/app/domain/valueobjects/Email.java
```

Generated value objects use Java records:

```java
public record Email(String value) {
    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
```

Supported scalar types:

```text
String
UUID
Integer
Long
BigDecimal
Boolean
```

Supported deterministic constraints:

```text
--not-null
--not-blank
--min <value>
--max <value>
--min-length <value>
--max-length <value>
--pattern <regex>
```

String-only constraints are `--not-blank`, `--min-length`, `--max-length`, and `--pattern`. Numeric constraints are supported for `Integer`, `Long`, and `BigDecimal`. Validation is framework-free and generated as constructor checks; the CLI does not add Jakarta annotations to domain objects.

Multi-field value objects are supported with repeated `--field`; in that mode scalar constraints are rejected because there is no single `value` component:

```bash
hive create vo Money --field amount:BigDecimal --field currency:String --factory
```

`--factory` adds a deterministic static `of(...)` constructor helper.

### hive create id

Generates an identifier under `domain.valueobjects`.

```bash
hive create id CustomerId
hive create id LegacyCustomerId --type Long
hive create id ExternalOrderId --type String
```

With the default config, this creates:

```text
src/main/java/com/example/app/domain/valueobjects/CustomerId.java
```

Generated identifiers reuse the `hive-core` aggregate identifier contract:

```java
public record CustomerId(UUID value) implements AggregateId<UUID> {
    public CustomerId {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
    }
}
```

Supported backing types are `UUID`, `Long`, and `String`. The default is `UUID`.

### hive create entity

Generates a domain entity under `domain.entities`.

```bash
hive create entity OrderLine --id OrderLineId --field productId:ProductId --field quantity:Quantity
```

With the default config, this creates:

```text
src/main/java/com/example/app/domain/entities/OrderLine.java
```

Generated entities keep state controlled: fields are `final`, constructor-initialized, and no setters are generated.

```java
public final class OrderLine {
    private final OrderLineId id;
    private final ProductId productId;
    private final Quantity quantity;

    public OrderLine(OrderLineId id, ProductId productId, Quantity quantity) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
    }

    public OrderLineId getId() {
        return id;
    }

    public ProductId productId() {
        return productId;
    }

    public Quantity quantity() {
        return quantity;
    }
}
```

`--field` may be repeated and must use `name:Type` syntax.

### hive create aggregate

Generates an aggregate under `domain.aggregates`.

```bash
hive create aggregate Order --id OrderId --field status:OrderStatus
```

With the default config, this creates:

```text
src/main/java/com/example/app/domain/aggregates/Order.java
```

Generated aggregates implement `AggregateRoot<ID>` and include a small domain-event buffer, but do not infer behavior:

```java
public final class Order implements AggregateRoot<OrderId> {
    private final OrderId id;
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    private final OrderStatus status;

    public Order(OrderId id, OrderStatus status) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
        this.status = status;
    }

    @Override
    public OrderId getId() {
        return id;
    }

    @Override
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    // TODO: add domain behavior here.
}
```

`--field` may be repeated and must use `name:Type` syntax.

### hive create enum

Generates a domain enum under `domain.valueobjects`.

```bash
hive create enum OrderStatus --value DRAFT --value CONFIRMED --value CANCELLED
```

With the default config, this creates:

```text
src/main/java/com/example/app/domain/valueobjects/OrderStatus.java
```

At least one `--value` is required. Values are normalized to uppercase Java enum constants.

### hive create event

Generates a domain event under `domain.events`.

```bash
hive create event OrderCreated --field orderId:OrderId --field occurredAt:Instant
```

With the default config, this creates:

```text
src/main/java/com/example/app/domain/events/OrderCreated.java
```

Generated events are immutable records implementing `DomainEvent`:

```java
public record OrderCreated(OrderId orderId, Instant occurredAt) implements DomainEvent {
}
```

No event bus or infrastructure dependency is generated.

### hive create exception

Generates a framework-free domain exception under `domain.exceptions`.

```bash
hive create exception OrderAlreadyConfirmed
```

With the default config, this creates:

```text
src/main/java/com/example/app/domain/exceptions/OrderAlreadyConfirmedException.java
```

The `Exception` suffix is added automatically when missing.

Use `--message` to generate a default constructor with a fixed message:

```bash
hive create exception OrderAlreadyConfirmed --message "Order is already confirmed"
```

### hive create domainservice

Generates a framework-free domain service skeleton under `domain.services`.

```bash
hive create domainservice Pricing
```

With the default config, this creates:

```text
src/main/java/com/example/app/domain/services/PricingService.java
```

The `Service` suffix is added automatically when missing. The generated class contains only a TODO marker for domain behavior.

### hive create snapshot

Generates an immutable snapshot record under `domain.snapshots`.

```bash
hive create snapshot OrderSnapshot --field id:OrderId --field status:OrderStatus
```

With the default config, this creates:

```text
src/main/java/com/example/app/domain/snapshots/OrderSnapshot.java
```

### hive create record

Generates a deterministic Java record under `commons`.

```bash
hive create record CustomerResponse --field id:UUID --field tags:List<String>
```

With the default config, this creates:

```text
src/main/java/com/example/app/commons/CustomerResponse.java
```

Common JDK imports such as `UUID`, `BigDecimal`, `Instant`, `Optional`, `List`, `Set`, and `Map` are resolved deterministically.

### hive create class

Generates a deterministic plain Java class under `commons`.

```bash
hive create class CustomerDto --field name:String --field email:String --getters --setters --all-args-constructor
```

Supported options:

```text
--getters
--setters
--constructor
--all-args-constructor
```

Setters are never generated unless `--setters` is passed. No Lombok dependency is added.

### hive create command

Generates an immutable application command class under `application.ports.in.commands`.

```bash
hive create command CreateOrder --field customerId:CustomerId
```

The `Command` suffix is added automatically when missing. The generated class is `final`, implements `io.sinapsi.hive.core.command.Command`, stores properties in `private final` fields, has a private constructor, exposes record-style accessors, and includes a nested `Factory` that extends `AbstractCommandFactory`.

### hive create module

Generates a module folder structure under `basePackage.modules.<moduleName>`.

```bash
hive create module customer
```

With the default config, this creates:

```text
src/main/java/com/example/app/modules/customer/commons
src/main/java/com/example/app/modules/customer/configurations
src/main/java/com/example/app/modules/customer/application/ports/in/commands
src/main/java/com/example/app/modules/customer/application/ports/out
src/main/java/com/example/app/modules/customer/application/services
src/main/java/com/example/app/modules/customer/domain/valueobjects
src/main/java/com/example/app/modules/customer/domain/aggregates
src/main/java/com/example/app/modules/customer/domain/events
src/main/java/com/example/app/modules/customer/domain/entities
src/main/java/com/example/app/modules/customer/domain/services
src/main/java/com/example/app/modules/customer/domain/snapshots
src/main/java/com/example/app/modules/customer/domain/exceptions
src/main/java/com/example/app/modules/customer/infrastructure/configs
src/main/java/com/example/app/modules/customer/infrastructure/adapters/in
src/main/java/com/example/app/modules/customer/infrastructure/adapters/out
```

Module names may use `kebab-case` or `snake_case`; the CLI converts them to lower camel package names.

Example:

```bash
hive create module customer-profile
```

creates package segment:

```text
customerProfile
```

### hive create usecase <moduleName> <UseCaseName>

Generates a use case inside a module's application package.

```bash
hive create usecase customer RegisterCustomer
```

With the default config, this creates:

```text
src/main/java/com/example/app/modules/customer/application/ports/in/RegisterCustomerUseCase.java
src/main/java/com/example/app/modules/customer/application/services/RegisterCustomerService.java
```

As with single-context use cases, `RegisterCustomerCommand` is nested inside `RegisterCustomerUseCase`; `--factory` nests `RegisterCustomerCommand.Factory` there too, ensures `hive-core`, and adds `hive-validator`.

Use `--force` to overwrite existing generated files:

```bash
hive create usecase customer RegisterCustomer --force
```

### hive create archtest

Generates a reusable ArchUnit test wired to Hive's base rules.

```bash
hive create archtest
```

With the default config, this creates:

```text
src/test/java/com/example/app/ArchitectureTest.java
```

Generated test:

```java
@AnalyzeClasses(packages = "com.example.app")
class ArchitectureTest {
    @ArchTest
    static final ArchRule hiveBaseRules = HexagonalRules.allBaseRules("com.example.app");
}
```

The generated test uses `hive-archunit` and ArchUnit JUnit 5. The CLI adds both test dependencies when they are missing:

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

Use `--force` to overwrite an existing generated architecture test:

```bash
hive create archtest --force
```

### hive c4 generate

Generates [C4](https://c4model.com/) architecture diagrams from the project's hexagonal package structure. Unlike the `init`/`create` commands, this one reads existing code rather than scaffolding new files.

```bash
hive c4 generate --module todo --render --site
```

With a `todo` module, this writes:

```text
docs/architecture/c3-todo.puml
docs/architecture/c3-todo.svg     # only when PlantUML is available
docs/architecture/index.html      # only with --site (or c4.generateSite: true)
```

Options:

```text
--module <name>     Module / bounded-context name. In the modular layout it selects one
                    module; in the single layout it sets the diagram name.
--level <level>     context | container | component. Default: component (the C3 diagram).
--output <path>     Output directory. Default: docs/architecture.
--format <format>   Diagram format. For now: puml.
--render [format]   Render the generated .puml to svg or png. Default: svg.
--site              Also generate a static index.html architecture page.
--open              Open the generated page (or diagram) afterwards.
--force             Overwrite generated files when they already exist.
--json              Print a machine-readable result.
```

How it classifies code (the Hive convention):

```text
*.infrastructure.adapters.in.*    -> Inbound Adapter
*.application.ports.in.*          -> Input Port
*.application.services.*Service   -> Application Service
*.domain.aggregates.*             -> Domain Aggregate
*.application.ports.out.*         -> Output Port
*.infrastructure.adapters.out.*   -> Outbound Adapter
*.configurations.* / *.infrastructure.configs.*  -> Configuration
```

Relationships are inferred conservatively (exact name matching first, shared-domain-token overlap otherwise) so the diagram reads as the hexagon flow instead of a fully connected graph. Commands, value objects, entities, events, and other domain detail are scanned but hidden from the component view to keep it readable.

Both layouts are supported: the single layout produces one diagram named after the base package's last segment; the modular layout (`hive create module <name>`) produces one focused diagram per module.

Rendering is optional. The command looks for a `plantuml` executable on the `PATH`, or `java -jar plantuml.jar` when `c4.plantumlPath` points at a jar. If none is found, it keeps the `.puml` output and prints how to enable rendering — it does not fail. Optional `hive.yml` block:

```yaml
c4:
  plantumlPath: plantuml
  defaultRenderFormat: svg
  generateSite: true
  theme: hive
```

`--json` output is consistent with the other commands and also reports the architecture model:

```json
{
  "command": "c4 generate",
  "level": "component",
  "module": "todo",
  "output": "docs/architecture",
  "created": ["docs/architecture/c3-todo.puml", "docs/architecture/index.html"],
  "skipped": [],
  "warnings": [],
  "elements": [{ "id": "todoController", "name": "TodoController", "type": "INBOUND_ADAPTER" }],
  "relations": [{ "source": "todoController", "target": "createTodoUseCase", "description": "calls" }]
}
```

## Name Validation

Use case names must be valid Java type names.

Valid:

```text
CreateUser
RegisterCustomer
PlaceOrder
```

Invalid:

```text
create-user
123CreateUser
Create User
```

Module names are converted to package names.

## Current Generated Layout

The generators target the standard Hive package convention:

```text
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
  exceptions
```

For modules, `hive create module` creates the broader Hive package convention:

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

This layout is compatible with strict `hive-archunit` rules such as:

```java
HexagonalRules.allBaseRules("com.example.app")
```

## What The CLI Does Not Generate Yet

The MVP does not generate:

- Jakarta validation SPI files
- adapters from `hive-adapter`
- Spring Boot configuration
- output port implementations
- persistence repositories or technology-specific data access
- generated artifact manifests and hash-based regeneration safety
- `hive diff`, `hive explain`, and cross-artifact merge semantics
- modular ACL contracts

These are expected future additions.

## JSON Output

Every `init`, `create`, and `check` command accepts `--json`, which prints a machine-readable result instead of the human-friendly message:

```bash
hive create port SaveUser --json
```

```json
{
  "command": "create port",
  "name": "SaveUser",
  "created": [
    "src/main/java/com/example/app/application/ports/out/SaveUserPort.java"
  ]
}
```

`check --json` reports `{"command": "check", "ok": <bool>, "warnings": [...]}`. Paths are relative to the project root and use forward slashes. This, combined with the `// TODO:` markers in generated files, lets a tool drive the CLI for the structural files and then fill the marked spots.

## Design Notes

- Command classes are thin Picocli entry points.
- File generation logic lives in services.
- Generated files are deterministic.
- Existing files are protected unless `--force` is passed.
- The CLI is project-local and uses `.hive-project` to find the root.
- The CLI is intentionally lightweight and library-first.

## License

`hive-cli` is licensed under AGPL-3.0. See [LICENSE](LICENSE).

The AGPL-3.0 license applies to the CLI program itself. Source files and other
artifacts generated by running `hive-cli` are not automatically subject to
AGPL-3.0 merely because they were generated by the CLI. Generated output remains
under the license chosen by the user/project, except to the extent that an
output artifact contains substantial copyrightable HIVE-authored code or
template material copied into it. The generic generators are intended to emit
original structural skeletons rather than substantial copied portions of the CLI
implementation.

This is a statement of project licensing intent, not legal advice.
