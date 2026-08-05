package com.bluemarlin.drinkdiary.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import com.bluemarlin.drinkdiary.ui.component.DDContainedButton
import com.bluemarlin.drinkdiary.ui.component.DDDashboardMetricTile
import com.bluemarlin.drinkdiary.ui.component.DDDrinkRecordCard
import com.bluemarlin.drinkdiary.ui.component.DDDrinkTypeDonutCard
import com.bluemarlin.drinkdiary.ui.component.DDEmptyContent
import com.bluemarlin.drinkdiary.ui.component.DDErrorContent
import com.bluemarlin.drinkdiary.ui.component.DDLoadingContent
import com.bluemarlin.drinkdiary.ui.component.DDPeriodSegmentedControl
import com.bluemarlin.drinkdiary.ui.component.formatPrice
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType
import com.bluemarlin.drinkdiary.ui.navigation.DDTopLevelTab

private val TopLevelBottomContentPadding = 112.dp

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    onAddRecord: () -> Unit,
    onOpenRecord: (Long) -> Unit,
    onOpenStatus: (CollectionStatus) -> Unit,
    onCollectionClick: () -> Unit,
    onSearchClick: () -> Unit,
    onOpenInsights: () -> Unit,
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
            modifier =
                Modifier
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
                    is DashboardUiState.Success ->
                        DashboardSuccessContent(
                            summary = uiState.summary,
                            expanded = expanded,
                            onOpenRecord = onOpenRecord,
                            onOpenStatus = onOpenStatus,
                            onOpenInsights = onOpenInsights,
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
    onOpenInsights: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = TopLevelBottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            DashboardMetricGrid(
                summary = summary,
                expanded = expanded,
                onOpenStatus = onOpenStatus,
            )
        }
        item {
            DDDrinkTypeDonutCard(
                wineCount = summary.wineCount,
                whiskeyCount = summary.whiskeyCount,
                beerCount = summary.beerCount,
                totalCount = summary.totalCount,
            )
        }
        item {
            DDContainedButton(
                text = "고급 인사이트 보기",
                onClick = onOpenInsights,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (summary.normalRecords.isNotEmpty()) {
            item { Text("일반 기록", style = MaterialTheme.typography.titleMedium) }
            items(summary.normalRecords.take(5), key = { it.id }) { record ->
                DDDrinkRecordCard(record = record, onClick = { onOpenRecord(record.id) })
            }
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

@Composable
private fun DashboardMetricGrid(
    summary: DashboardSummary,
    expanded: Boolean,
    onOpenStatus: (CollectionStatus) -> Unit,
) {
    val averageRatingText = summary.averageRating?.let { "%.1f".format(it) } ?: "-"
    val averageSpentText = summary.averageSpent?.let { "평균 ${formatPrice(it)}" } ?: "가격 입력 기록 없음"
    if (expanded) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardMetricTiles(summary, averageRatingText, averageSpentText, onOpenStatus)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DDDashboardMetricTile(
                    title = "기록 수",
                    value = "${summary.totalCount}개",
                    supportingText = "선택 기간 전체 기록",
                    modifier = Modifier.weight(1f),
                )
                DDDashboardMetricTile(
                    title = "총 지출",
                    value = formatPrice(summary.totalSpent),
                    supportingText = "$averageSpentText · ${summary.pricedRecordCount}건",
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DDDashboardMetricTile(
                    title = "평균 별점",
                    value = averageRatingText,
                    supportingText = "5점 만점",
                    modifier = Modifier.weight(1f),
                )
                DDDashboardMetricTile(
                    title = "재구매 후보",
                    value = "${summary.repurchaseCount}개",
                    supportingText = "다시 마시고 싶은 기록",
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenStatus(CollectionStatus.Repurchase) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.DashboardMetricTiles(
    summary: DashboardSummary,
    averageRatingText: String,
    averageSpentText: String,
    onOpenStatus: (CollectionStatus) -> Unit,
) {
    DDDashboardMetricTile(
        title = "기록 수",
        value = "${summary.totalCount}개",
        supportingText = "선택 기간 전체 기록",
        modifier = Modifier.weight(1f),
    )
    DDDashboardMetricTile(
        title = "총 지출",
        value = formatPrice(summary.totalSpent),
        supportingText = "$averageSpentText · ${summary.pricedRecordCount}건",
        modifier = Modifier.weight(1f),
    )
    DDDashboardMetricTile(
        title = "평균 별점",
        value = averageRatingText,
        supportingText = "5점 만점",
        modifier = Modifier.weight(1f),
    )
    DDDashboardMetricTile(
        title = "재구매 후보",
        value = "${summary.repurchaseCount}개",
        supportingText = "다시 마시고 싶은 기록",
        modifier = Modifier.weight(1f),
        onClick = { onOpenStatus(CollectionStatus.Repurchase) },
    )
}
