# DrinkDiary UI Technical Flow

> [!WARNING]
> **보관 문서 — 살아있는 명세가 아니다.** 구 MVP의 화면 구성·UI 흐름이며, 현재 저장소의 코드를 설명하지만
> 재정의된 제품 방향과 충돌한다(맥주 포함, 5축 슬라이더 입력 등).
> 현행 명세는 `../../specs/`를 따른다 — 제품은 `specs/planner/prd.md`,
> 구조는 `specs/developer/software-architecture.md`.
> 재작성 시 참고용으로만 남긴다. **여기에 새 내용을 추가하지 않는다.**


## 1. 문서 목적

이 문서는 `../planner/usecase.md`를 기준으로 DrinkDiary의 화면 구성, 사용자 흐름, UI 상태, 화면 제약을 정의한다.
UI는 Jetpack Compose와 Material 3를 기준으로 설계하며, 화면 상태는 ViewModel이 제공하는 단방향 상태 흐름을 렌더링한다.

## 2. UI 설계 원칙

- 앱의 중심은 나만의 술 컬렉션 관리이다.
- 주류 종류는 와인, 위스키, 맥주 3개만 제공한다.
- 컬렉션 상태는 일반 기록, 재구매 후보, 비선호 3개만 제공한다.
- 별점과 컬렉션 상태는 독립 입력으로 둔다. 이유: 높은 별점이 항상 재구매를 의미하지 않고, 낮은 별점이 항상 비선호를 의미하지 않을 수 있기 때문이다.
- 화면은 Loading, Empty, Success, Error 상태를 명시적으로 표현한다.
- 사진 선택 실패 또는 권한 거부 시 사진 없이 기록을 계속 작성할 수 있어야 한다.

## 3. 화면 구성

| 화면 | 역할 | 관련 Use Case |
| --- | --- | --- |
| DashboardScreen | 기간별 컬렉션 요약, 재구매 후보, 비선호 현황 표시 | UC-05, UC-07, UC-08 |
| CollectionScreen | 컬렉션 목록, 주류 종류 필터, 컬렉션 상태 필터 표시 | UC-02, UC-06, UC-07, UC-08 |
| RecordDetailScreen | 개별 기록 상세 표시, 수정/삭제 진입 | UC-02, UC-03, UC-04 |
| RecordEditorScreen | 신규 등록 및 기존 기록 수정 | UC-01, UC-03 |

## 4. 내비게이션 구조

```text
MainActivity
  -> DrinkDiaryApp
  -> AppNavHost
     -> DashboardRoute
     -> CollectionRoute
     -> RecordDetailRoute(recordId)
     -> RecordEditorRoute(recordId?)
```

최상위 내비게이션은 `Dashboard`, `Collection` 2개 목적지를 제공한다.
내비게이션 컴포넌트는 Window Size Class와 화면 방향에 따라 변경한다.

| 조건 | 권장 내비게이션 | 이유 |
| --- | --- | --- |
| Compact width | Bottom Navigation | 세로형 휴대폰에서 엄지 접근성이 좋다. |
| Medium width | Bottom Navigation 또는 Navigation Rail | 접힘 기기, 소형 태블릿, 가로 모드에서 사용 가능한 폭에 따라 선택한다. |
| Expanded width | Navigation Rail | 대화면에서 주요 탭을 좌측에 고정해 콘텐츠 세로 공간을 확보한다. |

기록 등록은 Floating Action Button으로 제공한다.
Compact에서는 하단 내비게이션과 겹치지 않게 배치하고, Medium 이상에서는 rail 또는 콘텐츠 영역의 trailing side에 배치한다.
이유: 등록은 자주 쓰는 액션이지만 독립적인 탐색 섹션이 아니라 컬렉션에 항목을 추가하는 행위이기 때문이다.

## 5. 주요 UI Flow

### 5.1 앱 시작 및 대시보드 조회

```text
앱 실행
  -> DashboardScreen
  -> 연간 대시보드 기본 표시
  -> 사용자가 주간 / 월간 / 연간 기간 선택
  -> DashboardViewModel이 기간 변경 이벤트 처리
  -> 선택 기간 기준 요약 갱신
```

