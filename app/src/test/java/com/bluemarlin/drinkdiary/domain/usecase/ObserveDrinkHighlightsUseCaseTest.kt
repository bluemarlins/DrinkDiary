package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.HighlightKind
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

class ObserveDrinkHighlightsUseCaseTest {
    private fun record(
        id: Long,
        name: String,
        rating: Double,
        at: Long,
        imageUri: String? = null,
    ) = DrinkRecord(
        id = id,
        type = DrinkType.Whiskey,
        name = name,
        rating = rating,
        imageUri = imageUri,
        recordedAtMillis = at,
    )

    private fun highlights(
        records: List<DrinkRecord>,
        scope: TypeScope = TypeScope.Whiskey,
    ) = runBlocking { ObserveDrinkHighlightsUseCase(FakeRepository(records)).invoke(scope).first() }

    private fun kind(
        records: List<DrinkRecord>,
        kind: HighlightKind,
    ) = highlights(records).firstOrNull { it.kind == kind }

    @Test
    fun `no records means no cards`() {
        assertTrue(highlights(emptyList()).isEmpty())
    }

    @Test
    fun `the top rated card picks the highest score`() {
        val records =
            listOf(
                record(1, "낮음", 2.0, at = 100),
                record(2, "높음", 5.0, at = 200),
                record(3, "중간", 4.0, at = 300),
            )

        assertEquals("높음", kind(records, HighlightKind.TopRated)!!.record.name)
    }

    // 동점이면 최근 것. 같은 점수를 여러 번 줬을 때 매번 다른 잔이 뜨면 화면이 이유 없이 바뀐다.
    @Test
    fun `a tie on rating is broken by recency`() {
        val records = listOf(record(1, "먼저", 5.0, at = 100), record(2, "나중", 5.0, at = 200))

        assertEquals("나중", kind(records, HighlightKind.TopRated)!!.record.name)
    }

    // 한 번 마신 것을 "여러 번"이라 부를 수 없다.
    @Test
    fun `a name recorded once is never the repeated card`() {
        val records = listOf(record(1, "한 번", 5.0, at = 100), record(2, "다른 것", 4.0, at = 200))

        assertNull(kind(records, HighlightKind.MostRepeated))
    }

    @Test
    fun `the repeated card counts the name and shows its best glass`() {
        val records =
            listOf(
                record(1, "발베니 12", 3.0, at = 100),
                record(2, "발베니 12", 5.0, at = 200),
                record(3, "발베니 12", 4.0, at = 300),
                record(4, "다른 것", 5.0, at = 400),
            )
        val repeated = kind(records, HighlightKind.MostRepeated)!!

        assertEquals(3, repeated.repeatCount)
        // 여러 번 마셨다는 사실을 말하는 자리이므로 대표 잔은 가장 높게 준 것이다.
        assertEquals(2L, repeated.record.id)
    }

    // 이름 앞뒤 공백이 다르다고 다른 술이 되면 "여러 번"이 영영 안 나온다.
    //
    // 세 번째 기록이 있는 것은 자리다툼을 피하기 위해서다 — 두 건뿐이면 그 한 잔이 최고점이자
    // 최근이자 대표가 되어 카드 하나로 합쳐진다(아래 `one record never fills two cards`).
    @Test
    fun `names are matched with surrounding blanks trimmed`() {
        val records =
            listOf(
                record(1, "아드벡 10", 4.0, at = 100),
                record(2, " 아드벡 10 ", 4.0, at = 200),
                record(3, "다른 것", 5.0, at = 300),
            )

        assertEquals(2, kind(records, HighlightKind.MostRepeated)!!.repeatCount)
    }

    @Test
    fun `the latest card follows the recorded time`() {
        val records = listOf(record(1, "먼저", 5.0, at = 100), record(2, "나중", 1.0, at = 999))

        assertEquals("나중", kind(records, HighlightKind.Latest)!!.record.name)
    }

    // 같은 기록이 두 자리를 차지하면 카드가 같은 사진을 두 번 보여준다.
    @Test
    fun `one record never fills two cards`() {
        val single = highlights(listOf(record(1, "하나뿐", 5.0, at = 100)))

        assertEquals(1, single.size)
        assertEquals(HighlightKind.TopRated, single.single().kind)
    }

    // 사진이 있는 것만 골라 보여주면 화면은 예뻐지지만 "가장 높게 준 잔"이 사실이 아니게 된다.
    @Test
    fun `a record without a photo is still a highlight`() {
        val records =
            listOf(
                record(1, "사진 있음", 3.0, at = 100, imageUri = "file:///photo.jpg"),
                record(2, "사진 없음", 5.0, at = 200),
            )

        assertEquals("사진 없음", kind(records, HighlightKind.TopRated)!!.record.name)
        assertNull(kind(records, HighlightKind.TopRated)!!.record.imageUri)
    }

    @Test
    fun `scope decides which records the repository is asked for`() {
        val repository = FakeRepository(emptyList())

        runBlocking {
            ObserveDrinkHighlightsUseCase(repository).invoke(TypeScope.Wine).first()
            assertEquals(DrinkType.Wine, repository.lastRequestedType)

            ObserveDrinkHighlightsUseCase(repository).invoke(TypeScope.Combined).first()
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
