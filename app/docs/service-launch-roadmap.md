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

**설정(Settings) 화면 + Auto/Dark/Light 테마 전환 (2026-08-08)** — 세션 초반 "다크 무디 테마를 항상 강제 적용, 토글 아님"으로 명시적으로 결정했던 것을 사용자 요청으로 명시적으로 뒤집었다. 새 설치 기본값은 **자동(시스템 설정 따름)**으로 사용자가 직접 확인(AskUserQuestion).

- 기존 Room/UseCase 계층 패턴을 그대로 따라 `ThemeMode` enum + `ThemePreferenceRepository`(SharedPreferences 구현, 새 라이브러리 의존성 추가 없음) + `Observe`/`SetThemeModeUseCase` 추가.
- `MainActivity`가 테마 설정을 구독해 `resolvedDarkTheme`(Auto→`isSystemInDarkTheme()`, Dark→true, Light→false)를 계산, `DrinkDiaryTheme`에 전달. 상태표시줄/내비게이션 바 아이콘 색도 `LaunchedEffect`로 테마 전환 시 함께 반응하도록 변경(과거엔 `enableEdgeToEdge`에서 다크 고정 1회 호출).
- 하단 내비게이션에 3번째 탭 "설정" 추가 — `scaffold-toolbar-plan.md`의 enum(`DDTopLevelTab`) 리팩터링은 그 문서 자체가 범위 밖이라고 명시한 별도 작업이라 이번엔 기존 문자열 태그(`"dashboard"`/`"collection"`) 관례를 따라 `"settings"`만 추가.
- 실기기(라이트/다크 모드 모두)에서 Dashboard 캘린더 포함 전체 화면이 라이트 테마에서도 깨지지 않고 읽히는 것을 확인 — 이번 세션에 추가된 컴포넌트들이 전부 `colorScheme.*` 롤 참조라 라이트 테마 전용 버그 없이 자동으로 대응됨. 다만 다크 화면들처럼 3라운드 UI 품질 루프로 라이트 테마를 전면 검증하지는 않음(범위 밖으로 명시, 필요 시 후속 라운드).
- 앱 완전 종료 후 재시작 시 선택한 테마가 유지되는지(SharedPreferences 영속성) 실기기에서 확인 완료.

**Dashboard 인포그래픽: 타입별 평점 비교 + 주간 트렌드 미니 그래프 (2026-08-08)** — "캘린더만 있으니 대시보드가 밋밋하다"는 사용자 피드백에 따라 진행. 사용자가 차트/드로잉/애니메이션에 외부 라이브러리 사용을 명시적으로 허용해, 프로젝트 최초로 새 UI 라이브러리(Vico)를 도입했다.

- **의존성 추가**: `com.patrykandpatrick.vico:compose-m3` v3.2.3(Compose 네이티브, Material3 컬러 스킴 자동 연동). 값이 3개뿐인 타입별 평점 비교는 시각적 일관성을 위해 라이브러리 없이 순수 Compose(`DDTypeRatingComparisonCard`)로 구현하고, 실제 "차트"가 필요한 8주 트렌드만 Vico(`DDWeeklyTrendChart`)를 사용 — 라이브러리 사용을 필요한 곳에만 한정.
- `ObserveWeeklyTrendUseCase`(신규) — `DDDashboardCalendar`와 동일한 패턴으로 `selectedPeriod`와 무관하게 항상 최근 8주(월요일 시작) 고정 창을 `Flow<List<Int>>`로 노출.
- `DashboardSummary`에 `wine/whiskey/beerAverageRating: Double?` 3개 필드 추가(해당 타입 기록 0건이면 null → 카드에 "-" 표시).
- **버그 1 — Vico 크래시**: `CartesianChartModelProducer.runTransaction { columnModel { series(weeklyCounts) } }`에 `weeklyTrend` StateFlow의 초기값(`emptyList()`)이 그대로 전달되어 `IllegalArgumentException("Series can't be empty")`로 즉시 크래시. `weeklyCounts.isNotEmpty()`일 때만 트랜잭션을 실행하도록 가드 추가로 해결.
- **버그 2 — 레이아웃 붕괴**: 캘린더+주간 트렌드 차트를 non-scrollable `Column` 안에 순서대로 쌓고, 그 아래 히어로/통계/타입별 평점 카드는 `Modifier.weight(1f)`를 준 `Box`/`LazyColumn`에 맡기는 기존 구조에서, 트렌드 차트(~140dp)가 추가되며 고정 높이 요소의 합이 뷰포트를 넘어서 weighted 영역이 0에 가깝게 coerce됨 — 히어로 카드 이하 전부가 화면에 렌더링되지 않는 상태가 됨(스크린샷으로 발견, `uiautomator dump`로 실제 좌표 확인 후 원인 특정). **해결**: `DashboardRoute` 전체를 하나의 스크롤 가능한 `LazyColumn`으로 재구성 — 세그먼트 컨트롤/캘린더/트렌드 차트도 `item{}`으로 넣고, 상태별 성공 콘텐츠는 `LazyListScope` 확장 함수(`dashboardSuccessItems`)로 변경. "항상 보이는" 요소라는 요구사항은 `when(state)` 분기 밖에 두는 것으로 유지되고, "고정 헤더" 여부와는 무관함을 확인 — `design-system.md` 14절에 재발 방지 규칙으로 기록.
- 실기기(SM_F971N)에서 주간/월간/연간 탭 전환 시 트렌드 차트는 그대로 유지되고 히어로 카드·종류별 비중·타입별 평점 비교만 갱신되는 것을 확인.

