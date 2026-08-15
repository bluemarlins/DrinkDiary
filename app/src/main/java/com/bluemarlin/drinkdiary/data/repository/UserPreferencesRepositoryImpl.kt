package com.bluemarlin.drinkdiary.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bluemarlin.drinkdiary.domain.model.TagCategory
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
        val ALWAYS_ASK_TAGS = stringSetPreferencesKey("always_ask_tags")
        val HAS_CHOSEN_TAG_PREFERENCES = booleanPreferencesKey("has_chosen_tag_preferences")
    }

    private val preferences: Flow<Preferences> =
        context.dataStore.data.catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }

    override val isProUser: Flow<Boolean> =
        preferences.map { it[PreferencesKeys.IS_PRO_USER] ?: false }

    override suspend fun setProUser(isPro: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_PRO_USER] = isPro }
    }

    override val alwaysAskTags: Flow<Set<TagCategory>> =
        preferences.map { stored ->
            // 모르는 이름은 버린다. 태그 집합이 아직 가설이라 값이 사라질 수 있고,
            // 그때 설정 전체를 못 읽으면 안 된다.
            stored[PreferencesKeys.ALWAYS_ASK_TAGS]
                .orEmpty()
                .mapNotNull { name -> TagCategory.entries.find { it.name == name } }
                .toSet()
        }

    override val hasChosenTagPreferences: Flow<Boolean> =
        preferences.map { it[PreferencesKeys.HAS_CHOSEN_TAG_PREFERENCES] ?: false }

    override suspend fun setAlwaysAskTags(tags: Set<TagCategory>) {
        context.dataStore.edit {
            it[PreferencesKeys.ALWAYS_ASK_TAGS] = tags.map(TagCategory::name).toSet()
            // 아무것도 안 고른 것도 대답이다. 다시 묻지 않는다.
            it[PreferencesKeys.HAS_CHOSEN_TAG_PREFERENCES] = true
        }
    }
}
