---
name: hive
description: Scaffold and implement hexagonal (ports & adapters) features in Java projects that use the Hive toolkit. Use when the project has a .hive-project marker, or the user mentions hexagonal architecture, use cases, input/output ports, adapters, modules, or the `hive` CLI — to add or modify a slice of the application.
---

# Hive — hexagonal scaffolding & implementation

Use this whenever the user asks to **add or change** a use case, output port, outbound adapter, or module in a Hive project. The division of labour is fixed: the deterministic `hive` CLI creates the structure (zero ambiguity, correct-by-construction); you write only the logic at the marked spots.

## 1. Detect the project
A Hive project has a `.hive-project` marker file (walk upward from the working directory to find it). If there is none, this skill does not apply — suggest `/hive:new` to bootstrap one first.

## 2. Resolve the CLI
Use `hive` if it is on the PATH; otherwise use the `./hive` launcher at the project root. (If neither runs, see the project README for how to install or build the CLI.)

## 3. Generate structure with the CLI — never hand-write it
Pick the right command and always pass `--json` so you learn the exact files created:

- Use case:    `hive create usecase <Name> [--factory] --json`
- Output port:  `hive create port <Name> --json`
- Adapter:     `hive create adapter <Name> --port <Port> --json`
- Module:      `hive create module <name> --json`  (then `hive create usecase <module> <Name> --json`)
- Arch test:   `hive create archtest --json`

Read the `created` array from the JSON for the precise paths. Pass `--force` only when you intend to overwrite an existing file.

## 4. Implement: fill the `// TODO:` markers
Open each created file and replace the `// TODO:` markers with real code:
- command fields, output-port methods, use-case logic, adapter implementations, result fields.

Respect the Hive conventions:
- **Domain stays framework-free** — no Spring, JPA, HTTP, persistence, messaging, or validation frameworks in `domain`.
- Business logic lives in the **domain** or the **use-case service**, never in commands.
- Commands are immutable input models: they don't validate themselves and don't build aggregates.
- Output ports model **capabilities**, not technologies; adapters implement them.
- Mapping is an application/adapter concern, not a domain one.

## 5. Validate — always
Run `mvn -q verify`. The ArchUnit rules and tests are the guardrail: fix any rule, compile, or test failure before reporting done. Do not move generated files out of their packages — the rules enforce the layout.

When finished, briefly report what you added (from the JSON) and which `// TODO:` you filled.
