# DrinkDiary 상품화(Play Store 출시 + 광고 수익화) Roadmap

## 1. 문서 목적

이 문서는 `app/docs` 하위 문서들을 부서 기준으로 재조직화하고, DrinkDiary를 Play Store에 출시하고 광고(AdMob 등)로 수익을 내는 것을 목표로 필요한 단계를 재정의한다.

실행 주체는 Claude(오케스트레이터, 개발/디자인 구조화 담당)와 agy CLI(리서치/이미지 생성 담당, Gemini 모델 사용)로 분리한다.

## 2. 문서 구조 재편

```text
app/docs
  service-launch-roadmap.md   (본 문서, 마스터 로드맵)
  product/     # 서비스 기획
    usecase.md
    development-todo.md
  design/      # 디자인
    design-system.md
    scaffold-toolbar-plan.md
    app-icon.md
  dev/         # 개발
    software-architecture.md
    database-design.md
    ads-integration.md
  research/    # 리서치 (Phase 0 완료, Phase 1에서 추가)
    competitor-analysis.md
    ad-monetization.md
    persona-aso.md
    address-disclosure-policy.md
  legal/       # 법무/정책 (Phase 1 초안 완료)
    privacy-policy.md
    data-safety-mapping.md
    content-rating-draft.md
  marketing/   # 마케팅 (Phase 3에서 스토어 그래픽 선반영, 나머지는 Phase 5)
    store-listing-assets.md

app/store-listing/            # Play Store 리스팅용 그래픽 원본 (Phase 3 산출)
  feature-graphic.png
  screenshots/
```

기존 6개 문서는 내용 변경 없이 부서 폴더로 이동만 했다. `usecase.md`, `development-todo.md`, `software-architecture.md`, `database-design.md`, `design-system.md`, `scaffold-toolbar-plan.md`는 계속 유효한 기준 문서로 유지한다.

## 3. 코드 기준 확인된 상품화 격차 (2026-08-06 기준)

| 항목 | 근거 | 상태 |
|---|---|---|
| 릴리즈 빌드 최적화 | `app/build.gradle.kts` `isMinifyEnabled = false` | 미적용 |
| 광고 SDK | 의존성/코드 전무 | 미착수 |
| 애널리틱스/크래시 리포팅 | 의존성/코드 전무 | 미착수 |
| 하단 내비게이션 아이콘 | `DDScreenScaffold.kt`가 `Text("홈")`/`Text("목록")`로 구현, 실제 아이콘 아님 | Placeholder |
| 앱 아이콘 | `app/src/main/ic_launcher-playstore.png` | **완료 (브랜드 아이콘 확보됨)** |
| 개인정보처리방침 | 문서/URL 없음 | 미착수 (광고 SDK 연동 전 필수) |
| 데이터 안전성 양식 | 없음 | 미착수 |
| Scaffold/Toolbar 리팩토링 | `design/scaffold-toolbar-plan.md` 계획만 존재, 코드 미반영 | 계획 단계 |
| UX 마감 (`development-todo.md` Step 14) | 접근성 라벨, 오류 문구 등 점검 미완 | 미완 |
| 테스트 보강 (Step 15) | Mapper/UseCase 테스트만 존재, ViewModel/UI 테스트 없음 | 부분 완료 |

## 4. AI 오케스트레이션 원칙

| 업무 유형 | 담당 | 실행 방식 |
|---|---|---|
| 경쟁앱/시장/광고 수익성 리서치, ASO 키워드 리서치 | agy CLI | Claude가 Bash로 `agy --model gemini-*` 직접 호출, 결과를 문서로 정리 |
| 아이콘 변형, Feature Graphic, 스크린샷 배경 이미지 생성 | agy CLI | Claude가 Bash로 `agy --model gemini-*` 직접 호출, 산출물을 Android 리소스 규격에 맞게 후처리 |
| 광고 SDK 통합, 릴리즈 설정, 리팩토링, 테스트 코드 | Claude | 코드베이스 직접 수정 |
| 디자인 시스템 구조화(`DD*` 컴포넌트 API/토큰) | Claude | 코드베이스 직접 수정 |
| 스토어 카피/정책 문서 최종본 | Claude | agy 리서치 결과를 반영해 작성 |
| 산출물 병합 및 최종 결정 | Claude (오케스트레이터) | — |

### 4.1 AI 개발 환경 구성 (Prompting / Skills / Harness / Loop Engineering)

