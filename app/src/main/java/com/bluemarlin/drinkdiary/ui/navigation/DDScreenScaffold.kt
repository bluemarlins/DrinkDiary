package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ads.DDBannerAdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDScreenScaffold(
    title: String,
    selectedTab: String? = null,
    showBottomBar: Boolean = true,
    showBannerAd: Boolean = false,
    onDashboardClick: (() -> Unit)? = null,
    onCollectionClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    snackbarHost: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val defaultSnackbarHostState = remember { SnackbarHostState() }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = showBottomBar && maxWidth >= 840.dp
        val host = snackbarHost ?: { SnackbarHost(hostState = defaultSnackbarHostState) }

        if (useNavigationRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                AppNavigationRail(
                    selectedTab = selectedTab,
                    onDashboardClick = onDashboardClick,
                    onCollectionClick = onCollectionClick,
                    onSettingsClick = onSettingsClick,
                )
                AppScaffold(
                    title = title,
                    showBottomBar = false,
                    showBannerAd = showBannerAd,
                    selectedTab = selectedTab,
                    onDashboardClick = onDashboardClick,
                    onCollectionClick = onCollectionClick,
                    onSettingsClick = onSettingsClick,
                    onBackClick = onBackClick,
                    floatingActionButton = floatingActionButton,
                    snackbarHost = host,
                    content = content,
                )
            }
        } else {
            AppScaffold(
                title = title,
                showBottomBar = showBottomBar,
                showBannerAd = showBannerAd,
                selectedTab = selectedTab,
                onDashboardClick = onDashboardClick,
                onCollectionClick = onCollectionClick,
                onSettingsClick = onSettingsClick,
                onBackClick = onBackClick,
                floatingActionButton = floatingActionButton,
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
    showBannerAd: Boolean,
    selectedTab: String?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSettingsClick: (() -> Unit)?,
    onBackClick: (() -> Unit)?,
    floatingActionButton: @Composable (() -> Unit)?,
    snackbarHost: @Composable (() -> Unit),
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { DDTopAppBar(title = title, onBackClick = onBackClick) },
            )
        },
        bottomBar = {
            Column {
                if (showBannerAd) {
                    DDBannerAdView()
                }
                if (showBottomBar) {
                    DDBottomNavigationBar(
                        selectedTab = selectedTab,
                        onDashboardClick = onDashboardClick,
                        onCollectionClick = onCollectionClick,
                        onSettingsClick = onSettingsClick,
                    )
                }
            }
        },
        floatingActionButton = floatingActionButton ?: {},
        snackbarHost = snackbarHost,
        content = content,
    )
}

@Composable
fun DDTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
) {
    Row {
        if (onBackClick != null) {
            TextButton(
                onClick = onBackClick,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
            ) { Text("뒤로") }
        }
        Text(title)
    }
}

@Composable
fun DDBottomNavigationBar(
    selectedTab: String?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSettingsClick: (() -> Unit)? = null,
) {
    NavigationBar {
        AppNavigationItems(
            selectedTab = selectedTab,
            onDashboardClick = onDashboardClick,
            onCollectionClick = onCollectionClick,
            onSettingsClick = onSettingsClick,
        )
    }
}

@Composable
private fun AppNavigationRail(
    selectedTab: String?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSettingsClick: (() -> Unit)?,
) {
    NavigationRail {
        NavigationRailItem(
            selected = selectedTab == "dashboard",
            onClick = { onDashboardClick?.invoke() },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("대시보드") },
        )
        NavigationRailItem(
            selected = selectedTab == "collection",
            onClick = { onCollectionClick?.invoke() },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text("컬렉션") },
        )
        NavigationRailItem(
            selected = selectedTab == "settings",
            onClick = { onSettingsClick?.invoke() },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("설정") },
        )
    }
}

@Composable
private fun RowScope.AppNavigationItems(
    selectedTab: String?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSettingsClick: (() -> Unit)?,
) {
    NavigationBarItem(
        selected = selectedTab == "dashboard",
        onClick = { onDashboardClick?.invoke() },
        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
        label = { Text("대시보드") },
    )
    NavigationBarItem(
        selected = selectedTab == "collection",
        onClick = { onCollectionClick?.invoke() },
        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
        label = { Text("컬렉션") },
    )
    NavigationBarItem(
        selected = selectedTab == "settings",
        onClick = { onSettingsClick?.invoke() },
        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
        label = { Text("설정") },
    )
}
