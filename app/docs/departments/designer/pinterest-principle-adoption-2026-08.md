# Pinterest 디자인 시스템 — 원칙 흡수 검토 (2026-08-17)

> [!NOTE]
> **부서 초안. 확정 명세 아님.** 사용자 승인 후 `../../specs/designer/design-system.md`로 승격한다.
> 소스: `https://getdesign.md/pinterest/design-md` (사용자 제공 전문).
> 사용자 지시로 **적용 깊이는 "원칙만 흡수"** — 팔레트·서체·4:5 고정은 건드리지 않는다.

## 1. 왜 이 소스를 보는가, 그리고 무엇까지만 보는가

Pinterest 시스템의 지시 원칙은 한 문장이다 — **"사진 앞에서 비켜선다(get out of the photograph's
way)."** 이건 이 저장소가 2026-08-17에 이미 걷기 시작한 방향과 같다. `DDPhotoField`가 기록 폼의
첫 자리로 올라갔고(`prd.md` F1-3), `DDDrinkHighlightRow`가 취향 유형 바로 아래에 놓였고
(F3-4 (a)), `DDMonthlySummaryCard`에서 사진이 빠졌다. 그 커밋들이 고친 문제("대시보드가 통째로
글자가 됐다")를 Pinterest는 시스템 차원의 규칙으로 못 박아 두고 있다.

**그래서 볼 가치가 있는 것은 팔레트가 아니라 사진 표면을 다루는 규칙 몇 개뿐이다.**

이 소스의 한계는 문서가 스스로 밝히고 있다(Known Gaps).

- 캡처 대상이 **로그아웃 상태의 웹 마케팅 페이지와 검색 결과**다. `authenticated chrome`
  (로그인 홈 피드·보드·프로필)과 **Pinterest 모바일 앱 화면은 캡처 범위 밖**이라고 적혀 있다.
- 즉 이 문서는 **인앱 크롬의 소스로는 약하다.** 70px 히어로 헤드라인, sticky top-nav,
  4-column 푸터, 로그인 모달 같은 것은 우리에게 대응물이 없다.
- hover 상태 미문서화 — 터치 전용인 우리에겐 애초에 무관하다.

## 2. 채택하지 않는 것 (재논쟁하지 않기 위해 사유를 남긴다)

| Pinterest 규격 | 반려 사유 |
| --- | --- |
| Primary = Pinterest Red `#e60023` | 보틀그린 `#2F6F4E`는 `branding.md` 2-2 **사용자 확정값**이고 앱 아이콘 배경(5절)까지 같은 값이다. `DesignTokenTest`가 hex와 대비비를 검사한다. 색 하나를 바꾸는 게 아니라 브랜드·에셋·테스트를 동시에 뒤집는 일이다 |
| Pin Sans / Inter 단일 산세리프 | Serif+Sans 페어링이 확정이고 `DisplayTasteCode`(취향 유형 코드)가 Serif를 쓴다. 그 Serif가 "아날로그 양장본 저널"이라는 1절 컨셉을 혼자 지탱한다 |
| `display-xl` 70px, 히어로/피처 카드 | 마케팅 페이지 티어다. 앱에 히어로 섹션이 없다 |
| 핀 이미지 **자연 비율 보존**(1:1/2:3/3:4/4:5 혼재) | 4:5 고정은 근거 있는 결정이다 — "찍을 때 본 모양과 볼 때 보는 모양이 다르면 같은 사진으로 읽히지 않는다"(`design-system.md` 5.3). 자연 비율은 그 근거를 정면으로 부순다 |
| 컬렉션을 메이슨리 핀 그리드로 | 두 가지가 막는다. (a) 컬렉션은 훑어서 **구매를 판단하는** 자리라 이름·만족도·재구매 뱃지가 사라지면 F5(매장 조회)가 무너진다. Pinterest 검색은 "볼 것"을 찾고 우리는 "샀던 것"을 찾는다. (b) **사진 없는 기록도 카드가 되어야 하는데**(5.3) 메이슨리에 넣을 자연 비율이 없다 |
| 그림자 전면 금지 | 현행 2절이 "1px 스트로크 + 부드러운 다단계 섀도우"를 허용한다. 시스템 전체 규칙은 그대로 두고 **사진 표면에 한해서만** 뺀다(3-1절) |
| 반경 16px/32px 2단 | `6/12/18dp` 3단이 이미 있다. Pinterest의 유효한 부분은 절대값이 아니라 **"md/lg를 표면 크기로 가른다"**는 배정 규칙이고, 그건 우리 스케일 안에서 그대로 쓸 수 있다(3-5절) |

## 3. 채택하는 원칙 다섯 개

각 항목은 **Pinterest 규칙 → 우리 언어로의 번역 → 현행 코드의 어긋난 지점 → 제안** 순이다.
근거로 든 줄 번호는 2026-08-17 `master`(`0d65875`) 기준이다.

### 3-1. 사진이 채워지면 테두리를 걷는다

> Pinterest: `pin-card`는 padding 0, 그림자 없음, 테두리 없음. *"the photograph IS the card."*

**번역**: 테두리는 **표면**에 긋는 선이지 **내용**에 두르는 액자가 아니다. 사진에는 이미 자기
경계가 있다.

**현행**: 사진을 그리는 두 곳 모두 사진이 있든 없든 무조건 1px 테두리를 두른다.

- [DDDrinkHighlightRow.kt:73](app/src/main/java/com/bluemarlin/drinkdiary/ui/component/DDDrinkHighlightRow.kt:73) — `border = BorderStroke(1.dp, outlineVariant)`
- [DDPhotoField.kt:65](app/src/main/java/com/bluemarlin/drinkdiary/ui/component/DDPhotoField.kt:65) — 같은 줄, `imageUri` 분기 밖에 있다

**제안**: **사진이 있을 때는 테두리를 그리지 않는다. 비어 있을 때는 남긴다.**
빈 상태에서 테두리는 "여기 자리가 있다"고 말하는 유일한 신호이고(`DDPhotoField`의 존재 이유가
그것이다), `DDDrinkHighlightRow`의 '사진 없음' 카드도 마찬가지다. 채워지면 그 선은 할 일이 없다.

현행 2절 "1px 스트로크로 은은한 계층 표현"과 충돌하지 않는다 — 다만 **예외를 명시해야** 규칙을
읽은 사람이 사진에도 선을 두른다. 5절 참고.

### 3-2. 메타데이터 한 조각은 사진 아래가 아니라 사진 위로

> Pinterest: `pin-overlay-pill` — 불투명 `canvas` 배경, `rounded.full`, padding `6px 12px`,
> 사진 모서리에 고정. *"metadata sits over the image as an overlay pill, not below it."*

**번역**: 사진 아래에 쌓는 줄 수만큼 사진이 짧아진다.

**현행**: `HighlightCard`는 132dp 폭 카드(사진 높이 165dp) 아래에 **세 줄**을 쌓는다 —
`label` / `name` / `detail`([DDDrinkHighlightRow.kt:99-114](app/src/main/java/com/bluemarlin/drinkdiary/ui/component/DDDrinkHighlightRow.kt:99)).
글자 블록이 대략 54dp로 **사진 층 전체 높이의 1/4**이다. `design-system.md` 6절이 이 자리에 대해
"글자보다 먼저 보여야 할 것이 사진이다"라고 적은 것과 어긋난다.

**제안**: **`label`("가장 높게 준" 등)만 사진 왼쪽 아래 오버레이 필로 올린다.**

- `name`·`detail`은 아래에 남긴다. 술 이름은 이 앱에서 사진보다 덜 중요하지 않고, 필을 둘 겹치면
  사진을 가리려고 사진을 앞세운 꼴이 된다. **위로 올리는 것은 한 조각뿐이다.**
- 필 배경은 **불투명 `surface`**여야 한다. 반투명은 사진에 따라 대비가 달라져 2절의 AA(4.5:1)를
  지킬 수 없다 — Pinterest도 필 배경을 불투명 `canvas`로 못 박아 두었다.
- 결과: 카드당 텍스트 줄 3 → 2. 같은 폭에서 사진이 차지하는 비율이 올라간다.

`label`은 이미 어미 없는 짧은 명사구라(`DDHighlightCard` 주석, `branding.md` 2-3) 필에 그대로 들어간다.

### 3-3. 액센트는 "화면당 한 번"을 목표로 아낀다

> Pinterest: *"Keep `{colors.primary}` scarce — at most one red CTA per fold."*

**번역**: 우리 2절의 "액센트 최대 2개"·60-30-10과 같은 말인데, **한 화면에 몇 번 찍히는지를
세어 본 적이 없다.**

**현행 — 세어 보면 이미 어기고 있다.** 컬렉션 목록 한 행에서 `primary`가 최대 세 곳이다.

| 자리 | 위치 |
| --- | --- |
| 만족도 숫자 | [DDDrinkRecordCard.kt:157](app/src/main/java/com/bluemarlin/drinkdiary/ui/component/DDDrinkRecordCard.kt:157) |
| 재구매 뱃지 | [DDDrinkRecordCard.kt:43](app/src/main/java/com/bluemarlin/drinkdiary/ui/component/DDDrinkRecordCard.kt:43) |
| 선택 테두리 / 체크 아이콘 | [:91](app/src/main/java/com/bluemarlin/drinkdiary/ui/component/DDDrinkRecordCard.kt:91), [:121](app/src/main/java/com/bluemarlin/drinkdiary/ui/component/DDDrinkRecordCard.kt:121) |

만족도는 **모든 행에** 있으므로 8행짜리 화면이면 보틀그린이 최소 8회, 하단 네비 선택 항목까지
더해 열 번 넘게 찍힌다. 어긴 결과는 미감 문제가 아니라 **강조가 강조로 안 읽히는 것**이다.

**제안**: **목록 행의 만족도 숫자에서 `primary`를 뺀다**(`onSurface`로).

- 그 행에서 보틀그린이 남는 자리는 **재구매 뱃지 하나**다. 그게 매장에서 3초 만에 확인해야 하는
  것이고(`design-system.md` 5.3 `DDRepurchaseBadge`), 색을 독점할 자격이 있는 유일한 신호다.
- 만족도 숫자는 이미 `titleLarge`로 **크기가 강조를 맡고 있다.** 색까지 줄 필요가 없다.
- **선택 모드의 `primary`는 남긴다.** 일시적 상태이고, 그 순간엔 그게 유일한 강조인 게 맞다.

### 3-4. 사진끼리의 거터를 좁힌다

> Pinterest: 핀 그리드 거터 `spacing.sm`(8px) — 시스템에서 가장 좁다. 의도는
> *"designed so imagery effectively touches across columns."*

**번역**: 사진이 붙으면 "사진 묶음"으로 읽히고, 떨어지면 "카드 여러 장"으로 읽힌다.

**현행**: `DDDrinkHighlightRow`의 카드 간격이 `spacing.sm`(12dp)이다
([DDDrinkHighlightRow.kt:50](app/src/main/java/com/bluemarlin/drinkdiary/ui/component/DDDrinkHighlightRow.kt:50)).

**제안**: `spacing.xs`(**8dp**)로 한 칸 내린다. **새 토큰을 만들지 않는다** — 기존 스케일 안에서
배정만 바꾼다(2절 "비표준 dp 금지"를 지킨다).

### 3-5. 반경은 표면 크기를 따라간다

> Pinterest: `rounded.md`(16px)가 대부분, `rounded.lg`(32px)는 큰 카드·모달만.
> *"Don't introduce a third radius value between 16px and 32px."*

**번역**: 유효한 것은 절대값이 아니라 **"작은 표면은 md, 큰 표면은 lg"**라는 배정 규칙이다.

**현행**: 폭이 1.6배 차이 나는 두 사진 표면이 같은 토큰을 쓴다.

| 표면 | 폭 | 반경 | 폭 대비 |
| --- | --- | --- | --- |
| `HighlightCard` | 132dp (고정) | `shapes.large` 18dp | 13.6% |
| `DDPhotoField` | 콘텐츠폭의 66% ≈ 216dp<br>(360dp 폰, 마진 16dp 기준) | `shapes.large` 18dp | 8.3% |

같은 토큰인데 **눈에는 다른 모양**이다. 작은 쪽이 눈에 띄게 더 둥글다.

**제안**: **작은 사진 타일(`HighlightCard`)은 `shapes.medium`(12dp), 큰 사진 표면
(`DDPhotoField`)은 `shapes.large`(18dp).** 값은 늘리지 않고 배정만 고친다 — 12/132 = 9.1%로
두 표면의 체감 곡률이 가까워진다.

## 4. 원칙과 별개로 발견한 것 — 4:5 규칙이 컬렉션 목록에는 적용돼 있지 않다

`design-system.md` 5.3은 `DDPhotoField`와 `DDDrinkHighlightRow`에 대해서만 4:5를 못 박았다.
그런데 컬렉션 목록의 썸네일은 **1:1 정사각**이다 —
[DDDrinkRecordCard.kt:103](app/src/main/java/com/bluemarlin/drinkdiary/ui/component/DDDrinkRecordCard.kt:103),
`Modifier.size(56.dp)`.

4:5를 정한 근거("찍을 때 본 모양과 볼 때 보는 모양이 다르면 같은 사진으로 읽히지 않는다")는
컬렉션에도 그대로 적용된다. **오히려 컬렉션이 사진을 가장 많이 보는 자리다** — 기록 하나당
대시보드에서 많아야 한 번 보이는 사진을, 목록에서는 스크롤할 때마다 본다. 세로로 찍은 병이
목록에서만 정사각으로 잘리면, 라벨 윗부분과 아랫부분이 사라진다.

**제안**: **48×60dp(4:5)로.** 폭을 56→48로 줄이면 이름 줄이 8dp 넓어진다. 행 높이는 텍스트 열
(이름 22 + 부제 18 + 뱃지 + 간격)이 결정하고 있어 60dp 썸네일이 행을 키우지 않을 것으로 보이나,
**이건 계산이지 관측이 아니다 — 에뮬레이터에서 확인해야 한다**(6절).

이 항목은 Pinterest에서 온 것이 아니라 **우리 규칙의 미적용 지점**이다. 승격 시 5.3의
`DDDrinkRecordCard` 행에 4:5를 명시해야 같은 누락이 반복되지 않는다.

## 5. 승격 시 `design-system.md`에 들어갈 문구 (초안)

**2절 표 6번 행(표면 장식)에 예외를 단다:**

> • 1px의 단정한 외곽선과 부드러운 다단계 섀도우로 은은한 계층 표현
> — **단, 사진이 채워진 표면에는 테두리를 두르지 않는다.** 테두리는 표면에 긋는 선이지 내용에
> 두르는 액자가 아니며, 사진에는 이미 자기 경계가 있다. **빈 상태에서는 남긴다** — 그때는
> 그 선이 "여기 자리가 있다"고 말하는 유일한 신호다.

**2절 표 1번 행(색상 배합)에 세는 단위를 준다:**

> • 한 화면 내 액센트 컬러 최대 2개로 제한 — **"종류 2개"가 아니라 "찍히는 횟수"로 센다.**
> 목록처럼 같은 행이 반복되는 화면에서는 **반복되는 요소에 액센트를 두지 않는다.** 한 색이
> 여덟 번 찍히면 그 색은 더 이상 강조가 아니다.

**5.3절 `DDDrinkHighlightRow` 항목에 덧붙임:**

> 카드 아래 텍스트는 **두 줄까지**다. 레이블은 사진 왼쪽 아래 **불투명 오버레이 필**로 올린다 —
> 사진 아래에 쌓는 줄 수만큼 사진이 짧아지고, 이 자리는 글자보다 사진이 먼저인 자리다.
> 필이 반투명이면 사진에 따라 대비가 무너져 2절의 AA를 지킬 수 없다. **위로 올리는 것은 한
> 조각뿐이다** — 둘 이상이면 사진을 앞세우려고 사진을 가리는 꼴이 된다.
> 카드 간격은 `xs`(8dp)다: 사진은 붙어야 묶음으로 읽힌다.

**5.3절 `DDDrinkRecordCard` 항목에 덧붙임:**

> 썸네일은 `DDPhotoField`·`DDDrinkHighlightRow`와 **같은 4:5**(48×60dp)다. 목록은 사진을 가장
> 많이 보는 자리이고, 여기서만 정사각으로 자르면 세로로 찍은 병의 라벨 위아래가 사라진다.
> **만족도 숫자에 액센트를 쓰지 않는다** — 모든 행에 있는 것에 색을 주면 색이 값을 잃는다.
> 이 행에서 보틀그린을 갖는 것은 재구매 뱃지 하나다.

**3.3절 라운드 토큰 설명에 배정 규칙 추가:**

> 사진 표면의 반경은 **표면 크기를 따른다** — 작은 타일(≤150dp)은 `ShapeMedium`(12dp),
> 큰 표면은 `ShapeLarge`(18dp). 폭이 1.6배만 달라도 같은 토큰이 눈에는 다른 모양이 된다
> (132dp에서 18dp는 폭의 13.6%, 216dp에서는 8.3%).

## 6. 승인 후 할 일과 검증 방법

| 순서 | 할 일 |
| --- | --- |
| 1 | 이 초안을 `specs/designer/design-system.md`에 반영(5절 문구) 후 `task-log.md`에 승격 기록 |
| 2 | 코드 반영 — `DDDrinkHighlightRow`(3-1·3-2·3-4·3-5), `DDPhotoField`(3-1), `DDDrinkRecordCard`(3-3·4절) |
| 3 | `ktlintCheck` / `testDebugUnitTest` / `lint` |
| 4 | **에뮬레이터 시각 검증** |

4번은 생략할 수 없다. 이번 제안은 전부 **눈으로만 판정되는 것**이다 — 게이트는 테두리가
사라졌는지, 필이 사진을 가리는지, 48dp 썸네일이 행 높이를 밀어 올리는지 아무것도 말해 주지
않는다. `design-system.md` 5.2·6절의 주석들이 "2026-08-17 에뮬레이터에서 확인"을 근거로 달려
있는 것과 같은 이유다.

**색 토큰은 이번 작업에서 바뀌지 않으므로 `DesignTokenTest`는 그대로 통과해야 한다.** 이 테스트가
깨진다면 범위를 넘어선 것이니 되돌린다.

## 7. 검증 결과 (2026-08-17, 에뮬레이터 `Medium_Phone_API_36.1`)

승인 후 반영하고 라이트·다크 양쪽에서 육안 확인했다. 게이트는 `ktlintCheck` /
`testDebugUnitTest` / `lint` / `assembleDebug` 모두 통과했고, 색 토큰을 건드리지 않았으므로
`DesignTokenTest`도 그대로 통과했다.

| 항목 | 결과 |
| --- | --- |
| 3-1 테두리 | ✅ `DDPhotoField` 빈 상태 선 유지 / 채워지면 사라짐. `DDDrinkHighlightRow`도 '사진 없음' 카드에만 선이 남음 |
| 3-2 오버레이 필 | ✅ 레이블이 사진 왼쪽 아래 불투명 필로 올라가고 아래 텍스트가 2줄로 줄었다 |
| 3-3 액센트 | ✅ 컬렉션 목록의 만족도 숫자가 잉크색으로 내려갔고, 행에 남은 브랜드색은 재구매 뱃지뿐이다 |
| 3-4 거터 | ✅ 실측 ≈8dp |
| 3-5 반경 | ✅ 132dp 타일이 `ShapeMedium`으로 내려갔다. 실측 카드 폭 131dp(설계 132dp) |
| 4절 4:5 썸네일 | ✅ 세로 썸네일로 바뀌었고, **행 높이는 밀리지 않았다** — 사진 있는 행(발렌타인 17)과 사진 없는 행(샤블리)이 같은 높이였다. 초안 작성 시 "계산이지 관측이 아니다"라고 남겼던 항목이 관측으로 확인됐다 |

**아직 확인되지 않은 것 두 가지** — 지금 데이터로는 재현되지 않는다.

- **긴 레이블의 필 넘침.** 코드는 말줄임(`TextOverflow.Ellipsis`)으로 자르게 돼 있다. 아래로
  되돌리지 않은 이유는 되돌리면 레이블 길이에 따라 세 번째 줄이 생겼다 말았다 하면서 같은 목록의
  카드들이 서로 다른 높이를 갖기 때문이다. 현재 레이블("가장 높게 준", "가장 최근")은 전부 짧아
  **넘치는 경우를 실제로 보지 못했다.**
- **아주 어두운 사진 위의 필.** 확인에 쓴 사진이 밝은 톤이라 다크 테마에서 필(`surface #221C17`)이
  잘 떴다. **필 안의 글자 대비는 구조적으로 보장된다** — 배경이 불투명 `surface`이고 글자가
  `onSurface`라 사진과 무관하다. 불확실한 것은 글자 가독성이 아니라 **어두운 사진 위에서 필의
  윤곽이 묻히는지**이며, 이는 대비 요건이 아니라 미감 문제다.

## 8. 손대지 않은 것

- **`DDMonthlySummaryCard`·`DDRecentTrendCard`의 테두리** — 사진이 없는 표면이므로 3-1의 대상이
  아니다. 그대로 뒀다. 이 개정은 사진 표면만 건드린다.
- **`DDTasteSentenceCard`·`DDProfileProgressCard`** — 같은 이유로 선을 유지한다. 대시보드에서
  취향 카드의 선은 남고 사진 카드의 선만 사라지는 것이 의도다.
