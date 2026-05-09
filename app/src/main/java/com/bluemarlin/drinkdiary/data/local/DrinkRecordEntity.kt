package com.bluemarlin.drinkdiary.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drink_records",
    indices = [
        Index(value = ["recordedAtMillis"]),
        Index(value = ["type"]),
        Index(value = ["collectionStatus"]),
        Index(value = ["type", "collectionStatus"]),
        Index(value = ["recordedAtMillis", "collectionStatus"]),
    ],
)
data class DrinkRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String,
    val name: String,
    val imageUri: String?,
    val price: Long?,
    val place: String?,
    val tastingNote: String?,
    val rating: Double,
    val detailRating1: Double,
    val detailRating2: Double,
    val detailRating3: Double,
    val detailRating4: Double,
    val detailRating5: Double,
    val collectionStatus: String,
    val recordedAtMillis: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
