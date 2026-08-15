package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TagPreference
import com.bluemarlin.drinkdiary.domain.model.TagValueRating
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 태그별 만족도 차이를 낸다. 감각 축과 별개의 경로다.
//
// 감각 축이 술을 구분하지 못한다는 것이 검증됐으므로(103종 중 유형 성립 9.7%),
// 범주형 사실을 두 번째 경로로 쓴다. 무엇보다 이쪽 결과는 **가져갈 수 있다** —
// "셰리 캐스크 4.3점"은 우리가 모르는 술 앞에서도 쓸 수 있지만, 취향 유형은 그렇지 않다.
// software-architecture.md 4-2절.
class ObserveTagPreferenceUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(scope: TypeScope): Flow<List<TagPreference>> {
        val drinkType =
            when (scope) {
                TypeScope.Wine -> DrinkType.Wine
                TypeScope.Whiskey -> DrinkType.Whiskey
                TypeScope.Combined -> null
            }

        return repository.observeRecords(drinkType).map { records ->
            TagCategory.entries.mapNotNull { category -> summarise(category, records) }
        }
    }

    private fun summarise(
        category: TagCategory,
        records: List<DrinkRecord>,
    ): TagPreference? {
        val tagged = records.mapNotNull { record -> record.tags[category]?.let { it to record.rating } }
        if (tagged.isEmpty()) return null

        val byValue =
            tagged
                .groupBy({ it.first }, { it.second })
                .map { (value, ratings) -> TagValueRating(value, ratings.size, ratings.average()) }
                // 표본이 적은 값을 위에 올리면 우연이 결론처럼 보인다.
                .sortedWith(compareByDescending<TagValueRating> { it.averageRating }.thenByDescending { it.samples })

        // 값이 하나뿐이면 비교가 아니다 — "셰리만 마셨다"는 셰리를 좋아한다는 뜻이 아니다.
        val comparable = byValue.filter { it.samples >= TagThresholds.MIN_SAMPLES_PER_VALUE }
        val gap =
            if (comparable.size < 2) {
                null
            } else {
                comparable.first().averageRating - comparable.last().averageRating
            }

        return TagPreference(
            category = category,
            values = byValue,
            // 차이가 작으면 방향을 말하지 않는다. 감각 축에서와 같은 이유다.
            meaningfulGap = gap != null && gap >= TagThresholds.MIN_RATING_GAP,
        )
    }
}

object TagThresholds {
    // 값 하나가 결론에 참여하려면 이만큼은 모여야 한다.
    const val MIN_SAMPLES_PER_VALUE = 3

    // 이 정도는 벌어져야 "이쪽을 더 좋아한다"고 말한다 (평점 1~5 기준).
    const val MIN_RATING_GAP = 0.5
}
