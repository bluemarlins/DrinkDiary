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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
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
    onSettingsClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val period by viewModel.selectedPeriod.collectAsState()

    DDScreenScaffold(
        title = stringResource(R.string.dashboard_title),
        screenType = DDScreenType.TopLevel,
        selectedTab = DDTopLevelTab.Dashboard,
        onDashboardClick = {},
        onCollectionClick = onCollectionClick,
        onSearchClick = onSearchClick,
        toolbarActions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
            }
        },
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
                    DashboardUiState.Empty ->
                        DDEmptyContent(
                            stringResource(R.string.dashboard_empty_message),
                            stringResource(R.string.dashboard_add_record),
                            onAddRecord,
                        )
                    is DashboardUiState.Error -> DDErrorContent(stringResource(uiState.messageRes))
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
                text = stringResource(R.string.dashboard_view_insights),
                onClick = onOpenInsights,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (summary.normalRecords.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.dashboard_normal_records),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(summary.normalRecords.take(5), key = { it.id }) { record ->
                DDDrinkRecordCard(record = record, onClick = { onOpenRecord(record.id) })
            }
        }
        if (summary.repurchaseRecords.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.dashboard_repurchase_candidates),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(summary.repurchaseRecords.take(5), key = { it.id }) { record ->
                DDDrinkRecordCard(record = record, onClick = { onOpenRecord(record.id) })
            }
        }
        if (summary.notForMeRecords.isNotEmpty()) {
            item { Text(stringResource(R.string.dashboard_not_for_me), style = MaterialTheme.typography.titleMedium) }
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
    val averageSpentText =
        summary.averageSpent?.let { stringResource(R.string.dashboard_average_price_format, formatPrice(it)) }
            ?: stringResource(R.string.dashboard_no_price_entered)
    if (expanded) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardMetricTiles(summary, averageRatingText, averageSpentText, onOpenStatus)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DDDashboardMetricTile(
                    title = stringResource(R.string.dashboard_metric_record_count),
                    value = stringResource(R.string.dashboard_metric_record_unit, summary.totalCount),
                    supportingText = stringResource(R.string.dashboard_metric_total_supporting),
                    modifier = Modifier.weight(1f),
                )
                DDDashboardMetricTile(
                    title = stringResource(R.string.dashboard_metric_total_spent),
                    value = formatPrice(summary.totalSpent),
                    supportingText =
                        stringResource(
                            R.string.dashboard_metric_supporting_format,
                            averageSpentText,
                            summary.pricedRecordCount,
                        ),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DDDashboardMetricTile(
                    title = stringResource(R.string.dashboard_metric_average_rating),
                    value = averageRatingText,
                    supportingText = stringResource(R.string.dashboard_metric_rating_unit),
                    modifier = Modifier.weight(1f),
                )
                DDDashboardMetricTile(
                    title = stringResource(R.string.dashboard_repurchase_candidates),
                    value = stringResource(R.string.dashboard_metric_record_unit, summary.repurchaseCount),
                    supportingText = stringResource(R.string.dashboard_metric_repurchase_supporting),
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
        title = stringResource(R.string.dashboard_metric_record_count),
        value = stringResource(R.string.dashboard_metric_record_unit, summary.totalCount),
        supportingText = stringResource(R.string.dashboard_metric_total_supporting),
        modifier = Modifier.weight(1f),
    )
    DDDashboardMetricTile(
        title = stringResource(R.string.dashboard_metric_total_spent),
        value = formatPrice(summary.totalSpent),
        supportingText =
            stringResource(
                R.string.dashboard_metric_supporting_format,
                averageSpentText,
                summary.pricedRecordCount,
            ),
        modifier = Modifier.weight(1f),
    )
    DDDashboardMetricTile(
        title = stringResource(R.string.dashboard_metric_average_rating),
        value = averageRatingText,
        supportingText = stringResource(R.string.dashboard_metric_rating_unit),
        modifier = Modifier.weight(1f),
    )
    DDDashboardMetricTile(
        title = stringResource(R.string.dashboard_repurchase_candidates),
        value = stringResource(R.string.dashboard_metric_record_unit, summary.repurchaseCount),
        supportingText = stringResource(R.string.dashboard_metric_repurchase_supporting),
        modifier = Modifier.weight(1f),
        onClick = { onOpenStatus(CollectionStatus.Repurchase) },
    )
}
