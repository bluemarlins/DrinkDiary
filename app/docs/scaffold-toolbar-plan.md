# Scaffold 및 Toolbar 일관화 작업 계획

## 1. 목적

모든 주요 화면에서 동일한 `Scaffold + Toolbar` 구조를 사용하도록 UI 기반을 정리한다.

현재 `Dashboard`, `Collection`, `RecordDetail`, `RecordEditor` 화면은 `DDScreenScaffold`를 사용하고 있으나, Toolbar 구성과 화면별 내비게이션 정책이 하나의 명확한 API로 정리되어 있지 않다. 이 문서는 실제 코드 수정 전에 공통 구조, 화면별 적용 규칙, 단계별 작업 범위를 정의한다.

## 2. 현재 상태 요약

현재 화면 구조는 아래와 같다.

| 화면 | 현재 Scaffold 사용 | Toolbar | Bottom Navigation | 주요 액션 |
| --- | --- | --- | --- | --- |
| Dashboard | `DDScreenScaffold` | title 표시 | 표시 | FAB로 신규 기록 |
| Collection | `DDScreenScaffold` | title 표시 | 표시 | FAB로 신규 기록 |
| RecordDetail | `DDScreenScaffold` | title + 뒤로가기 | 숨김 | 수정, 삭제 |
| RecordEditor | `DDScreenScaffold` | title + 뒤로가기 | 숨김 | 저장, 취소 |

현재 문제점은 아래와 같다.

- `DDTopAppBar`가 Material `TopAppBar`의 `title` 슬롯 내부에서 다시 `Row`를 구성한다.
- 뒤로가기 버튼이 Toolbar의 `navigationIcon` 슬롯이 아니라 title Row 내부에 배치된다.
- Toolbar 액션 영역이 정의되어 있지 않아 수정, 삭제, 저장 같은 화면 액션을 일관되게 올리기 어렵다.
- `showBottomBar`, `selectedTab`, `onBackClick` 같은 인자가 늘어나면 화면 정책이 더 복잡해질 수 있다.
- Scaffold content padding 소비 규칙이 화면마다 반복된다.

## 3. 목표 구조

공통 화면 컨테이너는 아래 책임만 갖는다.

- Material 3 `Scaffold` 제공
- 공통 Toolbar 제공
- 화면 종류에 따른 Bottom Navigation 또는 Navigation Rail 제공
- FAB, Snackbar, Toolbar Action slot 제공
- content padding 전달

화면 Composable은 아래 책임만 갖는다.

- 화면 상태 렌더링
- 화면별 content layout 구성
- 이벤트를 ViewModel 또는 Navigation callback으로 전달

## 4. 제안 API

### 4.1 Screen Type

화면의 성격을 명시적으로 표현한다.

```kotlin
enum class DDScreenType {
    TopLevel,
    Detail,
    Editor,
}
```

의도:

- `TopLevel`: Dashboard, Collection. Bottom Navigation 또는 Navigation Rail 표시.
- `Detail`: RecordDetail. 뒤로가기 표시, Bottom Navigation 숨김.
- `Editor`: RecordEditor. 뒤로가기 표시, Bottom Navigation 숨김.

### 4.2 Scaffold State Model

`DDScreenScaffold` 호출부의 인자를 줄이기 위해 설정 모델을 둘 수 있다.

```kotlin
data class DDScreenConfig(
    val title: String,
    val screenType: DDScreenType,
    val selectedTab: DDTopLevelTab? = null,
)
```

단순성을 우선한다면 data class 없이 `screenType`만 추가하는 방식도 가능하다.

MVP에서는 API 변경 범위를 줄이기 위해 아래 형태를 우선 추천한다.

```kotlin
@Composable
fun DDScreenScaffold(
    title: String,
    screenType: DDScreenType,
    selectedTab: DDTopLevelTab? = null,
    onBackClick: (() -> Unit)? = null,
    onDashboardClick: (() -> Unit)? = null,
    onCollectionClick: (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    toolbarActions: @Composable RowScope.() -> Unit = {},
    snackbarHost: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
)
```

## 5. Toolbar 설계

Toolbar는 Material 3 `TopAppBar`의 슬롯을 그대로 사용한다.

```kotlin
TopAppBar(
    title = { Text(title) },
    navigationIcon = {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
        }
    },
    actions = toolbarActions,
)
```

적용 이유:

- Material 3 권장 구조와 맞는다.
- title, navigation, actions의 역할이 명확히 분리된다.
- 접근성 라벨을 일관되게 부여할 수 있다.
- 나중에 상세 화면 수정 버튼, 에디터 저장 버튼을 Toolbar 액션으로 옮기기 쉽다.

