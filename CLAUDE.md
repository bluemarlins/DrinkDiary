# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

DrinkDiary is a local-first, single-module Android app (Kotlin + Jetpack Compose) for logging personal
drink records (wine, whiskey, beer) with ratings, tasting notes, and a "would buy again / not for me"
collection status. No backend or sync — everything persists to a local Room database. The app has a
freemium tier: free users are capped at a fixed number of records and lose access to some screens/exports
until upgrading to "Pro" (state tracked locally, no real payment/backend integration yet).

Package: `com.bluemarlin.drinkdiary`. `minSdk 35`, `compileSdk`/`targetSdk 36`.

## Commands

All commands run from the repo root using the Gradle wrapper (PowerShell):

```powershell
.\gradlew.bat :app:assembleDebug        # build debug APK
.\gradlew.bat :app:testDebugUnitTest    # run JVM unit tests (app/src/test)
.\gradlew.bat :app:connectedDebugAndroidTest  # instrumented tests (app/src/androidTest), needs device/emulator
.\gradlew.bat :app:lint                 # Android Lint
.\gradlew.bat :app:ktlintCheck          # ktlint style check
.\gradlew.bat :app:ktlintFormat         # auto-fix ktlint violations
```

Run a single unit test class or method with `--tests`:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCaseTest"
.\gradlew.bat :app:testDebugUnitTest --tests "com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCaseTest.validation fails when rating is out of range"
```

Unit tests use Robolectric for anything touching Room/Android framework classes (e.g.
`DrinkRecordDaoTest`), which is why `testOptions.unitTests.isIncludeAndroidResources = true` is set in
`app/build.gradle.kts` — don't remove it.

Definition of done for any task (also enforced for the `agy` sub-agent, see below): unit tests pass,
`ktlintCheck` passes, `lint` passes, no out-of-scope file diff, and new UseCase/Mapper/ViewModel logic has
a corresponding unit test.

## Architecture

Single module, layered MVVM + Repository + UseCase. Dependencies flow one direction only:

```text
UI (Compose Screen) -> ViewModel -> UseCase -> Repository -> DAO (Room) -> Database
```

- **`domain/model`** — pure Kotlin data classes/enums (`DrinkRecord`, `DrinkType`, `CollectionStatus`,
  `DashboardPeriod`, `DashboardSummary`, `DrinkRecordFilter`, `DrinkRecordInput`, `InsightsSummary`,
  `DrinkRatingBreakdown`). No Android or Room dependency.
- **`domain/usecase`** — one class per use case (`SaveDrinkRecordUseCase`, `ObserveDrinkRecordsUseCase`,
  `ObserveDashboardSummaryUseCase`, `ObserveSearchResultsUseCase`, `ObserveInsightsUseCase`,
  `DeleteDrinkRecordUseCase`, `GenerateCsvExportUseCase`, `CheckRecordLimitUseCase`). Input validation and
  business rules live here, not in the ViewModel or UI — e.g. required-field and rating-range validation
  happens in `SaveDrinkRecordUseCase`, and the free-tier record cap (`CheckRecordLimitUseCase.LIMIT`,
  combines `DrinkRecordRepository.observeRecordsCount()` with `UserPreferencesRepository.isProUser`) lives
  here rather than in the editor ViewModel, so the rule stays enforced regardless of which screen calls it.
- **`domain/repository`** — `DrinkRecordRepository` and `UserPreferencesRepository` interfaces consumed by
  use cases.
- **`data/local`** — Room `DrinkDiaryDatabase`, `DrinkRecordDao`, `DrinkRecordEntity`. Enum fields are
  stored as strings (not ordinals) so schema stays readable and resilient to enum reordering. Room schema
  history is exported to `app/schemas/` (`room.schemaLocation` in `app/build.gradle.kts`) — add a new Room
  migration (see `DrinkDiaryDatabase.MIGRATION_1_2`, `MIGRATION_2_3`) whenever the entity/schema changes,
  and register it in `AppContainer`.
- **`data/mapper`** — `DrinkRecordMapper` converts between `DrinkRecordEntity` and the domain `DrinkRecord`.
- **`data/repository`** — `DrinkRecordRepositoryImpl` (the only place that talks to the DAO and translates
  DB failures into `AppResult`/`AppError`) and `UserPreferencesRepositoryImpl` (Jetpack DataStore-backed,
  holds the `isProUser` flag used for the freemium gate).
- **`ui/<feature>`** — one package per screen (`dashboard`, `collection`, `search`, `detail`, `editor`,
  `insights`, `settings`), each with a `*Screen.kt` (stateless Composables) and a `*ViewModel.kt` (exposes
  UI state, calls use cases). ViewModels are constructed via a small hand-written `Factory` (no
  Hilt/Koin/DI framework).
- **`ui/component/Components.kt`** — shared `DD*`-prefixed Material 3 components (`DDPrimaryButton`,
  `DDDestructiveButton`, `DDRatingInput`, `DDImagePicker`, `DDProUpgradeDialog`, `DDProLockOverlay`, dashboard
  cards, filters, badges, etc.). Prefer reusing/extending these over building screen-local one-offs; see
  `app/docs/design-system.md` for the full component catalog and usage rules (e.g. destructive actions
  always use `DDDestructiveButton`, at most one Primary button per screen; freemium gating always uses
  `DDProUpgradeDialog`/`DDProLockOverlay` rather than a bespoke paywall).
- **`ui/navigation/DrinkDiaryApp.kt`** — app-level navigation using **Navigation 3**
  (`androidx.navigation3`), with a private sealed `AppRoute : NavKey` hierarchy and a manually managed
  `mutableStateListOf<AppRoute>` back stack (no `NavController`). Top-level routes (Dashboard/Collection/
  Search) use a shared-axis transition; drill-in routes (Detail/Editor/Insights/Settings) use a slide
  transition defined via `NavDisplay.transitionSpec` metadata. ViewModels for each entry are created with a
  `key` string scoped to route args (e.g. `"detail_${recordId}"`) so distinct records/edits get independent
  ViewModel instances.
- **Dependency wiring** — `DrinkDiaryApplication`/`AppContainer` (`DrinkDiaryApplication.kt`) manually
  builds the Room database, the DataStore-backed `UserPreferencesRepository`, and all repository/use-case
  singletons; screens pull them via
  `(LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer`.
- **Error handling** — `AppResult<T>` (`Success`/`Failure`) and `AppError` (`NotFound`, `Storage`,
  `Validation(SaveDrinkRecordError)`) are the shared result/error types threaded from repository up
  through use cases into ViewModel UI state. Every major screen models Loading/Empty/Success/Error states
  explicitly instead of failing silently.

## Documentation layout

Work is organized into six virtual departments — Planner, Researcher, Designer, Developer, QA, and
Release/Compliance — defined in `app/docs/orchestration/persona-registry.artifact.md`. That file also
records which departments may be delegated to `agy` and which are Claude-only.

**Where a document goes is decided by its status, not by who wrote it:**

- **`app/docs/*.md` — confirmed specs.** The single source of truth, and they must stay in sync with the
  code. Mostly Korean: `software-architecture.md` (layer responsibilities, data flow diagrams),
  `database-design.md`, `design-system.md`, `usecase.md`, `ui-flow.md`, `navigation-flow-usecases.md`,
  `development-todo.md`, `product-plan.md` (business model and phase roadmap, including the
  freemium/Pro rationale).
- **`app/docs/departments/<dept>/` — that department's working output.** Research reports, strategy
  drafts, implementation plans, review memos. Kept as a record of how a decision was reached; carries no
  obligation to match the current code.
- **`app/docs/orchestration/` — agent operating rules.** `harness.md`, `agy-playbook.md`,
  `persona-registry.artifact.md`, and the live backlog `task-log.md`.

A draft is promoted from `departments/` into a confirmed spec only after the user signs off, and the
promotion is recorded in `task-log.md`. Never write a new planning or design document straight into
`app/docs/` — start it in the owning department's directory.

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
design, code review, and every commit/push judgement.

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

The shared code rules and Definition of Done live in `app/docs/orchestration/harness.md`; the exact
invocation templates and flags in `app/docs/orchestration/agy-playbook.md`; the department personas in
`app/docs/orchestration/persona-registry.artifact.md`. The live backlog is
`app/docs/orchestration/task-log.md`.

**Safety rule from a prior incident** (`harness.md` §5): a background `agy` call once reverted the entire
uncommitted working tree to the last commit while other uncommitted work was in progress, destroying it
(recovered only because the full file contents were still in the Claude conversation transcript). Treat
`agy`-written output as untrusted until reviewed and treat uncommitted changes as expendable in general:
- Commit verified work immediately — don't batch several features into one eventual commit.
- Never run more than one `agy` invocation concurrently (including backgrounded ones), especially while
  other uncommitted changes exist elsewhere in the tree.
- If an `agy` call reports "timeout"/"ERROR", check `git status` for actual working-tree state before
  deciding whether to retry — it may have written files despite the reported failure.

## Working conventions

- Respond to the user in Korean unless they explicitly ask for another language (per `AGENTS.md`).
- Keep Composables stateless/hoisted; put state and business logic in the ViewModel/UseCase layer, not in
  the Composable.
- New persisted fields or entity changes require a Room migration, not a destructive schema change.
- Prefer the existing `DD*` shared components over new bespoke UI; only add a new shared component when an
  existing one truly doesn't fit.
