package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.TastePreference
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
        answer: TraitAnswer?,
        rating: Double,
        type: DrinkType = DrinkType.Wine,
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

    private fun bodyOf(records: List<DrinkRecord>) = profileOf(records).preference(Trait.Body)!!

    @Test
    fun `preference follows the better rated side, not the bigger one`() {
        // Low가 6건으로 더 많지만 만족도는 High 쪽이 높다.
        val records =
            List(3) { record(TraitAnswer.High, 4.5) } +
                List(6) { record(TraitAnswer.Low, 2.0) }

        assertEquals(TastePreference.High, bodyOf(records).preference)
    }

    // 이 방식으로 바꾼 이유 그 자체다. 구 알고리즘은 Mid 기록을 통째로 버렸다.
    @Test
    fun `mid answers are data, not holes`() {
        val records =
            List(3) { record(TraitAnswer.Low, 2.0) } +
                List(3) { record(TraitAnswer.Mid, 3.5) } +
                List(3) { record(TraitAnswer.High, 5.0) }

        val body = bodyOf(records)
        assertEquals(TastePreference.High, body.preference)
        assertEquals(9, body.samples)
        assertEquals(3, body.midSamples)
    }

    // Low/High가 각 3건씩이라 구 임계치(3+3)는 통과하지만, 표본 총합이 6 미만이면 판단하지 않는다.
    @Test
    fun `not evaluated below the sample floor`() {
        val records = List(2) { record(TraitAnswer.High, 5.0) } + List(3) { record(TraitAnswer.Low, 1.0) }

        val body = bodyOf(records)
        assertNull(body.preference)
        assertFalse(body.evaluated)
        assertEquals(5, body.samples)
    }

    // 없는 취향을 지어내지 않는 것이 이 알고리즘의 존재 이유다.
    @Test
    fun `no relationship reports neutral, not a direction`() {
        // 축 값과 만족도가 따로 논다.
        val records =
            listOf(
                record(TraitAnswer.Low, 4.0),
                record(TraitAnswer.High, 4.0),
                record(TraitAnswer.Mid, 4.0),
                record(TraitAnswer.Low, 3.0),
                record(TraitAnswer.High, 3.0),
                record(TraitAnswer.Mid, 3.0),
            )

        val body = bodyOf(records)
        assertEquals(TastePreference.Neutral, body.preference)
        assertTrue("중립은 판정된 상태다", body.evaluated)
    }

    // 중립과 표본 부족을 같은 것으로 다루면 "취향이 없다"와 "아직 모른다"가 구분되지 않는다.
    @Test
    fun `neutral and not-evaluated are different states`() {
        val neutral =
            bodyOf(
                listOf(
                    record(TraitAnswer.Low, 3.0),
                    record(TraitAnswer.High, 3.0),
                    record(TraitAnswer.Mid, 3.5),
                    record(TraitAnswer.Low, 3.5),
                    record(TraitAnswer.High, 3.0),
                    record(TraitAnswer.Mid, 3.0),
                ),
            )
        val tooFew = bodyOf(List(3) { record(TraitAnswer.High, 5.0) })

        assertEquals(TastePreference.Neutral, neutral.preference)
        assertNull(tooFew.preference)
    }

    @Test
    fun `a trait everyone answered the same way cannot be judged`() {
        // 전부 '보통'이면 그 축은 만족도를 가르지 않는다 — 분산이 0이라 상관이 정의되지 않는다.
        val records = List(8) { record(TraitAnswer.Mid, (it % 5 + 1).toDouble()) }

        assertEquals(TastePreference.Neutral, bodyOf(records).preference)
    }

    @Test
    fun `strength grows with a cleaner relationship`() {
        val noisy =
            listOf(
                record(TraitAnswer.Low, 3.0),
                record(TraitAnswer.Mid, 2.0),
                record(TraitAnswer.High, 4.0),
                record(TraitAnswer.Low, 2.0),
                record(TraitAnswer.Mid, 4.0),
                record(TraitAnswer.High, 3.0),
            )
        val clean =
            listOf(
                record(TraitAnswer.Low, 1.0),
                record(TraitAnswer.Mid, 3.0),
                record(TraitAnswer.High, 5.0),
                record(TraitAnswer.Low, 1.0),
                record(TraitAnswer.Mid, 3.0),
                record(TraitAnswer.High, 5.0),
            )

        assertTrue(bodyOf(clean).strength > bodyOf(noisy).strength)
    }

    @Test
    fun `records with no answer for a trait are ignored entirely`() {
        val records =
            List(3) { record(TraitAnswer.High, 4.5) } +
                List(3) { record(TraitAnswer.Low, 2.0) } +
                List(2) { record(null, 5.0) }

        assertEquals(6, bodyOf(records).samples)
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

        assertEquals(Trait.of(DrinkType.Wine), traits)
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

        override suspend fun deleteByIds(ids: Set<Long>): AppResult<Int> = AppResult.Success(ids.size)
    }
}
