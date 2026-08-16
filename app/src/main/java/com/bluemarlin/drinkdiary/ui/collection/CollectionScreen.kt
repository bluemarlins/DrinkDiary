package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.ui.component.DDChip
import com.bluemarlin.drinkdiary.ui.component.DDDrinkRecordCard
import com.bluemarlin.drinkdiary.ui.component.DDEmptyContent
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDScreenMargin

@Composable
fun CollectionScreen(
    state: CollectionUiState,
    onFilterChange: (DrinkType?) -> Unit,
    onOpen: (Long) -> Unit,
    onToggleSelect: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(contentPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = LocalDDScreenMargin.current, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(null to "전체", DrinkType.Wine to "와인", DrinkType.Whiskey to "위스키")
                .forEach { (type, label) ->
                    DDChip(
                        label = label,
                        selected = state.filter == type,
                        onClick = { onFilterChange(type) },
                    )
                }
        }

        // 아직 한 번도 읽지 않은 상태를 "기록이 없다"로 말하지 않는다 — 로딩과 빈 목록은 다르다.
        if (state.loaded && state.records.isEmpty()) {
            val filtered = state.filter != null
            DDEmptyContent(
                title = if (filtered) "이 주종으로 남긴 기록이 없어요" else "아직 기록이 없어요",
                description = if (filtered) "위에서 '전체'를 눌러보세요." else "오른쪽 아래 + 를 눌러 한 잔 기록해 보세요.",
                modifier = Modifier.fillMaxSize().padding(horizontal = LocalDDScreenMargin.current),
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    start = LocalDDScreenMargin.current,
                    end = LocalDDScreenMargin.current,
                    bottom = 96.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.records, key = { it.id }) { record ->
                DDDrinkRecordCard(
                    record = record,
                    // 선택 모드에서 탭은 선택 토글이다 — 상세로 나가면 고르던 것을 잃는다.
                    onClick = {
                        if (state.selectionMode) onToggleSelect(record.id) else onOpen(record.id)
                    },
                    selected = record.id in state.selected,
                    onLongClick = { onToggleSelect(record.id) },
                )
            }
        }
    }
}
