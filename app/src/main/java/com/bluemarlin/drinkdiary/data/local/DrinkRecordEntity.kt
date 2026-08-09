package com.bluemarlin.drinkdiary.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bluemarlin.drinkdiary.domain.model.DrinkType

/** Separator used to both join and delimit tasting tag keys — see [DrinkRecordEntity.tastingTags]. */
const val TASTING_TAG_DELIMITER = "|"

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
    /**
     * Tasting tag keys joined with [TASTING_TAG_DELIMITER] and *also* wrapped in it, e.g.
     * `|citrus|oak|`, so a `LIKE '%|oak|%'` tag filter cannot match a longer key that merely
     * contains it. Empty string when there are no tags. Conversion lives in DrinkRecordMapper.
     */
    val tastingTags: String,
    /** Null means the user never set it — read it back as [DrinkType.defaultAbv], an estimate. */
    val abv: Double?,
    /** Null means the user never set it — read it back as [DrinkType.defaultVolumeMl], an estimate. */
    val volumeMl: Int?,
    val collectionStatus: String,
    val recordedAtMillis: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
