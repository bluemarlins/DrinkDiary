package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.ui.component.DDIconButton
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

enum class DDScreenType {
    TopLevel,
    Detail,
    Editor,
}

// 하단 내비게이션의 목적지다. **선언 순서가 곧 화면에 그려지는 순서**이므로 여기를 바꾸면
// 탭 위치가 바뀐다(2026-08-19 사용자 확정: 대시보드 / 찾기 / 컬렉션 / 설정).
enum class DDTopLevelTab {
    Dashboard,
    Search,
    Collection,
    Settings,
}

// 명세 4절의 세 구간이다. 이전에는 `maxWidth >= 840.dp` 하나뿐이라 600~839dp가 통째로
// 빠져 있었고, Expanded에 와야 할 영구 드로어 대신 Rail이 왔다.
enum class DDWindowSize {
    Compact,
    Medium,
    Expanded,
}

// 화면이 자기 가장자리 여백을 직접 정하지 않는다. 명세 4절이 브레이크포인트별로 정한 값이라
// 화면마다 따로 쓰면 창 크기가 바뀔 때 한 화면만 남는다.
val LocalDDScreenMargin = staticCompositionLocalOf { 16.dp }

// 하단 내비 바가 차지하는 높이(막대 64dp + 위아래 여백 8dp씩). Compact에서 `AppScaffold`는
// 내용이 반투명 바 밑으로 흐르도록 bottom 인셋을 0으로 넘기므로, 바 위에 무언가를 얹으려면
// 이 값을 직접 비켜야 한다. 바와 소비자가 같은 상수를 보게 해서 둘이 갈라지지 않게 한다.
val DDBottomNavigationBarHeight = 80.dp

// 상단 플로팅 바가 차지하는 높이(막대 56dp + 위아래 여백 8dp씩). 상태바 인셋은 여기 포함하지
// 않는다 — 그건 기기마다 다르고 `statusBarsPadding()`이 따로 준다.
//
// **하단과 같은 규칙이다.** 콘텐츠는 이 바 뒤로 흐르고, 화면은 스크롤 컨테이너의
// `contentPadding`으로 이만큼을 비운다. 바깥 `Modifier.padding`으로 비우면 흐르지 않아서
// 블러가 비출 것이 없어진다 — 플로팅이 아니라 그냥 떠 있는 불투명 막대가 된다.
val DDTopAppBarHeight = 72.dp

// 하단 플로팅 바의 폭. **화면 폭의 60%**를 기본으로 하되 dp로 위아래를 막는다
// (2026-08-19 사용자 확정).
//
// - **최소 280dp**: 탭 4개 × 70dp다. "대시보드"는 한글 4글자라 항목당 70dp 아래로 내려가면
//   레이블이 줄거나 잘린다. 320dp짜리 최협 화면에서도 좌우 마진 32dp를 빼고 들어간다.
// - **최대 480dp**: 탭 4개 × 120dp. 그 이상 넓히면 아이템 사이가 벌어져 바가 '빈 슬래브'로
//   읽히고, 엄지가 닿지 않는 가장자리에 탭이 놓인다.
val DDBottomBarWidthFraction = 0.6f
val DDBottomBarMinWidth = 280.dp
val DDBottomBarMaxWidth = 480.dp

// T4에서는 읽는 곳이 없어 지웠던 값이다. 명세 4절 마지막 열(화면별 적응형 레이아웃)을 구현하면서
// 화면이 자기 구간을 알아야 할 이유가 생겨 되살린다.
val LocalDDWindowSize = staticCompositionLocalOf { DDWindowSize.Compact }