**Dashboard 재배치: 캘린더 하단 이동 + 접기/펼치기 (2026-08-08)** — 실기기 검증에서 "캘린더가 화면을 너무 많이 차지해 한눈에 파악이라는 대시보드 목적이 흐려진다"는 사용자 피드백에 따라 진행. UX 논의에서 "요약 통계를 먼저 보여주고 캘린더는 필요할 때 찾아보는 보조 정보로 내리자"는 방향에 합의.

- **순서 변경**: 세그먼트 컨트롤 → 주간 트렌드(그대로 상단, 항상 표시) → 상태별 콘텐츠(히어로/통계/종류별 비중/타입별 평점 비교/재구매·비선호 목록) → **캘린더(신규 위치, 최하단)**.
- **캘린더 접기/펼치기**: `DDDashboardCalendar`에 `expanded`/`onToggleExpanded` 파라미터 추가, 기본 접힘. 접힌 상태는 "이번 달 기록 N일" 한 줄 요약만 보여주고, 펼치면 기존 월 그리드가 나타난다. 토글 UI는 새로 만들지 않고 `RecordEditorScreen`의 기존 "세부 평가 ▼/접기 ▲" 패턴(`TextButton` + `secondary` 컬러)을 그대로 재사용. 그리드 등장/소멸에는 `research-component-motion-ux.md`가 처음부터 권장했지만 어디에도 적용된 적 없던 `AnimatedVisibility(fadeIn()+expandVertically())` 조합을 이번에 처음 실제로 적용.
- **디자인 스킬 확인**: 사용자가 "design 관련 skill이 있으면 설정해서 인포그래픽 개선 방안을 마련해달라"고 요청 — 확인 결과 프로젝트/전역 어디에도 전용 디자인 Skill은 없음(`jetpack-compose-m3`=Wear OS 전용, `styles`=실험적 Compose Styles API, 둘 다 무관). 이 프로젝트에서 디자인 리서치 역할은 이미 `agy-research` 스킬(`research-immersive-ui.md`, `research-component-motion-ux.md`를 생산한 도구)과 `ui-quality-reviewer` 서브에이전트가 맡고 있어, 새 스킬을 만들지 않고 그대로 활용하기로 함 — 인포그래픽 개선 제안 리서치를 별도로 진행(아래 항목).
- 실기기 대신 에뮬레이터(`Medium_Phone_API_36.1`)에서 검증 — 이 세션 중 실기기(R3KL406ERJM)가 반복적으로 adb 연결이 끊겨 에뮬레이터로 전환. `uiautomator dump` 기반 정확한 좌표로 접기/펼치기 양방향 토글, "이번 달 기록 3일" 캡션, 순서 변경을 모두 확인.

