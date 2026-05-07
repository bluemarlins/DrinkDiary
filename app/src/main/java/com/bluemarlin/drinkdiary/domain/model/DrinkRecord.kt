package com.bluemarlin.drinkdiary.domain.model

data class DrinkRecord(
    val id: Long,
    val type: DrinkType,
    val name: String,
    val imageUri: String?,
    val price: Long?,
    val place: String?,
    val tastingNote: String?,
    val rating: Double,
    val ratingBreakdown: DrinkRatingBreakdown,
    val collectionStatus: CollectionStatus,
    val recordedAtMillis: Long,
)
