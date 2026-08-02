package com.bluemarlin.drinkdiary.data.mapper

import com.bluemarlin.drinkdiary.data.local.DrinkRecordEntity
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRatingBreakdown
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DrinkRecordMapperTest {
    @Test
    fun entityToDomainMapsStableEnumValues() {
        val entity = entity(type = DrinkType.Whiskey.name, collectionStatus = CollectionStatus.Repurchase.name)

        val record = entity.toDomain()

        requireNotNull(record)
        assertEquals(DrinkType.Whiskey, record.type)
        assertEquals(CollectionStatus.Repurchase, record.collectionStatus)
        assertEquals("Oak Reserve", record.name)
        assertEquals(4.0, record.rating, 0.0001)
        assertEquals(3.5, record.ratingBreakdown.third, 0.0001)
        assertEquals(2.5, record.ratingBreakdown.fifth, 0.0001)
    }

    @Test
    fun entityToDomainReturnsNullForUnknownDrinkType() {
        val entity = entity(type = "Unknown", collectionStatus = CollectionStatus.Normal.name)

        assertNull(entity.toDomain())
    }

    @Test
    fun entityToDomainReturnsNullForUnknownCollectionStatus() {
        val entity = entity(type = DrinkType.Beer.name, collectionStatus = "Unknown")

        assertNull(entity.toDomain())
    }

    @Test
    fun entityToDomainNormalizesLegacySensoryMetricsToHalfUnits() {
        val entity =
            entity(type = DrinkType.Beer.name, collectionStatus = CollectionStatus.Normal.name).copy(
                detailRating1 = 3.3,
                detailRating2 = 4.7,
                detailRating3 = -1.0,
                detailRating4 = 6.0,
            )

        val record = entity.toDomain()

        requireNotNull(record)
        assertEquals(3.5, record.ratingBreakdown.first, 0.0001)
        assertEquals(4.5, record.ratingBreakdown.second, 0.0001)
        assertEquals(0.0, record.ratingBreakdown.third, 0.0001)
        assertEquals(5.0, record.ratingBreakdown.fourth, 0.0001)
    }

    @Test
    fun domainToEntityUsesStableEnumNames() {
        val record =
            DrinkRecord(
                id = 12L,
                type = DrinkType.Wine,
                name = "House Red",
                imageUri = "content://image",
                price = 18000L,
                place = "Wine Bar",
                tastingNote = "Light body",
                rating = 5.0,
                ratingBreakdown = DrinkRatingBreakdown(4.5, 4.0, 3.5, 3.0, 2.5),
                collectionStatus = CollectionStatus.NotForMe,
                recordedAtMillis = 1_700_000_000_000L,
            )

        val entity = record.toEntity(createdAtMillis = 10L, updatedAtMillis = 20L)

        assertEquals(DrinkType.Wine.name, entity.type)
        assertEquals(CollectionStatus.NotForMe.name, entity.collectionStatus)
        assertEquals(4.5, entity.detailRating1, 0.0001)
        assertEquals(3.0, entity.detailRating4, 0.0001)
        assertEquals(2.5, entity.detailRating5, 0.0001)
        assertEquals(10L, entity.createdAtMillis)
        assertEquals(20L, entity.updatedAtMillis)
    }

    private fun entity(
        type: String,
        collectionStatus: String,
    ) = DrinkRecordEntity(
        id = 1L,
        type = type,
        name = "Oak Reserve",
        imageUri = null,
        price = 32_000L,
        place = "Shop",
        tastingNote = "Balanced",
        rating = 4.0,
        detailRating1 = 4.5,
        detailRating2 = 4.0,
        detailRating3 = 3.5,
        detailRating4 = 3.0,
        detailRating5 = 2.5,
        collectionStatus = collectionStatus,
        recordedAtMillis = 1_700_000_000_000L,
        createdAtMillis = 1L,
        updatedAtMillis = 2L,
    )
}
