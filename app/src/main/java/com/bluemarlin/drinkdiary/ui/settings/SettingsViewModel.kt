package com.bluemarlin.drinkdiary.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val loaded: Boolean = false,
    val alwaysAskTags: Set<TagCategory> = emptySet(),
)

class SettingsViewModel(
    private val preferences: UserPreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.alwaysAskTags.collect { tags ->
                _uiState.update { it.copy(loaded = true, alwaysAskTags = tags) }
            }
        }
    }

    // 누르는 즉시 저장한다. 설정에 "저장" 버튼을 두면 안 누르고 나가서 안 바뀐다.
    fun toggle(category: TagCategory) {
        val current = _uiState.value.alwaysAskTags
        val next = if (category in current) current - category else current + category
        viewModelScope.launch { preferences.setAlwaysAskTags(next) }
    }

    class Factory(
        private val preferences: UserPreferencesRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(preferences) as T
    }
}
