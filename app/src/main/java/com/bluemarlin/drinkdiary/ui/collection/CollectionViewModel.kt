package com.bluemarlin.drinkdiary.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// null 이면 두 주종을 한 목록에 섞어 보여준다 — PRD F1 "두 주종이 하나의 컬렉션에 쌓인다".
//
// `selectionMode`는 저장하지 않고 `selected`에서 파생시킨다. 둘을 따로 들면 "선택 모드인데
// 아무것도 선택되지 않은" 상태가 생기고, 그때 일괄 작업 바가 무엇을 지울지 말할 수 없다.
data class CollectionUiState(
    val filter: DrinkType? = null,
    val records: List<DrinkRecord> = emptyList(),
    val loaded: Boolean = false,
    val error: String? = null,
    val selectionMode: Boolean = false,
    val selected: Set<Long> = emptySet(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionViewModel(
    private val repository: DrinkRecordRepository,
) : ViewModel() {
    private val filter = MutableStateFlow<DrinkType?>(null)
    private val selection = MutableStateFlow<Set<Long>>(emptySet())
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CollectionUiState> =
        combine(
            filter,
            filter.flatMapLatest { repository.observeRecords(it) },
            selection,
            error,
        ) { selectedFilter, records, selectedIds, message ->
            CollectionUiState(
                filter = selectedFilter,
                records = records,
                loaded = true,
                error = message,
                selectionMode = selectedIds.isNotEmpty(),
                selected = selectedIds,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionUiState())

    fun selectFilter(type: DrinkType?) {
        // 안 보이는 기록이 선택된 채 남으면 사용자는 무엇을 지우는지 알 수 없다. 되돌리기가
        // 없는 기능이라(prd.md F1-2) 그 상태를 만들지 않는다.
        selection.value = emptySet()
        filter.value = type
    }

    fun toggleSelection(id: Long) {
        selection.update { current -> if (id in current) current - id else current + id }
    }

    fun clearSelection() {
        selection.value = emptySet()
    }

    // prd.md F1-2. 확인은 화면이 이미 받았다 — 여기까지 왔으면 지운다.
    fun deleteSelected() {
        val targets = selection.value
        if (targets.isEmpty()) return
        viewModelScope.launch {
            when (repository.deleteByIds(targets)) {
                is AppResult.Success -> {
                    selection.value = emptySet()
                    error.value = null
                }
                is AppResult.Failure -> error.value = "지우지 못했습니다. 다시 시도해 주세요."
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            // 삭제 실패를 조용히 넘기면 목록이 그대로라 사용자는 지워진 줄 안다(harness.md §7).
            when (repository.deleteById(id)) {
                is AppResult.Success -> error.value = null
                is AppResult.Failure -> error.value = "지우지 못했습니다. 다시 시도해 주세요."
            }
        }
    }

    fun dismissError() = error.update { null }

    class Factory(
        private val repository: DrinkRecordRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CollectionViewModel(repository) as T
    }
}
