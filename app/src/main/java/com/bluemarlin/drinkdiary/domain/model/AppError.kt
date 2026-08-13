package com.bluemarlin.drinkdiary.domain.model

sealed interface AppError {
    data object NotFound : AppError

    data object Storage : AppError

    data class Validation(
        val error: SaveRecordError,
    ) : AppError
}

// 문구가 아니라 무엇이 잘못됐는지만 담는다. 표시 문자열은 UI 계층에서 해석한다.
data class SaveRecordError(
    val type: Boolean = false,
    val name: Boolean = false,
    val rating: Boolean = false,
    val recordedAt: Boolean = false,
    val vintage: Boolean = false,
) {
    val hasError: Boolean get() = type || name || rating || recordedAt || vintage
}
