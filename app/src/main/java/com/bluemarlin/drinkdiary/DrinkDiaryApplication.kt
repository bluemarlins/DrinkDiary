package com.bluemarlin.drinkdiary

import android.app.Application
import androidx.room.Room
import com.bluemarlin.drinkdiary.data.local.AssetBottleDictionary
import com.bluemarlin.drinkdiary.data.local.DrinkDiaryDatabase
import com.bluemarlin.drinkdiary.data.local.MIGRATION_2_3
import com.bluemarlin.drinkdiary.data.repository.DrinkRecordRepositoryImpl
import com.bluemarlin.drinkdiary.data.repository.UserPreferencesRepositoryImpl
import com.bluemarlin.drinkdiary.domain.repository.BottleDictionary
import com.bluemarlin.drinkdiary.domain.repository.BottleMatcher
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import com.bluemarlin.drinkdiary.domain.usecase.ObserveAnswerReflectionUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveMonthlySummaryUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveRecentTrendUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTagPreferenceUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTasteProfileUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTastingGapsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ResolveProfileReadinessUseCase

class DrinkDiaryApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
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
}
