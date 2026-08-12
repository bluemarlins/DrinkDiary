package com.bluemarlin.drinkdiary.domain.model

data class SaveDrinkRecordError(
    val type: Int? = null,
    val name: Int? = null,
    val price: Int? = null,
    val rating: Int? = null,
    val collectionStatus: Int? = null,
    val recordedAt: Int? = null,
) {
    val hasError: Boolean
        get() = listOf(type, name, price, rating, collectionStatus, recordedAt).any { it != null }
}
