package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.MonthlySummary
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.YearMonth
import java.time.ZoneId

// "이번 달"의 경계는 **기기의 시간대**에서 정한다. UTC로 자르면 월초·월말의 기록이
// 옆 달로 넘어간다 — 밤에 마시고 기록하는 앱이라 그 경계에 실제로 기록이 몰린다.
//
// `Clock`을 주입받는 이유는 테스트다. `System.currentTimeMillis()`를 직접 부르면
// 이 UseCase는 실행하는 달에 따라 결과가 달라져 검증할 수 없다.
class ObserveMonthlySummaryUseCase(
    private val repository: DrinkRecordRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    operator fun invoke(): Flow<MonthlySummary> =
        repository.observeRecords().map { records ->
            summarize(records.filter { it.recordedAtMillis in currentMonthRange() })
        }

    private fun currentMonthRange(): LongRange {
        val zone: ZoneId = clock.zone
        val month = YearMonth.now(clock)
        val start =
            month
                .atDay(1)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()
        // 다음 달 1일 0시 **직전**까지. `atEndOfMonth().atTime(23,59,59)`로 자르면
        // 마지막 1초 미만이 빠진다.
        val end =
            month
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli() - 1
        return start..end
    }

    private fun summarize(records: List<DrinkRecord>): MonthlySummary {
        if (records.isEmpty()) return MonthlySummary.Empty

        // 주종 순서를 enum 선언 순으로 고정한다. 개수 순으로 두면 한 잔 차이로 막대가
        // 자리를 바꿔 "달라진 것"처럼 보인다.
        val byType =
            DrinkType.entries
                .map { type -> MonthlySummary.TypeCount(type, records.count { it.type == type }) }
                .filter { it.count > 0 }

        return MonthlySummary(
            total = records.size,
            byType = byType,
            averageRating = records.map { it.rating }.average(),
            repurchaseCount = records.count { it.collectionStatus == CollectionStatus.Repurchase },
            // 동점이면 최근 것. 같은 점수를 여러 번 줬을 때 매번 다른 잔이 뜨면
            // 화면이 이유 없이 바뀌는 것처럼 보인다.
            topRecord = records.maxWithOrNull(compareBy({ it.rating }, { it.recordedAtMillis })),
        )
    }
}
