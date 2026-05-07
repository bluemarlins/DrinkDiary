package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DashboardPeriod
import com.bluemarlin.drinkdiary.domain.model.DashboardSummary
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import com.bluemarlin.drinkdiary.domain.model.DrinkRatingBreakdown
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveDashboardSummaryUseCaseTest {
    @Test
    fun monthlySummaryUsesOnlyRecordsInCurrentMonthRange() = runBlocking {
        val zone = ZoneId.systemDefault()
        val records = listOf(
            record(
                id = 1L,
                type = DrinkType.Wine,
                rating = 5.0,
                status = CollectionStatus.Repurchase,
                recordedAtMillis = millisOf(2026, 5, 1, 0, 0, zone),
            ),
            record(
                id = 2L,
                type = DrinkType.Beer,
                rating = 3.0,
                status = CollectionStatus.NotForMe,
                recordedAtMillis = millisOf(2026, 5, 31, 23, 59, zone),
            ),
            record(
                id = 3L,
                type = DrinkType.Whiskey,
                rating = 1.0,
                status = CollectionStatus.Normal,
                recordedAtMillis = millisOf(2026, 6, 1, 0, 0, zone),
            ),
        )
        val repository = RangeFilteringRepository(records)
        val useCase = ObserveDashboardSummaryUseCase(repository)

        val summary = useCase(
            period = DashboardPeriod.Monthly,
            nowMillis = millisOf(2026, 5, 15, 12, 0, zone),
        ).first()

        assertEquals(2, summary.totalCount)
        assertEquals(4.0, requireNotNull(summary.averageRating), 0.0001)
        assertEquals(1, summary.wineCount)
        assertEquals(0, summary.whiskeyCount)
        assertEquals(1, summary.beerCount)
        assertEquals(1, summary.repurchaseCount)
        assertEquals(1, summary.notForMeCount)
        assertEquals(listOf(1L), summary.repurchaseRecords.map { it.id })
        assertEquals(listOf(2L), summary.notForMeRecords.map { it.id })
    }

    @Test
    fun emptyPeriodReturnsEmptySummary() = runBlocking {
        val zone = ZoneId.systemDefault()
        val repository = RangeFilteringRepository(emptyList())
        val useCase = ObserveDashboardSummaryUseCase(repository)

        val summary = useCase(
            period = DashboardPeriod.Weekly,
            nowMillis = millisOf(2026, 5, 5, 10, 0, zone),
        ).first()

        assertEquals(DashboardSummary.Empty, summary)
    }

    private class RangeFilteringRepository(
        private val records: List<DrinkRecord>,
    ) : DrinkRecordRepository {
        override fun observeRecords(filter: DrinkRecordFilter): Flow<List<DrinkRecord>> = flowOf(records)
        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(records.firstOrNull { it.id == id })
        override fun observeRecordsByPeriod(startMillis: Long, endMillis: Long): Flow<List<DrinkRecord>> =
            flowOf(records.filter { it.recordedAtMillis in startMillis..endMillis })

        override suspend fun save(record: DrinkRecord): AppResult<Long> = AppResult.Success(record.id)
        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }

    private fun record(
        id: Long,
        type: DrinkType,
        rating: Double,
        status: CollectionStatus,
        recordedAtMillis: Long,
    ) = DrinkRecord(
        id = id,
        type = type,
        name = "Record $id",
        imageUri = null,
        price = null,
        place = null,
        tastingNote = null,
        rating = rating,
        ratingBreakdown = DrinkRatingBreakdown.fromRepresentativeRating(rating),
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
    ): Long = LocalDateTime.of(year, month, day, hour, minute)
        .atZone(zone)
        .toInstant()
        .toEpochMilli()
}
