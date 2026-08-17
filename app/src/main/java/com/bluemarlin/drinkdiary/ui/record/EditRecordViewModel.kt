package com.bluemarlin.drinkdiary.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import com.bluemarlin.drinkdiary.domain.usecase.DeletePhotoUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ImportPhotoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditUiState(
    val loaded: Boolean = false,
    val missing: Boolean = false,
    val type: DrinkType = DrinkType.Wine,
    val form: RecordForm = RecordForm(),
    val taste: TasteInput = TasteInput(),
    val alwaysAskTags: Set<TagCategory> = emptySet(),
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

// 편집 저장의 전부. **원본을 copy로 덮는 것이 핵심이다** — 새 DrinkRecord를 조립하면
// 화면에 없는 필드(id, recordedAtMillis)가 조용히 기본값으로 바뀐다.
//
// `recordedAtMillis`는 **마신 시각이지 고친 시각이 아니다.** 오타를 고쳤다고 그 술을 오늘 마신 것이
// 되면 목록 순서가 뒤집힌다. `type`도 원본 것을 그대로 쓴다 — 와인이 위스키가 되는 편집은 없다.
//
// 순수 함수인 이유는 테스트 때문이다. ViewModel 안에 두면 이 규칙들을 확인하려고
// 코루틴 테스트 라이브러리를 새로 들여야 한다(harness.md §10 — 라이브러리 추가 금지).
fun DrinkRecord.applying(
    form: RecordForm,
    taste: TasteInput,
): DrinkRecord =
    copy(
        name = form.name.trim(),
        vintage = form.vintage.toIntOrNull(),
        servingStyle = if (type == DrinkType.Whiskey) form.servingStyle else null,
        // 편집 화면은 공통 축만 보여주지만 taste에는 원본의 고유 축 답이 그대로 실려 있다.
        // 저장이 답을 통째로 갈아끼우므로, 여기서 빠지면 그 답은 영영 사라진다.
        taste = taste,
        tags = form.tags,
        rating = form.rating,
        collectionStatus = form.collectionStatus,
        imageUri = form.imageUri,
        price = form.price.toLongOrNull(),
        place = form.place.ifBlank { null },
        memo = form.memo.ifBlank { null },
    )

// 기록 수정. 기록 작성(RecordViewModel)과 합치지 않은 이유는 두 흐름의 성질이 다르기 때문이다 —
// 작성은 탭 수를 세고 첫 기록 물음을 띄우는 **마법사**이고, 수정은 이미 있는 값을 고르는 **폼**이다.
// 한 ViewModel에 넣으면 양쪽 다 조건문으로 뒤덮인다.
class EditRecordViewModel(
    private val recordId: Long,
    private val repository: DrinkRecordRepository,
    private val preferences: UserPreferencesRepository,
    private val importPhoto: ImportPhotoUseCase,
    private val deletePhoto: DeletePhotoUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    // 원본을 통째로 들고 있는다. 저장할 때 copy로 덮어야 **화면에 없는 필드가 조용히 사라지지 않는다**
    // — 특히 recordedAtMillis. 마신 시각이지 고친 시각이 아니라서, 수정했다고 목록 순서가 바뀌면 안 된다.
    private var original: DrinkRecord? = null

    init {
        viewModelScope.launch {
            val record = repository.observeRecord(recordId).first()
            if (record == null) {
                _uiState.update { it.copy(loaded = true, missing = true) }
                return@launch
            }
            original = record
            _uiState.update {
                it.copy(
                    loaded = true,
                    type = record.type,
                    form = RecordForm.of(record),
                    taste = record.taste,
                )
            }
        }
        viewModelScope.launch {
            preferences.alwaysAskTags.collect { tags ->
                _uiState.update { it.copy(alwaysAskTags = tags) }
            }
        }
    }

    fun updateForm(form: RecordForm) = _uiState.update { it.copy(form = form) }

    // 작성 경로와 같은 규칙이다 — 고른 즉시 앱 안으로 들여온다(prd.md F1-3).
    fun pickPhoto(sourceUri: String) {
        viewModelScope.launch {
            // **원본의 사진은 여기서 지우지 않는다.** 고치다 말고 나갈 수 있고, 그러면 그 기록은
            // 여전히 이 사진을 가리킨다. 저장까지 간 뒤에야 참조가 끊긴다.
            val replaced =
                _uiState.value.form.imageUri
                    .takeIf { it != original?.imageUri }
            when (val result = importPhoto(sourceUri)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(form = it.form.copy(imageUri = result.value)) }
                    replaced?.let { deletePhoto(it) }
                }
                is AppResult.Failure ->
                    _uiState.update { it.copy(error = "사진을 가져오지 못했어요. 다시 골라 주세요.") }
            }
        }
    }

    fun answer(
        trait: Trait,
        answer: TraitAnswer,
    ) = _uiState.update { it.copy(taste = it.taste.with(trait, answer)) }

    fun save() {
        val source = original ?: return
        val state = _uiState.value
        if (!state.form.isSavable || state.saving) return

        _uiState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            // 저장이 성공해야 원본 사진의 참조가 끊긴다. 저장 전에 지우면 실패했을 때
            // 살아 있는 기록의 사진만 사라진다.
            val dropped = source.imageUri.takeIf { it != state.form.imageUri }
            when (repository.save(source.applying(state.form, state.taste))) {
                is AppResult.Success -> {
                    dropped?.let { deletePhoto(it) }
                    _uiState.update { it.copy(saving = false, saved = true) }
                }
                // 저장 실패를 조용히 넘기면 사용자는 고쳐진 줄 안다(harness.md §7).
                is AppResult.Failure ->
                    _uiState.update { it.copy(saving = false, error = "고치지 못했어요. 다시 시도해 주세요.") }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }

    class Factory(
        private val recordId: Long,
        private val repository: DrinkRecordRepository,
        private val preferences: UserPreferencesRepository,
        private val importPhoto: ImportPhotoUseCase,
        private val deletePhoto: DeletePhotoUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditRecordViewModel(recordId, repository, preferences, importPhoto, deletePhoto) as T
    }
}
