package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.ThemeMode
import com.bluemarlin.drinkdiary.domain.repository.ThemePreferenceRepository

class SetThemeModeUseCase(
    private val repository: ThemePreferenceRepository,
) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}
