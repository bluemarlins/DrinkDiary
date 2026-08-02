package com.bluemarlin.drinkdiary.domain.model

data class DrinkRatingBreakdown(
    val first: Double = DefaultSensoryMetricValue,
    val second: Double = DefaultSensoryMetricValue,
    val third: Double = DefaultSensoryMetricValue,
    val fourth: Double = DefaultSensoryMetricValue,
    val fifth: Double = DefaultSensoryMetricValue,
) {
    val values: List<Double>
        get() = listOf(first, second, third, fourth, fifth)
}

data class DrinkRatingCriterion(
    val index: Int,
    val label: String,
    val description: String,
    val minLabel: String,
    val maxLabel: String,
)

fun DrinkType.ratingCriteria(): List<DrinkRatingCriterion> =
    when (this) {
        DrinkType.Wine ->
            listOf(
                DrinkRatingCriterion(0, "당도", "단맛의 정도를 기록해요.", "드라이함", "달콤함"),
                DrinkRatingCriterion(1, "산미", "입 안에서 느껴지는 신선함과 산뜻함을 기록해요.", "부드러움", "상큼함"),
                DrinkRatingCriterion(2, "탄닌", "떫은 느낌과 구조감을 기록해요.", "부드러움", "떫고 강함"),
                DrinkRatingCriterion(3, "바디감", "무게감과 밀도를 기록해요.", "가벼움", "묵직함"),
                DrinkRatingCriterion(4, "향", "향의 풍부함과 강도를 기록해요.", "은은함", "풍부함"),
            )
        DrinkType.Whiskey ->
            listOf(
                DrinkRatingCriterion(0, "향", "잔에서 느껴지는 향의 강도를 기록해요.", "은은함", "강렬함"),
                DrinkRatingCriterion(1, "맛의 진함", "입 안에서 느껴지는 풍미의 밀도를 기록해요.", "가벼움", "진함"),
                DrinkRatingCriterion(2, "바디감", "질감과 무게감을 기록해요.", "가벼움", "묵직함"),
                DrinkRatingCriterion(3, "피트/스모키", "스모키하거나 피트한 인상을 기록해요.", "없음", "강함"),
                DrinkRatingCriterion(4, "피니시", "여운의 길이를 기록해요.", "짧음", "김"),
            )
        DrinkType.Beer ->
            listOf(
                DrinkRatingCriterion(0, "향", "홉, 몰트, 과일, 효모 향의 강도를 기록해요.", "은은함", "풍부함"),
                DrinkRatingCriterion(1, "쓴맛", "홉에서 오는 쓴맛의 정도를 기록해요.", "낮음", "강함"),
                DrinkRatingCriterion(2, "탄산감", "탄산의 세기와 자극을 기록해요.", "부드러움", "강함"),
                DrinkRatingCriterion(3, "바디감", "무게감과 밀도를 기록해요.", "가벼움", "묵직함"),
                DrinkRatingCriterion(4, "목넘김", "마실 때의 산뜻함과 편안함을 기록해요.", "무거움", "산뜻함"),
            )
    }

fun DrinkRatingBreakdown.update(
    index: Int,
    rating: Double,
): DrinkRatingBreakdown =
    when (index) {
        0 -> copy(first = rating)
        1 -> copy(second = rating)
        2 -> copy(third = rating)
        3 -> copy(fourth = rating)
        4 -> copy(fifth = rating)
        else -> this
    }

fun DrinkRatingBreakdown.normalizedSensoryMetrics(): DrinkRatingBreakdown =
    copy(
        first = roundToHalf(first).coerceIn(0.0, 5.0),
        second = roundToHalf(second).coerceIn(0.0, 5.0),
        third = roundToHalf(third).coerceIn(0.0, 5.0),
        fourth = roundToHalf(fourth).coerceIn(0.0, 5.0),
        fifth = roundToHalf(fifth).coerceIn(0.0, 5.0),
    )

fun DrinkRatingCriterion.currentLabel(value: Double): String =
    when {
        value <= 1.0 -> minLabel
        value < 2.5 -> "$minLabel 쪽"
        value <= 3.0 -> "중간"
        value < 4.5 -> "$maxLabel 쪽"
        else -> maxLabel
    }

fun Double.isValidOverallRating(): Boolean {
    val scaled = this * 10.0
    return this in 0.0..5.0 && kotlin.math.abs(scaled - kotlin.math.round(scaled)) < 0.0001
}

fun Double.isValidSensoryMetric(): Boolean {
    val scaled = this * 2.0
    return this in 0.0..5.0 && kotlin.math.abs(scaled - kotlin.math.round(scaled)) < 0.0001
}

fun roundToHalf(value: Double): Double = kotlin.math.round(value * 2.0) / 2.0

fun roundToTenth(value: Double): Double = kotlin.math.round(value * 10.0) / 10.0

const val DefaultSensoryMetricValue = 2.5
