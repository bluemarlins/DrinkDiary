package com.bluemarlin.drinkdiary.domain.model

enum class DrinkType(val label: String) {
    Wine("와인"),
    Whiskey("위스키"),
    Beer("맥주");

    companion object {
        fun fromStorageValue(value: String): DrinkType? = entries.firstOrNull { it.name == value }
    }
}

/**
 * Typical strength and serving size per type, used to pre-fill the editor and to stand in for
 * records where the user never entered a figure. These are deliberately ordinary values, not
 * averages of anything — they exist so intake can be estimated without asking the user to
 * measure. Changing them retroactively changes the estimate for records that left the field
 * blank, which is intended: a better default is a better estimate.
 */
fun DrinkType.defaultAbv(): Double = when (this) {
    DrinkType.Wine -> 12.0
    DrinkType.Whiskey -> 40.0
    DrinkType.Beer -> 5.0
}

fun DrinkType.defaultVolumeMl(): Int = when (this) {
    DrinkType.Wine -> 150
    DrinkType.Whiskey -> 30
    DrinkType.Beer -> 500
}
