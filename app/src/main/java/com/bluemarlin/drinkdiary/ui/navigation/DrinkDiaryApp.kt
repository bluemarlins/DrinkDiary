package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
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
import com.bluemarlin.drinkdiary.ui.search.SearchRoute
import com.bluemarlin.drinkdiary.ui.search.SearchViewModel

private object Routes {
    const val Dashboard = "dashboard"
    const val Collection = "collection"
    const val Search = "search"
    const val CollectionWithStatus = "collection/{status}"
    const val Detail = "detail/{recordId}"
    const val EditorNew = "editor/new"
    const val EditorEdit = "editor/{recordId}"
}

@Composable
fun DrinkDiaryApp() {
    val navController = rememberNavController()
    val appContainer = (LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer

    NavHost(navController = navController, startDestination = Routes.Dashboard) {
        composable(Routes.Dashboard) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(appContainer.observeDashboardSummaryUseCase),
            )
            DashboardRoute(
                viewModel = viewModel,
                onAddRecord = { navController.navigate(Routes.EditorNew) },
                onOpenRecord = { navController.navigate("detail/$it") },
                onOpenStatus = { navController.navigate("collection/${it.name}") },
                onCollectionClick = { navController.navigateTopLevel(Routes.Collection) },
                onSearchClick = { navController.navigateTopLevel(Routes.Search) },
            )
        }
        composable(Routes.Collection) {
            CollectionEntry(
                initialStatus = null,
                onDashboardClick = { navController.navigateTopLevel(Routes.Dashboard) },
                onSearchClick = { navController.navigateTopLevel(Routes.Search) },
                onAddRecord = { navController.navigate(Routes.EditorNew) },
                onOpenRecord = { navController.navigate("detail/$it") },
            )
        }
        composable(
            route = Routes.CollectionWithStatus,
            arguments = listOf(navArgument("status") { type = NavType.StringType }),
        ) { entry ->
            val status = entry.arguments?.getString("status")?.let(CollectionStatus::fromStorageValue)
            CollectionEntry(
                initialStatus = status,
                onDashboardClick = { navController.navigateTopLevel(Routes.Dashboard) },
                onSearchClick = { navController.navigateTopLevel(Routes.Search) },
                onAddRecord = { navController.navigate(Routes.EditorNew) },
                onOpenRecord = { navController.navigate("detail/$it") },
            )
        }
        composable(Routes.Search) {
            val viewModel: SearchViewModel = viewModel(
                key = "search",
                factory = SearchViewModel.Factory(appContainer.observeSearchResultsUseCase),
            )
            SearchRoute(
                viewModel = viewModel,
                onDashboardClick = { navController.navigateTopLevel(Routes.Dashboard) },
                onCollectionClick = { navController.navigateTopLevel(Routes.Collection) },
                onOpenRecord = { navController.navigate("detail/$it") },
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
    onSearchClick: () -> Unit,
    onAddRecord: () -> Unit,
    onOpenRecord: (Long) -> Unit,
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
        onSearchClick = onSearchClick,
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

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
