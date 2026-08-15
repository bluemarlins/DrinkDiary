package com.bluemarlin.drinkdiary.domain.repository

import com.bluemarlin.drinkdiary.domain.model.TagCategory
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isProUser: Flow<Boolean>

    suspend fun setProUser(isPro: Boolean)

    // 사용자가 "매번 물어봐 달라"고 고른 태그. 기본은 비어 있다 —
    // 아무것도 안 고른 사용자의 5탭 예산은 그대로여야 한다.
    val alwaysAskTags: Flow<Set<TagCategory>>

    // 첫 기록 직후에 한 번만 묻고, 답했다는 사실을 남긴다.
    // 첫 기록 '전'에 묻지 않는 이유는 prd.md S1 — 온보딩이 길면 그 자리에서 이탈한다.
    val hasChosenTagPreferences: Flow<Boolean>

    suspend fun setAlwaysAskTags(tags: Set<TagCategory>)
}
