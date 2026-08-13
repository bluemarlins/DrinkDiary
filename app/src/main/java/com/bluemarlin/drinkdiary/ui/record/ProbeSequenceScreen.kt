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
    // 기본 경로는 공통 축 4개만 묻는다. 주종 고유 축(산미·탄닌·피트·알코올)을 여기 넣으면
    // 와인 기준 탭이 7회가 되어 PRD F2의 5회 상한을 넘긴다(실기기에서 확인).
    // 고유 축은 선택 입력인 확장 경로로 보낸다 — mvp-scope.md F2, design-principles.md 쟁점 4.
    // 유형(TasteType)도 공통 축만으로 성립하므로 기본 경로에서 빠져도 손실이 없다.
    val traits = Trait.shared
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
