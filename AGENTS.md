---
description: Shared operating rules for every AI agent working in this repository.
alwaysApply: true
---

# Agent Rules

This is the single source of truth for how any AI agent — Claude Code, `agy` (Google Antigravity),
or anything else — works in this repository. `CLAUDE.md` points here rather than restating it, so the
same rules apply whichever model is driving.

This file covers **governance**: how work is divided, where documents go, and how agents behave.
It deliberately contains no Gradle commands, package layouts, or Android coding rules — those live in
the owning department's documents:

| Looking for | Read |
| --- | --- |
| Code rules every agent must follow (architecture, UI, style, error handling, testing, security, dependencies) | `app/docs/orchestration/harness.md` |
| Definition of Done | `app/docs/orchestration/harness.md` §4 |
| Build / test / lint commands, Robolectric setup | `app/docs/specs/developer/build-and-test.md` |
| Package structure, layer responsibilities, data flow | `app/docs/specs/developer/software-architecture.md` |
| `DD*` component catalog and usage rules | `app/docs/specs/designer/design-system.md` |
| Product scope, business model, roadmap | `app/docs/specs/planner/product-plan.md` |
| `agy` invocation templates and flags | `app/docs/orchestration/agy-playbook.md` |
| Department personas and who may be delegated to | `app/docs/orchestration/persona-registry.artifact.md` |
| Live backlog | `app/docs/orchestration/task-log.md` |

## Project

DrinkDiary (user-facing brand: **테이스트 아카이브 / Taste Archive**) is a local-first, single-module
Android app for logging personal drink records (wine, whiskey, beer) with ratings, tasting notes, and a
"would buy again / not for me" collection status. No backend or sync — everything persists locally. A
freemium tier caps free users at a fixed number of records and gates some features behind "Archive Pro"
(state tracked locally; no real payment integration yet).

Do not invent product behavior, domain models, or feature requirements that are not in the specs under
`app/docs/specs/`. If a requirement is missing, say so rather than filling the gap silently.

## Language and response style

- Respond to the user in Korean unless they explicitly ask for another language.
- Be concise, practical, and implementation-oriented. When explaining a technical decision, give the
  reason briefly.
- When providing code, name the file and its package location. Separate each file clearly when a change
  spans several. When modifying existing code, say briefly what changed.
- If requirements are ambiguous, make the safest reasonable assumption and state that you made it.
- Report outcomes honestly: if a gate was not run, say it was not run; do not describe unverified work
  as verified.

## Documentation layout

Work is organized into six virtual departments — Planner, Researcher, Designer, Developer, QA, and
Release/Compliance — defined in `app/docs/orchestration/persona-registry.artifact.md`. That file also
records which departments may be delegated to `agy` and which are Claude-only.

Documents are filed on two axes: **status** picks the top-level directory, **owning department** picks
the subdirectory.

- **`app/docs/specs/<dept>/` — confirmed specs.** The single source of truth; these must stay in sync
  with the code. Mostly Korean.
  - `specs/planner/` — `usecase.md`, `product-plan.md`
  - `specs/designer/` — `ui-flow.md`, `design-system.md`, `branding.md`, icon assets under `assets/`
  - `specs/developer/` — `software-architecture.md`, `database-design.md`, `build-and-test.md`
  - `specs/qa/` — `navigation-flow-usecases.md`
- **`app/docs/departments/<dept>/` — that department's working output.** Research reports, strategy
  drafts, implementation plans, review memos. Kept as a record of how a decision was reached; carries no
  obligation to match the current code.
- **`app/docs/orchestration/` — agent operating rules only.** `harness.md`, `agy-playbook.md`,
  `persona-registry.artifact.md`, and the live backlog `task-log.md`. Product content never lives here;
  if a document describes the product rather than how agents work, it belongs in `specs/` or
  `departments/`.

`task-log.md` is the only live backlog. The historical MVP checklist now sits at
`departments/developer/development-todo.md` as a completed record — do not add new work there.

A draft is promoted from `departments/<dept>/` into `specs/<dept>/` only after the user signs off, and
the promotion is recorded in `task-log.md`. Never write a new planning or design document straight into
`specs/` — start it in the owning department's directory under `departments/`.

## Multi-agent workflow

Feature work can be split between Claude and the `agy` CLI (Google Antigravity, invoked
non-interactively as a coding sub-agent). The division is deliberate: **agy is hands, Claude is head.**
agy fills in work whose shape is already decided; anything that decides something stays with Claude.

Delegate to `agy` only when all four hold:

1. The spec is already fixed — agy implements a decision, it never makes one.
2. Correctness is machine-checkable (`ktlintCheck` / `lint` / unit tests).
3. The edit scope narrows to a subtree that `--add-dir` can fence off.
4. Throwing the result away and redoing it costs little.

Keep with Claude: architecture and business decisions, Room migrations, multi-layer refactors, test
design, code review, and every commit/push judgement. QA verification is Claude-only — letting agy
verify its own output would be marking its own homework.

**Model roster — only these four may be used with `agy`:**

| Model | Role |
| --- | --- |
| `gemini-3.5-flash-medium` | Structured, repetitive work (pattern-copy CRUD use cases, mappers, formatted reports) |
| `gemini-3.6-flash-high` | Visual, subjective, generative work (UI polish, copy, naming, `generate_image` assets) |
| `gemini-3.1-pro-high` | Heavy implementation of an already-specified multi-step algorithm |
| `claude-sonnet-4-6` | Final escalation tier when the Gemini models repeatedly fail |

Every other model `agy models` lists is out of roster — see `harness.md` §6-2 for why, and change that
table first if the roster needs to change. Two consecutive Definition-of-Done failures escalate to the
next tier rather than retrying the same model; failing at `claude-sonnet-4-6` means the task should
never have been delegated, so Claude writes it directly.

**Safety rule from a prior incident** (`harness.md` §5): a background `agy` call once reverted the entire
uncommitted working tree to the last commit while other uncommitted work was in progress, destroying it
(recovered only because the full file contents were still in the conversation transcript). Treat
`agy`-written output as untrusted until reviewed, and treat uncommitted changes as expendable:

- Commit verified work immediately — don't batch several features into one eventual commit.
- Never run more than one `agy` invocation concurrently (including backgrounded ones), especially while
  other uncommitted changes exist elsewhere in the tree.
- If an `agy` call reports "timeout"/"ERROR", check `git status` for the actual working-tree state before
  deciding whether to retry — it may have written files despite the reported failure.

Remote `push` requires the user's confirmation each time. Approval for autonomous local iteration never
implies approval to push.
