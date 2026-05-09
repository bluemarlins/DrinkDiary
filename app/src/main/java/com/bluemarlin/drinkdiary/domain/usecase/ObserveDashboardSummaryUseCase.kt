package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DashboardPeriod
import com.bluemarlin.drinkdiary.domain.model.DashboardSummary
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.map

class ObserveDashboardSummaryUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(
        period: DashboardPeriod,
        nowMillis: Long = System.currentTimeMillis(),
    ) = periodRange(period, nowMillis).let { range ->
        repository.observeRecordsByPeriod(range.startMillis, range.endMillis).map { records ->
            if (records.isEmpty()) {
                DashboardSummary.Empty
            } else {
                val prices = records.mapNotNull { it.price }
                DashboardSummary(
                    totalCount = records.size,
                    averageRating = records.map { it.rating }.average(),
                    wineCount = records.count { it.type == DrinkType.Wine },
                    whiskeyCount = records.count { it.type == DrinkType.Whiskey },
                    beerCount = records.count { it.type == DrinkType.Beer },
                    repurchaseCount = records.count { it.collectionStatus == CollectionStatus.Repurchase },
                    notForMeCount = records.count { it.collectionStatus == CollectionStatus.NotForMe },
                    totalSpent = prices.sum(),
                    averageSpent = prices.takeIf { it.isNotEmpty() }?.average()?.toLong(),
                    pricedRecordCount = prices.size,
                    normalRecords = records.filter { it.collectionStatus == CollectionStatus.Normal },
                    repurchaseRecords = records.filter { it.collectionStatus == CollectionStatus.Repurchase },
                    notForMeRecords = records.filter { it.collectionStatus == CollectionStatus.NotForMe },
                )
            }
        }
    }

    private fun periodRange(period: DashboardPeriod, nowMillis: Long): MillisRange {
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        val startDate = when (period) {
            DashboardPeriod.Weekly -> today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            DashboardPeriod.Monthly -> today.withDayOfMonth(1)
            DashboardPeriod.Yearly -> today.withDayOfYear(1)
        }
        val endDate = when (period) {
            DashboardPeriod.Weekly -> startDate.plusDays(7)
            DashboardPeriod.Monthly -> startDate.plusMonths(1)
            DashboardPeriod.Yearly -> startDate.plusYears(1)
        }
        return MillisRange(
            startMillis = startDate.startOfDayMillis(zone),
            endMillis = endDate.startOfDayMillis(zone) - 1L,
        )
    }

    private fun LocalDate.startOfDayMillis(zone: ZoneId): Long =
        atStartOfDay(zone).toInstant().toEpochMilli()

    private data class MillisRange(
        val startMillis: Long,
        val endMillis: Long,
    )
}
