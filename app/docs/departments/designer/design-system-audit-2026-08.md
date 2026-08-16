# 디자인 시스템 점검 (2026-08-16)

> [!NOTE]
> **상태: 초안 — 사용자 확정 전.** 점검: Claude(코드 대조). agy 위임 없음 —
> 이 감사는 우리 명세와 우리 코드를 맞대는 일이라 검증이 곧 작업 자체다.
>
> 아래 수치는 전부 `app/src/main/java/com/bluemarlin/drinkdiary/ui` 실제 대조 결과다.

## 결론 먼저

**`design-system.md`는 지금 위험한 문서다.** 낡은 게 문제가 아니라, **틀린 내용을 자신 있게
말하고 있어서** 이 문서를 읽는 에이전트가 없는 컴포넌트를 있다고 믿는다.

동시에, **코드 쪽 문제는 생각보다 작다.** 실제 결함은 넷뿐이고 전부 구체적이다.

## 1. 카탈로그 대 현실

`design-system.md`는 약 50개의 `DD*` 컴포넌트를 표로 정의한다. **실제로 존재하는 것은 6개다.**

| 존재하는 것 | 위치 |
| --- | --- |
| `DDScreenScaffold` | `ui/navigation/DDScreenScaffold.kt` |
| `DDTopAppBar` | 〃 |
| `DDBottomNavigationBar` | 〃 |
| `DDProbeQuestion` | `ui/component/ProbeComponents.kt` |
| `DDProbeProgress` | 〃 |
| `DDUriImage` | `ui/component/DDUriImage.kt` |

카탈로그의 나머지 — `DDPrimaryButton`, `DDRatingStars`, `DDEmptyContent`, `DDLoadingContent`,
`DDInfoRow`, `DDDrinkRecordListItem` 등 — 는 **하나도 없다.**

### 문서의 자기 진단조차 낡았다

문서 상단 2026-08-13 경고는 *"코드에 있으나 문서화되지 않은 것 10개"*라며
`DDSensoryMetricSlider`, `DDRatingBreakdownRadarChart`, `DDDashboardMetricTile`,
`DDDrinkTypeDonutCard`, `DDMonthlyTrendCard`, `DDPriceBracketCard`, `DDProUpgradeDialog`,
`DDProLockOverlay`, `DDRatingValueText`, `DDUriImage`를 나열한다.

**그중 9개는 지금 코드에 존재하지 않는다.** 제품 재정의 때 삭제됐다. 남은 건 `DDUriImage` 하나뿐이다.
**경고문 자체가 오답을 가르치고 있다.**

### 그 밖에 문서가 아직 틀리게 말하는 것

- §2 *"주류 종류는 와인, 위스키, 맥주 3개"* — **맥주는 폐기됐다**(`problem-definition.md` 7-1).
  §7·§10에도 맥주가 남아 있다.
- §11 화면별 매핑이 구 4화면(Dashboard/Collection/Detail/Editor) 기준이다.
  실제는 `profile`/`collection`/`record`/`settings`이며 편집·설정은 표에 아예 없다.
- §6 *"기록 일시는 필수 입력"* — 지금은 저장 시각을 자동으로 넣고 묻지 않는다.
- §12 권장 패키지(`dashboard`/`detail`/`editor`)와 실제 패키지가 다르다.

## 2. 실제 코드 결함 — 넷

### ① 같은 만족도가 화면마다 다른 형식이다 **(가장 확실한 결함)**

| 화면 | 코드 | 화면 표시 |
| --- | --- | --- |
| 컬렉션 목록 | `"%.0f".format(record.rating)` | `3` |
| 기록 상세 | `"%.0f / 5".format(record.rating)` | `3 / 5` |
| 취향 요약 | `"%.1f점".format(value.averageRating)` | `4.7점` |

**같은 5점 척도가 세 가지 얼굴을 하고 있다.** 그리고 이걸 막으려고 만든 장치가 이미 있다 —
`DrinkLabels`는 주석에 *"기록 화면과 컬렉션 화면이 같은 것을 다른 말로 부르지 않도록"*이라고
적혀 있다. **만족도만 거기 안 들어가 있다.**

### ② 간격 토큰이 있는데 아무도 안 쓴다

`DrinkDiarySpacing`(xxs 4 / xs 8 / sm 12 / md 16 / lg 24 / xl 32)이 `theme/Type.kt`에 정의돼 있고
**사용처가 0곳이다.** 모든 화면이 dp를 직접 적는다.

