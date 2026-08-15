package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.AbvBand
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Origin
import com.bluemarlin.drinkdiary.domain.model.PeatTag
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.WhiskyStyle
import com.bluemarlin.drinkdiary.domain.model.WineColor
import com.bluemarlin.drinkdiary.ui.DrinkLabels

// 라벨을 보고 고르는 선택 태그(PRD F2-1). 전부 선택이므로 기본 경로의 탭 예산에 들어가지 않는다 —
// 이 화면은 "더 남기기"를 편 사용자만 본다.
//
// 한 번 더 누르면 해제된다. 잘못 고른 값을 되돌릴 방법이 없으면 사용자는 아예 안 고른다.
@Composable
fun TagPicker(
    type: DrinkType,
    tags: DrinkTags,
    onTagsChange: (DrinkTags) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("라벨에 있는 것들 (선택)", style = MaterialTheme.typography.titleSmall)

        TagCategory.of(type).forEach { category ->
            TagRow(
                label = DrinkLabels.tagCategory(category),
                options = optionsOf(category),
                selected = tags[category],
                onSelect = { value -> onTagsChange(tags.withTag(category, value)) },
                type = type,
                category = category,
            )
        }
    }
}

@Composable
private fun TagRow(
    label: String,
    options: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    type: DrinkType,
    category: TagCategory,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { value ->
                val isSelected = selected == value
                FilterChip(
                    selected = isSelected,
                    // 같은 값을 다시 누르면 해제.
                    onClick = { onSelect(if (isSelected) null else value) },
                    label = { Text(DrinkLabels.tagValue(category, value, type)) },
                )
            }
        }
    }
}

private fun optionsOf(category: TagCategory): List<String> =
    when (category) {
        TagCategory.WhiskyStyle -> WhiskyStyle.entries.map { it.name }
        TagCategory.Peat -> PeatTag.entries.map { it.name }
        TagCategory.WineColor -> WineColor.entries.map { it.name }
        TagCategory.AbvBand -> AbvBand.entries.map { it.name }
        TagCategory.Origin -> Origin.entries.map { it.name }
    }

private fun DrinkTags.withTag(
    category: TagCategory,
    value: String?,
): DrinkTags =
    when (category) {
        TagCategory.WhiskyStyle ->
            copy(
                whiskyStyle =
                    value?.let { name ->
                        WhiskyStyle.entries.find { it.name == name }
                    },
            )
        TagCategory.Peat -> copy(peat = value?.let { name -> PeatTag.entries.find { it.name == name } })
        TagCategory.WineColor -> copy(wineColor = value?.let { name -> WineColor.entries.find { it.name == name } })
        TagCategory.AbvBand -> copy(abvBand = value?.let { name -> AbvBand.entries.find { it.name == name } })
        TagCategory.Origin -> copy(origin = value?.let { name -> Origin.entries.find { it.name == name } })
    }
