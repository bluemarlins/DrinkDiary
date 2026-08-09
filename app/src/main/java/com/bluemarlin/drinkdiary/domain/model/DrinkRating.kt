package com.bluemarlin.drinkdiary.domain.model

import kotlin.math.abs
import kotlin.math.round

/** Smallest adjustment the rating slider makes. */
const val RATING_STEP = 0.1

const val MIN_RATING = 0.5
const val MAX_RATING = 5.0

/**
 * Snaps to one decimal place. Slider positions are continuous floats, so raw values must be
 * quantized before they are validated or stored — otherwise 4.300000000000001 reaches the
 * database and every later comparison has to carry the same tolerance.
 */
fun roundToStep(value: Double): Double = round(value * 10.0) / 10.0

/**
 * Ratings run [MIN_RATING]..[MAX_RATING] in [RATING_STEP] increments. 0.0 is deliberately
 * invalid — it is the unset value, so requiring a real rating is what forces the user to rate.
 *
 * The step check compares against the rounded value with a tolerance rather than testing
 * `(this * 10).rem(1.0) == 0.0`: multiplying by ten reintroduces binary-float error (4.3 * 10
 * is 43.000000000000007), so the naive form rejects values the slider legitimately produces.
 *
 * Ratings saved before this was 0.1-based used 0.5 steps, which are exact multiples of 0.1
 * and stay valid — no data migration was needed for the change.
 */
fun Double.isValidRating(): Boolean =
    this in MIN_RATING..MAX_RATING && abs(this - roundToStep(this)) < 1e-9
