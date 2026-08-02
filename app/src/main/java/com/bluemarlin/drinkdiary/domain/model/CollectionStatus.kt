package com.bluemarlin.drinkdiary.domain.model

enum class CollectionStatus(
    val label: String,
) {
    Normal("일반 기록"),
    Repurchase("재구매 후보"),
    NotForMe("비선호"),
    ;

    companion object {
        fun fromStorageValue(value: String): CollectionStatus? = entries.firstOrNull { it.name == value }
    }
}
