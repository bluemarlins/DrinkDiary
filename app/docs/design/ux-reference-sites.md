# 모바일 UX 레퍼런스 사이트 (Astryx 외)

## 문서 정보

- 리서치 도구: `agy` CLI (`gemini-3.1-pro-high`)
- 작성일: 2026-08-07
- **주의**: 사이트 URL/유료 여부/최신성은 AI 리서치 산출물이므로 실제 방문해서 최종 확인 필요. 특히 "AI 에이전트 맥락 인지형 디자인 툴" 섹션의 구체적 제품명은 검증 우선순위가 낮음(변화가 빠른 영역).

## 결론 (우리 프로젝트에 바로 쓸 수 있는 것 우선순위)

1. **Now in Android** (`github.com/android/nowinandroid`) — Google이 직접 유지보수하는 Compose+M3 프로덕션 레퍼런스. 우리 아키텍처(MVVM+Repository+UseCase)와 가장 비교하기 쉬운 대상.
2. **Material Design 3** (`m3.material.io`) — 우리가 이미 쓰는 라이브러리의 근간. 컴포넌트별 "올바른 상태(state)" 확인에 직접 활용.
3. **Mobbin** (`mobbin.com`) — 실제 상용 앱 화면(대시보드, 빈 상태, 온보딩 등) 카테고리별 열람 가능한 최고 수준 레퍼런스지만 **유료 구독 필요**. Dashboard/Collection 화면 리디자인 시 카테고리 검색으로 참고할 가치 있음.
4. **"AGENTS.md 스타일 머신 리더블 가이드라인"** (제품이 아니라 관행) — 지난 턴 Astryx 논의와 동일한 결론: `design-system.md`에 "하드코딩 색상 금지, `MaterialTheme.colorScheme` 필수 사용" 같은 명시적 규칙을 추가하는 것이 우리 규모에 가장 실용적.

## 1. 공식/표준 디자인 시스템

| 사이트 | URL | 유용성 | 유의점 |
|---|---|---|---|
| Material Design 3 | m3.material.io | Compose Material3의 근간, 컴포넌트 상태(States)/동적 색상/타이포 스케일 기준 | 웹 가이드도 섞여 있어 모바일 사양만 필터링 필요 (무료) |
| Apple HIG | developer.apple.com/design/human-interface-guidelines | 제스처/트랜지션/여백 등 "프리미엄한 느낌"의 UX 디테일 벤치마킹 | 코드 아님, 원칙/시각적 피드백 참고용 (무료) |

## 2. 실제 앱 UI 패턴 갤러리

| 사이트 | URL | 유용성 | 유의점 |
|---|---|---|---|
| Mobbin | mobbin.com | iOS/Android 실제 앱 화면을 카테고리·플로우별 검색, 현존 최고 수준 | 무료 티어 매우 제한적, Android 필터는 **유료 구독 필요** |
| Page Flows | pageflows.com | 정적 스크린샷이 아닌 사용자 플로우 비디오(결제, 비밀번호 찾기 등) | 모바일/웹 혼재, 전체 플로우는 **유료** |
| Screenlane | screenlane.com | 마이크로 인터랙션/화면별(로그인, 설정 등) 분류 | 일부 무료, 업데이트 주기는 Mobbin보다 느림 |

## 3. 주요 IT 기업 공개 디자인 시스템 (코드 재사용 아닌 원칙 참고용)

| 사이트 | URL | 유용성 | 유의점 |
|---|---|---|---|
| Shopify Polaris | polaris.shopify.com | 접근성/콘텐츠 가이드/상태 설계의 원칙과 이유를 가장 깊이 설명 | 웹/React 중심, 시각 요소보다 아키텍처·원칙 학습용 (무료) |
| Uber Base | base.uber.com | 굵직한 타이포그래피, 모션, 그리드 시스템 | 웹(React) 중심, 네이티브 코드 없음 (무료) |
| Atlassian Design System | atlassian.design | 복잡한 데이터(폼/테이블/네비게이션) 배치 로직 | B2C 감성보다 엔터프라이즈 톤 (무료) |

## 4. Android/Compose 전용 공식 리소스

| 리소스 | URL | 유용성 |
|---|---|---|
| Now in Android | github.com/android/nowinandroid | Google 공식 Compose+M3 모범 사례 앱. 테마 적용/반응형 레이아웃/모듈화 아키텍처를 실제 코드로 확인 (엔지니어링 중심) |
| Compose Material 3 Catalog | github.com/android/compose-samples | 바텀시트/스낵바/카드 등 M3 컴포넌트 렌더링·애니메이션을 기기에서 직접 확인, 소스 복사 가능 |
| Android Design Hub | developer.android.com/design/ui | 폴더블/태블릿 등 폼팩터별 반응형 UI 공식 가이드 |

## 5. AI 에이전트 맥락 인지형 디자인 툴 (참고용, 검증 우선순위 낮음)

- **MCP for Figma/Storybook** (modelcontextprotocol.io) — AI가 Figma 컴포넌트 속성/디자인 토큰을 직접 읽어 환각 없이 코드 생성. 우리처럼 Figma 원본이 없는 경우 적용 어려움.
- **Relay for Compose** (developer.android.com/develop/ui/compose/tooling/relay) — Google 공식, Figma 컴포넌트를 Compose 코드로 변환. 정적 뷰 중심, 복잡한 상태/애니메이션엔 한계.
- **AGENTS.md 관행** — 특정 툴이 아니라 "머신 리더블 가이드라인을 프로젝트에 문서화"하는 관행. 우리는 이미 `design-system.md` + `CLAUDE.md` 조합으로 비슷한 방향이며, 지난 Astryx 논의에서 나온 "명시적 금지 규칙 추가" 제안과 같은 결론.
