package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordInput
import com.bluemarlin.drinkdiary.domain.model.DrinkRatingBreakdown
import com.bluemarlin.drinkdiary.domain.model.SaveDrinkRecordError
import com.bluemarlin.drinkdiary.domain.model.isValidRating
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository

class SaveDrinkRecordUseCase(
    private val repository: DrinkRecordRepository,
) {
    suspend operator fun invoke(input: DrinkRecordInput): AppResult<Long> {
        val price = input.priceText.trim().takeIf { it.isNotEmpty() }?.toLongOrNull()
        val ratingBreakdown = if (input.ratingBreakdown.values.all { it == 0.0 } && input.rating.isValidRating()) {
            DrinkRatingBreakdown.fromRepresentativeRating(input.rating)
        } else {
            input.ratingBreakdown
        }
        val errors = SaveDrinkRecordError(
            type = if (input.type == null) "주류 종류를 선택해 주세요." else null,
            name = if (input.name.isBlank()) "이름을 입력해 주세요." else null,
            price = when {
                input.priceText.isBlank() -> null
                price == null -> "가격은 숫자로 입력해 주세요."
                price < 0 -> "가격은 0 이상이어야 합니다."
                else -> null
            },
            rating = if (!input.rating.isValidRating() || ratingBreakdown.values.any { !it.isValidRating() }) {
                "별점은 0.5~5점 사이에서 0.1 단위로 선택해 주세요."
            } else {
                null
            },
            collectionStatus = if (input.collectionStatus == null) "컬렉션 상태를 선택해 주세요." else null,
            recordedAt = if (input.recordedAtMillis <= 0L) "기록 일시를 선택해 주세요." else null,
        )

        if (errors.hasError) {
            return AppResult.Failure(AppError.Validation(errors))
        }

        val record = DrinkRecord(
            id = input.id,
            type = requireNotNull(input.type),
            name = input.name.trim(),
            imageUri = input.imageUri?.takeIf { it.isNotBlank() },
            price = price,
            place = input.place.trim().takeIf { it.isNotEmpty() },
            tastingNote = input.tastingNote.trim().takeIf { it.isNotEmpty() },
            rating = input.rating,
            ratingBreakdown = ratingBreakdown,
            collectionStatus = requireNotNull(input.collectionStatus),
            recordedAtMillis = input.recordedAtMillis,
        )
        return repository.save(record)
    }
}
