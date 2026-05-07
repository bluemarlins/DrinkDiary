package com.bluemarlin.drinkdiary.domain.model

data class DrinkRecordInput(
    val id: Long = 0L,
    val type: DrinkType? = null,
    val name: String = "",
    val imageUri: String? = null,
    val priceText: String = "",
    val place: String = "",
    val tastingNote: String = "",
    val rating: Double = 0.0,
    val ratingBreakdown: DrinkRatingBreakdown = DrinkRatingBreakdown(),
    val ratingBreakdownExpanded: Boolean = false,
    val collectionStatus: CollectionStatus? = CollectionStatus.Normal,
    val recordedAtMillis: Long = System.currentTimeMillis(),
)
