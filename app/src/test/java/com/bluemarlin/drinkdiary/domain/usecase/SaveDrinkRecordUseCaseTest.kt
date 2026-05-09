package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
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
    fun blankNameReturnsValidationError() = runBlocking {
        val useCase = SaveDrinkRecordUseCase(FakeRepository())

        val result = useCase(
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
    fun validInputIsTrimmedAndSaved() = runBlocking {
        val repository = FakeRepository()
        val useCase = SaveDrinkRecordUseCase(repository)

        val result = useCase(
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
    fun ratingCanBeSavedInTenthUnits() = runBlocking {
        val repository = FakeRepository()
        val useCase = SaveDrinkRecordUseCase(repository)

        val result = useCase(
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
    fun ratingOutsideTenthUnitsReturnsValidationError() = runBlocking {
        val useCase = SaveDrinkRecordUseCase(FakeRepository())

        val result = useCase(
            DrinkRecordInput(
                type = DrinkType.Beer,
                name = "Lager",
                rating = 4.75,
                collectionStatus = CollectionStatus.Normal,
            ),
        )

        assertTrue(result is AppResult.Failure)
        val error = (result as AppResult.Failure).error as AppError.Validation
        assertEquals("별점은 0.5~5점 사이에서 0.1 단위로 선택해 주세요.", error.error.rating)
    }

    private class FakeRepository : DrinkRecordRepository {
        var savedRecord: DrinkRecord? = null

        override fun observeRecords(filter: DrinkRecordFilter): Flow<List<DrinkRecord>> = emptyFlow()
        override fun observeRecord(id: Long): Flow<DrinkRecord?> = emptyFlow()
        override fun observeRecordsByPeriod(startMillis: Long, endMillis: Long): Flow<List<DrinkRecord>> = emptyFlow()
        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = emptyFlow()

        override suspend fun save(record: DrinkRecord): AppResult<Long> {
            savedRecord = record
            return AppResult.Success(1L)
        }

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }
}
