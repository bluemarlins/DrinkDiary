package com.bluemarlin.drinkdiary.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluemarlin.drinkdiary.AppContainer
import com.bluemarlin.drinkdiary.DrinkDiaryApplication
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.collection.CollectionListDetail
import com.bluemarlin.drinkdiary.ui.collection.CollectionScreen
import com.bluemarlin.drinkdiary.ui.collection.CollectionUiState
import com.bluemarlin.drinkdiary.ui.collection.CollectionViewModel
import com.bluemarlin.drinkdiary.ui.collection.RecordDetailScreen
import com.bluemarlin.drinkdiary.ui.component.DDBatchActionBar
import com.bluemarlin.drinkdiary.ui.component.DDConfirmDialog
import com.bluemarlin.drinkdiary.ui.component.DDIconButton
import com.bluemarlin.drinkdiary.ui.profile.ProfileScreen
import com.bluemarlin.drinkdiary.ui.profile.ProfileViewModel
import com.bluemarlin.drinkdiary.ui.record.EditRecordScreen
import com.bluemarlin.drinkdiary.ui.record.EditRecordViewModel
import com.bluemarlin.drinkdiary.ui.record.RecordFlow
import com.bluemarlin.drinkdiary.ui.settings.SettingsScreen
import com.bluemarlin.drinkdiary.ui.settings.SettingsViewModel

private sealed interface Screen {
    data object Dashboard : Screen

    data object Collection : Screen

    data object Record : Screen

    data class Detail(
        val id: Long,
    ) : Screen

    data class Edit(
        val id: Long,
    ) : Screen

    data object Settings : Screen
}

// 모션이 방향을 알려면 화면이 계층의 어디에 있는지를 알아야 한다. 깊이가 같은 이동은
// 방향이 없는 이동이다(명세 1절 3번).
private val Screen.depth: Int
    get() =
        when (this) {
            Screen.Dashboard, Screen.Collection -> 0
            Screen.Record, Screen.Settings, is Screen.Detail -> 1
            is Screen.Edit -> 2
        }

// Compact가 아니면 목록과 상세를 한 화면에 놓는다(명세 4절 마지막 열). 그 순간 상세는 더 이상
// 별도 화면이 아니므로 최상위처럼 취급해야 한다 — 하단 탭이 사라지면 안 되고, 뒤로 화살표가
// 하단 탭과 나란히 놓이면 안 된다.
private fun isListDetail(
    screen: Screen,
    windowSize: DDWindowSize,
): Boolean = windowSize != DDWindowSize.Compact && (screen == Screen.Collection || screen is Screen.Detail)

