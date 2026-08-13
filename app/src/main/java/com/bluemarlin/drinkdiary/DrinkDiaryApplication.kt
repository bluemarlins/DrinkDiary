package com.bluemarlin.drinkdiary

import android.app.Application
import com.bluemarlin.drinkdiary.data.repository.UserPreferencesRepositoryImpl
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository

class DrinkDiaryApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}

// 재정의 진행 중 — Room 스키마와 UseCase는 새 도메인 모델 위에서 다시 조립한다.
// 현재는 주종과 무관한 Pro 상태만 살아 있다.
class AppContainer(
    application: Application,
) {
    val userPreferencesRepository: UserPreferencesRepository =
        UserPreferencesRepositoryImpl(application)
}
