# 디자인 시스템 점검 (2026-08-16)

> [!NOTE]
> **상태: 초안 — 사용자 확정 전.** 점검: Claude(코드 대조). agy 위임 없음 —
> 우리 명세와 우리 코드를 맞대는 일이라 검증이 곧 작업 자체다.
>
> 이 문서는 세 판을 거쳤다. **1판**(커밋 `9fb85aa`)은 낡은 작업 트리 스냅샷을 보고 틀렸고,
> **2판**은 정정 후 결함 3건을 찾아 고쳤다(커밋 `f68c964`). **3판(현재)**은 §2·§3·§4 전체를
> 값 단위로 재대조한 결과이며, 앞의 두 판이 **카탈로그(§5)만 보고 파운데이션(§3·§4)을
> 보지 않았다는 사실**을 드러낸다.
>
> 1판의 교훈은 여전히 유효하다: 다른 세션이 같은 저장소를 건드릴 수 있으므로
> **감사 직전에 파일을 다시 읽는다.** 대화 중간의 파일 목록은 그 시점의 스냅샷이지 현재 상태가 아니다.

---

## 1. 이미 고친 것 (2판, 커밋 `f68c964`)

| 결함 | 고친 방식 |
| --- | --- |
| 만족도가 화면마다 `3` / `3 / 5` / `4.7점` 세 형식 | `DrinkLabels.rating(Double)` 신설. 정수는 `3점`, 평균은 `4.7점` |
| 간격 토큰 **이름**이 명세와 어긋남(명세 `lg`=20, 코드 `lg`=24) | 코드를 §3.3 스케일에 이름까지 맞춤 |
| 목록에서 '안 맞아요'가 안 보임(재구매만 `★`) | 둘 다 `DDSemanticBadge`로. '그냥 그래요'는 계속 안 그린다 |

`DD*` 컴포넌트 16개가 실존하고 화면에서 실제로 쓰인다는 2판의 확인도 유효하다.

---

## 2. 3판 — 전면 재대조 (2026-08-16)

### 2-1. 대조 방법

앞의 두 판과 다른 점은 **명세의 표를 값 단위로 코드와 맞댔다**는 것이다.
컴포넌트 이름이 있는지가 아니라, §3.1의 30개 hex·§3.2의 8개 Tracking·§4의 3개 브레이크포인트가
코드에 그 값으로 존재하는지를 하나씩 확인했다. 명도 대비는 WCAG 2.1 상대 휘도 공식으로 직접 계산했고,
칩 높이는 `androidx.compose.material3` 1.3.0 소스(`Chip.kt`, `FilterChipTokens`)를 열어 확인했다.

**결과: 명세의 "구조"는 지켜졌고 "값"은 도달하지 않았다.**

### 2-2. 지켜진 것

| 항목 | 근거 |
| --- | --- |
| 서체 2종 제한 (§2-3) | `FontFamily.Serif`는 `DisplayTasteCode` 한 곳뿐 |
| 타입 롤의 크기·행간·굵기 (§3.2) | 8종 중 7종 일치 — **`LabelSmall`만 어긋났다**(명세 11/14sp, 코드 12/16sp). T1에서 정정 |
| Shape 토큰 6/12/18dp (§3.3) | 정의도 일치하고 화면은 `shapes.small/medium/large`로만 쓴다 |
| 간격 토큰 이름 (§3.3) | 2판에서 맞춤 |
| 버튼 48dp (§2-5) | `DDButtons.kt` 4종 모두 `defaultMinSize(minHeight = 48.dp)` |
| 본문 명도 대비 AAA (§2-2) | 실측 라이트 14.4:1 / 다크 14.5:1 |
| 카드 3중 중첩 없음 (§2-6) | 전 화면 최대 1중 |

### 2-3. 위반 — 심각

#### ① 색상 토큰 §3.1이 코드에 없다 — 30개 값 중 6개만 일치

