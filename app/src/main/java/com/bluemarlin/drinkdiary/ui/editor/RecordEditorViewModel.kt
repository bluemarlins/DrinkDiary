package com.bluemarlin.drinkdiary.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordInput
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.SaveDrinkRecordError
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDrinkRecordUseCase
import com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordEditorUiState(
    val input: DrinkRecordInput = DrinkRecordInput(),
    val validationError: SaveDrinkRecordError = SaveDrinkRecordError(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface RecordEditorEvent {
    data class Saved(val recordId: Long) : RecordEditorEvent
}

class RecordEditorViewModel(
    private val recordId: Long?,
    private val observeDrinkRecordUseCase: ObserveDrinkRecordUseCase,
    private val saveDrinkRecordUseCase: SaveDrinkRecordUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordEditorUiState(loading = recordId != null))
    val uiState: StateFlow<RecordEditorUiState> = _uiState

    private val _events = MutableSharedFlow<RecordEditorEvent>()
    val events: SharedFlow<RecordEditorEvent> = _events

    init {
        if (recordId != null) {
            viewModelScope.launch {
                val record = observeDrinkRecordUseCase(recordId).first()
                if (record == null) {
                    _uiState.update { it.copy(loading = false, errorMessage = "수정할 기록을 찾지 못했습니다.") }
                } else {
                    _uiState.value = RecordEditorUiState(
                        input = DrinkRecordInput(
                            id = record.id,
                            type = record.type,
                            name = record.name,
                            imageUri = record.imageUri,
                            priceText = record.price?.toString().orEmpty(),
                            place = record.place.orEmpty(),
                            tastingNote = record.tastingNote.orEmpty(),
                            tastingTags = record.tastingTags.toSet(),
                            rating = record.rating,
                            abv = record.abv,
                            volumeMl = record.volumeMl,
                            collectionStatus = record.collectionStatus,
                            recordedAtMillis = record.recordedAtMillis,
                        ),
                    )
                }
            }
        }
    }

    // Tags stay as-is when the type changes: a tag the user already picked shouldn't vanish
    // because they corrected the drink type, and the picker keeps showing off-catalog keys.
    fun updateType(value: DrinkType) = updateInput { it.copy(type = value) }
    fun updateName(value: String) = updateInput { it.copy(name = value) }
    fun updateImageUri(value: String?) = updateInput { it.copy(imageUri = value) }
    fun updatePrice(value: String) = updateInput { it.copy(priceText = value) }
    fun updatePlace(value: String) = updateInput { it.copy(place = value) }
    fun updateTastingNote(value: String) = updateInput { it.copy(tastingNote = value) }
    fun updateRating(value: Double) = updateInput { it.copy(rating = value) }

    fun toggleTastingTag(key: String) = updateInput {
        val tags = if (key in it.tastingTags) it.tastingTags - key else it.tastingTags + key
        it.copy(tastingTags = tags)
    }

    fun updateCollectionStatus(value: CollectionStatus) = updateInput { it.copy(collectionStatus = value) }
    fun updateRecordedAtMillis(value: Long) = updateInput { it.copy(recordedAtMillis = value) }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, validationError = SaveDrinkRecordError(), errorMessage = null) }
            when (val result = saveDrinkRecordUseCase(_uiState.value.input)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(saving = false) }
                    _events.emit(RecordEditorEvent.Saved(result.value))
                }
                is AppResult.Failure -> {
                    val validation = (result.error as? AppError.Validation)?.error
                    _uiState.update {
                        it.copy(
                            saving = false,
                            validationError = validation ?: SaveDrinkRecordError(),
                            errorMessage = if (validation == null) "저장하지 못했습니다. 다시 시도해 주세요." else null,
                        )
                    }
                }
            }
        }
    }

    private fun updateInput(block: (DrinkRecordInput) -> DrinkRecordInput) {
        _uiState.update { it.copy(input = block(it.input), validationError = SaveDrinkRecordError()) }
    }

    class Factory(
        private val recordId: Long?,
        private val observeDrinkRecordUseCase: ObserveDrinkRecordUseCase,
        private val saveDrinkRecordUseCase: SaveDrinkRecordUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RecordEditorViewModel(recordId, observeDrinkRecordUseCase, saveDrinkRecordUseCase) as T
    }
}
