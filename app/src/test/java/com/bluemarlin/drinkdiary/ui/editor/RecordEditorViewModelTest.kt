package com.bluemarlin.drinkdiary.ui.editor

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDrinkRecordUseCase
import com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordEditorViewModelTest {
    @Test
    fun newEditorStartsWithoutUnsavedChanges() {
        val viewModel = viewModel()

        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun changingInputMarksEditorAsHavingUnsavedChanges() {
        val viewModel = viewModel()

        viewModel.updateName("Lager")

        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun changingOnlyExpandedStateDoesNotMarkEditorAsHavingUnsavedChanges() {
        val viewModel = viewModel()

        viewModel.toggleRatingBreakdown()

        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }

    private fun viewModel(): RecordEditorViewModel {
        val repository = FakeRepository()
        return RecordEditorViewModel(
            recordId = null,
            observeDrinkRecordUseCase = ObserveDrinkRecordUseCase(repository),
            saveDrinkRecordUseCase = SaveDrinkRecordUseCase(repository),
        )
    }

    private class FakeRepository : DrinkRecordRepository {
        override fun observeRecords(filter: DrinkRecordFilter): Flow<List<DrinkRecord>> = emptyFlow()

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = emptyFlow()

        override fun observeRecordsByPeriod(
            startMillis: Long,
            endMillis: Long,
        ): Flow<List<DrinkRecord>> = emptyFlow()

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = emptyFlow()

        override suspend fun save(record: DrinkRecord): AppResult<Long> = AppResult.Success(1L)

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }
}
