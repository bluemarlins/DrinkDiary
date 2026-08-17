package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

@Immutable
data class DDTastingGapLine(
    // 어느 카테고리의 이야기인지. 문장만 있으면 "셰리"와 "스모키함"이 같은 층위로 읽힌다.
    val label: String,
    val sentence: String,
)

// 아직 안 마셔본 조합(prd.md F3-3 (b)). **추천 카드가 아니다** — 강조색도 액션 버튼도 두지 않는다.
// 누를 것이 있으면 그것은 권유가 되고, 권유는 우리가 갖고 있지 않은 근거(남의 평점)를 요구한다.
@Composable
fun DDTastingGapCard(
    lines: List<DDTastingGapLine>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(DrinkDiarySpacing.md),
            verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.md),
        ) {
            Text("아직 안 마셔본 것", style = MaterialTheme.typography.titleMedium)
            // 왜 이 자리가 있는지를 밝힌다. 다양성이 부족하면 비교가 성립하지 않는다는 것은
            // 우리 알고리즘의 사정이지만, 그 결과를 겪는 것은 사용자다.
            Text(
                text = "비교할 짝이 생기면 여기서 더 말할 수 있어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            lines.forEach { line ->
                Column(verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xxs)) {
                    Text(
                        text = line.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(line.sentence, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
