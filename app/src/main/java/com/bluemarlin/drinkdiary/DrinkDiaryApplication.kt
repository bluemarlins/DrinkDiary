package com.bluemarlin.drinkdiary

import android.app.Application
import androidx.room.Room
import java.util.Locale
import com.bluemarlin.drinkdiary.ads.InterstitialAdManager
import com.bluemarlin.drinkdiary.data.local.DrinkDiaryDatabase
import com.bluemarlin.drinkdiary.data.repository.DrinkRecordRepositoryImpl
import com.bluemarlin.drinkdiary.data.repository.ThemePreferenceRepositoryImpl
import com.bluemarlin.drinkdiary.debug.DebugSeeder
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.ThemePreferenceRepository
import com.bluemarlin.drinkdiary.domain.usecase.DeleteDrinkRecordUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDashboardSummaryUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDrinkRecordUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDrinkRecordsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveMonthRecordDatesUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveThemeModeUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveWeeklyTrendUseCase
import com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCase
import com.bluemarlin.drinkdiary.domain.usecase.SetThemeModeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        // Seeds dummy data in debug builds only; no-op in release — see
        // app/src/debug/.../debug/DebugSeeder.kt and app/src/release/.../debug/DebugSeeder.kt.
        CoroutineScope(Dispatchers.IO).launch { DebugSeeder.seedIfNeeded(this@DrinkDiaryApplication, appContainer) }
    }
}

class AppContainer(application: Application) {
    private val database: DrinkDiaryDatabase = Room.databaseBuilder(
        application,
        DrinkDiaryDatabase::class.java,
        "drink_diary.db",
    ).addMigrations(
        DrinkDiaryDatabase.MIGRATION_1_2,
        DrinkDiaryDatabase.MIGRATION_2_3,
    ).build()

    private val repository: DrinkRecordRepository =
        DrinkRecordRepositoryImpl(database.drinkRecordDao())
    private val themePreferenceRepository: ThemePreferenceRepository =
        ThemePreferenceRepositoryImpl(application)

    val observeDrinkRecordsUseCase = ObserveDrinkRecordsUseCase(repository)
    val observeDrinkRecordUseCase = ObserveDrinkRecordUseCase(repository)
    val saveDrinkRecordUseCase = SaveDrinkRecordUseCase(repository)
    val deleteDrinkRecordUseCase = DeleteDrinkRecordUseCase(repository)
    val observeDashboardSummaryUseCase = ObserveDashboardSummaryUseCase(repository)
    val observeMonthRecordDatesUseCase = ObserveMonthRecordDatesUseCase(repository)
    val observeWeeklyTrendUseCase = ObserveWeeklyTrendUseCase(repository)
    val observeThemeModeUseCase = ObserveThemeModeUseCase(themePreferenceRepository)
    val setThemeModeUseCase = SetThemeModeUseCase(themePreferenceRepository)

    val interstitialAdManager = InterstitialAdManager(application)
}
