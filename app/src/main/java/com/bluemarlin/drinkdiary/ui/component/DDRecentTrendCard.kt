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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// 최근 흐름(prd.md F3-3 (a)). **유형 카드 바로 아래**에 놓이므로 그보다 조용해야 한다 —
// `HeadlineSentence`를 쓰면 회고가 결론과 같은 목소리를 갖고, 그 순간 사용자는 이쪽을
// 판정으로 읽는다. 그래서 `DDTasteSentenceCard`를 재사용하지 않는다.
//
// 문구는 넘겨받는다. 이 카드는 무엇을 말할지 정하지 않는다(`RecentTrendCopy`가 정한다).
@Composable
fun DDRecentTrendCard(
    caption: String,
    lines: List<String>,
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
            verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
        ) {
            Text("최근 흐름", style = MaterialTheme.typography.titleMedium)
            // 무엇과 무엇을 견줬는지 먼저 밝힌다. 이 줄이 없으면 아래 문장이
            // "당신은 이런 사람이다"로 읽힌다.
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs)) {
                lines.forEach { line ->
                    Text(line, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