대시보드 기본 기간은 연간을 권장한다.
이유: 초기 사용 단계에서도 누적 기록을 가장 넓게 보여줘 홈 진입 시 빈 상태를 줄일 수 있다.

### 5.2 신규 기록 등록

```text
DashboardScreen 또는 CollectionScreen
  -> 등록 FAB 선택
  -> RecordEditorScreen(new)
  -> 사진 / 주류 종류 / 이름 / 가격 / 장소 / 테이스팅 노트 / 별점 / 컬렉션 상태 / 기록 일시 입력
  -> 저장 선택
  -> 필수값 검증
  -> 저장 성공 시 RecordDetailScreen(recordId) 이동
```

필수 입력값은 주류 종류, 이름, 별점, 컬렉션 상태, 기록 일시이다.
입력 오류가 있으면 저장하지 않고 해당 필드 근처에 오류를 표시한다.

### 5.3 컬렉션 조회 및 필터링

```text
CollectionScreen
  -> 전체 기록 최신순 표시
  -> 주류 종류 필터: 전체 / 와인 / 위스키 / 맥주
  -> 컬렉션 상태 필터: 전체 / 일반 기록 / 재구매 후보 / 비선호
  -> 필터 변경 시 목록 자동 갱신
  -> 기록 선택 시 RecordDetailScreen(recordId) 이동
```

주류 종류 필터와 컬렉션 상태 필터는 동시에 적용한다.
예: `위스키 + 재구매 후보`, `맥주 + 비선호`.

### 5.4 기록 상세 조회

```text
CollectionScreen 또는 DashboardScreen
  -> 기록 선택
  -> RecordDetailScreen(recordId)
  -> 사진, 이름, 주류 종류, 가격, 장소, 테이스팅 노트, 별점, 컬렉션 상태, 기록 일시 표시
```

상세 화면에서는 수정과 삭제 액션을 제공한다.
삭제는 즉시 수행하지 않고 확인 다이얼로그를 거친다.

### 5.5 기록 수정

```text
RecordDetailScreen
  -> 수정 선택
  -> RecordEditorScreen(recordId)
  -> 기존 입력값 표시
  -> 값 변경
  -> 저장 선택
  -> 필수값 검증
  -> 저장 성공 시 RecordDetailScreen(recordId) 갱신
```

수정 중 대상 기록이 삭제되었거나 찾을 수 없으면 오류 메시지를 표시하고 `CollectionScreen`으로 이동한다.

### 5.6 기록 삭제

```text
RecordDetailScreen
  -> 삭제 선택
  -> 삭제 확인 다이얼로그
  -> 삭제 확정
  -> 삭제 성공
  -> 이전 진입 화면으로 복귀
```

삭제된 기록은 컬렉션 목록과 대시보드 집계에서 즉시 제외되어야 한다.

### 5.7 재구매 후보 확인

```text
DashboardScreen
  -> 재구매 후보 요약 카드 선택
  -> CollectionScreen(collectionStatus = Repurchase)
  -> 재구매 후보 기록만 표시
```

재구매 후보가 없으면 빈 상태와 기록 등록 진입 동선을 표시한다.

### 5.8 비선호 술 확인

```text
DashboardScreen
  -> 비선호 요약 카드 선택
  -> CollectionScreen(collectionStatus = NotForMe)
  -> 비선호 기록만 표시
```

비선호 목록은 같은 선택을 반복하지 않게 돕는 보조 컬렉션으로 다룬다.

### 5.9 가로 모드 및 대화면 사용 흐름

```text
앱 실행 또는 화면 회전
  -> Window Size Class 및 방향 계산
  -> AppScaffold가 내비게이션 유형 결정
  -> 현재 route와 ViewModel 상태 유지
  -> 화면별 adaptive layout 렌더링
```

화면 회전이나 창 크기 변경 시 사용자가 입력 중인 값, 선택한 필터, 스크롤 가능한 콘텐츠 접근성이 유지되어야 한다.
RecordEditorScreen은 키보드가 표시된 가로 모드에서도 저장 버튼과 현재 입력 필드가 가려지지 않아야 한다.
CollectionScreen은 가로 모드에서 필터 영역이 목록 스크롤을 과도하게 밀어내지 않도록 접힘 가능한 필터, 가로 칩 목록, 또는 좌측 필터 패널 중 화면 폭에 맞는 방식을 사용한다.
RecordDetailScreen은 술 이미지와 상세 정보를 동시에 확인할 수 있도록 Medium 이상에서 2-pane 구성을 우선한다.

