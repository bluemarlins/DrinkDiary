package com.bluemarlin.drinkdiary.domain.model

data class DrinkRatingBreakdown(
    val first: Double = 0.0,
    val second: Double = 0.0,
    val third: Double = 0.0,
    val fourth: Double = 0.0,
) {
    val values: List<Double>
        get() = listOf(first, second, third, fourth)

    val average: Double
        get() = roundToTenth(values.average())

    companion object {
        fun fromRepresentativeRating(rating: Double): DrinkRatingBreakdown =
            DrinkRatingBreakdown(rating, rating, rating, rating)
    }
}

data class DrinkRatingCriterion(
    val index: Int,
    val label: String,
)

fun DrinkType.ratingCriteria(): List<DrinkRatingCriterion> = when (this) {
    DrinkType.Wine -> listOf(
        DrinkRatingCriterion(0, "향"),
        DrinkRatingCriterion(1, "산도"),
        DrinkRatingCriterion(2, "바디감"),
        DrinkRatingCriterion(3, "밸런스"),
    )
    DrinkType.Whiskey -> listOf(
        DrinkRatingCriterion(0, "향"),
        DrinkRatingCriterion(1, "맛"),
        DrinkRatingCriterion(2, "바디감"),
        DrinkRatingCriterion(3, "피니시"),
    )
    DrinkType.Beer -> listOf(
        DrinkRatingCriterion(0, "향"),
        DrinkRatingCriterion(1, "맛"),
        DrinkRatingCriterion(2, "탄산감"),
        DrinkRatingCriterion(3, "음용성"),
    )
}

fun DrinkRatingBreakdown.update(index: Int, rating: Double): DrinkRatingBreakdown = when (index) {
    0 -> copy(first = rating)
    1 -> copy(second = rating)
    2 -> copy(third = rating)
    3 -> copy(fourth = rating)
    else -> this
}

fun Double.isValidRating(): Boolean {
    val scaled = this * 10.0
    return this in 0.5..5.0 && kotlin.math.abs(scaled - kotlin.math.round(scaled)) < 0.0001
}

fun roundToHalf(value: Double): Double = kotlin.math.round(value * 2.0) / 2.0

fun roundToTenth(value: Double): Double = kotlin.math.round(value * 10.0) / 10.0
