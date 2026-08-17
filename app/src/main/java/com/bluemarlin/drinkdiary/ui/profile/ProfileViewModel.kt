package com.bluemarlin.drinkdiary.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.AnswerReflection
import com.bluemarlin.drinkdiary.domain.model.DrinkHighlight
import com.bluemarlin.drinkdiary.domain.model.MonthlySummary
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.RecentTrend
import com.bluemarlin.drinkdiary.domain.model.TagPreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TastingGap
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.usecase.ObserveAnswerReflectionUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDrinkHighlightsUseCase
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
    // 판정 전 구간에서만 쓴다. **답의 되비침이지 취향이 아니다**(prd.md F3-3 (d)).
    val reflection: AnswerReflection = AnswerReflection.Empty,
    // 읽지 않아도 보이는 층(prd.md F3-4 (a)).
    val highlights: List<DrinkHighlight> = emptyList(),
)

// F3-3·F3-4의 층들을 한 묶음으로 합친다. `combine`의 타입 있는 오버로드가 다섯 개까지라
// 여섯 번째부터는 vararg + 캐스팅이 되는데, 그 캐스팅은 컴파일러가 지켜 주지 않는다.
private data class DashboardInsights(
    val trend: RecentTrend?,
    val gaps: List<TastingGap>,
    val reflection: AnswerReflection,
    val highlights: List<DrinkHighlight>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val observeTasteProfile: ObserveTasteProfileUseCase,
    private val observeTagPreference: ObserveTagPreferenceUseCase,
    private val resolveReadiness: ResolveProfileReadinessUseCase,
    private val observeMonthlySummary: ObserveMonthlySummaryUseCase,
    private val observeRecentTrend: ObserveRecentTrendUseCase,
    private val observeTastingGaps: ObserveTastingGapsUseCase,
    private val observeAnswerReflection: ObserveAnswerReflectionUseCase,
    private val observeDrinkHighlights: ObserveDrinkHighlightsUseCase,
) : ViewModel() {
    private val scope = MutableStateFlow(TypeScope.Wine)

    val uiState: StateFlow<ProfileUiState> =
        scope
            .flatMapLatest { selected ->
                val insights =
                    combine(
                        observeRecentTrend(selected),
                        observeTastingGaps(selected),
                        observeAnswerReflection(selected),
                        observeDrinkHighlights(selected),
                        ::DashboardInsights,
                    )

                // 감각 축과 태그는 별개 경로다. 한쪽이 비어도 다른 쪽은 말할 수 있어야 한다.
                // 월 요약은 스코프 밖이라 flatMapLatest 안에서 다시 구독해도 같은 값이 온다.
                combine(
                    observeTasteProfile(selected),
                    observeTagPreference(selected),
                    observeMonthlySummary(),
                    insights,
                ) { profile, tags, monthly, extra ->
                    ProfileUiState(
                        scope = selected,
                        profile = profile,
                        tagPreferences = tags,
                        readiness = resolveReadiness(profile),
                        monthly = monthly,
                        recentTrend = extra.trend,
                        tastingGaps = extra.gaps,
                        reflection = extra.reflection,
                        highlights = extra.highlights,
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
        private val observeAnswerReflection: ObserveAnswerReflectionUseCase,
        private val observeDrinkHighlights: ObserveDrinkHighlightsUseCase,
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
                observeAnswerReflection,
                observeDrinkHighlights,
            ) as T
    }
}
