package com.bluemarlin.drinkdiary.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTasteProfileUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ResolveProfileReadinessUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val scope: TypeScope = TypeScope.Wine,
    val profile: TasteProfile? = null,
    val readiness: ProfileReadiness = ProfileReadiness.NotReady,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val observeTasteProfile: ObserveTasteProfileUseCase,
    private val resolveReadiness: ResolveProfileReadinessUseCase,
) : ViewModel() {
    private val scope = MutableStateFlow(TypeScope.Wine)

    val uiState: StateFlow<ProfileUiState> =
        scope
            .flatMapLatest { selected -> observeTasteProfile(selected).map { selected to it } }
            .map { (selected, profile) ->
                ProfileUiState(
                    scope = selected,
                    profile = profile,
                    readiness = resolveReadiness(profile),
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun selectScope(newScope: TypeScope) {
        scope.value = newScope
    }

    class Factory(
        private val observeTasteProfile: ObserveTasteProfileUseCase,
        private val resolveReadiness: ResolveProfileReadinessUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProfileViewModel(observeTasteProfile, resolveReadiness) as T
    }
}
