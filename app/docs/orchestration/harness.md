# Agent Harness — 코드 일관성 규칙

이 문서는 Claude와 `agy` CLI(Gemini/Claude/GPT-OSS 모델)를 포함해 이 저장소에서 코드를 작성하는
**모든 에이전트가 공통으로 따라야 하는 규칙**이다. `agy`를 호출하는 모든 프롬프트에는 이 문서
경로(`app/docs/orchestration/harness.md`)를 컨텍스트로 명시한다. 세부 배경은
`app/docs/software-architecture.md`, `app/docs/design-system.md`, 루트 `CLAUDE.md`를 참조하되,
이 문서는 그 요약이자 강제 규칙 목록이다.

## 1. 아키텍처 규칙

- 의존 방향은 항상 `UI(Compose) -> ViewModel -> UseCase -> Repository -> DAO(Room) -> Database` 한
  방향이다. 역방향 참조 금지.
- 새 비즈니스 로직/검증 규칙은 `domain/usecase`에 둔다. ViewModel이나 Composable에 검증 로직을
  넣지 않는다.
- `domain/model`은 Android/Room에 의존하지 않는 순수 Kotlin이어야 한다.
- Room 스키마를 변경하는 모든 작업은 새 마이그레이션을 추가해야 한다(`DrinkDiaryDatabase`의
  `MIGRATION_x_y` 패턴 참고). 마이그레이션 없는 스키마 변경은 금지.
- Enum은 문자열로 저장한다(ordinal 저장 금지).

## 2. UI 규칙

- Composable은 최대한 stateless로 만들고 상태/이벤트는 상위에서 주입한다.
- 화면별 커스텀 UI보다 `ui/component/Components.kt`의 `DD*` 공용 컴포넌트 재사용을 우선한다.
  기존 컴포넌트로 표현 가능한 UI를 새로 만들지 않는다.
- 삭제처럼 되돌리기 어려운 액션은 항상 `DDDestructiveButton` 또는 destructive 색상의
  `DDTextButton`을 사용한다. 한 화면의 Primary 액션은 1개로 제한한다.
- 색상/타이포/간격은 `ui/theme`의 토큰(`DrinkDiaryTheme`, `DrinkDiaryThemeTokens` 등)을 통해서만
  사용한다. 화면에 하드코딩된 dp/Color 값을 새로 추가하지 않는다.

## 3. 코드 스타일 / 자동 게이트

- 포맷팅은 사람이 아니라 도구가 강제한다: ktlint(`:app:ktlintCheck` / `:app:ktlintFormat`)를
  병합 전 필수로 통과시킨다. 어떤 모델이 코드를 작성했든 최종 포맷은 ktlint 규칙으로 수렴한다.
- Android Lint(`:app:lint`)를 반드시 통과한다.
- 새 파일에는 주석을 최소화한다. WHY가 비자명한 경우에만 한 줄 주석을 추가한다(기존 CLAUDE.md
  컨벤션과 동일).

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

## 6. 에이전트 역할 분담

| 역할 | 담당 |
| --- | --- |
| 기획/PRD/아키텍처 결정/코드 리뷰/테스트 설계 | Claude |
| 벤치마킹 조사, 정해진 디자인 시스템 내 UI 다듬기, 반복적 보일러플레이트 코드 작성 | `agy` |

호출 방법과 태스크 유형별 모델/플래그는 `agy-playbook.md`를 따른다.
