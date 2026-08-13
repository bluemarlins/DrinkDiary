package com.bluemarlin.drinkdiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TasteTypeTest {
    private fun directions(
        sweet: TraitAnswer,
        body: TraitAnswer,
        intensity: TraitAnswer,
        after: TraitAnswer,
    ) = mapOf(
        Trait.Sweetness to sweet,
        Trait.Body to body,
        Trait.Intensity to intensity,
        Trait.Aftertaste to after,
    )

    @Test
    fun `code follows the fixed axis order`() {
        val type =
            TasteType.from(
                directions(TraitAnswer.Low, TraitAnswer.High, TraitAnswer.High, TraitAnswer.High),
            )
        assertEquals("DFRE", type?.code)
    }

    @Test
    fun `opposite answers produce the opposite code`() {
        val type =
            TasteType.from(
                directions(TraitAnswer.High, TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low),
            )
        assertEquals("SLMQ", type?.code)
    }

    @Test
    fun `an unresolved axis means no type at all`() {
        val type =
            TasteType.from(
                directions(TraitAnswer.Low, TraitAnswer.High, TraitAnswer.Unsure, TraitAnswer.High),
            )
        assertNull(type)
    }

    @Test
    fun `a missing axis means no type at all`() {
        val type =
            TasteType.from(
                mapOf(Trait.Sweetness to TraitAnswer.Low, Trait.Body to TraitAnswer.High),
            )
        assertNull(type)
    }

    @Test
    fun `only shared traits take part in the type`() {
        assertEquals(TasteType.axisOrder, Trait.shared)
    }
}