// 재정의 진행 중 — F3(취향 요약)와 F1(컬렉션)이 최상위, 기록은 FAB로 진입한다.
// lookup/share/settings 는 software-architecture.md 6절에 따라 이어 붙인다.
//
// **스캐폴드는 하나다.** 화면마다 새로 만들면 탭을 옮길 때 상단 바와 하단 바까지 다시 그려지는데,
// 그것들은 제자리에 있어야 하는 것들이다. 화면별 값은 `screen`에서 계산해 넘기고,
// 바뀌는 것은 content 슬롯 안쪽뿐이다.
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

    // 시스템 뒤로가기로도 앱을 벗어나지 않고 온 곳으로 돌아온다.
    // 편집에서 뒤로가면 대시보드가 아니라 **그 기록의 상세**로 가야 한다 — 고치다 만 사람을
    // 목록 맨 위로 보내면 방금 보던 기록을 다시 찾아야 한다.
    // 2단 화면에서는 상세가 오른쪽 칸이므로, 뒤로가기는 그 칸을 비우는 일이 된다.
    // 선택 모드가 켜져 있으면 뒤로가기는 **먼저 그것을 끈다.** 지우려고 고르던 중에 화면이
    // 통째로 바뀌면 고른 것을 잃고, 무엇을 되돌린 것인지도 알 수 없다.
    BackHandler(enabled = collection.selectionMode) { collectionViewModel.clearSelection() }

    BackHandler(enabled = !collection.selectionMode && screen != Screen.Dashboard) {
        screen =
            when (val current = screen) {
                is Screen.Detail -> Screen.Collection
                is Screen.Edit -> Screen.Detail(current.id)
                else -> Screen.Dashboard
            }
    }

    // prd.md F1-2. 선택 모드는 컬렉션 계열 화면에서만 성립한다 — 대시보드에 있는 동안
    // 목록의 선택이 상단 바를 점유하면 사용자는 자기가 어디에 있는지 잃는다.
    var confirmingBulkDelete by remember { mutableStateOf(false) }

    val host: @Composable () -> Unit = { SnackbarHost(snackbar) }

    // 크롬(제목·탭·FAB)이 구간에 따라 달라지므로 스캐폴드 **바깥**에서 폭을 알아야 한다.
    // `LocalDDWindowSize`는 스캐폴드 안에서 제공되므로 여기서는 아직 기본값이다.
    BoxWithConstraints(modifier = modifier) {
        val windowSize =
            when {
                maxWidth < 600.dp -> DDWindowSize.Compact
                maxWidth < 840.dp -> DDWindowSize.Medium
                else -> DDWindowSize.Expanded
            }
        val listDetail = isListDetail(screen, windowSize)
        val topLevel = screen.depth == 0 || listDetail
        // 선택 모드는 컬렉션 목록이 보이는 동안에만 화면을 지배한다.
        val inCollection = screen == Screen.Collection || listDetail
        val selecting = collection.selectionMode && inCollection

        val title =
            if (selecting) {
                "${collection.selected.size}개 선택"
            } else if (listDetail) {
                "컬렉션"
            } else {
                when (val current = screen) {
                    Screen.Dashboard -> "테이스트 아카이브"
                    Screen.Collection -> "컬렉션"
                    Screen.Record -> "기록하기"
                    is Screen.Detail ->
                        collection.records
                            .firstOrNull { it.id == current.id }
                            ?.let { DrinkLabels.drinkType(it.type) } ?: "기록"
                    is Screen.Edit -> "기록 고치기"
                    Screen.Settings -> "설정"
                }
            }

        val screenType =
            when {
                listDetail -> DDScreenType.TopLevel
                screen == Screen.Dashboard || screen == Screen.Collection -> DDScreenType.TopLevel
                screen == Screen.Record || screen is Screen.Edit -> DDScreenType.Editor
                else -> DDScreenType.Detail
            }

        val selectedTab =
            when {
                listDetail -> DDTopLevelTab.Collection
                screen == Screen.Dashboard -> DDTopLevelTab.Dashboard
                screen == Screen.Collection -> DDTopLevelTab.Collection
                else -> null
            }

        // 검색(F5)은 아직 화면이 없어 핸들러를 넘기지 않는다 — 탭도 그려지지 않는다.
        val onDashboardClick: (() -> Unit)? =
            if (topLevel) {
                { screen = Screen.Dashboard }
            } else {
                null
            }
        val onCollectionClick: (() -> Unit)? =
            if (topLevel) {
                { screen = Screen.Collection }
            } else {
                null
            }

        val onBackClick: (() -> Unit)? =
            if (listDetail) {
                null
            } else {
                when (val current = screen) {
                    Screen.Record, Screen.Settings -> {
                        { screen = Screen.Dashboard }
                    }
                    is Screen.Detail -> {
                        { screen = Screen.Collection }
                    }
                    is Screen.Edit -> {
                        { screen = Screen.Detail(current.id) }
                    }
                    Screen.Dashboard, Screen.Collection -> null
                }
            }

        // 지우러 들어온 사람에게 더하기를 권하지 않는다(prd.md F1-2).
        val floatingActionButton: (@Composable () -> Unit)? =
            if (topLevel && !selecting) {
                {
                    FloatingActionButton(onClick = { screen = Screen.Record }) {
                        Icon(painter = painterResource(R.drawable.ic_add), contentDescription = "기록 추가")
                    }
                }
            } else {
                null
            }

        val toolbarActions: @Composable RowScope.() -> Unit = {
            if (selecting) {
                DDIconButton(
                    onClick = collectionViewModel::clearSelection,
                    contentDescription = "선택 해제",
                ) {
                    Icon(painter = painterResource(R.drawable.ic_close), contentDescription = null)
                }
            } else if (screen == Screen.Dashboard) {
                // 설정은 하단 탭이 아니라 툴바에 둔다. 탭은 매일 오가는 곳이고
                // 설정은 한 번 정하면 다시 안 오는 곳이다.
                DDIconButton(
                    onClick = { screen = Screen.Settings },
                    contentDescription = "설정",
                ) {
                    Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = null)
                }
            }
        }

        DDScreenScaffold(
            title = title,
            screenType = screenType,
            selectedTab = selectedTab,
            onDashboardClick = onDashboardClick,
            onCollectionClick = onCollectionClick,
            onBackClick = onBackClick,
            floatingActionButton = floatingActionButton,
            toolbarActions = toolbarActions,
            snackbarHost = host,
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        when {
                            targetState.depth > initialState.depth -> depthIn()
                            targetState.depth < initialState.depth -> depthOut()
                            else -> fadeThrough()
                        }
                    },
                    // 같은 깊이 0의 두 탭은 키가 같다. 그래야 탭을 옮길 때 바깥 스캐폴드가
                    // 다시 만들어지지 않고 안쪽 내용만 바뀐다. 2단 화면에서는 목록과 상세가
                    // **같은 화면**이므로 그 둘도 키가 같아야 한다 — 사이에서 깊이 전이가 돌면 안 된다.
                    contentKey = { if (it.depth == 0 || isListDetail(it, windowSize)) "top" else it },
                    label = "screen",
                ) { current ->
                    if (current.depth == 0 || isListDetail(current, windowSize)) {
                        // 바깥 전이가 돌지 않는 자리라 탭 모션은 여기서 건다.
                        AnimatedContent(
                            targetState = current,
                            transitionSpec = { fadeThrough() },
                            label = "tab",
                        ) { tabScreen ->
                            ScreenContent(
                                screen = tabScreen,
                                padding = padding,
                                appContainer = appContainer,
                                collection = collection,
                                collectionViewModel = collectionViewModel,
                                snackbar = snackbar,
                                onNavigate = { screen = it },
                                listDetail = isListDetail(tabScreen, windowSize),
                            )
                        }
                    } else {
                        ScreenContent(
                            screen = current,
                            padding = padding,
                            appContainer = appContainer,
                            collection = collection,
                            collectionViewModel = collectionViewModel,
                            snackbar = snackbar,
                            onNavigate = { screen = it },
                            listDetail = false,
                        )
                    }
                }

                // 근거 초안(design-system-ux-research.md 4.2절 3번)이 말하는 "하단 일괄 작업바 Slide-in".
                // 하단 바 위에 얹는다 — 스캐폴드의 bottomBar 자리를 뺏으면 탭이 사라진다.
                AnimatedVisibility(
                    visible = selecting,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            // Compact에서만 하단 바가 있다. 그 위로 올린다.
                            .padding(
                                bottom =
                                    if (windowSize == DDWindowSize.Compact) DDBottomNavigationBarHeight else 0.dp,
                            ).padding(LocalDDScreenMargin.current),
                ) {
                    DDBatchActionBar(
                        selectedCount = collection.selected.size,
                        onDelete = { confirmingBulkDelete = true },
                    )
                }
            }
        }

        // **되돌리기가 없으므로 이 팝업이 유일한 방어선이다**(prd.md F1-2).
        // 그래서 몇 건인지와 무엇이 함께 사라지는지를 정확히 적는다.
        if (confirmingBulkDelete) {
            val count = collection.selected.size
            DDConfirmDialog(
                title = "${count}개 기록을 지울까요?",
                message = "취향 답까지 함께 지워지고, 되돌릴 수 없어요. 취향 유형도 이 ${count}개만큼 다시 계산돼요.",
                confirmLabel = "지우기",
                dismissLabel = "그대로 두기",
                onConfirm = {
                    confirmingBulkDelete = false
                    collectionViewModel.deleteSelected()
                },
                onDismiss = { confirmingBulkDelete = false },
            )
        }
    }
}

