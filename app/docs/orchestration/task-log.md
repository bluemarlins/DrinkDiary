# Task Log

Claude와 `agy`가 수행하는 태스크의 살아있는 백로그/로그. 상태는 `todo` / `in-progress` /
`review` / `done` / `blocked` 중 하나. 완료 시 관련 커밋 해시를 기록한다. Phase 정의는
`app/docs/product-plan.md` 참조.

## 야간 자율 작업 중단 시점 요약 (사용자 요청으로 저장 후 정지)

사용자가 취침 전 6개 항목(MVP 구현·테스트, 디자인 시스템/모션, 한/영 로컬라이제이션, Advance 기능
구상, 더미 데이터 DB 점검, 상품화 완성도)을 자율 진행하도록 요청 → 이후 "저장하고 정지" 요청으로
중단. 아래는 이 시점의 정확한 상태다(과장 없이 있는 그대로).

- **완료 + 검증됨**: 고급 인사이트 UseCase(P2-1/P2-2), CSV 내보내기 UseCase(P2-4/P2-5), Room DAO
  더미데이터 CRUD 테스트(Robolectric), Advance 기능 구상 문서.
- **완료했으나 최종 게이트 미확인**: 고급 인사이트 UI(P2-3, 커밋 `ffce97d`) — `ktlintCheck`와
  컴파일은 통과 확인했지만 `testDebugUnitTest`/`lint` 전체 통과는 빌드가 느려 확인 못 하고 중단.
  **다음 세션에서 가장 먼저 `.\gradlew.bat :app:testDebugUnitTest :app:lint` 재실행 필요.**
- **착수 안 함**: CSV 내보내기 UI/파일 저장 진입점(P2-6), 한/영 로컬라이제이션(약 123개 하드코딩
  한글 문자열, 14개 파일 — 범위만 조사함), 디자인 시스템 모션/인터랙션 고도화, Phase 2 통합
  회귀(P2-8).
- **중요 사고**: 여러 `agy` 호출을 동시(백그라운드 포함)에 띄웠다가 그 중 하나가 워킹 트리를
  마지막 커밋으로 되돌리는 부작용을 일으켜 미커밋 작업이 통째로 사라진 적 있음(대화 컨텍스트에서
  복구 완료). 상세: `orchestration/harness.md` 5절 사고 이력. 이후로는 agy 호출을 한 번에 하나씩만
  실행하고 검증 즉시 커밋하는 방식으로 전환함.
- **push 안 함**: 이 세션에서 만든 모든 커밋은 로컬에만 있다. 사용자 확인 후 push 필요.

## Phase 0 — 하네스/스캐폴딩 구축

| ID | 태스크 | 담당 | 상태 | 커밋 | 비고 |
| --- | --- | --- | --- | --- | --- |
| P0-1 | 문서 구조화 (product-plan/harness/agy-playbook/task-log) | Claude | done | (본 작업) | |
| P0-2 | ktlint + .editorconfig 도입 | Claude | done | (본 작업) | 기존 코드 위반 수정 완료, `ktlintCheck`/`testDebugUnitTest`/`lint` 모두 통과 확인 |
| P0-3 | CLAUDE.md에 Multi-agent workflow 섹션 추가 | Claude | done | (본 작업) | |
| P0-4 | 벤치마킹 앱 조사 (agy 파이프라인 첫 검증 호출) | agy | done | | 파일 수정 없이 완료, 결과를 product-plan.md 5절에 반영. 재현: `agy -p "..." --model gemini-3.5-flash-medium --output-format json`, conversation_id `2440f94f-c74c-4303-99b2-fdfa4d0d8cec` |

## Phase 1 — 상품화 준비 (코드 작성 없음, 문서/기획 산출물)

Billing 연동은 Phase 3(가장 마지막)으로 이동. Phase 1은 순수 기획/브랜딩/카피 작업이다.

| ID | 태스크 | 담당 | 상태 | 커밋 | 비고 |
| --- | --- | --- | --- | --- | --- |
| P1-1 | 앱 이름 후보 브레인스토밍 (대량 생성) | agy | done | | 1차 conversation `fe5823a5`(감성적이라 반려) → 2차 conversation `c6d24b88`(기능 직관성 기준 재생성). **사용자 확정: 테이스트 아카이브 / Taste Archive** — `app_name` 리소스 반영 완료 |
| P1-2 | 가치 제안(Value Proposition) 확정 | Claude | done | | branding.md 2절 |
| P1-3 | 브랜딩 방향(톤앤매너, 컬러/키워드) 확정 | Claude | done | | branding.md 3절, 기존 `ui/theme` 팔레트 그대로 채택 |
| P1-4 | Play 스토어 설명(짧은/긴 설명, 키워드) 초안 작성 | agy | done | | conversation `f2370ee9`. Claude 검토 완료, branding.md 4절 |
| P1-5 | Free 기능 최종 스펙 확정 | Claude | done | | branding.md 5절, 기존 MVP 범위 그대로 확정 |
| P1-6 | 프리미엄(Advance) 기능 최종 스펙 확정 | Claude | done | | branding.md 6절, 우선순위: 고급 인사이트 > 내보내기 > 테마/위젯 |
| P1-7 | App Icon 컨셉 방향 제시 + 실제 이미지 생성 | agy | done | | 컨셉 conversation `e24a39f0` → **사용자 확정: 컨셉 2**. `generate_image`로 실제 PNG 생성(conversation `654b81c6`), Claude가 밀도별 리사이즈 후 앱 리소스 반영 완료 |
| P1-8 | Phase 1 결과 통합 (`orchestration/branding.md` 작성) + 사용자 마일스톤 보고 | Claude | done | | 이름/아이콘 모두 확정 및 반영 완료 — Phase 2 착수 승인 대기 |

