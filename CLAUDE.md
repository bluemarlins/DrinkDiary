# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

DrinkDiary is a local-first, single-module Android app (Kotlin + Jetpack Compose) for logging personal
drink records (wine, whiskey, beer) with ratings, tasting notes, and a "would buy again / not for me"
collection status. No backend, accounts, or sync — everything persists to a local Room database.

Package: `com.bluemarlin.drinkdiary`. `minSdk 35`, `compileSdk`/`targetSdk 36`.

## Commands

All commands run from the repo root using the Gradle wrapper (PowerShell):

```powershell
.\gradlew.bat :app:assembleDebug        # build debug APK
.\gradlew.bat :app:testDebugUnitTest    # run JVM unit tests (app/src/test)
.\gradlew.bat :app:connectedDebugAndroidTest  # instrumented tests (app/src/androidTest), needs device/emulator
.\gradlew.bat :app:lint                 # Android Lint
```

Run a single unit test class or method with `--tests`:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCaseTest"
.\gradlew.bat :app:testDebugUnitTest --tests "com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCaseTest.validation fails when rating is out of range"
```

There is no ktlint/detekt config in the repo; `:app:lint` (Android Lint) is the only static check.

## Architecture

Single module, layered MVVM + Repository + UseCase. Dependencies flow one direction only:

```text
UI (Compose Screen) -> ViewModel -> UseCase -> Repository -> DAO (Room) -> Database
```

- **`domain/model`** — pure Kotlin data classes/enums (`DrinkRecord`, `DrinkType`, `CollectionStatus`,
  `DashboardPeriod`, `DashboardSummary`, `DrinkRecordFilter`, `DrinkRecordInput`). No Android or Room
  dependency.
- **`domain/usecase`** — one class per use case (`SaveDrinkRecordUseCase`, `ObserveDrinkRecordsUseCase`,
  `ObserveDashboardSummaryUseCase`, `ObserveSearchResultsUseCase`, `DeleteDrinkRecordUseCase`, etc.).
  Input validation and business rules live here, not in the ViewModel or UI — e.g. required-field and
  rating-range validation happens in `SaveDrinkRecordUseCase`, so the rule stays enforced regardless of
  which screen calls it.
- **`domain/repository`** — `DrinkRecordRepository` interface consumed by use cases.
- **`data/local`** — Room `DrinkDiaryDatabase`, `DrinkRecordDao`, `DrinkRecordEntity`. Enum fields are
  stored as strings (not ordinals) so schema stays readable and resilient to enum reordering. Room schema
  history is exported to `app/schemas/` (`room.schemaLocation` in `app/build.gradle.kts`) — add a new Room
  migration (see `DrinkDiaryDatabase.MIGRATION_1_2`, `MIGRATION_2_3`) whenever the entity/schema changes,
  and register it in `AppContainer`.
- **`data/mapper`** — `DrinkRecordMapper` converts between `DrinkRecordEntity` and the domain `DrinkRecord`.
- **`data/repository`** — `DrinkRecordRepositoryImpl`, the only place that talks to the DAO and translates
  DB failures into `AppResult`/`AppError`.
- **`ui/<feature>`** — one package per screen (`dashboard`, `collection`, `detail`, `editor`, `search`),
  each with a `*Screen.kt` (stateless Composables) and a `*ViewModel.kt` (exposes UI state, calls use
  cases). ViewModels are constructed via a small hand-written `Factory` (no Hilt/Koin/DI framework).
- **`ui/component/Components.kt`** — shared `DD*`-prefixed Material 3 components (`DDPrimaryButton`,
  `DDDestructiveButton`, `DDRatingInput`, `DDImagePicker`, etc.). Prefer reusing/extending these over
  building screen-local one-offs; see `app/docs/design-system.md` for the full component catalog and
  usage rules (e.g. destructive actions always use `DDDestructiveButton`, at most one Primary button per
  screen).
- **`ui/navigation/DrinkDiaryApp.kt`** — app-level navigation using **Navigation 3**
  (`androidx.navigation3`), migrated from the traditional Navigation Compose graph. Routes are a private
  sealed `AppRoute : NavKey` hierarchy with a manually managed `mutableStateListOf<AppRoute>` back stack
  (no `NavController`). Top-level routes (Dashboard/Collection/Search) use a shared-axis transition;
  drill-in routes (Detail/Editor) use a slide transition defined via `NavDisplay.transitionSpec` metadata.
  ViewModels for each entry are created with a `key` string scoped to route args (e.g.
  `"detail_${recordId}"`) so distinct records/edits get independent ViewModel instances.
- **Dependency wiring** — `DrinkDiaryApplication`/`AppContainer` (`DrinkDiaryApplication.kt`) manually
  builds the Room database and all repository/use-case singletons; screens pull them via
  `(LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer`.
- **Error handling** — `AppResult<T>` (`Success`/`Failure`) and `AppError` (`NotFound`, `Storage`,
  `Validation(SaveDrinkRecordError)`) are the shared result/error types threaded from repository up
  through use cases into ViewModel UI state. Every major screen models Loading/Empty/Success/Error states
  explicitly instead of failing silently.

Detailed design docs live under `app/docs/` (mostly in Korean): `software-architecture.md` (layer
responsibilities, data flow diagrams), `database-design.md`, `design-system.md`, `usecase.md`,
`ui-flow.md`, `navigation-flow-usecases.md`, `development-todo.md`.

## Working conventions

- Respond to the user in Korean unless they explicitly ask for another language (per `AGENTS.md`).
- Keep Composables stateless/hoisted; put state and business logic in the ViewModel/UseCase layer, not in
  the Composable.
- New persisted fields or entity changes require a Room migration, not a destructive schema change.
- Prefer the existing `DD*` shared components over new bespoke UI; only add a new shared component when an
  existing one truly doesn't fit.
