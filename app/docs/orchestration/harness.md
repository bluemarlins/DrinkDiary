# Agent Harness — 코드 일관성 규칙

이 문서는 Claude와 `agy` CLI를 포함해 이 저장소에서 코드를 작성하는 **모든 에이전트가 공통으로
따라야 하는 코드 규칙**이다. `agy`를 호출하는 모든 프롬프트에는 이 문서 경로
(`app/docs/orchestration/harness.md`)를 컨텍스트로 명시한다.

역할 분담·문서 경로·위임 조건 같은 **운영 규칙**은 루트 `AGENTS.md`에 있다(모든 AI 모델 공용
원본이며 `CLAUDE.md`는 이 파일을 가리킨다). 이 문서는 그중 **코드에 적용되는 규칙**만 다룬다.

세부 배경은 `../specs/developer/software-architecture.md`,
`../specs/developer/build-and-test.md`, `../specs/designer/design-system.md`를 참조하되, 이 문서는
그 요약이자 강제 규칙 목록이다. 제품 판단 기준은 `../specs/planner/design-principles.md`의
의사결정 필터를 따른다.

## 1. 아키텍처 규칙

- 의존 방향은 항상 `UI(Compose) -> ViewModel -> UseCase -> Repository -> DAO(Room) -> Database` 한
  방향이다. 역방향 참조 금지.
- 새 비즈니스 로직/검증 규칙은 `domain/usecase`에 둔다. ViewModel이나 Composable에 검증 로직을
  넣지 않는다.
- `domain/model`은 Android/Room에 의존하지 않는 순수 Kotlin이어야 한다.
- Room 스키마 변경 규칙은 **출시 여부에 따라 다르다.**
  - **게시 후(Play 스토어 배포 이력이 있는 경우)**: 새 마이그레이션을 반드시 추가한다
    (`DrinkDiaryDatabase`의 `MIGRATION_x_y` 패턴 참고). 마이그레이션 없는 스키마 변경은 금지.
  - **출시 전(현재 상태)**: 보호할 실사용자 데이터가 없으므로 스키마를 새로 정의할 수 있다.
    호환을 위한 마이그레이션을 억지로 쌓지 않는다. 단, 스키마를 재정의하기로 한 **결정과 근거를
    문서에 남긴 뒤**에만 수행한다(예:
    `../departments/planner/problem-definition.md` 7-1절 — 맥주 주종 제거).
  - 첫 게시 시점에 이 항목을 "게시 후" 규칙으로 전환하는 것은 Release/Compliance 부서의 책임이다.
- Enum은 문자열로 저장한다(ordinal 저장 금지).
- 로컬 우선(local-first) 설계를 유지한다. 백엔드/클라우드는 명시적으로 요구될 때만 도입한다.
- 구조적 로컬 영속성은 Room, 키-값 설정은 SharedPreferences가 아니라 DataStore를 쓴다.
- DB/파일/네트워크 작업은 메인 스레드에서 실행하지 않는다. 비동기·반응형은 Coroutines와 Flow를
  기본으로 한다.
- 기술 스택 기본값: Kotlin, Jetpack Compose, Material 3, AndroidX/Jetpack 공식 컴포넌트.

## 2. UI 규칙

- Composable은 최대한 stateless로 만들고 상태/이벤트는 상위에서 주입한다.
- 화면별 커스텀 UI보다 `ui/component/Components.kt`의 `DD*` 공용 컴포넌트 재사용을 우선한다.
  기존 컴포넌트로 표현 가능한 UI를 새로 만들지 않는다.
- 삭제처럼 되돌리기 어려운 액션은 항상 `DDDestructiveButton` 또는 destructive 색상의
  `DDTextButton`을 사용한다. 한 화면의 Primary 액션은 1개로 제한한다.
- 색상/타이포/간격은 `ui/theme`의 토큰(`DrinkDiaryTheme`, `DrinkDiaryThemeTokens` 등)을 통해서만
  사용한다. 화면에 하드코딩된 dp/Color 값을 새로 추가하지 않는다.
- 상태 호이스팅과 단방향 데이터 흐름(UDF)을 따른다. 화면 수준 상태는 ViewModel이 관리한다.
- 화면 수준 Composable과 재사용 컴포넌트를 분리한다. 재사용 컴포넌트는 `ui/component`에 둔다.
- 불필요한 리컴포지션을 피한다. 안정적(stable) 모델과 `remember`/`derivedStateOf`를 적절히 쓴다.
- 큰 목록은 `LazyColumn`/`LazyRow`/`LazyVerticalGrid`로 렌더링한다.
- 이미지는 효율적으로 로드하고 대용량 비트맵을 메모리에 붙들지 않는다.

