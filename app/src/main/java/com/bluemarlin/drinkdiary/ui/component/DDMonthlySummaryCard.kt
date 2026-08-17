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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.MonthlySummary
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// 이번 달 회고. **취향 카드 아래**에 둔다 — 판정이 주인공이고 이쪽은 곁이다.
// 숫자만 나열하지 않고 한 문장으로 먼저 말하는 것은 F3의 "차트가 아니라 문장이 먼저"와 같은 이유다.
@Composable
fun DDMonthlySummaryCard(
    summary: MonthlySummary,
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
            Text("이번 달", style = MaterialTheme.typography.titleMedium)

            if (summary.isEmpty) {
                // 0잔을 "0"이라는 숫자로 보여주면 실패한 성적표가 된다. 이 달은 아직 안 끝났다.
                Text(
                    text = "이번 달은 아직 기록이 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            Text(
                text = summarySentence(summary),
                style = MaterialTheme.typography.bodyLarge,
            )

            TypeBar(summary)

            Row(horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xl)) {
                summary.averageRating?.let { Stat("평균 만족도", DrinkLabels.rating(it)) }
                if (summary.repurchaseCount > 0) {
                    Stat("또 살래요", "${summary.repurchaseCount}잔")
                }
            }

            summary.topRecord?.let { top ->
                TopRecord(name = top.name, rating = DrinkLabels.rating(top.rating), imageUri = top.imageUri)
            }
        }
    }
}

private fun summarySentence(summary: MonthlySummary): String {
    val parts = summary.byType.joinToString(" · ") { "${DrinkLabels.drinkType(it.type)} ${it.count}잔" }
    return "${summary.total}잔 마셨어요. $parts"
}

// 주종 비율. 축도 눈금도 없다 — 읽을 것은 "어느 쪽을 더 마셨나" 하나뿐이라
// 막대 하나면 충분하고, 그 이상은 입문자에게 해석 부담만 준다(prd.md S2).
@Composable
private fun TypeBar(summary: MonthlySummary) {
    if (summary.byType.size < 2) return

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .clearAndSetSemantics { },
    ) {
        summary.byType.forEach { slice ->
            Box(
                modifier =
                    Modifier
                        .weight(slice.count.toFloat())
                        .fillMaxWidth()
                        .background(typeColor(slice.type)),
            )
        }
    }
}

@Composable
private fun typeColor(type: DrinkType): Color =
    when (type) {
        DrinkType.Wine -> MaterialTheme.colorScheme.tertiary
        DrinkType.Whiskey -> MaterialTheme.colorScheme.secondary
    }

@Composable
private fun Stat(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xxs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

// 이미지가 붙는 자리는 여기다. **취향 유형 카드가 아니다** — 유형은 81가지 계산 결과이고
// 특정 병의 사진을 얹으면 "그 술이 곧 당신의 유형"으로 읽힌다. 반면 이 자리는
// 실제로 그 한 잔을 가리키므로 사진이 그 잔의 것이 맞다.
@Composable
private fun TopRecord(
    name: String,
    rating: String,
    imageUri: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (imageUri != null) {
            DDUriImage(
                imageUri = imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.small),
            )
        }
        Column(
            modifier =
                Modifier.semantics {
                    contentDescription = "가장 높게 준 한 잔 $name $rating"
                },
            verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xxs),
        ) {
            Text(
                text = "가장 높게 준 한 잔",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("$name · $rating", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
