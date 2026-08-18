---
name: hive-cli-project
description: Initialize, check, inspect, and verify Hive CLI projects. Use when the user asks to bootstrap a Hive project, run `hive init`, validate structure, inspect Hive metadata, add ArchUnit rules, or diagnose project setup.
---

# Hive CLI project lifecycle

Use this skill for project setup and project-level checks.

## Resolve the CLI

Use `hive` if available. Otherwise use `./hive` from the project or toolkit root. Inside the Hive toolkit repo, build it with:

```bash
mvn -q -pl hive-cli -am package
```

## Initialize

For a new project:

```bash
hive init --readme --json
```

If the user supplied a base package, update `hive.yml` after `hive init`:

```yaml
basePackage: com.acme.app
layout: single
javaSourceRoot: src/main/java
testSourceRoot: src/test/java
```

Do not hand-create `.hive-project`, `hive.yml`, `pom.xml`, or source roots unless the CLI is unavailable and the user explicitly accepts manual fallback.

## Check and inspect

Use:

```bash
hive check --json
hive inspect config --json
hive inspect model --json
hive inspect generated --json
```

`hive check` exit codes:

```text
0  project structure looks good
1  project found, but expected files or directories are missing
2  no .hive-project marker was found
```

## Architecture test

Add Hive ArchUnit rules with:

```bash
hive create archtest --json
```

Use `--force` only when replacing an existing generated architecture test is intentional.

## Verify

After setup or fixes:

```bash
mvn -q verify
```

If dependencies are missing in a local snapshot workflow, tell the user to run `mvn -q install` in the Hive toolkit repo, or do it when that repo is the current workspace.
