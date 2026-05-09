package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    floatingActionButton: @Composable (() -> Unit)?,
    toolbarActions: @Composable RowScope.() -> Unit,
    snackbarHost: @Composable (() -> Unit),
    content: @Composable (PaddingValues) -> Unit,
) {
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
                )
            }
        },
        floatingActionButton = floatingActionButton ?: {},
        snackbarHost = snackbarHost,
        content = content,
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
                TextButton(onClick = onBackClick) { Text("뒤로") }
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
) {
    NavigationBar {
        AppNavigationItems(
            selectedTab = selectedTab,
            onDashboardClick = onDashboardClick,
            onCollectionClick = onCollectionClick,
            onSearchClick = onSearchClick,
        )
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
        NavigationRailItem(
            selected = selectedTab == DDTopLevelTab.Dashboard,
            onClick = { onDashboardClick?.invoke() },
            icon = { Text("홈") },
            label = { Text("대시보드") },
        )
        NavigationRailItem(
            selected = selectedTab == DDTopLevelTab.Collection,
            onClick = { onCollectionClick?.invoke() },
            icon = { Text("목록") },
            label = { Text("컬렉션") },
        )
        NavigationRailItem(
            selected = selectedTab == DDTopLevelTab.Search,
            onClick = { onSearchClick?.invoke() },
            icon = { Text("검색") },
            label = { Text("검색") },
        )
    }
}

@Composable
private fun RowScope.AppNavigationItems(
    selectedTab: DDTopLevelTab?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSearchClick: (() -> Unit)?,
) {
    NavigationBarItem(
        selected = selectedTab == DDTopLevelTab.Dashboard,
        onClick = { onDashboardClick?.invoke() },
        icon = { Text("홈") },
        label = { Text("대시보드") },
    )
    NavigationBarItem(
        selected = selectedTab == DDTopLevelTab.Collection,
        onClick = { onCollectionClick?.invoke() },
        icon = { Text("목록") },
        label = { Text("컬렉션") },
    )
    NavigationBarItem(
        selected = selectedTab == DDTopLevelTab.Search,
        onClick = { onSearchClick?.invoke() },
        icon = { Text("검색") },
        label = { Text("검색") },
    )
}
