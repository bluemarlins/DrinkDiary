package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemarlin.drinkdiary.ui.component.DDDrinkRecordCard
import com.bluemarlin.drinkdiary.ui.component.DDDrinkRecordGridCard
import com.bluemarlin.drinkdiary.ui.component.DDEmptyContent
import com.bluemarlin.drinkdiary.ui.navigation.DDBottomNavigationBarHeight
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDScreenMargin
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

@Composable
fun CollectionScreen(
    state: CollectionUiState,
    onOpen: (Long) -> Unit,
    onToggleSelect: (Long) -> Unit,
    onToggleViewMode: () -> Unit = {},
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    start = LocalDDScreenMargin.current,
                    top = contentPadding.calculateTopPadding(),
                    end = LocalDDScreenMargin.current,
                ),
    ) {
        // 아직 한 번도 읽지 않은 상태를 "기록이 없다"로 말하지 않는다 — 로딩과 빈 목록은 다르다.
        if (state.loaded && state.records.isEmpty()) {
            val filtered = state.filter != null
            DDEmptyContent(
                title = if (filtered) "이 주종으로 남긴 기록이 없어요" else "아직 기록이 없어요",
                description = if (filtered) "상단 메뉴에서 '전체'를 눌러보세요." else "오른쪽 아래 + 를 눌러 한 잔 기록해 보세요.",
                modifier = Modifier.fillMaxSize(),
            )
            return@Column
        }

        // Subheader Toolbar: Total Count + View Mode Switcher
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = DrinkDiarySpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "총 ${state.records.size}잔의 아카이브",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )

            Surface(
                onClick = onToggleViewMode,
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = if (state.viewMode == CollectionViewMode.Grid) "▦ 그리드" else "▤ 리스트",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        val bottomPadding = DDBottomNavigationBarHeight + LocalDDScreenMargin.current

        when (state.viewMode) {
            CollectionViewMode.Grid -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = bottomPadding),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.records, key = { it.id }) { record ->
                        DDDrinkRecordGridCard(
                            record = record,
                            onClick = {
                                if (state.selectionMode) onToggleSelect(record.id) else onOpen(record.id)
                            },
                            selected = record.id in state.selected,
                            onLongClick = { onToggleSelect(record.id) },
                        )
                    }
                }
            }

            CollectionViewMode.List -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = bottomPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.records, key = { it.id }) { record ->
                        DDDrinkRecordCard(
                            record = record,
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
    }
}
