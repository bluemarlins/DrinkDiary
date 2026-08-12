# S/W 아키텍처 설계 — 재정의 MVP

> [!NOTE]
> **상태: 초안 — 사용자 확정 전.** 근거: `../planner/prd.md`(요구사항),
> `../planner/mvp-scope.md`(F1~F6). 확정 시 `specs/developer/software-architecture.md`를 대체한다.
> 스키마 재정의는 `../planner/problem-definition.md` 7-1절에서 승인됨(출시 전이므로 마이그레이션 불필요).

## 1. 설계 원칙

기존 계층 구조(`UI → ViewModel → UseCase → Repository → DAO → Room`)는 **유지한다.** 이 구조가
문제였던 적이 없다. 바뀌어야 하는 것은 **도메인 모델**이다.

재설계의 핵심 질문은 하나다:

> **와인과 위스키는 감각 축이 다른데, 어떻게 하나의 취향 언어로 묶어 가로지르는 요약을 만들 것인가?**

이 질문에 대한 답이 아키텍처 전체를 결정한다.

## 2. 핵심 설계 — 2층 취향 모델

### 2-1. 문제

- 와인의 축: 당도, 산미, 탄닌, 바디, 향
- 위스키의 축: 향, 피트, 바디, 피니시, 도수감

축을 그대로 두면 주종을 가로지르는 요약(F3)이 불가능하다. 반대로 억지로 하나로 합치면 각 주종의
고유한 감각이 사라진다.

### 2-2. 해법 — 공통 축(Trait) + 주종별 질문(Probe) 분리

```text
[입력 계층]  주종별 Probe (사용자가 보는 질문·선택지)
                    │  각 Probe는 하나의 공통 Trait에 매핑된다
                    ▼
[취향 계층]  공통 Trait (내부 취향 언어, 주종 무관)
                    │
                    ▼
[요약 계층]  TasteProfile (Trait별 선호 방향과 확신도)
```

**공통 Trait 후보** (주종 무관, 입문자가 감각 가능한 것):

| Trait | 와인 Probe 예 | 위스키 Probe 예 |
| --- | --- | --- |
| `Sweetness` | "달았나요?" | "달큰했나요?" |
| `Intensity` | "향이 진했나요?" | "향이 강했나요?" |
| `Astringency` | "떫었나요?" | "혀가 조이는 느낌이 있었나요?" |
| `Body` | "묵직했나요?" | "묵직했나요?" |
| `Aftertaste` | "여운이 길었나요?" | "여운이 길었나요?" |

**이 분리가 핵심인 이유**: 사용자는 주종에 맞는 자연스러운 말로 답하는데(F2의 "일상어" 요구),
내부에는 주종 무관한 공통 값이 쌓인다. 그래서 **"당신은 주종을 가리지 않고 여운이 긴 술을
좋아한다"** 같은 요약(F3)이 가능해진다. 경쟁 앱이 못 하는 지점이 정확히 여기다.

Probe 문구는 데이터이지 코드가 아니다 — **Trait에 매핑된 Probe 목록을 교체 가능하게** 둔다.
F2의 축 개수·단계가 아직 가설이므로(PRD 7절 열린 질문 2), 문구와 개수를 바꿔도 저장 구조가
흔들리지 않아야 한다.

### 2-3. 3단계 척도

PRD F2는 "탭 5회 이내, 슬라이더 없음"을 요구한다. 따라서 Trait 값은 연속값이 아니라 **3단계**로
받는다.

```kotlin
enum class TraitLevel { Low, Mid, High }   // 예: 안 떫음 / 보통 / 떫음
```

기존 `Double` 기반 5축 슬라이더(`DrinkRatingBreakdown`)는 폐기한다. 이것이 PRD S5 실패
시나리오의 원인이다.

## 3. 도메인 모델

