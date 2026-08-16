package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.BottleEntry
import com.bluemarlin.drinkdiary.domain.model.BottleFacts
import com.bluemarlin.drinkdiary.domain.model.CaskGroup
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.PeatTag
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.model.WhiskyStyle
import com.bluemarlin.drinkdiary.domain.repository.BottleMatcher
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

class ObserveTagPreferenceUseCaseTest {
    private fun record(
        rating: Double,
        tags: DrinkTags = DrinkTags(),
    ) = DrinkRecord(
        type = DrinkType.Whiskey,
        name = "record",
        tags = tags,
        rating = rating,
        recordedAtMillis = 0L,
    )

    private fun peated(rating: Double) = record(rating, DrinkTags(peat = PeatTag.Peated))

    private fun unpeated(rating: Double) = record(rating, DrinkTags(peat = PeatTag.Unpeated))

    // 사전이 아무것도 모르는 상태. 태그 경로만 검사한다 — 사전 쪽은 BottleMatcherTest 담당.
    private val emptyDictionary = BottleMatcher { emptyList() }

    private fun preferences(records: List<DrinkRecord>) =
        runBlocking {
            ObserveTagPreferenceUseCase(FakeRepository(records), emptyDictionary)
                .invoke(TypeScope.Whiskey)
                .first()
        }

    private fun peat(records: List<DrinkRecord>) = preferences(records).firstOrNull { it.category == TagCategory.Peat }

    @Test
    fun `a category nobody tagged is absent entirely`() {
        assertNull(peat(List(5) { record(4.0) }))
        assertTrue(preferences(List(5) { record(4.0) }).isEmpty())
    }

    @Test
    fun `values are ordered by rating, best first`() {
        val pref = peat(List(3) { peated(4.6) } + List(3) { unpeated(2.4) })!!

        assertEquals(PeatTag.Peated.name, pref.best?.value)
        assertEquals(4.6, pref.best!!.averageRating, 1e-9)
        assertEquals(3, pref.best!!.samples)
        assertTrue(pref.meaningfulGap)
    }

    // 한쪽만 마셔본 것은 선호가 아니다 — "셰리만 마셨다"가 "셰리를 좋아한다"가 되면 안 된다.
    @Test
    fun `a single value is listed but never called a preference`() {
        val pref = peat(List(6) { peated(5.0) })!!

        assertEquals(1, pref.values.size)
        assertFalse(pref.meaningfulGap)
    }

    @Test
    fun `a value below the sample floor cannot decide the gap`() {
        // 논피트가 1잔뿐이라 비교 대상이 되지 못한다.
        val pref = peat(List(4) { peated(4.8) } + listOf(unpeated(1.0)))!!

        assertFalse(pref.meaningfulGap)
        // 다만 목록에는 남는다 — 감추면 사용자가 근거를 확인할 수 없다.
        assertEquals(2, pref.values.size)
    }

    @Test
    fun `a gap too small to matter is not called a preference`() {
        val pref = peat(List(3) { peated(4.0) } + List(3) { unpeated(3.8) })!!

        assertEquals(2, pref.values.size)
        assertFalse(pref.meaningfulGap)
    }

    @Test
    fun `categories are independent of each other`() {
        val records =
            List(3) { record(5.0, DrinkTags(peat = PeatTag.Peated, whiskyStyle = WhiskyStyle.SingleMalt)) } +
                List(3) { record(2.0, DrinkTags(peat = PeatTag.Unpeated, whiskyStyle = WhiskyStyle.SingleMalt)) }

        val all = preferences(records)
        val style = all.first { it.category == TagCategory.WhiskyStyle }

        assertTrue(peat(records)!!.meaningfulGap)
        // 싱글몰트만 있으니 비교가 성립하지 않는다.
        assertFalse(style.meaningfulGap)
    }

    @Test
    fun `untagged records do not count toward any category`() {
        val pref = peat(List(3) { peated(5.0) } + List(3) { unpeated(2.0) } + List(4) { record(1.0) })!!

        assertEquals(6, pref.totalSamples)
    }

    // 사전이 채운 값도 사용자 태그와 똑같이 판정된다. 이것이 사전을 넣은 이유다 —
    // 캐스크는 라벨에서 읽히지 않아 사용자가 답할 수 없다.
    @Test
    fun `dictionary facts are judged like any other tag`() {
        val dictionary =
            BottleMatcher {
                listOf(
                    BottleEntry(
                        DrinkType.Whiskey,
                        "글렌드로낙 12",
                        setOf("글렌드로낙 12"),
                        BottleFacts(cask = CaskGroup.Sherry),
                    ),
                    BottleEntry(
                        DrinkType.Whiskey,
                        "아드벡 10",
                        setOf("아드벡 10"),
                        BottleFacts(cask = CaskGroup.Bourbon),
                    ),
                )
            }
        val named = { name: String, rating: Double ->
            DrinkRecord(
                type = DrinkType.Whiskey,
                name = name,
                rating = rating,
                recordedAtMillis = 0L,
            )
        }
        val records =
            List(3) { named("글렌드로낙 12", 4.8) } + List(3) { named("아드벡 10", 2.5) }

        val cask =
            runBlocking {
                ObserveTagPreferenceUseCase(FakeRepository(records), dictionary)
                    .invoke(TypeScope.Whiskey)
                    .first()
            }.first { it.category == TagCategory.Cask }

        assertEquals(CaskGroup.Sherry.name, cask.best?.value)
        assertTrue(cask.meaningfulGap)
        // 사용자는 캐스크를 한 번도 입력하지 않았다.
        assertTrue(records.all { it.tags.isEmpty })
    }

    @Test
    fun `a bottle the dictionary does not know contributes nothing`() {
        val dictionary = BottleMatcher { emptyList() }
        val records =
            List(6) {
                DrinkRecord(
                    type = DrinkType.Whiskey,
                    name = "처음 보는 위스키",
                    rating = 5.0,
                    recordedAtMillis = 0L,
                )
            }

        val all =
            runBlocking {
                ObserveTagPreferenceUseCase(FakeRepository(records), dictionary)
                    .invoke(TypeScope.Whiskey)
                    .first()
            }

        assertTrue(all.none { it.category == TagCategory.Cask })
    }

    @Test
    fun `scope decides which records the repository is asked for`() {
        val repository = FakeRepository(emptyList())

        runBlocking {
            ObserveTagPreferenceUseCase(repository, emptyDictionary).invoke(TypeScope.Wine).first()
            assertEquals(DrinkType.Wine, repository.lastRequestedType)

            ObserveTagPreferenceUseCase(repository, emptyDictionary).invoke(TypeScope.Combined).first()
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