val LocalHazeState = compositionLocalOf<HazeState?> { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDScreenScaffold(
    title: String,
    screenType: DDScreenType = DDScreenType.TopLevel,
    selectedTab: DDTopLevelTab? = null,
    onDashboardClick: (() -> Unit)? = null,
    onCollectionClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    toolbarActions: (@Composable RowScope.() -> Unit)? = null,
    snackbarHost: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val defaultSnackbarHostState = remember { SnackbarHostState() }
    val hazeState = remember { HazeState() }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowSize =
            when {
                maxWidth < 600.dp -> DDWindowSize.Compact
                maxWidth < 840.dp -> DDWindowSize.Medium
                else -> DDWindowSize.Expanded
            }
        val screenMargin =
            when (windowSize) {
                DDWindowSize.Compact -> DrinkDiarySpacing.md
                DDWindowSize.Medium -> DrinkDiarySpacing.xl
                DDWindowSize.Expanded -> DrinkDiarySpacing.xxl
            }

        CompositionLocalProvider(
            LocalDDWindowSize provides windowSize,
            LocalDDScreenMargin provides screenMargin,
            LocalHazeState provides
                (if (screenType == DDScreenType.TopLevel && windowSize == DDWindowSize.Compact) hazeState else null),
        ) {
            val host = snackbarHost ?: { SnackbarHost(hostState = defaultSnackbarHostState) }
            val scaffold: @Composable (Boolean, HazeState?) -> Unit = { showBottomBar, haze ->
                AppScaffold(
                    title = title,
                    showBottomBar = showBottomBar,
                    constrainContentWidth = windowSize == DDWindowSize.Expanded,
                    selectedTab = selectedTab,
                    onDashboardClick = onDashboardClick,
                    onCollectionClick = onCollectionClick,
                    onSearchClick = onSearchClick,
                    onSettingsClick = onSettingsClick,
                    onBackClick = onBackClick,
                    hazeState = haze,
                    floatingActionButton = floatingActionButton,
                    toolbarActions = toolbarActions,
                    snackbarHost = host,
                    content = content,
                )
            }

            if (screenType != DDScreenType.TopLevel) {
                scaffold(false, null)
                return@CompositionLocalProvider
            }

            when (windowSize) {
                DDWindowSize.Compact -> scaffold(true, hazeState)

                // **Medium과 Expanded가 같은 레일을 쓴다**(2026-08-20 사용자 확정).
                //
                // 이전에는 Expanded에서 `PermanentNavigationDrawer`를 썼는데, M3 드로어 시트의
                // 기본 폭이 360dp다. 876dp짜리 가로 화면에서 **왼쪽이 41%를 먹고** 본문이
                // 516dp만 남았다 — 태블릿에서 더 보여 주려고 넓힌 화면을 목적지 이름 넉 줄로
                // 되돌려준 셈이다.
                //
                // 레일은 80dp다. **화면 폭에 비례시키지 않는다** — 아이콘 하나의 터치 타깃은
                // 화면이 넓어진다고 커질 이유가 없고, 비례시키면 넓은 화면일수록 더 낭비한다.
                DDWindowSize.Medium, DDWindowSize.Expanded ->
                    Row(modifier = Modifier.fillMaxSize()) {
                        AppNavigationRail(
                            selectedTab = selectedTab,
                            onDashboardClick = onDashboardClick,
                            onCollectionClick = onCollectionClick,
                            onSearchClick = onSearchClick,
                            onSettingsClick = onSettingsClick,
                            header = floatingActionButton,
                        )
                        scaffold(false, null)
                    }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    title: String,
    showBottomBar: Boolean,
    constrainContentWidth: Boolean,
    selectedTab: DDTopLevelTab?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSearchClick: (() -> Unit)?,
    onSettingsClick: (() -> Unit)?,
    onBackClick: (() -> Unit)?,
    hazeState: HazeState?,
    floatingActionButton: @Composable (() -> Unit)?,
    toolbarActions: (@Composable RowScope.() -> Unit)?,
    snackbarHost: @Composable (() -> Unit),
    content: @Composable (PaddingValues) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    // **앱바가 언제 떠야 하는지는 콘텐츠가 정한다.** 스크롤이 0이면 겹칠 것이 없으므로 알약도
    // 필요 없다 — 그때는 평범한 도킹 앱바다. 콘텐츠가 위로 올라와 앱바 영역과 겹치기 시작하는
    // 그 순간부터 알약이 자라난다.
    //
    // 스크롤 상태는 화면마다 다르고(Column·LazyColumn) 스캐폴드는 그걸 알 수 없다. 그래서
    // 자식들이 흘려보내는 스크롤 델타를 `NestedScrollConnection`으로 주워 담는다 —
    // 화면 코드를 하나도 고치지 않고도 네 화면 모두에서 동작한다.
    var scrolledPx by remember { mutableFloatStateOf(0f) }
    val overlapConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    scrolledPx = (scrolledPx - available.y).coerceAtLeast(0f)
                    return Offset.Zero
                }
            }
        }
    // **탭을 옮기면 누적을 지운다.** 안 지우면 대시보드에서 내려 읽다가 설정으로 가도 알약이
    // 뜬 채로 남는다 — 그 화면은 아직 겹친 적이 없는데도 겹친 모양을 하고 있게 된다.
    LaunchedEffect(selectedTab) { scrolledPx = 0f }

    // 손가락 떨림으로 알약이 깜박이지 않게 하는 최소 문턱이다.
    val overlapped = scrolledPx > with(LocalDensity.current) { 4.dp.toPx() }

    Scaffold(
        topBar = {
            // 플로팅은 **콘텐츠가 뒤로 흐르는 자리에서만** 성립한다. `hazeState`가 있는 구간이
            // 정확히 그 자리다(Compact + 최상위). 상세·편집처럼 흐르지 않는 화면에 알약을 얹으면
            // 블러가 비출 것이 없어서 그냥 떠 있는 불투명 막대가 된다.
            if (hazeState != null) {
                DDFloatingTopAppBar(
                    title = title,
                    onBackClick = onBackClick,
                    actions = toolbarActions,
                    hazeState = hazeState,
                    floating = overlapped,
                )
            } else {
                DDTopAppBar(
                    title = title,
                    onBackClick = onBackClick,
                    actions = toolbarActions,
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                DDBottomNavigationBar(
                    selectedTab = selectedTab,
                    onDashboardClick = onDashboardClick,
                    onCollectionClick = onCollectionClick,
                    onSearchClick = onSearchClick,
                    onSettingsClick = onSettingsClick,
                    floatingActionButton = floatingActionButton,
                    hazeState = hazeState,
                )
            }
        },
        floatingActionButton = {
            if (!showBottomBar) {
                floatingActionButton?.invoke()
            }
        },
        snackbarHost = snackbarHost,
        content = { padding ->
            if (hazeState != null) {
                val overlayContentPadding =
                    PaddingValues(
                        start = padding.calculateStartPadding(layoutDirection),
                        top = padding.calculateTopPadding(),
                        end = padding.calculateEndPadding(layoutDirection),
                        bottom = 0.dp,
                    )
                val topFade = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                val bottomFade = DrinkDiarySpacing.xl
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .nestedScroll(overlapConnection)
                            .hazeSource(state = hazeState),
                ) {
                    // **페이딩 엣지.** 콘텐츠가 화면 끝에서 뚝 잘리는 대신 알파로 사라진다.
                    // 잘린 글자 반 줄은 "여기가 끝"이 아니라 "덜 그려졌다"로 읽힌다.
                    //
                    // 페이드 범위는 **바가 아니라 화면 가장자리**에 맞춘다. 바 뒤까지 지워 버리면
                    // 블러가 비출 것이 없어져 플로팅 자체가 죽는다 — 위는 상태바 높이만큼,
                    // 아래는 24dp만 녹인다.
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                .drawWithContent {
                                    drawContent()
                                    val top = (topFade.toPx() / size.height).coerceIn(0f, 0.5f)
                                    val bottom = (bottomFade.toPx() / size.height).coerceIn(0f, 0.5f)
                                    drawRect(
                                        brush =
                                            Brush.verticalGradient(
                                                0f to Color.Transparent,
                                                top to Color.Black,
                                                (1f - bottom) to Color.Black,
                                                1f to Color.Transparent,
                                            ),
                                        blendMode = BlendMode.DstIn,
                                    )
                                },
                    ) {
                        content(overlayContentPadding)
                    }
                }
            } else if (constrainContentWidth) {
                // 명세 4절: Expanded에서 콘텐츠 최대폭 720dp. 태블릿에서 한 줄이 화면 끝까지
                // 늘어나면 눈이 줄 끝에서 다음 줄 머리를 못 찾는다.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Box(modifier = Modifier.widthIn(max = 720.dp).fillMaxSize()) {
                        content(padding)
                    }
                }
            } else {
                content(padding)
            }
        },
    )
}

