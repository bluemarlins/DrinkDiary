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
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    companion object {
        // 편집 화면이 같은 폼을 쓰기 위한 역변환. 저장 규칙이 한 곳에만 있어야
        // "이름과 만족도는 필수"가 기록에서만 지켜지는 일이 안 생긴다.
        fun of(record: DrinkRecord): RecordForm =
            RecordForm(
                name = record.name,
                rating = record.rating,
                collectionStatus = record.collectionStatus,
                vintage = record.vintage?.toString().orEmpty(),
                servingStyle = record.servingStyle,
                price = record.price?.toString().orEmpty(),
                place = record.place.orEmpty(),
                memo = record.memo.orEmpty(),
                imageUri = record.imageUri,
                tags = record.tags,
            )
    }
}

data class RecordUiState(
    val type: DrinkType? = null,
    val taste: TasteInput = TasteInput(),
    val form: RecordForm = RecordForm(),
    val taps: Int = 0,
    val saving: Boolean = false,
    val savedId: Long? = null,
    val error: String? = null,
    // 사용자가 "매번 물어봐 달라"고 고른 태그. '더 남기기' 밖으로 나온다.
    val alwaysAskTags: Set<TagCategory> = emptySet(),
    // 첫 기록 직후 한 번만 뜨는 물음. 답하고 나면 다시 뜨지 않는다.
    val askTagPreference: Boolean = false,
)

class RecordViewModel(
    private val repository: DrinkRecordRepository,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.alwaysAskTags.collect { tags ->
                _uiState.update { it.copy(alwaysAskTags = tags) }
            }
        }
    }

    fun chooseAlwaysAskTags(tags: Set<TagCategory>) {
        viewModelScope.launch {
            preferences.setAlwaysAskTags(tags)
            _uiState.update { it.copy(askTagPreference = false) }
        }
    }

    // 첫 화면이 주종과 분류를 함께 받는다. 분류는 그대로 태그가 되고,
    // TagPicker는 이 태그를 다시 묻지 않는다(DrinkPicker.promotedTags).
    fun pickDrink(
        type: DrinkType,
        tags: DrinkTags,
    ) = _uiState.update {
        it.copy(
            type = type,
            taste = TasteInput(),
            form = it.form.copy(tags = tags),
            taps = it.taps + 1,
        )
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
                is AppResult.Success -> {
                    // 첫 기록을 마친 직후에만 묻는다. 저장이 성공한 뒤라 이미 가치를 받은 상태다.
                    val askNow = !preferences.hasChosenTagPreferences.first()
                    _uiState.update {
                        it.copy(saving = false, savedId = result.value, askTagPreference = askNow)
                    }
                }
                is AppResult.Failure ->
                    _uiState.update { it.copy(saving = false, error = "저장하지 못했습니다. 다시 시도해 주세요.") }
            }
        }
    }

    // 다시 기록해도 이미 고른 설정은 유지한다.
    fun startOver() = _uiState.update { RecordUiState(alwaysAskTags = it.alwaysAskTags) }

    class Factory(
        private val repository: DrinkRecordRepository,
        private val preferences: UserPreferencesRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RecordViewModel(repository, preferences) as T
    }
}