아이콘 의존성은 현재 프로젝트에 별도 Material Icons 의존성이 없으면 추가하지 않고 `TextButton`을 유지할 수 있다. 다만 장기적으로는 `material-icons-extended` 또는 기본 icons dependency 도입을 검토한다.

## 6. Bottom Navigation 및 Navigation Rail

현재 `maxWidth >= 840.dp` 기준으로 Navigation Rail을 사용한다. 이 정책은 유지한다.

개선 방향:

- 문자열 `"dashboard"`, `"collection"` 대신 enum 사용

```kotlin
enum class DDTopLevelTab {
    Dashboard,
    Collection,
}
```

적용 이유:

- 오타로 인한 선택 상태 오류를 줄인다.
- 화면 호출부가 더 명확해진다.

화면별 정책:

| 화면 | selectedTab | Bottom/Rail |
| --- | --- | --- |
| Dashboard | `DDTopLevelTab.Dashboard` | 표시 |
| Collection | `DDTopLevelTab.Collection` | 표시 |
| RecordDetail | null | 숨김 |
| RecordEditor | null | 숨김 |

## 7. 화면별 적용 계획

### Dashboard

- `screenType = DDScreenType.TopLevel`
- `selectedTab = DDTopLevelTab.Dashboard`
- FAB 유지
- Toolbar action 없음

### Collection

- `screenType = DDScreenType.TopLevel`
- `selectedTab = DDTopLevelTab.Collection`
- FAB 유지
- Toolbar action 없음

### RecordDetail

- `screenType = DDScreenType.Detail`
- `onBackClick` 필수
- Bottom Navigation 숨김
- 1차 구현에서는 수정/삭제 버튼을 본문에 유지
- 후속 개선에서 Toolbar action으로 수정 버튼 또는 삭제 메뉴 이동 가능

### RecordEditor

- `screenType = DDScreenType.Editor`
- `onBackClick` 필수
- Bottom Navigation 숨김
- 저장/취소 버튼은 현재 폼 하단에 유지
- 후속 개선에서 저장 버튼을 Toolbar action으로 옮길 수 있음

## 8. Insets 및 Content Padding 규칙

모든 화면은 `DDScreenScaffold`에서 전달받은 `PaddingValues`를 적용한다.

권장 패턴:

```kotlin
BoxWithConstraints(
    modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .consumeWindowInsets(padding)
        .padding(16.dp)
) {
    // screen content
}
```

리스트 화면은 장기적으로 `LazyColumn(contentPadding = ...)` 형태를 검토한다. 현재 구조에서는 Scaffold padding을 부모에서 적용하고 있으므로, 중복 padding을 만들지 않도록 한 방식만 사용한다.

IME가 있는 `RecordEditor`는 기존처럼 content container에 `imePadding()`을 유지한다.

## 9. 작업 순서

1. `DDTopLevelTab`, `DDScreenType` enum 추가
2. `DDScreenScaffold` API에 `screenType`, `selectedTab: DDTopLevelTab?`, `toolbarActions` 추가
3. `TopAppBar` 슬롯 구조 정리
   - title은 title 슬롯
   - 뒤로가기는 navigationIcon 슬롯
   - 화면 액션은 actions 슬롯
4. `DDBottomNavigationBar`, `AppNavigationRail`이 enum 기반으로 동작하도록 변경
5. Dashboard 호출부 수정
6. Collection 호출부 수정
7. RecordDetail 호출부 수정
8. RecordEditor 호출부 수정
9. 빌드 및 화면별 수동 점검

## 10. 검증 기준

- `./gradlew :app:assembleDebug` 성공
- `./gradlew :app:testDebugUnitTest` 성공
- Dashboard와 Collection에서 Bottom Navigation 선택 상태가 정상 표시됨
- 넓은 화면에서 Navigation Rail 선택 상태가 정상 표시됨
- Detail과 Editor에서 Bottom Navigation이 표시되지 않음
- Detail과 Editor에서 뒤로가기 버튼이 Toolbar 왼쪽에 표시됨
- Scaffold padding이 중복 적용되지 않음
- RecordEditor에서 키보드가 입력 필드를 가리지 않음

## 11. 비범위

이번 작업에서는 아래 항목을 포함하지 않는다.

- 화면 디자인 전체 개편
- 상세 화면 수정/삭제 액션의 Toolbar 이동
- 에디터 저장 버튼의 Toolbar 이동
- Navigation Compose route 구조 변경
- 새 아이콘 라이브러리 추가

이유: 이번 작업의 목표는 모든 화면의 공통 Scaffold/Toolbar 구조를 안정적으로 맞추는 것이며, 화면별 UX 액션 배치 변경은 별도 작업으로 분리하는 편이 안전하다.