```text
domain/model
  DrinkType            Wine, Whiskey                  (Beer 삭제)
  DrinkRecord          기록 1건
  ServingStyle         Neat, OnTheRocks, Highball, ... (위스키 P4)
  Vintage              연도 (와인 P4, nullable)
  Trait                Sweetness, Intensity, Astringency, Body, Aftertaste
  TraitLevel           Low, Mid, High
  TasteInput           Map<Trait, TraitLevel>          사용자 입력 결과
  Probe                Trait에 매핑된 질문·선택지 (주종별)
  TasteProfile         Trait별 선호 방향 + 확신도 + 요약 문장
  ProfileReadiness     NotReady(남은 개수) | Partial | Ready
  CollectionStatus     Repurchase, NotForMe, Normal    (유지)
  ShareCard            공유 카드 생성 입력값
```

**`domain/model`은 Android/Room에 의존하지 않는다**(`harness.md` §1). 현재 코드가 이 규칙을 어기고
`R`을 import하는 문제는 이번 재설계에서 함께 해소한다 — **Probe 문구는 도메인이 아니라 UI 계층에서
문자열 리소스로 해석**한다. 도메인은 `Trait`와 `TraitLevel`만 안다.

## 4. UseCase

| UseCase | 대응 기능 | 책임 |
| --- | --- | --- |
| `SaveDrinkRecordUseCase` | F1 | 검증 + 저장. 빈티지·음용방법 포함 |
| `ObserveProbesUseCase` | F2 | 주종에 맞는 Probe 목록 제공 |
| `ObserveTasteProfileUseCase` | F3 | 기록 → `TasteProfile` 계산. **핵심 알고리즘** |
| `CheckProfileReadinessUseCase` | F3 | 임계치 도달 여부 + 남은 개수 |
| `SearchRecordsUseCase` | F5 | 이름 부분 일치 + 재구매 후보 우선 정렬 |
| `GenerateShareCardUseCase` | F4 | 카드에 들어갈 데이터 조립(렌더링은 UI) |
| `CheckRecordLimitUseCase` | F6 | **주종별** 카운트 대조 |

### 4-1. `ObserveTasteProfileUseCase` — 가장 중요한 알고리즘

이 UseCase가 F3의 전부이며, 제품 가치의 대부분이 여기 있다.

**입력**: 사용자의 모든 기록(각 기록은 `TasteInput` + 전체 만족도 + `CollectionStatus`)
**출력**: Trait별 선호 방향 + 확신도 + 자연어 요약 문장

핵심은 **"많이 마신 것"이 아니라 "높게 평가한 것"의 경향을 찾는 것**이다. 떫은 와인을 20번 마셨어도
전부 낮게 평가했다면 그건 선호가 아니다.

```text
각 Trait에 대해:
  High로 답한 기록들의 평균 만족도  vs  Low로 답한 기록들의 평균 만족도
  차이가 유의미하면 → 그 방향을 선호로 판정
  양쪽 표본이 모두 최소 개수 이상일 때만 판정 (아니면 "판단 유보")
```

**확신도는 표본 수와 차이 크기로 정한다.** 이것이 `ProfileReadiness`의 근거가 되고, "3개만 더
기록하면 위스키 취향도 보인다"(PRD S2)를 계산 가능하게 만든다.

> [!IMPORTANT]
> **다양성 문제**(`problem-definition.md` 8-1절): 30개를 마셔도 전부 같은 스타일이면 패턴이 나오지
> 않는다. 위 설계는 이를 자연스럽게 처리한다 — 한쪽 방향 표본만 있으면 비교가 불가능해 "판단 유보"가
> 되고, 사용자에게 **"다른 스타일도 마셔보라"**고 안내할 근거가 된다. 개수가 아니라 대비가 임계치를
> 정한다는 분석이 여기서 구현으로 이어진다.

## 5. 데이터 계층

```text
data/local
  DrinkDiaryDatabase   version = 1  (재정의, 기존 마이그레이션 폐기)
  DrinkRecordEntity    주종/이름/빈티지/음용방법/사진/가격/장소/메모/만족도/컬렉션상태/기록일시
  TraitAnswerEntity    기록 1건에 대한 Trait별 답 (기록 : 답 = 1 : N)
  DrinkRecordDao
```

**`TraitAnswerEntity`를 별도 테이블로 두는 이유**: Trait 개수가 아직 가설이므로(PRD 7절), 컬럼으로
고정하면 축을 바꿀 때마다 스키마가 흔들린다. 행으로 두면 Trait 추가·삭제가 스키마 변경 없이 가능하다.
enum은 문자열로 저장한다(`harness.md` §1).

