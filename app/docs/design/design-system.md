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

### 4.1 다크 무디(Dark & Moody) 테마 — 강제 적용

Pinterest 레퍼런스 리서치(`app/docs/design/research-immersive-ui.md`) 결과에 따라 앱은 시스템 라이트/다크 설정과 무관하게 **항상 다크 테마로 렌더링**한다(브랜드 아이덴티티, 토글 아님). `DrinkDiaryTheme`의 `darkTheme` 기본값이 `true`로 고정되어 있고, `MainActivity`도 `enableEdgeToEdge`에서 시스템 바를 라이트 아이콘으로 강제한다. `LightColorScheme`은 롤백 여지를 위해 코드에는 남아있지만 호출되지 않는다.

- **뉴트럴 팔레트**: 배경/서피스 계열은 `Color.kt`의 `DeepForest10~30`(딥 포레스트 그린)을 사용해 "와인 셀러" 무드를 낸다. primary(Cellar Green)/secondary(Malt Gold)/tertiary(Rose)의 브랜드 컬러 자체와 각 Container 매핑은 라이트 테마 때와 동일하게 유지한다.
- **골드 = CTA/액센트, 반드시 튀어야 하는 요소**: 배경이 초록 톤이라 `primary`(초록) 계열 요소는 배경에 묻힌다. FAB, `DDPrimaryButton`, 별점(`DDRatingStars`), 재구매 후보 배지의 골드 텍스트/보더는 모두 `secondary`(Gold)를 쓴다. 단, **정적 상태 배지**(재구매 후보 칩)는 `secondary` 반투명 fill + 1dp 보더 + `secondary` 텍스트로 두고, **실제 탭 가능한 CTA**(FAB, PrimaryButton)만 `secondary` 풀필로 채운다 — 둘을 동일한 풀필 골드로 두면 배지가 버튼처럼 보여 CTA의 우선순위가 흐려진다(RecordDetail UI 루프에서 확인된 실제 버그).
- **글래스모피즘 근사**: 실제 backdrop blur 라이브러리 없이 `ddGlassBorderModifier()`(`Components.kt`, internal public) — `secondary→tertiary` 그래디언트 1.5dp 보더 + `surfaceContainerHigh.copy(alpha=0.88f)` 카드 fill로 "유리 같은" 느낌을 근사한다. 새 카드를 다크 테마 화면에 추가할 때는 이 헬퍼를 재사용한다.
- **에디토리얼 세리프**: 폰트 에셋 추가 없이 시스템 내장 `FontFamily.Serif`를 Dashboard 히어로 숫자(`DDHeroSummaryCard`)와 RecordDetail 술 이름에 적용한다.
- **사진 없음 워터마크**: 사진 미등록 시 플레이스홀더는 골드↔로즈 그래디언트 배경 위에 앱 런처 글리프를 `ColorFilter.tint`로 단색화해 28% 알파로 올린다 — 원본 다색 아이콘을 그대로 반투명 처리하면 "빛바랜 스티커"처럼 보이므로 반드시 단색 틴트를 거친다.
- 현재 이 테마가 적용된 화면은 **Dashboard, RecordDetail, Collection, RecordEditor** 전부다.
- **선택된 필터 칩의 시각 언어**: `FilterChip`이 선택되면 골드 풀필 + 체크 아이콘(`Icons.Filled.Check`)으로 표시한다(Dashboard 기간 세그먼트의 "✓ 월간"과 동일한 문법). `FilterChipDefaults.filterChipColors()`에서 `selectedContainerColor`/`selectedLabelColor`만 지정하고 `selectedLeadingIconColor`를 빠뜨리면 아이콘이 M3 기본 틴트(연한 크림색)로 남아 골드 배경 대비 ~1.4:1로 바래 보인다 — **선택 색상을 커스터마이징할 때는 라벨과 아이콘 색을 항상 함께 지정**한다.
- **에러 색상 = Rose**: `Theme.kt`의 `error`/`onError`/`errorContainer`/`onErrorContainer`는 M3 베이스라인 빨강 대신 기존 Rose 패밀리(`Rose80/40/90/30/10`)로 채워져 있다. `DDDestructiveButton`, `DDFormErrorText`, 텍스트 필드의 `isError` 테두리는 모두 `colorScheme.error`를 참조하므로 이 한 곳만 맞으면 자동으로 통일된다. 세그먼트 선택기(`DDDrinkTypeSelector` 등)나 별점 입력(`DDRatingInput`)처럼 `isError` 개념이 없는 커스텀 컨트롤은 에러 시 캡션 텍스트만으로는 부족하다 — 컨트롤 자체에도 `colorScheme.error` 보더/텍스트 색을 입혀야 한다(`ddSelectorErrorModifier` 참고).
- **Wish/Pass는 상태 배지뿐 아니라 선택 컨트롤에도 적용**: `DDCollectionStatusSelector`(RecordEditor의 세그먼트 선택기)의 비선호도 `DDCollectionStatusBadge`와 동일하게 Rose(`tertiaryContainer`)를 쓴다 — Gold(재구매/일반)와 섞이면 Wish/Pass 구분이 선택 컨트롤에서부터 무너진다.
- **알려진 한계 — 날짜 선택기 로케일**: `DDDateTimeField`는 Compose Material3 `DatePickerDialog`를 쓰며 버튼 라벨(확인/취소)과 색상은 한글/Gold로 고정되어 있지만, 내부 달력 텍스트(월 이름, 요일 헤더)는 기기 시스템 로케일을 따른다. `Locale.setDefault()`/`LocalConfiguration` 오버라이드 둘 다 이 텍스트에는 적용되지 않음을 확인함(M3 DatePicker가 `androidx.compose.ui.text.intl.Locale`로 Activity의 실제 Configuration을 직접 읽기 때문) — 완전히 고치려면 `attachBaseContext` 수준의 앱 전역 로케일 래핑이 필요해 의도적으로 범위 밖으로 남김.

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
| DDDashboardCalendar | 이번 달 캘린더 + 기록 날짜 점 표시 + 기간별(주간/월간) 하이라이트 | Dashboard |