// 상단 플로팅 알약 바. **하단 바와 같은 규격을 쓴다** — 좌우 `screenMargin`, `ShapeLarge` 18dp,
// 같은 haze 스타일, 1dp `outlineVariant` 테두리. 둘이 다른 반경이나 다른 틴트를 쓰면 한 화면에
// 다른 시스템이 둘 있는 것으로 보인다.
//
// **틴트가 하단보다 진하다(0.42 → 0.58).** 상단은 스크롤이 시작되는 쪽이라 사진의 밝은 윗부분이
// 지나갈 확률이 높고, 여기 얹히는 것은 아이콘이 아니라 **타이틀 텍스트**다. 텍스트는 아이콘보다
// 대비가 조금만 흔들려도 먼저 읽히지 않는다(명세 2절 AAA 7:1).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDFloatingTopAppBar(
    title: String,
    onBackClick: (() -> Unit)?,
    actions: (@Composable RowScope.() -> Unit)?,
    hazeState: HazeState?,
    floating: Boolean,
) {
    // 0 = 도킹(평범한 앱바), 1 = 알약이 완전히 자란 상태. 사이 값은 모핑 중이다.
    val t by animateFloatAsState(
        targetValue = if (floating) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "topBarMorph",
    )
    val shape = MaterialTheme.shapes.large

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = LocalDDScreenMargin.current, vertical = DrinkDiarySpacing.xs)
                .height(DDTopAppBarHeight - DrinkDiarySpacing.xs * 2),
        contentAlignment = Alignment.CenterStart,
    ) {
        // **알약은 화면 폭이 아니라 제 내용만큼만 차지한다**(`wrapContentWidth`).
        // 플로팅의 기조가 콘텐츠를 최대한 보여 주는 것이라, 바가 가리는 면적은 글자가 차지하는
        // 만큼이 상한이다. 전폭 알약은 도킹 바를 모서리만 깎아 놓은 것과 다르지 않다.
        Row(
            modifier =
                Modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .clip(shape)
                    .then(
                        // 도킹 상태(t=0)에서는 크롬을 아예 그리지 않는다 — 배경도 테두리도 없는
                        // 맨 타이틀이라 그게 곧 '겹치지 않은 상태'의 시각적 뜻이다.
                        if (hazeState != null && t > 0.01f) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style =
                                    HazeStyle(
                                        backgroundColor = MaterialTheme.colorScheme.surface,
                                        tint = HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.58f * t)),
                                        // 56dp 알약 높이에 맞춰 블러 반경을 20dp(높이의 약 35%)로 최적화한다.
                                        // 32dp는 요소 높이의 절반을 넘어 배경 맥락이 단색처럼 뭉개졌고,
                                        // 20dp에서 품격 있는 유리 질감과 텍스트 가독성, GPU 효율을 동시에 얻는다.
                                        // 블러도 함께 자란다. 0에서 시작해야 알약이 '켜지는' 대신
                                        // '맺히는' 것으로 보인다.
                                        blurRadius = 20.dp * t,
                                        // 에디토리얼 저널 무드의 미세한 질감을 더하고 사진 위 컬러 밴딩을 방지한다.
                                        noiseFactor = 0.08f * t,
                                    ),
                            )
                        } else {
                            Modifier
                        },
                    ).border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = t),
                        shape = shape,
                    )
                    // 안쪽 여백도 함께 자란다. t=0일 때 0이라 타이틀이 도킹 앱바와 **같은 자리**에
                    // 서고, 알약은 글자를 밀어내지 않고 글자 주위로 부풀어 오른다.
                    .padding(horizontal = DrinkDiarySpacing.sm * t),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBackClick != null) {
                DDIconButton(
                    onClick = onBackClick,
                    contentDescription = stringResource(R.string.back),
                ) {
                    Icon(painter = painterResource(R.drawable.ic_back), contentDescription = null)
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // **더보기는 타이틀 알약에서 떼어 오른쪽 끝에 따로 둔다**(2026-08-20 사용자 확정).
        // 한 알약에 같이 담으면 알약이 아이콘만큼 더 길어지고, 그만큼 콘텐츠를 더 가린다.
        //
        // **모양이 다르다 — 이쪽은 원이다.** 타이틀은 가로로 긴 글자라 알약이 맞고, 아이콘은
        // 사방이 같은 크기라 원이 맞다. 같은 `ShapeLarge`를 주면 정사각에 모서리만 깎인 모양이
        // 되어 알약도 원도 아니게 된다.
        if (actions != null) {
            Row(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        // 아이콘 하나면 정사각이라 `CircleShape`가 정확히 원이 된다.
                        // 둘 이상이면 자연히 알약으로 늘어난다 — 깨지지 않고 물러난다.
                        .defaultMinSize(minWidth = DDTopAppBarHeight - DrinkDiarySpacing.xs * 2)
                        .clip(CircleShape)
                        .then(
                            if (hazeState != null && t > 0.01f) {
                                Modifier.hazeEffect(
                                    state = hazeState,
                                    style =
                                        HazeStyle(
                                            backgroundColor = MaterialTheme.colorScheme.surface,
                                            tint = HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.58f * t)),
                                            blurRadius = 20.dp * t,
                                            noiseFactor = 0.08f * t,
                                        ),
                                )
                            } else {
                                Modifier
                            },
                        ).border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = t),
                            shape = CircleShape,
                        ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBackClick != null) {
                DDIconButton(
                    onClick = onBackClick,
                    contentDescription = stringResource(R.string.back),
                ) {
                    // DDIconButton이 semantics로 설명을 걸므로 여기서 또 걸지 않는다 —
                    // 스크린 리더가 같은 말을 두 번 읽는다.
                    Icon(painter = painterResource(R.drawable.ic_back), contentDescription = null)
                }
            }
        },
        actions = { actions?.invoke(this) },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
    )
}

