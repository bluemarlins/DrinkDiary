package com.bluemarlin.drinkdiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TraitTest {
    @Test
    fun `wine and whiskey share four traits and differ in their own`() {
        val wine = Trait.of(DrinkType.Wine)
        val whiskey = Trait.of(DrinkType.Whiskey)

        assertEquals(Trait.shared, wine.filter { it.shared })
        assertEquals(Trait.shared, whiskey.filter { it.shared })
        assertEquals(listOf(Trait.Acidity, Trait.Tannin), wine.filterNot { it.shared })
        assertEquals(listOf(Trait.Peat, Trait.AlcoholBurn), whiskey.filterNot { it.shared })
    }

    // 순서값은 상관 계산의 x축이다. 뒤집히면 모든 판정 방향이 뒤집힌다.
    @Test
    fun `answer levels are ordered low to high`() {
        assertEquals(0, TraitAnswer.Low.level)
        assertEquals(1, TraitAnswer.Mid.level)
        assertEquals(2, TraitAnswer.High.level)
    }

    @Test
    fun `mid answers are not counted as leaning`() {
        val input =
            TasteInput()
                .with(Trait.Body, TraitAnswer.High)
                .with(Trait.Sweetness, TraitAnswer.Mid)
                .with(Trait.Intensity, TraitAnswer.Low)

        assertEquals(2, input.leaningCount)
        // 세지 않는다고 버리는 것은 아니다 — 답 자체는 그대로 남아 판정에 쓰인다.
        assertEquals(3, input.answers.size)
        assertEquals(TraitAnswer.Mid, input[Trait.Sweetness])
    }
}
