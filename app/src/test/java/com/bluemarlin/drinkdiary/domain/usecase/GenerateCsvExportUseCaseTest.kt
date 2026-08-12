package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRatingBreakdown
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class GenerateCsvExportUseCaseTest {
    @Test
    fun returnsOnlyHeaderWhenNoRecords() =
        runBlocking {
            val repository = FakeDrinkRecordRepository(emptyList())
            val useCase = GenerateCsvExportUseCase(repository)

            val csv = useCase()
            val expectedHeader =
                "\"id\",\"type\",\"name\",\"price\",\"place\",\"tastingNote\",\"rating\"," +
                    "\"detailRating1\",\"detailRating2\",\"detailRating3\",\"detailRating4\",\"detailRating5\"," +
                    "\"collectionStatus\",\"recordedAt\"\n"
            assertEquals(expectedHeader, csv)
        }

    @Test
    fun recordsSortedByRecordedAtMillisDescending() =
        runBlocking {
            val zone = ZoneId.systemDefault()
            val records =
                listOf(
                    record(id = 1L, type = DrinkType.Wine, recordedAtMillis = millisOf(2026, 8, 3, 10, 0, zone)),
                    record(id = 2L, type = DrinkType.Beer, recordedAtMillis = millisOf(2026, 8, 3, 11, 0, zone)),
                    record(id = 3L, type = DrinkType.Whiskey, recordedAtMillis = millisOf(2026, 8, 3, 9, 0, zone)),
                )
            val repository = FakeDrinkRecordRepository(records)
            val useCase = GenerateCsvExportUseCase(repository)

            val csv = useCase()
            val lines = csv.trimEnd().split("\n")
            assertEquals(4, lines.size)

            assertField(lines[1], 0, "2")
            assertField(lines[2], 0, "1")
            assertField(lines[3], 0, "3")
        }

    @Test
    fun nullFieldsAreWrittenAsEmptyStrings() =
        runBlocking {
            val zone = ZoneId.systemDefault()
            val records =
                listOf(
                    DrinkRecord(
                        id = 1L,
                        type = DrinkType.Wine,
                        name = "Wine",
                        imageUri = null,
                        price = null,
                        place = null,
                        tastingNote = null,
                        rating = 4.5,
                        ratingBreakdown = DrinkRatingBreakdown(1.0, 2.0, 3.0, 4.0, 5.0),
                        collectionStatus = CollectionStatus.Normal,
                        recordedAtMillis = millisOf(2026, 8, 3, 10, 0, zone),
                    ),
                )
            val repository = FakeDrinkRecordRepository(records)
            val useCase = GenerateCsvExportUseCase(repository)

            val csv = useCase()
            val lines = csv.trimEnd().split("\n")

            assertField(lines[1], 3, "")
            assertField(lines[1], 4, "")
            assertField(lines[1], 5, "")
        }

    @Test
    fun specialCharactersAreEscapedAndQuoted() =
        runBlocking {
            val zone = ZoneId.systemDefault()
            val records =
                listOf(
                    DrinkRecord(
                        id = 1L,
                        type = DrinkType.Wine,
                        name = "A name with , comma and \"quotes\"",
                        imageUri = null,
                        price = 10000L,
                        place = "Home\nSweet\nHome",
                        tastingNote = "Delicious!\nLine 2",
                        rating = 4.5,
                        ratingBreakdown = DrinkRatingBreakdown(),
                        collectionStatus = CollectionStatus.Normal,
                        recordedAtMillis = millisOf(2026, 8, 3, 10, 0, zone),
                    ),
                )
            val repository = FakeDrinkRecordRepository(records)
            val useCase = GenerateCsvExportUseCase(repository)

            val csv = useCase()

            val expectedRecordRow =
                "\"1\",\"Wine\",\"A name with , comma and \"\"quotes\"\"\",\"10000\",\"Home\nSweet\nHome\"," +
                    "\"Delicious!\nLine 2\",\"4.5\",\"2.5\",\"2.5\",\"2.5\",\"2.5\",\"2.5\",\"Normal\",\"2026-08-03T10:00:00\"\n"

            val expectedHeader =
                "\"id\",\"type\",\"name\",\"price\",\"place\",\"tastingNote\",\"rating\"," +
                    "\"detailRating1\",\"detailRating2\",\"detailRating3\",\"detailRating4\",\"detailRating5\"," +
                    "\"collectionStatus\",\"recordedAt\"\n"

            assertEquals(expectedHeader + expectedRecordRow, csv)
        }

    private fun assertField(
        line: String,
        fieldIndex: Int,
        expectedValue: String,
    ) {
        val fields = line.split(",")
        val field = fields[fieldIndex]
        val expectedQuoted = "\"${expectedValue.replace("\"", "\"\"")}\""
        assertEquals(expectedQuoted, field)
    }

    private class FakeDrinkRecordRepository(
        private val records: List<DrinkRecord>,
    ) : DrinkRecordRepository {
        override fun observeRecords(filter: DrinkRecordFilter): Flow<List<DrinkRecord>> = flowOf(records)

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(records.firstOrNull { it.id == id })

        override fun observeRecordsByPeriod(
            startMillis: Long,
            endMillis: Long,
        ): Flow<List<DrinkRecord>> = flowOf(records)

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = flowOf(emptyList())

        override fun observeRecordsCount(): Flow<Int> = flowOf(0)

        override suspend fun save(record: DrinkRecord): AppResult<Long> = AppResult.Success(record.id)

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }

    private fun record(
        id: Long,
        type: DrinkType,
        recordedAtMillis: Long,
    ) = DrinkRecord(
        id = id,
        type = type,
        name = "Record $id",
        imageUri = null,
        price = 1000L,
        place = "Place $id",
        tastingNote = "Note $id",
        rating = 4.0,
        ratingBreakdown = DrinkRatingBreakdown(),
        collectionStatus = CollectionStatus.Normal,
        recordedAtMillis = recordedAtMillis,
    )

    private fun millisOf(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        zone: ZoneId,
    ): Long =
        LocalDateTime
            .of(year, month, day, hour, minute)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
}
