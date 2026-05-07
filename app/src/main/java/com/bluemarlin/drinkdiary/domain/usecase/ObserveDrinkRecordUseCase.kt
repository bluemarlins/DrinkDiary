package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow

class ObserveDrinkRecordUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(id: Long): Flow<DrinkRecord?> = repository.observeRecord(id)
}