@Composable
fun DDBottomNavigationBar(
    selectedTab: DDTopLevelTab?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSearchClick: (() -> Unit)?,
    onSettingsClick: (() -> Unit)?,
    floatingActionButton: @Composable (() -> Unit)? = null,
    hazeState: HazeState? = null,
) {
    val shape = MaterialTheme.shapes.large
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = DrinkDiarySpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        // FAB가 있으면 FAB 크기(56dp) + 간격(8dp)만큼 바가 차지할 가용 폭에서 제외한다.
        val fabSpacing = if (floatingActionButton != null) DrinkDiarySpacing.sm else 0.dp
        val fabSize =
            if (floatingActionButton !=
                null
            ) {
                (DDBottomNavigationBarHeight - DrinkDiarySpacing.xs * 2)
            } else {
                0.dp
            }
        val totalFabOccupied = if (floatingActionButton != null) (fabSize + fabSpacing) else 0.dp

        val available = maxWidth - LocalDDScreenMargin.current * 2 - totalFabOccupied
        val barWidth =
            (maxWidth * DDBottomBarWidthFraction)
                .coerceIn(DDBottomBarMinWidth, DDBottomBarMaxWidth)
                .coerceAtMost(available)

        Row(
            modifier = Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(barWidth)
                        .height(DDBottomNavigationBarHeight - DrinkDiarySpacing.xs * 2)
                        .clip(shape)
                        .then(
                            if (hazeState != null) {
                                Modifier.hazeEffect(
                                    state = hazeState,
                                    style =
                                        HazeStyle(
                                            backgroundColor = MaterialTheme.colorScheme.surface,
                                            tint = HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.42f)),
                                            // 64dp 바 높이에 맞춰 블러 반경을 24dp(높이의 약 37.5%)로 최적화한다.
                                            // 32dp는 요소 높이의 절반에 달해 배경의 맥락을 지나치게 뭉개고,
                                            // 24dp에서 아이콘 가독성을 유지하면서도 뒤 콘텐츠의 형태감을 자연스럽게 투영한다.
                                            blurRadius = 24.dp,
                                            // 에디토리얼 저널 무드의 미세한 질감을 더하고 사진 위 컬러 밴딩을 방지한다.
                                            noiseFactor = 0.08f,
                                        ),
                                )
                            } else {
                                Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f), shape)
                            },
                        ).border(
                            // 흰색 그라데이션 테두리를 걷어냈다. 하드코딩 색이라 테마에 반응하지 않아
                            // 다크에서 흰 테두리가 그대로 빛났고(명세 2-6 "빛나는 네온 테두리 금지"),
                            // 명세 2-1의 "임의 Hex 하드코딩 금지"에도 걸렸다.
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = shape,
                        ),
            ) {
                NavigationBar(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                ) {
                    AppNavigationItems(
                        selectedTab = selectedTab,
                        onDashboardClick = onDashboardClick,
                        onCollectionClick = onCollectionClick,
                        onSearchClick = onSearchClick,
                        onSettingsClick = onSettingsClick,
                    )
                }
            }

            if (floatingActionButton != null) {
                floatingActionButton()
            }
        }
    }
}