`UserPreferencesRepository`(DataStore, `isProUser`)는 현행 유지.

## 6. UI 구조

```text
ui/
  record/       F1·F2  기록 작성 — Probe 기반 3탭 입력
  profile/      F3     취향 요약 (문장 우선, 차트 보조)
  collection/   F1     기록 목록
  lookup/       F5     매장용 빠른 조회 — 진입 즉시 검색 포커스
  share/        F4     공유 카드 생성·미리보기
  settings/     F6     Pro 안내, 내보내기
  component/           DD* 공용 컴포넌트
```

**F5는 별도 화면으로 둔다.** 기존 `search`를 재사용하지 않는 이유는 목적이 다르기 때문이다 —
검색은 "찾기", 조회는 **"살지 말지 판단"**이다. 결과에 만족도와 재구매 여부가 즉시 보여야 하고,
진입에서 판단까지 15초(PRD S3) 안에 끝나야 한다.

**공유 카드 렌더링**: Compose 화면을 비트맵으로 캡처해 9:16으로 생성한다. 별도 이미지 라이브러리를
추가하지 않는다(`harness.md` §10 — 공식 대안으로 충분하면 서드파티를 넣지 않는다).

## 7. 기존 코드 처리

| 대상 | 처리 |
| --- | --- |
| `DrinkRatingBreakdown` (5축 Double) | **폐기** — PRD S5 실패 원인 |
| `DrinkType.Beer` | **삭제** |
| Room 마이그레이션 v1→v2→v3 | **폐기**, version 1로 재시작 |
| `domain/model`의 `R` import | **제거** — Probe 문구를 UI로 이동해 자연 해소 |
| `InsightsSummary`(월별 추이·가격대 만족도) | **보류** — F3와 목적이 다르다. 재도입은 MVP 이후 |
| 로컬라이제이션(ko/en strings) | **재사용** — Probe 문구는 새로 필요 |
| `Components.kt`의 DD* | **선별 재사용** |
| Navigation 3 구조 | **유지** — 화면 목록만 교체 |

> [!WARNING]
> 미검증 코드(커밋 `6086080`)는 이 재설계로 상당 부분 대체된다. 다만 **재설계가 중단되면 컴파일
> 여부가 확인되지 않은 코드가 원격에 남는다**(`problem-definition.md` 11절). 재설계 착수 시점에
> 기존 코드를 어디까지 걷어낼지 먼저 정한다.

## 8. 구현 순서

`mvp-scope.md` 5절 우선순위(리스크 큰 것부터)를 따른다.

1. **도메인 모델 + `ObserveTasteProfileUseCase`** — 알고리즘을 UI 없이 단위 테스트로 먼저 검증.
   여기가 무너지면 나머지는 의미가 없다.
2. **Room 스키마 재정의 + Repository/DAO**
3. **F2 기록 입력 UI** — Probe 문구를 바꿔가며 실기기에서 검증
4. **F3 취향 요약 화면**
5. **F1 컬렉션 / F5 조회**
6. **F4 공유 카드**
7. **F6 한도·업그레이드**

1번은 순수 Kotlin이라 Android 없이 테스트 가능하다. **`agy` 위임 가능**(명세 확정 + 기계 검증
가능 + 서브트리 격리 가능): `gemini-3.1-pro-high`(다단계 계산 로직). 3번은 시각 판단이 필요하므로
`gemini-3.6-flash-high` 또는 Claude 직접.

## 9. 열린 질문

1. **Trait 목록 확정** — 5개 안은 가설. F2 프로토타입으로 검증 후 확정.
2. **선호 판정 임계치** — 표본 최소 개수와 유의미한 차이의 기준. 더미 데이터로 시뮬레이션 필요.
3. **주종 가로지르기의 타당성** — 와인의 "떫음"과 위스키의 "조임"을 같은 Trait으로 묶는 것이
   사용자에게 자연스러운지. **이 설계의 최대 가정이며, 틀리면 2절 전체를 다시 짜야 한다.**
