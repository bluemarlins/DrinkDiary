package com.bluemarlin.drinkdiary.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordForm(
    val name: String = "",
    val rating: Double = 0.0,
    val collectionStatus: CollectionStatus = CollectionStatus.Normal,
    val vintage: String = "",
    val servingStyle: ServingStyle? = null,
    val price: String = "",
    val place: String = "",
    val memo: String = "",
    val imageUri: String? = null,
    val tags: DrinkTags = DrinkTags(),
) {
    // 만족도는 선택이 아니다 — 선호 판정의 종속 변수라 없으면 그 기록은 유형에 기여하지 못한다.
    val isSavable: Boolean get() = name.isNotBlank() && rating > 0.0
}

data class RecordUiState(
    val type: DrinkType? = null,
    val taste: TasteInput = TasteInput(),
    val form: RecordForm = RecordForm(),
    val taps: Int = 0,
    val saving: Boolean = false,
    val savedId: Long? = null,
    val error: String? = null,
)

class RecordViewModel(
    private val repository: DrinkRecordRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    fun pickType(type: DrinkType) =
        _uiState.update {
            it.copy(type = type, taste = TasteInput(), taps = it.taps + 1)
        }

    fun answer(
        trait: Trait,
        answer: TraitAnswer,
    ) = _uiState.update {
        it.copy(taste = it.taste.with(trait, answer), taps = it.taps + 1)
    }

    fun updateForm(form: RecordForm) = _uiState.update { it.copy(form = form) }

    fun save() {
        val state = _uiState.value
        val type = state.type ?: return
        if (!state.form.isSavable || state.saving) return

        _uiState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val record =
                DrinkRecord(
                    type = type,
                    name = state.form.name.trim(),
                    vintage = state.form.vintage.toIntOrNull(),
                    servingStyle = if (type == DrinkType.Whiskey) state.form.servingStyle else null,
                    taste = state.taste,
                    tags = state.form.tags,
                    rating = state.form.rating,
                    collectionStatus = state.form.collectionStatus,
                    imageUri = state.form.imageUri,
                    price = state.form.price.toLongOrNull(),
                    place = state.form.place.ifBlank { null },
                    memo = state.form.memo.ifBlank { null },
                    recordedAtMillis = System.currentTimeMillis(),
                )
            when (val result = repository.save(record)) {
                is AppResult.Success ->
                    _uiState.update { it.copy(saving = false, savedId = result.value) }
                is AppResult.Failure ->
                    _uiState.update { it.copy(saving = false, error = "저장하지 못했습니다. 다시 시도해 주세요.") }
            }
        }
    }

    fun startOver() = _uiState.value.let { _uiState.value = RecordUiState() }

    class Factory(
        private val repository: DrinkRecordRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RecordViewModel(repository) as T
    }
}
