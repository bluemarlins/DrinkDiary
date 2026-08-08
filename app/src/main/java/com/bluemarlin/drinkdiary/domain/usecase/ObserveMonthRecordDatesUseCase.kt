package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.map

class ObserveMonthRecordDatesUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(yearMonth: YearMonth, nowMillis: Long = System.currentTimeMillis()) =
        monthRange(yearMonth).let { range ->
            repository.observeRecordsByPeriod(range.startMillis, range.endMillis).map { records ->
                val zone = ZoneId.systemDefault()
                records.map { Instant.ofEpochMilli(it.recordedAtMillis).atZone(zone).toLocalDate() }.toSet()
            }
        }

    private fun monthRange(yearMonth: YearMonth): MillisRange {
        val zone = ZoneId.systemDefault()
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth().plusDays(1)
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
