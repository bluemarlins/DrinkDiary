# DrinkDiary Application Development Todo

## 1. 문서 목적

이 문서는 `app/docs`의 유스케이스, UI 흐름, 소프트웨어 아키텍처, 데이터베이스 설계, 디자인 시스템 문서를 기준으로 DrinkDiary Android 앱 개발 Todo를 정의한다.

각 Step은 AI Agent가 독립적으로 수행하기 쉬운 크기로 나누었다. 선행 Step의 산출물을 다음 Step이 사용할 수 있도록 도메인, 데이터, UI 기반, 화면 구현, 통합 검증 순서로 진행한다.

## 2. MVP 구현 범위

- 로컬 우선 Android 앱
- Kotlin, Jetpack Compose, Material 3
- 단일 모듈 MVVM + Repository + UseCase 구조
- Room 기반 주류 기록 저장
- 주류 종류: 와인, 위스키, 맥주
- 컬렉션 상태: 일반 기록, 재구매 후보, 비선호
- 화면: Dashboard, Collection, RecordDetail, RecordEditor
- 기능: 기록 등록, 조회, 수정, 삭제, 필터링, 기간별 대시보드 요약

## 3. 구현 전 고정 결정

아래 항목은 기존 문서의 권장값을 MVP 기본값으로 사용한다.

| 항목 | 결정 |
| --- | --- |
| 별점 범위 | 1~5점 |
| 사진 개수 | 단일 이미지 URI |
| 장소 입력 | 자유 텍스트 |
| 가격 | 구매 또는 소비 가격을 구분하지 않는 선택 입력 |
| 별점과 컬렉션 상태 | 독립 입력 |
| 대시보드 기본 기간 | 연간 |
| 대시보드 지표 | 기록 수, 평균 별점, 종류별 수/비중, 재구매 후보 수, 비선호 수 |

## 4. Step별 Todo

### Step 01. 프로젝트 의존성 및 빌드 기반 구성

**목표**

Room, Navigation Compose, ViewModel Compose 등 MVP 구현에 필요한 AndroidX 기반 의존성을 추가한다.

**Agent 작업 범위**

- `gradle/libs.versions.toml`에 필요한 라이브러리와 플러그인 추가
- `app/build.gradle.kts`에 KSP, Room, Navigation Compose, Lifecycle ViewModel Compose 의존성 연결
- Room schema export 위치 설정 검토
- Java/Kotlin 타깃과 Compose 설정이 현재 AGP/Kotlin 버전과 충돌하지 않는지 확인

**완료 기준**

- Gradle sync 가능한 구성
- `./gradlew :app:assembleDebug` 실행 가능

**주의 사항**

- 불필요한 서드파티 DI 라이브러리는 추가하지 않는다.
- Hilt는 MVP 필수 요건이 아니므로 수동 의존성 제공 또는 간단한 AppContainer부터 시작한다.

---

### Step 02. Domain Model 및 공통 결과 타입 구현

**목표**

앱 전 계층에서 사용할 타입 안전한 도메인 모델과 입력 모델을 만든다.

**Agent 작업 범위**

- `domain/model` 패키지 생성
- `DrinkType`, `CollectionStatus`, `DashboardPeriod` enum 구현
- `DrinkRecord`, `DrinkRecordFilter`, `DashboardSummary` data class 구현
- 저장 입력 전용 모델 구현
  - 예: `DrinkRecordInput`
- 저장 검증 오류 모델 구현
  - 예: `SaveDrinkRecordError`, `FieldValidationError`
- 사용자에게 직접 노출하지 않을 내부 오류 타입 정의

**완료 기준**

- UI와 데이터 계층이 문자열 대신 도메인 타입을 참조할 수 있음
- 별점, 이름, 가격 등 검증 대상 필드를 표현할 수 있음

**테스트 후보**

- enum DB 문자열 매핑에 사용할 안정적인 name/value 확인
- 필수 필드 누락을 표현할 수 있는 오류 모델 확인

---

### Step 03. Room 데이터 계층 구현

**목표**

로컬 저장소의 Entity, DAO, Database를 구현한다.

**Agent 작업 범위**

- `data/local/DrinkRecordEntity.kt` 구현
- `data/local/DrinkRecordDao.kt` 구현
- `data/local/DrinkDiaryDatabase.kt` 구현
- 문서의 인덱스 적용
- `observeRecords`, `observeRecord`, `observeRecordsByPeriod`, `upsert`, `deleteById` 구현

**완료 기준**

- Room 컴파일 성공
- `drink_records` 테이블 version 1 생성 가능
- Flow 기반 조회 API 제공

**테스트 후보**

- DAO insert/update/delete
- 최신순 정렬
- 주류 종류와 컬렉션 상태 복합 필터
- 기간 조건 조회

