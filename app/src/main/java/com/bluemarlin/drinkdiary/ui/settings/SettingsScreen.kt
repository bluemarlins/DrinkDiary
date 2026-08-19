package com.bluemarlin.drinkdiary.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDScreenMargin
import com.bluemarlin.drinkdiary.ui.record.promotableTags
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onToggle: (TagCategory) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = contentPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(LocalDDScreenMargin.current),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs)) {
            Text("기록할 때 물어볼 것", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "켜면 기록할 때마다 바로 보이고, 끄면 '더 남기기' 안으로 들어가요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        promotableTags.forEach { (category, hint) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xxs),
                ) {
                    Text(DrinkLabels.tagCategory(category), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = category in state.alwaysAskTags,
                    onCheckedChange = { onToggle(category) },
                )
            }
        }

        HorizontalDivider()

        Text(
            // 정직하게 말한다. 지금 켜도 **이미 저장된 기록에는 그 답이 없다.**
            // 이 항목의 취향 판정은 앞으로 쌓이는 기록부터 시작한다 — 숨기면 사용자는
            // "왜 아직 아무것도 안 나오지"에서 앱이 고장난 줄 안다.
            text = "지금 켜도 이미 저장한 기록에는 그 답이 없어요. 이 항목의 취향은 앞으로 남기는 기록부터 쌓여요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
