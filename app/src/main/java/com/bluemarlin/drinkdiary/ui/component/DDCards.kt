package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.theme.DisplayTasteCode
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing
import com.bluemarlin.drinkdiary.ui.theme.HeadlineSentence

@Composable
fun DDTasteTypeBadge(
    code: String,
    name: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = code,
            style = DisplayTasteCode,
            color = MaterialTheme.colorScheme.primary,
        )
        Box(
            modifier =
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = DrinkDiarySpacing.sm, vertical = DrinkDiarySpacing.xxs),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun DDTasteSentenceCard(
    sentence: String,
    modifier: Modifier = Modifier,
    details: List<String> = emptyList(),
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = sentence,
                style = HeadlineSentence,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (details.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    details.forEach { detail ->
                        Text(
                            text = "• $detail",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// 임계치 전 구간의 카드. **게이지만 있던 자리에 되비침이 들어온다**(prd.md F3-3 (d)) —
// 그 자리가 비어 있으면 이 구간의 화면 전체가 "아직 아무것도 없다"가 된다.
// `details`는 사용자가 남긴 답을 되읽어 주는 줄이며 **취향 판정이 아니다**.
@Composable
fun DDProfileProgressCard(
    title: String,
    description: String,
    currentCount: Int,
    targetCount: Int,
    modifier: Modifier = Modifier,
    details: List<String> = emptyList(),
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (details.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    details.forEach { detail ->
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Progress bar
            val safeTarget = targetCount.coerceAtLeast(1)
            val progressFraction = (currentCount.toFloat() / safeTarget).coerceIn(0f, 1f)

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(progressFraction)
                            .height(6.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}
