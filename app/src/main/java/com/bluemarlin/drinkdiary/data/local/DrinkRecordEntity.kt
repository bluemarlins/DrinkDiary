package com.bluemarlin.drinkdiary.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drink_records")
data class DrinkRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String,
    val name: String,
    val vintage: Int?,
    val servingStyle: String?,
    val rating: Double,
    val collectionStatus: String,
    val imageUri: String?,
    val price: Long?,
    val place: String?,
    val memo: String?,
    val recordedAtMillis: Long,
)
