# Hive — Claude Code plugin

Drives the [Hive](https://github.com/Sinapsi-Technologies/hive) CLI from Claude Code to create and evolve hexagonal (ports & adapters) Java projects.

The plugin pairs the **deterministic CLI** (structure, repeatable code generation) with **Claude** (fills the generated `// TODO:` markers with real logic), and validates changes with Maven and the Hive ArchUnit rules.

## What's inside

- **`/hive:new [basePackage]`** - slash command. Bootstraps a new project: resolves the CLI, runs `hive init --readme --json`, scaffolds a starter slice, and verifies.
- **`hive` skill** - top-level workflow for Hive projects. Maps user requests to the real CLI surface and keeps the deterministic CLI / Claude logic split clear.
- **`hive-cli-project` skill** - `hive init`, `hive check`, `hive inspect`, `hive create archtest`, and project verification.
- **`hive-cli-codegen` skill** - `hive create ...` and `hive generate ...` for use cases, commands, ports, outbound adapters, inbound REST/MCP/listener/scheduler adapters, modules, domain primitives, records, classes, and blueprints.
- **`hive-cli-c4` skill** - `hive c4 generate`, PlantUML rendering, architecture site output, and C4 troubleshooting.

## Install

```text
/plugin marketplace add Sinapsi-Technologies/hive
/plugin install hive@sinapsi
```

## Prerequisites

- The `hive` CLI available (on the PATH, the `./hive` launcher, or built from source).
- For a generated project to build, the `io.sinapsi.hive` libraries must be resolvable — from Maven Central once released, or via `mvn install` of the Hive repo for local development.

## Usage

```text
/hive:new com.acme.billing      # bootstrap a project
"add a RegisterInvoice use case with a SaveInvoice port and an in-memory adapter"
"create an Email value object and a Customer aggregate"
"generate C4 diagrams for the todo module"
```

## CLI surface captured by the skills

The skills intentionally mirror the current CLI:

```text
hive init
hive check
hive inspect config|model|generated
hive create usecase|command|port|adapter|module|archtest
hive create inbound rest|mcp|listener|scheduler
hive create vo|id|entity|aggregate|enum|event|exception|domainservice|snapshot
hive create record|class
hive blueprint schema
hive blueprint validate <file.yml>
hive generate <file.yml>
hive generate --all
hive c4 generate
```

Use `hive create vo` for value objects.
Use `hive blueprint schema --json` as the authoritative blueprint contract for the installed CLI version instead of copying blueprint syntax into agents or docs.
