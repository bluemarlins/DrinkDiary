package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDTagChipGroup

// 편집 화면용 취향 입력. **기록 때의 4단계 마법사를 다시 태우지 않는다** —
// 고치러 온 사람은 안내받을 게 아니라 한 곳을 바로 찾아 고쳐야 한다.
// 한 화면에 축을 다 펼치는 대신, 기록 경로에서는 이 형태를 쓰지 않는다(거기서는 마찰 예산이 우선).
//
// 보여주는 축은 기록 경로가 묻는 공통 축뿐이다. 다만 **화면에 없는 답도 지우지 않는다** —
// 저장이 답을 통째로 갈아끼우므로, 안 보이는 축의 답은 ViewModel이 그대로 들고 있다가 함께 쓴다.
@Composable
fun TasteEditor(
    type: com.bluemarlin.drinkdiary.domain.model.DrinkType,
    taste: TasteInput,
    onAnswer: (Trait, TraitAnswer) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("그때 남긴 취향", style = MaterialTheme.typography.titleMedium)

        Trait.of(type).forEach { trait ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = DrinkLabels.trait(trait),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Low → Mid → High 순서로 고정한다. 축마다 순서가 달라지면 잘못 누른다.
                // 취소를 두지 않는다. 답을 비우면 그 축은 판정에서 빠지는데,
                // 편집 화면에서 그걸 의도적으로 하고 싶은 경우가 없다.
                DDTagChipGroup(
                    options = TraitAnswer.entries,
                    selected = taste[trait],
                    onSelect = { answer -> onAnswer(trait, answer) },
                    label = { answer -> DrinkLabels.answer(trait, answer) },
                )
            }
        }
    }
}