도메인 컴포넌트 제약:

- `DDDrinkTypeBadge`는 와인, 위스키, 맥주 외 값을 표시하지 않는다.
- `DDCollectionStatusBadge`는 재구매 후보와 비선호를 명확히 구분한다.
- 별점 표시는 입력용 `DDRatingInput`과 조회용 `DDRatingStars`를 분리한다.
- `DDDashboardCalendar`는 `DashboardPeriod`와 이번 달 기록 날짜(`Set<LocalDate>`)만 입력받는 순수 표시 컴포넌트다. 월 이동은 지원하지 않는다(항상 이번 달 고정 — `ObserveDashboardSummaryUseCase`의 "오늘 기준" 범위 계산과 동일한 전제). 요일 헤더는 월요일 시작(월화수목금토일)으로, 주간 하이라이트가 `ObserveDashboardSummaryUseCase`의 월~일 주간 정의와 한 행 안에서 정확히 일치하도록 한다(한국의 일반적인 일요일 시작 캘린더 관례와는 다름 — 의도적인 선택).

## 11. 화면별 사용 컴포넌트

### DashboardScreen

| 목적 | 컴포넌트 |
| --- | --- |
| 화면 구조 | DDScreenScaffold, DDTopAppBar, DDBottomNavigationBar |
| 기간 선택 | DDPeriodSegmentedControl |
| 캘린더 | DDDashboardCalendar |
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

## 14. AI 에이전트 작업 금지 규칙 (Behavioral Rules)

Astryx(Meta의 AI-에이전트 인지형 디자인 시스템 문서, `app/docs/design/ux-reference-sites.md` 5절 참고)가 보여준 패턴 — "AI가 디자인 시스템을 임의로 추측하지 않도록 명시적 금지 규칙을 문서화" — 을 이 프로젝트에 맞게 적용한다. 아래 규칙은 전부 실제로 이 세션에서 겪은 실패 패턴에서 나왔다(에뮬레이터 스크린샷으로 검증한 뒤 발견됨).

### 하지 말아야 할 것

