package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TastingGap
import com.bluemarlin.drinkdiary.ui.DrinkLabels

// 공백 안내의 문구(prd.md F3-3 (b)).
//
// **권유하지 않는다.** "드셔보세요"라고 쓰는 순간 이 카드는 추천이 되고, 추천이라면
// 무엇을 근거로 권하느냐는 물음이 따라온다 — 우리에게는 남의 평점이 없고, 있어도 쓰지 않는다.
// 말할 수 있는 것은 사실 하나뿐이다: 한쪽은 쌓였고 다른 쪽은 비어 있다.
object TastingGapCopy {
    fun label(gap: TastingGap): String = DrinkLabels.tagCategory(gap.category)

    fun sentence(
        gap: TastingGap,
        type: DrinkType?,
    ): String {
        val recorded = DrinkLabels.tagValue(gap.category, gap.recordedValue, type)
        val missing = DrinkLabels.tagValue(gap.category, gap.missingValue, type)
        // 비어 있는 쪽에 "0잔"을 붙이지 않는다. 0은 성적이 아니라 아직 안 한 일이다(F3-2와 같은 이유).
        return "${Josa.topic(recorded)} ${gap.recordedSamples}잔 마셨는데, ${Josa.topic(missing)} 아직 없어요."
    }
}