@Composable
private fun AppNavigationRail(
    selectedTab: DDTopLevelTab?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSearchClick: (() -> Unit)?,
    onSettingsClick: (() -> Unit)?,
    header: (@Composable () -> Unit)? = null,
) {
    val itemColors =
        NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        )

    NavigationRail(
        header =
            if (header != null) {
                { header() }
            } else {
                null
            },
    ) {
        onDashboardClick?.let { onClick ->
            NavigationRailItem(
                selected = selectedTab == DDTopLevelTab.Dashboard,
                onClick = onClick,
                icon = { Icon(painter = painterResource(R.drawable.ic_nav_dashboard), contentDescription = null) },
                label = { Text(stringResource(R.string.nav_dashboard)) },
                colors = itemColors,
            )
        }
        onSearchClick?.let { onClick ->
            NavigationRailItem(
                selected = selectedTab == DDTopLevelTab.Search,
                onClick = onClick,
                icon = { Icon(painter = painterResource(R.drawable.ic_nav_search), contentDescription = null) },
                label = { Text(stringResource(R.string.nav_search)) },
                colors = itemColors,
            )
        }
        onCollectionClick?.let { onClick ->
            NavigationRailItem(
                selected = selectedTab == DDTopLevelTab.Collection,
                onClick = onClick,
                icon = { Icon(painter = painterResource(R.drawable.ic_nav_collection), contentDescription = null) },
                label = { Text(stringResource(R.string.nav_collection)) },
                colors = itemColors,
            )
        }

        onSettingsClick?.let { onClick ->
            NavigationRailItem(
                selected = selectedTab == DDTopLevelTab.Settings,
                onClick = onClick,
                icon = { Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = null) },
                label = { Text(stringResource(R.string.nav_settings)) },
                colors = itemColors,
            )
        }
    }
}