2026-08-07, Claude+agy 조합 개발 방법론(공식 Claude Code 하네스/스킬/루프 엔지니어링 가이드, Anthropic의 장기 실행 에이전트 하네스 원칙, 2025-2026 멀티에이전트 오케스트레이션 동향)을 리서치하고 이 저장소에 실제로 적용했다. "토이 프로젝트 → 실사용 가능한 수준" 격상은 이 환경을 갖춘 뒤 후속 UX 고도화 작업에서 실제로 사용해 달성하는 것이 목표다.

**구성된 것**:
- `.claude/skills/agy-research/SKILL.md` — 리서치/이미지생성 위임 표준화(모델 선택, 경로/구두점 주의사항, 산출물 라우팅)
- `.claude/skills/agy-commit/SKILL.md` — commit/push 위임 표준화
- `.claude/skills/verify-build/SKILL.md` — 빌드/테스트/설치/스크린샷 검증 시퀀스 표준화(uiautomator 좌표 스캔, 광고 배너 크롭 등 이번 세션에서 겪은 시행착오 포함)
- `.claude/agents/ui-quality-reviewer.md` — 서브에이전트. 격리된 컨텍스트에서 스크린샷을 찍고 경쟁앱 리서치/디자인시스템 발췌 + agy 독립 비평을 받아 구조화된 findings만 반환
- `.claude/settings.json` — gradlew/adb/git 읽기 명령 allowlist. **의도적으로 `agy ... --dangerously-skip-permissions` 호출 자체와 `git commit`/`git push`는 제외** — 이중 체크포인트를 유지하고 commit/push는 agy에 위임하는 관행이 흐트러지지 않도록 함
- `CLAUDE.md`에 Verification-first(스크린샷/테스트로 검증 없이 "된 것 같다" 선언 금지), Explore→Plan→Code(화면 전체/여러 파일 걸친 작업은 plan mode 우선) 규칙 추가

**UI 품질 루프** (향후 UX 고도화 작업의 표준 프로세스):
1. Claude가 화면 구현/수정
2. `verify-build` skill로 실제 에뮬레이터 스크린샷 확보
3. `ui-quality-reviewer` 서브에이전트가 agy에 독립 비평 위임 — agy는 Claude가 방금 만든 코드에 편향이 없는 별도 모델이므로 maker-checker 분리가 자연스럽게 성립
4. Claude가 findings 반영
5. 최대 3라운드 반복 또는 agy가 "추가 findings 없음"을 보고할 때 종료 — 라운드 수는 TodoWrite로 가시적으로 추적

업그레이드 경로: Compose 스크린샷 테스트(`testing-setup` skill) 도입 시 기계적 pass/fail이 생기므로, 그때 가벼운 Stop hook을 안전하게 추가할 수 있다. 지금은 UX 품질이 정성적 판단이라 무제한 자동 루프 대신 위 반자동 루프로 제한한다.

**Dashboard 3라운드 완주 (2026-08-07)** — 루프를 실제로 캡(3라운드)까지 돌려 완료:

| 라운드 | 발견 | 조치 |
|---|---|---|
| 1 (시범, 서브에이전트 등록 전이라 수동 재현) | M3 기본 퍼플 테마 노출(`Theme.kt`가 primary/secondary/tertiary만 오버라이드), 카드/세그먼트 코너 라운딩 8dp 초과, 재구매후보/비선호 카드에 썸네일·뱃지 없음(텍스트만), FAB가 마지막 카드를 가림 | `Theme.kt`에 background/surface/`*Container` 전체 롤 지정, FAB·세그먼트에 명시적 8dp shape, `DDDrinkRecordCard`에 썸네일+뱃지 추가, LazyColumn bottom contentPadding 추가 |
| 2 | 재구매후보/비선호 뱃지가 색상 구분 없이 동일해 핵심 차별화 요소(Wish/Pass)가 텍스트로만 구분됨, 나머지 카드들(`DDDashboardSummaryCard` 등)은 여전히 8dp 미준수, "종류별 비중"이 평문 텍스트, 썸네일 placeholder가 카드와 동일 색이라 안 보임 | `DDCollectionStatusBadge`를 상태별 색상 필 칩으로 분리(재구매=Cellar Green, 비선호=Rose), 남은 카드에 8dp shape 일괄 적용, 비중을 3색 프로포셔널 바+범례로 시각화, 썸네일 placeholder를 `surfaceContainerHighest`로 대비 확보 |
| 3 (최종, 캡 도달) | 회귀 없음 확인, "프로덕션 수준에 근접, 출시 막을 요소 없음"으로 판정. Non-blocking 백로그: 뱃지에 아이콘 글리프 없음(색상만으로 구분), 섹션 헤더와 뱃지 라벨 중복, "사진" 텍스트 대신 카메라 아이콘 고려, 별점 색상이 다소 탁함 | 이번 라운드 캡 도달로 보류 — 다음 UX 작업 백로그로 이월 |