---

### Step 04. Mapper 및 Repository 구현

**목표**

Room Entity와 Domain Model 변환 책임을 분리하고 Repository API를 제공한다.

**Agent 작업 범위**

- `data/mapper/DrinkRecordMapper.kt` 구현
- `domain/repository/DrinkRecordRepository.kt` 정의
- `data/repository/DrinkRecordRepositoryImpl.kt` 구현
- Entity 문자열 값과 enum 간 변환 처리
- DB 예외를 앱 내부 오류로 변환하는 최소 정책 적용

**완료 기준**

- UI/ViewModel이 DAO를 직접 참조하지 않음
- Repository에서 Flow Domain Model을 제공
- 잘못된 enum 문자열에 대한 방어 로직 존재

**테스트 후보**

- Entity to Domain 변환
- Domain to Entity 변환
- Repository 필터 인자 변환

---

### Step 05. UseCase 구현

**목표**

앱 규칙, 저장 검증, 기간별 집계 로직을 UseCase로 분리한다.

**Agent 작업 범위**

- `ObserveDrinkRecordsUseCase`
- `ObserveDrinkRecordUseCase`
- `SaveDrinkRecordUseCase`
- `DeleteDrinkRecordUseCase`
- `ObserveDashboardSummaryUseCase`
- 기간 계산 유틸 구현
  - 주간, 월간, 연간의 시작/종료 millis 계산
- 저장 검증 구현
  - 이름 공백 금지
  - 별점 1~5
  - 가격 0 이상
  - 필수값 누락 방지

**완료 기준**

- 유스케이스가 Repository만 의존
- 저장 실패 사유를 UI가 필드 단위로 표시할 수 있음
- 대시보드 요약이 기간 내 기록만 기준으로 계산됨

**테스트 후보**

- 필수값 검증
- 별점 범위 검증
- 가격 음수 검증
- 평균 별점 계산
- 주류 종류별 수 계산
- 재구매 후보/비선호 목록 추출
- 월간 기본 기간 계산

---

### Step 06. 앱 의존성 제공 및 MainActivity 연결

**목표**

ViewModel과 UseCase가 사용할 의존성을 앱 시작 시 구성한다.

**Agent 작업 범위**

- `DrinkDiaryApplication` 또는 간단한 `AppContainer` 구현
- Database, DAO, Repository, UseCase 생성
- `AndroidManifest.xml`에 Application 등록 여부 검토
- `MainActivity`에서 `DrinkDiaryApp` 진입점 구성

**완료 기준**

- 앱 실행 시 의존성 생성 가능
- ViewModel Factory 또는 생성 패턴이 화면에서 재사용 가능

**주의 사항**

- DI 프레임워크 추가는 보류한다. 이유: 현재 단일 모듈 MVP에서는 수동 구성으로 충분하다.

---

### Step 07. 디자인 시스템 Foundation 및 공통 컴포넌트 구현

**목표**

화면 구현 전에 재사용 가능한 Compose UI 기반을 만든다.

**Agent 작업 범위**

- `ui/theme`
  - `DrinkDiaryTheme`
  - 색상, 타이포그래피, spacing, shapes 정리
- `ui/component/action`
  - `DDPrimaryButton`, `DDSecondaryButton`, `DDContainedButton`, `DDDestructiveButton`, `DDAddRecordFab`
- `ui/component/feedback`
  - `DDLoadingContent`, `DDEmptyContent`, `DDErrorContent`, `DDConfirmDialog`
- Preview 작성

**완료 기준**

- 모든 주요 화면에서 공통 버튼/상태 UI 재사용 가능
- Material 3 기반
- 카드/버튼 radius는 8dp 이하 기준 유지

**테스트 후보**

- Compose Preview 빌드
- 간단한 UI 테스트 또는 screenshot 검토

---

### Step 08. 입력, 선택, 도메인 표시 컴포넌트 구현

**목표**

RecordEditor, Collection, Detail, Dashboard에서 공통으로 쓸 입력 및 표시 컴포넌트를 만든다.

**Agent 작업 범위**

- `ui/component/input`
  - `DDTextField`
  - `DDNumberField`
  - `DDMultilineTextField`
  - `DDDateTimeField`
  - `DDImagePicker`
  - `DDRatingInput`
  - `DDFormSection`
  - `DDFormErrorText`
- `ui/component/selection`
  - `DDDrinkTypeSelector`
  - `DDDrinkTypeFilter`
  - `DDCollectionStatusSelector`
  - `DDCollectionStatusFilter`
  - `DDPeriodSegmentedControl`
- `ui/component/domain`
  - `DDDrinkTypeBadge`
  - `DDCollectionStatusBadge`
  - `DDRatingStars`
  - `DDPriceText`
  - `DDRecordedDateText`

