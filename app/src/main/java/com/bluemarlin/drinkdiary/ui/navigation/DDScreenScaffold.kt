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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
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
        val showTopLevelNavigation = screenType == DDScreenType.TopLevel
        val useNavigationRail = showTopLevelNavigation && maxWidth >= 840.dp
        val host = snackbarHost ?: { SnackbarHost(hostState = defaultSnackbarHostState) }

        if (useNavigationRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                AppNavigationRail(
                    selectedTab = selectedTab,
                    onDashboardClick = onDashboardClick,
                    onCollectionClick = onCollectionClick,
                    onSearchClick = onSearchClick,
                )
                AppScaffold(
                    title = title,
                    showBottomBar = false,
                    selectedTab = selectedTab,
                    onDashboardClick = onDashboardClick,
                    onCollectionClick = onCollectionClick,
                    onSearchClick = onSearchClick,
                    onBackClick = onBackClick,
                    hazeState = null,
                    floatingActionButton = floatingActionButton,
                    toolbarActions = toolbarActions,
                    snackbarHost = host,
                    content = content,
                )
            }
        } else {
            AppScaffold(
                title = title,
                showBottomBar = showTopLevelNavigation,
                selectedTab = selectedTab,
                onDashboardClick = onDashboardClick,
                onCollectionClick = onCollectionClick,
                onSearchClick = onSearchClick,
                onBackClick = onBackClick,
                hazeState = if (showTopLevelNavigation) hazeState else null,
                floatingActionButton = floatingActionButton,
                toolbarActions = toolbarActions,
                snackbarHost = host,
                content = content,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    title: String,
    showBottomBar: Boolean,
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
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp)
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
                    width = Dp.Hairline,
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.White.copy(alpha = 0.58f),
                                    Color.White.copy(alpha = 0.12f),
                                ),
                        ),
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
    NavigationRail {
        onDashboardClick?.let { onClick ->
            NavigationRailItem(
                selected = selectedTab == DDTopLevelTab.Dashboard,
                onClick = onClick,
                icon = { Text(stringResource(R.string.nav_dashboard)) },
            )
        }
        onCollectionClick?.let { onClick ->
            NavigationRailItem(
                selected = selectedTab == DDTopLevelTab.Collection,
                onClick = onClick,
                icon = { Text(stringResource(R.string.nav_collection)) },
            )
        }
        onSearchClick?.let { onClick ->
            NavigationRailItem(
                selected = selectedTab == DDTopLevelTab.Search,
                onClick = onClick,
                icon = { Text(stringResource(R.string.nav_search)) },
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
    onDashboardClick?.let { onClick ->
        NavigationBarItem(
            selected = selectedTab == DDTopLevelTab.Dashboard,
            onClick = onClick,
            icon = { Text(stringResource(R.string.nav_dashboard)) },
        )
    }
    onCollectionClick?.let { onClick ->
        NavigationBarItem(
            selected = selectedTab == DDTopLevelTab.Collection,
            onClick = onClick,
            icon = { Text(stringResource(R.string.nav_collection)) },
        )
    }
    onSearchClick?.let { onClick ->
        NavigationBarItem(
            selected = selectedTab == DDTopLevelTab.Search,
            onClick = onClick,
            icon = { Text(stringResource(R.string.nav_search)) },
        )
    }
}
