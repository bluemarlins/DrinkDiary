# 명세 ↔ 코드 충돌 점검 (2026-08-17)

> [!NOTE]
> **상태: 초안 — 사용자 확정 전.** 점검: Claude(전 명세를 코드와 대조). agy 위임 없음 —
> 우리 명세와 우리 코드를 맞대는 일이라 검증이 곧 작업 자체다(`AGENTS.md`, QA는 Claude 전담).
>
> 대상: `app/docs/specs/` 전 문서 + `AGENTS.md` + `orchestration/harness.md`,
> `build-and-test.md`. 기준 커밋 `20f916c`.

## 0. 요약

**14건을 찾았다. 코드 결함 3건, 문서 낡음 11건이다.**

디자인 시스템 쪽(색·타이포·브레이크포인트·터치 타깃)은 **테스트가 지키고 있어 이번 점검에서
새 충돌이 나오지 않았다**(`DesignTokenTest`). 반면 **테스트가 닿지 않는 계층 — 기획 문서의
"현재 구현" 서술과 아키텍처 계층 규칙 — 에 충돌이 몰려 있다.**

| # | 심각도 | 어디가 틀렸나 | 항목 |
| --- | --- | --- | --- |
| A-1 | **심각** | 코드 | UseCase 계층이 비어 있다 (`harness.md` §1 위반) |
| A-2 | 경미 | 코드 | `DDSecondaryButton` 사용처 0 |
| A-3 | 경미 | 코드 | 검색 탭 에셋 3벌이 런타임에 도달 불가 |
| B-1 | **심각** | 문서 | PRD S5 "현재 구현이 정확히 이 상태다" |
| B-2 | **심각** | 문서 | `mvp-scope.md` §6 전체가 재정의 이전 |
| B-3 | 중간 | 문서 | `problem-definition.md` §10·§11 낡음 |
| B-4 | 중간 | 문서 | PRD 7절이 낡은 임계치를 "현재"라 부름 |
| B-5 | 중간 | 문서 | `branding.md` "제품에 유형 개념이 아직 없다" |
| B-6 | 중간 | 문서 | `software-architecture.md` §7 ↔ §5-1 내부 모순 |
| B-7 | 경미 | 문서 | `build-and-test.md` 예시가 없는 테스트 클래스를 가리킴 |
| B-8 | 경미 | 문서 | `harness.md` §2가 없는 파일(`Components.kt`)을 가리킴 |
| B-9 | 중간 | 문서 | `design-system.md` §6-3의 "검색창"이 화면에 없음 |
| B-10 | 중간 | 문서 | §5 카탈로그와 코드의 양방향 차이 |
| B-11 | 경미 | 문서 | PRD가 디자인 시스템에 없는 `DDProLockOverlay`를 지정 |
| B-12 | 사소 | 문서 | `AGENTS.md` 문서 목록이 오늘 승격한 파일을 모름 |

**미구현(F4 공유 카드 · F5 매장 조회 · F6 과금)은 충돌로 세지 않았다.** 명세가 앞서고 코드가
따라오지 않은 것은 정상이며, 로드맵이 그렇게 적혀 있다.

---

> [!NOTE]
> **후속 (2026-08-17 같은 날)**: B-2·B-3은 **전면 갱신 전에 경고 배너만 먼저 달았다.**
> 대시보드 재구성(`prd.md` F3-2)이 두 절의 "통계에 머문다"는 지적을 부분적으로 되돌리면서,
> 그 절들을 손대지 않은 채 두면 명세가 서로 다른 주장을 하게 됐기 때문이다.
> **본문 자체는 여전히 낡았고 C-1이 남아 있다.**
>
> B-10(카탈로그 차이)도 일부 좁혀졌다 — `DDMonthlySummaryCard`가 카탈로그에 등재됐고,
> `TraitStatusRow`는 코드와 §6 매핑에서 함께 사라졌다.

## A. 코드가 문서를 어기는 것

### A-1. UseCase 계층이 비어 있다 — `harness.md` §1 위반 **[심각]**