## 6. 화면 상태 모델

### DashboardUiState

| 상태 | 설명 |
| --- | --- |
| Loading | 기간별 집계 조회 중 |
| Empty | 선택 기간에 기록이 없음 |
| Success | 기록 수, 평균 별점, 주류 종류 비중, 재구매 후보 수, 비선호 수 표시 |
| Error | 집계 조회 실패, 재시도 액션 제공 |

### CollectionUiState

| 상태 | 설명 |
| --- | --- |
| Loading | 컬렉션 목록 조회 중 |
| Empty | 필터 조건에 해당하는 기록이 없음 |
| Success | 필터링된 컬렉션 목록 표시 |
| Error | 목록 조회 실패, 재시도 액션 제공 |

### RecordDetailUiState

| 상태 | 설명 |
| --- | --- |
| Loading | 상세 기록 조회 중 |
| Success | 기록 상세 표시 |
| NotFound | 기록이 없거나 삭제됨 |
| Error | 상세 조회 또는 삭제 실패 |

### RecordEditorUiState

| 상태 | 설명 |
| --- | --- |
| Editing | 사용자가 입력 중 |
| Saving | 저장 처리 중 |
| Saved | 저장 성공 |
| ValidationError | 필수값 누락 또는 잘못된 입력 |
| Error | 저장 실패 |

## 7. Adaptive Layout 설계

UI는 `WindowSizeClass`를 기준으로 Compact, Medium, Expanded 레이아웃을 제공한다.
화면 방향은 보조 정보로 사용하고, 최종 배치는 사용 가능한 width size class를 우선한다.
이유: 태블릿, 폴더블, 멀티 윈도우 환경에서는 단순 세로/가로 판단보다 실제 사용 가능한 폭이 중요하다.

### 7.1 공통 레이아웃 기준

| Width Size Class | 대표 환경 | 레이아웃 기준 |
| --- | --- | --- |
| Compact | 일반 휴대폰 세로, 좁은 멀티 윈도우 | 단일 컬럼, Bottom Navigation, 콘텐츠 전체 세로 스크롤 |
| Medium | 휴대폰 가로, 폴더블 커버/펼침 일부, 소형 태블릿 | 단일 컬럼 또는 보조 패널, Bottom Navigation 또는 Navigation Rail |
| Expanded | 태블릿, 데스크톱 크기 창, 넓은 폴더블 | Navigation Rail, 2-pane 또는 grid 기반 콘텐츠 배치 |

공통 제약:

- `calculateWindowSizeClass` 또는 equivalent API로 width/height class를 계산해 앱 루트에서 전달한다.
- `AppScaffold`는 Bottom Navigation과 Navigation Rail을 동시에 렌더링하지 않는다.
- 시스템 바, IME, display cutout을 고려해 `WindowInsets`를 적용한다.
- 화면 회전 시 route, 입력 상태, 필터 상태는 ViewModel 또는 저장 가능한 UI state로 유지한다.
- 콘텐츠가 한 화면에 맞지 않는 경우 축소보다 스크롤을 우선한다. 이유: 입력 필드와 상세 정보의 가독성을 유지하기 위함이다.

### 7.2 DashboardScreen

| 조건 | 레이아웃 |
| --- | --- |
| Compact | 요약 카드와 기간 선택을 세로로 배치한다. |
| Medium | 주요 요약 카드를 2열까지 확장하고, 기간 선택은 상단에 고정한다. |
| Expanded | Navigation Rail 우측 콘텐츠 영역에 요약 카드 grid와 보조 목록을 함께 배치한다. |

대시보드의 재구매 후보와 비선호 요약은 대화면에서 별도 컬럼 또는 grid item으로 배치할 수 있다.
단, 선택 시 이동 흐름은 기존 `CollectionScreen(collectionStatus = ...)`를 유지한다.

### 7.3 CollectionScreen