| 명세 토큰 | 명세 Light / Dark | 코드 Light / Dark |
| --- | --- | --- |
| `Paper` | `#FFF8F2` / `#15110E` | `#FFF8F2` ✓ / `#161211` |
| `Surface` | `#FFFFFF` / `#221C17` | `#FFFCF8` / `#211B19` |
| `SurfaceSunk` | `#F6EDE4` / `#1B1612` | `#F4EAE1` / **`#3B332F`** |
| `Ink` | `#241E19` / `#F2E9E0` | `#2C2723` / `#F3ECE6` |
| `InkSoft` | `#6B5F56` / `#B3A498` | `#655B53` / `#D4C7BD` |
| `InkFaint` | `#9C8F84` / `#7D7066` | **대응 토큰 없음** |
| `Line` | `#E7DACC` / `#3A302A` | `#D8CCC1` / `#504741` |
| `LineStrong` | `#D3C2B0` / `#4E4139` | **`#8B8178`** / **`#9E9288`** |
| `Primary` | `#2F6F4E` ✓ / `#6FBF93` | `#2F6F4E` / `#A8D5BA` |
| `PrimaryContainer` | `#E4EFE8` / `#1E3229` | `#D8F0E0` / `#1A5136` |
| `Wine` | `#93425E` ✓ / `#DB90AC` | `#93425E` / `#EFB8C8` |
| `WineContainer` | `#F6E6EC` / `#38222B` | `#FFD8E4` / `#6F2A43` |
| `Malt` | `#9C6722` ✓ / `#DFA75B` | `#9C6722` / `#E7C46A` |
| `MaltContainer` | `#F8ECDA` / `#38290F` | `#FFE2B8` / `#5C3D0E` |
| `Destructive` | `#BA1A1A` ✓ / `#FFB4AB` ✓ | `#BA1A1A` / `#FFB4AB` |

이름 체계도 다르다 — 명세는 `Paper`/`Ink`/`Line`인데 코드는 `DrinkPaperLight`/`CellarInkDark`/
`BottleGreenLight`다. **명세를 읽고 `InkFaint`를 찾으면 없고, `LineStrong`을 찾으면 완전히 다른
회색이 나온다.** §3.1 표는 코드에 반영된 적이 없다.

덤으로 `ChartWineLight #FF8FB3` 등 4개는 명세에 없는 색이고, `Theme.kt`에서 제공만 되고
**소비처가 0곳**이다(죽은 코드).

#### ② 반응형 §4 — 3단계 중 1단계만, 경계도 다르다

`DDScreenScaffold.kt`의 분기는 `maxWidth >= 840.dp` 하나뿐이다.

| | 명세 §4 | 코드 |
| --- | --- | --- |
| Compact `<600dp` | BottomNav / 마진 **16dp** | BottomNav / **20dp** |
| Medium `600~839dp` | **NavigationRail** / 24dp | BottomNav / 20dp |
| Expanded `≥840dp` | **NavigationDrawer(영구)** / 32dp / 콘텐츠폭 720dp 제한 | NavigationRail / 20dp |

Medium 구간이 통째로 없고, Expanded에 와야 할 Drawer 대신 Rail이 온다. **화면 마진 적응이 없다** —
6개 화면 전부 `DrinkDiarySpacing.lg`(20dp) 고정.

**2판이 남긴 "§2 vs §3.3 충돌"의 실체가 여기 있다.** §3.3이 `lg`=20dp를 정의하는 건 맞지만
**화면 가장자리 마진을 정하는 절은 §4이고 거기서 Compact은 16dp다.** 2판이 20dp로 통일한 6곳은
§3.3을 지키면서 §4를 어겼다. 두 절은 서로 다른 것을 정의하므로 충돌이 아니었다 — 2판이 잘못 읽었다.

#### ③ 터치 타깃 48dp — 칩이 전부 32dp

Material3 1.3.0 `Chip.kt`에는 `minimumInteractiveComponentSize`가 없고
`FilterChipTokens.ContainerHeight = 32dp`가 그대로 최종 높이다. §2-5는 48dp 보장은 물론
**"36dp 미만 배치 금지"**라고 썼는데 실제로는 32dp다.