## Phase 2 — MVP 고도화 구현

착수 전 도구 상태 확인(harness.md 6-1절 기준, 이 phase 착수 시점 1회 확인):
- **Skill**: `jetpack-compose-m3`는 이름과 달리 Wear OS 전용이라 이 앱(일반 모바일)에는 적용 불가.
  `styles`(Compose Styles API)는 현재 코드베이스가 쓰지 않는 별도 API 도입 결정이라 지금 범위에
  끌어오지 않음. `testing-setup`은 이미 확립된 유닛테스트 패턴(UseCase/ViewModel/Mapper)으로
  충분해 신규 테스트 하네스 구축은 불필요 — 즉, 이번 Phase의 Compose/테스트 작업에 skill이 주는
  이점은 없고 기존 `design-system.md`/`harness.md` 컨벤션을 그대로 따르는 것으로 충분하다.
- **Android CLI/기기**: `adb devices`에 실기기(`R3CY50L9FCT`) 연결 확인됨 → UI 작업은 커밋 전
  루트 `installDebugApk` 태스크로 실기기에 설치해 시각 검증 가능(단순 컴파일/린트 통과에 그치지
  않음).
- **MCP**: 이 세션에 인증·연결된 MCP 중 Android/모바일 관련은 없음(Claude Browser는 웹 전용) →
  배분 판단에서 제외.

담당 배분(Claude: 설계/알고리즘/최종 리뷰+실기기 검증, agy: 명세에 따른 구현+테스트 작성):

| ID | 태스크 | 담당 | 상태 | 커밋 | 비고 |
| --- | --- | --- | --- | --- | --- |
| P2-1 | 고급 인사이트 UseCase 설계 (트렌드/가격대 만족도/재구매율 계산 알고리즘 확정) | Claude | done | c5dee4e | |
| P2-2 | 고급 인사이트 UseCase 구현 + 단위 테스트 | agy | done | c5dee4e | gemini-3.1-pro-high, `ktlintCheck`/`testDebugUnitTest`/`lint` 통과 확인 |
| P2-3 | 고급 인사이트 대시보드 UI (신규 카드/화면, DD* 컴포넌트 재사용) | agy+Claude | review | ffce97d | 데이터 유실 사고로 Claude가 대화 컨텍스트에서 재작성. `ktlintCheck`+컴파일 통과 확인, `testDebugUnitTest`/`lint` 최종 확인 필요. 기기 미연결로 실기기 시각 검증 못함 |
| P2-4 | 데이터 내보내기(CSV) UseCase + 파일 쓰기 로직 설계 | Claude | done | b6625f3 | |
| P2-5 | 데이터 내보내기 UseCase 구현 + 단위 테스트 | agy | done | b6625f3 | gemini-3.5-flash-medium, `ktlintCheck`/`testDebugUnitTest` 통과 확인 |
| P2-6 | 데이터 내보내기 UI (설정/내보내기 진입점, SAF 파일 저장) | agy | todo | | 착수 안 함 |
| P2-7 | 커스텀 테마/위젯 | — | todo | | 착수 안 함, 우선순위 최하 |
| P2-8 | Phase 2 통합 리뷰 + 전체 회귀 테스트 + 실기기 설치 검증 | Claude | todo | | 착수 안 함. P2-3 최종 게이트 확인부터 시작 |
| P2-9 | (계획 외 추가) Room DAO 더미데이터 CRUD 테스트 | Claude | done | 79aa4f2 | Robolectric 도입, `@Config(sdk=[35])`로 JDK17 환경에서 SDK36 미지원 이슈 우회 |
| P2-10 | (계획 외 추가) 한/영 로컬라이제이션 | Claude | todo | | 범위 조사만 완료(14개 파일, 약 123개 하드코딩 한글 문자열). strings.xml 추출 착수 안 함 |
| P2-11 | (계획 외 추가) 디자인 시스템 모션/인터랙션 고도화 | — | todo | | 착수 안 함 |

## Phase 3 — Billing 연동 (최종 단계)

| ID | 태스크 | 담당 | 상태 | 커밋 | 비고 |
| --- | --- | --- | --- | --- | --- |
| P3-1 | Billing 연동 방식 결정 (Google Play Billing Library, entitlement 로컬 저장 전략) | Claude | todo | | 아키텍처 결정 — agy 위임 안 함 |
| P3-2 | Entitlement 게이팅 인프라 구현 | agy | todo | | P3-1 결정 이후 착수 |

## Phase 4

착수 시점에 세부 태스크로 분해해 이 문서에 추가한다.
