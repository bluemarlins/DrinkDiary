# DrinkDiary S/W Architecture

## 1. 문서 목적

이 문서는 DrinkDiary의 전체 소프트웨어 구조를 MVVM + Repository 기준으로 정의한다.
현재 앱은 로컬 우선 Android 앱이며, 서버 동기화와 계정 기능은 포함하지 않는다.

## 2. 아키텍처 결정

DrinkDiary는 단일 모듈 MVVM + Repository 구조를 사용한다.

```text
UI(Compose)
  -> ViewModel
  -> UseCase
  -> Repository
  -> DAO(Room)
  -> Database
```

이 구조를 선택하는 이유:

- 현재 요구사항은 로컬 CRUD, 필터링, 기간별 집계가 중심이다.
- 단일 모듈로 시작하면 초기 구현 비용이 낮다.
- UI, 상태 관리, 비즈니스 규칙, 데이터 접근 책임을 분리할 수 있다.
- 향후 기능이 커지면 feature 모듈 또는 core 모듈로 분리하기 쉽다.

## 3. 패키지 구조 (현행)

기본 정보: 패키지 `com.bluemarlin.drinkdiary`, `minSdk 35`, `compileSdk`/`targetSdk 36`.
단일 모듈이며 의존 방향은 `UI -> ViewModel -> UseCase -> Repository -> DAO(Room) -> Database` 한 방향이다.

```text
com.bluemarlin.drinkdiary
  ├─ data
  │  ├─ local
  │  │  ├─ DrinkDiaryDatabase
  │  │  ├─ DrinkRecordDao
  │  │  └─ DrinkRecordEntity
  │  ├─ mapper
  │  │  └─ DrinkRecordMapper
  │  └─ repository
  │     ├─ DrinkRecordRepositoryImpl
  │     └─ UserPreferencesRepositoryImpl
  ├─ domain
  │  ├─ model
  │  │  ├─ DrinkRecord / DrinkType / CollectionStatus
  │  │  ├─ DashboardPeriod / DashboardSummary
  │  │  ├─ DrinkRecordFilter / DrinkRecordInput
  │  │  └─ InsightsSummary / DrinkRatingBreakdown
  │  ├─ repository
  │  │  ├─ DrinkRecordRepository
  │  │  └─ UserPreferencesRepository
  │  └─ usecase
  │     ├─ ObserveDrinkRecordsUseCase / ObserveDrinkRecordUseCase
  │     ├─ SaveDrinkRecordUseCase / DeleteDrinkRecordUseCase
  │     ├─ ObserveDashboardSummaryUseCase / ObserveSearchResultsUseCase
  │     ├─ ObserveInsightsUseCase
  │     ├─ GenerateCsvExportUseCase
  │     └─ CheckRecordLimitUseCase
  ├─ ui
  │  ├─ dashboard / collection / search
  │  ├─ detail / editor / insights / settings
  │  ├─ component (Components.kt — DD* 공용 컴포넌트)
  │  ├─ navigation
  │  └─ theme
  ├─ DrinkDiaryApplication (AppContainer)
  └─ MainActivity
```

### 3-1. 패키지별 책임 상세

- **`domain/model`** — 순수 Kotlin data class/enum. Android·Room 의존 금지.
- **`domain/usecase`** — UseCase당 클래스 1개. 입력 검증과 비즈니스 규칙은 ViewModel/UI가 아니라
  여기에 둔다. 예: 필수 입력·평점 범위 검증은 `SaveDrinkRecordUseCase`에, 무료 티어 기록 상한
  (`CheckRecordLimitUseCase.LIMIT`, `DrinkRecordRepository.observeRecordsCount()`와
  `UserPreferencesRepository.isProUser`를 결합)은 에디터 ViewModel이 아니라 UseCase에 둔다 —
  어느 화면에서 호출하든 규칙이 동일하게 적용되게 하기 위함이다.
- **`domain/repository`** — `DrinkRecordRepository`, `UserPreferencesRepository` 인터페이스.
- **`data/local`** — Room `DrinkDiaryDatabase`, `DrinkRecordDao`, `DrinkRecordEntity`. Enum은 ordinal이
  아니라 문자열로 저장해 스키마 가독성과 enum 재정렬 내성을 확보한다. 스키마 이력은 `app/schemas/`에
  내보낸다(`room.schemaLocation`). 엔티티/스키마 변경 시 반드시 새 마이그레이션을 추가하고
  (`DrinkDiaryDatabase.MIGRATION_1_2`, `MIGRATION_2_3` 참고) `AppContainer`에 등록한다.
