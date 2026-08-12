package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRatingBreakdown
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.InsightsSummary
import com.bluemarlin.drinkdiary.domain.model.PriceBracket
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ObserveInsightsUseCaseTest {
    @Test
    fun returnsEmptySummaryWhenNoRecordsExist() =
        runBlocking {
            val repository = RangeFilteringRepository(emptyList())
            val useCase = ObserveInsightsUseCase(repository)

            val summary = useCase().first()

            assertEquals(InsightsSummary.Empty, summary)
        }

    @Test
    fun monthlyTrendAggregatesRecordsAcrossMonthsIncludingEmptyMonths() =
        runBlocking {
            val zone = ZoneId.systemDefault()
            val records =
                listOf(
                    record(
                        id = 1L,
                        rating = 5.0,
                        status = CollectionStatus.Repurchase,
                        recordedAtMillis = millisOf(2026, 3, 10, 10, 0, zone),
                    ),
                    record(
                        id = 2L,
                        rating = 3.0,
                        status = CollectionStatus.Normal,
                        recordedAtMillis = millisOf(2026, 5, 1, 0, 0, zone),
                    ),
                    record(
                        id = 3L,
                        rating = 4.0,
                        status = CollectionStatus.Repurchase,
                        recordedAtMillis = millisOf(2026, 5, 20, 15, 0, zone),
                    ),
                )
            val repository = RangeFilteringRepository(records)
            val useCase = ObserveInsightsUseCase(repository)

            val summary = useCase(monthsBack = 3, nowMillis = millisOf(2026, 5, 25, 12, 0, zone)).first()

            assertEquals(3, summary.monthlyTrend.size)
            assertEquals("2026-03", summary.monthlyTrend[0].yearMonthLabel)
            assertEquals(1, summary.monthlyTrend[0].totalCount)
            assertEquals(5.0, requireNotNull(summary.monthlyTrend[0].averageRating), 0.0001)
            assertEquals(1.0, requireNotNull(summary.monthlyTrend[0].repurchaseRate), 0.0001)

            assertEquals("2026-04", summary.monthlyTrend[1].yearMonthLabel)
            assertEquals(0, summary.monthlyTrend[1].totalCount)
            assertNull(summary.monthlyTrend[1].averageRating)
            assertNull(summary.monthlyTrend[1].repurchaseRate)

            assertEquals("2026-05", summary.monthlyTrend[2].yearMonthLabel)
            assertEquals(2, summary.monthlyTrend[2].totalCount)
            assertEquals(3.5, requireNotNull(summary.monthlyTrend[2].averageRating), 0.0001)
            assertEquals(0.5, requireNotNull(summary.monthlyTrend[2].repurchaseRate), 0.0001)
        }

    @Test
    fun priceBracketsCategorizesBoundaryPricesCorrectly() =
        runBlocking {
            val zone = ZoneId.systemDefault()
            val records =
                listOf(
                    record(
                        id = 1L,
                        price = 19_999L,
                        rating = 2.0,
                        recordedAtMillis = millisOf(2026, 5, 10, 10, 0, zone),
                    ),
                    record(
                        id = 2L,
                        price = 20_000L,
                        rating = 3.0,
                        recordedAtMillis = millisOf(2026, 5, 10, 10, 0, zone),
                    ),
                    record(
                        id = 3L,
                        price = 49_999L,
                        rating = 4.0,
                        recordedAtMillis = millisOf(2026, 5, 10, 10, 0, zone),
                    ),
                    record(
                        id = 4L,
                        price = 50_000L,
                        rating = 4.0,
                        recordedAtMillis = millisOf(2026, 5, 10, 10, 0, zone),
                    ),
                    record(
                        id = 5L,
                        price = 99_999L,
                        rating = 5.0,
                        recordedAtMillis = millisOf(2026, 5, 10, 10, 0, zone),
                    ),
                    record(
                        id = 6L,
                        price = 100_000L,
                        rating = 5.0,
                        recordedAtMillis = millisOf(2026, 5, 10, 10, 0, zone),
                    ),
                )
            val useCase = ObserveInsightsUseCase(RangeFilteringRepository(records))

            val summary = useCase(monthsBack = 1, nowMillis = millisOf(2026, 5, 15, 12, 0, zone)).first()

            assertEquals(4, summary.priceBrackets.size)

            val under20k = summary.priceBrackets.first { it.bracket == PriceBracket.Under20k }
            assertEquals(1, under20k.count)
            assertEquals(2.0, requireNotNull(under20k.averageRating), 0.0001)

            val between20kAnd50k = summary.priceBrackets.first { it.bracket == PriceBracket.Between20kAnd50k }
            assertEquals(2, between20kAnd50k.count)
            assertEquals(3.5, requireNotNull(between20kAnd50k.averageRating), 0.0001)

            val between50kAnd100k = summary.priceBrackets.first { it.bracket == PriceBracket.Between50kAnd100k }
            assertEquals(2, between50kAnd100k.count)
            assertEquals(4.5, requireNotNull(between50kAnd100k.averageRating), 0.0001)

            val over100k = summary.priceBrackets.first { it.bracket == PriceBracket.Over100k }
            assertEquals(1, over100k.count)
            assertEquals(5.0, requireNotNull(over100k.averageRating), 0.0001)
        }

    @Test
    fun nullPriceRecordsAreExcludedFromPriceBrackets() =
        runBlocking {
            val zone = ZoneId.systemDefault()
            val records =
                listOf(
                    record(id = 1L, price = null, rating = 4.0, recordedAtMillis = millisOf(2026, 5, 10, 10, 0, zone)),
                    record(
                        id = 2L,
                        price = 15_000L,
                        rating = 5.0,
                        recordedAtMillis = millisOf(2026, 5, 10, 10, 0, zone),
                    ),
                )
            val useCase = ObserveInsightsUseCase(RangeFilteringRepository(records))

            val summary = useCase(monthsBack = 1, nowMillis = millisOf(2026, 5, 15, 12, 0, zone)).first()

            val under20k = summary.priceBrackets.first { it.bracket == PriceBracket.Under20k }
            assertEquals(1, under20k.count)
            assertEquals(5.0, requireNotNull(under20k.averageRating), 0.0001)

            val totalPricedCount = summary.priceBrackets.sumOf { it.count }
            assertEquals(1, totalPricedCount)

            assertEquals(1, summary.monthlyTrend.size)
            assertEquals(2, summary.monthlyTrend[0].totalCount)
        }

    private class RangeFilteringRepository(
        private val records: List<DrinkRecord>,
    ) : DrinkRecordRepository {
        override fun observeRecords(filter: DrinkRecordFilter): Flow<List<DrinkRecord>> = flowOf(records)

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(records.firstOrNull { it.id == id })

        override fun observeRecordsByPeriod(
            startMillis: Long,
            endMillis: Long,
        ): Flow<List<DrinkRecord>> = flowOf(records.filter { it.recordedAtMillis in startMillis..endMillis })

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = flowOf(emptyList())

        override fun observeRecordsCount(): Flow<Int> = flowOf(0)

        override suspend fun save(record: DrinkRecord): AppResult<Long> = AppResult.Success(record.id)

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }

    private fun record(
        id: Long,
        type: DrinkType = DrinkType.Wine,
        rating: Double = 3.0,
        status: CollectionStatus = CollectionStatus.Normal,
        recordedAtMillis: Long,
        price: Long? = null,
    ) = DrinkRecord(
        id = id,
        type = type,
        name = "Record $id",
        imageUri = null,
        price = price,
        place = null,
        tastingNote = null,
        rating = rating,
        ratingBreakdown = DrinkRatingBreakdown(),
        collectionStatus = status,
        recordedAtMillis = recordedAtMillis,
    )

    private fun millisOf(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        zone: ZoneId,
    ): Long =
        LocalDateTime
            .of(year, month, day, hour, minute)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
}
