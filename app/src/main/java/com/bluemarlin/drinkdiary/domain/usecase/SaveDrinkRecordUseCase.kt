package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordInput
import com.bluemarlin.drinkdiary.domain.model.SaveDrinkRecordError
import com.bluemarlin.drinkdiary.domain.model.isValidRating
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository

class SaveDrinkRecordUseCase(
    private val repository: DrinkRecordRepository,
) {
    suspend operator fun invoke(input: DrinkRecordInput): AppResult<Long> {
        val price = input.priceText.trim().takeIf { it.isNotEmpty() }?.toLongOrNull()
        // Custom tags are free text, so they get length-capped and de-duplicated here; unknown
        // keys are allowed through on purpose, since a user-invented tag is a valid tag.
        val tags = input.tastingTags
            .mapNotNull { it.trim().takeIf(String::isNotEmpty)?.take(MAX_TAG_LENGTH) }
            .distinct()

        val errors = SaveDrinkRecordError(
            type = if (input.type == null) "주류 종류를 선택해 주세요." else null,
            name = if (input.name.isBlank()) "이름을 입력해 주세요." else null,
            price = when {
                input.priceText.isBlank() -> null
                price == null -> "가격은 숫자로 입력해 주세요."
                price < 0 -> "가격은 0 이상이어야 합니다."
                else -> null
            },
            rating = if (!input.rating.isValidRating()) {
                "별점은 0.5~5점 사이에서 0.5 단위로 선택해 주세요."
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
            tastingTags = tags.take(MAX_TAG_COUNT),
            rating = input.rating,
            abv = input.abv,
            volumeMl = input.volumeMl,
            collectionStatus = requireNotNull(input.collectionStatus),
            recordedAtMillis = input.recordedAtMillis,
        )
        return repository.save(record)
    }

    private companion object {
        const val MAX_TAG_COUNT = 15
        const val MAX_TAG_LENGTH = 20
    }
}
