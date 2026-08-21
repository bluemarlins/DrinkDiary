package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.WhiskyStyle
import com.bluemarlin.drinkdiary.domain.model.WineColor
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// 첫 질문 하나로 주종과 분류를 함께 받는다.
//
// 이 분류는 감각이 아니라 라벨·잔에서 바로 나오는 사실이고, 우리 측정에서 감각 축보다 술을
// 잘 나눴다(departments/planner/axis-validation-2026-08.md). 그래서 선택 태그가 아니라
// 기본 경로에 둔다.
//
// **탭은 늘지 않는다.** 원래도 주종을 고르는 화면이었고, 선택지만 세분화한 것이다 —
// F2의 5탭 예산(주종 1 + 공통 축 4)이 그대로 유지된다.
data class DrinkChoice(
    val type: DrinkType,
    val label: String,
    val tags: DrinkTags,
)

// 첫 화면이 이미 물은 태그. TagPicker는 이걸 빼고 보여준다 — 아니면 같은 것을 두 번 묻게 된다.
fun promotedTags(type: DrinkType): Set<TagCategory> =
    when (type) {
        DrinkType.Wine -> setOf(TagCategory.WineColor, TagCategory.Origin)
        DrinkType.Whiskey -> setOf(TagCategory.WhiskyStyle, TagCategory.Origin)
    }

private val wineChoices =
    WineColor.entries.map { color ->
        DrinkChoice(
            type = DrinkType.Wine,
            label =
                when (color) {
                    WineColor.Red -> "레드"
                    WineColor.White -> "화이트"
                    WineColor.Sparkling -> "스파클링"
                    WineColor.Natural -> "내추럴"
                    WineColor.Port -> "포트"
                    WineColor.Other -> "그 외"
                },
            tags = DrinkTags(wineColor = color),
        )
    }

private val whiskeyChoices =
    WhiskyStyle.entries.map { style ->
        DrinkChoice(
            type = DrinkType.Whiskey,
            label =
                when (style) {
                    WhiskyStyle.SingleMalt -> "싱글몰트"
                    WhiskyStyle.BlendedMalt -> "블렌디드 몰트"
                    WhiskyStyle.Blended -> "블렌디드"
                    WhiskyStyle.Bourbon -> "버번"
                    WhiskyStyle.Rye -> "라이"
                    WhiskyStyle.Other -> "그 외"
                },
            tags = DrinkTags(whiskyStyle = style),
        )
    }

@Composable
fun DrinkPicker(
    onPick: (DrinkChoice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text("무엇을 마셨나요?", style = MaterialTheme.typography.titleLarge)

        ChoiceGroup(title = "와인", choices = wineChoices, onPick = onPick)
        ChoiceGroup(title = "위스키", choices = whiskeyChoices, onPick = onPick)
    }
}

@Composable
private fun ChoiceGroup(
    title: String,
    choices: List<DrinkChoice>,
    onPick: (DrinkChoice) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
            verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
        ) {
            choices.forEach { choice ->
                Card(
                    onClick = { onPick(choice) },
                    shape = MaterialTheme.shapes.medium,
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Text(
                        text = choice.label,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = DrinkDiarySpacing.xl, vertical = DrinkDiarySpacing.md),
                    )
                }
            }
        }
    }
}
