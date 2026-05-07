# DrinkDiary Design System

## 1. 문서 목적

이 문서는 DrinkDiary 앱의 화면별 UI 일관성을 유지하기 위한 Basic UI Component 목록과 사용 기준을 정의한다.
각 Screen은 가능한 한 이 문서의 공통 컴포넌트를 조합해 구현한다.

## 2. 설계 기준

- UI는 Jetpack Compose와 Material 3를 기준으로 한다.
- 화면별 커스텀 UI보다 공통 컴포넌트 재사용을 우선한다.
- 주류 종류는 와인, 위스키, 맥주 3개만 표현한다.
- 컬렉션 상태는 일반 기록, 재구매 후보, 비선호 3개만 표현한다.
- Loading, Empty, Error, Success 상태를 모든 주요 화면에서 일관되게 표현한다.
- Composable은 가능한 stateless로 만들고 상태와 이벤트는 상위에서 주입한다.

## 3. 컴포넌트 분류

| 분류 | 목적 |
| --- | --- |
| Foundation | 색상, 타이포그래피, 간격, 모양 등 기본 토큰 |
| Action | 버튼, FAB, 아이콘 버튼 등 사용자 액션 |
| Input | 텍스트, 숫자, 날짜, 사진, 별점 입력 |
| Selection & Filter | 필터, 세그먼트, 칩, 선택 항목 |
| Display | 카드, 목록 아이템, 정보 행, 요약 카드 |
| Feedback | 로딩, 빈 상태, 오류, 다이얼로그, 스낵바 |
| Domain | DrinkDiary 도메인에 특화된 컴포넌트 |

## 4. Foundation Components

| 컴포넌트 | 역할 | 사용 위치 |
| --- | --- | --- |
| DrinkDiaryTheme | 앱 전체 Material 3 Theme 제공 | 앱 루트 |
| DrinkDiaryColors | Primary, Secondary, Surface, Error 등 색상 토큰 | 모든 화면 |
| DrinkDiaryTypography | 제목, 본문, 라벨 텍스트 스타일 | 모든 화면 |
| DrinkDiarySpacing | 4, 8, 12, 16, 24, 32dp 간격 토큰 | 모든 화면 |
| DrinkDiaryShapes | 버튼, 카드, 이미지 모서리 반경 정의 | 모든 화면 |

권장 사항:

- 카드와 버튼은 과도한 라운드를 피하고 8dp 이하를 기본으로 한다.
- 화면 내부 텍스트는 Material 3 Typography를 기반으로 통일한다.
- 화면마다 직접 dp 값을 흩뿌리기보다 spacing 토큰을 사용한다.

## 5. Action Components

| 컴포넌트 | 역할 | 사용 위치 |
| --- | --- | --- |
| DDPrimaryButton | 저장, 등록 완료 등 주요 긍정 액션 | 기록 등록/수정 |
| DDSecondaryButton | 취소, 보조 이동 등 부가 액션 | 입력 폼, 다이얼로그 |
| DDContainedButton | 강조 배경을 가진 일반 실행 액션 | 빈 상태, 상세 화면 |
| DDOutlinedButton | 덜 강조되는 선택형 액션 | 빈 상태, 필터 보조 액션 |
| DDTextButton | 낮은 강조도의 텍스트 액션 | 다이얼로그, 상세 화면 |
| DDDestructiveButton | 삭제처럼 되돌리기 어려운 액션 | 기록 상세, 삭제 다이얼로그 |
| DDIconButton | 수정, 삭제, 뒤로가기, 이미지 제거 등 아이콘 액션 | 상단 바, 상세, 입력 폼 |
| DDAddRecordFab | 신규 기록 등록 진입 | Dashboard, Collection |

버튼 사용 기준:

- 저장, 등록 완료는 `DDPrimaryButton`을 사용한다.
- 주요 저장 액션이 아닌 일반 강조 액션은 `DDContainedButton`을 사용한다.
- 삭제는 항상 `DDDestructiveButton` 또는 destructive 색상의 `DDTextButton`을 사용한다.
- 한 화면의 가장 중요한 액션은 1개만 Primary로 둔다.
- 아이콘이 의미를 충분히 전달할 수 있는 경우 `DDIconButton`을 사용하고 접근성 라벨을 제공한다.

## 6. Input Components

| 컴포넌트 | 역할 | 사용 위치 |
| --- | --- | --- |
| DDTextField | 이름, 장소 등 단문 입력 | 기록 등록/수정 |
| DDNumberField | 가격 입력 | 기록 등록/수정 |
| DDMultilineTextField | 테이스팅 노트 입력 | 기록 등록/수정 |
| DDDateTimeField | 기록 일시 선택 | 기록 등록/수정 |
| DDImagePicker | 사진 선택 및 미리보기 | 기록 등록/수정 |
| DDRatingInput | 5점 기준 별점 입력 | 기록 등록/수정 |
| DDFormSection | 입력 항목을 의미 단위로 묶는 섹션 | 기록 등록/수정 |
| DDFormErrorText | 필드 단위 오류 메시지 표시 | 기록 등록/수정 |

