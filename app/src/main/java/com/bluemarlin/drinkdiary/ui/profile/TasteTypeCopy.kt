package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.TasteType
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer

// 별칭을 손으로 짓지 않고 축 값에서 조립한다
// (taste-type-naming.md 3절 — 짧은 이름은 Body·Intensity, 문장은 4축 모두 사용).
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

    fun traitLabel(trait: Trait): String =
        when (trait) {
            Trait.Sweetness -> "단맛"
            Trait.Body -> "무게감"
            Trait.Intensity -> "향의 세기"
            Trait.Aftertaste -> "여운"
            Trait.Acidity -> "산미"
            Trait.Tannin -> "떫음"
            Trait.Peat -> "스모키함"
            Trait.AlcoholBurn -> "알코올감"
        }

    // 판정된 방향의 표시 라벨. answer는 High/Low만 들어온다 — Unsure는 판정된 방향이 될 수 없다.
    fun poleLabel(
        trait: Trait,
        answer: TraitAnswer,
    ): String {
        val (low, high) =
            when (trait) {
                Trait.Sweetness -> "드라이" to "달콤"
                Trait.Body -> "가벼움" to "묵직함"
                Trait.Intensity -> "은은함" to "진함"
                Trait.Aftertaste -> "산뜻함" to "긴 여운"
                Trait.Acidity -> "부드러움" to "산뜻함"
                Trait.Tannin -> "안 떫음" to "떫음"
                Trait.Peat -> "스모키하지 않음" to "스모키함"
                Trait.AlcoholBurn -> "부드러움" to "화끈함"
            }
        return if (answer == TraitAnswer.High) high else low
    }
}
