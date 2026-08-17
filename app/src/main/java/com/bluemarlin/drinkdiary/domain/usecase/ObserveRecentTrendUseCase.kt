package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.RecentTrend
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TraitShift
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.abs

// 최근 N잔을 그 이전 기록과 대조한다(prd.md F3-3 (a)).
//
// **"최근"은 개수로 자른다. 기간으로 자르지 않는다.** 두 달 쉬었다 돌아온 사용자에게
// 시간 창은 비어 있고, 그러면 돌아온 바로 그날 화면이 사라진다. 개수로 자르면 현재 시각을
// 볼 일이 없어 `Clock` 주입도 필요 없다 — 실행 시점이 결과를 바꾸지 않는다.
class ObserveRecentTrendUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(scope: TypeScope): Flow<RecentTrend?> {
        val drinkType =
            when (scope) {
                TypeScope.Wine -> DrinkType.Wine
                TypeScope.Whiskey -> DrinkType.Whiskey
                TypeScope.Combined -> null
            }

        return repository.observeRecords(drinkType).map(::trendOf)
    }

    private fun trendOf(records: List<DrinkRecord>): RecentTrend? {
        // 저장소가 정렬해 주더라도 여기서 다시 정렬한다. "최근"의 정의가 데이터 계층의
        // 쿼리에 달려 있으면, 그 쿼리를 고치는 날 이 화면이 조용히 틀린 말을 한다.
        val ordered = records.sortedByDescending { it.recordedAtMillis }
        val recent = ordered.take(TrendThresholds.RECENT_WINDOW)
        val earlier = ordered.drop(TrendThresholds.RECENT_WINDOW)

        // 대조군이 없으면 "최근"이라는 말 자체가 성립하지 않는다.
        if (recent.size < TrendThresholds.MIN_EACH_SIDE) return null
        if (earlier.size < TrendThresholds.MIN_EACH_SIDE) return null

        return RecentTrend(
            recentCount = recent.size,
            earlierCount = earlier.size,
            recentAverageRating = recent.map { it.rating }.average(),
            earlierAverageRating = earlier.map { it.rating }.average(),
            shift = shiftOf(recent, earlier),
        )
    }

    // 공통 축만 본다. 기본 경로가 매번 묻는 것이 그 넷이라 표본이 고르다. 고유 축은 확장
    // 경로에서만 답이 달리므로, 거기서 나온 변화는 답이 달라진 것인지 물어본 횟수가 달라진
    // 것인지 구분되지 않는다.
    private fun shiftOf(
        recent: List<DrinkRecord>,
        earlier: List<DrinkRecord>,
    ): TraitShift? =
        Trait.shared
            .mapNotNull { trait ->
                val recentLevels = recent.mapNotNull { it.taste[trait]?.level }
                val earlierLevels = earlier.mapNotNull { it.taste[trait]?.level }
                if (recentLevels.size < TrendThresholds.MIN_EACH_SIDE) return@mapNotNull null
                if (earlierLevels.size < TrendThresholds.MIN_EACH_SIDE) return@mapNotNull null

                val delta = recentLevels.average() - earlierLevels.average()
                // 작은 흔들림을 방향으로 바꾸지 않는다. 없는 변화를 말하는 것은
                // 없는 취향을 지어내는 것과 같은 종류의 거짓말이다.
                if (abs(delta) < TrendThresholds.MIN_LEVEL_SHIFT) null else trait to delta
            }
            // 가장 크게 움직인 축 하나만 말한다. 동점이면 축 선언 순서 —
            // 같은 데이터에서 화면이 매번 다른 축을 고르면 그것 자체가 신뢰를 깎는다.
            .maxByOrNull { abs(it.second) }
            ?.let { (trait, delta) ->
                TraitShift(trait, if (delta > 0) TraitAnswer.High else TraitAnswer.Low)
            }
}

object TrendThresholds {
    // "최근"으로 묶는 잔 수.
    const val RECENT_WINDOW = 5

    // 최근 쪽과 그 이전 쪽 모두 이만큼은 있어야 대조가 성립한다.
    const val MIN_EACH_SIDE = 3

    // 축 답의 평균이 이만큼(0~2 척도) 움직여야 방향을 말한다.
    const val MIN_LEVEL_SHIFT = 0.6

    // 만족도가 이만큼 벌어져야 높다/낮다고 말한다 (평점 1~5 기준).
    const val MIN_RATING_DELTA = 0.5
}
