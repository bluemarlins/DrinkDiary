package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// 명세 5.2절 `DDRatingInput`. 기록 화면의 private 함수였는데, 편집 화면도 같은 폼을 쓰는 이상
// 만족도 입력은 화면의 것이 아니라 시스템의 것이다.
@Composable
fun DDRatingInput(
    rating: Double,
    onRatingChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs),
    ) {
        (1..5).forEach { value ->
            val selected = rating >= value
            val containerColor =
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            val contentColor =
                if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            val borderColor =
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }

            Card(
                onClick = { onRatingChange(value.toDouble()) },
                // 숫자만 읽으면 무엇의 숫자인지 알 수 없다. 스크린 리더에는 "만족도 3점"으로 들린다.
                modifier =
                    Modifier
                        .size(56.dp)
                        .semantics { contentDescription = "만족도 ${value}점" },
                shape = MaterialTheme.shapes.medium,
                colors =
                    CardDefaults.cardColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                border = BorderStroke(1.dp, borderColor),
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
