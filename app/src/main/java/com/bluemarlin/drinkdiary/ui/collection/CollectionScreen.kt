package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 아직 한 번도 읽지 않은 상태를 "기록이 없다"로 말하지 않는다 — 로딩과 빈 목록은 다르다.
        if (state.loaded && state.records.isEmpty()) {
            val filtered = state.filter != null
            DDEmptyContent(
                title = if (filtered) "이 주종으로 남긴 기록이 없어요" else "아직 기록이 없어요",
                description = if (filtered) "상단 메뉴에서 '전체'를 눌러보세요." else "오른쪽 아래 + 를 눌러 한 잔 기록해 보세요.",
                modifier = Modifier.fillMaxSize(),
            )
            return@Box
        }

        val scrollPadding =
            PaddingValues(
                start = LocalDDScreenMargin.current,
                top = contentPadding.calculateTopPadding() + DrinkDiarySpacing.xs,
                end = LocalDDScreenMargin.current,
                bottom = DDBottomNavigationBarHeight + LocalDDScreenMargin.current,
            )

        when (state.viewMode) {
            CollectionViewMode.Grid -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = scrollPadding,
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
                    contentPadding = scrollPadding,
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
