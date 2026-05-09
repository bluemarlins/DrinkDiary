package com.bluemarlin.drinkdiary.ui.editor

import androidx.activity.compose.BackHandler
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
import com.bluemarlin.drinkdiary.ui.component.DDSensoryMetricSlider
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
        title = if (state.input.id == 0L) "기록 등록" else "기록 수정",
        screenType = DDScreenType.Editor,
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
                            onBack = requestBack,
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
                            onBack = requestBack,
                        )
                        DDFormSection("사진") {
                            DDImagePicker(state.input.imageUri, viewModel::updateImageUri)
                        }
                    }
                }
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("입력 초기화") },
            text = { Text("입력한 내용이 초기화됩니다. 나가시겠어요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                ) {
                    Text("예")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("아니오")
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
        DDFormSection("전체 평점") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    DDRatingInput(
                        rating = state.input.rating,
                        onRatingChange = viewModel::updateRating,
                        error = state.validationError.rating,
                    )
                }
                TextButton(
                    onClick = viewModel::toggleRatingBreakdown,
                    enabled = drinkTypeSelected,
                ) {
                    Text(if (state.input.ratingBreakdownExpanded) "프로필 접기 ▲" else "테이스팅 프로필 ▼")
                }
            }
            if (state.input.ratingBreakdownExpanded) {
                Text(
                    text = "이 항목들은 평점이 아니라 맛과 향의 특성 지표예요. 높을수록 좋다는 뜻은 아니에요.",
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
        DDFormSection("메모와 분류") {
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