**완료 기준**

- 입력 컴포넌트는 가능한 stateless
- 필수 선택값은 정해진 enum만 허용
- 이미지 선택 실패 시 사진 없이 계속 진행 가능한 구조

**주의 사항**

- 사진은 DB에 바이너리 저장하지 않고 URI만 전달한다.
- Android Photo Picker 사용을 우선 검토한다.

---

### Step 09. Navigation 및 공통 화면 Scaffold 구현

**목표**

Dashboard, Collection, Detail, Editor 화면 간 이동 구조를 만든다.

**Agent 작업 범위**

- `ui/navigation/AppNavHost.kt`
- route 정의
  - Dashboard
  - Collection
  - RecordDetail(recordId)
  - RecordEditor(recordId?)
- `DDScreenScaffold`
- `DDTopAppBar`
- `DDBottomNavigationBar`
- FAB에서 신규 등록 화면 이동
- 저장 성공 후 상세 화면 이동
- 삭제 성공 후 이전 화면 또는 Collection으로 이동

**완료 기준**

- 앱 시작 시 Dashboard 표시
- 하단 탭으로 Dashboard/Collection 이동 가능
- recordId 인자를 가진 상세/수정 화면 이동 가능

**테스트 후보**

- Navigation route 인자 파싱
- Bottom navigation 선택 상태

---

### Step 10. RecordEditor 기능 구현

**목표**

신규 기록 등록과 기존 기록 수정을 하나의 화면과 ViewModel에서 처리한다.

**Agent 작업 범위**

- `RecordEditorViewModel`
- `RecordEditorUiState`
- `RecordEditorRoute`
- `RecordEditorScreen`
- 신규 모드 기본값 설정
  - 대시보드 기본과 별개로 기록 일시는 현재 시각
  - 컬렉션 상태 기본값은 일반 기록
- 수정 모드에서 기존 값 로드
- 저장 이벤트와 검증 오류 표시
- 저장 성공 시 `RecordDetail(recordId)`로 이동

**완료 기준**

- 필수값 누락 시 저장하지 않음
- 필드 근처에 검증 오류 표시
- 신규 저장과 수정 저장 모두 동작

**테스트 후보**

- 신규 입력 상태 초기화
- 수정 모드 기존 값 반영
- 저장 성공 이벤트
- 검증 실패 UI 상태

---

### Step 11. Collection 기능 구현

**목표**

전체 기록 목록과 주류 종류/컬렉션 상태 복합 필터를 구현한다.

**Agent 작업 범위**

- `CollectionViewModel`
- `CollectionUiState`
- `CollectionRoute`
- `CollectionScreen`
- `DDDrinkRecordListItem`
- 필터 상태 관리
- Empty 상태 구분
  - 전체 기록 없음
  - 필터 결과 없음
- 목록 아이템 선택 시 상세 화면 이동

**완료 기준**

- 최신순 목록 표시
- 주류 종류 필터와 컬렉션 상태 필터 동시 적용
- 재구매 후보/비선호 필터 진입을 Navigation 인자로 받을 수 있음

**테스트 후보**

- 전체 목록 상태
- 단일 필터
- 복합 필터
- Empty 상태 메시지 분기

---

### Step 12. RecordDetail 기능 구현

**목표**

개별 기록 상세 조회, 수정 진입, 삭제 기능을 구현한다.

**Agent 작업 범위**

- `RecordDetailViewModel`
- `RecordDetailUiState`
- `RecordDetailRoute`
- `RecordDetailScreen`
- `DDInfoRow`
- `DDRecordHeroImage`
- `DDTastingNoteBlock`
- 수정 버튼에서 Editor 수정 모드 이동
- 삭제 확인 다이얼로그
- 삭제 성공 이벤트 처리

**완료 기준**

- 상세 정보 전체 표시
- 이미지 없거나 로드 실패 시 대체 상태 표시
- 삭제 전 확인 다이얼로그 표시
- NotFound 상태 처리

**테스트 후보**

- recordId 조회 성공
- recordId NotFound
- 삭제 취소
- 삭제 성공 이벤트

---

### Step 13. Dashboard 기능 구현

**목표**

월간 기본 대시보드와 주간/월간/연간 기간별 요약을 구현한다.

**Agent 작업 범위**

- `DashboardViewModel`
- `DashboardUiState`
- `DashboardRoute`
- `DashboardScreen`
- `DDDashboardSummaryCard`
- `DDStatusSummaryCard`
- `DDDrinkTypeRatioCard`
- `DDDrinkRecordCard`
- 기간 선택 이벤트 처리
- 재구매 후보 카드 선택 시 Collection 필터 이동
- 비선호 카드 선택 시 Collection 필터 이동

**완료 기준**

