package com.bluemarlin.drinkdiary.data.repository

import android.content.Context
import com.bluemarlin.drinkdiary.domain.model.ThemeMode
import com.bluemarlin.drinkdiary.domain.repository.ThemePreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// SharedPreferences, not DataStore — a single three-state setting doesn't warrant a new
// dependency. SharedPreferences itself isn't Flow-native, so a MutableStateFlow mirrors
// it in memory: seeded from the stored value at construction, kept in sync on every write.
class ThemePreferenceRepositoryImpl(context: Context) : ThemePreferenceRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val themeMode: MutableStateFlow<ThemeMode> = MutableStateFlow(readStoredThemeMode())

    override fun observeThemeMode(): StateFlow<ThemeMode> = themeMode

    override suspend fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        themeMode.value = mode
    }

    private fun readStoredThemeMode(): ThemeMode {
        val stored = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.Auto
        return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.Auto)
    }

    private companion object {
        const val PREFS_NAME = "drink_diary_preferences"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
