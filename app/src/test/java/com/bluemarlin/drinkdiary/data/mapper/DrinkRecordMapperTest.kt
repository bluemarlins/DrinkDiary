package com.bluemarlin.drinkdiary.data.mapper

import com.bluemarlin.drinkdiary.data.local.DrinkRecordEntity
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        assertEquals(listOf("smoky", "vanilla"), record.tastingTags)
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
    fun domainToEntityUsesStableEnumNames() {
        val record = record(tags = listOf("oak", "dry"))

        val entity = record.toEntity(createdAtMillis = 10L, updatedAtMillis = 20L)

        assertEquals(DrinkType.Wine.name, entity.type)
        assertEquals(CollectionStatus.NotForMe.name, entity.collectionStatus)
        assertEquals(10L, entity.createdAtMillis)
        assertEquals(20L, entity.updatedAtMillis)
    }

    @Test
    fun tagsAreStoredWrappedInDelimiters() {
        val entity = record(tags = listOf("oak", "dry")).toEntity(1L, 2L)

        // Both wrapped and separated, so a `LIKE '%|oak|%'` filter can never match a longer key.
        assertEquals("|oak|dry|", entity.tastingTags)
    }

    @Test
    fun tagStorageRoundTripsThroughBothDirections() {
        val tags = listOf("citrus", "long_finish", "직접입력태그")

        val restored = record(tags = tags).toEntity(1L, 2L).toDomain()

        assertEquals(tags, requireNotNull(restored).tastingTags)
    }

    @Test
    fun emptyTagsAreStoredAsEmptyStringNotBareDelimiters() {
        val entity = record(tags = emptyList()).toEntity(1L, 2L)

        assertEquals("", entity.tastingTags)
        assertEquals(emptyList<String>(), requireNotNull(entity.toDomain()).tastingTags)
    }

    @Test
    fun delimitersInsideCustomTagsAreStrippedSoTheyCannotSplitTheList() {
        val entity = record(tags = listOf("a|b")).toEntity(1L, 2L)

        assertEquals("|ab|", entity.tastingTags)
        assertEquals(listOf("ab"), requireNotNull(entity.toDomain()).tastingTags)
    }

    @Test
    fun duplicateTagsAreCollapsed() {
        val entity = record(tags = listOf("oak", "oak", "dry")).toEntity(1L, 2L)

        assertEquals("|oak|dry|", entity.tastingTags)
    }

    @Test
    fun nullIntakeFieldsFallBackToTypeDefaultsAndAreFlaggedAsEstimates() {
        val record = requireNotNull(entity(DrinkType.Beer.name, CollectionStatus.Normal.name).toDomain())

        assertEquals(5.0, record.effectiveAbv, 0.0001)
        assertEquals(500, record.effectiveVolumeMl)
        assertTrue(record.isIntakeEstimated)
    }

    private fun record(tags: List<String>) = DrinkRecord(
        id = 12L,
        type = DrinkType.Wine,
        name = "House Red",
        imageUri = "content://image",
        price = 18000L,
        place = "Wine Bar",
        tastingNote = "Light body",
        tastingTags = tags,
        rating = 5.0,
        abv = null,
        volumeMl = null,
        collectionStatus = CollectionStatus.NotForMe,
        recordedAtMillis = 1_700_000_000_000L,
    )

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
        tastingTags = "|smoky|vanilla|",
        rating = 4.0,
        abv = null,
        volumeMl = null,
        collectionStatus = collectionStatus,
        recordedAtMillis = 1_700_000_000_000L,
        createdAtMillis = 1L,
        updatedAtMillis = 2L,
    )
}