## 3. 코드 스타일 / 자동 게이트

- 포맷팅은 사람이 아니라 도구가 강제한다: ktlint(`:app:ktlintCheck` / `:app:ktlintFormat`)를
  병합 전 필수로 통과시킨다. 어떤 모델이 코드를 작성했든 최종 포맷은 ktlint 규칙으로 수렴한다.
- Android Lint(`:app:lint`)를 반드시 통과한다.
- 새 파일에는 주석을 최소화한다. WHY가 비자명한 경우에만 한 줄 주석을 추가한다.
- 관용적이고 읽기 쉬운 Kotlin을 쓴다. 가능한 곳에서는 불변 상태를 선호한다.
- data class / sealed interface / enum 같은 타입 안전한 모델을 우선한다.
- 함수와 Composable은 작고 한 가지 일에 집중하게 유지한다. 중복 로직을 만들지 않는다.
- Deprecated API는 명확한 이유 없이 도입하지 않는다.

## 4. Definition of Done (모든 태스크 공통)

태스크를 완료로 표시하기 전에 아래를 전부 만족해야 한다.

1. `.\gradlew.bat :app:testDebugUnitTest` 통과
2. `.\gradlew.bat :app:ktlintCheck` 통과
3. `.\gradlew.bat :app:lint` 통과
4. 태스크 범위와 무관한 파일 diff가 없음 (`git status`/`git diff`로 확인)
5. 위 1~2절 아키텍처/UI 규칙 준수
6. 새로 추가된 UseCase/Mapper/ViewModel 로직에는 대응하는 단위 테스트가 존재

## 5. 브랜치 / 커밋 규칙

- 태스크 단위 작업은 `agy/<phase>-<slug>` 형식의 브랜치에서 진행한다(예:
  `agy/phase1-export-csv`).
- `agy`가 만든 변경은 신뢰하지 않은 산출물로 취급한다. Claude가 `git diff`로 검수하고 Definition
  of Done을 통과한 뒤에만 로컬 커밋한다.
- 원격 push는 매 건마다 사용자 확인을 받은 뒤에만 수행한다. "자율 반복" 승인은 로컬 반복 작업에만
  적용되며 push를 자동으로 승인하지 않는다.
- **[사고 이력 — 2026-08-03] agy 호출 중 하나가 다른 uncommitted 작업 전체를 날린 사건**: 여러
  `agy` 태스크(일부는 백그라운드)를 동시에 띄운 상태에서, 그 중 하나가 워킹 트리를 마지막 커밋
  상태로 완전히 되돌리는 부작용을 일으켰다 — Insights UI 전체, Robolectric 설정, 네비게이션 배선
  등 미커밋 작업이 통째로 사라짐(Claude의 대화 컨텍스트에 파일 전문이 남아있어 복구함).
  `--add-dir`는 편집 "범위"만 제한할 뿐, agy가 내부적으로 실행할 수 있는 셸/git 명령까지 막아준다는
  보장이 없다는 뜻이다. **따라서**:
  1. **검증이 끝난 작업은 즉시 커밋한다** — 여러 파일/기능을 모았다가 한 번에 커밋하지 않는다.
     커밋되지 않은 변경은 언제든 사라질 수 있는 것으로 취급한다.
  2. **agy 호출을 동시에(백그라운드 포함) 여러 개 띄우지 않는다** — 특히 다른 곳에 미커밋 변경이
     남아있는 상태에서는 절대 금지. 한 번에 하나씩, 끝나면 바로 검수·커밋 후 다음 호출.
  3. agy 호출이 "timeout"/"ERROR"를 반환해도 **실제로는 파일을 이미 썼을 수 있다** — 실패로
     보고돼도 항상 `git status`로 실제 워킹 트리 상태를 먼저 확인한 뒤 재시도 여부를 결정한다.

## 6. 에이전트 역할 분담

| 역할 | 담당 |
| --- | --- |
| 기획/PRD/아키텍처 결정/코드 리뷰/테스트 설계 | Claude |
| 벤치마킹 조사, 정해진 디자인 시스템 내 UI 다듬기, 반복적 보일러플레이트 코드 작성 | `agy` |