**인포그래픽 대시보드 개선 제안 리서치 (2026-08-08)** — `agy-research`(`gemini-3.5-flash-medium`)로 델리게이트해 Spotify Wrapped/Duolingo/Oura Ring/Notion류 습관 트래킹 앱의 인포그래픽 패턴을 조사하고, DrinkDiary 대시보드 4개 컴포넌트(히어로 통계 카드, 주종 비율 바, 종류별 평점 비교, 주간 트렌드 차트)에 대한 구체적 before/after 제안을 `app/docs/design/research-infographic-dashboard.md`에 저장. **이 문서는 승인된 계획이 아니라 검토용 제안서** — 자동 구현하지 않았다.

**핵심 발견사항**:
- 우선순위 1위 제안: 히어로 카드를 48sp Serif 골드 숫자 + 감성 서브텍스트 + 골드-로즈 그라데이션 보더로 전면 개편 — 대시보드 진입 시 첫인상에 가장 큰 영향, 백엔드/DB 변경 없이 Compose 스타일링만으로 구현 가능.
- 우선순위 2위: 주종 비율 바를 3분할 사각 바 → 캡슐형 트랙 + 미니 아이콘 + 인라인 Serif 텍스트로 전환.
- 우선순위 3위: 주간 트렌드 차트에서 Vico 격자선 제거, 막대 상단 라운딩 + 세로 골드 그라데이션 채우기.
- 종류별 평점 비교는 단색 비례 바 대신 5단계 다이아몬드 세그먼트 게이지(4.0 이상=골드, 3.0 미만=로즈)로 전환하는 안도 제시됨.
- 다음 라운드에서 이 중 어떤 항목을 실제로 구현할지는 사용자 승인 필요.

**인포그래픽 개선 TOP 3 구현 (2026-08-08)** — 위 제안서의 우선순위 1→2→3을 사용자 승인 하에 순서대로 구현.

- **히어로 카드**: `DDHeroSummaryCard`의 `value`를 `displaySmall`→`displayMedium`+`FontWeight.Bold`+Gold(`secondary`) 색상으로 확대, 옵트인 `caption` 파라미터 추가("DrinkDiary에 담긴 나만의 테이스팅 기록"). 보더(gold-to-rose 그라데이션)는 기존 `ddGlassBorderModifier()`로 이미 충족되어 있어 변경 없음.
- **주종 비율 바**: `DDDrinkTypeRatioCard`를 10dp 각진 바 → 28dp 캡슐 트랙(`RoundedCornerShape(14.dp)`)으로, 세그먼트가 15% 이상일 때만 인라인 Serif % 텍스트, 최대 비중 세그먼트에 얇은 Gold 보더로 강조. 미니 주종 아이콘은 **범위 밖으로 제외** — `material-icons-extended`(대용량 아이콘팩) 추가가 필요해 "불필요한 의존성 지양" 원칙과 충돌.
- **주간 트렌드 차트**: Vico 컬럼을 상단 라운딩(`RoundedCornerShape(topStart/topEnd = 6.dp)`) + 세로 Gold 그라데이션 채우기로 커스텀. Vico API는 추측하지 않고 Gradle 캐시의 `compose-android-3.2.3.aar`를 직접 추출해 `javap`로 `rememberLineComponent`의 정확한 파라미터 순서를 확인하고, 공식 샘플(`RockMetalRatios.kt`, `ElectricCarSales.kt`)로 `Fill(Brush)`/`ColumnCartesianLayer.ColumnProvider.series()` 사용법을 검증 — 결과적으로 첫 빌드에 컴파일 에러 없이 성공(이전 라운드의 "추측하지 않기" 교훈이 실제로 시간을 절약함). 피크/현재 주차 강조 도트는 **범위 밖으로 제외** — Vico 커스텀 `CartesianLayerDecoration` 구현이 필요한 별도 과제.
- 에뮬레이터에서 확인: 세 컴포넌트 모두 정상 렌더링, 주간/월간 탭 전환 시 트렌드 차트 유지 + 통계/비율 바 갱신에 회귀 없음.

**기능 감사 및 제품 방향 재정립 (2026-08-08)** — "디자인이 너무 old하다, 대규모 UI/GUI/VI 개편이 필요하다"는 판단에 따라, **개편 전에 기능부터 점검**하기로 하고 사용자가 제시한 5가지 가치 기준으로 전수 감사를 진행했다. 산출물은 `app/docs/product/feature-audit-and-direction.md`(핵심)와 `app/docs/research/tasting-vocabulary.md`(어휘 명세). **이번 라운드는 구현 없이 문서와 스킬 정비까지만.**

