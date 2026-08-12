package com.bluemarlin.drinkdiary.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDrinkRecordsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
sealed interface CollectionUiState {
    data object Loading : CollectionUiState

    data class Empty(
        val filtered: Boolean,
    ) : CollectionUiState

    data class Success(
        val records: List<DrinkRecord>,
    ) : CollectionUiState

    data class Error(
        val messageRes: Int,
    ) : CollectionUiState
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CollectionViewModel(
    private val observeDrinkRecordsUseCase: ObserveDrinkRecordsUseCase,
    initialStatus: CollectionStatus?,
) : ViewModel() {
    val selectedType = MutableStateFlow<DrinkType?>(null)
    val selectedStatus = MutableStateFlow(initialStatus)

    private val filter =
        combine(selectedType, selectedStatus) { type, status ->
            DrinkRecordFilter(type, status)
        }

    val uiState: StateFlow<CollectionUiState> =
        filter
            .flatMapLatest { currentFilter ->
                observeDrinkRecordsUseCase(currentFilter).map { records ->
                    if (records.isEmpty()) {
                        CollectionUiState.Empty(
                            currentFilter.drinkType != null || currentFilter.collectionStatus != null,
                        )
                    } else {
                        CollectionUiState.Success(records)
                    }
                }
            }.catch { emit(CollectionUiState.Error(R.string.error_load_failed)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionUiState.Loading)

    fun selectType(type: DrinkType?) {
        selectedType.value = type
    }

    fun selectStatus(status: CollectionStatus?) {
        selectedStatus.value = status
    }

    class Factory(
        private val observeDrinkRecordsUseCase: ObserveDrinkRecordsUseCase,
        private val initialStatus: CollectionStatus?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CollectionViewModel(observeDrinkRecordsUseCase, initialStatus) as T
    }
}
