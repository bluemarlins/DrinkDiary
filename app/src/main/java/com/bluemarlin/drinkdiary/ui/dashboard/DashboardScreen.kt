package com.bluemarlin.drinkdiary.ui.dashboard

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import com.bluemarlin.drinkdiary.domain.model.DashboardSummary
import com.bluemarlin.drinkdiary.ui.component.DDAddRecordFab
import com.bluemarlin.drinkdiary.ui.component.DDDashboardSummaryCard
import com.bluemarlin.drinkdiary.ui.component.DDDrinkTypeRatioCard
import com.bluemarlin.drinkdiary.ui.component.DDDrinkRecordCard
import com.bluemarlin.drinkdiary.ui.component.DDEmptyContent
import com.bluemarlin.drinkdiary.ui.component.DDErrorContent
import com.bluemarlin.drinkdiary.ui.component.DDLoadingContent
import com.bluemarlin.drinkdiary.ui.component.DDPeriodSegmentedControl
import com.bluemarlin.drinkdiary.ui.component.DDStatusSummaryCard
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDTopLevelTab

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    onAddRecord: () -> Unit,
    onOpenRecord: (Long) -> Unit,
    onOpenStatus: (CollectionStatus) -> Unit,
    onCollectionClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val period by viewModel.selectedPeriod.collectAsState()

    DDScreenScaffold(
        title = "대시보드",
        screenType = DDScreenType.TopLevel,
        selectedTab = DDTopLevelTab.Dashboard,
        onDashboardClick = {},
        onCollectionClick = onCollectionClick,
        onSearchClick = onSearchClick,
        floatingActionButton = { DDAddRecordFab(onClick = onAddRecord) },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .padding(16.dp),
        ) {
            val contentMaxWidth = if (maxWidth >= 840.dp) 1100.dp else maxWidth
            val expanded = maxWidth >= 840.dp
            Column(
                modifier = Modifier.fillMaxSize().widthIn(max = contentMaxWidth),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DDPeriodSegmentedControl(selected = period, onSelected = viewModel::selectPeriod)
                when (val uiState = state) {
                    DashboardUiState.Loading -> DDLoadingContent()
                    DashboardUiState.Empty -> DDEmptyContent("선택한 기간에 기록이 없습니다.", "기록 추가", onAddRecord)
                    is DashboardUiState.Error -> DDErrorContent(uiState.message)
                    is DashboardUiState.Success -> DashboardSuccessContent(
                        summary = uiState.summary,
                        expanded = expanded,
                        onOpenRecord = onOpenRecord,
                        onOpenStatus = onOpenStatus,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSuccessContent(
    summary: DashboardSummary,
    expanded: Boolean,
    onOpenRecord: (Long) -> Unit,
    onOpenStatus: (CollectionStatus) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            if (expanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    DDDashboardSummaryCard("기록 수", "${summary.totalCount}개", Modifier.weight(1f))
                    DDDashboardSummaryCard("평균 별점", summary.averageRating?.let { "%.1f".format(it) } ?: "-", Modifier.weight(1f))
                    DDStatusSummaryCard(CollectionStatus.Repurchase, summary.repurchaseCount, Modifier.weight(1f)) {
                        onOpenStatus(CollectionStatus.Repurchase)
                    }
                    DDStatusSummaryCard(CollectionStatus.NotForMe, summary.notForMeCount, Modifier.weight(1f)) {
                        onOpenStatus(CollectionStatus.NotForMe)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        DDDashboardSummaryCard("기록 수", "${summary.totalCount}개", Modifier.weight(1f))
                        DDDashboardSummaryCard("평균 별점", summary.averageRating?.let { "%.1f".format(it) } ?: "-", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        DDStatusSummaryCard(CollectionStatus.Repurchase, summary.repurchaseCount, Modifier.weight(1f)) {
                            onOpenStatus(CollectionStatus.Repurchase)
                        }
                        DDStatusSummaryCard(CollectionStatus.NotForMe, summary.notForMeCount, Modifier.weight(1f)) {
                            onOpenStatus(CollectionStatus.NotForMe)
                        }
                    }
                }
            }
        }
        item {
            DDDrinkTypeRatioCard(
                wineCount = summary.wineCount,
                whiskeyCount = summary.whiskeyCount,
                beerCount = summary.beerCount,
                totalCount = summary.totalCount,
            )
        }
        if (summary.repurchaseRecords.isNotEmpty()) {
            item { Text("재구매 후보", style = MaterialTheme.typography.titleMedium) }
            items(summary.repurchaseRecords.take(5), key = { it.id }) { record ->
                DDDrinkRecordCard(record = record, onClick = { onOpenRecord(record.id) })
            }
        }
        if (summary.notForMeRecords.isNotEmpty()) {
            item { Text("비선호", style = MaterialTheme.typography.titleMedium) }
            items(summary.notForMeRecords.take(5), key = { it.id }) { record ->
                DDDrinkRecordCard(record = record, onClick = { onOpenRecord(record.id) })
            }
        }
    }
}
