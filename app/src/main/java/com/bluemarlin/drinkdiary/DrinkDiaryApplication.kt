package com.bluemarlin.drinkdiary

import android.app.Application
import androidx.room.Room
import com.bluemarlin.drinkdiary.data.local.AssetBottleDictionary
import com.bluemarlin.drinkdiary.data.local.DrinkDiaryDatabase
import com.bluemarlin.drinkdiary.data.repository.DrinkRecordRepositoryImpl
import com.bluemarlin.drinkdiary.data.repository.UserPreferencesRepositoryImpl
import com.bluemarlin.drinkdiary.domain.repository.BottleDictionary
import com.bluemarlin.drinkdiary.domain.repository.BottleMatcher
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTagPreferenceUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTasteProfileUseCase
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
    // 출시 전이라 마이그레이션을 쌓지 않는다(problem-definition.md 7-1절).
    // 스키마가 바뀌면 기존 설치본의 데이터는 버린다 — 아직 사용자 데이터가 없고,
    // 마이그레이션을 쌓기 시작하면 재정의 중인 스키마에 발이 묶인다.
    @Suppress("DEPRECATION")
    private val database: DrinkDiaryDatabase =
        Room
            .databaseBuilder(application, DrinkDiaryDatabase::class.java, "taste_archive.db")
            .fallbackToDestructiveMigration()
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
}
