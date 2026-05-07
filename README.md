# DrinkDiary

DrinkDiary is a local-first Android application for building a personal drink collection. It helps users record what they drank, review their own tasting history, and distinguish drinks they would buy again from drinks that were not a good fit.

The app focuses on three drink categories: wine, whiskey, and beer. Each record can include a photo, name, price, place, tasting note, rating, collection status, and recorded date.

## Core Features

- Create, view, edit, and delete drink records.
- Classify records as normal, repurchase candidates, or not-for-me.
- Filter the collection by drink type and collection status.
- Review weekly, monthly, and yearly dashboard summaries.
- Track total records, average rating, drink type distribution, repurchase candidates, and not-for-me records.
- Continue writing records even when photo selection fails or is skipped.

## Main Screens

### Dashboard

The dashboard provides a quick summary of the user's drink collection for a selected period. It shows record counts, average rating, drink type ratio, repurchase candidates, and not-for-me records.

The default dashboard period is monthly because it provides a practical balance between short-term feedback and enough data for meaningful summaries.

### Collection

The collection screen lists saved drink records in reverse chronological order. Users can combine drink type filters with collection status filters, such as `Whiskey + Repurchase` or `Beer + Not For Me`.

### Record Detail

The detail screen shows the full content of a single drink record, including image, drink type, rating, collection status, price, place, tasting note, and recorded date. Edit and delete actions are available from this screen.

### Record Editor

The editor supports both new record creation and existing record updates. Required fields are drink type, name, rating, collection status, and recorded date. Optional fields include photo, price, place, and tasting note.

## Product Scope

DrinkDiary is designed as an offline-capable personal collection app. The current scope does not include user accounts, cloud synchronization, external drink databases, social sharing, recommendation systems, purchase links, or inventory management.

## Architecture

DrinkDiary uses a single-module MVVM architecture with Repository and UseCase layers.

```text
UI (Jetpack Compose)
  -> ViewModel
  -> UseCase
  -> Repository
  -> DAO (Room)
  -> Database
```

This structure keeps UI rendering, state management, business rules, and data access separate while staying simple enough for the MVP.

## Data Model

The main domain model is a drink record with the following fields:

- Drink type: `Wine`, `Whiskey`, or `Beer`
- Name
- Optional image URI
- Optional price
- Optional place
- Optional tasting note
- Rating from 1 to 5
- Collection status: `Normal`, `Repurchase`, or `NotForMe`
- Recorded date and time

Room is used for local persistence because drink records are structured data and need filtering, sorting, and period-based dashboard aggregation. Enum values are stored as strings to keep database values readable and resilient to enum ordering changes.

## UI and Design

The UI is built with Jetpack Compose and Material 3. Screens render explicit loading, empty, success, and error states. Shared UI components are preferred over screen-specific implementations to keep the experience consistent.

The navigation adapts to screen width:

- Compact width: bottom navigation
- Medium width: bottom navigation or navigation rail depending on available space
- Expanded width: navigation rail

This supports phones, tablets, foldables, landscape mode, and multi-window environments.

## Technology Stack

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Lifecycle and ViewModel
- Kotlin Coroutines and Flow
- Room
- Navigation Compose
- Gradle Kotlin DSL

## Validation

The app should be validated with:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Important behavior to verify includes record CRUD, validation errors, collection filtering, dashboard aggregation, adaptive navigation, and editor usability when the keyboard is visible.

## Documentation

Detailed planning documents are available under `app/docs`:

- `usecase.md`
- `ui-flow.md`
- `software-architecture.md`
- `database-design.md`
- `design-system.md`
- `development-todo.md`
- `scaffold-toolbar-plan.md`
