package com.bluemarlin.drinkdiary.ui.collection

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.PhotoRepository
import com.bluemarlin.drinkdiary.domain.usecase.DeleteDrinkRecordsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CollectionViewModelTest {
    private fun record(
        id: Long,
        type: DrinkType,
    ) = DrinkRecord(
        id = id,
        type = type,
        name = "Drink $id",
        rating = 4.0,
        recordedAtMillis = id,
    )

    private val allRecords =
        listOf(
            record(1, DrinkType.Wine),
            record(2, DrinkType.Whiskey),
            record(3, DrinkType.Wine),
        )

    @Test
    fun `initial state loads all records without filter`() =
        runBlocking {
            val repository = FakeRecordRepository(allRecords)
            val useCase = DeleteDrinkRecordsUseCase(repository, FakePhotoRepository())
            val viewModel = CollectionViewModel(repository, useCase)

            ShadowLooper.idleMainLooper()

            val state = viewModel.uiState.first { it.loaded }
            assertNull(state.filter)
            assertEquals(3, state.records.size)
            assertEquals(3, state.allRecords.size)
        }

    @Test
    fun `changing filter updates records and clears selection`() =
        runBlocking {
            val repository = FakeRecordRepository(allRecords)
            val useCase = DeleteDrinkRecordsUseCase(repository, FakePhotoRepository())
            val viewModel = CollectionViewModel(repository, useCase)

            viewModel.toggleSelection(1L)
            viewModel.selectFilter(DrinkType.Wine)
            ShadowLooper.idleMainLooper()

            val state = viewModel.uiState.first { it.filter == DrinkType.Wine }
            assertEquals(listOf(1L, 3L), state.records.map { it.id })
            assertEquals(3, state.allRecords.size)
            assertFalse("필터 전환 시 선택이 해제되어야 한다", state.selectionMode)
            assertTrue(state.selected.isEmpty())
        }

    @Test
    fun `toggling and clearing selection`() =
        runBlocking {
            val repository = FakeRecordRepository(allRecords)
            val useCase = DeleteDrinkRecordsUseCase(repository, FakePhotoRepository())
            val viewModel = CollectionViewModel(repository, useCase)

            viewModel.toggleSelection(1L)
            viewModel.toggleSelection(2L)
            viewModel.toggleSelection(1L)

            viewModel.clearSelection()
            val state = viewModel.uiState.first { it.loaded }
            assertTrue(state.selected.isEmpty())
            assertFalse(state.selectionMode)
        }

    private class FakeRecordRepository(
        private val initial: List<DrinkRecord>,
    ) : DrinkRecordRepository {
        private val listFlow = MutableStateFlow(initial)

        override fun observeRecords(type: DrinkType?): Flow<List<DrinkRecord>> =
            MutableStateFlow(
                if (type == null) listFlow.value else listFlow.value.filter { it.type == type },
            ).asStateFlow()

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(listFlow.value.firstOrNull { it.id == id })

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> =
            flowOf(listFlow.value.filter { it.name.contains(query, ignoreCase = true) })

        override suspend fun save(record: DrinkRecord): AppResult<Long> {
            listFlow.value = listFlow.value + record
            return AppResult.Success(record.id)
        }

        override suspend fun deleteById(id: Long): AppResult<Unit> {
            listFlow.value = listFlow.value.filterNot { it.id == id }
            return AppResult.Success(Unit)
        }

        override suspend fun deleteByIds(ids: Set<Long>): AppResult<Int> {
            listFlow.value = listFlow.value.filterNot { it.id in ids }
            return AppResult.Success(ids.size)
        }
    }

    private class FakePhotoRepository : PhotoRepository {
        override suspend fun import(sourceUri: String): AppResult<String> = AppResult.Success(sourceUri)

        override suspend fun delete(uri: String): AppResult<Unit> = AppResult.Success(Unit)
    }
}
