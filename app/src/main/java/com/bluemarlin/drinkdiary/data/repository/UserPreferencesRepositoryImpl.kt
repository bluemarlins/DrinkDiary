package com.bluemarlin.drinkdiary.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepositoryImpl(
    private val context: Context,
) : UserPreferencesRepository {
    private object PreferencesKeys {
        val IS_PRO_USER = booleanPreferencesKey("is_pro_user")
    }

    override val isProUser: Flow<Boolean> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[PreferencesKeys.IS_PRO_USER] ?: false
            }

    override suspend fun setProUser(isPro: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PRO_USER] = isPro
        }
    }
}