`harness.md` §1이 두 문장으로 못박는다.

> 의존 방향은 항상 `UI -> ViewModel -> **UseCase** -> Repository -> DAO -> Database` 한 방향이다.
> 새 비즈니스 로직/검증 규칙은 `domain/usecase`에 둔다. **ViewModel이나 Composable에 검증 로직을 넣지 않는다.**

**실제로는 ViewModel이 Repository를 직접 호출한다.**

| 위치 | 호출 |
| --- | --- |
| `ui/record/RecordViewModel.kt:141` | `repository.save(record)` |
| `ui/record/EditRecordViewModel.kt:115` | `repository.save(source.applying(...))` |
| `ui/collection/CollectionViewModel.kt:44` | `repository.observeRecords(it)` |
| `ui/collection/CollectionViewModel.kt:78` | `repository.deleteByIds(targets)` |
| `ui/collection/CollectionViewModel.kt:91` | `repository.deleteById(id)` |

`domain/usecase`에 실재하는 것은 셋뿐이다 — `ObserveTasteProfileUseCase`,
`ObserveTagPreferenceUseCase`, `ResolveProfileReadinessUseCase`. **전부 F3(취향 요약) 것이다.**
F1·F2의 UseCase는 하나도 없다.

`software-architecture.md` §4가 이름을 찍어 요구한 것 중 **구현된 기능에 해당하는 두 개**가 없다.

- `SaveDrinkRecordUseCase` (F1) — "검증 + 저장"
- `ObserveProbesUseCase` (F2) — "기본 경로는 `Trait.shared`만"

> 나머지 미구현 UseCase(`SearchRecordsUseCase`·`GenerateShareCardUseCase`·
> `ObserveProEntitlementUseCase`)는 F4·F5·F6 미착수라 정상이다. `CheckProfileReadinessUseCase`는
> 코드에서 `ResolveProfileReadinessUseCase`로 **이름만 다르다** — 문서 쪽을 코드에 맞추면 된다.

**실질적 피해가 이미 한 곳에 나타나 있다.** PRD F1이 "필수는 주종·이름·**만족도**"라고 정한
규칙이 지금 이렇게 산다.

```kotlin
// ui/record/RecordViewModel.kt:38  ← UI 계층의 data class 프로퍼티
val isSavable: Boolean get() = name.isNotBlank() && rating > 0.0
```

제품 규칙이 UI 계층에 있다. **그리고 단위 테스트가 없다** — `isSavable`은 테스트 소스 어디에도
나오지 않는다. `harness.md` §4-6("새 ViewModel 로직에는 대응하는 단위 테스트가 존재")도 함께 어긴다.

**다만 중복은 아니다.** 작성·편집 두 ViewModel이 같은 `RecordForm.isSavable`을 본다. 계층이
틀렸을 뿐 규칙이 갈라지지는 않았으므로, 지금 옮기면 비용이 작다.

### A-2. `DDSecondaryButton` — 정의만 있고 사용처가 0 **[경미]**

```
app/src/main/java/.../ui/component/DDButtons.kt:55  fun DDSecondaryButton(
```

이 파일 밖 참조가 **0건**이다. `design-system.md` 5-1은 이 컴포넌트를 "이전 단계, 취소 등 보조
액션"으로 정의하는데, 기록 마법사의 '이전'도 이것을 쓰지 않는다.

카탈로그에 있으나 아무도 안 쓰는 컴포넌트는 **"이 자리에는 이걸 쓴다"는 규칙이 실제로는 강제되지
않고 있다**는 신호다 — 3판 감사에서 칩 5곳이 32dp로 샜던 것과 같은 구조다.

### A-3. 검색 탭 에셋 3벌이 런타임에 도달할 수 없다 **[경미]**

`DDScreenScaffold`는 세 내비게이션(Bottom / Rail / Drawer) 전부에서 검색 항목을 그린다.
그런데 `onSearchClick`의 기본값이 `null`이고(`DDScreenScaffold.kt:97`),
**`DrinkDiaryApp`은 이 인자를 한 번도 넘기지 않는다.**

