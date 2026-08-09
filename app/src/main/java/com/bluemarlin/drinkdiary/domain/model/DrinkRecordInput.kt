package com.bluemarlin.drinkdiary.domain.model

data class DrinkRecordInput(
    val id: Long = 0L,
    val type: DrinkType? = null,
    val name: String = "",
    val imageUri: String? = null,
    val priceText: String = "",
    val place: String = "",
    val tastingNote: String = "",
    /** Set rather than List because membership is checked on every chip render. */
    val tastingTags: Set<String> = emptySet(),
    val rating: Double = 0.0,
    val abv: Double? = null,
    val volumeMl: Int? = null,
    val collectionStatus: CollectionStatus? = CollectionStatus.Normal,
    val recordedAtMillis: Long = System.currentTimeMillis(),
)