| 금지 사항 | 이유 / 실제 발생 사례 |
| --- | --- |
| Composable 안에서 색상 하드코딩(`Color(0xFF...)`) | 브랜드 색상은 반드시 `MaterialTheme.colorScheme.*`를 거쳐야 다크테마/일관성이 유지된다. 원본 색상 상수 정의는 `Color.kt`에서만 허용. |
| `Theme.kt`에서 `lightColorScheme()`/`darkColorScheme()`에 `primary`/`secondary`/`tertiary`만 지정하고 나머지 롤을 비워두기 | 비워두면 M3 베이스라인 색(보라색 계열)이 그대로 노출된다 — 실제로 발생: `background`/`surface`/`surfaceContainer*` 롤을 안 채워서 카드·FAB·바텀내비 전체가 브랜드 컬러 대신 M3 기본 퍼플로 렌더링됐었다. **컬러스킴을 만들거나 수정할 때는 primary/secondary/tertiary + 각 Container/onContainer + background/surface + surfaceContainer 전체 사다리(Lowest~Highest)까지 명시적으로 채운다.** |
| `Card`, `FloatingActionButton`, `SegmentedButton` 등에 `shape` 파라미터 생략 | 컴포넌트별로 테마의 `Shapes`를 그대로 상속하는지 여부가 다르다(FAB/SegmentedButton은 상속하지 않는 경우가 실제로 있었다) — **8dp 규정을 지키려면 카드류·FAB·세그먼트 버튼에 `shape = RoundedCornerShape(8.dp)`를 명시적으로 지정**한다. "테마에 이미 8dp로 설정했으니 괜찮겠지"라고 가정하지 않는다. |
| 임의의 dp 값 사용(`14.dp`, `18.dp`, `20.dp` 등) | `DrinkDiarySpacing` 토큰(4/8/12/16/24/32dp) 밖의 값은 화면마다 미묘하게 다른 간격을 만든다. |
| 두 개의 인접/중첩 컴포넌트에 동일한 `colorScheme` 롤을 배경으로 사용 | 예: 카드 배경이 `surfaceVariant`인데 그 안의 이미지 placeholder도 `surfaceVariant`를 쓰면 서로 구분이 안 되고 텍스트만 떠 보인다(실제 발생). 부모-자식 관계에 있는 배경색은 항상 인접 롤(예: `surfaceVariant` 위에는 `surfaceContainerHighest`)로 대비를 만든다. |
| 상태/타입이 다른 도메인 배지를 텍스트 라벨로만 구분 | `DDCollectionStatusBadge`는 재구매 후보/비선호/일반을 텍스트만으로 구분하면 앱의 핵심 차별화 요소(Wish/Pass)가 스캔이 안 된다. **색상까지 함께 구분**한다(재구매=Gold 계열, 비선호=Rose 계열 — 다크 테마 전환 후 재구매는 Cellar Green에서 Gold로 재매핑됨, 4.1절 참고). |
| `LazyColumn` 화면에 `FloatingActionButton`을 얹으면서 `contentPadding` 없이 방치 | 목록 마지막 아이템이 FAB에 가려진다. FAB가 떠 있는 화면의 `LazyColumn`은 `contentPadding = PaddingValues(bottom = 96.dp)` 이상을 준다. |
| raw `Text`/`Button`/`Card`를 DD* 컴포넌트가 이미 존재하는 상황에서 새로 조합 | 재사용 가능한 `DD*`가 있으면 그것을 쓴다(2절 원칙). 화면 전용 신규 조합이 필요하면 `ui.component` 밖 feature 패키지에 두되, 먼저 이 문서에 없는지 확인한다. |
| 같은 "액센트 색"이라며 `secondary`와 `secondaryContainer`를 서로 다른 두 컴포넌트(FAB vs 배지)에 섞어 쓰기 | M3의 `*Container` 롤은 base 롤보다 명도/채도가 낮은 톤-다운 변형이라, 같은 색이라고 생각하고 섞으면 나란히 놓였을 때 밝기 불일치가 눈에 띈다(다크 테마 루프에서 실제 발견: FAB=`secondary`인데 배지=`secondaryContainer`라 배지가 칙칙해 보임). **같은 액센트로 묶이는 요소는 base/Container 중 하나로 통일**한다. |
| 정적 상태 배지(칩)를 실제 탭 가능한 CTA 버튼과 동일한 풀필 색상으로 채우기 | 같은 화면에 풀필 골드 버튼과 풀필 골드 배지가 나란히 있으면 배지가 두 번째 버튼처럼 보여 CTA의 시각적 우선순위가 흐려진다(RecordDetail에서 실제 발견: 재구매 후보 배지 vs 수정 버튼). **정적 배지는 반투명 fill + 얇은 보더 + 컬러 텍스트**로, **탭 가능한 CTA만 풀필**로 구분한다. |
| `FilterChip`/`AssistChip`의 선택색을 커스터마이징하면서 `selectedContainerColor`/`selectedLabelColor`만 지정하고 `selectedLeadingIconColor`는 빠뜨리기 | 아이콘이 M3 기본 틴트로 남아 라벨 텍스트와 다른 색이 되고, 커스텀 배경색과의 대비도 검증되지 않은 상태로 남는다(Collection에서 실제 발견: 골드 배경에 연한 크림색 체크 아이콘이 ~1.4:1로 바래 보임). **선택 색상을 커스터마이징할 때는 container/label/leadingIcon 세 가지를 항상 함께 지정**한다. |

### 확인 질문 (작업 시작 전 스스로 답해볼 것)

1. 지금 만들려는 카드/버튼의 코너 반경은 몇 dp이며, 어떻게 8dp를 보장하는가?
2. 지금 배경색으로 쓰려는 `colorScheme` 롤은 부모 컨테이너와 같은 색인가, 다른 색인가?
3. 재구매 후보/비선호를 화면에 표시한다면 색상으로도 구분되는가, 텍스트로만 구분되는가?
4. 새로 추가하는 dp 값이 `DrinkDiarySpacing` 토큰(4/8/12/16/24/32) 안에 있는가?

답이 불확실하면 코드를 작성하기 전에 `Components.kt`/`Theme.kt`/`Color.kt`를 먼저 읽는다 — 이 문서에 없는 세부사항은 실제 구현 코드가 최종 근거다.
