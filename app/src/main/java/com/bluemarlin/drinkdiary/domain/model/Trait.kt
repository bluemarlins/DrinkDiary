package com.bluemarlin.drinkdiary.domain.model

// 공통 축은 주종을 가로지르는 요약에 쓰고, 고유 축은 해당 주종 안에서만 쓴다.
enum class Trait(
    val shared: Boolean,
) {
    Sweetness(shared = true),
    Body(shared = true),
    Intensity(shared = true),
    Aftertaste(shared = true),

    Acidity(shared = false),
    Tannin(shared = false),

    Peat(shared = false),
    AlcoholBurn(shared = false),
    ;

    companion object {
        val shared: List<Trait> = entries.filter { it.shared }

        fun of(type: DrinkType): List<Trait> =
            shared +
                when (type) {
                    DrinkType.Wine -> listOf(Acidity, Tannin)
                    DrinkType.Whiskey -> listOf(Peat, AlcoholBurn)
                }
    }
}
