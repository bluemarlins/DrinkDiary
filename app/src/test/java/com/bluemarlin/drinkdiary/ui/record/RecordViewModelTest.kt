package com.bluemarlin.drinkdiary.ui.record

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Origin
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.WhiskyStyle
import com.bluemarlin.drinkdiary.domain.model.WineColor
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.PhotoRepository
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import com.bluemarlin.drinkdiary.domain.usecase.DeletePhotoUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ImportPhotoUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RecordViewModelTest {
    @Test
    fun `full recording flow saves record with 5 taste traits and origin tag`() =
        runBlocking {
            val recordRepo = FakeRecordRepository()
            val photoRepo = FakePhotoRepository()
            val prefRepo = FakePreferencesRepository()
            val viewModel =
                RecordViewModel(
                    repository = recordRepo,
                    preferences = prefRepo,
                    importPhoto = ImportPhotoUseCase(photoRepo),
                    deletePhoto = DeletePhotoUseCase(photoRepo),
                )

            // Step 1: Drink & Type
            viewModel.pickDrink(DrinkType.Wine, DrinkTags(wineColor = WineColor.Natural))
            assertEquals(DrinkType.Wine, viewModel.uiState.value.type)
            assertEquals(WineColor.Natural, viewModel.uiState.value.form.tags.wineColor)

            // Step 2: Origin
            viewModel.pickOrigin(Origin.France)
            assertEquals(Origin.France, viewModel.uiState.value.form.tags.origin)

            // Step 3: Basic info
            viewModel.updateForm(
                viewModel.uiState.value.form.copy(
                    name = "Domaine Naturaliste",
                    rating = 4.5,
                    collectionStatus = CollectionStatus.Repurchase,
                ),
            )

            // Step 4: 5 axes taste probes
            viewModel.answer(Trait.Sweetness, TraitAnswer.Low)
            viewModel.answer(Trait.Acidity, TraitAnswer.High)
            viewModel.answer(Trait.Tannin, TraitAnswer.Mid)
            viewModel.answer(Trait.Body, TraitAnswer.Mid)
            viewModel.answer(Trait.Aftertaste, TraitAnswer.High)

            assertEquals(5, viewModel.uiState.value.taste.answers.size)
            assertEquals(TraitAnswer.High, viewModel.uiState.value.taste[Trait.Acidity])

            // Step 5: Save
            viewModel.save()
            ShadowLooper.idleMainLooper()

            val state = viewModel.uiState.first { it.savedId != null }
            assertNotNull(state.savedId)

            val savedRecord = recordRepo.savedRecords.first()
            assertEquals("Domaine Naturaliste", savedRecord.name)
            assertEquals(DrinkType.Wine, savedRecord.type)
            assertEquals(Origin.France, savedRecord.tags.origin)
            assertEquals(WineColor.Natural, savedRecord.tags.wineColor)
            assertEquals(4.5, savedRecord.rating, 0.01)
            assertEquals(5, savedRecord.taste.answers.size)
        }

    @Test
    fun `whiskey recording flow saves record with Korean origin and blended malt style`() =
        runBlocking {
            val recordRepo = FakeRecordRepository()
            val photoRepo = FakePhotoRepository()
            val prefRepo = FakePreferencesRepository()
            val viewModel =
                RecordViewModel(
                    repository = recordRepo,
                    preferences = prefRepo,
                    importPhoto = ImportPhotoUseCase(photoRepo),
                    deletePhoto = DeletePhotoUseCase(photoRepo),
                )

            // Step 1: Drink & Style
            viewModel.pickDrink(DrinkType.Whiskey, DrinkTags(whiskyStyle = WhiskyStyle.BlendedMalt))
            assertEquals(DrinkType.Whiskey, viewModel.uiState.value.type)

            // Step 2: Origin
            viewModel.pickOrigin(Origin.Korea)
            assertEquals(Origin.Korea, viewModel.uiState.value.form.tags.origin)

            // Step 3-5: Info & Rating
            viewModel.updateForm(
                viewModel.uiState.value.form.copy(
                    name = "기원 배치 1",
                    rating = 4.8,
                    collectionStatus = CollectionStatus.Repurchase,
                ),
            )

            // Step 6: 5 axes probes
            viewModel.answer(Trait.Sweetness, TraitAnswer.Mid)
            viewModel.answer(Trait.Body, TraitAnswer.High)
            viewModel.answer(Trait.Peat, TraitAnswer.Low)
            viewModel.answer(Trait.AlcoholBurn, TraitAnswer.VeryLow)
            viewModel.answer(Trait.Aftertaste, TraitAnswer.VeryHigh)

            viewModel.save()
            ShadowLooper.idleMainLooper()

            val state = viewModel.uiState.first { it.savedId != null }
            assertNotNull(state.savedId)

            val savedRecord = recordRepo.savedRecords.first()
            assertEquals("기원 배치 1", savedRecord.name)
            assertEquals(DrinkType.Whiskey, savedRecord.type)
            assertEquals(Origin.Korea, savedRecord.tags.origin)
            assertEquals(WhiskyStyle.BlendedMalt, savedRecord.tags.whiskyStyle)
            assertEquals(TraitAnswer.VeryHigh, savedRecord.taste[Trait.Aftertaste])
        }

    private class FakeRecordRepository : DrinkRecordRepository {
        val savedRecords = mutableListOf<DrinkRecord>()
        private var nextId = 1L

        override fun observeRecords(type: DrinkType?): Flow<List<DrinkRecord>> = flowOf(savedRecords)

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(savedRecords.find { it.id == id })

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = flowOf(emptyList())

        override suspend fun save(record: DrinkRecord): AppResult<Long> {
            val id = if (record.id == 0L) nextId++ else record.id
            savedRecords.add(record.copy(id = id))
            return AppResult.Success(id)
        }

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun deleteByIds(ids: Set<Long>): AppResult<Int> = AppResult.Success(ids.size)
    }

    private class FakePhotoRepository : PhotoRepository {
        override suspend fun import(sourceUri: String): AppResult<String> = AppResult.Success("file://$sourceUri")

        override suspend fun delete(uri: String): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakePreferencesRepository : UserPreferencesRepository {
        private val tagsFlow = MutableStateFlow<Set<TagCategory>>(emptySet())

        override val isProUser: Flow<Boolean> = flowOf(false)

        override suspend fun setProUser(isPro: Boolean) {}

        override val alwaysAskTags: Flow<Set<TagCategory>> = tagsFlow.asStateFlow()

        override val hasChosenTagPreferences: Flow<Boolean> = flowOf(false)

        override suspend fun setAlwaysAskTags(tags: Set<TagCategory>) {
            tagsFlow.value = tags
        }
    }
}
