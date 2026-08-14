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
data class CollectionUiState(
    val filter: DrinkType? = null,
    val records: List<DrinkRecord> = emptyList(),
    val loaded: Boolean = false,
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionViewModel(
    private val repository: DrinkRecordRepository,
) : ViewModel() {
    private val filter = MutableStateFlow<DrinkType?>(null)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CollectionUiState> =
        combine(
            filter,
            filter.flatMapLatest { repository.observeRecords(it) },
            error,
        ) { selected, records, message ->
            CollectionUiState(
                filter = selected,
                records = records,
                loaded = true,
                error = message,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionUiState())

    fun selectFilter(type: DrinkType?) {
        filter.value = type
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