**핵심 발견사항 — 5개 기준 모두 갭, 그중 3개는 기능 자체가 부재**:
- **기록 UX(심각)**: 현실적 기록 1건에 12~13탭. 병목은 `DDRatingInput`이 탭 가능한 별이 아니라 `-`/`+` 스테퍼라는 점 — 별점 4.0에만 8탭이 든다. 임시저장 없음(프로세스 종료 시 작성분 소실), 사진 피커 취소 시 기존 사진이 지워지는 버그도 발견.
- **테이스팅 칩(미구현)**: 자유 입력 필드 하나뿐. 앱 전체에 다중 선택 칩 UI가 존재하지 않음.
- **필터(심각)**: `FilterPanel`이 `LazyColumn` 바깥에 고정되어 폰에서 약 140dp를 상시 점유 — 사용자가 지적한 그 문제가 코드로 확인됨. 가로 모드에서는 드롭다운으로 바뀌며 오히려 더 커진다. 검색·정렬 전무, 필터 축 2개뿐.
- **주종 커스텀(미구현)**: 설정 화면 전체가 45줄, 테마 모드 하나뿐. `DrinkType`이 6곳 이상 하드코딩되어 선행 리팩터링 필요.
- **건강 인사이트(심각)**: `DrinkRecord`에 도수·용량·잔수·칼로리 필드가 **하나도 없음**(grep 0건 재확인). 대시보드는 개수와 평균만 계산하며, `periodRange`가 단일 기간만 계산해 전 기간 대비는 현 구조로 계산 자체가 불가능.

**확정된 설계 결정 3가지**(AskUserQuestion으로 사용자 직접 선택):
- 건강 데이터는 **주종별 기본값 자동 적용 + 칩 보정** — 입력 부담을 늘리지 않으면서 순수 알코올 환산을 가능하게 함(맥주 5%/500ml, 와인 12%/150ml, 위스키 40%/30ml).
- 기존 **세부 평가 4항목 별점을 테이스팅 칩으로 대체** — 역할 중복 해소 + 입력 속도 개선 + 태그 빈도라는 집계 가능한 취향 데이터 확보.
- 재정립된 방향: **"기록은 SNS만큼 가볍게, 회고는 데이터로 깊게"**. 기록 자체가 무거워 컬렉션이 쌓이지 않으면 뒤의 모든 가치가 성립하지 않는다는 판단.

**구현 순서 권고**: 스키마 변경(태그 + 도수/용량 + `detailRating` 정리)을 **마이그레이션 1회로 묶고** DB 버전 2→3. 별점 UI 교체는 스키마 무관이라 가장 먼저 체감 효과를 낼 수 있다. 상세는 감사 문서 5절.

**다음 라운드 = 대규모 UI/GUI/VI 개편** — 단, 이 문서의 기능 방향이 먼저 확정되어야 개편 대상이 명확해지므로 **기능 → UI 순서를 지킨다**.

**DB 스키마 확정 v2→v3 + 테이스팅 태그 도입 (2026-08-09)** — 감사 문서의 방향을 구현하기 시작. 사용자 요청 순서는 Rating Input 개선이었으나, **스키마를 먼저 확정하지 않으면 나중에 또 마이그레이션이 필요해 호환성 문제가 생긴다**는 판단으로 스키마를 선행했다. 감사에서 스키마 변경이 필요한 항목은 태그와 도수/용량 둘뿐이었으므로 **이번 한 번으로 예견 가능한 스키마 변경이 모두 끝났다**.

