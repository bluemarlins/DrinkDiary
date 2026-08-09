package com.bluemarlin.drinkdiary.domain.model

/**
 * Ratings are half-star steps from 0.5 to 5.0. 0.0 is deliberately invalid — it is the
 * unset value, so requiring a real rating is what forces the user to actually rate.
 */
fun Double.isValidRating(): Boolean = this in 0.5..5.0 && (this * 2).rem(1.0) == 0.0

fun roundToHalf(value: Double): Double = kotlin.math.round(value * 2.0) / 2.0
