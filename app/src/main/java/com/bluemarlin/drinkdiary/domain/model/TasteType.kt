package com.bluemarlin.drinkdiary.domain.model

// 공통 축 4개의 선호 방향 조합. 축 순서는 고정이며 코드에서 역추적할 수 있어야 한다.
data class TasteType(
    val sweetness: TraitAnswer,
    val body: TraitAnswer,
    val intensity: TraitAnswer,
    val aftertaste: TraitAnswer,
) {
    val code: String
        get() =
            buildString {
                append(if (sweetness == TraitAnswer.High) 'S' else 'D')
                append(if (body == TraitAnswer.High) 'F' else 'L')
                append(if (intensity == TraitAnswer.High) 'R' else 'M')
                append(if (aftertaste == TraitAnswer.High) 'E' else 'Q')
            }

    companion object {
        val axisOrder: List<Trait> =
            listOf(Trait.Sweetness, Trait.Body, Trait.Intensity, Trait.Aftertaste)

        // 축 하나라도 방향이 없으면 유형은 성립하지 않는다.
        fun from(directions: Map<Trait, TraitAnswer>): TasteType? {
            val picked = axisOrder.map { directions[it] }
            if (picked.any { it == null || !it.isDirectional }) return null
            return TasteType(picked[0]!!, picked[1]!!, picked[2]!!, picked[3]!!)
        }
    }
}

enum class TypeScope {
    Wine,
    Whiskey,
    Combined,
}