| 조건 | 레이아웃 |
| --- | --- |
| Compact | 필터를 상단 칩/세그먼트로 표시하고 목록은 `LazyColumn`으로 표시한다. |
| Medium | 가로 모드에서 필터를 한 줄 또는 접힘 영역으로 압축하고 목록 높이를 확보한다. |
| Expanded | 좌측 필터 패널과 우측 목록 또는 adaptive grid를 사용할 수 있다. |

목록 아이템은 Compact에서 핵심 정보 중심으로 표시하고, Medium 이상에서는 이미지, 장소, 가격 같은 보조 정보를 더 넓게 표시할 수 있다.
목록이 많은 경우 모든 size class에서 lazy layout을 사용한다.

### 7.4 RecordDetailScreen

| 조건 | 레이아웃 |
| --- | --- |
| Compact | 이미지, 핵심 정보, 상세 정보를 세로 순서로 표시한다. |
| Medium | 가로 모드에서 이미지와 주요 정보를 2-pane으로 배치한다. |
| Expanded | 좌측 이미지/요약 영역, 우측 상세 정보/액션 영역으로 분리한다. |

이미지는 가로 모드에서 화면 높이를 과도하게 차지하지 않도록 max height를 제한한다.
상세 정보는 독립 스크롤 영역을 허용해 테이스팅 노트가 길어도 액션 버튼 접근성을 유지한다.

### 7.5 RecordEditorScreen

| 조건 | 레이아웃 |
| --- | --- |
| Compact | 단일 컬럼 입력 폼과 하단 저장 액션을 사용한다. |
| Medium | 가로 모드에서 이미지 선택 영역과 텍스트 입력 영역을 2-pane으로 나눌 수 있다. |
| Expanded | 입력 섹션을 2열로 배치하고, 저장/취소 액션은 고정된 trailing 또는 bottom 영역에 둔다. |

입력 화면 제약:

- 모든 입력 필드는 IME 표시 중에도 스크롤로 접근 가능해야 한다.
- 저장 버튼은 키보드에 가려지지 않아야 하며, `imePadding` 또는 고정 액션 영역을 적용한다.
- 테이스팅 노트는 가로 모드에서 최소 높이를 보장하되 전체 화면을 독점하지 않는다.
- 이미지 미리보기는 Medium 이상에서 별도 pane에 배치할 수 있지만, 사진 없이 저장 가능한 흐름은 유지한다.

## 8. Compose 구현 제약

- Screen Composable은 ViewModel 상태를 수집하고 Route Composable은 내비게이션 이벤트를 처리한다.
- 입력 폼 Composable은 가능한 stateless로 작성하고 값과 이벤트를 파라미터로 받는다.
- 큰 목록은 `LazyColumn`을 사용한다.
- 주류 종류와 컬렉션 상태는 `FilterChip`, `SegmentedButton`, `DropdownMenu` 중 화면 밀도에 맞게 선택한다.
- 별점은 5점 기준의 명시적 선택 UI를 권장한다.
- 가격은 선택 항목이므로 비어 있는 상태를 허용한다.
- 사용자에게 보여주는 오류 메시지는 기술 상세가 아니라 행동 가능한 메시지로 제공한다.
- 앱 루트는 `DrinkDiaryWindowSizeClass` 같은 UI 전용 모델로 width/height class를 하위 화면에 전달한다.
- Navigation Rail과 Bottom Navigation 선택은 AppScaffold에서 처리하고, 개별 Screen은 콘텐츠 배치에 집중한다.
- 대화면에서 2-pane을 사용할 때도 ViewModel과 route는 동일하게 유지한다. 이유: 화면 크기에 따라 비즈니스 흐름이 분기되지 않게 하기 위함이다.
- 가로 모드 입력 화면은 `imePadding`, `navigationBarsPadding`, `verticalScroll` 또는 lazy form 구조를 사용해 키보드와 시스템 바에 가려지는 필드를 없앤다.
- 이미지 영역은 `aspectRatio`, `heightIn`, `widthIn`으로 크기 제한을 둔다. 이유: 가로 모드에서 이미지가 텍스트와 액션 영역을 밀어내지 않게 하기 위함이다.
