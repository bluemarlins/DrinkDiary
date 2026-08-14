package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.sqrt

// 축 값(Low=0, Mid=1, High=2)과 만족도의 상관으로 선호를 판정한다.
//
// 구 알고리즘은 High군과 Low군의 평균 만족도를 비교하고 Mid를 버렸다. 두 가지가 무너졌다 —
// 데이터의 절반을 버렸고(술의 절반 이상이 중간대), 표본이 늘면 노이즈만으로 관문이 열려
// 없는 취향을 지어냈다. software-architecture.md 4-1절.
class ObserveTasteProfileUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(scope: TypeScope): Flow<TasteProfile> {
        val (drinkType, traits) =
            when (scope) {
                TypeScope.Wine -> DrinkType.Wine to Trait.of(DrinkType.Wine)
                TypeScope.Whiskey -> DrinkType.Whiskey to Trait.of(DrinkType.Whiskey)
                TypeScope.Combined -> null to Trait.shared
            }

        return repository.observeRecords(drinkType).map { records ->
            TasteProfile(
                scope = scope,
                recordCount = records.size,
                preferences =
                    traits.map { trait ->
                        judge(
                            trait = trait,
                            pairs =
                                records.mapNotNull { record ->
                                    record.taste[trait]?.let { it to record.rating }
                                },
                        )
                    },
            )
        }
    }

    private fun judge(
        trait: Trait,
        pairs: List<Pair<TraitAnswer, Double>>,
    ): TraitPreference {
        val midSamples = pairs.count { it.first == TraitAnswer.Mid }

        // 표본 부족은 "취향이 없다"가 아니라 "아직 모른다"다. preference = null 로 구분한다.
        if (pairs.size < TasteThresholds.MIN_SAMPLES) {
            return TraitPreference(trait, null, pairs.size, 0.0, midSamples)
        }

        val correlation = correlationOf(pairs)

        // 상관이 정의되지 않는 경우(한쪽이 전부 같은 값)는 '관계 없음'으로 읽는다.
        // 예: 모든 기록에 Mid로 답했다면 그 축은 만족도를 가르지 않는다.
        val preference =
            when {
                correlation == null -> TastePreference.Neutral
                abs(correlation) < TasteThresholds.MIN_CORRELATION -> TastePreference.Neutral
                correlation > 0 -> TastePreference.High
                else -> TastePreference.Low
            }

        return TraitPreference(
            trait = trait,
            preference = preference,
            samples = pairs.size,
            strength = correlation?.let(::abs) ?: 0.0,
            midSamples = midSamples,
        )
    }

    // 피어슨 상관. 어느 한쪽이라도 분산이 0이면 정의되지 않으므로 null.
    private fun correlationOf(pairs: List<Pair<TraitAnswer, Double>>): Double? {
        val n = pairs.size
        val xs = pairs.map { it.first.level.toDouble() }
        val ys = pairs.map { it.second }
        val meanX = xs.sum() / n
        val meanY = ys.sum() / n

        var covariance = 0.0
        var varianceX = 0.0
        var varianceY = 0.0
        for (i in 0 until n) {
            val dx = xs[i] - meanX
            val dy = ys[i] - meanY
            covariance += dx * dy
            varianceX += dx * dx
            varianceY += dy * dy
        }

        if (varianceX == 0.0 || varianceY == 0.0) return null
        return covariance / sqrt(varianceX * varianceY)
    }
}
