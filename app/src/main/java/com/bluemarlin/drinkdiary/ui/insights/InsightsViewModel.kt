package com.bluemarlin.drinkdiary.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.InsightsSummary
import com.bluemarlin.drinkdiary.domain.usecase.ObserveInsightsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface InsightsUiState {
    data object Loading : InsightsUiState

    data object Empty : InsightsUiState

    data class Success(
        val summary: InsightsSummary,
    ) : InsightsUiState

    data class Error(
        val message: String,
    ) : InsightsUiState
}

class InsightsViewModel(
    private val observeInsightsUseCase: ObserveInsightsUseCase,
) : ViewModel() {
    val uiState: StateFlow<InsightsUiState> =
        observeInsightsUseCase()
            .map { summary ->
                val isMonthlyTrendEmpty =
                    summary.monthlyTrend.isEmpty() || summary.monthlyTrend.all { it.totalCount == 0 }
                val isPriceBracketsEmpty =
                    summary.priceBrackets.isEmpty() || summary.priceBrackets.all { it.count == 0 }
                if (isMonthlyTrendEmpty && isPriceBracketsEmpty) {
                    InsightsUiState.Empty
                } else {
                    InsightsUiState.Success(summary)
                }
            }.catch { emit(InsightsUiState.Error("인사이트를 불러오지 못했습니다.")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState.Loading)

    class Factory(
        private val observeInsightsUseCase: ObserveInsightsUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = InsightsViewModel(observeInsightsUseCase) as T
    }
}
