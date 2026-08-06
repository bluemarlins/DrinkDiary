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
  dev/         # 개발
    software-architecture.md
    database-design.md
  research/    # 리서치 (Phase 0 완료, Phase 1에서 추가)
    competitor-analysis.md
    ad-monetization.md
    persona-aso.md
    address-disclosure-policy.md
  legal/       # 법무/정책 (Phase 1 초안 완료)
    privacy-policy.md
    data-safety-mapping.md
    content-rating-draft.md
  marketing/   # 마케팅 (Phase 5에서 채움)
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

### Phase 2. 수익화 개발

담당: Claude
- AdMob SDK 통합 (`gradle/libs.versions.toml`에 의존성 추가)
- UMP(사용자 메시징 플랫폼)로 GDPR/동의 처리
- 광고 배치 UX 설계 (Dashboard/Collection 화면, 정책 위반 안 되는 빈도)

### Phase 3. 디자인 고도화

담당: agy(이미지 초안) + Claude(통합)
- `DDBottomNavigationBar` 등 placeholder 텍스트 아이콘 → 실아이콘 교체
- Feature Graphic(1024x500), 스크린샷 세트 생성
- 기존 `ic_launcher-playstore.png` 브랜드 톤 유지

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
| 2. 수익화 개발 | 대기 |
| 3. 디자인 고도화 | 대기 |
| 4. 릴리즈 준비 | 대기 |
| 5. 스토어 등록/마케팅 | 대기 |
| 6. 출시 후 운영 | 대기 |
