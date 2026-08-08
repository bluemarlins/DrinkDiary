package com.bluemarlin.drinkdiary

import android.app.Application
import androidx.room.Room
import java.util.Locale
import com.bluemarlin.drinkdiary.ads.InterstitialAdManager
import com.bluemarlin.drinkdiary.data.local.DrinkDiaryDatabase
import com.bluemarlin.drinkdiary.data.repository.DrinkRecordRepositoryImpl
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.usecase.DeleteDrinkRecordUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDashboardSummaryUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDrinkRecordUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDrinkRecordsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCase

class DrinkDiaryApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // DrinkDiary is Korean-only (no other locale is supported anywhere in the UI —
        // see CLAUDE.md). Some Compose Material3 components (the DatePicker's month/
        // weekday chrome) read `Locale.getDefault()` directly rather than the app's own
        // Korean strings, so on a device set to a non-Korean system locale they'd render
        // in English otherwise. Force the JVM default once at startup.
        Locale.setDefault(Locale.KOREAN)
        appContainer = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val database: DrinkDiaryDatabase = Room.databaseBuilder(
        application,
        DrinkDiaryDatabase::class.java,
        "drink_diary.db",
    ).addMigrations(DrinkDiaryDatabase.MIGRATION_1_2).build()

    private val repository: DrinkRecordRepository =
        DrinkRecordRepositoryImpl(database.drinkRecordDao())

    val observeDrinkRecordsUseCase = ObserveDrinkRecordsUseCase(repository)
    val observeDrinkRecordUseCase = ObserveDrinkRecordUseCase(repository)
    val saveDrinkRecordUseCase = SaveDrinkRecordUseCase(repository)
    val deleteDrinkRecordUseCase = DeleteDrinkRecordUseCase(repository)
    val observeDashboardSummaryUseCase = ObserveDashboardSummaryUseCase(repository)

    val interstitialAdManager = InterstitialAdManager(application)
}
