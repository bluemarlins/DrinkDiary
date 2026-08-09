package com.bluemarlin.drinkdiary.ui.editor

import android.app.Activity
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.DrinkDiaryApplication
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
import com.bluemarlin.drinkdiary.ui.component.DDTastingTagPicker
import com.bluemarlin.drinkdiary.ui.component.DDTextField
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold

@Composable
fun RecordEditorRoute(
    viewModel: RecordEditorViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val appContainer = (context.applicationContext as DrinkDiaryApplication).appContainer

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is RecordEditorEvent.Saved) {
                // Navigate first, then check the interstitial — ads must appear after the
                // screen transition completes, never gate the transition itself (see
                // app/docs/research/competitor-analysis.md on save-blocking interstitials).
                onSaved(event.recordId)
                val activity = context as? Activity
                if (activity != null) {
                    appContainer.interstitialAdManager.maybeShowAfterSave(activity) {}
                }
            }
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    DDScreenScaffold(
        title = if (state.input.id == 0L) "기록 등록" else "기록 수정",
        showBottomBar = false,
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
                        Column(
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                        ) {
                            RecordEditorFields(state = state, viewModel = viewModel)
                            RecordEditorActions(onBack = onBack, onSave = viewModel::save, saving = state.saving)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        RecordEditorFields(state = state, viewModel = viewModel)
                        // 사진 comes before the action row, not after — a form filled
                        // top-to-bottom shouldn't hit 취소/저장 before reaching photo
                        // attachment, which would otherwise be stranded below "the end".
                        DDFormSection("사진") {
                            DDImagePicker(state.input.imageUri, viewModel::updateImageUri)
                        }
                        RecordEditorActions(onBack = onBack, onSave = viewModel::save, saving = state.saving)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordEditorFields(
    state: RecordEditorUiState,
    viewModel: RecordEditorViewModel,
    modifier: Modifier = Modifier,
) {
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("별점")
                DDRatingInput(
                    rating = state.input.rating,
                    onRatingChange = viewModel::updateRating,
                    error = state.validationError.rating,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("테이스팅 태그")
                DDTastingTagPicker(
                    type = state.input.type,
                    selected = state.input.tastingTags,
                    onToggle = viewModel::toggleTastingTag,
                )
            }
            DDCollectionStatusSelector(
                selected = state.input.collectionStatus,
                onSelected = viewModel::updateCollectionStatus,
                error = state.validationError.collectionStatus,
            )
            DDMultilineTextField("테이스팅 노트", state.input.tastingNote, viewModel::updateTastingNote)
        }
    }
}

@Composable
private fun RecordEditorActions(onBack: () -> Unit, onSave: () -> Unit, saving: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DDSecondaryButton("취소", onClick = onBack, modifier = Modifier.weight(1f))
        DDPrimaryButton("저장", onClick = onSave, modifier = Modifier.weight(1f), enabled = !saving)
    }
}
