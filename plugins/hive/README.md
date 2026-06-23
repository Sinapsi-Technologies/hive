# Hive — Claude Code plugin

Drives the [Hive](https://github.com/Sinapsi-Technologies/hive) CLI from Claude Code to create and evolve hexagonal (ports & adapters) Java projects.

The plugin pairs the **deterministic CLI** (structure, zero tokens, correct-by-construction) with **Claude** (fills the generated `// TODO:` markers with real logic), and validates every change with `mvn verify` + the Hive ArchUnit rules.

## What's inside

- **`/hive:new [basePackage]`** — slash command. Bootstraps a new project: resolves the CLI, `hive init`, scaffolds a starter slice, and runs `mvn verify` until green.
- **`hive` skill** — triggers when you ask to add or change a use case / port / adapter / module. Runs `hive create … --json`, reads the created paths, fills the `// TODO:` markers, and verifies.

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
```
