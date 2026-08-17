package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

@Immutable
data class DDHighlightCard(
    val id: Long,
    // "가장 높게 준" 같은 짧은 명사구. 어미를 갖지 않는다(branding.md 2-3).
    val label: String,
    val name: String,
    // 점수나 횟수처럼 그 카드를 한정하는 한 조각.
    val detail: String,
    val imageUri: String?,
)

// 대시보드의 사진 층(prd.md F3-4 (a)). **취향 유형 바로 아래**에 온다 — 결론이 주인공이라는
// F3-1은 그대로다.
//
// 제목 줄을 두지 않았다. 카드마다 자기 레이블이 붙어 있어서 위에 한 줄을 더 얹으면
// 그것이 곧 이 절이 없애려던 텍스트다(F3-4 (c)).
@Composable
fun DDDrinkHighlightRow(
    cards: List<DDHighlightCard>,
    modifier: Modifier = Modifier,
) {
    if (cards.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
        // 카드가 화면 가장자리에 붙지 않게 하면서, 스크롤은 가장자리까지 나가게 한다.
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(cards, key = { "${it.label}-${it.id}" }) { card -> HighlightCard(card) }
    }
}

@Composable
private fun HighlightCard(card: DDHighlightCard) {
    Column(
        modifier = Modifier.width(CARD_WIDTH),
        verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    // 기록 폼과 같은 세로 비율이다. 찍을 때 본 모양과 볼 때 보는 모양이
                    // 다르면 같은 사진으로 읽히지 않는다.
                    .aspectRatio(BOTTLE_ASPECT)
                    .clip(MaterialTheme.shapes.large),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = MaterialTheme.shapes.large,
        ) {
            // 사진이 없어도 카드는 남는다. 사진 있는 것만 골라 보여주면 화면은 예뻐지지만
            // "가장 높게 준 잔"이 사실이 아니게 된다.
            if (card.imageUri != null) {
                DDUriImage(
                    imageUri = card.imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Column(
                    modifier = Modifier.padding(DrinkDiarySpacing.sm),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = "사진 없음",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Text(
            text = card.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = card.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = card.detail,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val CARD_WIDTH = 132.dp

private const val BOTTLE_ASPECT = 4f / 5f
