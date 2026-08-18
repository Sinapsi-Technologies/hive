---
name: hive
description: Work on Java projects that use the Hive toolkit and its `hive` CLI. Use when the user mentions Hive, hexagonal architecture, ports and adapters, use cases, domain primitives, modules, generated scaffolding, ArchUnit rules, blueprints, or C4 diagrams.
---

# Hive project workflow

Use this skill as the top-level workflow for Hive projects. Hive's rule is simple: let the deterministic CLI create structure, then edit only the generated TODOs and surrounding business logic.

## Project detection

A Hive project contains `.hive-project`. Walk upward from the current directory to find it.

- If the marker exists, work from that project root.
- If there is no marker and the user wants a new project, use `/hive:new`.
- If there is no marker and the user wants to modify an existing project, ask them to initialize or point you to the Hive project root.

## CLI resolution

Use this order:

1. `hive`
2. `./hive` from the Hive project root
3. if inside the Hive toolkit repo, build once with `mvn -q -pl hive-cli -am package`, then use `./hive`

Always prefer CLI commands over hand-written structural files.

## Command map

Project lifecycle:

```bash
hive init [--readme] [--force] [--json]
hive check [--json]
hive inspect config|model|generated [--json]
```

Application layer:

```bash
hive create usecase [moduleName] <UseCaseName> [--field name:Type] [--factory] [--force] [--json]
hive create command [moduleName] <CommandName> [--field name:Type] [--force] [--json]
hive create port [moduleName] <PortName> [--method "ReturnType method(Type arg)"] [--force] [--json]
hive create adapter [moduleName] <AdapterName> --port <PortName> [--port <PortName>] [--force] [--json]
```

Domain layer:

```bash
hive create vo [moduleName] <Name> [--type String|UUID|Integer|Long|BigDecimal|Boolean] [--field name:Type] [--not-null] [--not-blank] [--min value] [--max value] [--min-length n] [--max-length n] [--pattern regex] [--factory] [--force] [--json]
hive create id [moduleName] <Name> [--type UUID|Long|String] [--force] [--json]
hive create entity [moduleName] <Name> --id <IdType> [--field name:Type] [--force] [--json]
hive create aggregate [moduleName] <Name> --id <IdType> [--field name:Type] [--force] [--json]
hive create enum [moduleName] <Name> --value <VALUE> [--value <VALUE>] [--force] [--json]
hive create event [moduleName] <Name> [--field name:Type] [--force] [--json]
hive create exception [moduleName] <Name> [--message "Message"] [--force] [--json]
hive create domainservice [moduleName] <Name> [--force] [--json]
hive create snapshot [moduleName] <Name> [--field name:Type] [--force] [--json]
```

Shared/simple Java artifacts:

```bash
hive create record [moduleName] <Name> [--field name:Type] [--force] [--json]
hive create class [moduleName] <Name> [--field name:Type] [--getters] [--setters] [--constructor] [--all-args-constructor] [--force] [--json]
```

Modules, architecture tests, blueprints, and diagrams:

```bash
hive create module <moduleName> [--json]
hive create archtest [--force] [--json]
hive generate <file.yml> [--force] [--json]
hive generate --all [--force] [--json]
hive c4 generate [--module name] [--level context|container|component] [--output path] [--format puml] [--render [svg|png]] [--site] [--open] [--force] [--json]
```

Use `hive create vo` for value objects.

## Operating rules

- Pass `--json` for scaffolding commands whenever you will edit the generated files; read the `created` array for exact paths.
- Use `--force` only when the user clearly wants to overwrite existing generated files.
- Do not move generated files out of Hive packages. ArchUnit rules enforce the layout.
- Domain code must stay framework-free: no Spring, JPA, HTTP, persistence, messaging, or Jakarta validation annotations in `domain`.
- Commands are immutable input models. Business logic belongs in domain types or application services.
- Output ports describe capabilities, not technologies. Adapters implement technologies.
- If a requested artifact maps to a CLI generator, run the CLI first. Fill TODOs after generation.

## Verification

For normal changes, finish with:

```bash
mvn -q verify
```

For CLI-only changes inside the Hive toolkit repository, use:

```bash
mvn -q -pl hive-cli -am test
```

Fix compile, test, and ArchUnit failures before reporting done.
