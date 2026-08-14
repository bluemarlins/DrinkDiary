package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluemarlin.drinkdiary.DrinkDiaryApplication
import com.bluemarlin.drinkdiary.ui.profile.ProfileScreen
import com.bluemarlin.drinkdiary.ui.profile.ProfileViewModel
import com.bluemarlin.drinkdiary.ui.record.RecordFlow

private enum class RootScreen { Dashboard, Record }

// 재정의 진행 중 — F3(취향 요약)가 홈, F1·F2(기록)는 FAB에서 진입한다.
// collection/lookup/share/settings 는 software-architecture.md 6절에 따라 이어 붙인다.
@Composable
fun DrinkDiaryApp(modifier: Modifier = Modifier) {
    val appContainer = (LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer
    var screen by remember { mutableStateOf(RootScreen.Dashboard) }

    when (screen) {
        RootScreen.Dashboard -> {
            val viewModel: ProfileViewModel =
                viewModel(
                    factory =
                        ProfileViewModel.Factory(
                            appContainer.observeTasteProfileUseCase,
                            appContainer.resolveProfileReadinessUseCase,
                        ),
                )
            val state by viewModel.uiState.collectAsState()

            // 하단 탭(컬렉션·검색)은 해당 화면이 생길 때 붙인다. 지금 켜면 눌러도 아무 일이
            // 일어나지 않는 탭 두 개가 노출된다(실기기에서 확인).
            DDScreenScaffold(
                title = "테이스트 아카이브",
                screenType = DDScreenType.Detail,
                floatingActionButton = {
                    FloatingActionButton(onClick = { screen = RootScreen.Record }) {
                        Text("+")
                    }
                },
            ) { padding ->
                ProfileScreen(
                    state = state,
                    onScopeChange = viewModel::selectScope,
                    modifier = modifier.padding(padding),
                )
            }
        }

        RootScreen.Record -> {
            DDScreenScaffold(
                title = "기록하기",
                screenType = DDScreenType.Editor,
                onBackClick = { screen = RootScreen.Dashboard },
            ) { padding ->
                RecordFlow(modifier = modifier.padding(padding))
            }
        }
    }
}