이후 Collection, RecordDetail(라이트 테마 기준)도 동일 루프로 각 3라운드 완주, RecordEditor는 세션 한도로 중단(백로그, 아래 참고).

**Pinterest 몰입감 리서치 → 다크 무디(Dark & Moody) 테마 전면 전환 (2026-08-08)** — 사용자가 Pinterest의 "Mobile Application Wine"/"Mobile UI" 레퍼런스를 검토한 뒤, 완성된 라이트 크림 테마가 여전히 "평범/유틸리티적"이라고 판단. agy 리서치(`app/docs/design/research-immersive-ui.md`)를 거쳐 **다크 무디 팔레트 + 글래스모피즘 근사 + 에디토리얼 세리프 + 벤토 레이아웃**으로 전면 전환하기로 결정(AskUserQuestion으로 확정: 전면 전환, Dashboard+RecordDetail 우선). 상세 설계 결정은 `app/docs/design/design-system.md` 4.1절 참고.

| 화면 | 라운드 | 발견 | 조치 |
|---|---|---|---|
| Dashboard | 1 | 배경/카드 대비가 탁함, 카드별 글래스 보더 불일치("종류별 비중" 카드에 아예 없음), 재구매후보 뱃지/FAB가 초록 배경에 묻힘 | `DeepForest10` 배경을 더 어둡게, 전체 카드를 `surfaceContainerHigh.copy(alpha=0.88f)`+글래스 보더로 통일, 재구매 뱃지/FAB를 Gold로 재매핑 |
| Dashboard | 2 | 재구매 뱃지(`secondaryContainer`)가 FAB(`secondary`)보다 칙칙함, FAB가 광고 배너에 거의 붙음 | 뱃지를 FAB와 동일한 `secondary`로 통일(이후 3라운드에서 다시 반투명으로 조정, 아래 RecordDetail 라운드 참고), FAB에 `-12.dp` 오프셋 추가 |
| Dashboard | 3 (최종, 캡 도달) | 회귀 없음, 픽셀 샘플링으로 뱃지/FAB 색상 일치 및 FAB-배너 간격 확인. 완료 판정 | 미해결 백로그 1건 이월: sticky header 아래로 스크롤된 카드 모서리가 살짝 비치는 클리핑 |
| RecordDetail | 1 | 정보 카드에 글래스 보더 없음(Dashboard와 불일치), 사진없음 워터마크가 원본 다색 그대로 노출, "수정" 버튼이 다크 전환 전 잔재 색(연한 민트그린, `primary`) | 정보 카드에 `ddGlassBorderModifier()` 적용, 워터마크에 `ColorFilter.tint` 단색화, `DDPrimaryButton`을 `secondary`(Gold)로 재매핑 |
| RecordDetail | 2 | 재구매 뱃지(풀필 Gold)와 "수정" 버튼(풀필 Gold)이 동일 톤이라 배지가 두 번째 버튼처럼 보임, `DDRatingStars`가 여전히 `primary`(초록) 잔재색 | 배지를 반투명 fill+보더로 격하(`secondary.copy(alpha=0.18f)` + 1dp 보더), `DDRatingStars`를 `secondary`로 재매핑(Dashboard/Collection 별점에도 공유 적용됨) |
| RecordDetail | 3 (최종, 캡 도달) | 픽셀 샘플링으로 배지/버튼 위계 분리 및 별점 색상 확인, 회귀 없음. 완료 판정 | 미해결 백로그(non-blocking): 빈 값 필드가 단순 "-"만 표시, 히어로 그라디언트와 정보 카드 톤 경계가 다소 어색함 |

**이번 라운드 범위 밖(다음 라운드 백로그)**: Collection/RecordEditor 화면의 다크 무디 재검토, 진짜 backdrop blur 라이브러리 도입, 방사형 차트 테이스팅 시각화, 배경 노이즈 텍스처, 마이크로 인터랙션/햅틱, RecordEditor UI 품질 루프 재개(세션 한도로 중단된 채 남아있음).

