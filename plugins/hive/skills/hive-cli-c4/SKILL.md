---
name: hive-cli-c4
description: Generate, render, inspect, or troubleshoot Hive C4 architecture diagrams from a Hive project's package structure. Use when the user asks for C4, PlantUML, architecture diagrams, component diagrams, architecture site output, or `hive c4 generate`.
---

# Hive CLI C4 diagrams

Use this skill for architecture visualization.

## Generate diagrams

Default component diagram:

```bash
hive c4 generate --json
```

For a module or bounded context:

```bash
hive c4 generate --module todo --json
```

Render SVG and generate the static site:

```bash
hive c4 generate --module todo --render svg --site --json
```

Useful options:

```text
--module <name>       Module or bounded-context name.
--level <level>       context, container, or component. Default: component.
--output <path>       Output directory. Default: docs/architecture.
--format <format>     Diagram source format. Currently puml.
--render [format]     Render generated .puml to svg or png. Default: svg.
--site                Generate index.html.
--open                Open the generated page or diagram.
--force               Overwrite generated files.
--json                Print generated outputs as JSON.
```

Prefer `--json` so you can report exact output paths.

## What the scanner reads

The C4 generator reads Hive package conventions:

```text
application/ports/in
application/services
application/ports/out
domain
infrastructure/adapters/in
infrastructure/adapters/out
```

It does not infer business semantics or draw a complete dependency graph. It draws the important hexagonal relationships visible from package structure and type references.

## Troubleshooting

- If rendering fails because PlantUML is missing, keep the generated `.puml` and tell the user rendering needs PlantUML configured in `hive.yml` or available on PATH.
- If the diagram is empty, run `hive check --json` and inspect whether the expected source roots and packages exist.
- If the project is expected to generate code from blueprint YAML before diagramming, validate those files with `hive blueprint validate <file.yml> --json`; use `hive blueprint schema --json` for the installed blueprint contract.
- Do not move source files to make diagrams work. Fix generation or package violations through Hive conventions.

## Verify

For code changes that affect architecture or generated diagrams:

```bash
mvn -q verify
```
