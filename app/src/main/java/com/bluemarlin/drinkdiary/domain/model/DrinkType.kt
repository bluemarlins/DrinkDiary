package com.bluemarlin.drinkdiary.domain.model

enum class DrinkType {
    Wine,
    Whiskey,
    ;

    companion object {
        fun fromName(name: String?): DrinkType? = entries.firstOrNull { it.name == name }
    }
}
