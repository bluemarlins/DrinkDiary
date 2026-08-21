package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.ui.component.DDProbeProgress
import com.bluemarlin.drinkdiary.ui.component.DDProbeQuestion

@Composable
fun ProbeSequenceScreen(
    type: DrinkType,
    answers: TasteInput,
    onAnswer: (Trait, TraitAnswer) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 주종별 5축(와인: 당도·산미·탄닌·바디·여운 / 위스키: 단맛·바디·피트·알코올·여운)을 순차적으로 묻는다.
    val traits = Trait.of(type)
    var currentIndex by remember { mutableStateOf(0) }

    if (traits.isEmpty()) {
        return
    }

    val safeIndex = currentIndex.coerceIn(0, traits.lastIndex)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        DDProbeProgress(
            current = safeIndex + 1,
            total = traits.size,
            modifier = Modifier.fillMaxWidth(),
        )

        Crossfade(
            targetState = safeIndex,
            label = "ProbeSequenceAnimation",
            modifier = Modifier.fillMaxWidth(),
        ) { index ->
            val trait = traits[index]
            val copy = probeCopy(type = type, trait = trait)

            DDProbeQuestion(
                copy = copy,
                selected = answers[trait],
                onSelect = { answer ->
                    onAnswer(trait, answer)
                    if (index < traits.lastIndex) {
                        currentIndex = index + 1
                    } else {
                        onComplete()
                    }
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProbeSequenceScreenPreview() {
    MaterialTheme {
        Surface {
            ProbeSequenceScreen(
                type = DrinkType.Wine,
                answers = TasteInput(),
                onAnswer = { _, _ -> },
                onComplete = {},
            )
        }
    }
}
