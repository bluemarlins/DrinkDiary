package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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

private enum class Step { PickDrink, Probes, Detail, Saved }

@Composable
fun RecordFlow(modifier: Modifier = Modifier) {
    val appContainer = (LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer
    val viewModel: RecordViewModel =
        viewModel(factory = RecordViewModel.Factory(appContainer.drinkRecordRepository))
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
                    onFormChange = viewModel::updateForm,
                    onSave = viewModel::save,
                    saving = state.saving,
                    modifier = modifier,
                )

            Step.Saved ->
                RecordSaved(
                    taps = state.taps,
                    leaning = state.taste.leaningCount,
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
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("기록했습니다.", style = MaterialTheme.typography.headlineSmall)
        Text("취향 입력은 탭 ${taps}번으로 끝났습니다.", style = MaterialTheme.typography.bodyLarge)
        Text(
            // '보통'을 뺀 개수를 말하되 그것이 버려졌다는 인상을 주지 않는다 — 판정에는 다 쓰인다.
            text = "뚜렷한 인상을 남긴 축 ${leaning}개.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "유형은 한 잔으로 나오지 않습니다. 기록이 쌓여 대비가 생겨야 판정됩니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text("한 잔 더 기록하기", modifier = Modifier.padding(20.dp))
        }
    }
}
