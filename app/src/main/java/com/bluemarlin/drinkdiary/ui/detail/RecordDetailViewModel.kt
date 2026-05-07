package com.bluemarlin.drinkdiary.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.usecase.DeleteDrinkRecordUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDrinkRecordUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RecordDetailUiState {
    data object Loading : RecordDetailUiState
    data object NotFound : RecordDetailUiState
    data class Success(val record: DrinkRecord) : RecordDetailUiState
    data class Error(val message: String) : RecordDetailUiState
}

sealed interface RecordDetailEvent {
    data object Deleted : RecordDetailEvent
}

class RecordDetailViewModel(
    recordId: Long,
    observeDrinkRecordUseCase: ObserveDrinkRecordUseCase,
    private val deleteDrinkRecordUseCase: DeleteDrinkRecordUseCase,
) : ViewModel() {
    val uiState: StateFlow<RecordDetailUiState> = observeDrinkRecordUseCase(recordId)
        .map { record -> record?.let { RecordDetailUiState.Success(it) } ?: RecordDetailUiState.NotFound }
        .catch { emit(RecordDetailUiState.Error("기록을 불러오지 못했습니다.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordDetailUiState.Loading)

    private val _events = MutableSharedFlow<RecordDetailEvent>()
    val events: SharedFlow<RecordDetailEvent> = _events

    fun delete(recordId: Long) {
        viewModelScope.launch {
            if (deleteDrinkRecordUseCase(recordId) is AppResult.Success) {
                _events.emit(RecordDetailEvent.Deleted)
            }
        }
    }

    class Factory(
        private val recordId: Long,
        private val observeDrinkRecordUseCase: ObserveDrinkRecordUseCase,
        private val deleteDrinkRecordUseCase: DeleteDrinkRecordUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RecordDetailViewModel(recordId, observeDrinkRecordUseCase, deleteDrinkRecordUseCase) as T
    }
}
