# Task List - 테이스트 아카이브 Phase 2 고도화 (No-Ad Freemium)

## 1. 전략 및 문서 업데이트 (Documentation)
- [x] `product-strategy.md` 업데이트 (완료됨)
- [ ] `product-plan.md` 업데이트 (신규 수익화 모델 반영)
- [ ] `branding.md` 업데이트 (가치 제안 및 프리미엄 스펙 동기화)

## 2. 도메인 및 데이터 계층 (Domain & Data)
- [ ] `ObserveDrinkRecordsUseCase` 수정: 현재 기록 개수를 가져오는 기능 추가 (기록 제한 체크용)
- [ ] 프리미엄 상태(Archive Pro) 관리용 `PreferenceRepository` 또는 `DataStore` 설정

## 3. UI/UX 구현 (UI & Screens)
- [ ] **기록 제한 게이팅 (Gating)**
    - [ ] `RecordEditorViewModel`: 기록 전 30개 제한 확인 로직 추가
    - [ ] `ProUpgradeDialog`: 30개 도달 시 노출할 안내 다이얼로그 구현
- [ ] **설정 화면 (Settings)**
    - [ ] `SettingsScreen` 신규 구현
    - [ ] CSV 내보내기 버튼 및 SAF 연동
    - [ ] Pro 구매 안내 섹션 추가
- [ ] **인사이트 화면 (Insights) 고도화**
    - [ ] 기존 인사이트 UI 검증 및 수정
    - [ ] 유료 기능(Advanced Insights) 잠금 UI 적용

## 4. 로컬라이제이션 (Localization)
- [ ] `res/values/strings.xml` 하드코딩 문자열 추출 (한국어)
- [ ] `res/values-en/strings.xml` 생성 및 번역 (영어)

## 5. 검증 및 마감 (Verification)
- [ ] `ktlint` 및 `lint` 전수 검사
- [ ] 유닛 테스트 (기록 제한 로직)
- [ ] 실기기 설치 및 시각 검증
