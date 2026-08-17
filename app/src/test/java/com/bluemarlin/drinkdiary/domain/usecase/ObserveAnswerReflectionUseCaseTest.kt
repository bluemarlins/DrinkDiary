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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveAnswerReflectionUseCaseTest {
    private fun record(
        rating: Double = 3.0,
        answers: Map<Trait, TraitAnswer> = emptyMap(),
    ) = DrinkRecord(
        type = DrinkType.Whiskey,
        name = "record",
        taste = TasteInput(answers),
        rating = rating,
        recordedAtMillis = 0L,
    )

    private fun reflection(
        records: List<DrinkRecord>,
        scope: TypeScope = TypeScope.Whiskey,
    ) = runBlocking { ObserveAnswerReflectionUseCase(FakeRepository(records)).invoke(scope).first() }

    private fun answered(
        trait: Trait,
        vararg answers: TraitAnswer,
    ) = answers.map { record(answers = mapOf(trait to it)) }

    @Test
    fun `a side answered more often is reflected back`() {
        val result =
            reflection(answered(Trait.Body, TraitAnswer.High, TraitAnswer.High, TraitAnswer.Low))

        assertEquals(1, result.leanings.size)
        assertEquals(Trait.Body, result.leanings.single().trait)
        assertEquals(TraitAnswer.High, result.leanings.single().direction)
    }

    // 한 잔은 경향이 아니다.
    @Test
    fun `a single leaning answer says nothing`() {
        assertTrue(reflection(answered(Trait.Body, TraitAnswer.High)).isEmpty)
        assertTrue(reflection(answered(Trait.Body, TraitAnswer.High, TraitAnswer.High)).isEmpty.not())
    }

    // '보통'이 대부분인 축을 억지로 한쪽에 세우면 없는 경향을 지어내는 것이 된다.
    @Test
    fun `an axis answered mostly Mid has no leaning`() {
        val records = answered(Trait.Body, TraitAnswer.Mid, TraitAnswer.Mid, TraitAnswer.Mid, TraitAnswer.High)

        assertTrue(reflection(records).isEmpty)
    }

    @Test
    fun `a tie is not a leaning`() {
        val records =
            answered(Trait.Body, TraitAnswer.High, TraitAnswer.High, TraitAnswer.Low, TraitAnswer.Low)

        assertTrue(reflection(records).isEmpty)
    }

    // 되비침은 판정이 아니다. 판정은 축값과 만족도의 상관이라 답이 몰린 쪽과 얼마든지
    // 어긋날 수 있다 — 묵직한 것만 골라 마셨어도 전부 낮게 평가했다면 그것은 선호가 아니다.
    @Test
    fun `the reflection ignores ratings entirely`() {
        val hatedButAnswered =
            listOf(
                record(1.0, mapOf(Trait.Body to TraitAnswer.High)),
                record(1.0, mapOf(Trait.Body to TraitAnswer.High)),
                record(5.0, mapOf(Trait.Body to TraitAnswer.Low)),
            )

        assertEquals(TraitAnswer.High, reflection(hatedButAnswered).leanings.single().direction)
    }

    // 축 선언 순서를 그대로 쓴다. 개수 순으로 두면 한 잔 차이로 줄이 자리를 바꿔
    // "달라진 것"처럼 보인다.
    @Test
    fun `leanings keep the trait declaration order`() {
        val records =
            List(2) {
                record(answers = mapOf(Trait.Body to TraitAnswer.High, Trait.Sweetness to TraitAnswer.Low))
            } + List(4) { record(answers = mapOf(Trait.Aftertaste to TraitAnswer.High)) }

        assertEquals(
            listOf(Trait.Sweetness, Trait.Body, Trait.Aftertaste),
            reflection(records).leanings.map { it.trait },
        )
    }

    // 고유 축은 확장 경로에서만 답이 달린다. 기본 경로가 묻지도 않는 축을 되비추면
    // 사용자는 자기가 답한 적 없는 말을 읽게 된다.
    @Test
    fun `only shared traits are reflected`() {
        val records = answered(Trait.Peat, TraitAnswer.High, TraitAnswer.High, TraitAnswer.High)

        assertTrue(reflection(records).isEmpty)
    }

    @Test
    fun `scope decides which records the repository is asked for`() {
        val repository = FakeRepository(emptyList())

        runBlocking {
            ObserveAnswerReflectionUseCase(repository).invoke(TypeScope.Wine).first()
            assertEquals(DrinkType.Wine, repository.lastRequestedType)

            ObserveAnswerReflectionUseCase(repository).invoke(TypeScope.Combined).first()
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
