package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordInput
import com.bluemarlin.drinkdiary.domain.model.SaveDrinkRecordError
import com.bluemarlin.drinkdiary.domain.model.isValidOverallRating
import com.bluemarlin.drinkdiary.domain.model.isValidSensoryMetric
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository

class SaveDrinkRecordUseCase(
    private val repository: DrinkRecordRepository,
) {
    suspend operator fun invoke(input: DrinkRecordInput): AppResult<Long> {
        val price =
            input.priceText
                .trim()
                .takeIf { it.isNotEmpty() }
                ?.toLongOrNull()
        val errors =
            SaveDrinkRecordError(
                type = if (input.type == null) R.string.error_select_drink_type else null,
                name = if (input.name.isBlank()) R.string.error_enter_name else null,
                price =
                    when {
                        input.priceText.isBlank() -> null
                        price == null -> R.string.error_price_numeric
                        price < 0 -> R.string.error_price_non_negative
                        else -> null
                    },
                rating =
                    if (!input.rating.isValidOverallRating() ||
                        input.ratingBreakdown.values.any { !it.isValidSensoryMetric() }
                    ) {
                        R.string.error_invalid_rating
                    } else {
                        null
                    },
                collectionStatus =
                    if (input.collectionStatus ==
                        null
                    ) {
                        R.string.error_select_collection_status
                    } else {
                        null
                    },
                recordedAt = if (input.recordedAtMillis <= 0L) R.string.error_select_recorded_at else null,
            )

        if (errors.hasError) {
            return AppResult.Failure(AppError.Validation(errors))
        }

        val record =
            DrinkRecord(
                id = input.id,
                type = requireNotNull(input.type),
                name = input.name.trim(),
                imageUri = input.imageUri?.takeIf { it.isNotBlank() },
                price = price,
                place = input.place.trim().takeIf { it.isNotEmpty() },
                tastingNote = input.tastingNote.trim().takeIf { it.isNotEmpty() },
                rating = input.rating,
                ratingBreakdown = input.ratingBreakdown,
                collectionStatus = requireNotNull(input.collectionStatus),
                recordedAtMillis = input.recordedAtMillis,
            )
        return repository.save(record)
    }
}
