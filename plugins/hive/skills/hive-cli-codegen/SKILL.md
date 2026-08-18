---
name: hive-cli-codegen
description: Generate Hive application, domain, module, blueprint, and simple Java artifacts with the `hive create` and `hive generate` commands. Use when the user asks to add use cases, commands, ports, adapters, value objects, ids, entities, aggregates, events, exceptions, snapshots, records, classes, modules, or blueprint-generated code.
---

# Hive CLI code generation

Use the CLI for structure and only then edit generated logic. Never manually create a file that a `hive create` or `hive generate` command can create.

Always pass `--json` when you need to edit generated files, then read the `created` array.

## Name and module convention

Most generators accept either:

```bash
hive create <kind> <Name>
hive create <kind> <moduleName> <Name>
```

The second form generates under `basePackage.modules.<moduleName>`. Module names may be kebab-case or snake_case; the CLI converts them to a Java package segment.

## Application generators

Use case with optional command fields:

```bash
hive create usecase CreateOrder --field customerId:CustomerId --field total:Money --json
hive create usecase billing CapturePayment --field orderId:OrderId --factory --json
```

Generated files:

```text
application/ports/in/commands/<Name>Command.java
application/ports/in/<Name>UseCase.java
application/services/<Name>Service.java
```

With `--factory`, the CLI also creates `<Name>CommandFactory.java` and adds `hive-validator`.

Standalone application command:

```bash
hive create command CreateOrder --field customerId:CustomerId --json
```

Output port:

```bash
hive create port LoadOrder --method "Optional<Order> load(OrderId id)" --json
hive create port SaveOrder --method "void save(Order order)" --json
```

Adapter:

```bash
hive create adapter OrderPersistence --port LoadOrderPort --port SaveOrderPort --json
```

If referenced generated ports already contain deterministic methods, adapter skeletons include matching method stubs and neutral default returns.

## Domain generators

Value object. The command is `vo`.

```bash
hive create vo Email --type String --not-blank --pattern ".+@.+" --json
hive create vo Money --field amount:BigDecimal --field currency:String --factory --json
```

Scalar value object types:

```text
String
UUID
Integer
Long
BigDecimal
Boolean
```

Scalar constraints:

```text
--not-null
--not-blank
--min <value>
--max <value>
--min-length <n>
--max-length <n>
--pattern <regex>
```

Multi-field value objects use repeated `--field`; do not combine them with scalar constraints.

Identifier:

```bash
hive create id OrderId --json
hive create id ExternalOrderId --type String --json
```

Entity:

```bash
hive create entity OrderLine --id OrderLineId --field productId:ProductId --field quantity:Quantity --json
```

Aggregate:

```bash
hive create aggregate Order --id OrderId --field status:OrderStatus --json
```

Enum:

```bash
hive create enum OrderStatus --value DRAFT --value CONFIRMED --value CANCELLED --json
```

Event:

```bash
hive create event OrderCreated --field orderId:OrderId --field occurredAt:Instant --json
```

Exception:

```bash
hive create exception OrderAlreadyConfirmed --message "Order is already confirmed" --json
```

Domain service and snapshot:

```bash
hive create domainservice Pricing --json
hive create snapshot OrderSnapshot --field id:OrderId --field status:OrderStatus --json
```

## Shared Java generators

Record:

```bash
hive create record CustomerResponse --field id:UUID --field tags:List<String> --json
```

Plain class:

```bash
hive create class CustomerDto --field name:String --field email:String --getters --setters --all-args-constructor --json
```

Setters are never generated unless `--setters` is passed. Lombok is not added.

## Module and blueprint generators

Module:

```bash
hive create module customer --json
```

Blueprint file:

```bash
hive generate .hive/model/order.yml --json
hive generate --all --json
```

Supported blueprint `kind` values:

```text
id
vo
entity
aggregate
enum
event
exception
domainService
snapshot
record
class
command
outputPort
adapter
```

## After generation

1. Read every path in `created`.
2. Fill only meaningful `// TODO:` markers requested by the user.
3. Keep domain framework-free.
4. Run `mvn -q verify`.
5. Report generated paths and verification result.
