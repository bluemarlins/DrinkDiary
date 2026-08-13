package com.bluemarlin.drinkdiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun `unsure is not a direction`() {
        assertFalse(TraitAnswer.Unsure.isDirectional)
        assertTrue(TraitAnswer.Low.isDirectional)
        assertTrue(TraitAnswer.High.isDirectional)
    }

    @Test
    fun `unsure answers are not counted as perceived`() {
        val input =
            TasteInput()
                .with(Trait.Body, TraitAnswer.High)
                .with(Trait.Sweetness, TraitAnswer.Unsure)
                .with(Trait.Intensity, TraitAnswer.Low)
        assertEquals(2, input.directionalCount)
    }
}
