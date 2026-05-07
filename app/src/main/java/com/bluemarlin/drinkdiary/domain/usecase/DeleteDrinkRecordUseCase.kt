package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository

class DeleteDrinkRecordUseCase(
    private val repository: DrinkRecordRepository,
) {
    suspend operator fun invoke(id: Long): AppResult<Unit> = repository.deleteById(id)
}
