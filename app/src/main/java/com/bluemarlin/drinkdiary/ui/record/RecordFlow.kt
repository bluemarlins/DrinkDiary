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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiaryTheme

private sealed interface RecordStep {
    data object PickDrink : RecordStep

    data class Probes(
        val type: DrinkType,
    ) : RecordStep

    data class Done(
        val type: DrinkType,
        val taps: Int,
    ) : RecordStep
}

// F2 검증용 흐름. 저장·이름 입력은 아직 붙이지 않았고, 탭 수가 PRD 상한(5회)을
// 실제로 지키는지 실기기에서 확인하는 것이 목적이다.
@Composable
fun RecordFlow(modifier: Modifier = Modifier) {
    var step: RecordStep by remember { mutableStateOf(RecordStep.PickDrink) }
    var taste by remember { mutableStateOf(TasteInput()) }
    var taps by remember { mutableStateOf(0) }

    Crossfade(targetState = step, label = "record-step") { current ->
        when (current) {
            RecordStep.PickDrink ->
                DrinkTypePicker(
                    onPick = {
                        taps++
                        taste = TasteInput()
                        step = RecordStep.Probes(it)
                    },
                    modifier = modifier,
                )

            is RecordStep.Probes ->
                ProbeSequenceScreen(
                    type = current.type,
                    answers = taste,
                    onAnswer = { trait, answer ->
                        taps++
                        taste = taste.with(trait, answer)
                    },
                    onComplete = { step = RecordStep.Done(current.type, taps) },
                    modifier = modifier,
                )

            is RecordStep.Done ->
                RecordDone(
                    taps = current.taps,
                    answered = taste.directionalCount,
                    unsure = taste.answers.size - taste.directionalCount,
                    onRestart = {
                        taps = 0
                        step = RecordStep.PickDrink
                    },
                    modifier = modifier,
                )
        }
    }
}

@Composable
private fun DrinkTypePicker(
    onPick: (DrinkType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("무엇을 마셨나요?", style = MaterialTheme.typography.headlineSmall)
        DrinkType.entries.forEach { type ->
            Card(
                onClick = { onPick(type) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (type == DrinkType.Wine) "와인" else "위스키",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(24.dp),
                )
            }
        }
    }
}

@Composable
private fun RecordDone(
    taps: Int,
    answered: Int,
    unsure: Int,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("기록했습니다.", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "탭 ${taps}번으로 끝났습니다.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "방향을 답한 축 ${answered}개, 모르겠다고 답한 축 ${unsure}개.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "유형은 한 잔으로 나오지 않습니다. 기록이 쌓여 대비가 생겨야 판정됩니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text("다시 기록하기", modifier = Modifier.padding(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordFlowPreview() {
    DrinkDiaryTheme { RecordFlow() }
}