- **`data/mapper`** — `DrinkRecordMapper`가 Entity ↔ 도메인 `DrinkRecord` 변환을 담당.
- **`data/repository`** — `DrinkRecordRepositoryImpl`은 DAO와 대화하는 유일한 지점이며 DB 실패를
  `AppResult`/`AppError`로 번역한다. `UserPreferencesRepositoryImpl`은 Jetpack DataStore 기반으로
  프리미엄 게이트에 쓰이는 `isProUser` 플래그를 보관한다.
- **`ui/<feature>`** — 화면당 패키지 1개. `*Screen.kt`(stateless Composable) + `*ViewModel.kt`(UI 상태
  노출, UseCase 호출). ViewModel은 DI 프레임워크(Hilt/Koin) 없이 손으로 쓴 `Factory`로 생성한다.
- **`ui/component/Components.kt`** — `DD*` 접두사 공용 Material 3 컴포넌트. 화면 로컬 일회성 UI를
  새로 만들기보다 이쪽을 재사용·확장한다. 전체 카탈로그와 사용 규칙은
  `../designer/design-system.md` 참고.
- **`ui/navigation/DrinkDiaryApp.kt`** — Navigation 3(`androidx.navigation3`) 기반. `NavController`
  없이 private sealed `AppRoute : NavKey` 계층과 수동 관리 `mutableStateListOf<AppRoute>` 백스택을
  쓴다. 최상위 라우트(Dashboard/Collection/Search)는 shared-axis, 드릴인 라우트
  (Detail/Editor/Insights/Settings)는 `NavDisplay.transitionSpec` 메타데이터로 정의한 슬라이드 전환을
  사용한다. 각 엔트리의 ViewModel은 라우트 인자로 스코프된 `key` 문자열(예: `"detail_${recordId}"`)로
  생성해 서로 다른 기록/편집이 독립 인스턴스를 갖게 한다.
- **의존성 배선** — `DrinkDiaryApplication`/`AppContainer`가 Room DB, DataStore 기반
  `UserPreferencesRepository`, 모든 repository/usecase 싱글턴을 수동으로 조립한다. 화면에서는
  `(LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer`로 가져온다.

## 4. 계층별 책임

| 계층 | 책임 |
| --- | --- |
| UI | Compose 화면 렌더링, 사용자 이벤트 전달, 내비게이션 요청 |
| ViewModel | UI 상태 생성, 이벤트 처리, UseCase 호출 |
| UseCase | 앱 규칙 처리, 입력 검증, 필터 조건 생성, 대시보드 요약 계산 |
| Repository | 데이터 소스 접근 추상화, Entity와 Domain Model 변환 |
| DAO | Room 쿼리 실행 |
| Database | 로컬 영속성 제공 |

## 5. Domain Model

```kotlin
enum class DrinkType {
    Wine,
    Whiskey,
    Beer
}

enum class CollectionStatus {
    Normal,
    Repurchase,
    NotForMe
}

enum class DashboardPeriod {
    Weekly,
    Monthly,
    Yearly
}

data class DrinkRecord(
    val id: Long,
    val type: DrinkType,
    val name: String,
    val imageUri: String?,
    val price: Long?,
    val place: String?,
    val tastingNote: String?,
    val rating: Int,
    val collectionStatus: CollectionStatus,
    val recordedAtMillis: Long
)

data class DrinkRecordFilter(
    val drinkType: DrinkType?,
    val collectionStatus: CollectionStatus?
)

data class DashboardSummary(
    val totalCount: Int,
    val averageRating: Double?,
    val wineCount: Int,
    val whiskeyCount: Int,
    val beerCount: Int,
    val repurchaseCount: Int,
    val notForMeCount: Int,
    val repurchaseRecords: List<DrinkRecord>,
    val notForMeRecords: List<DrinkRecord>
)
```

## 6. UseCase 설계

