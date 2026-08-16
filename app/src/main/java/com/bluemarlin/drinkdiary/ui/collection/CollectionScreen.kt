package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDSemanticBadge
import com.bluemarlin.drinkdiary.ui.component.DDUriImage
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

@Composable
fun CollectionScreen(
    state: CollectionUiState,
    onFilterChange: (DrinkType?) -> Unit,
    onOpen: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(contentPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = DrinkDiarySpacing.lg, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(null to "전체", DrinkType.Wine to "와인", DrinkType.Whiskey to "위스키")
                .forEach { (type, label) ->
                    FilterChip(
                        selected = state.filter == type,
                        onClick = { onFilterChange(type) },
                        label = { Text(label) },
                    )
                }
        }

        // 아직 한 번도 읽지 않은 상태를 "기록이 없다"로 말하지 않는다 — 로딩과 빈 목록은 다르다.
        if (state.loaded && state.records.isEmpty()) {
            EmptyCollection(filtered = state.filter != null)
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    start = DrinkDiarySpacing.lg,
                    end = DrinkDiarySpacing.lg,
                    bottom = 96.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.records, key = { it.id }) { record ->
                RecordRow(record = record, onClick = { onOpen(record.id) })
            }
        }
    }
}

// 비선호를 오류 색으로 칠하지 않는다. 잘못된 상태가 아니라 **취향이 아니었다**는 뜻이고,
// 부정 신호는 눈에 띄기만 하면 되지 소리칠 필요가 없다.
@Composable
private fun statusContainerColor(status: CollectionStatus) =
    when (status) {
        CollectionStatus.Repurchase -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

@Composable
private fun statusContentColor(status: CollectionStatus) =
    when (status) {
        CollectionStatus.Repurchase -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

@Composable
private fun EmptyCollection(filtered: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = DrinkDiarySpacing.lg),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (filtered) "이 주종으로 남긴 기록이 없어요" else "아직 기록이 없어요",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = if (filtered) "위에서 '전체'를 눌러보세요." else "오른쪽 아래 + 를 눌러 한 잔 기록해 보세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RecordRow(
    record: DrinkRecord,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            record.imageUri?.let {
                DDUriImage(
                    imageUri = it,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = record.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Text(
                    text = DrinkLabels.subtitle(record),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )

                // 이 줄이 있어야 목록을 훑어서 "다음에 뭘 살까"를 판단할 수 있다.
                //
                // 이전에는 재구매만 `★` 한 글자로 표시하고 비선호는 **아무것도 그리지 않았다.**
                // 그러면 빈칸이 "그냥 그래요"와 "안 맞아요"를 같은 것으로 만든다 — 구매 판단에서
                // 가장 중요한 신호가 사라진다. 같은 축은 같은 방식으로 적는다.
                //
                // '그냥 그래요'는 여전히 안 그린다. 모든 행에 붙으면 소음이고, 아무 표시가 없는 것이
                // 그 자체로 "특별할 것 없었다"는 뜻으로 읽힌다.
                if (record.collectionStatus != CollectionStatus.Normal) {
                    DDSemanticBadge(
                        text = DrinkLabels.collectionStatus(record.collectionStatus),
                        containerColor = statusContainerColor(record.collectionStatus),
                        contentColor = statusContentColor(record.collectionStatus),
                    )
                }
            }

            Text(
                text = DrinkLabels.rating(record.rating),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
