package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.usecase.TasteThresholds
import org.junit.Assert.assertEquals
import org.junit.Test

class TraitStatusTest {
    private fun pref(
        preference: TastePreference?,
        samples: Int = TasteThresholds.MIN_SAMPLES,
        mid: Int = 0,
    ) = TraitPreference(Trait.Body, preference, samples, 0.5, mid)

    @Test
    fun `a judged direction is resolved`() {
        assertEquals(TraitStatus.Resolved, traitStatus(pref(TastePreference.High)))
        assertEquals(TraitStatus.Resolved, traitStatus(pref(TastePreference.Low)))
    }

    // 화면이 이 둘을 같은 말로 보여주면 "취향이 없다"가 "아직 부족하다"로 읽힌다.
    @Test
    fun `neutral and not-yet-judged are different statuses`() {
        assertEquals(TraitStatus.Neutral, traitStatus(pref(TastePreference.Neutral)))
        assertEquals(TraitStatus.NeedsRecords, traitStatus(pref(null, samples = 2)))
    }

    @Test
    fun `records needed counts up to the floor and never below zero`() {
        assertEquals(TasteThresholds.MIN_SAMPLES - 2, recordsNeeded(pref(null, samples = 2)))
        assertEquals(0, recordsNeeded(pref(TastePreference.Neutral, samples = 20)))
    }

    @Test
    fun `mid ratio reports how often the axis was not perceived`() {
        assertEquals(0.5, pref(TastePreference.Neutral, samples = 8, mid = 4).midRatio, 1e-9)
        assertEquals(0.0, pref(null, samples = 0).midRatio, 1e-9)
    }
}