위반 지점: `CollectionScreen` 주종 필터 · `RecordDetailStep` 재구매/서빙 · `TagPicker` 라벨 태그
전부 · `ProfileScreen` 스코프 · `RecordDetailScreen`/`ProfileScreen`의 `AssistChip`.
`TextButton`도 `ButtonDefaults.MinHeight = 40dp`로 미달이다(설정 진입, 뒤로가기, 사진 넣기, 더 남기기).

#### ④ 아이콘 §2-4 — 에셋이 0개

`res/drawable/`에 `ic_launcher_background.xml` 하나뿐이다. 하단 내비는 아이콘 자리에 텍스트를 넣고,
뒤로가기는 `TextButton("뒤로")`, FAB는 `Text("+")`, 재구매 뱃지는 `★` 문자다.
**`DDIconButton`은 정의만 있고 사용처가 0곳이다.**

### 2-4. 위반 — 중간

#### ⑤ 자간(Tracking) 8종 전부 무효 — 단위를 잘못 썼다

명세는 `em`인데 코드는 `sp`다.

```text
DisplayTasteCode  명세 +0.05em (=1.6sp)  → 코드  0.05.sp    (32배 작음)
HeadlineSentence  명세 -0.02em (=-0.4sp) → 코드 (-0.02).sp  (20배 작음)
```

Compose의 `letterSpacing`은 `.sp`면 절대값, `.em`이면 폰트 크기 비례다. 지금 값은 사실상 0이라
**§3.2의 Tracking 열이 화면에 반영된 곳이 하나도 없다.** §2-3 "자간 보정 없는 거대 텍스트 남발 금지"에
정면으로 걸린다.

#### ⑥ 하단 내비 — 명세에 없는 글래스모피즘 + 하드코딩 흰색 그라데이션

```kotlin
Brush.verticalGradient(listOf(Color.White.copy(0.58f), Color.White.copy(0.12f)))
```

§2-1 "코드 내 임의 Hex 하드코딩 금지", §2-6 "빛나는 네온 테두리 및 그라데이션 남발 금지"에 걸리고,
**테마에 반응하지 않아 다크에서도 흰 테두리가 그대로 빛난다.** `haze` 블러 자체가 명세 어디에도 없다.
같은 블록의 `28dp` radius도 Shape 토큰(6/12/18) 밖이다.

#### ⑦ 위스키 뱃지가 AA 미달 — 그리고 명세를 따라도 미달이다

`DDDrinkBadge`의 `secondary #9C6722` on `secondaryContainer #FFE2B8` = **3.84:1**.
`labelSmall` 11sp는 본문 취급이라 4.5:1이 필요하다.

**명세 팔레트로 바꿔도 `Malt #9C6722` on `MaltContainer #F8ECDA`는 4.11:1로 여전히 미달이다.**
즉 이건 코드가 명세를 어긴 게 아니라 **§3.1이 자신의 §2 규칙("보조 4.5:1 AA")을 검증 없이
통과시킨 것**이다. 나머지 조합은 전부 AA 이상.

#### ⑧ 카탈로그 9개 미구현 — 그리고 이것이 ③이 새는 이유

`DDRatingInput`, `DDToggleRow`, `DDTextField`, `DDTagChipGroup`, `DDDrinkRecordCard`,
`DDLoadingContent`, `DDEmptyContent`, `DDConfirmDialog`, `DDSnackbar`가 없다.
`DDShareCard`는 F4 미착수라 정상이다.

§6이 이름을 찍어 요구한 것도 포함된다 — 기록 플로우의 `DDRatingInput`/`DDToggleRow`/`DDTagChipGroup`,
삭제의 `DDConfirmDialog`. 실제로는 각각 `private fun RatingPicker`, 생 `FilterChip`, `TagPicker`,
생 `AlertDialog`다.