입력 컴포넌트 제약:

- 이름, 주류 종류, 별점, 컬렉션 상태, 기록 일시는 필수 입력이다.
- 가격은 선택 입력이며 비어 있는 값을 허용한다.
- 가격 입력 시 0 미만 값은 허용하지 않는다.
- 사진 선택 실패 시 오류를 표시하되 사진 없이 저장 가능해야 한다.
- 테이스팅 노트는 여러 줄 입력을 지원한다.

## 7. Selection & Filter Components

| 컴포넌트 | 역할 | 사용 위치 |
| --- | --- | --- |
| DDDrinkTypeSelector | 와인, 위스키, 맥주 중 하나 선택 | 기록 등록/수정 |
| DDDrinkTypeFilter | 전체, 와인, 위스키, 맥주 필터 | Collection |
| DDCollectionStatusSelector | 일반 기록, 재구매 후보, 비선호 중 하나 선택 | 기록 등록/수정 |
| DDCollectionStatusFilter | 전체, 일반, 재구매 후보, 비선호 필터 | Collection |
| DDPeriodSegmentedControl | 주간, 월간, 연간 기간 선택 | Dashboard |
| DDFilterChipRow | 여러 필터 칩을 가로 배치 | Collection |

선택 컴포넌트 제약:

- 주류 종류와 컬렉션 상태는 정해진 값 외 직접 입력을 허용하지 않는다.
- Collection에서는 주류 종류 필터와 컬렉션 상태 필터를 동시에 적용할 수 있어야 한다.
- Dashboard 기간 선택은 주간, 월간, 연간 3개만 제공한다.

## 8. Display Components

| 컴포넌트 | 역할 | 사용 위치 |
| --- | --- | --- |
| DDScreenScaffold | 공통 TopBar, BottomBar, FAB 슬롯 제공 | 모든 주요 화면 |
| DDTopAppBar | 화면 제목과 상단 액션 표시 | 모든 주요 화면 |
| DDBottomNavigationBar | Dashboard, Collection 탭 제공 | 앱 루트 |
| DDDashboardSummaryCard | 기록 수, 평균 별점 등 요약 지표 표시 | Dashboard |
| DDStatusSummaryCard | 재구매 후보 수, 비선호 수 표시 | Dashboard |
| DDDrinkTypeRatioCard | 와인, 위스키, 맥주 비중 표시 | Dashboard |
| DDDrinkRecordListItem | 컬렉션 목록의 단일 기록 표시 | Collection |
| DDDrinkRecordCard | 주요 기록 카드 표시 | Dashboard |
| DDInfoRow | 상세 화면의 라벨-값 행 표시 | RecordDetail |
| DDImageThumbnail | 목록/카드용 이미지 썸네일 | Dashboard, Collection |
| DDRecordHeroImage | 상세 화면의 대표 이미지 | RecordDetail |

표시 컴포넌트 제약:

- 목록은 `LazyColumn` 기반으로 구현한다.
- 기록 목록 아이템에는 이름, 주류 종류, 별점, 컬렉션 상태, 기록 일시를 우선 표시한다.
- 가격과 장소는 공간이 부족한 목록에서는 생략 가능하지만 상세 화면에서는 표시한다.
- 이미지가 없거나 로드 실패한 경우 대체 이미지를 표시한다.

## 9. Feedback Components

| 컴포넌트 | 역할 | 사용 위치 |
| --- | --- | --- |
| DDLoadingContent | 화면 또는 섹션 로딩 표시 | Dashboard, Collection, Detail |
| DDEmptyContent | 기록 없음 또는 필터 결과 없음 표시 | Dashboard, Collection |
| DDErrorContent | 조회 실패, 저장 실패 등 오류 표시 | 모든 주요 화면 |
| DDConfirmDialog | 삭제 확인 등 사용자 확인 필요 상황 | RecordDetail |
| DDSnackbarMessage | 저장 실패, 삭제 실패 등 일시적 메시지 | 모든 주요 화면 |
| DDInlineValidationMessage | 필드 검증 오류 표시 | RecordEditor |

피드백 컴포넌트 제약:

- Empty 상태는 등록 진입 동선을 제공한다.
- Error 상태는 가능한 경우 재시도 액션을 제공한다.
- 삭제는 반드시 `DDConfirmDialog`를 거친다.
- 기술 상세 오류는 사용자에게 직접 노출하지 않는다.

## 10. Domain Components

