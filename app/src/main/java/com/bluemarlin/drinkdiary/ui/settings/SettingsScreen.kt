package com.bluemarlin.drinkdiary.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.component.DDFormSection
import com.bluemarlin.drinkdiary.ui.component.DDThemeModeSelector
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onDashboardClick: () -> Unit,
    onCollectionClick: () -> Unit,
) {
    val themeMode by viewModel.themeMode.collectAsState()

    DDScreenScaffold(
        title = "설정",
        selectedTab = "settings",
        onDashboardClick = onDashboardClick,
        onCollectionClick = onCollectionClick,
        onSettingsClick = {},
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            DDFormSection("테마") {
                DDThemeModeSelector(selected = themeMode, onSelected = viewModel::selectThemeMode)
            }
        }
    }
}
