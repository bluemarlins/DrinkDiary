package com.bluemarlin.drinkdiary.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluemarlin.drinkdiary.DrinkDiaryApplication
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.collection.CollectionScreen
import com.bluemarlin.drinkdiary.ui.collection.CollectionViewModel
import com.bluemarlin.drinkdiary.ui.collection.RecordDetailScreen
import com.bluemarlin.drinkdiary.ui.profile.ProfileScreen
import com.bluemarlin.drinkdiary.ui.profile.ProfileViewModel
import com.bluemarlin.drinkdiary.ui.record.RecordFlow

private sealed interface Screen {
    data object Dashboard : Screen

    data object Collection : Screen

    data object Record : Screen

    data class Detail(
        val id: Long,
    ) : Screen
}

// 재정의 진행 중 — F3(취향 요약)와 F1(컬렉션)이 최상위, 기록은 FAB로 진입한다.
// lookup/share/settings 는 software-architecture.md 6절에 따라 이어 붙인다.
@Composable
fun DrinkDiaryApp(modifier: Modifier = Modifier) {
    val appContainer = (LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer
    var screen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val snackbar = remember { SnackbarHostState() }

    val collectionViewModel: CollectionViewModel =
        viewModel(factory = CollectionViewModel.Factory(appContainer.drinkRecordRepository))
    val collection by collectionViewModel.uiState.collectAsState()

    // 삭제 실패를 조용히 넘기면 목록이 그대로라 사용자는 지워진 줄 안다(harness.md §7).
    LaunchedEffect(collection.error) {
        collection.error?.let {
            snackbar.showSnackbar(it)
            collectionViewModel.dismissError()
        }
    }

    // 시스템 뒤로가기로도 앱을 벗어나지 않고 목록·대시보드로 돌아온다.
    BackHandler(enabled = screen != Screen.Dashboard) {
        screen = if (screen is Screen.Detail) Screen.Collection else Screen.Dashboard
    }

    val host: @Composable () -> Unit = { SnackbarHost(snackbar) }

    when (val current = screen) {
        Screen.Dashboard -> {
            val profileViewModel: ProfileViewModel =
                viewModel(
                    factory =
                        ProfileViewModel.Factory(
                            appContainer.observeTasteProfileUseCase,
                            appContainer.resolveProfileReadinessUseCase,
                        ),
                )
            val profile by profileViewModel.uiState.collectAsState()

            TopLevel(
                title = "테이스트 아카이브",
                tab = DDTopLevelTab.Dashboard,
                onTabChange = { screen = it },
                onAdd = { screen = Screen.Record },
                snackbarHost = host,
            ) { padding ->
                ProfileScreen(
                    state = profile,
                    onScopeChange = profileViewModel::selectScope,
                    modifier = modifier.padding(padding),
                )
            }
        }

        Screen.Collection ->
            TopLevel(
                title = "컬렉션",
                tab = DDTopLevelTab.Collection,
                onTabChange = { screen = it },
                onAdd = { screen = Screen.Record },
                snackbarHost = host,
            ) { padding ->
                CollectionScreen(
                    state = collection,
                    onFilterChange = collectionViewModel::selectFilter,
                    onOpen = { screen = Screen.Detail(it) },
                    contentPadding = padding,
                    modifier = modifier,
                )
            }

        Screen.Record ->
            DDScreenScaffold(
                title = "기록하기",
                screenType = DDScreenType.Editor,
                onBackClick = { screen = Screen.Dashboard },
                snackbarHost = host,
            ) { padding ->
                RecordFlow(modifier = modifier.padding(padding))
            }

        is Screen.Detail -> {
            val record = collection.records.firstOrNull { it.id == current.id }
            DDScreenScaffold(
                title = record?.let { DrinkLabels.drinkType(it.type) } ?: "기록",
                screenType = DDScreenType.Detail,
                onBackClick = { screen = Screen.Collection },
                snackbarHost = host,
            ) { padding ->
                RecordDetailScreen(
                    record = record,
                    onDelete = {
                        collectionViewModel.delete(current.id)
                        screen = Screen.Collection
                    },
                    contentPadding = padding,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun TopLevel(
    title: String,
    tab: DDTopLevelTab,
    onTabChange: (Screen) -> Unit,
    onAdd: () -> Unit,
    snackbarHost: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    // 검색(F5)은 아직 화면이 없어 핸들러를 넘기지 않는다 — 탭도 그려지지 않는다.
    DDScreenScaffold(
        title = title,
        screenType = DDScreenType.TopLevel,
        selectedTab = tab,
        onDashboardClick = { onTabChange(Screen.Dashboard) },
        onCollectionClick = { onTabChange(Screen.Collection) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Text("+") }
        },
        snackbarHost = snackbarHost,
        content = content,
    )
}
