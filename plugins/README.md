# Sinapsi agent plugins

This repository is a local marketplace for Codex and Claude Code. Skills are
split into installable plugins by prefix so users can load only the workflows
they need:

| Plugin | Included skills |
| --- | --- |
| `hive` | Hive MCP workflow, lifecycle roles, module integration skills, and Mermaid |
| `hive-arch` | `arch-*` architecture-pattern skills |
| `hive-ddd` | `ddd-*` domain-driven design skills |
| `hive-gof` | `gof-*` Gang of Four pattern skills |
| `hive-tdd` | `tdd-*` test-driven development skills |

## Install in Codex

From the repository root:

```text
codex plugin marketplace add .
codex
/plugins
```

Select the `sinapsi` marketplace and install the desired plugins.

## Install in Claude Code

From the repository root:

```text
claude plugin marketplace add .
claude plugin install hive@sinapsi
claude plugin install hive-arch@sinapsi
claude plugin install hive-ddd@sinapsi
claude plugin install hive-gof@sinapsi
claude plugin install hive-tdd@sinapsi
```

Install only the packs you need. The `hive` plugin declares a local Hive MCP
connection at `http://127.0.0.1:9090/mcp`; the server process must already be
running. It exposes the analyst, architect, developer, and reviewer roles as
portable skills and as native Claude Code subagents. The four software-design
packs are skills-only plugins.

## Attribution

The `arch-*`, `ddd-*`, `gof-*`, and `tdd-*` skills are adapted from
[`yonatankarp/software-design-skills`](https://github.com/yonatankarp/software-design-skills)
and retain its MIT license and copyright notice. The operational `hive` plugin
is part of the Hive project and remains licensed under Apache-2.0.
