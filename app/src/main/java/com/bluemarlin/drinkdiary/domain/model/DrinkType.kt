package com.bluemarlin.drinkdiary.domain.model

enum class DrinkType(
    val label: String,
) {
    Wine("와인"),
    Whiskey("위스키"),
    Beer("맥주"),
    ;

    companion object {
        fun fromStorageValue(value: String): DrinkType? = entries.firstOrNull { it.name == value }
    }
}
