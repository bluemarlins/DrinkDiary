package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkHighlight
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.HighlightKind
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 사진과 함께 내보낼 몇 잔을 고른다(prd.md F3-4 (a)).
//
// **사진 유무로 고르지 않는다.** 가장 높게 준 잔은 사진이 없어도 가장 높게 준 잔이다.
// 사진이 있는 것만 골라 보여주면 화면은 예뻐지지만 사실이 아니게 되고, 사용자는 자기가
// 무엇을 가장 높게 평가했는지 영영 모른다. 빈 자리가 보이는 편이 정직하고, 그게 사진을
// 넣을 이유도 된다.
class ObserveDrinkHighlightsUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(scope: TypeScope): Flow<List<DrinkHighlight>> {
        val drinkType =
            when (scope) {
                TypeScope.Wine -> DrinkType.Wine
                TypeScope.Whiskey -> DrinkType.Whiskey
                TypeScope.Combined -> null
            }

        return repository.observeRecords(drinkType).map(::highlightsOf)
    }

    private fun highlightsOf(records: List<DrinkRecord>): List<DrinkHighlight> {
        if (records.isEmpty()) return emptyList()

        val picked =
            listOfNotNull(
                topRated(records),
                mostRepeated(records),
                latest(records),
            )

        // 같은 기록이 두 자리를 차지하면 카드가 같은 사진을 두 번 보여준다.
        // 앞선 것(더 말할 거리가 있는 쪽)을 남긴다.
        return picked.distinctBy { it.record.id }
    }

    // 동점이면 최근 것. 같은 점수를 여러 번 줬을 때 매번 다른 잔이 뜨면 화면이 이유 없이 바뀐다.
    private fun topRated(records: List<DrinkRecord>): DrinkHighlight? =
        records
            .maxWithOrNull(compareBy({ it.rating }, { it.recordedAtMillis }))
            ?.let { DrinkHighlight(HighlightKind.TopRated, it) }

    // 두 번 이상 기록한 이름이 없으면 이 자리는 비운다 — 한 번 마신 것을 "여러 번"이라 부를 수 없다.
    private fun mostRepeated(records: List<DrinkRecord>): DrinkHighlight? {
        val groups = records.groupBy { it.name.trim() }.filterKeys { it.isNotEmpty() }
        val (_, repeated) =
            groups.entries
                .map { it.key to it.value }
                // 개수가 같으면 최근에 마신 쪽. maxByOrNull은 첫 최대를 남기므로 정렬로 순서를 준다.
                .sortedByDescending { (_, group) -> group.maxOf { it.recordedAtMillis } }
                .maxByOrNull { (_, group) -> group.size }
                ?: return null

        if (repeated.size < REPEAT_FLOOR) return null

        // 그 이름을 대표하는 한 잔은 가장 높게 준 것이다. 여러 번 마셨다는 사실을 말하는 자리이므로
        // 대표 잔은 사용자가 가장 좋게 기억할 쪽이 맞다.
        val representative = repeated.maxWithOrNull(compareBy({ it.rating }, { it.recordedAtMillis }))!!
        return DrinkHighlight(HighlightKind.MostRepeated, representative, repeated.size)
    }

    private fun latest(records: List<DrinkRecord>): DrinkHighlight? =
        records
            .maxByOrNull { it.recordedAtMillis }
            ?.let { DrinkHighlight(HighlightKind.Latest, it) }

    private companion object {
        const val REPEAT_FLOOR = 2
    }
}
