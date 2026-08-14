package com.bluemarlin.drinkdiary.domain.model

// 공통 축 4개의 선호 상태 조합. 축 순서는 고정이며 코드에서 역추적할 수 있어야 한다.
// 축당 3상태이므로 3^4 = 81유형. X는 중립이며 MBTI의 X(균형)와 같은 뜻이다.
data class TasteType(
    val sweetness: TastePreference,
    val body: TastePreference,
    val intensity: TastePreference,
    val aftertaste: TastePreference,
) {
    val code: String
        get() =
            buildString {
                append(letter(sweetness, high = 'S', low = 'D'))
                append(letter(body, high = 'F', low = 'L'))
                append(letter(intensity, high = 'R', low = 'M'))
                append(letter(aftertaste, high = 'E', low = 'Q'))
            }

    val preferences: List<Pair<Trait, TastePreference>>
        get() = axisOrder.zip(listOf(sweetness, body, intensity, aftertaste))

    // 방향이 있는 축. 이름과 문장은 여기서만 조립한다.
    val directional: List<Pair<Trait, TastePreference>>
        get() = preferences.filter { it.second != TastePreference.Neutral }

    val neutral: List<Trait>
        get() = preferences.filter { it.second == TastePreference.Neutral }.map { it.first }

    companion object {
        val axisOrder: List<Trait> =
            listOf(Trait.Sweetness, Trait.Body, Trait.Intensity, Trait.Aftertaste)

        private fun letter(
            preference: TastePreference,
            high: Char,
            low: Char,
        ): Char =
            when (preference) {
                TastePreference.High -> high
                TastePreference.Low -> low
                TastePreference.Neutral -> 'X'
            }

        // 표본이 없어 아직 판단하지 못한 축(null)이 하나라도 있으면 유형은 성립하지 않는다.
        // 중립은 판단 결과이므로 유형을 막지 않는다 — 둘을 섞으면 "취향이 없다"와
        // "아직 모른다"가 같은 것으로 보이게 된다.
        fun from(judgements: Map<Trait, TastePreference?>): TasteType? {
            val picked = axisOrder.map { judgements[it] }
            if (picked.any { it == null }) return null
            return TasteType(picked[0]!!, picked[1]!!, picked[2]!!, picked[3]!!)
        }
    }
}

enum class TypeScope {
    Wine,
    Whiskey,
    Combined,
}
