---
name: hive-cli-codegen
description: Generate Hive application, domain, module, inbound/outbound adapter, blueprint, and simple Java artifacts with the `hive create` and `hive generate` commands. Use when the user asks to add use cases, commands, ports, outbound adapters, REST controllers, MCP tools, message listeners, schedulers, value objects, ids, entities, aggregates, events, exceptions, snapshots, records, classes, modules, or blueprint-generated code.
---

# Hive CLI code generation

Use the CLI for structure and only then edit generated logic. Never manually create a file that a `hive create` or `hive generate` command can create.

Always pass `--json` when you need to edit generated files, then read the `created` array.

## Blueprint contract discovery

For any blueprint workflow, first discover the installed CLI contract with:

```bash
hive blueprint schema --json
```

This command is mandatory before authoring or repairing blueprint YAML. Do not inspect Java parser/generator source files, README examples, or this skill to infer blueprint kinds or field shapes. If the command is not available, resolve the CLI (`hive`, then `./hive`; inside the toolkit repo build with `mvn -q -pl hive-cli -am package`) and retry. If it still fails, stop and report that the installed CLI cannot expose its blueprint schema.

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
application/ports/in/<Name>UseCase.java
application/services/<Name>Service.java
```

`<Name>Command` is generated as an immutable final class nested inside `<Name>UseCase`, with private final properties, a private constructor, a nested `Factory`, and record-style accessors. The CLI adds `hive-validator`; it does not create a separate factory file.

Standalone application command:

```bash
hive create command CreateOrder --field customerId:CustomerId --json
```

Standalone `hive create command` artifacts remain standalone under `application/ports/in/commands`, using the same immutable final class representation and nested `Factory`.

Output port:

```bash
hive create port LoadOrder --method "Optional<Order> load(OrderId id)" --json
hive create port SaveOrder --method "void save(Order order)" --json
```

Adapter:

```bash
hive create adapter OrderPersistence --port LoadOrderPort --port SaveOrderPort --json
hive create adapter OrderPersistence --group persistence --port LoadOrderPort --port SaveOrderPort --json
```

If referenced generated ports already contain deterministic methods, adapter skeletons include matching method stubs and neutral default returns.

`hive create adapter` is outbound only. Use it only for implementations of output ports.

Use `--group` when outbound adapters with different integration responsibilities would otherwise accumulate directly under `infrastructure/adapters/out`.
Prefer cohesive groups such as `persistence`, `payment`, `notification`, `storage`, or another project-specific capability boundary.
Do not create one group per port.

## Inbound adapter generators

Use inbound generators for transport entrypoints that call existing application UseCases. The CLI does not create missing UseCases implicitly; generate the UseCase first, usually with `--factory` so inbound code can create and validate Commands through `<Name>UseCase.<Name>Command.Factory`.

REST controller/resource:

```bash
hive create inbound rest Order --usecase CreateOrder --usecase ConfirmOrder --json
hive create inbound rest Order --operation "POST /orders -> CreateOrder" --operation "POST /orders/{id}/confirm -> ConfirmOrder" --json
```

MCP tool:

```bash
hive create inbound mcp Order --usecase CreateOrder --json
```

Message listener/consumer:

```bash
hive create inbound listener OrderCreated --usecase ProcessOrder --json
```

Scheduler/system trigger:

```bash
hive create inbound scheduler ExpireReservations --usecase ExpireReservations --json
```

Generated inbound files live under:

```text
infrastructure/adapters/in/rest
infrastructure/adapters/in/mcp
infrastructure/adapters/in/listener
infrastructure/adapters/in/scheduler
```

Generated inbound adapters must preserve this flow:

```text
Transport input -> transport model -> <Name>UseCase.<Name>Command.Factory -> <Name>UseCase.<Name>Command -> validation -> UseCase
```

Never expose generated Hive Commands directly as transport bodies or arguments. Keep HTTP, MCP SDK, broker, scheduler, and framework types inside infrastructure. If provider-specific details are unknown, leave the generated TODOs rather than inventing framework annotations, paths, verbs, status codes, acknowledgement APIs, retry behavior, or scheduling syntax.

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
hive blueprint schema --json
hive blueprint validate .hive/model/order.yml --json
hive generate .hive/model/order.yml --json
hive generate --all --json
```

The installed HIVE CLI is the authoritative source for the blueprint format supported by that version. Do not keep or reconstruct a static list of blueprint `kind` values in the skill, and do not read parser source code as a substitute for the CLI schema. Use `hive blueprint schema --json` to discover supported kinds, required and optional fields, constraints, aliases, and nested shapes, then use `hive blueprint validate <file.yml> --json` before generation.

## After generation

1. For blueprint workflows, inspect `hive blueprint schema --json`.
2. Validate blueprint files with `hive blueprint validate <file.yml> --json`.
3. Generate with `hive generate ... --json`.
4. Read every path in `created`.
5. Fill only meaningful `// TODO:` markers requested by the user.
6. Keep domain framework-free.
7. Run `mvn -q verify`.
8. Report generated paths and verification result.
