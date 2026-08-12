# [Taste Archive] 수익화 모델 개편 및 Phase 2 고도화 플랜

## 개요
사용자님의 광고(AdMob) 도입 제안을 바탕으로 **리서치 부서**와 **기획 부서**가 협업하여 분석한 결과, '테이스트 아카이브'의 핵심 가치인 **'프라이버시(Privacy)'**를 지키면서도 수익화 허들을 낮출 수 있는 **'No-Ad Freemium'** 모델로 전략을 고도화했습니다.

## User Review Required

> [!IMPORTANT]
> **광고(AdMob) 도입 보류**: 리서치 결과, AdMob SDK의 트래킹 정책이 우리 앱의 가장 큰 차별점인 "서버 없는 프라이버시" 약속과 충돌하여 브랜드 신뢰도를 떨어뜨릴 위험이 큽니다.
> 
> **대안 - 기록 제한 모델**: 광고를 넣는 대신, 무료 버전에서 **기록 개수를 30개로 제한**하고, 그 이상 기록하거나 백업/내보내기 기능을 쓰려면 **'평생 소장권'**을 구매하게 유도하는 방식을 제안합니다.

> [!TIP]
> **허들 낮추기 (Lifetime Purchase)**: 월 구독($1.49)의 부담을 느끼는 유저를 위해, 한 번만 결제하면 평생 광고 없이 모든 기능을 소유하는 **평생 소장권($19.99)**을 주력 상품으로 밀고 나갑니다. 이는 '내 기기에만 저장한다'는 로컬 퍼스트 철학과도 잘 맞습니다.

## Proposed Changes

### 1. 상품 전략 수정 (Planner/Researcher)
- [MODIFY] [product-strategy.md](file:///E:/Workspace/Source/DrinkDiary/app/docs/departments/planner/product-strategy.md): AdMob 대신 No-Ad Freemium 모델 반영.
- [MODIFY] [product-plan.md](file:///E:/Workspace/Source/DrinkDiary/app/docs/product-plan.md): 프리미엄 기능 및 수익화 로드맵 업데이트.

### 2. Phase 2: 기능 고도화 구현 (Developer)
- [NEW] **무료 티어 기록 제한 로직**: 30개 초과 기록 시 'Pro 전환' 팝업/화면 노출.
- [NEW] **설정(Settings) 화면**: CSV 내보내기, 백업 관리, Pro 구매 진입점 구현.
- [MODIFY] **고급 인사이트(Insights)**: Pro 유저만 접근 가능한 분석 차트 게이팅(Gating) 인프라 구축.

### 3. 다국어 지원 (Localization)
- [NEW] `strings.xml` (en/ko): "프라이버시를 팔지 않는 앱" 메시지를 포함한 다국어 리소스 추출.

## Verification Plan

### Automated Tests
- `.\gradlew :app:testDebugUnitTest`: 30개 기록 제한 로직 유닛 테스트.
- `.\gradlew :app:lint`: 신규 화면 및 리소스 린트 검사.

### Manual Verification
- 무료 티어에서 31번째 기록 시도 시 정상적으로 안내 문구가 뜨는지 확인.
- CSV 내보내기 파일이 정상적으로 생성되고 공유되는지 확인.
- 다국어(영어/한국어) 전환 시 문구가 어색하지 않은지 확인.
