package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import org.junit.Assert.assertEquals
import org.junit.Test

class TraitStatusTest {
    private fun pref(
        direction: TraitAnswer? = null,
        high: Int = 0,
        low: Int = 0,
        unsure: Int = 0,
    ) = TraitPreference(Trait.Body, direction, high, low, unsure)

    @Test
    fun `resolved wins regardless of sample counts`() {
        assertEquals(TraitStatus.Resolved, traitStatus(pref(direction = TraitAnswer.High, high = 3, low = 3)))
    }

    @Test
    fun `mostly unsure when unsure outnumbers the real answers`() {
        assertEquals(TraitStatus.MostlyUnsure, traitStatus(pref(high = 1, low = 1, unsure = 5)))
    }

    @Test
    fun `needs samples when either side is below the floor`() {
        assertEquals(TraitStatus.NeedsSamples, traitStatus(pref(high = 1, low = 5)))
        assertEquals(TraitStatus.NeedsSamples, traitStatus(pref(high = 5, low = 1)))
    }

    @Test
    fun `needs a clearer gap when both sides cleared the floor but still unresolved`() {
        assertEquals(TraitStatus.NeedsClearerGap, traitStatus(pref(high = 4, low = 4)))
    }

    @Test
    fun `minimum needed is the larger deficit, not the sum`() {
        assertEquals(2, minimumRecordsNeeded(pref(high = 1, low = 2)))
        assertEquals(0, minimumRecordsNeeded(pref(high = 4, low = 4)))
    }
}
