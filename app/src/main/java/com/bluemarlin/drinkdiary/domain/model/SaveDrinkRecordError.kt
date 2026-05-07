package com.bluemarlin.drinkdiary.domain.model

data class SaveDrinkRecordError(
    val type: String? = null,
    val name: String? = null,
    val price: String? = null,
    val rating: String? = null,
    val collectionStatus: String? = null,
    val recordedAt: String? = null,
) {
    val hasError: Boolean
        get() = listOf(type, name, price, rating, collectionStatus, recordedAt).any { it != null }
}