**Collection 다크 무디 적용 + 컴포넌트/모션/섀도우 리서치 (2026-08-08)** — 위 백로그 중 Collection을 이어서 진행, 3라운드 루프 완주.

| 라운드 | 발견 | 조치 |
|---|---|---|
| 1 | `DDDrinkRecordListItem`(리스트 카드)이 무스타일 `ElevatedCard`, 종류/상태 필터의 "전체" 선택 상태가 `primaryContainer`(초록)라 배경 대비 ~1.7:1로 거의 안 보이고 선택 여부를 알리는 다른 단서(체크 아이콘 등)도 없음 | 리스트 카드를 Dashboard와 동일한 글래스 보더+`surfaceContainerHigh` 스타일로 전환, `ddFilterChipColors()` 선택 색상을 Gold(`secondary`/`onSecondary`)로 재매핑 + 8개 필터 칩 전체에 선택 시 체크 아이콘(`Icons.Filled.Check`) 추가 |
| 2 | 체크 아이콘이 `selectedLeadingIconColor` 미지정으로 M3 기본 틴트(밝은 크림색)를 써서 Gold 배경 대비 ~1.4:1로 바랜 것처럼 보임(라벨 텍스트와 불일치) | `ddFilterChipColors()`/`ddStatusFilterChipColors()` 3개 분기 전부에 `selectedLeadingIconColor`를 라벨 색상과 동일하게 명시 |
| 3 (최종, 캡 도달) | 회귀 없음, agy 독립 비평 6개 루브릭 항목 전부 PASS, "Production-Ready: YES" 판정 | 미해결 백로그(non-blocking): 썸네일이 실제 이미지 URI 없는 샘플 데이터라 "사진" 텍스트만 표시 — 실 이미지 데이터로 재검증 필요 |

