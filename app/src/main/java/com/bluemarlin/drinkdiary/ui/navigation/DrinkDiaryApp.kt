package com.bluemarlin.drinkdiary.ui.navigation

import android.app.Activity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
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
import com.bluemarlin.drinkdiary.ui.insights.InsightsRoute
import com.bluemarlin.drinkdiary.ui.insights.InsightsViewModel
import com.bluemarlin.drinkdiary.ui.search.SearchRoute
import com.bluemarlin.drinkdiary.ui.search.SearchViewModel
import kotlin.math.roundToInt

private sealed interface AppRoute : NavKey {
    data object Dashboard : AppRoute

    data class Collection(
        val status: CollectionStatus? = null,
    ) : AppRoute

    data object Search : AppRoute

    data class Detail(
        val recordId: Long,
    ) : AppRoute

    data class Editor(
        val recordId: Long? = null,
    ) : AppRoute

    data object Insights : AppRoute
}

private const val NavigationSlideDurationMillis = 260
private const val TopLevelTransitionDurationMillis = 230
private const val FadeThroughDurationMillis = 180

@Composable
fun DrinkDiaryApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val appContainer = (context.applicationContext as DrinkDiaryApplication).appContainer
    val backStack = remember { mutableStateListOf<AppRoute>(AppRoute.Dashboard) }

    fun navigate(route: AppRoute) {
        backStack.add(route)
    }

    fun navigateTopLevel(route: AppRoute) {
        backStack.clear()
        backStack.add(route)
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        } else {
            activity?.finish()
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = ::goBack,
        transitionSpec = { drinkDiaryTransitionSpec() },
        popTransitionSpec = { drinkDiaryPopTransitionSpec() },
        predictivePopTransitionSpec = { _: Int -> drinkDiaryPopTransitionSpec() },
        entryProvider =
            entryProvider {
                entry<AppRoute.Dashboard> {
                    val viewModel: DashboardViewModel =
                        viewModel(
                            factory = DashboardViewModel.Factory(appContainer.observeDashboardSummaryUseCase),
                        )
                    DashboardRoute(
                        viewModel = viewModel,
                        onAddRecord = { navigate(AppRoute.Editor()) },
                        onOpenRecord = { navigate(AppRoute.Detail(it)) },
                        onOpenStatus = { navigateTopLevel(AppRoute.Collection(it)) },
                        onCollectionClick = { navigateTopLevel(AppRoute.Collection()) },
                        onSearchClick = { navigateTopLevel(AppRoute.Search) },
                        onOpenInsights = { navigate(AppRoute.Insights) },
                    )
                }
                entry<AppRoute.Collection> { route ->
                    CollectionEntry(
                        initialStatus = route.status,
                        onDashboardClick = { navigateTopLevel(AppRoute.Dashboard) },
                        onSearchClick = { navigateTopLevel(AppRoute.Search) },
                        onAddRecord = { navigate(AppRoute.Editor()) },
                        onOpenRecord = { navigate(AppRoute.Detail(it)) },
                    )
                }
                entry<AppRoute.Search> {
                    val viewModel: SearchViewModel =
                        viewModel(
                            key = "search",
                            factory = SearchViewModel.Factory(appContainer.observeSearchResultsUseCase),
                        )
                    SearchRoute(
                        viewModel = viewModel,
                        onDashboardClick = { navigateTopLevel(AppRoute.Dashboard) },
                        onCollectionClick = { navigateTopLevel(AppRoute.Collection()) },
                        onOpenRecord = { navigate(AppRoute.Detail(it)) },
                    )
                }
                entry<AppRoute.Detail>(metadata = detailTransitionMetadata()) { route ->
                    val viewModel: RecordDetailViewModel =
                        viewModel(
                            key = "detail_${route.recordId}",
                            factory =
                                RecordDetailViewModel.Factory(
                                    route.recordId,
                                    appContainer.observeDrinkRecordUseCase,
                                    appContainer.deleteDrinkRecordUseCase,
                                ),
                        )
                    RecordDetailRoute(
                        recordId = route.recordId,
                        viewModel = viewModel,
                        onBack = ::goBack,
                        onEdit = { navigate(AppRoute.Editor(it)) },
                    )
                }
                entry<AppRoute.Editor>(metadata = detailTransitionMetadata()) { route ->
                    EditorEntry(
                        recordId = route.recordId,
                        onBack = ::goBack,
                        onSaved = { navigateTopLevel(AppRoute.Collection()) },
                    )
                }
                entry<AppRoute.Insights>(metadata = detailTransitionMetadata()) {
                    val viewModel: InsightsViewModel =
                        viewModel(
                            factory = InsightsViewModel.Factory(appContainer.observeInsightsUseCase),
                        )
                    InsightsRoute(viewModel = viewModel, onBack = ::goBack)
                }
            },
    )
}

