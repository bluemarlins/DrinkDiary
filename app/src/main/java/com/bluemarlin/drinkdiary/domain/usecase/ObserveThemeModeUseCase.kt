package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.ThemeMode
import com.bluemarlin.drinkdiary.domain.repository.ThemePreferenceRepository
import kotlinx.coroutines.flow.Flow

class ObserveThemeModeUseCase(
    private val repository: ThemePreferenceRepository,
) {
    operator fun invoke(): Flow<ThemeMode> = repository.observeThemeMode()
}