| 컴포넌트 | 역할 | 사용 위치 |
| --- | --- | --- |
| DDDrinkTypeBadge | 와인, 위스키, 맥주 표시 | 목록, 상세, 카드 |
| DDCollectionStatusBadge | 일반 기록, 재구매 후보, 비선호 표시 | 목록, 상세, 카드 |
| DDRatingStars | 별점 표시 전용 | 목록, 상세, Dashboard |
| DDPriceText | 가격 표시 포맷 통일 | 목록, 상세 |
| DDRecordedDateText | 기록 일시 표시 포맷 통일 | 목록, 상세 |
| DDTastingNoteBlock | 테이스팅 노트 표시 | 상세 |

도메인 컴포넌트 제약:

- `DDDrinkTypeBadge`는 와인, 위스키, 맥주 외 값을 표시하지 않는다.
- `DDCollectionStatusBadge`는 재구매 후보와 비선호를 명확히 구분한다.
- 별점 표시는 입력용 `DDRatingInput`과 조회용 `DDRatingStars`를 분리한다.

## 11. 화면별 사용 컴포넌트

### DashboardScreen

| 목적 | 컴포넌트 |
| --- | --- |
| 화면 구조 | DDScreenScaffold, DDTopAppBar, DDBottomNavigationBar |
| 기간 선택 | DDPeriodSegmentedControl |
| 요약 표시 | DDDashboardSummaryCard, DDStatusSummaryCard, DDDrinkTypeRatioCard |
| 주요 기록 표시 | DDDrinkRecordCard, DDRatingStars, DDCollectionStatusBadge |
| 상태 표시 | DDLoadingContent, DDEmptyContent, DDErrorContent |
| 기록 등록 | DDAddRecordFab |

### CollectionScreen

| 목적 | 컴포넌트 |
| --- | --- |
| 화면 구조 | DDScreenScaffold, DDTopAppBar, DDBottomNavigationBar |
| 필터 | DDDrinkTypeFilter, DDCollectionStatusFilter, DDFilterChipRow |
| 목록 | DDDrinkRecordListItem, DDImageThumbnail, DDRatingStars |
| 상태 표시 | DDLoadingContent, DDEmptyContent, DDErrorContent |
| 기록 등록 | DDAddRecordFab |

### RecordDetailScreen

| 목적 | 컴포넌트 |
| --- | --- |
| 화면 구조 | DDScreenScaffold, DDTopAppBar |
| 이미지 | DDRecordHeroImage |
| 상세 정보 | DDInfoRow, DDDrinkTypeBadge, DDCollectionStatusBadge, DDRatingStars, DDPriceText, DDRecordedDateText, DDTastingNoteBlock |
| 액션 | DDPrimaryButton 또는 DDIconButton, DDDestructiveButton, DDConfirmDialog |
| 상태 표시 | DDLoadingContent, DDErrorContent |

### RecordEditorScreen

| 목적 | 컴포넌트 |
| --- | --- |
| 화면 구조 | DDScreenScaffold, DDTopAppBar |
| 입력 폼 | DDFormSection, DDTextField, DDNumberField, DDMultilineTextField, DDDateTimeField |
| 선택 입력 | DDImagePicker, DDDrinkTypeSelector, DDCollectionStatusSelector, DDRatingInput |
| 검증 | DDFormErrorText, DDInlineValidationMessage |
| 액션 | DDPrimaryButton, DDSecondaryButton |
| 상태 표시 | DDSnackbarMessage |

## 12. 추천 구현 위치

```text
com.bluemarlin.drinkdiary.ui
  ├─ component
  │  ├─ action
  │  ├─ input
  │  ├─ selection
  │  ├─ display
  │  ├─ feedback
  │  └─ domain
  ├─ dashboard
  ├─ collection
  ├─ detail
  ├─ editor
  ├─ navigation
  └─ theme
```

공통 컴포넌트는 `ui.component` 하위에 두고, 특정 화면에서만 쓰는 조합 컴포넌트는 각 feature 패키지에 둔다.
이유: 재사용 가능한 컴포넌트와 화면 전용 컴포넌트의 책임을 분리하기 위함이다.

## 13. 우선 구현 대상

초기 MVP에서는 아래 컴포넌트부터 구현한다.

1. DrinkDiaryTheme
2. DDScreenScaffold
3. DDPrimaryButton
4. DDSecondaryButton
5. DDContainedButton
6. DDDestructiveButton
7. DDTextField
8. DDNumberField
9. DDMultilineTextField
10. DDDrinkTypeSelector
11. DDCollectionStatusSelector
12. DDPeriodSegmentedControl
13. DDRatingInput
14. DDRatingStars
15. DDDrinkRecordListItem
16. DDDashboardSummaryCard
17. DDLoadingContent
18. DDEmptyContent
19. DDErrorContent
20. DDConfirmDialog
