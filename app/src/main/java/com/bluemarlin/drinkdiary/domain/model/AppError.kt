package com.bluemarlin.drinkdiary.domain.model

sealed interface AppError {
    data object NotFound : AppError
    data object Storage : AppError
    data class Validation(val error: SaveDrinkRecordError) : AppError
}
