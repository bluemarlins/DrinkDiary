package com.bluemarlin.drinkdiary.domain.model

import com.bluemarlin.drinkdiary.R

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
    val labelRes: Int,
    val descriptionRes: Int,
    val minLabelRes: Int,
    val maxLabelRes: Int,
)

fun DrinkType.ratingCriteria(): List<DrinkRatingCriterion> =
    when (this) {
        DrinkType.Wine ->
            listOf(
                DrinkRatingCriterion(
                    0,
                    R.string.criterion_label_sweetness,
                    R.string.criterion_desc_sweetness,
                    R.string.criterion_min_sweetness,
                    R.string.criterion_max_sweetness,
                ),
                DrinkRatingCriterion(
                    1,
                    R.string.criterion_label_acidity,
                    R.string.criterion_desc_acidity,
                    R.string.criterion_min_acidity,
                    R.string.criterion_max_acidity,
                ),
                DrinkRatingCriterion(
                    2,
                    R.string.criterion_label_tannin,
                    R.string.criterion_desc_tannin,
                    R.string.criterion_min_tannin,
                    R.string.criterion_max_tannin,
                ),
                DrinkRatingCriterion(
                    3,
                    R.string.criterion_label_body,
                    R.string.criterion_desc_body,
                    R.string.criterion_min_body,
                    R.string.criterion_max_body,
                ),
                DrinkRatingCriterion(
                    4,
                    R.string.criterion_label_aroma,
                    R.string.criterion_desc_aroma_wine,
                    R.string.criterion_min_aroma,
                    R.string.criterion_max_aroma,
                ),
            )
        DrinkType.Whiskey ->
            listOf(
                DrinkRatingCriterion(
                    0,
                    R.string.criterion_label_aroma,
                    R.string.criterion_desc_aroma_whiskey,
                    R.string.criterion_min_aroma,
                    R.string.criterion_max_aroma_whiskey,
                ),
                DrinkRatingCriterion(
                    1,
                    R.string.criterion_label_flavor_intensity,
                    R.string.criterion_desc_flavor_intensity,
                    R.string.criterion_min_flavor_intensity,
                    R.string.criterion_max_flavor_intensity,
                ),
                DrinkRatingCriterion(
                    2,
                    R.string.criterion_label_body,
                    R.string.criterion_desc_body,
                    R.string.criterion_min_body,
                    R.string.criterion_max_body,
                ),
                DrinkRatingCriterion(
                    3,
                    R.string.criterion_label_peat_smoky,
                    R.string.criterion_desc_peat_smoky,
                    R.string.criterion_min_peat_smoky,
                    R.string.criterion_max_peat_smoky,
                ),
                DrinkRatingCriterion(
                    4,
                    R.string.criterion_label_finish,
                    R.string.criterion_desc_finish,
                    R.string.criterion_min_finish,
                    R.string.criterion_max_finish,
                ),
            )
        DrinkType.Beer ->
            listOf(
                DrinkRatingCriterion(
                    0,
                    R.string.criterion_label_aroma,
                    R.string.criterion_desc_aroma_beer,
                    R.string.criterion_min_aroma,
                    R.string.criterion_max_aroma,
                ),
                DrinkRatingCriterion(
                    1,
                    R.string.criterion_label_bitterness,
                    R.string.criterion_desc_bitterness,
                    R.string.criterion_min_bitterness,
                    R.string.criterion_max_bitterness,
                ),
                DrinkRatingCriterion(
                    2,
                    R.string.criterion_label_carbonation,
                    R.string.criterion_desc_carbonation,
                    R.string.criterion_min_carbonation,
                    R.string.criterion_max_carbonation,
                ),
                DrinkRatingCriterion(
                    3,
                    R.string.criterion_label_body,
                    R.string.criterion_desc_body,
                    R.string.criterion_min_body,
                    R.string.criterion_max_body,
                ),
                DrinkRatingCriterion(
                    4,
                    R.string.criterion_label_mouthfeel,
                    R.string.criterion_desc_mouthfeel,
                    R.string.criterion_min_mouthfeel,
                    R.string.criterion_max_mouthfeel,
                ),
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
