package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.ui.component.DDAddRecordFab
import com.bluemarlin.drinkdiary.ui.component.DDCollectionStatusFilter
import com.bluemarlin.drinkdiary.ui.component.DDDrinkRecordListItem
import com.bluemarlin.drinkdiary.ui.component.DDDrinkTypeFilter
import com.bluemarlin.drinkdiary.ui.component.DDEmptyContent
import com.bluemarlin.drinkdiary.ui.component.DDErrorContent
import com.bluemarlin.drinkdiary.ui.component.DDLoadingContent
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold

@Composable
fun CollectionRoute(
    viewModel: CollectionViewModel,
    onAddRecord: () -> Unit,
    onOpenRecord: (Long) -> Unit,
    onDashboardClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()

    DDScreenScaffold(
        title = "컬렉션",
        selectedTab = "collection",
        showBannerAd = true,
        onDashboardClick = onDashboardClick,
        onCollectionClick = {},
        floatingActionButton = { DDAddRecordFab(onClick = onAddRecord) },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .padding(16.dp),
        ) {
            val expanded = maxWidth >= 840.dp
            val onResetFilters = {
                viewModel.selectType(null)
                viewModel.selectStatus(null)
            }
            if (expanded) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    FilterPanel(
                        selectedType = selectedType,
                        selectedStatus = selectedStatus,
                        onTypeSelected = viewModel::selectType,
                        onStatusSelected = viewModel::selectStatus,
                        modifier = Modifier.width(240.dp),
                    )
                    CollectionStateContent(
                        state = state,
                        onAddRecord = onAddRecord,
                        onOpenRecord = onOpenRecord,
                        onResetFilters = onResetFilters,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FilterPanel(
                        selectedType = selectedType,
                        selectedStatus = selectedStatus,
                        onTypeSelected = viewModel::selectType,
                        onStatusSelected = viewModel::selectStatus,
                    )
                    CollectionStateContent(
                        state = state,
                        onAddRecord = onAddRecord,
                        onOpenRecord = onOpenRecord,
                        onResetFilters = onResetFilters,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPanel(
    selectedType: DrinkType?,
    selectedStatus: CollectionStatus?,
    onTypeSelected: (DrinkType?) -> Unit,
    onStatusSelected: (CollectionStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("종류", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            DDDrinkTypeFilter(selected = selectedType, onSelected = onTypeSelected)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("상태", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            DDCollectionStatusFilter(selected = selectedStatus, onSelected = onStatusSelected)
        }
    }
}

@Composable
private fun CollectionStateContent(
    state: CollectionUiState,
    onAddRecord: () -> Unit,
    onOpenRecord: (Long) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        CollectionUiState.Loading -> DDLoadingContent(modifier)
        is CollectionUiState.Empty -> if (state.filtered) {
            DDEmptyContent(
                message = "필터 조건에 맞는 기록이 없습니다.",
                actionText = "필터 초기화",
                onAction = onResetFilters,
                modifier = modifier,
                secondaryActionText = "기록 추가",
                onSecondaryAction = onAddRecord,
            )
        } else {
            DDEmptyContent(
                message = "아직 기록이 없습니다.",
                actionText = "기록 추가",
                onAction = onAddRecord,
                modifier = modifier,
            )
        }
        is CollectionUiState.Error -> DDErrorContent(state.message, modifier = modifier)
        is CollectionUiState.Success -> DrinkRecordList(
            records = state.records,
            onOpenRecord = onOpenRecord,
            modifier = modifier,
        )
    }
}

@Composable
private fun DrinkRecordList(
    records: List<DrinkRecord>,
    onOpenRecord: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
    ) {
        items(records, key = { it.id }) { record ->
            DDDrinkRecordListItem(record = record, onClick = { onOpenRecord(record.id) })
        }
    }
}
