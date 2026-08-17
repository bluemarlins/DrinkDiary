package com.bluemarlin.drinkdiary.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.MonthlySummary
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.RecentTrend
import com.bluemarlin.drinkdiary.domain.model.TagPreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TastingGap
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.usecase.ObserveMonthlySummaryUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveRecentTrendUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTagPreferenceUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTasteProfileUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTastingGapsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ResolveProfileReadinessUseCase
import com.bluemarlin.drinkdiary.domain.usecase.TasteThresholds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val scope: TypeScope = TypeScope.Wine,
    val profile: TasteProfile? = null,
    val tagPreferences: List<TagPreference> = emptyList(),
    // 첫 프레임의 기본값. 실제 값은 UseCase가 계산해 곧바로 덮어쓴다.
    val readiness: ProfileReadiness =
        ProfileReadiness.NotReady(recordsNeeded = TasteThresholds.MIN_SAMPLES),
    // 이번 달 회고는 스코프와 무관하다 — 와인 탭을 봐도 이번 달에 마신 위스키는 마신 것이다.
    val monthly: MonthlySummary = MonthlySummary.Empty,
    // 대조군이 모이기 전에는 null이다. 없는 흐름을 지어내지 않는다(prd.md F3-3 (a)).
    val recentTrend: RecentTrend? = null,
    // 한쪽만 쌓인 자리. 추천이 아니라 공백 안내다(prd.md F3-3 (b)).
    val tastingGaps: List<TastingGap> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val observeTasteProfile: ObserveTasteProfileUseCase,
    private val observeTagPreference: ObserveTagPreferenceUseCase,
    private val resolveReadiness: ResolveProfileReadinessUseCase,
    private val observeMonthlySummary: ObserveMonthlySummaryUseCase,
    private val observeRecentTrend: ObserveRecentTrendUseCase,
    private val observeTastingGaps: ObserveTastingGapsUseCase,
) : ViewModel() {
    private val scope = MutableStateFlow(TypeScope.Wine)

    val uiState: StateFlow<ProfileUiState> =
        scope
            .flatMapLatest { selected ->
                // 감각 축과 태그는 별개 경로다. 한쪽이 비어도 다른 쪽은 말할 수 있어야 한다.
                // 월 요약은 스코프 밖이라 flatMapLatest 안에서 다시 구독해도 같은 값이 온다.
                combine(
                    observeTasteProfile(selected),
                    observeTagPreference(selected),
                    observeMonthlySummary(),
                    observeRecentTrend(selected),
                    observeTastingGaps(selected),
                ) { profile, tags, monthly, trend, gaps ->
                    ProfileUiState(
                        scope = selected,
                        profile = profile,
                        tagPreferences = tags,
                        readiness = resolveReadiness(profile),
                        monthly = monthly,
                        recentTrend = trend,
                        tastingGaps = gaps,
                    )
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun selectScope(newScope: TypeScope) {
        scope.value = newScope
    }

    class Factory(
        private val observeTasteProfile: ObserveTasteProfileUseCase,
        private val observeTagPreference: ObserveTagPreferenceUseCase,
        private val resolveReadiness: ResolveProfileReadinessUseCase,
        private val observeMonthlySummary: ObserveMonthlySummaryUseCase,
        private val observeRecentTrend: ObserveRecentTrendUseCase,
        private val observeTastingGaps: ObserveTastingGapsUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProfileViewModel(
                observeTasteProfile,
                observeTagPreference,
                resolveReadiness,
                observeMonthlySummary,
                observeRecentTrend,
                observeTastingGaps,
            ) as T
    }
}
