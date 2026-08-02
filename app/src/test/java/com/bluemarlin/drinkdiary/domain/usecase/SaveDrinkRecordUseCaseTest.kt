package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRatingBreakdown
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordInput
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveDrinkRecordUseCaseTest {
    @Test
    fun blankNameReturnsValidationError() =
        runBlocking {
            val useCase = SaveDrinkRecordUseCase(FakeRepository())

            val result =
                useCase(
                    DrinkRecordInput(
                        type = DrinkType.Wine,
                        name = " ",
                        rating = 4.0,
                        collectionStatus = CollectionStatus.Normal,
                    ),
                )

            assertTrue(result is AppResult.Failure)
            val error = (result as AppResult.Failure).error as AppError.Validation
            assertEquals("이름을 입력해 주세요.", error.error.name)
        }

    @Test
    fun validInputIsTrimmedAndSaved() =
        runBlocking {
            val repository = FakeRepository()
            val useCase = SaveDrinkRecordUseCase(repository)

            val result =
                useCase(
                    DrinkRecordInput(
                        type = DrinkType.Beer,
                        name = "  Lager  ",
                        priceText = "12000",
                        place = "  Pub  ",
                        rating = 5.0,
                        collectionStatus = CollectionStatus.Repurchase,
                    ),
                )

            assertTrue(result is AppResult.Success)
            assertEquals("Lager", repository.savedRecord?.name)
            assertEquals("Pub", repository.savedRecord?.place)
            assertEquals(12000L, repository.savedRecord?.price)
        }

    @Test
    fun ratingCanBeSavedInTenthUnits() =
        runBlocking {
            val repository = FakeRepository()
            val useCase = SaveDrinkRecordUseCase(repository)

            val result =
                useCase(
                    DrinkRecordInput(
                        type = DrinkType.Beer,
                        name = "Lager",
                        rating = 4.7,
                        collectionStatus = CollectionStatus.Repurchase,
                    ),
                )

            assertTrue(result is AppResult.Success)
            assertEquals(4.7, repository.savedRecord?.rating ?: 0.0, 0.0001)
        }

    @Test
    fun overallRatingOutsideTenthUnitsReturnsValidationError() =
        runBlocking {
            val useCase = SaveDrinkRecordUseCase(FakeRepository())

            val result =
                useCase(
                    DrinkRecordInput(
                        type = DrinkType.Beer,
                        name = "Lager",
                        rating = 4.75,
                        collectionStatus = CollectionStatus.Normal,
                    ),
                )

            assertTrue(result is AppResult.Failure)
            val error = (result as AppResult.Failure).error as AppError.Validation
            assertEquals("전체 평점은 0~5점, 테이스팅 프로필은 0.5 단위로 입력해 주세요.", error.error.rating)
        }

    @Test
    fun sensoryMetricsDoNotChangeOverallRating() =
        runBlocking {
            val repository = FakeRepository()
            val useCase = SaveDrinkRecordUseCase(repository)

            val result =
                useCase(
                    DrinkRecordInput(
                        type = DrinkType.Wine,
                        name = "Chardonnay",
                        rating = 2.0,
                        ratingBreakdown = DrinkRatingBreakdown(5.0, 5.0, 5.0, 5.0, 5.0),
                        collectionStatus = CollectionStatus.Normal,
                    ),
                )

            assertTrue(result is AppResult.Success)
            assertEquals(2.0, repository.savedRecord?.rating ?: 0.0, 0.0001)
            assertEquals(5.0, repository.savedRecord?.ratingBreakdown?.first ?: 0.0, 0.0001)
        }

    private class FakeRepository : DrinkRecordRepository {
        var savedRecord: DrinkRecord? = null

        override fun observeRecords(filter: DrinkRecordFilter): Flow<List<DrinkRecord>> = emptyFlow()

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = emptyFlow()

        override fun observeRecordsByPeriod(
            startMillis: Long,
            endMillis: Long,
        ): Flow<List<DrinkRecord>> = emptyFlow()

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = emptyFlow()

        override suspend fun save(record: DrinkRecord): AppResult<Long> {
            savedRecord = record
            return AppResult.Success(1L)
        }

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }
}
