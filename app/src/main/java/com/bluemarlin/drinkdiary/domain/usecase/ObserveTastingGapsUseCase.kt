package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TastingGap
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.model.gapCandidates
import com.bluemarlin.drinkdiary.domain.repository.BottleDictionary
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 아직 안 마셔본 조합을 찾는다(prd.md F3-3 (b)).
//
// 남의 평점을 한 톨도 쓰지 않는다. 쓰는 것은 두 가지뿐이다 — 내 기록에 무엇이 있는지와,
// 그 카테고리가 가질 수 있는 값이 무엇인지. 그래서 이것은 추천이 아니라 공백 안내다.
class ObserveTastingGapsUseCase(
    private val repository: DrinkRecordRepository,
    private val dictionary: BottleDictionary,
) {
    operator fun invoke(scope: TypeScope): Flow<List<TastingGap>> {
        val drinkType =
            when (scope) {
                TypeScope.Wine -> DrinkType.Wine
                TypeScope.Whiskey -> DrinkType.Whiskey
                TypeScope.Combined -> null
            }

        return repository.observeRecords(drinkType).map { records ->
            TagCategory.entries
                .mapNotNull { category -> gapOf(category, records) }
                // 가장 크게 쏠린 것부터. 전부 나열하면 화면이 할 일 목록이 된다.
                .sortedByDescending { it.recordedSamples }
                .take(GapThresholds.MAX_GAPS)
        }
    }

    private fun gapOf(
        category: TagCategory,
        records: List<DrinkRecord>,
    ): TastingGap? {
        val candidates = category.gapCandidates
        val counts =
            records
                .mapNotNull { it.tagValue(category, dictionary) }
                .groupingBy { it }
                .eachCount()

        // 동점이면 값 선언 순서 — 같은 데이터에서 화면이 매번 다른 짝을 고르면 안 된다.
        val recorded = candidates.maxByOrNull { counts[it] ?: 0 } ?: return null
        val samples = counts[recorded] ?: 0

        // 한 잔도 없는 카테고리에서 "아직 없어요"는 정보가 아니다. 그 카테고리는 애초에
        // 사용자의 관심 밖이었거나, 우리가 물어본 적조차 없다.
        if (samples < GapThresholds.MIN_RECORDED_SAMPLES) return null

        val missing = candidates.firstOrNull { (counts[it] ?: 0) == 0 } ?: return null

        return TastingGap(
            category = category,
            recordedValue = recorded,
            recordedSamples = samples,
            missingValue = missing,
        )
    }
}

object GapThresholds {
    // 한쪽이 이만큼은 쌓여야 "쏠렸다"고 말한다. 태그 선호의 값당 최소 표본과 같은 수다 —
    // 공백을 채웠을 때 실제로 비교가 성립하는 지점이 거기이기 때문이다.
    const val MIN_RECORDED_SAMPLES = TagThresholds.MIN_SAMPLES_PER_VALUE

    // 한 번에 보여줄 개수.
    const val MAX_GAPS = 2
}
