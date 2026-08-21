package com.bluemarlin.drinkdiary.domain.model

// 공통 축은 주종을 가로지르는 요약에 쓰고, 고유 축은 해당 주종 안에서만 쓴다.
enum class Trait(
    val shared: Boolean,
) {
    Sweetness(shared = true),
    Body(shared = true),
    Aftertaste(shared = true),

    Acidity(shared = false),
    Tannin(shared = false),

    Peat(shared = false),
    AlcoholBurn(shared = false),

    Intensity(shared = true),
    ;

    companion object {
        val shared: List<Trait> = listOf(Sweetness, Body, Intensity, Aftertaste)

        val wineTraits: List<Trait> = listOf(Sweetness, Acidity, Tannin, Body, Aftertaste)
        val whiskyTraits: List<Trait> = listOf(Sweetness, Body, Peat, AlcoholBurn, Aftertaste)

        fun of(type: DrinkType): List<Trait> =
            when (type) {
                DrinkType.Wine -> wineTraits
                DrinkType.Whiskey -> whiskyTraits
            }
    }
}
