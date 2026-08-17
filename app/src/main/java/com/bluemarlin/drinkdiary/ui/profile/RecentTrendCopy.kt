package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.RecentTrend
import com.bluemarlin.drinkdiary.domain.usecase.TrendThresholds
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDTrendBar
import kotlin.math.abs

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
    // 만족도는 1~5 척도다. 막대가 나타내는 것은 점수이지 빈도가 아니다(F3-4 (b)).
    private const val MAX_RATING = 5.0

    fun caption(trend: RecentTrend): String = "최근 ${trend.recentCount}잔을 그 이전 ${trend.earlierCount}잔과 견줘 봤어요."

    // 축 이름을 앞에 붙이지 않는다. 양극 이름이 이미 어느 축인지 말해 주고, 붙이면
    // "여운은 그 이전보다 긴 여운에"처럼 같은 말이 두 번 나온다.
    fun shiftLine(trend: RecentTrend): String {
        val shift = trend.shift ?: return "답하신 방향은 그 이전과 크게 다르지 않아요."
        return "그 이전보다 ${DrinkLabels.answer(shift.trait, shift.direction)}에 가깝게 답하셨어요."
    }

    // **강조색은 점수가 높은 쪽에 붙는다. '최근'에 붙는 것이 아니다.**
    // 라벨 절에서 초록이 "높게 준 쪽"을 뜻하는데 여기서 초록이 "최근"을 뜻하면, 최근에 더
    // 낮게 준 사용자는 낮은 막대가 초록인 화면을 보게 된다 — 색이 거짓말을 하는 것이다.
    fun recentBar(trend: RecentTrend): DDTrendBar =
        DDTrendBar(
            label = "최근 ${trend.recentCount}잔",
            value = DrinkLabels.rating(trend.recentAverageRating),
            fraction = (trend.recentAverageRating / MAX_RATING).toFloat(),
            emphasised = ratingGapMatters(trend) && trend.ratingDelta > 0,
        )

    fun earlierBar(trend: RecentTrend): DDTrendBar =
        DDTrendBar(
            label = "그 이전 ${trend.earlierCount}잔",
            value = DrinkLabels.rating(trend.earlierAverageRating),
            fraction = (trend.earlierAverageRating / MAX_RATING).toFloat(),
            emphasised = ratingGapMatters(trend) && trend.ratingDelta < 0,
        )

    // 막대 둘이 이미 길이로 말하지만, 임계 미만의 차이를 사용자가 차이로 읽지 않도록
    // 한 줄로 못박는다. 여덟 글자면 충분하다.
    fun verdict(trend: RecentTrend): String =
        when {
            trend.ratingDelta >= TrendThresholds.MIN_RATING_DELTA -> "그 이전보다 높게 주셨어요."
            trend.ratingDelta <= -TrendThresholds.MIN_RATING_DELTA -> "그 이전보다 낮게 주셨어요."
            else -> "그 이전과 비슷하게 주셨어요."
        }

    fun ratingGapMatters(trend: RecentTrend): Boolean = abs(trend.ratingDelta) >= TrendThresholds.MIN_RATING_DELTA
}
