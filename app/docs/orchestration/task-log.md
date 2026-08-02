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

## Phase 1 — 상품화 준비 (코드 작성 없음, 문서/기획 산출물)

Billing 연동은 Phase 3(가장 마지막)으로 이동. Phase 1은 순수 기획/브랜딩/카피 작업이다.

| ID | 태스크 | 담당 | 상태 | 커밋 | 비고 |
| --- | --- | --- | --- | --- | --- |
| P1-1 | 앱 이름 후보 브레인스토밍 (대량 생성) | agy | done | | conversation `fe5823a5`. Claude 추천: "그날의 팔레트" — branding.md 1절, 사용자 확정 대기 |
| P1-2 | 가치 제안(Value Proposition) 확정 | Claude | done | | branding.md 2절 |
| P1-3 | 브랜딩 방향(톤앤매너, 컬러/키워드) 확정 | Claude | done | | branding.md 3절, 기존 `ui/theme` 팔레트 그대로 채택 |
| P1-4 | Play 스토어 설명(짧은/긴 설명, 키워드) 초안 작성 | agy | done | | conversation `f2370ee9`. Claude 검토 완료, branding.md 4절 |
| P1-5 | Free 기능 최종 스펙 확정 | Claude | done | | branding.md 5절, 기존 MVP 범위 그대로 확정 |
| P1-6 | 프리미엄(Advance) 기능 최종 스펙 확정 | Claude | done | | branding.md 6절, 우선순위: 고급 인사이트 > 내보내기 > 테마/위젯 |
| P1-7 | App Icon 컨셉 방향 2~3안 제시 | agy | done | | conversation `e24a39f0`. Claude 추천: 컨셉 2(Archival Ledger) — branding.md 7절, 사용자 확정 대기 |
| P1-8 | Phase 1 결과 통합 (`orchestration/branding.md` 작성) + 사용자 마일스톤 보고 | Claude | in-progress | | 이름/아이콘 최종 확정만 남음 — branding.md 8절 |

## Phase 2 — MVP 고도화 구현

Phase 1에서 확정된 스펙을 기반으로 착수 시점에 세부 태스크로 분해한다.

## Phase 3 — Billing 연동 (최종 단계)

| ID | 태스크 | 담당 | 상태 | 커밋 | 비고 |
| --- | --- | --- | --- | --- | --- |
| P3-1 | Billing 연동 방식 결정 (Google Play Billing Library, entitlement 로컬 저장 전략) | Claude | todo | | 아키텍처 결정 — agy 위임 안 함 |
| P3-2 | Entitlement 게이팅 인프라 구현 | agy | todo | | P3-1 결정 이후 착수 |

## Phase 4

착수 시점에 세부 태스크로 분해해 이 문서에 추가한다.
