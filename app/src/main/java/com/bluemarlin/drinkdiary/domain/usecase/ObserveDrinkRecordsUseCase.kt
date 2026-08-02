package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow

class ObserveDrinkRecordsUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(filter: DrinkRecordFilter): Flow<List<DrinkRecord>> = repository.observeRecords(filter)
}
