package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveTasteProfileUseCaseTest {
    private fun record(
        type: DrinkType = DrinkType.Wine,
        answer: TraitAnswer?,
        rating: Double,
        trait: Trait = Trait.Body,
    ): DrinkRecord =
        DrinkRecord(
            type = type,
            name = "record",
            taste = answer?.let { TasteInput().with(trait, it) } ?: TasteInput(),
            rating = rating,
            recordedAtMillis = 0L,
        )

    private fun profileOf(
        records: List<DrinkRecord>,
        scope: TypeScope = TypeScope.Wine,
    ) = runBlocking { ObserveTasteProfileUseCase(FakeRepository(records)).invoke(scope).first() }

    @Test
    fun `preference follows the better rated side, not the bigger one`() {
        // Low가 5건으로 더 많지만 만족도는 High 쪽이 높다.
        val records =
            List(3) { record(answer = TraitAnswer.High, rating = 4.5) } +
                List(5) { record(answer = TraitAnswer.Low, rating = 2.0) }

        val body = profileOf(records).preference(Trait.Body)!!
        assertEquals(TraitAnswer.High, body.direction)
        assertEquals(3, body.highSamples)
        assertEquals(5, body.lowSamples)
    }

    @Test
    fun `no direction when one side is below the sample floor`() {
        val records =
            List(2) { record(answer = TraitAnswer.High, rating = 5.0) } +
                List(9) { record(answer = TraitAnswer.Low, rating = 1.0) }

        assertNull(profileOf(records).preference(Trait.Body)!!.direction)
    }

    @Test
    fun `no direction when the rating gap is not meaningful`() {
        val records =
            List(4) { record(answer = TraitAnswer.High, rating = 3.6) } +
                List(4) { record(answer = TraitAnswer.Low, rating = 3.4) }

        assertNull(profileOf(records).preference(Trait.Body)!!.direction)
    }

    @Test
    fun `unsure records are counted but never averaged`() {
        // Unsure 기록에 극단적인 평점을 줘도 판정이 흔들리면 안 된다.
        val records =
            List(3) { record(answer = TraitAnswer.High, rating = 4.5) } +
                List(3) { record(answer = TraitAnswer.Low, rating = 2.0) } +
                List(4) { record(answer = TraitAnswer.Unsure, rating = 0.0) }

        val body = profileOf(records).preference(Trait.Body)!!
        assertEquals(TraitAnswer.High, body.direction)
        assertEquals(4, body.unsureSamples)
        assertEquals(3, body.highSamples)
        assertEquals(3, body.lowSamples)
    }

    @Test
    fun `records with no answer for a trait are ignored entirely`() {
        val records =
            List(3) { record(answer = TraitAnswer.High, rating = 4.5) } +
                List(3) { record(answer = TraitAnswer.Low, rating = 2.0) } +
                List(2) { record(answer = null, rating = 5.0) }

        val body = profileOf(records).preference(Trait.Body)!!
        assertEquals(3, body.highSamples)
        assertEquals(3, body.lowSamples)
        assertEquals(0, body.unsureSamples)
        assertEquals(8, profileOf(records).recordCount)
    }

    @Test
    fun `combined scope covers shared traits only`() {
        val traits = profileOf(emptyList(), TypeScope.Combined).preferences.map { it.trait }

        assertEquals(Trait.shared, traits)
        assertFalse(traits.contains(Trait.Tannin))
        assertFalse(traits.contains(Trait.Peat))
    }

    @Test
    fun `wine scope carries its own traits and not the whiskey ones`() {
        val traits = profileOf(emptyList(), TypeScope.Wine).preferences.map { it.trait }

        assertTrue(traits.containsAll(Trait.shared))
        assertTrue(traits.contains(Trait.Tannin))
        assertFalse(traits.contains(Trait.Peat))
    }

    @Test
    fun `scope decides which records the repository is asked for`() {
        val repository = FakeRepository(emptyList())

        runBlocking {
            ObserveTasteProfileUseCase(repository).invoke(TypeScope.Whiskey).first()
            assertEquals(DrinkType.Whiskey, repository.lastRequestedType)

            ObserveTasteProfileUseCase(repository).invoke(TypeScope.Combined).first()
            assertNull(repository.lastRequestedType)
        }
    }

    private class FakeRepository(
        private val records: List<DrinkRecord>,
    ) : DrinkRecordRepository {
        var lastRequestedType: DrinkType? = null
            private set

        override fun observeRecords(type: DrinkType?): Flow<List<DrinkRecord>> {
            lastRequestedType = type
            return flowOf(records)
        }

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(null)

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = flowOf(emptyList())

        override suspend fun save(record: DrinkRecord): AppResult<Long> = AppResult.Success(record.id)

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }
}
