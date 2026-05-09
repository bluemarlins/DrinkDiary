package com.bluemarlin.drinkdiary.ui.editor

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
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
import com.bluemarlin.drinkdiary.ui.component.DDRatingInput
import com.bluemarlin.drinkdiary.ui.component.DDSecondaryButton
import com.bluemarlin.drinkdiary.ui.component.DDTextField
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType

@Composable
fun RecordEditorRoute(
    viewModel: RecordEditorViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is RecordEditorEvent.Saved) onSaved(event.recordId)
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    DDScreenScaffold(
        title = if (state.input.id == 0L) "기록 등록" else "기록 수정",
        screenType = DDScreenType.Editor,
        onBackClick = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.loading) {
            DDLoadingContent(Modifier.padding(padding))
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .padding(16.dp),
            ) {
                val twoPane = maxWidth >= 600.dp
                if (twoPane) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        DDFormSection("사진") {
                            DDImagePicker(
                                imageUri = state.input.imageUri,
                                onImageSelected = viewModel::updateImageUri,
                                modifier = Modifier.widthIn(max = 360.dp),
                            )
                        }
                        RecordEditorForm(
                            state = state,
                            viewModel = viewModel,
                            onBack = onBack,
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        RecordEditorForm(
                            state = state,
                            viewModel = viewModel,
                            onBack = onBack,
                        )
                        DDFormSection("사진") {
                            DDImagePicker(state.input.imageUri, viewModel::updateImageUri)
                        }
                    }
                }
            }
        }
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
        DDFormSection("기본 정보") {
            DDDrinkTypeSelector(state.input.type, viewModel::updateType, state.validationError.type)
            DDTextField("이름", state.input.name, viewModel::updateName, error = state.validationError.name)
            DDDateTimeField(
                label = "기록 일시",
                valueMillis = state.input.recordedAtMillis,
                onValueChange = viewModel::updateRecordedAtMillis,
                error = state.validationError.recordedAt,
            )
            DDNumberField("가격", state.input.priceText, viewModel::updatePrice, error = state.validationError.price)
            DDTextField("장소", state.input.place, viewModel::updatePlace)
        }
        DDFormSection("평가") {
            val representativeRating = if (state.input.ratingBreakdownExpanded) {
                state.input.ratingBreakdown.average
            } else {
                state.input.rating
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("대표 별점")
                    DDRatingInput(
                        rating = representativeRating,
                        onRatingChange = viewModel::updateRating,
                        error = state.validationError.rating,
                        enabled = !state.input.ratingBreakdownExpanded,
                    )
                    if (state.input.ratingBreakdownExpanded) {
                        Text("세부 평가 평균 %.1f".format(representativeRating))
                    }
                }
                TextButton(
                    onClick = viewModel::toggleRatingBreakdown,
                    enabled = drinkTypeSelected,
                ) {
                    Text(if (state.input.ratingBreakdownExpanded) "접기 ▲" else "세부 평가 ▼")
                }
            }
            if (state.input.ratingBreakdownExpanded) {
                state.input.type?.ratingCriteria()?.forEach { criterion ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(criterion.label)
                        DDRatingInput(
                            rating = state.input.ratingBreakdown.values[criterion.index],
                            onRatingChange = { viewModel.updateDetailRating(criterion.index, it) },
                        )
                    }
                }
            }
            DDCollectionStatusSelector(
                selected = state.input.collectionStatus,
                onSelected = viewModel::updateCollectionStatus,
                error = state.validationError.collectionStatus,
            )
            DDMultilineTextField("테이스팅 노트", state.input.tastingNote, viewModel::updateTastingNote)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DDSecondaryButton("취소", onClick = onBack, modifier = Modifier.weight(1f))
            DDPrimaryButton(
                text = "저장",
                onClick = viewModel::save,
                modifier = Modifier.weight(1f),
                enabled = !state.saving && drinkTypeSelected,
            )
        }
    }
}
