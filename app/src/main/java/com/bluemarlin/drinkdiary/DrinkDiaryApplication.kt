package com.bluemarlin.drinkdiary

import android.app.Application
import androidx.room.Room
import com.bluemarlin.drinkdiary.data.local.AssetBottleDictionary
import com.bluemarlin.drinkdiary.data.local.DrinkDiaryDatabase
import com.bluemarlin.drinkdiary.data.local.MIGRATION_2_3
import com.bluemarlin.drinkdiary.data.local.SamplePhotoGenerator
import com.bluemarlin.drinkdiary.data.repository.DrinkRecordRepositoryImpl
import com.bluemarlin.drinkdiary.data.repository.PhotoRepositoryImpl
import com.bluemarlin.drinkdiary.data.repository.UserPreferencesRepositoryImpl
import com.bluemarlin.drinkdiary.domain.model.PeatTag
import com.bluemarlin.drinkdiary.domain.model.SampleData
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.WineColor
import com.bluemarlin.drinkdiary.domain.repository.BottleDictionary
import com.bluemarlin.drinkdiary.domain.repository.BottleMatcher
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.PhotoRepository
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import com.bluemarlin.drinkdiary.domain.usecase.DeleteDrinkRecordsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.DeletePhotoUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ImportPhotoUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveAnswerReflectionUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDrinkHighlightsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveMonthlySummaryUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveRecentTrendUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTagPreferenceUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTasteProfileUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTastingGapsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ResolveProfileReadinessUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DrinkDiaryApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        CoroutineScope(Dispatchers.IO).launch {
            val photoMap = SamplePhotoGenerator.ensureSamplePhotos(this@DrinkDiaryApplication)
            val records = appContainer.drinkRecordRepository.observeRecords().first()

            // 1. 기존 데이터 중 5축 누락 항목 보정 및 누락된 사진 채우기
            records.forEach { record ->
                val expectedTraits = Trait.of(record.type)
                val missingTraits = expectedTraits.filter { it !in record.taste.answers }
                val targetPhoto = if (record.imageUri == null) photoMap[record.name.trim()] else record.imageUri
                val needsPhotoUpdate = record.imageUri == null && targetPhoto != null

                if (missingTraits.isNotEmpty() || needsPhotoUpdate) {
                    var updatedTaste = record.taste
                    missingTraits.forEach { trait ->
                        val defaultAnswer =
                            when (trait) {
                                Trait.Acidity ->
                                    if (record.tags.wineColor ==
                                        WineColor.White
                                    ) {
                                        TraitAnswer.High
                                    } else {
                                        TraitAnswer.Mid
                                    }
                                Trait.Tannin ->
                                    if (record.tags.wineColor ==
                                        WineColor.White
                                    ) {
                                        TraitAnswer.VeryLow
                                    } else {
                                        TraitAnswer.High
                                    }
                                Trait.Peat ->
                                    if (record.tags.peat == PeatTag.Peated ||
                                        record.name.contains("라프로익") ||
                                        record.name.contains("아드벡") ||
                                        record.name.contains("탈리스커")
                                    ) {
                                        TraitAnswer.High
                                    } else {
                                        TraitAnswer.VeryLow
                                    }
                                Trait.AlcoholBurn ->
                                    if (record.name.contains("CS") ||
                                        record.name.contains("Proof") ||
                                        record.name.contains("버번")
                                    ) {
                                        TraitAnswer.High
                                    } else {
                                        TraitAnswer.Low
                                    }
                                Trait.Sweetness -> TraitAnswer.Mid
                                Trait.Body -> TraitAnswer.Mid
                                Trait.Aftertaste -> TraitAnswer.Mid
                                else -> TraitAnswer.Mid
                            }
                        updatedTaste = updatedTaste.with(trait, defaultAnswer)
                    }
                    appContainer.drinkRecordRepository.save(
                        record.copy(taste = updatedTaste, imageUri = targetPhoto ?: record.imageUri),
                    )
                }
            }

            // 2. SampleData 중 DB에 없는 항목들을 채워넣기 (사진 포함)
            val currentRecords = appContainer.drinkRecordRepository.observeRecords().first()
            val existingNames = currentRecords.map { it.name.trim() }.toSet()
            SampleData.allRecords.forEach { sample ->
                if (sample.name.trim() !in existingNames) {
                    val photoUri = photoMap[sample.name.trim()]
                    appContainer.drinkRecordRepository.save(sample.copy(id = 0L, imageUri = photoUri))
                }
            }
        }
    }
}

// 재정의 진행 중 — F1~F6 화면이 붙으면서 UseCase가 더 조립된다.
class AppContainer(
    application: Application,
) {
    // 파괴적 폴백을 걷어냈다. 이제 스키마가 바뀌면 마이그레이션을 쓴다 —
    // **폴백이 남아 있으면 마이그레이션을 빠뜨린 날 앱은 멀쩡히 실행되고 기록만 사라진다.**
    // 지금은 열리지 않아 즉시 터지는 쪽이 낫다. 마이그레이션 누락은 개발 중에 드러나야 한다.
    private val database: DrinkDiaryDatabase =
        Room
            .databaseBuilder(application, DrinkDiaryDatabase::class.java, "taste_archive.db")
            .addMigrations(MIGRATION_2_3)
            .build()

    val drinkRecordRepository: DrinkRecordRepository =
        DrinkRecordRepositoryImpl(database.drinkRecordDao())

    val userPreferencesRepository: UserPreferencesRepository =
        UserPreferencesRepositoryImpl(application)

    // 사진은 앱 안에 복사해 둔다. 갤러리 URI를 그대로 저장하면 재시작에서 사라진다(prd.md F1-3).
    val photoRepository: PhotoRepository = PhotoRepositoryImpl(application)
    val importPhotoUseCase = ImportPhotoUseCase(photoRepository)
    val deletePhotoUseCase = DeletePhotoUseCase(photoRepository)

    // 기록을 지우는 일이 더 이상 행 하나를 지우는 일이 아니다 — 사진 파일도 함께 사라져야 한다.
    val deleteDrinkRecordsUseCase = DeleteDrinkRecordsUseCase(drinkRecordRepository, photoRepository)

    val observeTasteProfileUseCase = ObserveTasteProfileUseCase(drinkRecordRepository)

    // 사전은 assets에서 첫 조회 때 한 번 읽고 메모리에 둔다.
    private val bottleAssets = AssetBottleDictionary(application)
    val bottleDictionary: BottleDictionary = BottleMatcher { bottleAssets.entries }

    val observeTagPreferenceUseCase = ObserveTagPreferenceUseCase(drinkRecordRepository, bottleDictionary)
    val resolveProfileReadinessUseCase = ResolveProfileReadinessUseCase()
    val observeMonthlySummaryUseCase = ObserveMonthlySummaryUseCase(drinkRecordRepository)
    val observeRecentTrendUseCase = ObserveRecentTrendUseCase(drinkRecordRepository)
    val observeTastingGapsUseCase = ObserveTastingGapsUseCase(drinkRecordRepository, bottleDictionary)
    val observeAnswerReflectionUseCase = ObserveAnswerReflectionUseCase(drinkRecordRepository)
    val observeDrinkHighlightsUseCase = ObserveDrinkHighlightsUseCase(drinkRecordRepository)
}
