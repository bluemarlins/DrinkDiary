# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Read AGENTS.md first

The operating rules for this repository live in **`AGENTS.md`** at the repo root. Read it now if it is
not already in context — it is the single source of truth, shared by every agent (Claude Code, `agy`,
or any other model), so that the same rules apply regardless of which one is driving.

`AGENTS.md` covers the project summary, response language, the documentation layout, the six virtual
departments, when work may be delegated to `agy`, the model roster, and the safety rules learned from a
prior data-loss incident.

Nothing is restated here. Duplicating those rules in two files is how they drift apart, and this
repository has already been bitten by exactly that — see the spec conflicts recorded in
`app/docs/orchestration/task-log.md`.

## Where the rest lives

| Looking for | Read |
| --- | --- |
| Code rules every agent follows, and the Definition of Done | `app/docs/orchestration/harness.md` |
| Build / test / lint commands, Robolectric setup | `app/docs/specs/developer/build-and-test.md` |
| Package structure, layer responsibilities, data flow | `app/docs/specs/developer/software-architecture.md` |
| `DD*` component catalog and usage rules | `app/docs/specs/designer/design-system.md` |
| Product scope, business model, roadmap | `app/docs/specs/planner/prd.md` |
| `agy` invocation templates and flags | `app/docs/orchestration/agy-playbook.md` |
| Live backlog | `app/docs/orchestration/task-log.md` |
