package com.bluemarlin.drinkdiary.domain.model

data class DrinkRecord(
    val id: Long = 0L,
    val type: DrinkType,
    val name: String,
    // P4 — 같은 이름이 같은 맛을 보장하지 않는 변수들
    val vintage: Int? = null,
    val servingStyle: ServingStyle? = null,
    val taste: TasteInput = TasteInput(),
    val rating: Double,
    val collectionStatus: CollectionStatus = CollectionStatus.Normal,
    val imageUri: String? = null,
    val price: Long? = null,
    val place: String? = null,
    val memo: String? = null,
    val recordedAtMillis: Long,
)
