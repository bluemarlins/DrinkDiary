package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.RecentTrend
import com.bluemarlin.drinkdiary.domain.usecase.TrendThresholds
import com.bluemarlin.drinkdiary.ui.DrinkLabels

// 최근 흐름의 문구(prd.md F3-3 (a)).
//
// **서술어가 '답하셨어요'인 것이 이 절의 전부다.** "요즘 묵직한 걸 좋아하시네요"로 쓰는 순간
// 이 카드는 취향 판정이 되고, 그러면 화면이 근거라고 내놓는 것(최근 다섯 잔의 답)이 실제
// 판정 근거(축값과 만족도의 상관)와 달라진다. 유형 카드 바로 아래에 놓이므로 그 혼동은
// 실제로 일어난다.
//
// 축값의 평균은 방향을 고르는 데만 쓰고 화면에 숫자로 내보내지 않는다 — 평균을 보여주면
// 그 숫자가 판정 근거로 읽힌다(F3-3 공통 제약).
object RecentTrendCopy {
    fun caption(trend: RecentTrend): String = "최근 ${trend.recentCount}잔을 그 이전 ${trend.earlierCount}잔과 견줘 봤어요."

    fun lines(trend: RecentTrend): List<String> = listOf(shiftLine(trend), ratingLine(trend))

    // 축 이름을 앞에 붙이지 않는다. 양극 이름이 이미 어느 축인지 말해 주고, 붙이면
    // "여운은 그 이전보다 긴 여운에"처럼 같은 말이 두 번 나온다.
    private fun shiftLine(trend: RecentTrend): String {
        val shift = trend.shift ?: return "답하신 방향은 그 이전과 크게 다르지 않아요."
        return "그 이전보다 ${DrinkLabels.answer(shift.trait, shift.direction)}에 가깝게 답하셨어요."
    }

    // 만족도 평균은 회고다(F3-2의 '이번 달'과 같은 성격). 판정의 근거로 배치하지 않으려면
    // 문장이 술이 아니라 **행위**를 서술해야 한다 — "점수를 주셨어요".
    private fun ratingLine(trend: RecentTrend): String {
        val recent = DrinkLabels.rating(trend.recentAverageRating)
        val earlier = DrinkLabels.rating(trend.earlierAverageRating)
        val tail =
            when {
                trend.ratingDelta >= TrendThresholds.MIN_RATING_DELTA -> "그 이전($earlier)보다 높아요"
                trend.ratingDelta <= -TrendThresholds.MIN_RATING_DELTA -> "그 이전($earlier)보다 낮아요"
                else -> "그 이전($earlier)과 비슷해요"
            }
        // 만족도 표기는 항상 "…점"으로 끝나므로 조사는 '으로'로 고정된다.
        return "최근에는 평균 ${recent}으로 점수를 주셨어요. $tail."
    }
}