- **태그는 한국어 라벨이 아니라 안정적 ASCII 키로 저장**(`|citrus|oak|`). 이번 라운드의 핵심 결정 — DB에 `"알코올 스파이시"`를 넣었다면 사용자가 요청한 라벨 축약이 데이터 마이그레이션을 또 유발했을 것이다. 키/라벨을 분리해 **칩 문구 수정이 순수 표시 계층 변경**이 되었고, 8자였던 `알코올 스파이시`→`알싸함`, `부드러운 목넘김`→`술술 넘김` 등 전 라벨을 5자 이하로 정리했다. 공통 어휘는 주종을 가로질러 같은 키를 공유해(와인·위스키·맥주의 시트러스가 모두 `citrus`) 취향 프로파일 집계의 기반이 된다.
- **도수·용량의 NULL = 추정치**. 별도 플래그 컬럼 없이, 미입력이면 주종 기본값(맥주 5%/500ml, 와인 12%/150ml, 위스키 40%/30ml)으로 해석하고 `isIntakeEstimated`로 구분한다. 입력 UI는 Rating Input 개선과 함께 다음 라운드.
- **세부 평가 4개 컬럼 제거**. 숫자를 맛 표현으로 옮길 정직한 규칙이 없고, v2 마이그레이션이 네 값을 대표 별점 복사본으로 채워 대부분의 행에 실제 정보가 없었다. `minSdk 35`(SQLite 3.44)라 테이블 재생성 없이 `ALTER TABLE DROP COLUMN`으로 처리.
- **마이그레이션 실검증**: 앱을 지우고 새로 깔면 마이그레이션이 실행되지 않으므로, v2 데이터 30건이 있는 에뮬레이터에 덮어 설치해 확인했다. `user_version` 2→3, 세부평가 컬럼 삭제, 신규 3개 컬럼 추가, **30행 전부 보존**. 저장된 값이 라벨이 아닌 키(`|citrus|herbal|`)인 것까지 DB에서 직접 확인.
- **버그 — Compose 컴파일/런타임 버전 불일치(선행 결함)**: `FlowRow`가 `NoSuchMethodError`로 크래시. 원인은 이번 코드가 아니라 **Vico 추가 시점부터 잠복해 있던 스큐**였다 — `composeBom 2024.09.00`이 컴파일을 foundation 1.7.2에 묶는 동안 Vico가 런타임을 1.11.1로 끌어올려, 그 사이 시그니처가 바뀐 API를 처음 쓰는 순간 터졌다. 다른 화면들은 그 구간에서 안 바뀐 API만 써서 우연히 무사했던 것. **BOM을 2026.05.01로 올려 양쪽을 1.11.2로 일치**시켜 근본 해결했다(사용자 선택). 컴파일러가 실제 실행 버전을 검증하게 되었으므로 위험이 줄어든 방향이다.
- 알게 된 것: DB 파일을 꺼내 볼 때 `-wal`/`-shm`을 같이 복사하지 않으면 방금 저장한 행이 안 보인다. WAL 모드라 본 파일에 아직 반영되지 않았을 뿐인데 저장 실패로 오인하기 쉽다.
- **남은 검증**: Compose 1.7→1.11 변경이 다른 화면에 준 영향은 이번에 전수 확인하지 않았다. 다음 UI 개편 라운드에서 전 화면 회귀 검증 필요.

**Skill 정비 (2026-08-08)** — 이 세션에서 반복된 작업과 반복된 실패를 스킬로 고정했다.
- `verify-build` 갱신: 폴더블 다중 디스플레이에서 `screencap` 경고 텍스트가 PNG를 깨뜨리는 문제(`-d <displayId>` 필요), 콜드 스타트 스플래시가 기존 안내(2~4초)보다 훨씬 길다는 점, 에뮬레이터가 스냅샷 로드 실패로 조용히 죽어 `-no-snapshot` 재기동이 필요한 경우, **스테일 덤프가 유효한 덤프처럼 보이는 함정**(`adb uiautomator`에 `shell` 누락 시 이전 덤프가 그대로 pull됨), 좌표는 스크린샷에서 환산하지 말고 덤프 bounds에서 취할 것.
- `verify-external-api` 신규: 외부 라이브러리 API를 추측해 컴파일 에러를 낸 전례와 검증 후 첫 빌드에 성공한 전례가 모두 Vico에서 나왔으므로 절차를 고정 — Gradle 캐시 아티팩트 → `javap`로 시그니처 → 공식 샘플 소스 → 문서 순.
- `sync-design-docs` 신규: `DD*` 컴포넌트 변경 시 `design-system.md` 표·제약조건·화면별 목록과 로드맵 로그를 갱신하고 `agy-commit`으로 커밋하는 절차 고정.

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
