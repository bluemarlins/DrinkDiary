package com.bluemarlin.drinkdiary.domain.repository

import com.bluemarlin.drinkdiary.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemePreferenceRepository {
    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
