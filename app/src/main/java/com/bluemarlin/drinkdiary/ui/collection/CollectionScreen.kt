package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType
import com.bluemarlin.drinkdiary.ui.navigation.DDTopLevelTab

private val TopLevelBottomContentPadding = 112.dp

@Composable
fun CollectionRoute(
    viewModel: CollectionViewModel,
    onAddRecord: () -> Unit,
    onOpenRecord: (Long) -> Unit,
    onDashboardClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()

    DDScreenScaffold(
        title = "컬렉션",
        screenType = DDScreenType.TopLevel,
        selectedTab = DDTopLevelTab.Collection,
        onDashboardClick = onDashboardClick,
        onCollectionClick = {},
        onSearchClick = onSearchClick,
        floatingActionButton = { DDAddRecordFab(onClick = onAddRecord) },
    ) { padding ->
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .padding(16.dp),
        ) {
            val expanded = maxWidth >= 840.dp
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
        DDDrinkTypeFilter(selected = selectedType, onSelected = onTypeSelected)
        DDCollectionStatusFilter(selected = selectedStatus, onSelected = onStatusSelected)
    }
}

@Composable
private fun CollectionStateContent(
    state: CollectionUiState,
    onAddRecord: () -> Unit,
    onOpenRecord: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        CollectionUiState.Loading -> DDLoadingContent(modifier)
        is CollectionUiState.Empty ->
            DDEmptyContent(
                message = if (state.filtered) "필터 조건에 맞는 기록이 없습니다." else "아직 기록이 없습니다.",
                actionText = "기록 추가",
                onAction = onAddRecord,
                modifier = modifier,
            )
        is CollectionUiState.Error -> DDErrorContent(state.message, modifier = modifier)
        is CollectionUiState.Success ->
            DrinkRecordList(
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
        contentPadding = PaddingValues(bottom = TopLevelBottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(records, key = { it.id }) { record ->
            DDDrinkRecordListItem(record = record, onClick = { onOpenRecord(record.id) })
        }
    }
}
