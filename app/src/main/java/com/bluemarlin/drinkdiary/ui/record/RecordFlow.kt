package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluemarlin.drinkdiary.DrinkDiaryApplication
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TagCategory

private enum class Step { PickDrink, Probes, Detail, Saved }

@Composable
fun RecordFlow(modifier: Modifier = Modifier) {
    val appContainer = (LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer
    val viewModel: RecordViewModel =
        viewModel(
            factory =
                RecordViewModel.Factory(
                    appContainer.drinkRecordRepository,
                    appContainer.userPreferencesRepository,
                    appContainer.importPhotoUseCase,
                    appContainer.deletePhotoUseCase,
                ),
        )
    val state by viewModel.uiState.collectAsState()
    var step by remember { mutableStateOf(Step.PickDrink) }

    if (state.savedId != null && step != Step.Saved) step = Step.Saved

    Crossfade(targetState = step, label = "record-step") { current ->
        when (current) {
            Step.PickDrink ->
                DrinkPicker(
                    onPick = { choice ->
                        viewModel.pickDrink(choice.type, choice.tags)
                        step = Step.Probes
                    },
                    modifier = modifier,
                )

            Step.Probes ->
                ProbeSequenceScreen(
                    type = state.type ?: DrinkType.Wine,
                    answers = state.taste,
                    onAnswer = viewModel::answer,
                    onComplete = { step = Step.Detail },
                    modifier = modifier,
                )

            Step.Detail ->
                RecordDetailStep(
                    type = state.type ?: DrinkType.Wine,
                    form = state.form,
                    alwaysAskTags = state.alwaysAskTags,
                    onFormChange = viewModel::updateForm,
                    onPhotoPicked = viewModel::pickPhoto,
                    onSave = viewModel::save,
                    saving = state.saving,
                    modifier = modifier,
                    errorMessage = state.error,
                )

            Step.Saved ->
                RecordSaved(
                    taps = state.taps,
                    leaning = state.taste.leaningCount,
                    askTagPreference = state.askTagPreference,
                    onChooseTags = viewModel::chooseAlwaysAskTags,
                    onRestart = {
                        viewModel.startOver()
                        step = Step.PickDrink
                    },
                    modifier = modifier,
                )
        }
    }

    state.error?.let {
        // 저장 실패는 조용히 넘기지 않는다(harness.md §7).
        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(20.dp))
    }
}

@Composable
private fun RecordSaved(
    taps: Int,
    leaning: Int,
    askTagPreference: Boolean,
    onChooseTags: (Set<TagCategory>) -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("기록했어요.", style = MaterialTheme.typography.titleLarge)
        Text("취향 입력은 탭 ${taps}번으로 끝났어요.", style = MaterialTheme.typography.bodyLarge)
        Text(
            // '보통'을 뺀 개수를 말하되 그것이 버려졌다는 인상을 주지 않는다 — 판정에는 다 쓰인다.
            text = "뚜렷한 인상을 남긴 축 ${leaning}개.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "유형은 한 잔으로 나오지 않아요. 기록이 쌓여 대비가 생겨야 나와요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // 첫 기록 직후 한 번만. 이미 가치를 받은 뒤라 마찰로 느껴지지 않고,
        // 이 시점의 사용자는 방금 흐름을 겪어서 무엇을 묻는 건지 안다(prd.md S1).
        if (askTagPreference) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            TagPreferencePrompt(onConfirm = onChooseTags)
        } else {
            Card(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                Text("한 잔 더 기록하기", modifier = Modifier.padding(20.dp))
            }
        }
    }
}