```kotlin
onSearchClick?.let { onClick -> NavigationRailItem(...) }   // 항상 건너뛴다
```

**탭이 안 보이는 것 자체는 옳다** — F5가 미착수이고, 동작하지 않는 탭을 노출해 사고가 났던
전례가 있다(2026-08-14, `task-log.md`). 문제는 남은 잔해다.

- `res/drawable/ic_nav_search.xml`
- `res/values/strings.xml`의 `nav_search`
- 세 내비게이션 컴포저블의 분기 3벌

`strings.xml` 주석은 "`DDScreenScaffold`가 참조하는 라벨"만 남겼다고 적고 있어 **틀리지는 않았지만**,
"참조된다"와 "그려진다"가 다르다는 것을 감춘다. F5를 만들 때 살아나므로 **지울 필요는 없다.**
다만 그 사실을 코드나 문서 어디에도 적어두지 않아, 다음 사람이 "검색 탭이 왜 안 뜨지"를
디버깅하게 된다.

---

## B. 문서가 코드를 잘못 기술하는 것

### B-1. PRD S5 — "현재 구현이 정확히 이 상태다" **[심각]**

`prd.md:255-261`, 실패 시나리오 S5.

> 취향 입력 화면에 **슬라이더 5개**와 "탄닌", "바디"라는 단어가 보인다. (…)
> 이것이 경쟁 앱들이 실패하는 경로이며, **현재 구현이 정확히 이 상태다.**

**전부 사실이 아니다.** 슬라이더는 없고(`DrinkRatingBreakdown`은 삭제됨), 입력은 3선택 Probe이며,
문구는 `ProbeCopy.kt`의 일상어다. 탭 5회는 에뮬레이터에서 측정 확인됐다.

**PRD는 확정 명세다.** 이 문장을 읽은 사람이나 에이전트는 F2가 미구현이라고 판단하고, 이미 있는
것을 다시 만들거나 없는 결함을 고치려 든다.

### B-2. `mvp-scope.md` §6 "현재 구현과의 관계" 전체가 재정의 이전 **[심각]**

| 문서가 말하는 것 (§6) | 실제 |
| --- | --- |
| "**F2가 없다.** 현재 입력은 5축 슬라이더(`DrinkRatingBreakdown`)" | F2 구현 완료. 해당 클래스는 삭제됨 |
| "**F3이 통계에 머문다** — 월별 추이·가격대별 만족도" | 문장 요약 + 81유형으로 대체. `InsightsSummary` 보류 |
| "**P4 필드(빈티지·음용 방법)가 데이터 모델에 없다**" | `DrinkRecord.vintage`, `.servingStyle` 존재 |
| "F6의 일부(**기록 상한**)를 갖고 있다" | 개수 축 폐기(쟁점 5). 상한 없음 |
| "F1의 일부(기록 CRUD, 컬렉션, **검색**)" | 검색 화면 없음(A-3) |

**같은 문서 안에서 시점이 갈라져 있다.** §3의 F1은 2026-08-17 다건 삭제까지 반영돼 있는데,
§6만 2026-08-13 이전에 멈춰 있다. 한 문서를 처음 읽는 사람은 어느 절을 믿어야 할지 알 수 없다.

### B-3. `problem-definition.md` §10·§11 **[중간]**

§10 "현재 구현과의 간극"이 B-2와 같은 낡음을 반복한다 — `DrinkRatingBreakdown`을 현재형으로
부르고, "P4는 전혀 다루지 않는다: 빈티지·음용 방법 필드가 없다"고 단정한다.

§11-4 "**브랜딩 개정 ← 다음 작업**"도 이미 완료된 항목이다(`branding.md` 2-1·2-2 개정됨).

### B-4. PRD 7절이 낡은 임계치를 "현재"라고 부른다 **[중간]**

`prd.md:268`.

> **2026-08-14 실기기 측정**: 현재 임계치(`MIN_SAMPLES_PER_SIDE=3`, `MIN_RATING_GAP=0.5`)에서…

