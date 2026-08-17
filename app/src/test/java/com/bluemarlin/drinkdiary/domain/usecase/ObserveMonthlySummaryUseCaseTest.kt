package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class ObserveMonthlySummaryUseCaseTest {
    // 한국 시간대로 고정한다. 시간대가 바뀌면 월 경계가 바뀌므로 테스트가 실행 환경에
    // 좌우되면 안 된다.
    private val zone: ZoneId = ZoneId.of("Asia/Seoul")

    // "지금"은 2026-08-17 12:00 KST다.
    private val clock: Clock =
        Clock.fixed(at(2026, 8, 17, 12, 0), zone)

    private fun at(
        y: Int,
        mo: Int,
        d: Int,
        h: Int,
        mi: Int,
    ) = LocalDateTime.of(y, mo, d, h, mi).atZone(zone).toInstant()

    private fun millis(
        y: Int,
        mo: Int,
        d: Int,
        h: Int = 12,
        mi: Int = 0,
    ) = at(y, mo, d, h, mi).toEpochMilli()

    private fun record(
        id: Long,
        type: DrinkType = DrinkType.Whiskey,
        rating: Double = 3.0,
        at: Long,
        status: CollectionStatus = CollectionStatus.Normal,
        name: String = "record$id",
    ) = DrinkRecord(
        id = id,
        type = type,
        name = name,
        rating = rating,
        collectionStatus = status,
        recordedAtMillis = at,
    )

    private fun useCase(records: List<DrinkRecord>) =
        ObserveMonthlySummaryUseCase(
            repository =
                object : DrinkRecordRepository {
                    override fun observeRecords(type: DrinkType?): Flow<List<DrinkRecord>> = flowOf(records)

                    override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(null)

                    override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = flowOf(emptyList())

                    override suspend fun save(record: DrinkRecord): AppResult<Long> = AppResult.Success(0L)

                    override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)

                    override suspend fun deleteByIds(ids: Set<Long>): AppResult<Int> = AppResult.Success(0)
                },
            clock = clock,
        )

    @Test
    fun `records outside this month are excluded`() =
        runBlocking {
            val summary =
                useCase(
                    listOf(
                        record(1, at = millis(2026, 8, 1)),
                        record(2, at = millis(2026, 8, 31, 23, 59)),
                        // 경계 밖
                        record(3, at = millis(2026, 7, 31, 23, 59)),
                        record(4, at = millis(2026, 9, 1, 0, 0)),
                    ),
                )().first()

            assertEquals(2, summary.total)
        }

    // 월 경계는 UTC가 아니라 기기 시간대에서 잘려야 한다. KST 8월 1일 0시는 UTC로는
    // 7월 31일 15시라, UTC로 자르면 이 기록이 지난달로 밀린다.
    @Test
    fun `the month boundary follows the device time zone, not UTC`() =
        runBlocking {
            val summary = useCase(listOf(record(1, at = millis(2026, 8, 1, 0, 30))))().first()

            assertEquals(1, summary.total)
        }

    @Test
    fun `type counts keep declaration order and drop empty types`() =
        runBlocking {
            val summary =
                useCase(
                    listOf(
                        record(1, type = DrinkType.Whiskey, at = millis(2026, 8, 2)),
                        record(2, type = DrinkType.Whiskey, at = millis(2026, 8, 3)),
                    ),
                )().first()

            assertEquals(1, summary.byType.size)
            assertEquals(DrinkType.Whiskey, summary.byType.single().type)
            assertEquals(2, summary.byType.single().count)
        }

    @Test
    fun `average and repurchase count come from this month only`() =
        runBlocking {
            val summary =
                useCase(
                    listOf(
                        record(1, rating = 4.0, at = millis(2026, 8, 2), status = CollectionStatus.Repurchase),
                        record(2, rating = 2.0, at = millis(2026, 8, 3)),
                        // 지난달의 높은 점수는 이번 달 평균을 올리면 안 된다.
                        record(3, rating = 5.0, at = millis(2026, 7, 2), status = CollectionStatus.Repurchase),
                    ),
                )().first()

            assertEquals(3.0, summary.averageRating!!, 0.0001)
            assertEquals(1, summary.repurchaseCount)
        }

    // 동점이면 최근 것. 같은 점수를 여러 번 줬을 때 화면이 이유 없이 바뀌면 안 된다.
    @Test
    fun `top record breaks ties by recency`() =
        runBlocking {
            val summary =
                useCase(
                    listOf(
                        record(1, rating = 5.0, at = millis(2026, 8, 2), name = "먼저"),
                        record(2, rating = 5.0, at = millis(2026, 8, 9), name = "나중"),
                        record(3, rating = 4.0, at = millis(2026, 8, 15), name = "가장 최근이지만 낮음"),
                    ),
                )().first()

            assertEquals("나중", summary.topRecord!!.name)
        }

    // 기록이 없으면 평균도 없다. 0.0으로 채우면 "0점을 줬다"로 읽힌다.
    @Test
    fun `an empty month has no average rather than a zero`() =
        runBlocking {
            val summary = useCase(listOf(record(1, at = millis(2026, 7, 2))))().first()

            assertTrue(summary.isEmpty)
            assertNull(summary.averageRating)
            assertNull(summary.topRecord)
        }
}
