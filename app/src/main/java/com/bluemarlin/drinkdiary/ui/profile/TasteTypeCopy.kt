package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.TasteType
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer

// 별칭을 손으로 짓지 않고 축 값에서 조립한다
// (taste-type-naming.md 3절 — 짧은 이름은 Body·Intensity, 문장은 4축 모두 사용).
// 축·극 라벨은 기록 화면과 공유하므로 ui.DrinkLabels 에 있다.
object TasteTypeCopy {
    fun shortName(type: TasteType): String {
        val body = if (type.body == TraitAnswer.High) "묵직한" else "가벼운"
        val intensity = if (type.intensity == TraitAnswer.High) "진한" else "은은한"
        return "$body $intensity 취향"
    }

    fun sentence(type: TasteType): String {
        val sweetness = if (type.sweetness == TraitAnswer.High) "달콤" else "드라이"
        val body = if (type.body == TraitAnswer.High) "묵직하며" else "가벼우며"
        val intensity = if (type.intensity == TraitAnswer.High) "진한" else "은은한"
        val aftertaste = if (type.aftertaste == TraitAnswer.High) "여운이 깁니다" else "산뜻하게 끝납니다"
        return "${sweetness}하고 $body, $intensity 향에 $aftertaste"
    }
}
