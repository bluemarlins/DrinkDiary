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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ObserveRecentTrendUseCaseTest {
    // 순서만 의미가 있다. 값이 클수록 최근이다.
    private fun record(
        at: Long,
        rating: Double = 3.0,
        answers: Map<Trait, TraitAnswer> = emptyMap(),
    ) = DrinkRecord(
        type = DrinkType.Whiskey,
        name = "record$at",
        taste = TasteInput(answers),
        rating = rating,
        recordedAtMillis = at,
    )

    private fun trend(
        records: List<DrinkRecord>,
        scope: TypeScope = TypeScope.Whiskey,
    ) = runBlocking { ObserveRecentTrendUseCase(FakeRepository(records)).invoke(scope).first() }

    // 최근 다섯 잔에는 답을, 그 이전 다섯 잔에는 다른 답을 단다.
    private fun split(
        recentAnswer: TraitAnswer,
        earlierAnswer: TraitAnswer,
        trait: Trait = Trait.Body,
        recentRating: Double = 3.0,
        earlierRating: Double = 3.0,
    ): List<DrinkRecord> =
        (1..5).map { record(it.toLong(), earlierRating, mapOf(trait to earlierAnswer)) } +
            (6..10).map { record(it.toLong(), recentRating, mapOf(trait to recentAnswer)) }

    @Test
    fun `without a comparison group there is no recent trend`() {
        // 다섯 잔 전부가 '최근'이라 견줄 상대가 없다.
        assertNull(trend((1..5).map { record(it.toLong()) }))
        // 여섯 번째가 생겨도 이전 쪽이 한 잔뿐이라 아직 이르다.
        assertNull(trend((1..6).map { record(it.toLong()) }))
        assertNotNull(trend((1..8).map { record(it.toLong()) }))
    }

    // 저장소가 어떤 순서로 주든 '최근'은 기록 시각이 정한다. 정렬을 데이터 계층에 맡기면
    // 쿼리를 고치는 날 이 화면이 조용히 틀린 말을 한다.
    @Test
    fun `recency comes from the timestamp, not the repository order`() {
        val shuffled = split(TraitAnswer.High, TraitAnswer.Low).shuffled()

        assertEquals(Trait.Body, trend(shuffled)!!.shift!!.trait)
        assertEquals(TraitAnswer.High, trend(shuffled)!!.shift!!.direction)
    }

    @Test
    fun `a shift toward the low pole is reported as low`() {
        val result = trend(split(recentAnswer = TraitAnswer.Low, earlierAnswer = TraitAnswer.High))!!

        assertEquals(TraitAnswer.Low, result.shift!!.direction)
    }

    // 없는 변화를 방향으로 바꾸지 않는다.
    @Test
    fun `answers that did not move produce no shift`() {
        val result = trend(split(TraitAnswer.Mid, TraitAnswer.Mid))!!

        assertNull(result.shift)
    }

    // Low(0) → Mid(1)은 0.6에 못 미치는 0.4 이동이다. 작은 흔들림은 방향이 아니다.
    @Test
    fun `a shift below the threshold is not reported`() {
        val records =
            (1..5).map { record(it.toLong(), answers = mapOf(Trait.Body to TraitAnswer.Low)) } +
                (6..7).map { record(it.toLong(), answers = mapOf(Trait.Body to TraitAnswer.Mid)) } +
                (8..10).map { record(it.toLong(), answers = mapOf(Trait.Body to TraitAnswer.Low)) }

        assertNull(trend(records)!!.shift)
    }

    // 한 축만 말한다. 둘 이상 움직였으면 가장 크게 움직인 쪽이다.
    @Test
    fun `only the biggest shift is reported`() {
        val records =
            (1..5).map {
                record(
                    it.toLong(),
                    answers = mapOf(Trait.Body to TraitAnswer.Low, Trait.Sweetness to TraitAnswer.Mid),
                )
            } +
                (6..10).map {
                    record(
                        it.toLong(),
                        answers = mapOf(Trait.Body to TraitAnswer.High, Trait.Sweetness to TraitAnswer.High),
                    )
                }

        // 무게감은 2.0, 단맛은 1.0 움직였다.
        assertEquals(Trait.Body, trend(records)!!.shift!!.trait)
    }

    // 축마다 답이 달린 기록 수가 다르다. 한쪽 표본이 모자란 축은 후보가 아니다.
    @Test
    fun `a trait with too few answers cannot be the shift`() {
        val records =
            (1..5).map { record(it.toLong(), answers = mapOf(Trait.Body to TraitAnswer.Low)) } +
                (6..8).map { record(it.toLong(), answers = mapOf(Trait.Body to TraitAnswer.High)) } +
                (9..10).map {
                    record(it.toLong(), answers = mapOf(Trait.Sweetness to TraitAnswer.High))
                }

        // 최근 다섯 잔 중 무게감에 답한 것은 셋뿐 — 최소치를 딱 채운다.
        assertEquals(Trait.Body, trend(records)!!.shift!!.trait)
    }

    @Test
    fun `ratings are averaged per side`() {
        val result = trend(split(TraitAnswer.Mid, TraitAnswer.Mid, recentRating = 4.5, earlierRating = 3.0))!!

        assertEquals(5, result.recentCount)
        assertEquals(5, result.earlierCount)
        assertEquals(4.5, result.recentAverageRating, 1e-9)
        assertEquals(3.0, result.earlierAverageRating, 1e-9)
        assertEquals(1.5, result.ratingDelta, 1e-9)
    }

    @Test
    fun `scope decides which records the repository is asked for`() {
        val repository = FakeRepository(emptyList())

        runBlocking {
            ObserveRecentTrendUseCase(repository).invoke(TypeScope.Wine).first()
            assertEquals(DrinkType.Wine, repository.lastRequestedType)

            ObserveRecentTrendUseCase(repository).invoke(TypeScope.Combined).first()
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
