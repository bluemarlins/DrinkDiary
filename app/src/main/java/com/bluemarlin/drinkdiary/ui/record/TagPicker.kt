package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.bluemarlin.drinkdiary.ui.component.DDTagChipGroup

// 라벨을 보고 고르는 선택 태그(PRD F2-1). 전부 선택이므로 기본 경로의 탭 예산에 들어가지 않는다 —
// 이 화면은 "더 남기기"를 편 사용자만 본다.
//
// 한 번 더 누르면 해제된다. 잘못 고른 값을 되돌릴 방법이 없으면 사용자는 아예 안 고른다.
// 첫 화면이 물은 것을 뺀, 이 주종에서 남은 태그.
fun remainingTags(type: DrinkType): List<TagCategory> = TagCategory.of(type).filterNot { it in promotedTags(type) }

@Composable
fun TagPicker(
    type: DrinkType,
    tags: DrinkTags,
    categories: List<TagCategory>,
    onTagsChange: (DrinkTags) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = "라벨에 있는 것들 (선택)",
) {
    if (categories.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        title?.let { Text(it, style = MaterialTheme.typography.titleMedium) }

        categories.forEach { category ->
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
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DDTagChipGroup(
            options = options,
            selected = selected,
            // 같은 값을 다시 누르면 해제. 해제 규칙은 축마다 다르므로 컴포넌트가 아니라 여기서 정한다.
            onSelect = { value -> onSelect(if (selected == value) null else value) },
            label = { value -> DrinkLabels.tagValue(category, value, type) },
        )
    }
}

private fun optionsOf(category: TagCategory): List<String> =
    when (category) {
        TagCategory.WhiskyStyle -> WhiskyStyle.entries.map { it.name }
        TagCategory.Peat -> PeatTag.entries.map { it.name }
        TagCategory.WineColor -> WineColor.entries.map { it.name }
        TagCategory.AbvBand -> AbvBand.entries.map { it.name }
        TagCategory.Origin -> Origin.entries.map { it.name }
        // 사전이 채우는 값은 TagCategory.of()가 걸러내므로 여기 오지 않는다.
        // 그 필터가 깨지면 조용히 빈 화면이 되는 대신 여기서 터진다.
        TagCategory.Cask, TagCategory.WineStyle -> error("사전이 채우는 값은 사용자에게 묻지 않는다: $category")
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
        TagCategory.Cask, TagCategory.WineStyle -> error("사전이 채우는 값은 기록에 쓰지 않는다: $category")
    }