- 기본 기간은 월간
- 기록 수, 평균 별점, 종류별 비중, 재구매 후보 수, 비선호 수 표시
- 선택 기간에 기록이 없으면 Empty 상태와 등록 진입 동선 표시

**테스트 후보**

- 월간 기본 상태
- 기간 변경 상태
- Empty 상태
- 재구매 후보/비선호 카드 이동 이벤트

---

### Step 14. 앱 통합 및 UX 마감

**목표**

화면 간 상태 반영, 오류 메시지, 접근성, 반응형 레이아웃을 점검한다.

**Agent 작업 범위**

- 저장 후 상세/목록/대시보드 Flow 갱신 확인
- 삭제 후 목록/대시보드 집계 제외 확인
- 사용자 친화적 오류 문구 정리
- contentDescription 정리
- compact phone 기준 레이아웃 점검
- tablet/foldable에서 과도한 늘어짐이 없는지 최소 대응
- 날짜/가격 표시 포맷 통일

**완료 기준**

- 주요 CRUD 흐름이 끊기지 않음
- 기술 오류가 사용자에게 그대로 노출되지 않음
- 접근성 라벨 누락이 주요 액션에 없음

---

### Step 15. 테스트 보강 및 최종 검증

**목표**

중요 비즈니스 로직과 데이터 변환을 자동 테스트로 고정한다.

**Agent 작업 범위**

- UseCase 단위 테스트
- Mapper 단위 테스트
- DAO instrumented test 또는 Room in-memory test
- ViewModel 상태 전환 테스트
- 최소 Compose UI 테스트
  - 빈 상태에서 등록 버튼 표시
  - 필터 선택 후 목록 상태 변화
- 최종 빌드 검증

**완료 기준**

- `./gradlew :app:testDebugUnitTest` 통과
- `./gradlew :app:connectedDebugAndroidTest`는 실행 환경이 있을 때 통과
- `./gradlew :app:assembleDebug` 통과

## 5. AI Agent 배정 권장 순서

| 순서 | Agent 역할 | 담당 Step | 병렬 가능 여부 |
| --- | --- | --- | --- |
| 1 | Build Agent | Step 01 | 단독 선행 |
| 2 | Domain Agent | Step 02 | Step 01 이후 |
| 3 | Data Agent | Step 03, Step 04 | Step 02 이후 |
| 4 | UseCase Agent | Step 05 | Step 04 이후 |
| 5 | App Wiring Agent | Step 06 | Step 05 이후 |
| 6 | UI Foundation Agent | Step 07, Step 08 | Step 02 이후 병렬 가능 |
| 7 | Navigation Agent | Step 09 | Step 06, Step 07 이후 |
| 8 | Editor Agent | Step 10 | Step 05, Step 08, Step 09 이후 |
| 9 | Collection Agent | Step 11 | Step 05, Step 08, Step 09 이후 |
| 10 | Detail Agent | Step 12 | Step 10, Step 11과 일부 병렬 가능 |
| 11 | Dashboard Agent | Step 13 | Step 05, Step 08, Step 09 이후 |
| 12 | Integration Agent | Step 14 | 화면 구현 후 |
| 13 | Test Agent | Step 15 | 각 Step 완료 후 누적 진행 |

## 6. Agent 작업 지시 템플릿

각 Agent에게 작업을 맡길 때 아래 형식을 사용한다.

```text
목표:
- Step NN을 구현한다.

참고 문서:
- app/docs/development-todo.md
- 관련 세부 문서: app/docs/...

작업 범위:
- Todo의 Agent 작업 범위만 수정한다.
- 다른 Step의 파일은 필요한 인터페이스 수준만 건드린다.

제약:
- Kotlin + Jetpack Compose + Material 3 기준을 따른다.
- 비즈니스 로직은 Composable에 넣지 않는다.
- 기존 변경사항을 되돌리지 않는다.

완료 기준:
- Todo의 완료 기준을 만족한다.
- 가능한 테스트 또는 빌드 명령을 실행하고 결과를 보고한다.
```

## 7. 주요 리스크 및 확인 포인트

- Room/KSP 의존성 추가가 현재 AGP 9.0.1, Kotlin 2.0.21 구성과 맞아야 한다.
- 사진 URI는 장기 접근 권한이 필요한지 검토해야 한다. MVP에서는 Photo Picker URI 저장과 대체 이미지 처리를 우선한다.
- 날짜/시간 계산은 기기 시간대 기준으로 처리한다. 이유: 로컬 기록 앱이며 서버 기준 시간이 없기 때문이다.
- Dashboard 집계는 초기에는 UseCase에서 계산한다. 기록 수가 많아질 경우 DAO 집계 쿼리로 최적화한다.
- UI 텍스트와 도메인 라벨은 한글 기준으로 통일한다.
