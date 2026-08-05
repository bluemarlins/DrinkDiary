package com.bluemarlin.drinkdiary.ui.insights

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRatingBreakdown
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.usecase.ObserveInsightsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class InsightsViewModelTest {
    @Test
    fun uiStateEmitsEmptyWhenNoRecordsExist() =
        runBlocking {
            val repository = FakeRepository(emptyList())
            val viewModel = InsightsViewModel(ObserveInsightsUseCase(repository))

            val state = viewModel.uiState.first { it !is InsightsUiState.Loading }

            assertEquals(InsightsUiState.Empty, state)
        }

    @Test
    fun uiStateEmitsSuccessWhenDataExists() =
        runBlocking {
            val zone = ZoneId.systemDefault()
            val now =
                LocalDateTime
                    .now()
                    .atZone(zone)
                    .toInstant()
                    .toEpochMilli()
            val record =
                DrinkRecord(
                    id = 1L,
                    type = DrinkType.Wine,
                    name = "Wine 1",
                    imageUri = null,
                    price = 30_000L,
                    place = null,
                    tastingNote = null,
                    rating = 4.5,
                    ratingBreakdown = DrinkRatingBreakdown(),
                    collectionStatus = CollectionStatus.Repurchase,
                    recordedAtMillis = now,
                )
            val repository = FakeRepository(listOf(record))
            val viewModel = InsightsViewModel(ObserveInsightsUseCase(repository))

            val state = viewModel.uiState.first { it !is InsightsUiState.Loading }

            assertTrue(state is InsightsUiState.Success)
            val summary = (state as InsightsUiState.Success).summary
            assertTrue(summary.monthlyTrend.isNotEmpty())
            assertTrue(summary.priceBrackets.isNotEmpty())
        }

    private class FakeRepository(
        private val records: List<DrinkRecord>,
    ) : DrinkRecordRepository {
        override fun observeRecords(filter: DrinkRecordFilter): Flow<List<DrinkRecord>> = flowOf(records)

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(records.firstOrNull { it.id == id })

        override fun observeRecordsByPeriod(
            startMillis: Long,
            endMillis: Long,
        ): Flow<List<DrinkRecord>> = flowOf(records)

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = flowOf(emptyList())

        override suspend fun save(record: DrinkRecord): AppResult<Long> = AppResult.Success(record.id)

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }
}
