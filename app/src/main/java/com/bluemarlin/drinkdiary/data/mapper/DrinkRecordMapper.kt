package com.bluemarlin.drinkdiary.data.mapper

import com.bluemarlin.drinkdiary.data.local.DrinkRecordEntity
import com.bluemarlin.drinkdiary.data.local.TASTING_TAG_DELIMITER
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
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
        tastingTags = tastingTags.toTagList(),
        rating = rating,
        abv = abv,
        volumeMl = volumeMl,
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
    tastingTags = tastingTags.toTagStorageValue(),
    rating = rating,
    abv = abv,
    volumeMl = volumeMl,
    collectionStatus = collectionStatus.name,
    recordedAtMillis = recordedAtMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)

/**
 * Wraps the joined keys in the delimiter as well as separating with it (`|a|b|`), so that a
 * tag filter can match on `|key|` and never on a partial key. Empty input stays an empty
 * string rather than a lone delimiter pair, which keeps "no tags" cheap to test for.
 *
 * Custom tags are user-typed, so a stray delimiter would corrupt the row's whole tag list —
 * strip it here rather than trusting the input, since this is the single point every write
 * passes through.
 */
fun List<String>.toTagStorageValue(): String {
    val cleaned = mapNotNull { it.replace(TASTING_TAG_DELIMITER, "").trim().takeIf(String::isNotEmpty) }
        .distinct()
    if (cleaned.isEmpty()) return ""
    return cleaned.joinToString(
        separator = TASTING_TAG_DELIMITER,
        prefix = TASTING_TAG_DELIMITER,
        postfix = TASTING_TAG_DELIMITER,
    )
}

fun String.toTagList(): List<String> =
    split(TASTING_TAG_DELIMITER).mapNotNull { it.trim().takeIf(String::isNotEmpty) }
