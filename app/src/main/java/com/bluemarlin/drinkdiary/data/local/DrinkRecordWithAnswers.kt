package com.bluemarlin.drinkdiary.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class DrinkRecordWithAnswers(
    @Embedded val record: DrinkRecordEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recordId",
    )
    val answers: List<TraitAnswerEntity>,
)
