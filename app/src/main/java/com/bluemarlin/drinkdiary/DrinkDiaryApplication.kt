package com.bluemarlin.drinkdiary

import android.app.Application
import androidx.room.Room
import com.bluemarlin.drinkdiary.data.local.DrinkDiaryDatabase
import com.bluemarlin.drinkdiary.data.repository.DrinkRecordRepositoryImpl
import com.bluemarlin.drinkdiary.data.repository.UserPreferencesRepositoryImpl
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
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
    private val database: DrinkDiaryDatabase =
        Room
            .databaseBuilder(application, DrinkDiaryDatabase::class.java, "taste_archive.db")
            .build()

    private val recordRepository: DrinkRecordRepository =
        DrinkRecordRepositoryImpl(database.drinkRecordDao())

    val userPreferencesRepository: UserPreferencesRepository =
        UserPreferencesRepositoryImpl(application)

    val observeTasteProfileUseCase = ObserveTasteProfileUseCase(recordRepository)
    val resolveProfileReadinessUseCase = ResolveProfileReadinessUseCase()
}
