package com.bluemarlin.drinkdiary.domain.model

data class DrinkRecordFilter(
    val drinkType: DrinkType? = null,
    val collectionStatus: CollectionStatus? = null,
)