그 결과 화면 바깥 여백이 제각각이다: `DrinkPicker` 24, `RecordDetailStep`·`ProfileScreen`·
`SettingsScreen`·`RecordDetailScreen`·`EditRecordScreen` 20, `ProbeSequenceScreen` 16.

**그리고 가장 많이 쓰이는 20dp는 토큰 스케일에 아예 없다.** 토큰이 현실을 반영하지 못하니
아무도 쓰지 않는 것이 자연스럽다 — 코드를 토큰에 맞출 게 아니라 **토큰을 20 포함으로 고치는 쪽**이 맞다.

### ③ 차트 색이 있는데 차트가 없다

`DrinkDiaryChartColors`(wine/whiskey, 라이트/다크 4색)와 `DrinkDiaryThemeTokens.chartColors`,
`LocalDrinkDiaryChartColors`가 테마에 배선돼 있다. **정의 파일 밖 사용처는 0곳이다.**
차트가 하나도 없기 때문이다.

버릴 것은 아니다 — 벤치마킹에서 채택한 양극단 바가 처음 쓸 자리가 될 수 있다
(`../researcher/data-presentation-benchmark-2026-08.md`). **다만 지금은 죽은 코드라는 사실을
문서에 남긴다.**

### ④ 컬렉션 목록에 재구매 여부가 없다

목록 행은 이름 · 부제(주종·빈티지/음용방법·날짜) · 점수 3개만 보여준다.
**"또 살래요 / 안 맞아요"가 없다.**

제품의 목적이 *"다음에 무엇을 마실까"*인데, 목록을 훑어서는 그걸 알 수 없다.
점수 3점과 "안 맞아요"는 다른 말이고, 후자가 구매 판단에 더 가깝다.
`design-system.md` §8도 목록 아이템에 컬렉션 상태를 넣으라고 이미 적고 있다.

## 3. 결함이 **아닌** 것 — Material 3 직접 사용

코드는 `FilterChip`·`Button`·`Card`·`OutlinedTextField`·`Switch`·`AlertDialog`를 감싸지 않고
직접 쓴다. 카탈로그의 "공통 컴포넌트 재사용 우선" 원칙과는 어긋난다.

**이건 그대로 두는 것이 맞다.** `DDPrimaryButton = Button`짜리 래퍼 50개는 일관성을 만들지
않고 이름만 하나 더 만든다. 래퍼가 값을 하는 경우는 **동작이나 규칙이 들어갈 때**뿐이고,
실제로 그런 것 — `DDProbeQuestion`(3선택지 + 가운데는 '보통'), `DDProbeProgress`,
`DDScreenScaffold`(레일/바텀바 분기, 핸들러 없는 탭은 안 그림) — 은 이미 컴포넌트로 있다.

지금 저장소가 만든 진짜 공용 장치는 `DD*`가 아니라 **`DrinkLabels`**다. 문구를 한곳에 모으는 쪽이
이 앱에서는 컴포넌트를 모으는 것보다 값이 컸다. 결함 ①은 그 장치에 만족도가 빠져 있다는
얘기이지, 래퍼가 없다는 얘기가 아니다.

## 4. 제안 — F4 전에 할 것과 미룰 것

**할 것 (작고 확실함)**

1. 만족도 포맷을 `DrinkLabels`로 옮기고 세 화면을 통일한다 (결함 ①)
2. `DrinkDiarySpacing`에 20dp를 포함하도록 스케일을 고치고 화면 바깥 여백을 통일한다 (결함 ②)
3. 컬렉션 목록 행에 컬렉션 상태를 넣는다 (결함 ④)
4. `design-system.md`를 **현실 기준으로 다시 쓴다** — 없는 컴포넌트 40여 개를 표에서 내리고,
   맥주·구 화면 매핑·낡은 자기 진단을 걷어낸다

**미룰 것**

- 양극단 바 컴포넌트: 무엇을 그릴지 정한 뒤에 만든다(벤치마킹 문서 §5)
- `DD*` 래퍼 확충: 지금은 이득이 없다

**4번은 위임 후보다.** 없는 컴포넌트 목록은 기계적으로 대조 가능하고, 결과를 grep 한 번으로
검증할 수 있다. 다만 **무엇을 남기고 무엇을 버릴지는 설계 판단이라** 표 정리만 넘긴다.