**2판의 "M3 직접 사용은 결함이 아니다"는 판정을 뒤집는다.** 2판은 "래퍼가 값을 하는 경우는 동작이나
규칙이 들어갈 때뿐"이라고 썼는데, **바로 그 기준으로 결함이다.** 48dp 터치 타깃은 규칙이고,
그 규칙을 걸 자리가 없어서 칩 5곳이 전부 32dp로 샌다. `DDTagChipGroup` 하나가 있었으면
한 곳만 고치면 된다.

### 2-5. 위반 — 경미

- **토큰 밖 dp 산재** (§2-5): `10dp`(DDCards 뱃지 패딩·DrinkPicker 3곳·내비), `14dp`(3곳),
  `18dp`(2곳), `28dp`(2곳), `6dp` 간격 용도 3곳, `2dp` 2곳.
- **타입 스케일 밖 롤 21곳**: `bodySmall` 11회, `headlineSmall` 5회, `titleSmall` 3회,
  `labelMedium` 2회. `bodySmall`/`titleSmall`/`labelMedium`은 `Typography`에서 재정의하지 않아
  **Material 기본값(Roboto, 다른 행간·자간)이 그대로 나오고**, `headlineSmall`은 명세에 없는
  **9번째 스타일**(`CompactTitle` 20/26sp)을 코드가 따로 만들어 쓰고 있었다. §3.2에 대응 롤이 없다.
- **모션 §1-3 미적용**: Depth In/Out(Z-axis), 탭 전환 모핑, Selection Mode 전부 없음.
  있는 것은 `Crossfade` 2곳.
- **문구 불일치 잔존**: `DDRepurchaseBadge` 기본값 `"★ 다시 살래요"` vs `DrinkLabels` `"또 살래요"`.
  2판의 미해결 항목이 그대로다.
- **누를 수 없는 칩**: `AssistChip(onClick = {})` 2곳. 눌리게 생겼는데 아무 일도 없다.
- **선택 상태가 명세와 다른 색을 쓴다** (T2 작업 중 화면에서 발견): §3.1은 `PrimaryContainer`를
  "선택된 칩 배경", `SurfaceSunk`를 "칩 미선택 상태"로 정의하는데, M3의 `FilterChip`과
  `NavigationBarItem`은 선택 표시에 **`secondaryContainer`(= 우리 매핑에서 `MaltContainer`)**를
  기본값으로 쓴다. 팔레트만 맞추면 선택 칩이 위스키 앰버로 칠해진다. 색을 명시하지 않는 한
  M3 기본값이 명세를 이긴다 — T3(내비)·T5(칩)에서 잡는다.

---

## 3. 명세 자체를 고쳐야 하는 것

코드를 명세에 맞추기 **전에** 디자인 부서가 정해야 한다.

1. **`Malt` / `MaltContainer` 재설계** (2-4 ⑦). 지금 값으로는 코드를 명세에 맞춰도 AA에 못 미친다.
   → **결정: `Malt` Light를 `#9C6722` → `#8A5A1B`로 한 단계만 어둡게 한다**
   (`#F8ECDA` 대비 4.11:1 → **5.06:1**, `Paper` 위에서는 5.61:1). 더 어두운 값도 검토했으나
   (`#7A4E14`는 6.15:1) **AA를 넘기는 데 필요한 최소 변경**을 택했다 — 브랜드 앰버를 갈색으로
   밀어낼 이유가 없다. Dark는 `#DFA75B` on `#38290F` = 6.58:1로 이미 통과라 그대로 둔다.
2. **§2 표의 간격 토큰 목록에 `20dp` 추가** — §3.3이 정의하는데 §2가 빠뜨려 "비표준 dp"로 읽힌다.
3. **§3.2에 캡션 롤이 없다** — 코드가 `bodySmall`을 11회 쓰는 것은 필요가 있다는 뜻이다.
   → **결정: 롤을 늘리지 않고 `BodyMedium`(13sp)으로 흡수한다.** §3.2가 `BodyMedium`을
   "카드 설명, 보조 텍스트, 날짜/가격"으로 정의하므로 그 자리가 이미 캡션이다.
   서체 2종·롤 8종 제한이 이 시스템의 뼈대라 롤을 늘리는 쪽을 택하지 않는다.

