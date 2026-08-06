# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

DrinkDiary is a local-first Android app (Kotlin, Jetpack Compose, Material 3) for logging drinks (wine, whiskey, beer) and tracking which ones to rebuy or avoid, with weekly/monthly/yearly dashboard summaries. No accounts, no server sync — everything is stored on-device with Room.

Domain and UI text/labels are in Korean; source docs under `app/docs/` are also written in Korean.

## Commands

```
./gradlew :app:assembleDebug            # build debug APK
./gradlew :app:testDebugUnitTest        # run JVM unit tests
./gradlew :app:connectedDebugAndroidTest # run instrumented tests (needs device/emulator)
./gradlew buildAndInstall               # assembleDebug + install to connected device via adb
```

To run a single test class/method with Gradle, use `--tests`, e.g.:
```
./gradlew :app:testDebugUnitTest --tests "com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCaseTest"
```

## Architecture

Single-module MVVM + Repository + UseCase, one-directional data flow:

```
UI (Compose) -> ViewModel -> UseCase -> Repository -> DAO (Room) -> Database
```

- `data/local` — `DrinkDiaryDatabase`, `DrinkRecordDao`, `DrinkRecordEntity` (Room; enums stored as strings)
- `data/mapper` — Entity <-> domain model conversion (`DrinkRecordMapper`)
- `data/repository` — `DrinkRecordRepositoryImpl`, converts DB exceptions into app-level errors
- `domain/model` — `DrinkRecord`, `DrinkType`, `CollectionStatus`, `DashboardPeriod`, `DashboardSummary`, `DrinkRecordFilter`, `DrinkRecordInput`, `AppResult`/`AppError`/`SaveDrinkRecordError`
- `domain/repository` — `DrinkRecordRepository` interface
- `domain/usecase` — one class per use case (`SaveDrinkRecordUseCase`, `DeleteDrinkRecordUseCase`, `ObserveDrinkRecordsUseCase`, `ObserveDrinkRecordUseCase`, `ObserveDashboardSummaryUseCase`)
- `ui/<feature>` (`dashboard`, `collection`, `detail`, `editor`) — each has a `*ViewModel` + `*Screen`
- `ui/navigation` — `DrinkDiaryApp` (nav host/routes) and `DDScreenScaffold` (shared Scaffold/TopAppBar/BottomNav)
- `ui/component`, `ui/theme` — shared Compose building blocks and theme tokens

No DI framework (Hilt intentionally deferred). Dependencies are wired manually in `AppContainer` (in `DrinkDiaryApplication.kt`), which constructs the Room database, repository, and all use cases; `MainActivity` reads `appContainer` off the `Application`.

Business rules live in UseCases, not in Composables or the Repository:
- **Validation** (required fields, rating 1-5, price >= 0, blank-name rejection) happens in `SaveDrinkRecordUseCase`, so the rule stays centralized regardless of which screen calls it.
- **Dashboard aggregation** (counts, average rating, per-type breakdown, rebuy/not-for-me counts) is currently computed in `ObserveDashboardSummaryUseCase` over the period's records rather than via a DAO aggregate query — intentional for MVP simplicity; revisit with DAO-level `COUNT`/`AVG`/`GROUP BY` if record volume grows.
- Repository converts DB/query exceptions into `AppError`; ViewModels turn `AppError` into user-facing UI state (Loading/Empty/Success/Error) — raw technical errors are never shown to the user.

Room schema history is exported to `app/schemas/` (JSON per version) — bump the DB version and add a `Migration` in `DrinkDiaryDatabase.kt` (wired into `AppContainer`) for any schema change; don't edit exported schema JSON by hand.

## Reference docs (`app/docs/`, Korean)

Docs are organized by department under `app/docs/`: `product/` (requirements/roadmap), `design/` (component system/UI plans), `dev/` (architecture/data), `research/` and `marketing/` (added as the Play Store launch effort produces them). `app/docs/service-launch-roadmap.md` is the master roadmap tying these together — check its Phase tracking table for current status before starting new launch-prep work.

Read the relevant doc before changing behavior in that area — these define the actual product rules, not just background:
- `product/usecase.md` — the 8 use cases (UC-01..UC-08) and their flows/exceptions
- `dev/software-architecture.md` — layer responsibilities and data-flow diagrams this section summarizes
- `dev/database-design.md` — entity/index rationale and migration considerations
- `design/design-system.md` — canonical list of `DD*` components, when to use each, and per-screen composition (component reuse is preferred over ad hoc screen-local UI)
- `design/scaffold-toolbar-plan.md` — in-progress plan to move `DDScreenScaffold`/`DDTopAppBar` to enum-based `screenType`/`selectedTab` (`DDScreenType`, `DDTopLevelTab`) instead of string tags and to use TopAppBar's proper `navigationIcon`/`actions` slots; current code (`ui/navigation/DDScreenScaffold.kt`) still uses raw string tabs (`"dashboard"`/`"collection"`) and a back button built inside the title slot — check this doc before touching that file
- `product/development-todo.md` — the step-by-step build plan (Step 01-15) the codebase was built from, including the fixed MVP decisions (rating 1-5, single image URI, free-text place, monthly default dashboard period, etc.). Steps 01-13 are done; 14 (UX polish) and 15 (test coverage) are not.

## AI orchestration for the launch effort

For the Play Store launch/monetization initiative tracked in `app/docs/service-launch-roadmap.md`, Claude acts as orchestrator: research and image-generation tasks are delegated to the `agy` CLI (invoked directly via Bash, e.g. `agy -p "<prompt>" --model gemini-3.6-flash-high`), while code changes, design-system structuring, and merging all outputs stay with Claude. `agy` is a multi-model CLI (not the `gemini` npm package) — always pass `--model gemini-*` explicitly since it defaults to other models otherwise.

## Language

Respond to the user in Korean (한국어) when working in this repository.

## Conventions

- Wide screens (>= 840dp) get a `NavigationRail` instead of bottom nav — see `DDScreenScaffold`.
- Photos are stored as URI strings only (`imageUri`), never binary blobs in the DB; image load/permission failures must fall back to a placeholder without discarding the record.
- Card/button corner radius stays at 8dp or below per the design system.
- Prefer composing existing `DD*` components (`ui/component/**`) over new one-off UI.
