# Task Log

Claude와 `agy`가 수행하는 태스크의 살아있는 백로그/로그. 상태는 `todo` / `in-progress` /
`review` / `done` / `blocked` 중 하나. 완료 시 관련 커밋 해시를 기록한다. Phase 정의는
`app/docs/product-plan.md` 참조.

## Phase 0 — 하네스/스캐폴딩 구축

| ID | 태스크 | 담당 | 상태 | 커밋 | 비고 |
| --- | --- | --- | --- | --- | --- |
| P0-1 | 문서 구조화 (product-plan/harness/agy-playbook/task-log) | Claude | done | (본 작업) | |
| P0-2 | ktlint + .editorconfig 도입 | Claude | done | (본 작업) | 기존 코드 위반 수정 완료, `ktlintCheck`/`testDebugUnitTest`/`lint` 모두 통과 확인 |
| P0-3 | CLAUDE.md에 Multi-agent workflow 섹션 추가 | Claude | done | (본 작업) | |
| P0-4 | 벤치마킹 앱 조사 (agy 파이프라인 첫 검증 호출) | agy | done | | 파일 수정 없이 완료, 결과를 product-plan.md 5절에 반영. 재현: `agy -p "..." --model gemini-3.5-flash-medium --output-format json`, conversation_id `2440f94f-c74c-4303-99b2-fdfa4d0d8cec` |

## Phase 1 — 프리미엄 인프라 + 프리미엄 기능 MVP (seed, 착수 시 세부화)

| ID | 태스크 | 담당 | 상태 | 커밋 | 비고 |
| --- | --- | --- | --- | --- | --- |
| P1-1 | Billing 연동 방식 결정 (Google Play Billing Library, entitlement 로컬 저장 전략) | Claude | todo | | 아키텍처 결정 — agy 위임 안 함 |
| P1-2 | Entitlement 게이팅 인프라 구현 (Premium 여부에 따른 기능 잠금 공통 컴포넌트) | agy | todo | | P1-1 결정 이후 착수 |
| P1-3 | 프리미엄 기능: 고급 인사이트(기간별 취향 트렌드, 가격대 분석) UseCase + UI | agy | todo | | domain/data는 gemini-3.1-pro-high, UI는 gemini-3.6-flash-high |
| P1-4 | 프리미엄 기능: 데이터 내보내기 (CSV) | agy | todo | | |
| P1-5 | 프리미엄 업셀/페이월 화면 (디자인 시스템 범위 내) | agy | todo | | |
| P1-6 | Phase 1 통합 코드 리뷰 + 테스트 커버리지 점검 | Claude | todo | | Phase 1 마일스톤 보고 전 필수 |

## Phase 2 / Phase 3

착수 시점에 세부 태스크로 분해해 이 문서에 추가한다.
