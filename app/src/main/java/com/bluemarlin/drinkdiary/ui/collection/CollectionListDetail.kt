package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bluemarlin.drinkdiary.ui.component.DDEmptyContent

// 명세 4절 마지막 열 — Medium의 "2-Column 분할 (좌 40% : 우 60%)"과 Expanded의 "List-Detail".
// 폭이 있으면 목록에서 상세로 **나갈** 이유가 없다. 매장에서 여러 기록을 훑어 비교하는 것이
// 이 화면의 용도라, 한 건 볼 때마다 목록을 떠났다 돌아오면 훑던 자리를 잃는다.
//
// 목록은 `CollectionScreen`을 그대로 쓴다. 두 벌을 만들면 필터 규칙이 한쪽에만 남는다.
@Composable
fun CollectionListDetail(
    state: CollectionUiState,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onToggleSelect: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize()) {
        CollectionScreen(
            state = state,
            onOpen = onSelect,
            onToggleSelect = onToggleSelect,
            contentPadding = contentPadding,
            modifier = Modifier.weight(0.4f),
        )

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        val record = state.records.firstOrNull { it.id == selectedId }
        if (record != null) {
            RecordDetailScreen(
                record = record,
                onEdit = { onEdit(record.id) },
                onDelete = { onDelete(record.id) },
                contentPadding = contentPadding,
                modifier = Modifier.weight(0.6f),
            )
        } else {
            // 오른쪽을 비워두면 고장난 것으로 읽힌다. 무엇을 하면 되는지 적는다.
            Box(
                modifier = Modifier.weight(0.6f).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                DDEmptyContent(
                    title = "기록을 고르세요",
                    description = "왼쪽에서 기록을 누르면 여기에 보여요.",
                )
            }
        }
    }
}
