# Hive — Codex and Claude Code plugin

Connects to the Hive MCP server to analyze, model, generate, and validate Java
projects with hexagonal architecture. It also provides a backlog-driven feature
workflow, five portable role skills, and matching Claude Code subagents.

## MCP connection

The plugin declares the same local Streamable HTTP server for both hosts:

- `mcp.json`: portable Agent Plugins configuration used by Codex;
- `.mcp.json`: Claude Code compatibility configuration.

Both connect to `http://127.0.0.1:9090/mcp` without authentication. The plugin
configures the connection but does not start or package the Java server. Start
Hive MCP before using its tools:

```text
mvn -q -pl hive-mcp spring-boot:run
```

The loopback endpoint is intended for local Codex and Claude Code sessions. A
cloud installation needs a separately deployed HTTPS endpoint.

See the plugin's [Privacy](./PRIVACY.md) notice for data-handling boundaries.

## Feature and backlog workflow

- `hive-backlog` configures Local, GitHub, GitLab, or Jira as the work-item
  provider, maps states, preserves traceability, and guards remote writes;
- `hive-feature` takes one work item through only the necessary analysis,
  architecture, Platform, development, review, and delivery gates.

Claude Code also discovers the `hive-feature` orchestrator under `agents/`.
Codex can invoke the same workflow directly as a skill. Provider credentials
are never packaged: external backlog operations require an available
authenticated connector, MCP server, or client.

## Roles

The canonical instructions live in portable skills, so both Codex and Claude
Code can use them:

- `hive-analyst`: requirements and domain analysis;
- `hive-architect`: architecture and technical decisions;
- `hive-platform`: development environments, infrastructure services, CI,
  pull-request controls, deployment, and operational readiness;
- `hive-developer`: implementation and verification;
- `hive-reviewer`: evidence-based code review.

Claude Code additionally discovers the matching agents under `agents/`. Each
agent preloads its canonical role skill, avoiding duplicated instructions.
Codex invokes the same roles as skills, automatically when relevant or by name.

## Module skills

The plugin also provides focused, portable integration guidance:

- `hive-core`: base use-case, result, domain, event, and mapper contracts;
- `hive-port`: shared technical output ports;
- `hive-adapter`: lightweight default adapter implementations;
- `hive-validator`: framework-free command validation and Java SPI;
- `jakarta-hive-validator`: Jakarta Bean Validation adapter and runtime wiring.

These are skills in both Codex and Claude Code; they are not separate subagents.

## MCP workflow

The `hive` skill is the authoritative agent workflow and safety policy for
these MCP capabilities. Live server schemas remain authoritative for tool
parameters and result shapes:

- project selection and inspection;
- legacy source indexing and relation queries;
- structured domain-model validation and persistence;
- deterministic domain, application, and infrastructure generation;
- Maven dependency and OSV analysis;
- project and architecture verification.

The MCP server process must be running before using the skill. Codex normally
exposes its tools as `mcp__hive__*`; Claude Code scopes the plugin server under
`mcp__plugin_hive_hive__*`. Operations return exact generated paths and
validation results.

## Install in Claude Code

```text
/plugin marketplace add Sinapsi-Technologies/hive
/plugin install hive@sinapsi
```

## Install in Codex

From the repository root, register the marketplace and install `hive` from the
`sinapsi` source in `/plugins`:

```text
codex plugin marketplace add .
codex
/plugins
```

## Usage

```text
Select /Users/cristian/projects/todo with Hive MCP,
validate the domain model, and generate the aggregate and use case.
```