실제 `domain/usecase/TasteThresholds.kt`:

```kotlin
const val MIN_SAMPLES = 6
const val MIN_CORRELATION = 0.45
```

판정이 **양극단 비교 → 상관**으로 바뀌면서(2026-08-14) 상수의 이름도 의미도 바뀌었다.
`MIN_RATING_GAP = 0.5`는 이제 **태그 선호**(`TagThresholds`)에만 남아 있다.

**같은 PRD 안에서 F3은 상관 기반으로 개정돼 있는데 7절만 구 임계치를 현재라고 말한다.**
그 위에 얹힌 "유형이 나온 최소 기록 수 6개"라는 관측값도 구 알고리즘의 것이라, 지금 코드에서
재현되는 숫자가 아니다.

### B-5. `branding.md` — "제품에 유형 개념이 아직 없다" **[중간]**

`branding.md:110`. 스토어 카피 후보를 반려한 사유다.

> "당신의 취향에도 유형이 있습니다" | 문구는 강하나 **제품에 유형 개념이 아직 없다.**

`TasteType` 81유형이 구현돼 있고 `DDTasteTypeBadge`가 화면에 그린다.

**단순 낡음이 아니라 결정이 필요하다** — 반려 사유가 사라졌으므로 이 카피를 되살릴지는
기획 판단이다. 문서만 고치고 넘어갈 항목이 아니다.

### B-6. `software-architecture.md` §7 ↔ §5-1 내부 모순 **[중간]**

| 절 | 주장 |
| --- | --- |
| §7 (구) | "Room 마이그레이션 v1→v2→v3 **폐기, version 1로 재시작**" |
| §5-1 (2026-08-16) | "`version = 3` · `MIGRATION_2_3` · 파괴적 폴백 제거" |

코드는 `version = 3` + `MIGRATION_2_3`으로 §5-1이 맞다. §7의 "`domain/model`의 `R` import 제거"도
이미 해소됐다(도메인 계층에 Android/`R` import 0건 확인).

§7은 재정의 착수 시점의 계획표이므로 **완료 표시를 붙이는 편이 맞다** — §8이 구현 순서에
취소선을 쓰는 것과 같은 방식으로.

### B-7. `build-and-test.md` 예시가 없는 테스트 클래스를 가리킨다 **[경미]**

```powershell
--tests "com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCaseTest"   # 존재하지 않음
```

`SaveDrinkRecordUseCaseTest`는 없다(UseCase 자체가 없다 — A-1). §4의 Robolectric 예시
`DrinkRecordDaoTest`도 없다 — 실제 이름은 `DrinkRecordRepositoryTest`다.

복붙하면 Gradle이 "No tests found"로 **실패**하므로 조용히 통과하지는 않는다. 다만 검증 명령을
정의하는 문서가 실행되지 않는 예시를 싣고 있다.

### B-8. `harness.md` §2가 없는 파일을 가리킨다 **[경미]**

> 화면별 커스텀 UI보다 `ui/component/**Components.kt**`의 `DD*` 공용 컴포넌트 재사용을 우선한다.

`Components.kt`는 없다. 실제로는 10개 파일로 갈라져 있다 — `DDButtons` `DDCards` `DDChips`
`DDBadges` `DDFeedback` `DDRatingInput` `DDDrinkRecordCard` `DDBatchActionBar` `DDUriImage`
`ProbeComponents`. 규칙 자체는 유효하고 경로만 낡았다.

### B-9. `design-system.md` §6-3의 "검색창"이 화면에 없다 **[중간]**

> 3. **컬렉션 & 검색** (`ui/collection/CollectionScreen`) — **검색창** + 주종/재구매 필터 칩

`CollectionScreen`에 텍스트 입력이 없다. 필터 칩만 있다.

**F5(매장 조회)와 소관이 겹치는 서술이라 문서만 고칠 문제가 아니다.** 컬렉션에 검색을 둘지,
F5 전용 화면에만 둘지가 정해져 있지 않다 — `software-architecture.md` §6-2는
"**F5는 별도 화면으로 둔다. 기존 `search`를 재사용하지 않는다**"고 적어 두 문서가 다른 그림을
그린다.

