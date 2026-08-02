package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow

class ObserveSearchResultsUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(query: String): Flow<List<DrinkRecord>> = repository.observeSearchResults(query.trim())
}