// 핸들러가 없는 탭은 그리지 않는다. 눌러도 아무 일이 없는 탭을 노출하면 사용자는 앱이
// 고장난 것으로 읽는다(실기기에서 확인된 결함) — 화면이 생기면 핸들러와 함께 나타난다.
@Composable
private fun RowScope.AppNavigationItems(
    selectedTab: DDTopLevelTab?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSearchClick: (() -> Unit)?,
    onSettingsClick: (() -> Unit)?,
) {
    // 색을 명시하지 않으면 M3 기본값이 명세를 이긴다 — 선택 표시가 `secondaryContainer`,
    // 즉 우리 매핑에서는 위스키 앰버로 칠해진다. 명세 3.1절은 선택 상태를 `PrimaryContainer`로 정한다.
    val itemColors =
        NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        )

    onDashboardClick?.let { onClick ->
        NavigationBarItem(
            selected = selectedTab == DDTopLevelTab.Dashboard,
            onClick = onClick,
            icon = { Icon(painter = painterResource(R.drawable.ic_nav_dashboard), contentDescription = null) },
            label = { Text(stringResource(R.string.nav_dashboard)) },
            colors = itemColors,
        )
    }
    onSearchClick?.let { onClick ->
        NavigationBarItem(
            selected = selectedTab == DDTopLevelTab.Search,
            onClick = onClick,
            icon = { Icon(painter = painterResource(R.drawable.ic_nav_search), contentDescription = null) },
            label = { Text(stringResource(R.string.nav_search)) },
            colors = itemColors,
        )
    }
    onCollectionClick?.let { onClick ->
        NavigationBarItem(
            selected = selectedTab == DDTopLevelTab.Collection,
            onClick = onClick,
            icon = { Icon(painter = painterResource(R.drawable.ic_nav_collection), contentDescription = null) },
            label = { Text(stringResource(R.string.nav_collection)) },
            colors = itemColors,
        )
    }

    onSettingsClick?.let { onClick ->
        NavigationBarItem(
            selected = selectedTab == DDTopLevelTab.Settings,
            onClick = onClick,
            icon = { Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = null) },
            label = { Text(stringResource(R.string.nav_settings)) },
            colors = itemColors,
        )
    }
}
