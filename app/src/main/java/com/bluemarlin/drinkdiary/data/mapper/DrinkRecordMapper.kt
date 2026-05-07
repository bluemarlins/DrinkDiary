package com.bluemarlin.drinkdiary.data.mapper

import com.bluemarlin.drinkdiary.data.local.DrinkRecordEntity
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRatingBreakdown
import com.bluemarlin.drinkdiary.domain.model.DrinkType

fun DrinkRecordEntity.toDomain(): DrinkRecord? {
    val drinkType = DrinkType.fromStorageValue(type) ?: return null
    val status = CollectionStatus.fromStorageValue(collectionStatus) ?: return null
    return DrinkRecord(
        id = id,
        type = drinkType,
        name = name,
        imageUri = imageUri,
        price = price,
        place = place,
        tastingNote = tastingNote,
        rating = rating,
        ratingBreakdown = DrinkRatingBreakdown(
            first = detailRating1,
            second = detailRating2,
            third = detailRating3,
            fourth = detailRating4,
        ),
        collectionStatus = status,
        recordedAtMillis = recordedAtMillis,
    )
}

fun DrinkRecord.toEntity(
    createdAtMillis: Long,
    updatedAtMillis: Long,
): DrinkRecordEntity = DrinkRecordEntity(
    id = id,
    type = type.name,
    name = name,
    imageUri = imageUri,
    price = price,
    place = place,
    tastingNote = tastingNote,
    rating = rating,
    detailRating1 = ratingBreakdown.first,
    detailRating2 = ratingBreakdown.second,
    detailRating3 = ratingBreakdown.third,
    detailRating4 = ratingBreakdown.fourth,
    collectionStatus = collectionStatus.name,
    recordedAtMillis = recordedAtMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)