병행하여 agy(`gemini-3.1-pro-high`)로 Button/Text Field/Chips/Card/Image 컴포넌트별 디자인 방향 + Compose 모션/애니메이션 + 다크모드 그림자/Elevation 리서치를 진행해 `app/docs/design/research-component-motion-ux.md`에 저장. **핵심 발견사항**:
- 정적 배지는 아웃라인/반투명, 실제 탭 가능한 CTA만 풀필 — 이미 RecordDetail 라운드에서 실전 발견한 패턴과 리서치 결론이 일치(교차 검증됨)
- 다크모드에서는 전통적 drop-shadow 대신 M3 Tonal Elevation 사용 권장 — 현재 카드들의 `elevation=0.dp` + 글래스 보더 조합이 이미 이 원칙에 부합
- 별점 채우기/리스트 아이템 등장/FAB에 `AnimatedVisibility`/`animateColorAsState`/`animateContentSize` 등 신규 라이브러리 없이 구현 가능한 모션 제안 — 다음 라운드(#29) plan mode에서 구체 적용 범위 결정 필요
- 디자인 철학 "우아한 심도(Elegant Depth)" 4원칙(어둠을 캔버스로/빛으로 안내/절제된 감정 표현/활자가 곧 디자인)을 향후 컴포넌트 결정의 판단 기준으로 채택

**컴포넌트 디자인 시스템 통일 + RecordEditor 다크 테마 완주 (2026-08-08)** — 리서치 결과를 바탕으로 아직 손대지 않았던 컴포넌트(Button 계열의 Secondary/Dropdown, Text Field, RecordEditor 별점 입력)에 "Gold=인터랙션/CTA, Green=브랜드 정체성" 규칙을 마저 적용하고, 코드 탐색 중 발견한 실제 갭(Theme.kt에 `error` 색상 롤이 전혀 채워지지 않아 M3 베이스라인 빨강이 노출되던 문제)도 함께 수정. 세션 한도로 중단됐던 RecordEditor UI 품질 루프(task #17)를 이 기회에 재개해 3라운드 완주.

- `Theme.kt`: Dark/Light 둘 다 `error`/`onError`/`errorContainer`/`onErrorContainer`를 기존 Rose 패밀리로 채움 — `DDDestructiveButton`/`DDFormErrorText`/텍스트 필드 에러 테두리가 전부 자동으로 Rose 계열로 통일됨
- `DDSecondaryButton`/`DDOptionDropdown`: Gold 아웃라인으로 재매핑
- `DDTextField`/`DDNumberField`/`DDMultilineTextField`: 포커스 시 Gold 테두리/라벨/커서
- `DDRatingInput`: 별 색상을 `primary`(초록)에서 `secondary`(Gold)로

| 화면 | 라운드 | 발견 | 조치 |
|---|---|---|---|
| RecordEditor | 1 | `DDCollectionStatusSelector`의 비선호가 재구매후보와 동일 Gold 선택색(Wish/Pass 규칙 위반), 세그먼트 선택기/별점 입력이 에러 시 캡션 텍스트만 표시(컨트롤 자체는 무변화), 공유 컴포넌트인 "뒤로" 버튼이 여전히 Green, 포트레이트 레이아웃에서 사진 섹션이 취소/저장 버튼보다 아래에 위치, 기록 일시 필드가 레거시 `android.app.DatePickerDialog`(라이트 배경+틸 M2 액센트+영어 버튼) 사용 | 비선호를 Rose(`tertiaryContainer`)로 재매핑, 세그먼트 선택기에 에러 시 로즈 테두리 추가+별점 글리프를 에러 색상으로, 뒤로 버튼 Gold로, 사진 섹션을 액션 버튼보다 위로 재배치(`RecordEditorFields`+`RecordEditorActions`로 분리), 레거시 다이얼로그를 Compose M3 `DatePickerDialog`로 교체(다크 테마+Gold 자동 상속, 버튼 라벨 한글화) |
| RecordEditor | 2 | M3 DatePicker 다이얼로그 자체는 다크+Gold로 잘 나오지만 내부 달력 텍스트("Select date", "August 2026", "S M T W T F S")가 여전히 영어 | `Locale.setDefault`/`LocalConfiguration` 오버라이드 둘 다 시도했으나 M3 DatePicker가 `text.intl.Locale`(Activity의 실제 Configuration에 종속)로 로케일을 읽어 두 방법 모두 적용 안 됨 확인 |
| RecordEditor | 3 (최종, 캡 도달) | 회귀 없음, agy 독립 비평도 프로덕션 준비 완료 판정 | 미해결 백로그(non-blocking, 의도적으로 범위 밖): 날짜 선택기 내부 달력 텍스트 로케일 — 완전 해결하려면 Activity `attachBaseContext` 수준의 앱 전역 로케일 래핑이 필요해 이번 라운드 범위 밖으로 명시적으로 남김(`DDDateTimeField` 코드 주석에 근거 문서화됨) |

**Dashboard 캘린더 연동 + 개발용 더미 데이터 30건 (2026-08-08)** — "Theme만 바뀌었지 컴포넌트 자체의 변화가 크게 느껴지지 않는다"는 사용자 피드백에 따라, 첫 컴포넌트 단위 개선으로 Dashboard의 `DDPeriodSegmentedControl`(주간/월간/연간)을 실제 캘린더 뷰와 연동했다.

- `ObserveMonthRecordDatesUseCase`(신규) — 이번 달 전체의 기록 날짜를 `Set<LocalDate>`로 관찰. `DashboardViewModel`에 `selectedPeriod`와 독립된 `recordDatesInMonth` StateFlow로 노출(캘린더 점 표시는 선택된 기간과 무관하게 항상 이번 달 전체 기준).
- `DDDashboardCalendar`(신규, `Components.kt`) — 캘린더 라이브러리 의존성 추가 없이 순수 Compose로 월 그리드 구현. 월요일 시작 요일 배치(한국의 일반적 일요일 시작 관례와 다름 — `ObserveDashboardSummaryUseCase`의 월~일 주간 정의와 하이라이트 밴드가 한 행 안에서 정확히 일치하도록 의도적으로 선택). 기록이 있는 날짜는 Gold 점, 오늘은 Gold 링, 주간 선택 시에만 이번 주 행에 반투명 Gold 밴드 하이라이트(월간/연간은 하이라이트 없음 — 사용자가 AskUserQuestion으로 직접 선택: "월 단위 그리드로 1년 전체를 하이라이트하는 건 불가능하니 이번 달만 표시").
- **개발용 더미 데이터 30건**: agy(`gemini-3.1-pro-high`)가 유명 와인/위스키/맥주 각 10종의 이름·가격·테이스팅 노트를 조사(`app/docs/dev/seed-data.md`), agy(`gemini-3.6-flash-high`)가 실제 라벨을 스크래핑하지 않고 브랜드 텍스트/로고 없는 오리지널 일러스트 30장을 생성(`app/src/debug/assets/seed_images/`, 5개 병렬 배치로 생성 시간 단축). `DebugSeeder`가 앱 시작 시 기록이 0건이면 자동으로 채워 넣는다 — Android 소스셋이 variant별로 합산되는 방식이라 "같은 클래스를 debug에 두면 main을 오버라이드"하는 방식은 컴파일 에러가 나서(`Redeclaration`), `debug`/`release` 소스셋에 각각 실제 구현/no-op을 따로 둠(`main`에는 두지 않음). `./gradlew :app:assembleRelease` + APK 내 `seed_images` 부재 확인으로 릴리즈 격리 검증 완료.

## 5. Phase별 로드맵

### Phase 0. 리서치 (완료)

담당: agy CLI 리서치 → Claude 요약/정리
산출물: `app/docs/research/competitor-analysis.md`, `ad-monetization.md`, `persona-aso.md`

**핵심 발견사항 (후속 Phase에 반영해야 할 것)**:
- 경쟁앱들의 반복 불만 = 강제 회원가입, 서버 종속, 저장 시 전면광고로 흐름 끊김, 백업/내보내기 부재 → DrinkDiary의 로컬 우선 구조가 이미 강점이나, **전면 광고 배치(Phase 2)는 저장 직후가 아니라 화면 전환 완료 후로 설계해야 함**
- 초기 MAU(1,000명 이하) 기준 광고 수익은 월 1.7만~2만원 수준으로 미미 → **Phase 2에서 배너/보상형 광고만 최소 적용 + "광고 제거 1회성 IAP"를 함께 설계**하는 것이 현실적 (신규 미완 기능이므로 별도 의사결정 필요)
- 재구매/비선호 분류, 로컬/오프라인, 회원가입 없음은 이미 DrinkDiary의 차별화 포인트로 확보되어 있음 — 스토어 카피(Phase 5)에서 이 지점을 전면에 내세울 것
- ASO 1순위 키워드: `술 기록`, `음주 일기`, `와인 다이어리`, `테이스팅 노트` — Phase 5 스토어 리스팅 작성 시 타이틀에 반영
- GDPR/UMP 동의는 타겟이 한국이어도 EEA/UK 접속자 대상으로 필요 → Phase 2 범위에 UMP SDK 포함

### Phase 1. 법무/정책 기반 (초안 완료, 검수 대기)

담당: Claude 초안 + 사용자 검수
산출물: `app/docs/legal/privacy-policy.md`, `data-safety-mapping.md`, `content-rating-draft.md`

**전제 조건 (사용자 확인 완료)**:
- 사용자는 재직 중인 회사원이며 별도 사업자등록이 불가능한 상황 → 개인(Individual) Google Play/AdMob 계정으로 진행
- 재직 회사의 겸업/부업 금지 조항 확인 완료, 문제 없음
- 개발자 표시명: `spicyrabbit`
- 개인정보처리방침 문의 연락처: `greatescape8274@gmail.com`

**리서치로 확인된 사항 (`app/docs/research/` 참고)**:
- 개인 계정 + AdMob 광고만(인앱결제 없음) 조합에서는 Play Console 스토어 설정의 주소란을 비워두면 실거주지 주소가 공개되지 않음 (`address-disclosure-policy.md`)
- 애드몹 광고 수익은 세법상 "계속적·반복적" 성격의 **사업소득으로 분류될 가능성이 높음**. 초기 소액 구간에서는 사업자등록 없이 다음 해 5월 종합소득세로 신고 가능하나, **수익 규모가 커지면 사업자등록 필요 여부를 세무사와 상담해야 함** — 로드맵 상시 확인 항목으로 기록
- 근로소득 외 종합소득이 연 2,000만원을 초과하면 건강보험 소득월액보험료가 추가 부과됨 (초기 예상 수익 규모로는 해당 없음)

**남은 작업**:
- [ ] `privacy-policy.md`, `data-safety-mapping.md`, `content-rating-draft.md` 사용자 최종 검수
- [ ] 개인정보처리방침 공개용 URL 확보 (GitHub Pages 권장)
- [ ] Phase 4에서 실제 Play Console 계정 개설 시 주소란 공란 여부 등 정책 재확인

### Phase 2. 수익화 개발 (구현 완료, 테스트 ID 사용 중)

담당: Claude
산출물: `app/docs/dev/ads-integration.md`, `com.bluemarlin.drinkdiary.ads` 패키지

- AdMob SDK 통합 완료 (`play-services-ads:25.4.0`, `user-messaging-platform:4.0.0`)
- UMP 동의 흐름 연동 (`MainActivity`에서 앱 시작 시 요청)
- 배너 광고: Dashboard/Collection 하단(바텀 내비 위) — `DDScreenScaffold(showBannerAd = true)`
- 전면 광고: RecordEditor 저장 완료 시점, 4회 저장당 1회 노출 (빈도 제한)
- 에뮬레이터에서 실제 Test Ad 렌더링 확인, 빌드/유닛테스트 통과

**⚠️ 릴리즈 전 필수**: 현재 모든 광고 ID는 Google 공식 테스트 ID다. Phase 4에서 `spicyrabbit` AdMob 계정 개설 후 실제 ID로 교체해야 한다 (`ads-integration.md` 3절 체크리스트).

### Phase 3. 디자인 고도화 (완료)

담당: agy(이미지 초안/검토) + Claude(통합/코드/자동화)

- **앱 아이콘 재제작** — agy로 기존 아이콘(경쟁 앱과 차별성 부족, 작은 크기 가독성 저하로 평가됨) 검토 후 3종 대안 생성, "재구매 하트체크 와인잔" 컨셉 채택. 적응형 아이콘 배경/전경 레이어 분리도 함께 정상화(기존에는 Android 기본 템플릿 로봇 배경이 죽은 채로 남아있었고 전경이 불투명 이미지라 사실상 비-적응형으로 동작하던 상태였음). 상세: `app/docs/design/app-icon.md`
- **하단 내비게이션 실아이콘 교체** — `DDBottomNavigationBar`/`AppNavigationRail`의 `Text("홈")`/`Text("목록")` placeholder를 `material-icons-core`의 `Icons.Filled.Home`/`Icons.AutoMirrored.Filled.List`로 교체
- **Feature Graphic** — agy로 1024x500 생성 (`app/store-listing/feature-graphic.png`)
- **스크린샷 세트** — 에뮬레이터에서 adb 자동화로 샘플 기록 3건(와인/위스키/맥주) 입력 후 Dashboard/Collection/Detail/Editor 4장 캡처, AdMob 테스트 배너는 후처리로 제거. 상세: `app/docs/marketing/store-listing-assets.md`

빌드/유닛테스트 통과, 에뮬레이터에서 실제 아이콘·화면 렌더링 확인.

### Phase 4. 릴리즈 준비

담당: Claude
- `isMinifyEnabled = true` + ProGuard 규칙 점검
- 서명(keystore)/버전 전략
- 애널리틱스/크래시 리포팅 연동
- `development-todo.md` Step 14(UX 마감)/Step 15(테스트 보강) 완료
- `scaffold-toolbar-plan.md` 리팩토링 반영

### Phase 5. 스토어 등록 & 마케팅

담당: agy(리서치) + Claude(정리)
산출물: `app/docs/marketing/`
- ASO 최적화 카피 (제목/짧은 설명/전체 설명)
- Play Console 리스팅 등록
- 단계적 출시 전략 (내부 테스트 → 비공개 → 공개 → 프로덕션)
- **⚠️ 스크린샷 재촬영 필요**: `app/store-listing/`의 기존 스크린샷 세트(Phase 3 산출)는 라이트 크림 테마 기준이라 다크 무디 전환(4.1절) 이후 실제 앱과 다르다. 전체 화면(Collection/Editor 포함) 다크 테마 적용이 끝난 뒤 재촬영해야 한다.

### Phase 6. 출시 후 운영

담당: Claude 오케스트레이션
- 광고 수익/리텐션 지표 모니터링 루프 설계
- 사용자 피드백 기반 개선 사이클

## 6. 진행 상태 트래킹

| Phase | 상태 |
|---|---|
| 0. 리서치 | 완료 |
| 1. 법무/정책 | 초안 완료, 사용자 검수 대기 |
| 2. 수익화 개발 | 구현 완료 (테스트 ID, 실 계정 전환은 Phase 4) |
| 3. 디자인 고도화 | 완료 |
| 4. 릴리즈 준비 | 대기 |
| 5. 스토어 등록/마케팅 | 대기 |
| 6. 출시 후 운영 | 대기 |