| UseCase | 역할 | 관련 기능 |
| --- | --- | --- |
| ObserveDrinkRecordsUseCase | 필터 조건에 맞는 컬렉션 목록을 Flow로 제공 | UC-02, UC-06, UC-07, UC-08 |
| ObserveDrinkRecordUseCase | 단일 기록 상세를 Flow로 제공 | UC-02, UC-03, UC-04 |
| SaveDrinkRecordUseCase | 신규 등록 및 수정 처리, 필수값 검증 | UC-01, UC-03 |
| DeleteDrinkRecordUseCase | 기록 삭제 처리 | UC-04 |
| ObserveDashboardSummaryUseCase | 기간별 컬렉션 요약 계산 | UC-05, UC-07, UC-08 |

필수값 검증은 `SaveDrinkRecordUseCase`에서 수행한다.
이유: UI가 바뀌어도 저장 규칙을 한곳에서 유지하기 위함이다.

## 7. ViewModel 설계

### DashboardViewModel

- 기간 선택 상태를 관리한다.
- 선택 기간 기준으로 `DashboardSummary`를 구독한다.
- 재구매 후보 카드와 비선호 카드 선택 이벤트를 내비게이션 이벤트로 전달한다.

### CollectionViewModel

- 주류 종류 필터와 컬렉션 상태 필터를 관리한다.
- 필터 변경 시 목록 Flow를 갱신한다.
- Empty 상태는 전체 Empty와 필터 결과 Empty를 구분할 수 있어야 한다.

### RecordDetailViewModel

- `recordId` 기준 상세 기록을 구독한다.
- 삭제 확인 이후 삭제 UseCase를 호출한다.
- 삭제 성공 시 이전 화면으로 돌아가기 위한 이벤트를 발생시킨다.

### RecordEditorViewModel

- 신규 등록과 수정 모드를 모두 처리한다.
- 수정 모드에서는 기존 기록을 불러와 입력 상태로 변환한다.
- 저장 시 필수값 검증 결과를 UI 상태로 노출한다.

## 8. 데이터 흐름

### 컬렉션 목록 조회

```text
CollectionScreen
  -> CollectionViewModel
  -> ObserveDrinkRecordsUseCase(filter)
  -> DrinkRecordRepository.observeRecords(filter)
  -> DrinkRecordDao.observeRecords(...)
  -> Flow<List<DrinkRecordEntity>>
  -> Mapper
  -> Flow<List<DrinkRecord>>
  -> CollectionUiState
```

### 기록 저장

```text
RecordEditorScreen
  -> RecordEditorViewModel
  -> SaveDrinkRecordUseCase(input)
  -> 입력 검증
  -> DrinkRecordRepository.save(record)
  -> DrinkRecordDao.insertOrUpdate(entity)
  -> 저장 결과
```

### 대시보드 요약 조회

```text
DashboardScreen
  -> DashboardViewModel
  -> ObserveDashboardSummaryUseCase(period)
  -> DrinkRecordRepository.observeRecordsByPeriod(startMillis, endMillis)
  -> 기록 수 / 평균 별점 / 종류별 수 / 재구매 후보 수 / 비선호 수 계산
  -> DashboardUiState
```

대시보드 요약은 초기에는 UseCase에서 계산한다.
이유: MVP 단계에서는 구현과 테스트가 단순하고, 기록이 많아질 때 DAO 집계 쿼리로 최적화할 수 있다.

## 9. 오류 처리 원칙

- Repository는 DB 예외를 앱 내부 오류 타입으로 변환한다.
- ViewModel은 오류를 사용자에게 보여줄 수 있는 UI 상태로 변환한다.
- UI는 기술 상세 대신 재시도 또는 입력 수정이 가능한 메시지를 표시한다.
- 삭제, 저장 실패 시 기존 데이터를 임의로 지우거나 화면 상태를 성공처럼 처리하지 않는다.

## 10. 테스트 범위

| 대상 | 테스트 내용 |
| --- | --- |
| SaveDrinkRecordUseCase | 필수값 검증, 별점 범위 검증, 컬렉션 상태 저장 |
| ObserveDrinkRecordsUseCase | 주류 종류 필터, 컬렉션 상태 필터, 복합 필터 |
| ObserveDashboardSummaryUseCase | 기간 필터링, 평균 별점, 재구매 후보 수, 비선호 수 |
| Mapper | Entity와 Domain Model 간 변환 |
| ViewModel | Loading, Empty, Success, Error 상태 전환 |
