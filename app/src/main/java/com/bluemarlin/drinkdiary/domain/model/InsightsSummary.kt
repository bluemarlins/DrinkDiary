package com.bluemarlin.drinkdiary.domain.model

data class InsightsSummary(
    val monthlyTrend: List<MonthlyInsight>,
    val priceBrackets: List<PriceBracketInsight>,
) {
    companion object {
        val Empty = InsightsSummary(monthlyTrend = emptyList(), priceBrackets = emptyList())
    }
}

data class MonthlyInsight(
    val yearMonthLabel: String,
    val totalCount: Int,
    val averageRating: Double?,
    val repurchaseRate: Double?,
)

enum class PriceBracket(
    val label: String,
) {
    Under20k("2만원 미만"),
    Between20kAnd50k("2만원대~4만원대"),
    Between50kAnd100k("5만원대~9만원대"),
    Over100k("10만원 이상"),
}

data class PriceBracketInsight(
    val bracket: PriceBracket,
    val count: Int,
    val averageRating: Double?,
)