## 4. 유보한 것

- ~~**아이콘 에셋 제작 (2-3 ④)**~~ — **T7에서 완료.** 24×24dp·2dp 선·둥근 캡/조인으로 6종을
  자체 제작했다(`ic_nav_dashboard` 스템 글라스 / `ic_nav_collection` 양장본 / `ic_nav_search` /
  `ic_back` / `ic_add` / `ic_settings`). `DDIconButton`이 처음으로 실제 사용처를 얻었고,
  하단 바·Rail·Drawer의 라벨을 되살렸다.

  **렌더링해야만 보이는 결함이 하나 있었다**: 설정 아이콘의 노브를 반지름 2 원으로 그렸는데
  선 굵기가 2라 안쪽 구멍(r=1)이 선에 완전히 먹혀 **꽉 찬 원**이 됐다. path 데이터만 읽어서는
  알 수 없다 — 노브를 선을 가로지르는 짧은 세로 눈금으로 바꿨다.
  **2dp 균일 선 규격에서는 반지름 3.5 미만의 원이 링으로 읽히지 않는다.**
- ~~**모션 (2-5)**~~ — **T8에서 Depth In/Out과 탭 전환까지 완료.** 깊이가 다른 이동은 Z축
  (들어갈 때 0.8→1.0, 나올 때 1.1→1.0), 같은 깊이의 탭 이동은 fade-through로 나눴다.

  이를 위해 **스캐폴드를 하나로 올렸다.** 화면마다 스캐폴드를 만들면 탭을 옮길 때 상단 바와
  하단 바까지 다시 그려지는데, 그것들은 제자리에 있어야 하는 것들이다. 전이 중 프레임을 재서
  확인했다 — 콘텐츠 영역 차이 12.96, 하단 바 차이 1.82(= 사실상 제자리).

  **`Selection Mode 일괄 작업 바`는 T8 시점에 만들지 않았다** — 명세 1절 3번에만 있고
  `prd.md`·`mvp-scope.md` 어디에도 대응 기능이 없었기 때문이다(없는 제품 동작을 발명하지 않는다).

  → **2026-08-17 해소.** 사용자가 확정했다: **MVP에 넣고, 되돌리기는 확인 팝업 한 번이 전부**다.
  `prd.md` F1-2를 먼저 신설하고(커밋 `2b3af9c`) 그 위에 구현했다(T11·T12).
  근거 문서(`design-system-ux-research.md` 4.2절 3번)에는 원래 인터랙션까지 정의돼 있었다 —
  **확정 명세가 이름만 옮겨 적는 바람에 "정의가 없는 항목"처럼 보였다.**
- ~~**화면별 적응형 레이아웃 (§4 마지막 열)**~~ — **T9에서 완료.** T4가 내비게이션 구조와 마진까지
  맞췄고, T9가 마지막 열을 채웠다.
  - **컬렉션**: Compact이 아니면 목록(40%)과 상세(60%)를 한 화면에 놓는다(`CollectionListDetail`).
    폭이 있으면 목록에서 상세로 **나갈** 이유가 없다 — 여러 기록을 훑어 비교하는 것이 이 화면의
    용도라, 한 건 볼 때마다 목록을 떠났다 돌아오면 훑던 자리를 잃는다. 이때 상세는 더 이상
    별도 화면이 아니므로 하단 탭/레일이 유지되고 뒤로 화살표는 나오지 않는다.
  - **취향 프로필**: Compact이 아니면 요약(좌 40%)과 축 목록(우 60%)으로 나눈다. 요약은 짧고
    축 목록은 길어서 한 스크롤에 묶으면 왼쪽에 빈 스크롤이 생긴다 — 칸마다 따로 스크롤한다.
  - **기록 마법사(F2)·편집·설정은 1단으로 남긴다.** 2단을 강제하면 기본 입력 경로가 무거워져
    `prd.md` F2의 탭 예산과 충돌한다. §4의 문면보다 F2가 우선한다고 판단했다.

  T4에서 "읽는 곳이 없다"며 지웠던 `LocalDDWindowSize`를 되살렸다 — 이제 소비처가 생겼다.