호출 방법과 태스크 유형별 모델/플래그는 `agy-playbook.md`를 따르되, agy에 위임하기로 한 이후
"어떤 foundation 모델/effort로 부를지"는 아래 6-2절 매트릭스로 정한다.

> **참고**: `agy`/Claude 양쪽 다 구독·크레딧 잔여량을 코드/CLI로 조회할 방법이 없음을 확인함
> (2026-08-09). 따라서 배분 기준은 크레딧 상태가 아니라 **태스크 특성과 각 foundation 모델의
> 특성**으로 세분화한다. 크레딧 조회 API가 향후 열리면 6-2절에 별도 축으로 추가한다.

### 6-1. Claude vs agy 배분 시 확인할 것 (매 태스크마다)

기본 원칙(위 표) 외에, 태스크를 배분하기 전에 **그 시점에 실제로 사용 가능한 도구 상태**를 확인해
근거로 삼는다 — 매번 새로 확인한다(세션마다, 기기 연결 등은 바뀔 수 있음).

1. **설치된 Skill**: 관련 Skill이 있고 실제로 이 태스크에 적용 가능한지 확인한다(이름만 보고
   판단하지 않는다 — 예: `jetpack-compose-m3` skill은 이름과 달리 **Wear OS** Compose M3 전용이라
   이 앱의 일반 모바일 Compose UI 작업에는 적용되지 않는다). Skill이 실제로 맞으면 Claude가 그
   Skill을 로드해 직접 수행하는 쪽에 무게를 둔다.
2. **Android CLI/기기 상태**: `adb devices`로 연결된 실기기/에뮬레이터가 있는지 확인한다. 있으면
   agy가 작성한 UI든 Claude가 작성한 UI든 커밋 전에 실제로 `installDebugApk`(루트
   `build.gradle.kts` 태스크)로 설치해 시각적으로 확인한다. 연결된 기기가 없으면 컴파일/린트/
   유닛테스트까지만 검증 가능하다는 한계를 사용자에게 명시한다.
3. **MCP 연결 상태**: 해당 세션에서 실제로 인증·연결된 MCP 서버가 있는지 확인한다(미인증 서버는
   근거로 쓰지 않는다). Android/모바일 관련 도구가 연결되어 있지 않다면 이 판단에서는 제외한다.

위 확인 결과는 태스크 배분 근거로 `task-log.md`의 해당 항목 "비고"에 짧게 남긴다.

### 6-2. agy 모델 로스터 (4개 고정)

`agy models`는 11개 모델을 노출하지만, 이 저장소에서 실제로 쓰는 것은 **아래 4개뿐이다**.
로스터를 좁게 유지하는 이유는 비용이 아니라 판단의 일관성이다 — 선택지가 많으면 매번 다른
모델을 고르게 되고, 어떤 모델이 어떤 작업에 통했는지에 대한 근거가 쌓이지 않는다.

로스터 설계 원칙: **4개가 서로 대체 불가능한 역할을 하나씩 가질 것.** 역할이 겹치는 모델은
넣지 않는다.

| 모델 | 역할 | 대표 작업 | 실사용 이력 |
| --- | --- | --- | --- |
| `gemini-3.5-flash-medium` | 기본 작업마 — 정형·반복 | 기존 패턴 복제형 CRUD UseCase, Mapper, 정형 보고서 | P0-4(벤치마킹), P2-5(CSV export) 게이트 통과 |
| `gemini-3.6-flash-high` | 창의·시각·멀티모달 | UI 다듬기, 카피/네이밍, `generate_image` 에셋 생성 | P1-7(아이콘 PNG), P2-3(인사이트 UI), P1-1/P1-4 |
| `gemini-3.1-pro-high` | 무거운 구현 | 명세가 확정된 다단계 계산/알고리즘 로직 | P2-2(고급 인사이트 UseCase) 게이트 통과 |
| `claude-sonnet-4-6` | 최종 승급 티어 | Gemini 계열이 반복 실패한 작업 | 없음 — 첫 사용 시 결과를 이 표에 기록할 것 |

