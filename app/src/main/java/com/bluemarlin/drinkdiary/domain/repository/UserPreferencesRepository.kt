package com.bluemarlin.drinkdiary.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isProUser: Flow<Boolean>

    suspend fun setProUser(isPro: Boolean)
}
