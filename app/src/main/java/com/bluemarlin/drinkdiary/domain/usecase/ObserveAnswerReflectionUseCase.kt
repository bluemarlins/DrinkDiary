package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AnswerReflection
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TraitLeaning
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 판정 전 구간의 되비침(prd.md F3-3 (d)).
//
// **이것은 판정기가 아니다.** 여기서 세는 것은 답의 개수이고, 유형을 정하는 것은
// 축값과 만족도의 상관이다(4-1). 둘은 자주 다른 답을 낸다 — 묵직한 것만 골라 마셨더라도
// 그중 낮게 평가한 것이 많으면 그것은 선호가 아니다. 그래서 결과 타입 이름도
// `TastePreference`가 아니라 `TraitLeaning`이고, 문구도 "좋아하신다"가 아니라 "답하셨다"다.
class ObserveAnswerReflectionUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(scope: TypeScope): Flow<AnswerReflection> {
        val drinkType =
            when (scope) {
                TypeScope.Wine -> DrinkType.Wine
                TypeScope.Whiskey -> DrinkType.Whiskey
                TypeScope.Combined -> null
            }

        return repository.observeRecords(drinkType).map { records ->
            // 축 선언 순서를 그대로 쓴다. 개수 순으로 두면 한 잔 차이로 줄이 자리를 바꿔
            // "달라진 것"처럼 보인다(이번 달 회고의 주종 막대와 같은 이유).
            AnswerReflection(Trait.shared.mapNotNull { leaningOf(it, records) })
        }
    }

    private fun leaningOf(
        trait: Trait,
        records: List<DrinkRecord>,
    ): TraitLeaning? {
        val answers = records.mapNotNull { it.taste[trait] }
        val low = answers.count { it == TraitAnswer.Low }
        val high = answers.count { it == TraitAnswer.High }

        // 한쪽으로 기운 답이 없으면 그 축은 말하지 않는다. '보통'이 대부분인 축을
        // 억지로 한쪽에 세우면 없는 경향을 지어내는 것이 된다.
        val dominant = maxOf(low, high)
        if (dominant < ReflectionThresholds.MIN_LEANING) return null
        if (low == high) return null

        return TraitLeaning(trait, if (high > low) TraitAnswer.High else TraitAnswer.Low)
    }
}

object ReflectionThresholds {
    // 한쪽으로 답한 잔이 이만큼은 있어야 되비출 것이 있다고 본다. 판정 임계치보다 훨씬
    // 낮은 것은 의도된 것이다 — 이 값은 무엇을 **단정**하는 관문이 아니라, 사용자가 남긴
    // 답을 그대로 되읽어 줄 만한 최소한이다.
    const val MIN_LEANING = 2
}