### B-10. §5 카탈로그와 코드의 양방향 차이 **[중간]**

**카탈로그에 있고 코드에 없는 것** (`DDShareCard`는 F4 미착수라 정상, 제외):

| 컴포넌트 | 그 자리를 지금 무엇이 채우나 |
| --- | --- |
| `DDToggleRow` | `DDChip` (`RecordDetailStep.kt:92` 재구매 입력) |
| `DDTextField` | 생 `OutlinedTextField` |
| `DDLoadingContent` | 없음 |
| `DDSnackbar` | 없음 |

§6-1의 매핑 규칙이 `RecordDetailStep`에 `DDToggleRow`를 지정하는데 실제는 `DDChip`이다.

**코드에 있고 카탈로그에 없는 것**: `DDSemanticBadge`(2곳 사용), `DDUriImage`(2곳),
그리고 `DDScreenScaffold` / `DDTopAppBar` / `DDBottomNavigationBar`.

**뒤쪽이 더 중요하다** — §5에 **내비게이션 섀시 절이 아예 없다.** 브레이크포인트(§4)와 모션(§1-3)을
실제로 구현하는 것이 이 세 컴포넌트인데 카탈로그가 그 존재를 모른다.

### B-11. PRD가 디자인 시스템에 없는 컴포넌트를 지정한다 **[경미]**

`prd.md:192` — "Pro 기능 진입 시 잠금 상태와 잠금 해제 시 얻는 것을 보여준다(`DDProLockOverlay`)."

이 이름은 **PRD에만 있다.** `design-system.md` §5 어디에도 없고 코드에도 없다.
F6 미착수라 코드 부재는 정상이지만, **컴포넌트 이름을 정하는 것은 디자인 시스템의 일이다.**

### B-12. `AGENTS.md` 문서 목록이 오늘 승격한 파일을 모른다 **[사소]**

`AGENTS.md:68` — "`specs/designer/` — `design-system.md`, `branding.md`, icon assets under `assets/`".
오늘 승격한 `design-system-audit-2026-08.md`가 빠져 있다.

---

## C. 처리 제안

**문서만 고치면 되는 것과 결정이 필요한 것을 나눠야 한다.**

| 묶음 | 항목 | 성격 |
| --- | --- | --- |
| **C-1. 즉시 정정 가능** | B-1 · B-2 · B-3 · B-4 · B-6 · B-7 · B-8 · B-12 | 사실이 이미 정해져 있다. 코드를 보고 문서를 맞추면 끝 |
| **C-2. 기획 결정 필요** | B-5(유형 카피 부활 여부) · B-9(검색을 어디에 둘지) · B-11(`DDProLockOverlay` 명세화) | 어느 쪽이 맞는지 사람이 정해야 한다 |
| **C-3. 코드 작업** | A-1(UseCase 계층) · A-2 · A-3 | A-1은 규모가 있어 별도 태스크 |

**C-1은 한 커밋으로 묶을 수 있다.** 전부 "코드가 진실이고 문서가 낡음"이며 서로 얽히지 않는다.

**A-1은 쪼개서 해야 한다.** `SaveDrinkRecordUseCase` 하나를 만들어 `isSavable`을 도메인으로
옮기고 테스트를 붙이는 것이 첫 조각이고, 컬렉션·편집 경로는 그다음이다. 한 번에 다섯 개
호출지점을 옮기면 회귀 범위가 F1 전체가 된다.

> [!IMPORTANT]
> **이 점검이 놓치는 것을 밝혀둔다.** 정적 대조만 했다 — 문서의 서술과 코드의 값·구조를 맞댔을
> 뿐, 에뮬레이터에서 화면을 돌려보지 않았다. 3판 감사의 교훈(§5-1 "게이트가 통과해도 앱은 죽을 수
> 있다")이 여기에도 적용된다. **런타임에만 드러나는 불일치는 이 목록에 없다.**
