# Hive CLI

`hive-cli` is the Picocli-based scaffolding tool for Hive projects.

The CLI is intentionally small: it creates project markers, a minimal Maven `pom.xml`, basic source roots, simple use case files, optional command factories, optional module folders, ArchUnit test scaffolding, performs a lightweight structure check, and generates C4 architecture diagrams from the package structure.

It does not generate full applications, Spring Boot projects, validators, or domain models. It generates output ports and outbound adapter stubs, but not their implementations.

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
src/main/java/com/example/app/application/ports/in/commands/CreateUserCommand.java
src/main/java/com/example/app/application/ports/in/CreateUserUseCase.java
src/main/java/com/example/app/application/services/CreateUserService.java
```

With `--factory`, it also creates:

```text
src/main/java/com/example/app/application/ports/in/commands/CreateUserCommandFactory.java
```

The command also ensures `pom.xml` contains `hive-core`. When `--factory` is used, it adds `hive-validator` as well.

Generated command:

```java
public record CreateUserCommand() implements Command {
}
```

Generated input port:

```java
public interface CreateUserUseCase extends UseCase<CreateUserCommand, Result> {
}
```

Generated service:

```java
public final class CreateUserService implements CreateUserUseCase {
    @Override
    public Result handle(CreateUserCommand input) {
        return new CreateUserResult();
    }

    private record CreateUserResult() implements Result {
    }
}
```

Generated factory:

```java
public final class CreateUserCommandFactory extends AbstractCommandFactory<CreateUserCommand> {
    public CreateUserCommand create() {
        return validate(new CreateUserCommand());
    }
}
```

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

### hive create adapter

Generates an outbound adapter under `infrastructure.adapters.out` implementing an output port. The port is required via `--port`.

```bash
hive create adapter InMemorySaveUser --port SaveUser
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
src/main/java/com/example/app/modules/customer/application/ports/in/commands/RegisterCustomerCommand.java
src/main/java/com/example/app/modules/customer/application/ports/in/RegisterCustomerUseCase.java
src/main/java/com/example/app/modules/customer/application/services/RegisterCustomerService.java
```

With `--factory`, it also creates:

```text
src/main/java/com/example/app/modules/customer/application/ports/in/commands/RegisterCustomerCommandFactory.java
```

As with single-context use cases, `hive-core` is ensured and `hive-validator` is added when `--factory` is used.

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

The current MVP generator creates this application layout:

```text
application
  ports
    in
      commands
    out
  services
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
- domain aggregates, entities, or value objects
- output port implementations
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
