package com.bluemarlin.drinkdiary.ui.record

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluemarlin.drinkdiary.DrinkDiaryApplication
import com.bluemarlin.drinkdiary.domain.model.DrinkType

private enum class Step {
    PickDrink,
    PickOrigin,
    PickPhoto,
    InputName,
    InputRating,
    Probes,
    OptionalDetail,
}

@Composable
fun RecordFlow(
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appContainer = (LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer
    val viewModel: RecordViewModel =
        viewModel(
            factory =
                RecordViewModel.Factory(
                    appContainer.drinkRecordRepository,
                    appContainer.userPreferencesRepository,
                    appContainer.importPhotoUseCase,
                    appContainer.deletePhotoUseCase,
                ),
        )
    val state by viewModel.uiState.collectAsState()
    var step by remember { mutableStateOf(Step.PickDrink) }

    LaunchedEffect(state.savedId) {
        if (state.savedId != null) {
            onSaved()
        }
    }

    BackHandler(enabled = step != Step.PickDrink) {
        step =
            when (step) {
                Step.PickOrigin -> Step.PickDrink
                Step.PickPhoto -> Step.PickOrigin
                Step.InputName -> Step.PickPhoto
                Step.InputRating -> Step.InputName
                Step.Probes -> Step.InputRating
                Step.OptionalDetail -> Step.Probes
                Step.PickDrink -> step
            }
    }

    Crossfade(targetState = step, label = "record-step") { current ->
        when (current) {
            Step.PickDrink ->
                DrinkPicker(
                    onPick = { choice ->
                        viewModel.pickDrink(choice.type, choice.tags)
                        step = Step.PickOrigin
                    },
                    modifier = modifier,
                )

            Step.PickOrigin ->
                OriginPickerStep(
                    type = state.type ?: DrinkType.Wine,
                    onPick = { origin ->
                        viewModel.pickOrigin(origin)
                        step = Step.PickPhoto
                    },
                    onBack = { step = Step.PickDrink },
                    modifier = modifier,
                )

            Step.PickPhoto ->
                DrinkPhotoStep(
                    imageUri = state.form.imageUri,
                    onPhotoPicked = viewModel::pickPhoto,
                    onNext = { step = Step.InputName },
                    onBack = { step = Step.PickOrigin },
                    modifier = modifier,
                )

            Step.InputName ->
                DrinkNameStep(
                    name = state.form.name,
                    onNameChange = { viewModel.updateForm(state.form.copy(name = it)) },
                    onNext = { step = Step.InputRating },
                    onBack = { step = Step.PickPhoto },
                    modifier = modifier,
                )

            Step.InputRating ->
                DrinkRatingStep(
                    rating = state.form.rating,
                    collectionStatus = state.form.collectionStatus,
                    onRatingChange = { viewModel.updateForm(state.form.copy(rating = it)) },
                    onCollectionStatusChange = { viewModel.updateForm(state.form.copy(collectionStatus = it)) },
                    onNext = { step = Step.Probes },
                    onBack = { step = Step.InputName },
                    modifier = modifier,
                )

            Step.Probes ->
                ProbeSequenceScreen(
                    type = state.type ?: DrinkType.Wine,
                    answers = state.taste,
                    onAnswer = viewModel::answer,
                    onComplete = { step = Step.OptionalDetail },
                    onBack = { step = Step.InputRating },
                    modifier = modifier,
                )

            Step.OptionalDetail ->
                RecordOptionalDetailStep(
                    type = state.type ?: DrinkType.Wine,
                    form = state.form,
                    alwaysAskTags = state.alwaysAskTags,
                    onFormChange = viewModel::updateForm,
                    onSave = viewModel::save,
                    onBack = { step = Step.Probes },
                    saving = state.saving,
                    modifier = modifier,
                    errorMessage = state.error,
                )
        }
    }

    state.error?.let {
        // 저장 실패는 조용히 넘기지 않는다(harness.md §7).
        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(20.dp))
    }
}
