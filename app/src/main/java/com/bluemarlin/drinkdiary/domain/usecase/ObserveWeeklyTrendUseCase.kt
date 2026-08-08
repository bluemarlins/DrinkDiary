package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.map

// Independent of the Weekly/Monthly/Yearly period tab (same pattern as
// ObserveMonthRecordDatesUseCase for the calendar) — a trend needs a fixed rolling
// window regardless of which summary period is selected.
class ObserveWeeklyTrendUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(weekCount: Int = WEEK_COUNT, nowMillis: Long = System.currentTimeMillis()) = run {
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        // Monday-start, matching ObserveDashboardSummaryUseCase's Weekly definition and
        // DDDashboardCalendar's Monday-first grid.
        val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val windowStart = currentWeekStart.minusWeeks((weekCount - 1).toLong())
        val windowEnd = currentWeekStart.plusDays(7)
        val startMillis = windowStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = windowEnd.atStartOfDay(zone).toInstant().toEpochMilli() - 1L

        repository.observeRecordsByPeriod(startMillis, endMillis).map { records ->
            val counts = MutableList(weekCount) { 0 }
            records.forEach { record ->
                val recordDate = Instant.ofEpochMilli(record.recordedAtMillis).atZone(zone).toLocalDate()
                val weekIndex = ChronoUnit.WEEKS.between(windowStart, recordDate).toInt()
                if (weekIndex in 0 until weekCount) {
                    counts[weekIndex] = counts[weekIndex] + 1
                }
            }
            counts.toList()
        }
    }

    private companion object {
        const val WEEK_COUNT = 8
    }
}
