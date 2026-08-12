package com.bluemarlin.drinkdiary.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.bluemarlin.drinkdiary.ui.component.DDCollectionStatusSelector
import com.bluemarlin.drinkdiary.ui.component.DDDateTimeField
import com.bluemarlin.drinkdiary.ui.component.DDDrinkTypeSelector
import com.bluemarlin.drinkdiary.ui.component.DDFormSection
import com.bluemarlin.drinkdiary.ui.component.DDImagePicker
import com.bluemarlin.drinkdiary.ui.component.DDLoadingContent
import com.bluemarlin.drinkdiary.ui.component.DDMultilineTextField
import com.bluemarlin.drinkdiary.ui.component.DDNumberField
import com.bluemarlin.drinkdiary.ui.component.DDPrimaryButton
import com.bluemarlin.drinkdiary.ui.component.DDProUpgradeDialog
import com.bluemarlin.drinkdiary.ui.component.DDRatingInput
import com.bluemarlin.drinkdiary.ui.component.DDSecondaryButton
import com.bluemarlin.drinkdiary.ui.component.DDSensoryMetricSlider
import com.bluemarlin.drinkdiary.ui.component.DDTextField
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType

@Composable
fun RecordEditorRoute(
    viewModel: RecordEditorViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    onUpgradeClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val requestBack = {
        if (state.hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = !state.loading) {
        requestBack()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is RecordEditorEvent.Saved) onSaved(event.recordId)
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    DDScreenScaffold(
        title =
            if (state.input.id ==
                0L
            ) {
                stringResource(R.string.editor_title_new)
            } else {
                stringResource(R.string.editor_title_edit)
            },
        screenType = DDScreenType.Editor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.loading) {
            DDLoadingContent(Modifier.padding(padding))
        } else {
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
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .imePadding(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        DDFormSection(stringResource(R.string.editor_section_photo)) {
                            DDImagePicker(
                                imageUri = state.input.imageUri,
                                onImageSelected = viewModel::updateImageUri,
                                modifier = Modifier.widthIn(max = 360.dp),
                            )
                        }
                        RecordEditorForm(
                            state = state,
                            viewModel = viewModel,
                            onBack = requestBack,
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        )
                    }
                } else {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .imePadding()
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        RecordEditorForm(
                            state = state,
                            viewModel = viewModel,
                            onBack = requestBack,
                        )
                        DDFormSection(stringResource(R.string.editor_section_photo)) {
                            DDImagePicker(state.input.imageUri, viewModel::updateImageUri)
                        }
                    }
                }
            }
        }
    }

    if (state.showLimitReachedDialog) {
        DDProUpgradeDialog(
            onUpgradeClick = {
                viewModel.dismissLimitDialog()
                onUpgradeClick()
            },
            onDismiss = viewModel::dismissLimitDialog,
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.editor_discard_confirm_title)) },
            text = { Text(stringResource(R.string.editor_discard_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            },
        )
    }
}

@Composable
private fun RecordEditorForm(
    state: RecordEditorUiState,
    viewModel: RecordEditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val drinkTypeSelected = state.input.type != null

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        DDFormSection(stringResource(R.string.editor_section_basic)) {
            DDDrinkTypeSelector(
                state.input.type,
                viewModel::updateType,
                state.validationError.type?.let { stringResource(it) },
            )
            DDTextField(
                stringResource(R.string.editor_name_label),
                state.input.name,
                viewModel::updateName,
                error = state.validationError.name?.let { stringResource(it) },
            )
            DDDateTimeField(
                label = stringResource(R.string.detail_recorded_at),
                valueMillis = state.input.recordedAtMillis,
                onValueChange = viewModel::updateRecordedAtMillis,
                error = state.validationError.recordedAt?.let { stringResource(it) },
            )
            DDNumberField(
                stringResource(R.string.detail_price),
                state.input.priceText,
                viewModel::updatePrice,
                error = state.validationError.price?.let { stringResource(it) },
            )
            DDTextField(stringResource(R.string.detail_place), state.input.place, viewModel::updatePlace)
        }
        DDFormSection(stringResource(R.string.editor_section_rating)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    DDRatingInput(
                        rating = state.input.rating,
                        onRatingChange = viewModel::updateRating,
                        error = state.validationError.rating?.let { stringResource(it) },
                    )
                }
                TextButton(
                    onClick = viewModel::toggleRatingBreakdown,
                    enabled = drinkTypeSelected,
                ) {
                    Text(
                        if (state.input.ratingBreakdownExpanded) {
                            stringResource(
                                R.string.editor_collapse_profile,
                            )
                        } else {
                            stringResource(R.string.editor_expand_profile)
                        },
                    )
                }
            }
            if (state.input.ratingBreakdownExpanded) {
                Text(
                    text = stringResource(R.string.editor_profile_description),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
                state.input.type?.ratingCriteria()?.forEach { criterion ->
                    DDSensoryMetricSlider(
                        criterion = criterion,
                        value = state.input.ratingBreakdown.values[criterion.index],
                        onValueChange = { viewModel.updateDetailRating(criterion.index, it) },
                    )
                }
            }
        }
        DDFormSection(stringResource(R.string.editor_section_memo)) {
            DDCollectionStatusSelector(
                selected = state.input.collectionStatus,
                onSelected = viewModel::updateCollectionStatus,
                error = state.validationError.collectionStatus?.let { stringResource(it) },
            )
            DDMultilineTextField(
                stringResource(R.string.detail_tasting_note),
                state.input.tastingNote,
                viewModel::updateTastingNote,
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DDSecondaryButton(stringResource(R.string.cancel), onClick = onBack, modifier = Modifier.weight(1f))
            DDPrimaryButton(
                text = stringResource(R.string.save),
                onClick = viewModel::save,
                modifier = Modifier.weight(1f),
                enabled = !state.saving && drinkTypeSelected,
            )
        }
    }
}
