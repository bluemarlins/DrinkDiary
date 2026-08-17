package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.BottleEntry
import com.bluemarlin.drinkdiary.domain.model.BottleFacts
import com.bluemarlin.drinkdiary.domain.model.CaskGroup
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Origin
import com.bluemarlin.drinkdiary.domain.model.PeatTag
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TastingGap
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.model.WhiskyStyle
import com.bluemarlin.drinkdiary.domain.model.gapCandidates
import com.bluemarlin.drinkdiary.domain.repository.BottleDictionary
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

class ObserveTastingGapsUseCaseTest {
    private val emptyDictionary = BottleMatcher { emptyList() }

    private fun record(
        tags: DrinkTags = DrinkTags(),
        name: String = "record",
    ) = DrinkRecord(
        type = DrinkType.Whiskey,
        name = name,
        tags = tags,
        rating = 3.0,
        recordedAtMillis = 0L,
    )

    private fun gaps(
        records: List<DrinkRecord>,
        dictionary: BottleDictionary = emptyDictionary,
        scope: TypeScope = TypeScope.Whiskey,
    ) = runBlocking {
        ObserveTastingGapsUseCase(FakeRepository(records), dictionary).invoke(scope).first()
    }

    private fun gapFor(
        records: List<DrinkRecord>,
        category: TagCategory,
        dictionary: BottleDictionary = emptyDictionary,
    ): TastingGap? = gaps(records, dictionary).firstOrNull { it.category == category }

    @Test
    fun `a side that is stacked names the side that is empty`() {
        val records = List(3) { record(DrinkTags(peat = PeatTag.Peated)) }
        val gap = gapFor(records, TagCategory.Peat)!!

        assertEquals(PeatTag.Peated.name, gap.recordedValue)
        assertEquals(3, gap.recordedSamples)
        assertEquals(PeatTag.Unpeated.name, gap.missingValue)
    }

    // 한 잔도 없는 카테고리에서 "아직 없어요"는 정보가 아니다 — 우리가 물어본 적조차 없는 축이다.
    @Test
    fun `a category with no records at all says nothing`() {
        assertNull(gapFor(List(10) { record() }, TagCategory.Peat))
        assertTrue(gaps(List(10) { record() }).isEmpty())
    }

    @Test
    fun `a side below the sample floor is not called stacked`() {
        assertNull(gapFor(List(2) { record(DrinkTags(peat = PeatTag.Peated)) }, TagCategory.Peat))
        assertEquals(
            3,
            gapFor(List(3) { record(DrinkTags(peat = PeatTag.Peated)) }, TagCategory.Peat)!!.recordedSamples,
        )
    }

    // 양쪽 다 있으면 공백이 아니다. 비교가 이미 성립한다.
    @Test
    fun `a category with every value filled has no gap`() {
        val records =
            List(3) { record(DrinkTags(peat = PeatTag.Peated)) } +
                listOf(record(DrinkTags(peat = PeatTag.Unpeated)))

        assertNull(gapFor(records, TagCategory.Peat))
    }

    // 뭉뚱그리는 값은 마셔서 채울 수 있는 칸이 아니다.
    @Test
    fun `catch-all values are never named as a gap`() {
        assertFalse(CaskGroup.Mixed.name in TagCategory.Cask.gapCandidates)
        assertFalse("Other" in TagCategory.WineColor.gapCandidates)
    }

    // 사전이 채운 값도 사용자 태그와 똑같이 다룬다. 한쪽만 사전을 보면 같은 화면에
    // "셰리 3잔"과 "셰리는 아직 없어요"가 나란히 뜬다.
    @Test
    fun `dictionary facts count toward the gap just like tags`() {
        val dictionary =
            BottleMatcher {
                listOf(
                    BottleEntry(
                        DrinkType.Whiskey,
                        "글렌드로낙 12",
                        setOf("글렌드로낙 12"),
                        BottleFacts(cask = CaskGroup.Sherry),
                    ),
                )
            }
        val records = List(3) { record(name = "글렌드로낙 12") }
        val gap = gapFor(records, TagCategory.Cask, dictionary)!!

        assertEquals(CaskGroup.Sherry.name, gap.recordedValue)
        assertEquals(CaskGroup.Bourbon.name, gap.missingValue)
        assertTrue(records.all { it.tags.isEmpty })
    }

    // 전부 나열하면 화면이 할 일 목록이 된다.
    @Test
    fun `at most two gaps are reported, the most lopsided first`() {
        val records =
            List(3) {
                record(DrinkTags(peat = PeatTag.Peated, whiskyStyle = WhiskyStyle.SingleMalt))
            } +
                List(5) {
                    record(DrinkTags(whiskyStyle = WhiskyStyle.SingleMalt, origin = Origin.OldWorld))
                }

        val result = gaps(records)

        assertEquals(2, result.size)
        // 싱글몰트 8잔이 가장 크게 쏠렸다.
        assertEquals(TagCategory.WhiskyStyle, result.first().category)
        assertEquals(8, result.first().recordedSamples)
    }

    @Test
    fun `scope decides which records the repository is asked for`() {
        val repository = FakeRepository(emptyList())

        runBlocking {
            ObserveTastingGapsUseCase(repository, emptyDictionary).invoke(TypeScope.Wine).first()
            assertEquals(DrinkType.Wine, repository.lastRequestedType)

            ObserveTastingGapsUseCase(repository, emptyDictionary).invoke(TypeScope.Combined).first()
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
