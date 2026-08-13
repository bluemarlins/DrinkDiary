package com.bluemarlin.drinkdiary.data.mapper

import com.bluemarlin.drinkdiary.data.local.DrinkRecordEntity
import com.bluemarlin.drinkdiary.data.local.DrinkRecordWithAnswers
import com.bluemarlin.drinkdiary.data.local.TraitAnswerEntity
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer

fun DrinkRecordWithAnswers.toDomain(): DrinkRecord? {
    val type = runCatching { DrinkType.valueOf(record.type) }.getOrNull() ?: return null
    val servingStyle =
        record.servingStyle?.let {
            runCatching { ServingStyle.valueOf(it) }.getOrNull()
        }
    val collectionStatus = runCatching { CollectionStatus.valueOf(record.collectionStatus) }.getOrNull() ?: return null

    val traitMap = mutableMapOf<Trait, TraitAnswer>()
    for (answerEntity in answers) {
        val trait = runCatching { Trait.valueOf(answerEntity.trait) }.getOrNull()
        val answer = runCatching { TraitAnswer.valueOf(answerEntity.answer) }.getOrNull()
        if (trait != null && answer != null) {
            traitMap[trait] = answer
        }
    }

    return DrinkRecord(
        id = record.id,
        type = type,
        name = record.name,
        vintage = record.vintage,
        servingStyle = servingStyle,
        taste = TasteInput(traitMap),
        rating = record.rating,
        collectionStatus = collectionStatus,
        imageUri = record.imageUri,
        price = record.price,
        place = record.place,
        memo = record.memo,
        recordedAtMillis = record.recordedAtMillis,
    )
}

fun DrinkRecord.toEntity(): DrinkRecordEntity =
    DrinkRecordEntity(
        id = id,
        type = type.name,
        name = name,
        vintage = vintage,
        servingStyle = servingStyle?.name,
        rating = rating,
        collectionStatus = collectionStatus.name,
        imageUri = imageUri,
        price = price,
        place = place,
        memo = memo,
        recordedAtMillis = recordedAtMillis,
    )

fun DrinkRecord.toAnswerEntities(recordId: Long): List<TraitAnswerEntity> =
    taste.answers.map { (trait, answer) ->
        TraitAnswerEntity(
            recordId = recordId,
            trait = trait.name,
            answer = answer.name,
        )
    }
