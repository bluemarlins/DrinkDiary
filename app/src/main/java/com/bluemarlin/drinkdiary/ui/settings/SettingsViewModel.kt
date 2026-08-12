package com.bluemarlin.drinkdiary.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import com.bluemarlin.drinkdiary.domain.usecase.GenerateCsvExportUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isProUser: Boolean = false,
    val isExporting: Boolean = false,
)

sealed interface SettingsEvent {
    data class Exported(
        val csvContent: String,
    ) : SettingsEvent

    data class Error(
        val message: String,
    ) : SettingsEvent
}

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val generateCsvExportUseCase: GenerateCsvExportUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> =
        userPreferencesRepository.isProUser
            .map { isPro -> SettingsUiState(isProUser = isPro) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    fun exportToCsv() {
        viewModelScope.launch {
            // 실제 상용화 시에는 여기서 Pro 여부를 한 번 더 체크하거나 게이팅함
            val csv = generateCsvExportUseCase()
            _events.emit(SettingsEvent.Exported(csv))
        }
    }

    fun toggleProStatus() {
        viewModelScope.launch {
            val current = uiState.value.isProUser
            userPreferencesRepository.setProUser(!current)
        }
    }

    class Factory(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val generateCsvExportUseCase: GenerateCsvExportUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(userPreferencesRepository, generateCsvExportUseCase) as T
    }
}
