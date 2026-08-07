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

다음 화면(Collection/Detail/Editor)도 동일 루프로 순차 적용 필요.
- 재구매후보/비선호 상태가 본문 텍스트로만 표시되어 스캔성 낮음 — 별도 컬러 칩/뱃지 필요
- FAB가 목록 마지막 카드 콘텐츠를 가림 (하단 컨텐츠 패딩 부족)

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
