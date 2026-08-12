package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CheckRecordLimitUseCase(
    private val drinkRecordRepository: DrinkRecordRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> =
        combine(
            drinkRecordRepository.observeRecordsCount(),
            userPreferencesRepository.isProUser,
        ) { count, isPro ->
            if (isPro) {
                true
            } else {
                count < LIMIT
            }
        }

    companion object {
        const val LIMIT = 30
    }
}
