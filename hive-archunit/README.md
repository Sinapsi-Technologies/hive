# Hive ArchUnit

`hive-archunit` provides reusable ArchUnit rules for projects following Hive's lightweight hexagonal architecture convention.

The module has no Spring dependency. It only depends on ArchUnit and exposes static `ArchRule` factory methods through:

```java
io.sinapsi.hive.archunit.HexagonalRules
```

## Package Convention

Hive rules expect this package shape under your application base package:

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

Small command types may be nested near input contracts. Larger command classes may be extracted to:

```text
application.ports.in.commands
```

`application.ports.out` contains output ports: interfaces that describe capabilities needed by the application layer.

`infrastructure.adapters.in` contains inbound adapters, such as controllers, message listeners, CLI handlers, or schedulers.

`infrastructure.adapters.out` contains outbound adapters, such as persistence, messaging, file, HTTP, or external service adapters.

`configurations` and `infrastructure.configs` are composition/configuration packages. They are allowed to wire adapters and application services.

## Basic Usage

```java
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.sinapsi.hive.archunit.HexagonalRules;

@AnalyzeClasses(packages = "com.acme.shop")
class ArchitectureTest {

    @ArchTest
    static final ArchRule hive_rules =
            HexagonalRules.allBaseRules("com.acme.shop");
}
```

You can also use rules one by one:

```java
@ArchTest
static final ArchRule domain_is_framework_free =
        HexagonalRules.domainShouldNotDependOnFrameworks("com.acme.shop");
```

If `basePackage` is `null` or blank, rules use package patterns such as `..domain..`, making them usable in smaller experiments or tests.

## Rules

### domainShouldNotDependOnFrameworks

Checks classes under:

```text
<base-package>.domain..
```

Domain classes must not depend on:

```text
org.springframework..
jakarta.persistence..
javax.persistence..
```

Intent: keep the domain model framework-free. Aggregates, entities, value objects, domain events, snapshots, and domain exceptions should not know about Spring or persistence APIs.

### domainShouldNotDependOnApplication

Checks classes under:

```text
<base-package>.domain..
```

Domain classes must not depend on:

```text
<base-package>.application..
```

Intent: domain concepts should not depend on use case orchestration, ports, commands, or application services.

### domainShouldNotDependOnInfrastructure

Checks classes under:

```text
<base-package>.domain..
```

Domain classes must not depend on:

```text
<base-package>.infrastructure..
```

Intent: domain code must not depend on adapters, external systems, configuration, persistence, messaging, or delivery mechanisms.

### applicationShouldNotDependOnInfrastructure

Checks classes under:

```text
<base-package>.application..
```

Application classes must not depend on:

```text
<base-package>.infrastructure..
```

Intent: use cases and application services depend on ports and domain objects, not concrete infrastructure implementations.

### applicationShouldNotDependOnInboundAdapters

Checks classes under:

```text
<base-package>.application..
```

Application classes must not depend on:

```text
<base-package>.infrastructure.adapters.in..
```

Intent: inbound adapters call the application layer. The application layer should never know who calls it.

### applicationShouldNotDependOnOutboundAdapters

Checks classes under:

```text
<base-package>.application..
```

Application classes must not depend on:

```text
<base-package>.infrastructure.adapters.out..
```

Intent: application services should call output port interfaces, not concrete outbound adapters.

### inboundAdaptersShouldNotDependOnOutboundAdapters

Checks classes under:

```text
<base-package>.infrastructure.adapters.in..
```

Inbound adapters must not depend on:

```text
<base-package>.infrastructure.adapters.out..
```

Intent: controllers, listeners, schedulers, and CLI handlers should invoke input ports or use cases. They should not bypass the application layer by calling persistence or external service adapters directly.

### mappersShouldNotBeInDomain

Checks classes under:

```text
<base-package>.domain..
```

Classes ending with:

```text
Mapper
```

must not reside in the domain.

Intent: mapping is an application or adapter concern. Domain code should model business concepts, not translate DTOs, persistence rows, HTTP payloads, or external contracts.

### commandsShouldResideInAllowedPlaces

Checks every class ending with:

```text
Command
```

Command classes are allowed only in:

```text
<base-package>.application.ports.in..
<base-package>.application.ports.in.commands..
```

Intent: commands are input models for use cases. Small commands may live near input port/use case contracts; larger commands may be extracted into `application.ports.in.commands`.

This rule forbids command classes in packages such as:

```text
application.command
domain
infrastructure
```

### noStandaloneApplicationCommandPackageShouldBeUsed

Forbids classes in:

```text
<base-package>.application.command..
```

Intent: Hive uses `application.ports.in` as the home for input-side contracts and command models. A standalone `application.command` package creates a second command convention and makes projects drift.

### useCasesShouldResideInsideInputPorts

Checks classes ending with:

```text
UseCase
InputPort
```

They must reside under:

```text
<base-package>.application.ports.in..
```

Intent: use case contracts are inbound application ports. Keeping them under `application.ports.in` makes the dependency direction obvious for inbound adapters.

### outputPortsShouldResideInsideOutputPortsPackage

Checks classes whose names end with:

```text
Port
OutputPort
```

excluding names ending with:

```text
InputPort
```

Those classes must reside under:

```text
<base-package>.application.ports.out..
```

Intent: outbound capabilities required by the application layer should live in `application.ports.out`. Concrete implementations belong in `infrastructure.adapters.out`.

### servicesShouldNotResideInDomain

Checks classes under:

```text
<base-package>.domain..
```

Classes ending with:

```text
Service
```

must not reside in the domain.

Intent: Hive keeps orchestration in `application.services`. If a behavior is truly domain behavior, prefer placing it on an aggregate, entity, value object, or a more explicit domain type instead of creating a generic service.

### noCyclesBetweenMainLayers

Defines these layers:

```text
Domain               -> <base-package>.domain..
Application          -> <base-package>.application..
InboundAdapters      -> <base-package>.infrastructure.adapters.in..
OutboundAdapters     -> <base-package>.infrastructure.adapters.out..
InfrastructureConfig -> <base-package>.configurations..
                        <base-package>.infrastructure.configs..
```

Enforced access rules:

```text
Domain
  may be accessed by Application, InboundAdapters, OutboundAdapters, InfrastructureConfig

Application
  may be accessed by InboundAdapters, OutboundAdapters, InfrastructureConfig

InboundAdapters
  may be accessed only by InfrastructureConfig

OutboundAdapters
  may be accessed only by InfrastructureConfig

InfrastructureConfig
  may not be accessed by any layer
```

Intent: dependencies point inward. Adapters depend on application and domain contracts; application does not depend on adapters; domain remains at the center.

## allBaseRules

Composes all base rules into one `ArchRule`:

```java
@ArchTest
static final ArchRule hive_rules =
        HexagonalRules.allBaseRules("com.acme.shop");
```

This is the recommended starting point for new projects that follow the full Hive package convention.

For projects migrating gradually, use individual rules first and enable `allBaseRules` once the package structure is aligned.

## Not Included

This module intentionally does not include:

- modular monolith isolation rules
- ACL-specific rules
- Spring-specific annotations or dependencies

Hive ArchUnit focuses on the base hexagonal package convention and dependency direction.