@Composable
private fun ScreenContent(
    screen: Screen,
    padding: PaddingValues,
    appContainer: AppContainer,
    collection: CollectionUiState,
    collectionViewModel: CollectionViewModel,
    snackbar: SnackbarHostState,
    onNavigate: (Screen) -> Unit,
    listDetail: Boolean,
    modifier: Modifier = Modifier,
) {
    when (screen) {
        Screen.Dashboard -> {
            val profileViewModel: ProfileViewModel =
                viewModel(
                    factory =
                        ProfileViewModel.Factory(
                            appContainer.observeTasteProfileUseCase,
                            appContainer.observeTagPreferenceUseCase,
                            appContainer.resolveProfileReadinessUseCase,
                        ),
                )
            val profile by profileViewModel.uiState.collectAsState()

            ProfileScreen(
                state = profile,
                onScopeChange = profileViewModel::selectScope,
                modifier = modifier.padding(padding),
            )
        }

        Screen.Collection ->
            if (listDetail) {
                CollectionListDetail(
                    state = collection,
                    selectedId = null,
                    onFilterChange = collectionViewModel::selectFilter,
                    onSelect = { onNavigate(Screen.Detail(it)) },
                    onToggleSelect = collectionViewModel::toggleSelection,
                    onEdit = { onNavigate(Screen.Edit(it)) },
                    onDelete = {
                        collectionViewModel.delete(it)
                        onNavigate(Screen.Collection)
                    },
                    contentPadding = padding,
                    modifier = modifier,
                )
            } else {
                CollectionScreen(
                    state = collection,
                    onFilterChange = collectionViewModel::selectFilter,
                    onOpen = { onNavigate(Screen.Detail(it)) },
                    onToggleSelect = collectionViewModel::toggleSelection,
                    contentPadding = padding,
                    modifier = modifier,
                )
            }

        Screen.Record -> RecordFlow(modifier = modifier.padding(padding))

        is Screen.Detail ->
            if (listDetail) {
                CollectionListDetail(
                    state = collection,
                    selectedId = screen.id,
                    onFilterChange = collectionViewModel::selectFilter,
                    onSelect = { onNavigate(Screen.Detail(it)) },
                    onToggleSelect = collectionViewModel::toggleSelection,
                    onEdit = { onNavigate(Screen.Edit(it)) },
                    onDelete = {
                        collectionViewModel.delete(it)
                        onNavigate(Screen.Collection)
                    },
                    contentPadding = padding,
                    modifier = modifier,
                )
            } else {
                RecordDetailScreen(
                    record = collection.records.firstOrNull { it.id == screen.id },
                    onEdit = { onNavigate(Screen.Edit(screen.id)) },
                    onDelete = {
                        collectionViewModel.delete(screen.id)
                        onNavigate(Screen.Collection)
                    },
                    contentPadding = padding,
                    modifier = modifier,
                )
            }

        is Screen.Edit -> {
            // key로 id를 넘겨야 다른 기록을 편집할 때 앞 기록의 폼이 남지 않는다.
            val editViewModel: EditRecordViewModel =
                viewModel(
                    key = "edit-${screen.id}",
                    factory =
                        EditRecordViewModel.Factory(
                            screen.id,
                            appContainer.drinkRecordRepository,
                            appContainer.userPreferencesRepository,
                        ),
                )
            val edit by editViewModel.uiState.collectAsState()

            // 저장이 끝나면 상세로 돌아간다. 편집 화면에 머무르면 고쳐졌는지 알 수 없다.
            LaunchedEffect(edit.saved) {
                if (edit.saved) onNavigate(Screen.Detail(screen.id))
            }
            LaunchedEffect(edit.error) {
                edit.error?.let {
                    snackbar.showSnackbar(it)
                    editViewModel.dismissError()
                }
            }

            EditRecordScreen(
                state = edit,
                onFormChange = editViewModel::updateForm,
                onAnswer = editViewModel::answer,
                onSave = editViewModel::save,
                contentPadding = padding,
                modifier = modifier,
            )
        }

        Screen.Settings -> {
            val settingsViewModel: SettingsViewModel =
                viewModel(factory = SettingsViewModel.Factory(appContainer.userPreferencesRepository))
            val settings by settingsViewModel.uiState.collectAsState()

            SettingsScreen(
                state = settings,
                onToggle = settingsViewModel::toggle,
                contentPadding = padding,
                modifier = modifier,
            )
        }
    }
}
