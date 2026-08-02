package com.bluemarlin.drinkdiary.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.DashboardPeriod
import com.bluemarlin.drinkdiary.domain.model.DashboardSummary
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDashboardSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data object Empty : DashboardUiState

    data class Success(
        val summary: DashboardSummary,
    ) : DashboardUiState

    data class Error(
        val message: String,
    ) : DashboardUiState
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val observeDashboardSummaryUseCase: ObserveDashboardSummaryUseCase,
) : ViewModel() {
    val selectedPeriod = MutableStateFlow(DashboardPeriod.Yearly)

    val uiState: StateFlow<DashboardUiState> =
        selectedPeriod
            .flatMapLatest { period ->
                observeDashboardSummaryUseCase(period).map { summary ->
                    if (summary.totalCount == 0) DashboardUiState.Empty else DashboardUiState.Success(summary)
                }
            }.catch { emit(DashboardUiState.Error("대시보드를 불러오지 못했습니다.")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    fun selectPeriod(period: DashboardPeriod) {
        selectedPeriod.value = period
    }

    class Factory(
        private val observeDashboardSummaryUseCase: ObserveDashboardSummaryUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(observeDashboardSummaryUseCase) as T
    }
}
