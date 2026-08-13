package com.bluemarlin.drinkdiary.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bluemarlin.drinkdiary.data.local.DrinkDiaryDatabase
import com.bluemarlin.drinkdiary.data.repository.DrinkRecordRepositoryImpl
import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DrinkRecordRepositoryTest {
    private lateinit var database: DrinkDiaryDatabase
    private lateinit var repository: DrinkRecordRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, DrinkDiaryDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = DrinkRecordRepositoryImpl(database.drinkRecordDao())
    }

    @After
    fun tearDown() = database.close()

    private fun wine(
        name: String = "Barolo",
        taste: TasteInput = TasteInput(),
        rating: Double = 4.0,
        recordedAt: Long = 1_000L,
    ) = DrinkRecord(
        type = DrinkType.Wine,
        name = name,
        vintage = 2019,
        taste = taste,
        rating = rating,
        collectionStatus = CollectionStatus.Repurchase,
        recordedAtMillis = recordedAt,
    )

    private suspend fun saveId(record: DrinkRecord): Long = (repository.save(record) as AppResult.Success).value

    @Test
    fun `a saved record comes back with its taste answers intact`() =
        runBlocking {
            val taste =
                TasteInput()
                    .with(Trait.Body, TraitAnswer.High)
                    .with(Trait.Sweetness, TraitAnswer.Unsure)
                    .with(Trait.Tannin, TraitAnswer.Low)
            val id = saveId(wine(taste = taste))

            val loaded = repository.observeRecord(id).first()!!

            assertEquals("Barolo", loaded.name)
            assertEquals(2019, loaded.vintage)
            assertEquals(CollectionStatus.Repurchase, loaded.collectionStatus)
            assertEquals(TraitAnswer.High, loaded.taste[Trait.Body])
            assertEquals(TraitAnswer.Unsure, loaded.taste[Trait.Sweetness])
            assertEquals(TraitAnswer.Low, loaded.taste[Trait.Tannin])
        }

    @Test
    fun `serving style survives a round trip for whiskey`() =
        runBlocking {
            val id =
                saveId(
                    DrinkRecord(
                        type = DrinkType.Whiskey,
                        name = "Ardbeg",
                        servingStyle = ServingStyle.OnTheRocks,
                        rating = 4.5,
                        recordedAtMillis = 5L,
                    ),
                )

            assertEquals(ServingStyle.OnTheRocks, repository.observeRecord(id).first()!!.servingStyle)
        }

    @Test
    fun `re-saving a record replaces its answers instead of piling them up`() =
        runBlocking {
            val id = saveId(wine(taste = TasteInput().with(Trait.Body, TraitAnswer.High)))

            val edited =
                repository.observeRecord(id).first()!!.copy(
                    taste = TasteInput().with(Trait.Body, TraitAnswer.Low),
                )
            repository.save(edited)

            val reloaded = repository.observeRecord(id).first()!!
            assertEquals(1, reloaded.taste.answers.size)
            assertEquals(TraitAnswer.Low, reloaded.taste[Trait.Body])
        }

    @Test
    fun `records are filtered by drink type`() =
        runBlocking {
            saveId(wine(name = "Barolo"))
            saveId(
                DrinkRecord(
                    type = DrinkType.Whiskey,
                    name = "Ardbeg",
                    rating = 4.0,
                    recordedAtMillis = 2_000L,
                ),
            )

            assertEquals(listOf("Barolo"), repository.observeRecords(DrinkType.Wine).first().map { it.name })
            assertEquals(listOf("Ardbeg"), repository.observeRecords(DrinkType.Whiskey).first().map { it.name })
            assertEquals(2, repository.observeRecords(null).first().size)
        }

    @Test
    fun `records come back newest first`() =
        runBlocking {
            saveId(wine(name = "older", recordedAt = 100L))
            saveId(wine(name = "newer", recordedAt = 900L))

            assertEquals(listOf("newer", "older"), repository.observeRecords().first().map { it.name })
        }

    @Test
    fun `search matches part of the name`() =
        runBlocking {
            saveId(wine(name = "Barolo Riserva"))
            saveId(wine(name = "Chablis"))

            assertEquals(listOf("Barolo Riserva"), repository.observeSearchResults("arolo").first().map { it.name })
            assertTrue(repository.observeSearchResults("zzz").first().isEmpty())
        }

    @Test
    fun `deleting a record removes it and its answers`() =
        runBlocking {
            val id = saveId(wine(taste = TasteInput().with(Trait.Body, TraitAnswer.High)))

            assertTrue(repository.deleteById(id) is AppResult.Success)
            assertNull(repository.observeRecord(id).first())

            // 답이 함께 지워지지 않으면 같은 id 재사용 시 유령 답이 붙는다.
            val reused = saveId(wine(name = "reused"))
            assertTrue(
                repository
                    .observeRecord(reused)
                    .first()!!
                    .taste.answers
                    .isEmpty(),
            )
        }

    @Test
    fun `deleting something that is not there reports not found`() =
        runBlocking {
            val result = repository.deleteById(9_999L)

            assertTrue(result is AppResult.Failure)
            assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
        }
}
