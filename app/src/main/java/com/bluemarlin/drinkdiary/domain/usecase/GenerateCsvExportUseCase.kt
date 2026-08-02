package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GenerateCsvExportUseCase(
    private val repository: DrinkRecordRepository,
) {
    suspend operator fun invoke(): String {
        val records = repository.observeRecords(DrinkRecordFilter()).first()
        val sortedRecords = records.sortedByDescending { it.recordedAtMillis }

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val zoneId = ZoneId.systemDefault()

        val csvBuilder = StringBuilder()

        val headers =
            listOf(
                "id",
                "type",
                "name",
                "price",
                "place",
                "tastingNote",
                "rating",
                "detailRating1",
                "detailRating2",
                "detailRating3",
                "detailRating4",
                "detailRating5",
                "collectionStatus",
                "recordedAt",
            )
        csvBuilder.append(headers.joinToString(separator = ",", postfix = "\n") { escapeAndQuote(it) })

        for (record in sortedRecords) {
            val recordedAtStr =
                Instant
                    .ofEpochMilli(record.recordedAtMillis)
                    .atZone(zoneId)
                    .toLocalDateTime()
                    .format(formatter)

            val rowFields =
                listOf(
                    record.id.toString(),
                    record.type.name,
                    record.name,
                    record.price?.toString() ?: "",
                    record.place ?: "",
                    record.tastingNote ?: "",
                    record.rating.toString(),
                    record.ratingBreakdown.first.toString(),
                    record.ratingBreakdown.second.toString(),
                    record.ratingBreakdown.third.toString(),
                    record.ratingBreakdown.fourth.toString(),
                    record.ratingBreakdown.fifth.toString(),
                    record.collectionStatus.name,
                    recordedAtStr,
                )
            csvBuilder.append(rowFields.joinToString(separator = ",", postfix = "\n") { escapeAndQuote(it) })
        }

        return csvBuilder.toString()
    }

    private fun escapeAndQuote(field: String): String = "\"${field.replace("\"", "\"\"")}\""
}
