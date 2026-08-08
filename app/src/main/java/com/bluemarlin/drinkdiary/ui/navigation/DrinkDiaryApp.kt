package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bluemarlin.drinkdiary.DrinkDiaryApplication
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.ui.collection.CollectionRoute
import com.bluemarlin.drinkdiary.ui.collection.CollectionViewModel
import com.bluemarlin.drinkdiary.ui.dashboard.DashboardRoute
import com.bluemarlin.drinkdiary.ui.dashboard.DashboardViewModel
import com.bluemarlin.drinkdiary.ui.detail.RecordDetailRoute
import com.bluemarlin.drinkdiary.ui.detail.RecordDetailViewModel
import com.bluemarlin.drinkdiary.ui.editor.RecordEditorRoute
import com.bluemarlin.drinkdiary.ui.editor.RecordEditorViewModel
import com.bluemarlin.drinkdiary.ui.settings.SettingsRoute
import com.bluemarlin.drinkdiary.ui.settings.SettingsViewModel

private object Routes {
    const val Dashboard = "dashboard"
    const val Collection = "collection"
    const val CollectionWithStatus = "collection/{status}"
    const val Detail = "detail/{recordId}"
    const val EditorNew = "editor/new"
    const val EditorEdit = "editor/{recordId}"
    const val Settings = "settings"
}

@Composable
fun DrinkDiaryApp() {
    val navController = rememberNavController()
    val appContainer = (LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer

    NavHost(navController = navController, startDestination = Routes.Dashboard) {
        composable(Routes.Dashboard) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(
                    appContainer.observeDashboardSummaryUseCase,
                    appContainer.observeMonthRecordDatesUseCase,
                    appContainer.observeWeeklyTrendUseCase,
                ),
            )
            DashboardRoute(
                viewModel = viewModel,
                onAddRecord = { navController.navigate(Routes.EditorNew) },
                onOpenRecord = { navController.navigate("detail/$it") },
                onOpenStatus = { navController.navigate("collection/${it.name}") },
                onCollectionClick = { navController.navigate(Routes.Collection) },
                onSettingsClick = { navController.navigate(Routes.Settings) },
            )
        }
        composable(Routes.Collection) {
            CollectionEntry(
                initialStatus = null,
                onDashboardClick = { navController.navigate(Routes.Dashboard) },
                onAddRecord = { navController.navigate(Routes.EditorNew) },
                onOpenRecord = { navController.navigate("detail/$it") },
                onSettingsClick = { navController.navigate(Routes.Settings) },
            )
        }
        composable(
            route = Routes.CollectionWithStatus,
            arguments = listOf(navArgument("status") { type = NavType.StringType }),
        ) { entry ->
            val status = entry.arguments?.getString("status")?.let(CollectionStatus::fromStorageValue)
            CollectionEntry(
                initialStatus = status,
                onDashboardClick = { navController.navigate(Routes.Dashboard) },
                onAddRecord = { navController.navigate(Routes.EditorNew) },
                onOpenRecord = { navController.navigate("detail/$it") },
                onSettingsClick = { navController.navigate(Routes.Settings) },
            )
        }
        composable(Routes.Settings) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(
                    appContainer.observeThemeModeUseCase,
                    appContainer.setThemeModeUseCase,
                ),
            )
            SettingsRoute(
                viewModel = viewModel,
                onDashboardClick = { navController.navigate(Routes.Dashboard) },
                onCollectionClick = { navController.navigate(Routes.Collection) },
            )
        }
        composable(
            route = Routes.Detail,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType }),
        ) { entry ->
            val recordId = entry.arguments?.getLong("recordId") ?: return@composable
            val viewModel: RecordDetailViewModel = viewModel(
                key = "detail_$recordId",
                factory = RecordDetailViewModel.Factory(
                    recordId,
                    appContainer.observeDrinkRecordUseCase,
                    appContainer.deleteDrinkRecordUseCase,
                ),
            )
            RecordDetailRoute(
                recordId = recordId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate("editor/$it") },
            )
        }
        composable(Routes.EditorNew) {
            EditorEntry(
                recordId = null,
                onBack = { navController.popBackStack() },
                onSaved = { recordId ->
                    navController.navigate("detail/$recordId") {
                        popUpTo(Routes.EditorNew) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.EditorEdit,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType }),
        ) { entry ->
            val recordId = entry.arguments?.getLong("recordId") ?: return@composable
            EditorEntry(
                recordId = recordId,
                onBack = { navController.popBackStack() },
                onSaved = { savedId ->
                    navController.navigate("detail/$savedId") {
                        popUpTo("editor/$recordId") { inclusive = true }
                    }
                },
            )
        }
    }
}

@Composable
private fun CollectionEntry(
    initialStatus: CollectionStatus?,
    onDashboardClick: () -> Unit,
    onAddRecord: () -> Unit,
    onOpenRecord: (Long) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val appContainer = (LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer
    val viewModel: CollectionViewModel = viewModel(
        key = "collection_${initialStatus?.name ?: "all"}",
        factory = CollectionViewModel.Factory(appContainer.observeDrinkRecordsUseCase, initialStatus),
    )
    CollectionRoute(
        viewModel = viewModel,
        onAddRecord = onAddRecord,
        onOpenRecord = onOpenRecord,
        onDashboardClick = onDashboardClick,
        onSettingsClick = onSettingsClick,
    )
}

@Composable
private fun EditorEntry(
    recordId: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    val appContainer = (LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer
    val viewModel: RecordEditorViewModel = viewModel(
        key = "editor_${recordId ?: "new"}",
        factory = RecordEditorViewModel.Factory(
            recordId,
            appContainer.observeDrinkRecordUseCase,
            appContainer.saveDrinkRecordUseCase,
        ),
    )
    RecordEditorRoute(viewModel = viewModel, onBack = onBack, onSaved = onSaved)
}
