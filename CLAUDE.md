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

Detailed design docs live under `app/docs/` (mostly in Korean): `software-architecture.md` (layer
responsibilities, data flow diagrams), `database-design.md`, `design-system.md`, `usecase.md`,
`ui-flow.md`, `navigation-flow-usecases.md`, `development-todo.md`, `product-plan.md` (business model
and phase roadmap, including the freemium/Pro rationale).

## Multi-agent workflow

Feature work can be split between Claude (planning, architecture, review, tests) and the `agy` CLI
(Google Antigravity, invoked non-interactively as a coding sub-agent for benchmarking research, UI
polish, and boilerplate code generation). The shared rules every agent must follow, the Definition of
Done, and the exact `agy` invocation templates live in `app/docs/orchestration/harness.md` and
`app/docs/orchestration/agy-playbook.md`. The live backlog is `app/docs/orchestration/task-log.md`.

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
