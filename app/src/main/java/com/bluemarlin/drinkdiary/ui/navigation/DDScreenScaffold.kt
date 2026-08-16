package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

enum class DDScreenType {
    TopLevel,
    Detail,
    Editor,
}

enum class DDTopLevelTab {
    Dashboard,
    Collection,
    Search,
}

// 명세 4절의 세 구간이다. 이전에는 `maxWidth >= 840.dp` 하나뿐이라 600~839dp가 통째로
// 빠져 있었고, Expanded에 와야 할 영구 드로어 대신 Rail이 왔다.
private enum class DDWindowSize {
    Compact,
    Medium,
    Expanded,
}

// 화면이 자기 가장자리 여백을 직접 정하지 않는다. 명세 4절이 브레이크포인트별로 정한 값이라
// 화면마다 따로 쓰면 창 크기가 바뀔 때 한 화면만 남는다.
val LocalDDScreenMargin = staticCompositionLocalOf { 16.dp }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDScreenScaffold(
    title: String,
    screenType: DDScreenType,
    selectedTab: DDTopLevelTab? = null,
    onDashboardClick: (() -> Unit)? = null,
    onCollectionClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    toolbarActions: @Composable RowScope.() -> Unit = {},
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

        CompositionLocalProvider(LocalDDScreenMargin provides screenMargin) {
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

                DDWindowSize.Medium ->
                    Row(modifier = Modifier.fillMaxSize()) {
                        AppNavigationRail(
                            selectedTab = selectedTab,
                            onDashboardClick = onDashboardClick,
                            onCollectionClick = onCollectionClick,
                            onSearchClick = onSearchClick,
                        )
                        scaffold(false, null)
                    }

                DDWindowSize.Expanded ->
                    PermanentNavigationDrawer(
                        drawerContent = {
                            AppNavigationDrawerSheet(
                                selectedTab = selectedTab,
                                onDashboardClick = onDashboardClick,
                                onCollectionClick = onCollectionClick,
                                onSearchClick = onSearchClick,
                            )
                        },
                    ) {
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
    onBackClick: (() -> Unit)?,
    hazeState: HazeState?,
    floatingActionButton: @Composable (() -> Unit)?,
    toolbarActions: @Composable RowScope.() -> Unit,
    snackbarHost: @Composable (() -> Unit),
    content: @Composable (PaddingValues) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    Scaffold(
        topBar = {
            DDTopAppBar(
                title = title,
                onBackClick = onBackClick,
                actions = toolbarActions,
            )
        },
        bottomBar = {
            if (showBottomBar) {
                DDBottomNavigationBar(
                    selectedTab = selectedTab,
                    onDashboardClick = onDashboardClick,
                    onCollectionClick = onCollectionClick,
                    onSearchClick = onSearchClick,
                    hazeState = hazeState,
                )
            }
        },
        floatingActionButton = floatingActionButton ?: {},
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
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .haze(
                                state = hazeState,
                                style =
                                    HazeStyle(
                                        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f),
                                        blurRadius = 32.dp,
                                    ),
                            ),
                ) {
                    content(overlayContentPadding)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBackClick != null) {
                TextButton(onClick = onBackClick) { Text(stringResource(R.string.back)) }
            }
        },
        actions = actions,
    )
}

@Composable
fun DDBottomNavigationBar(
    selectedTab: DDTopLevelTab?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSearchClick: (() -> Unit)?,
    hazeState: HazeState? = null,
) {
    val shape = MaterialTheme.shapes.large
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = DrinkDiarySpacing.md, vertical = DrinkDiarySpacing.xs)
                .height(64.dp)
                .clip(shape)
                .then(
                    if (hazeState != null) {
                        Modifier.hazeChild(
                            state = hazeState,
                            shape = shape,
                            style =
                                HazeStyle(
                                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.42f),
                                    blurRadius = 32.dp,
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
            )
        }
    }
}

@Composable
private fun AppNavigationRail(
    selectedTab: DDTopLevelTab?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSearchClick: (() -> Unit)?,
) {
    val itemColors =
        NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        )

    NavigationRail {
        onDashboardClick?.let { onClick ->
            NavigationRailItem(
                selected = selectedTab == DDTopLevelTab.Dashboard,
                onClick = onClick,
                icon = { Text(stringResource(R.string.nav_dashboard)) },
                colors = itemColors,
            )
        }
        onCollectionClick?.let { onClick ->
            NavigationRailItem(
                selected = selectedTab == DDTopLevelTab.Collection,
                onClick = onClick,
                icon = { Text(stringResource(R.string.nav_collection)) },
                colors = itemColors,
            )
        }
        onSearchClick?.let { onClick ->
            NavigationRailItem(
                selected = selectedTab == DDTopLevelTab.Search,
                onClick = onClick,
                icon = { Text(stringResource(R.string.nav_search)) },
                colors = itemColors,
            )
        }
    }
}

@Composable
private fun AppNavigationDrawerSheet(
    selectedTab: DDTopLevelTab?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSearchClick: (() -> Unit)?,
) {
    PermanentDrawerSheet {
        val itemColors =
            NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )

        onDashboardClick?.let { onClick ->
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_dashboard)) },
                selected = selectedTab == DDTopLevelTab.Dashboard,
                onClick = onClick,
                colors = itemColors,
            )
        }
        onCollectionClick?.let { onClick ->
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_collection)) },
                selected = selectedTab == DDTopLevelTab.Collection,
                onClick = onClick,
                colors = itemColors,
            )
        }
        onSearchClick?.let { onClick ->
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.nav_search)) },
                selected = selectedTab == DDTopLevelTab.Search,
                onClick = onClick,
                colors = itemColors,
            )
        }
    }
}

// 핸들러가 없는 탭은 그리지 않는다. 눌러도 아무 일이 없는 탭을 노출하면 사용자는 앱이
// 고장난 것으로 읽는다(실기기에서 확인된 결함) — 화면이 생기면 핸들러와 함께 나타난다.
//
// 아이콘 에셋이 아직 없다. 아이콘 자리에 텍스트를 넣고 라벨도 함께 두면 같은 말이 두 번
// 보인다("홈" 위에 "대시보드") — 이름을 한 번만 보여준다. 아이콘이 생기면 label을 되살린다.
@Composable
private fun RowScope.AppNavigationItems(
    selectedTab: DDTopLevelTab?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSearchClick: (() -> Unit)?,
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
            icon = { Text(stringResource(R.string.nav_dashboard)) },
            colors = itemColors,
        )
    }
    onCollectionClick?.let { onClick ->
        NavigationBarItem(
            selected = selectedTab == DDTopLevelTab.Collection,
            onClick = onClick,
            icon = { Text(stringResource(R.string.nav_collection)) },
            colors = itemColors,
        )
    }
    onSearchClick?.let { onClick ->
        NavigationBarItem(
            selected = selectedTab == DDTopLevelTab.Search,
            onClick = onClick,
            icon = { Text(stringResource(R.string.nav_search)) },
            colors = itemColors,
        )
    }
}
