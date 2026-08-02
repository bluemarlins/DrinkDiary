package com.bluemarlin.drinkdiary.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DrinkRecordDaoTest {
    private lateinit var database: DrinkDiaryDatabase
    private lateinit var dao: DrinkRecordDao

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    DrinkDiaryDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        dao = database.drinkRecordDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndObserveRecord_returnsInsertedRecord() =
        runBlocking {
            val entity = dummyEntity(name = "Macallan 12")
            val id = dao.insert(entity)

            val stored = dao.observeRecord(id).first()

            assertEquals("Macallan 12", stored?.name)
            assertEquals("Wine", stored?.type)
        }

    @Test
    fun updateRecord_persistsChanges() =
        runBlocking {
            val id = dao.insert(dummyEntity())
            val original = requireNotNull(dao.getRecord(id))

            dao.update(original.copy(name = "업데이트된 이름", rating = 4.5))

            val stored = dao.getRecord(id)
            assertEquals("업데이트된 이름", stored?.name)
            assertEquals(4.5, stored?.rating)
        }

    @Test
    fun deleteById_removesRecord() =
        runBlocking {
            val id = dao.insert(dummyEntity())

            dao.deleteById(id)

            assertNull(dao.getRecord(id))
        }

    @Test
    fun observeRecords_filtersByTypeAndCollectionStatus() =
        runBlocking {
            dao.insert(dummyEntity(type = "Wine", status = "Repurchase"))
            dao.insert(dummyEntity(type = "Whiskey", status = "Normal"))
            dao.insert(dummyEntity(type = "Wine", status = "Normal"))

            val wineRepurchase = dao.observeRecords(type = "Wine", collectionStatus = "Repurchase").first()
            assertEquals(1, wineRepurchase.size)

            val allWine = dao.observeRecords(type = "Wine", collectionStatus = null).first()
            assertEquals(2, allWine.size)

            val all = dao.observeRecords(type = null, collectionStatus = null).first()
            assertEquals(3, all.size)
        }

    @Test
    fun observeRecordsByPeriod_returnsOnlyRecordsWithinRange() =
        runBlocking {
            dao.insert(dummyEntity(recordedAtMillis = 1_000L))
            dao.insert(dummyEntity(recordedAtMillis = 5_000L))
            dao.insert(dummyEntity(recordedAtMillis = 10_000L))

            val inRange = dao.observeRecordsByPeriod(2_000L, 6_000L).first()

            assertEquals(1, inRange.size)
            assertEquals(5_000L, inRange.first().recordedAtMillis)
        }

    @Test
    fun observeSearchResults_matchesNameCaseInsensitively() =
        runBlocking {
            dao.insert(dummyEntity(name = "Macallan 12"))
            dao.insert(dummyEntity(name = "Talisker 10"))

            val results = dao.observeSearchResults("macallan").first()

            assertEquals(1, results.size)
            assertEquals("Macallan 12", results.first().name)
        }

    private fun dummyEntity(
        type: String = "Wine",
        name: String = "Test Record",
        status: String = "Normal",
        recordedAtMillis: Long = System.currentTimeMillis(),
    ) = DrinkRecordEntity(
        type = type,
        name = name,
        imageUri = null,
        price = 30_000L,
        place = "Test Place",
        tastingNote = "Test note",
        rating = 4.0,
        detailRating1 = 4.0,
        detailRating2 = 4.0,
        detailRating3 = 4.0,
        detailRating4 = 4.0,
        detailRating5 = 4.0,
        collectionStatus = status,
        recordedAtMillis = recordedAtMillis,
        createdAtMillis = System.currentTimeMillis(),
        updatedAtMillis = System.currentTimeMillis(),
    )
}
