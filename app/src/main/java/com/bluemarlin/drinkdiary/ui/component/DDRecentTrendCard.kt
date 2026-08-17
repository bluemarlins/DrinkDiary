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
data class DDTrendBar(
    val label: String,
    val value: String,
    val fraction: Float,
    // 강조색은 **점수가 높은 쪽**에만 붙는다. 라벨 절에서 초록이 "높게 준 쪽"을 뜻하므로,
    // 여기서 초록이 "최근"을 뜻하면 같은 색이 두 화면에서 다른 말을 하게 된다.
    val emphasised: Boolean = false,
)

// 최근 흐름(prd.md F3-3 (a)). **유형 카드 아래**에 놓이므로 그보다 조용해야 한다 —
// `HeadlineSentence`를 쓰면 회고가 결론과 같은 목소리를 갖고, 그 순간 사용자는 이쪽을
// 판정으로 읽는다. 그래서 `DDTasteSentenceCard`를 재사용하지 않는다.
//
// 만족도는 문장이 아니라 막대다(prd.md F3-4 (c)). 원래는 "최근에는 평균 2.4점으로 점수를
// 주셨어요. 그 이전(4.4점)보다 낮아요"라는 두 줄짜리 문장이었는데, 화면 오른쪽 아래는 FAB가
// 떠 있는 자리라 그 줄이 실제로 가려졌다. 두 막대면 같은 것을 더 짧게, 더 빨리 말한다.
@Composable
fun DDRecentTrendCard(
    caption: String,
    shiftLine: String,
    recent: DDTrendBar,
    earlier: DDTrendBar,
    verdict: String,
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
            // 무엇과 무엇을 견줬는지 먼저 밝힌다. 이 줄이 없으면 아래가
            // "당신은 이런 사람이다"로 읽힌다.
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(shiftLine, style = MaterialTheme.typography.bodyLarge)

            Column(verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs)) {
                // 차이가 말할 만할 때만 한쪽을 강조한다. 미세한 차이를 색으로 가르면
                // 눈이 먼저 속는다 — 라벨 대조에서와 같은 규칙이다.
                listOf(recent, earlier).forEach { bar ->
                    DDRatingBar(
                        label = bar.label,
                        value = bar.value,
                        fraction = bar.fraction,
                        emphasised = bar.emphasised,
                    )
                }
            }

            Text(
                text = verdict,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