**`gemini-3.1-pro-high` 실사용 기록 (2026-08-14, `ObserveTasteProfileUseCase`)**: 명세를 상세히 준 결과
**메인 알고리즘은 정확했다** — 평균 기준 판정, 양쪽 최소 표본, Unsure 제외, Combined의 shared 축 한정까지
모두 지시대로였다. 반면 **테스트는 폐기했다**: 프로젝트에 없는 MockK를 임의로 도입했고, `value class`를
목킹하는 접근이었다("가짜 구현을 만들어라"고 명시했는데 무시). 또 `--add-dir`를 줬는데도 저장소가 아닌
agy 자신의 스크래치(`~/.gemini/antigravity-cli/scratch/DrinkDiary/`)에 파일을 썼다 — 이미지 생성 때와
같은 현상이며, **호출 후 저장소가 아니라 스크래치를 확인해야 한다.**

→ 교훈: **구현은 위임 가능, 검증은 아니다.** 이는 persona-registry의 "QA는 Claude 전담" 규정과 일치한다.
위임 프롬프트에 "프로젝트에 없는 라이브러리를 도입하지 마라"를 넣을 것.

로스터 밖 모델(`*-low` 변형, `gemini-3.6-flash-medium`, `gemini-3.5-flash-high`,
`gemini-3.1-pro-low`, `claude-opus-4-6-thinking`, `gpt-oss-120b-medium`)은 사용하지 않는다.
`-low` 변형은 품질 저하 대비 이득이 불분명하고 재시도 비용이 절감분보다 크며, 나머지는 위 4개와
역할이 겹친다. 로스터를 바꾸려면 이 표를 먼저 고치고 근거를 남긴다.

`claude-sonnet-4-6`을 로스터에 넣은 이유는 **부하 분산**이다. Gemini가 막힌 작업을 Claude Code
세션으로 되돌리는 대신 agy 구독 안에서 Claude 품질을 얻을 수 있는 유일한 경로다.

**승급 경로**: DoD 게이트(ktlint/lint/유닛테스트)를 2회 연속 통과하지 못하면 같은 모델로
재시도하지 않고 다음 단계로 올린다.

```text
[코드 작업]  3.5-flash-medium → 3.1-pro-high → claude-sonnet-4-6 → Claude 직접
[창의/시각]  3.6-flash-high → Claude 직접
```

승급 이력은 `task-log.md` 비고에 남긴다.

**Claude 직접 처리로 되돌리는 경우**: `claude-sonnet-4-6`까지 갔는데도 게이트를 통과하지 못하면
더 이상 agy에 위임하지 않고 Claude(오케스트레이터)가 직접 작성한다 — 태스크 자체가 6절 원칙표상
"agy 위임" 범주로 잘못 분류됐을 가능성이 크다는 신호로 본다.

## 7. 오류 처리

- Loading / Empty / Success / Error 상태를 명시적으로 모델링한다. 조용한 실패(silent failure)를
  만들지 않는다.
- 저장소·UseCase·ViewModel을 관통하는 결과/오류 타입은 `AppResult<T>`(`Success`/`Failure`)와
  `AppError`(`NotFound`, `Storage`, `Validation(SaveDrinkRecordError)`)를 쓴다.
- 사용자에게 보이는 메시지는 이해 가능한 표현으로 쓰고, 기술적 상세(스택 트레이스, 예외 클래스명)는
  디버깅 목적이 아니면 노출하지 않는다.

## 8. 테스트

- 비즈니스 로직은 테스트 가능하게 설계한다. 핵심 로직을 Android 프레임워크 클래스에 강하게
  결합하지 않는다.
- 검증·정렬·필터링·매핑·저장소 로직은 유닛 테스트를 우선한다.
- 구현이 중요한 동작에 영향을 준다면 관련 테스트 케이스를 함께 제안한다.
- 실행 명령과 Robolectric 전제는 `../specs/developer/build-and-test.md`를 따른다.

## 9. 보안 / 프라이버시

- 민감 정보를 안전하지 않은 방식으로 저장하지 않는다.
- 불필요한 권한을 요구하지 않는다. 런타임 권한은 실제로 필요한 시점에만 요청한다.
- 저장소·미디어 접근·인증은 플랫폼 권장 API를 우선한다.

## 10. 의존성 관리

- 안정적이고 유지보수가 활발한 라이브러리를 선호한다.
- 공식 AndroidX/Kotlin 대안으로 충분하면 서드파티 의존성을 추가하지 않는다.
- 의존성을 추가할 때는 왜 필요한지 근거를 남긴다.
- Gradle 설정은 최신 AGP/Kotlin 버전과 호환되게 유지한다. 버전은 `gradle/libs.versions.toml`에서
  관리한다.
