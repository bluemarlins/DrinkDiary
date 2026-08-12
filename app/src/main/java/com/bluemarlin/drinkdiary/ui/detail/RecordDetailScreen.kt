package com.bluemarlin.drinkdiary.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.domain.model.ratingCriteria
import com.bluemarlin.drinkdiary.ui.component.DDCollectionStatusBadge
import com.bluemarlin.drinkdiary.ui.component.DDConfirmDialog
import com.bluemarlin.drinkdiary.ui.component.DDDestructiveButton
import com.bluemarlin.drinkdiary.ui.component.DDDrinkTypeBadge
import com.bluemarlin.drinkdiary.ui.component.DDErrorContent
import com.bluemarlin.drinkdiary.ui.component.DDInfoRow
import com.bluemarlin.drinkdiary.ui.component.DDLoadingContent
import com.bluemarlin.drinkdiary.ui.component.DDPrimaryButton
import com.bluemarlin.drinkdiary.ui.component.DDRatingBreakdownRadarChart
import com.bluemarlin.drinkdiary.ui.component.DDRatingValueText
import com.bluemarlin.drinkdiary.ui.component.DDRecordHeroImage
import com.bluemarlin.drinkdiary.ui.component.formatPrice
import com.bluemarlin.drinkdiary.ui.component.formatRecordedDate
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType

@Composable
fun RecordDetailRoute(
    recordId: Long,
    viewModel: RecordDetailViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is RecordDetailEvent.Deleted) onBack()
        }
    }

    DDScreenScaffold(
        title = stringResource(R.string.detail_title),
        screenType = DDScreenType.Detail,
    ) { padding ->
        when (val uiState = state) {
            RecordDetailUiState.Loading -> DDLoadingContent(Modifier.padding(padding))
            RecordDetailUiState.NotFound ->
                DDErrorContent(
                    stringResource(R.string.detail_not_found),
                    modifier = Modifier.padding(padding),
                )
            is RecordDetailUiState.Error -> DDErrorContent(uiState.message, modifier = Modifier.padding(padding))
            is RecordDetailUiState.Success -> {
                val record = uiState.record
                BoxWithConstraints(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .consumeWindowInsets(padding)
                            .padding(16.dp),
                ) {
                    val twoPane = maxWidth >= 600.dp
                    if (twoPane) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            DDRecordHeroImage(
                                imageUri = record.imageUri,
                                modifier = Modifier.weight(0.9f).heightIn(max = 520.dp),
                            )
                            RecordDetailInfo(
                                name = record.name,
                                typeContent = {
                                    DDDrinkTypeBadge(record.type)
                                    DDCollectionStatusBadge(record.collectionStatus)
                                },
                                rating = { DDRatingValueText(record.rating) },
                                ratingDetails = {
                                    DDRatingBreakdownRadarChart(
                                        criteria = record.type.ratingCriteria(),
                                        breakdown = record.ratingBreakdown,
                                    )
                                },
                                price = formatPrice(record.price),
                                place = record.place ?: "-",
                                recordedAt = formatRecordedDate(record.recordedAtMillis),
                                tastingNote = record.tastingNote ?: "-",
                                onEdit = { onEdit(record.id) },
                                onDelete = { showDeleteDialog = true },
                                modifier = Modifier.weight(1f),
                                scrollable = true,
                            )
                        }
                    } else {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            DDRecordHeroImage(record.imageUri)
                            RecordDetailInfo(
                                name = record.name,
                                typeContent = {
                                    DDDrinkTypeBadge(record.type)
                                    DDCollectionStatusBadge(record.collectionStatus)
                                },
                                rating = { DDRatingValueText(record.rating) },
                                ratingDetails = {
                                    DDRatingBreakdownRadarChart(
                                        criteria = record.type.ratingCriteria(),
                                        breakdown = record.ratingBreakdown,
                                    )
                                },
                                price = formatPrice(record.price),
                                place = record.place ?: "-",
                                recordedAt = formatRecordedDate(record.recordedAtMillis),
                                tastingNote = record.tastingNote ?: "-",
                                onEdit = { onEdit(record.id) },
                                onDelete = { showDeleteDialog = true },
                                scrollable = false,
                            )
                        }
                    }
                }
                if (showDeleteDialog) {
                    DDConfirmDialog(
                        title = stringResource(R.string.detail_delete_confirm_title),
                        message = stringResource(R.string.detail_delete_confirm_message),
                        onConfirm = {
                            showDeleteDialog = false
                            viewModel.delete(recordId)
                        },
                        onDismiss = { showDeleteDialog = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordDetailInfo(
    name: String,
    typeContent: @Composable RowScope.() -> Unit,
    rating: @Composable () -> Unit,
    ratingDetails: @Composable () -> Unit,
    price: String,
    place: String,
    recordedAt: String,
    tastingNote: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
) {
    Column(
        modifier =
            modifier
                .widthIn(max = 560.dp)
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(name, style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = typeContent)
        Text(stringResource(R.string.detail_overall_rating), style = MaterialTheme.typography.titleMedium)
        rating()
        ratingDetails()
        DDInfoRow(stringResource(R.string.detail_price), price)
        DDInfoRow(stringResource(R.string.detail_place), place)
        DDInfoRow(stringResource(R.string.detail_recorded_at), recordedAt)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.detail_tasting_note), style = MaterialTheme.typography.titleMedium)
            Text(tastingNote, style = MaterialTheme.typography.bodyMedium)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DDPrimaryButton(stringResource(R.string.detail_edit), onClick = onEdit, modifier = Modifier.weight(1f))
            DDDestructiveButton(stringResource(R.string.delete), onClick = onDelete, modifier = Modifier.weight(1f))
        }
    }
}
