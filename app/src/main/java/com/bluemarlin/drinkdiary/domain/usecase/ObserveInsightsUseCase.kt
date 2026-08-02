package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.InsightsSummary
import com.bluemarlin.drinkdiary.domain.model.MonthlyInsight
import com.bluemarlin.drinkdiary.domain.model.PriceBracket
import com.bluemarlin.drinkdiary.domain.model.PriceBracketInsight
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class ObserveInsightsUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(
        monthsBack: Int = 6,
        nowMillis: Long = System.currentTimeMillis(),
    ): Flow<InsightsSummary> =
        periodRange(monthsBack, nowMillis).let { range ->
            repository.observeRecordsByPeriod(range.startMillis, range.endMillis).map { records ->
                if (records.isEmpty()) {
                    InsightsSummary.Empty
                } else {
                    val zone = ZoneId.systemDefault()
                    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
                    val currentYearMonth = YearMonth.from(today)
                    val yearMonths =
                        (monthsBack - 1 downTo 0).map { i ->
                            currentYearMonth.minusMonths(i.toLong())
                        }

                    val recordsByMonth =
                        records.groupBy { record ->
                            val recordDate = Instant.ofEpochMilli(record.recordedAtMillis).atZone(zone).toLocalDate()
                            YearMonth.from(recordDate)
                        }

                    val monthlyTrend =
                        yearMonths.map { ym ->
                            val monthRecords = recordsByMonth[ym] ?: emptyList()
                            val totalCount = monthRecords.size
                            val averageRating =
                                if (totalCount == 0) null else monthRecords.map { it.rating }.average()
                            val repurchaseCount =
                                monthRecords.count { it.collectionStatus == CollectionStatus.Repurchase }
                            val repurchaseRate =
                                if (totalCount == 0) null else repurchaseCount.toDouble() / totalCount

                            MonthlyInsight(
                                yearMonthLabel = ym.toString(),
                                totalCount = totalCount,
                                averageRating = averageRating,
                                repurchaseRate = repurchaseRate,
                            )
                        }

                    val pricedRecords = records.filter { it.price != null }
                    val priceBrackets =
                        PriceBracket.entries.map { bracket ->
                            val bracketRecords =
                                pricedRecords.filter { record ->
                                    val price = record.price!!
                                    when (bracket) {
                                        PriceBracket.Under20k -> price < 20_000L
                                        PriceBracket.Between20kAnd50k -> price >= 20_000L && price < 50_000L
                                        PriceBracket.Between50kAnd100k -> price >= 50_000L && price < 100_000L
                                        PriceBracket.Over100k -> price >= 100_000L
                                    }
                                }
                            val count = bracketRecords.size
                            val averageRating =
                                if (count == 0) null else bracketRecords.map { it.rating }.average()

                            PriceBracketInsight(
                                bracket = bracket,
                                count = count,
                                averageRating = averageRating,
                            )
                        }

                    InsightsSummary(
                        monthlyTrend = monthlyTrend,
                        priceBrackets = priceBrackets,
                    )
                }
            }
        }

    private fun periodRange(
        monthsBack: Int,
        nowMillis: Long,
    ): MillisRange {
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        val currentYearMonth = YearMonth.from(today)
        val startYearMonth = currentYearMonth.minusMonths((monthsBack - 1).coerceAtLeast(0).toLong())
        val startDate = startYearMonth.atDay(1)
        val endDate = currentYearMonth.plusMonths(1).atDay(1)
        return MillisRange(
            startMillis = startDate.atStartOfDay(zone).toInstant().toEpochMilli(),
            endMillis = endDate.atStartOfDay(zone).toInstant().toEpochMilli() - 1L,
        )
    }

    private data class MillisRange(
        val startMillis: Long,
        val endMillis: Long,
    )
}
