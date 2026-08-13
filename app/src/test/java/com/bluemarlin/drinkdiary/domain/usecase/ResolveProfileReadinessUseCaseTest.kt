package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveProfileReadinessUseCaseTest {
    private val resolve = ResolveProfileReadinessUseCase()

    private fun pref(
        trait: Trait,
        direction: TraitAnswer?,
        high: Int = 3,
        low: Int = 3,
        unsure: Int = 0,
    ) = TraitPreference(trait, direction, high, low, unsure)

    private fun profile(prefs: List<TraitPreference>) =
        TasteProfile(scope = TypeScope.Wine, recordCount = 12, preferences = prefs)

    @Test
    fun `all four shared axes resolved gives a type`() {
        val readiness =
            resolve(
                profile(
                    listOf(
                        pref(Trait.Sweetness, TraitAnswer.Low),
                        pref(Trait.Body, TraitAnswer.High),
                        pref(Trait.Intensity, TraitAnswer.High),
                        pref(Trait.Aftertaste, TraitAnswer.High),
                    ),
                ),
            )

        assertTrue(readiness is ProfileReadiness.Ready)
        assertEquals("DFRE", (readiness as ProfileReadiness.Ready).type.code)
    }

    @Test
    fun `one unresolved axis blocks the type but reports progress`() {
        val readiness =
            resolve(
                profile(
                    listOf(
                        pref(Trait.Sweetness, TraitAnswer.Low),
                        pref(Trait.Body, TraitAnswer.High),
                        pref(Trait.Intensity, TraitAnswer.High),
                        pref(Trait.Aftertaste, null),
                    ),
                ),
            )

        assertTrue(readiness is ProfileReadiness.Partial)
        assertEquals(listOf(Trait.Aftertaste), (readiness as ProfileReadiness.Partial).unresolved)
    }

    @Test
    fun `an axis stalled by unsure answers is reported separately`() {
        // 여운은 "잘 모르겠다"가 많아서 막혔고, 강도는 대비되는 경험이 부족해 막혔다.
        val readiness =
            resolve(
                profile(
                    listOf(
                        pref(Trait.Sweetness, TraitAnswer.Low),
                        pref(Trait.Body, TraitAnswer.High),
                        pref(Trait.Intensity, null, high = 2, low = 1, unsure = 0),
                        pref(Trait.Aftertaste, null, high = 1, low = 1, unsure = 8),
                    ),
                ),
            ) as ProfileReadiness.Partial

        assertEquals(listOf(Trait.Intensity, Trait.Aftertaste), readiness.unresolved)
        assertEquals(listOf(Trait.Aftertaste), readiness.blockedByUnsure)
    }

    @Test
    fun `nothing resolved is not ready`() {
        val readiness =
            resolve(
                profile(
                    listOf(
                        pref(Trait.Sweetness, null),
                        pref(Trait.Body, null),
                        pref(Trait.Intensity, null),
                        pref(Trait.Aftertaste, null),
                    ),
                ),
            )

        assertEquals(ProfileReadiness.NotReady, readiness)
    }

    @Test
    fun `a resolved drink-specific axis alone does not count as progress toward a type`() {
        // 탄닌은 유형에 관여하지 않으므로 이것만 판정돼서는 진행도가 생기지 않는다.
        val readiness =
            resolve(
                profile(
                    listOf(
                        pref(Trait.Sweetness, null),
                        pref(Trait.Body, null),
                        pref(Trait.Intensity, null),
                        pref(Trait.Aftertaste, null),
                        pref(Trait.Tannin, TraitAnswer.High),
                    ),
                ),
            )

        assertEquals(ProfileReadiness.NotReady, readiness)
    }
}
