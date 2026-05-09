package com.bluemarlin.drinkdiary.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.usecase.ObserveSearchResultsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data class InvalidQuery(val query: String) : SearchUiState
    data object Loading : SearchUiState
    data class Empty(val query: String) : SearchUiState
    data class Success(val query: String, val records: List<DrinkRecord>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val observeSearchResultsUseCase: ObserveSearchResultsUseCase,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val uiState: StateFlow<SearchUiState> = _query
        .debounce(SearchDebounceMillis)
        .map { it.trim() }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            when {
                query.isEmpty() -> flowOf(SearchUiState.Idle)
                query.length < MinSearchQueryLength -> flowOf(SearchUiState.InvalidQuery(query))
                else -> observeSearchResultsUseCase(query)
                    .map<List<DrinkRecord>, SearchUiState> { records ->
                        if (records.isEmpty()) {
                            SearchUiState.Empty(query)
                        } else {
                            SearchUiState.Success(query, records)
                        }
                    }
                    .onStart { emit(SearchUiState.Loading) }
                    .catch { emit(SearchUiState.Error("검색하지 못했습니다. 다시 시도해 주세요.")) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState.Idle,
        )

    fun updateQuery(value: String) {
        _query.value = value
    }

    fun clearQuery() {
        _query.value = ""
    }

    class Factory(
        private val observeSearchResultsUseCase: ObserveSearchResultsUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SearchViewModel(observeSearchResultsUseCase) as T
    }

    private companion object {
        const val MinSearchQueryLength = 2
        const val SearchDebounceMillis = 250L
    }
}