private fun AnimatedContentTransitionScope<Scene<AppRoute>>.drinkDiaryTransitionSpec(): ContentTransform {
    val initialRoute = initialState.key as? AppRoute
    val targetRoute = targetState.key as? AppRoute
    return if (initialRoute.isTopLevelRoute() && targetRoute.isTopLevelRoute()) {
        topLevelSharedAxisTransition(initialRoute, targetRoute)
    } else {
        fadeThroughTransition()
    }
}

private fun AnimatedContentTransitionScope<Scene<AppRoute>>.drinkDiaryPopTransitionSpec(): ContentTransform {
    val initialRoute = initialState.key as? AppRoute
    val targetRoute = targetState.key as? AppRoute
    return if (initialRoute.isTopLevelRoute() && targetRoute.isTopLevelRoute()) {
        topLevelSharedAxisTransition(initialRoute, targetRoute)
    } else {
        fadeThroughTransition()
    }
}

private fun topLevelSharedAxisTransition(
    initialRoute: AppRoute?,
    targetRoute: AppRoute?,
): ContentTransform {
    val direction =
        when {
            initialRoute == null || targetRoute == null -> 1
            topLevelRouteIndex(targetRoute) >= topLevelRouteIndex(initialRoute) -> 1
            else -> -1
        }
    val animationSpec =
        tween<IntOffset>(
            durationMillis = TopLevelTransitionDurationMillis,
            easing = FastOutSlowInEasing,
        )
    val fadeSpec =
        tween<Float>(
            durationMillis = TopLevelTransitionDurationMillis,
            easing = FastOutSlowInEasing,
        )

    return (
        slideInHorizontally(
            initialOffsetX = { direction * (it * 0.12f).roundToInt() },
            animationSpec = animationSpec,
        ) + fadeIn(animationSpec = fadeSpec)
    ) togetherWith (
        slideOutHorizontally(
            targetOffsetX = { -direction * (it * 0.08f).roundToInt() },
            animationSpec = animationSpec,
        ) + fadeOut(animationSpec = fadeSpec)
    )
}

private fun fadeThroughTransition(): ContentTransform {
    val fadeSpec =
        tween<Float>(
            durationMillis = FadeThroughDurationMillis,
            easing = FastOutSlowInEasing,
        )
    return (
        fadeIn(animationSpec = fadeSpec) +
            scaleIn(
                initialScale = 0.98f,
                animationSpec = fadeSpec,
            )
    ) togetherWith fadeOut(animationSpec = fadeSpec)
}

private fun AppRoute?.isTopLevelRoute(): Boolean =
    this is AppRoute.Dashboard || this is AppRoute.Collection || this is AppRoute.Search

private fun topLevelRouteIndex(route: AppRoute): Int =
    when (route) {
        AppRoute.Dashboard -> 0
        is AppRoute.Collection -> 1
        AppRoute.Search -> 2
        is AppRoute.Detail,
        is AppRoute.Editor,
        AppRoute.Insights,
        -> 1
    }

private fun detailTransitionMetadata(): Map<String, Any> =
    NavDisplay.transitionSpec {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(NavigationSlideDurationMillis),
        ) togetherWith
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(NavigationSlideDurationMillis),
            )
    } +
        NavDisplay.popTransitionSpec {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(NavigationSlideDurationMillis),
            ) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(NavigationSlideDurationMillis),
                )
        } +
        NavDisplay.predictivePopTransitionSpec {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(NavigationSlideDurationMillis),
            ) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(NavigationSlideDurationMillis),
                )
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
    val viewModel: CollectionViewModel =
        viewModel(
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
    val viewModel: RecordEditorViewModel =
        viewModel(
            key = "editor_${recordId ?: "new"}",
            factory =
                RecordEditorViewModel.Factory(
                    recordId,
                    appContainer.observeDrinkRecordUseCase,
                    appContainer.saveDrinkRecordUseCase,
                ),
        )
    RecordEditorRoute(viewModel = viewModel, onBack = onBack, onSaved = onSaved)
}
