package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
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
        // 사진은 붙어야 **한 묶음**으로 읽힌다. 떨어지면 카드 여러 장이 된다(design-system.md 5.3).
        // 새 토큰을 만들지 않고 기존 스케일에서 한 칸 내렸다(sm 12dp → xs 8dp).
        horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs),
        // 바깥 여백은 이 목록을 놓는 화면이 이미 갖고 있다. 여기서 또 주면 이중이 된다.
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
                    // 반경은 표면 크기를 따른다(design-system.md 3.3절). 132dp 타일에 18dp는
                    // 폭의 13.6%라 216dp짜리 `DDPhotoField`(8.3%)와 눈에는 다른 모양이 된다.
                    .clip(MaterialTheme.shapes.medium),
            color = MaterialTheme.colorScheme.surfaceVariant,
            // 사진이 채워지면 테두리를 걷는다(design-system.md 2절 6번). 사진 없는 카드에만
            // 남는다 — 그 카드는 테두리가 없으면 배경과 구분되지 않는다.
            border =
                if (card.imageUri == null) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                } else {
                    null
                },
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 사진이 없어도 카드는 남는다. 사진 있는 것만 골라 보여주면 화면은 예뻐지지만
                // "가장 높게 준 잔"이 사실이 아니게 된다.
                if (card.imageUri != null) {
                    DDUriImage(
                        imageUri = card.imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text = "사진 없음",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                // **레이블은 사진 아래가 아니라 사진 위로**(design-system.md 5.3).
                // 아래에 쌓는 줄 수만큼 사진이 짧아지고, 이 자리는 글자보다 사진이 먼저다.
                // 올리는 것은 **한 조각뿐**이다 — 이름·상세까지 얹으면 사진을 앞세우려고
                // 사진을 가리는 꼴이 된다.
                OverlayPill(
                    text = card.label,
                    modifier =
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(DrinkDiarySpacing.xs),
                )
            }
        }

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

// 사진 위에 얹는 칩. **배경은 불투명이어야 한다**(design-system.md 2절 6번) — 반투명이면
// 배경이 사진마다 달라져 2절이 요구하는 AA(4.5:1) 대비를 보장할 수 없다.
//
// 카드가 132dp로 좁아 긴 레이블은 넘친다. 아래로 되돌리지 않고 **말줄임**으로 자른다 —
// 되돌리면 이 변경이 없애려던 세 번째 줄이 레이블 길이에 따라 다시 생겨서, 같은 목록의
// 카드들이 서로 다른 높이를 갖게 된다.
@Composable
private fun OverlayPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier.padding(
                    horizontal = DrinkDiarySpacing.xs,
                    vertical = DrinkDiarySpacing.xxs,
                ),
        )
    }
}

private val CARD_WIDTH = 132.dp

private const val BOTTLE_ASPECT = 4f / 5f
