package com.bluemarlin.drinkdiary.domain.model

import com.bluemarlin.drinkdiary.R

enum class DrinkType(
    val labelRes: Int,
) {
    Wine(R.string.drink_type_wine),
    Whiskey(R.string.drink_type_whiskey),
    Beer(R.string.drink_type_beer),
    ;

    companion object {
        fun fromStorageValue(value: String): DrinkType? = entries.firstOrNull { it.name == value }
    }
}