## 5. 수정 계획 — 6단계

`agy`(`gemini-3.7-flash-high`) 구현 → Claude 리뷰·게이트·에뮬레이터 확인 → 커밋을 단계마다 반복한다.
파운데이션을 먼저 세우고 컴포넌트를 그 위에 올리는 순서다.

| 단계 | 범위 | 해결하는 항목 |
| --- | --- | --- |
| T1 | 타이포 — `letterSpacing` `em` 전환 + 스케일 밖 롤 16곳 정리 | ⑤, 2-5 |
| T2 | 색상 토큰 — `Color.kt`/`Theme.kt`를 §3.1 이름·값으로 | ①, ⑦ |
| T3 | 하단 내비 정화 — 흰색 그라데이션 제거, 비표준 dp/radius 정리 | ⑥, 2-5 |
| T4 | 반응형 3단계 — Compact/Medium/Expanded + 마진 16/24/32 | ② |
| T5 | `DDTagChipGroup`·`DDChip` 신설 + 칩 5곳 교체 (48dp) | ③, ⑧ 일부 |
| T6 | `DDConfirmDialog`·`DDEmptyContent`·`DDRatingInput`·`DDDrinkRecordCard` + 문구 통일 | ⑧, 2-5 |

각 단계의 Definition of Done은 `harness.md` §4 그대로다. 추가로 **`DesignTokenTest`(Claude 작성)**를
T2에서 도입해 팔레트 hex와 명도 대비를 테스트로 고정한다 — 이번 감사에서 손으로 계산한 것을
다음 회귀 때 또 손으로 계산하지 않기 위해서다.

### 5-1. 이번 작업에서 배운 것 — **게이트가 통과해도 앱은 죽을 수 있다**

T1에서 명세 3.2절의 Tracking을 `.em`으로 옮겼다. 명세가 em이라고 썼으니 맞는 판단처럼 보였고,
`ktlintCheck` · `lint` · 112개 유닛테스트가 전부 통과했다. **그런데 텍스트 필드가 있는 화면은 전부
죽었다** — `java.lang.IllegalArgumentException: Cannot perform operation for Em and Sp`.

`OutlinedTextField`의 라벨은 `bodyLarge`(우리 것)와 `bodySmall`(재정의하지 않아 Material 기본값)
사이를 애니메이션으로 보간하는데, Compose는 letterSpacing의 Em과 Sp를 섞어 보간하지 못한다.
**우리 토큰만 보면 일관되고, Material 기본값과 만나는 경계에서만 터진다.**

T5까지 넉 단계 동안 이걸 못 봤다. 에뮬레이터 확인을 매번 했지만 DB가 비어 있어 **기록 상세 단계에
도달한 적이 없었고**, 그 화면이 이 앱의 유일한 텍스트 필드 화면이다. T6에서 기록을 하나 만들어
보고 나서야 드러났다.

세 가지를 남긴다.

1. **em 값은 폰트 크기로 환산해 sp로 적는다.** 명세의 의도(크기 비례 자간)는 지키면서 단위 혼합을
   피한다. 곱셈식(`(32 * 0.05).sp`)을 소스에 남겨 명세의 em 값이 코드에서 읽히게 했다.
2. **`DesignTokenTest`에 단위 검사를 추가했다.** 어떤 롤이든 letterSpacing이 Em이면 실패한다.
3. **에뮬레이터 확인은 "화면이 뜨는지"가 아니라 "그 변경이 닿는 화면에 실제로 가는지"여야 한다.**
   데이터가 없으면 절반의 화면에 도달할 수 없다.
