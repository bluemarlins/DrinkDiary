package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveProfileReadinessUseCaseTest {
    private val resolve = ResolveProfileReadinessUseCase()

    private val H = TastePreference.High
    private val L = TastePreference.Low
    private val N = TastePreference.Neutral

    private fun pref(
        trait: Trait,
        preference: TastePreference?,
        samples: Int = TasteThresholds.MIN_SAMPLES,
    ) = TraitPreference(trait, preference, samples, 0.6, 0)

    private fun profile(prefs: List<TraitPreference>) =
        TasteProfile(scope = TypeScope.Wine, recordCount = 12, preferences = prefs)

    @Test
    fun `all four shared axes judged gives a type`() {
        val readiness =
            resolve(
                profile(
                    listOf(
                        pref(Trait.Sweetness, L),
                        pref(Trait.Body, H),
                        pref(Trait.Intensity, H),
                        pref(Trait.Aftertaste, H),
                    ),
                ),
            )

        assertTrue(readiness is ProfileReadiness.Ready)
        assertEquals("DFRE", (readiness as ProfileReadiness.Ready).type.code)
    }

    // 중립이 섞여도 유형은 나온다. 이걸 막으면 사용자 대부분이 영원히 유형을 못 본다.
    @Test
    fun `a neutral axis does not block the type`() {
        val readiness =
            resolve(
                profile(
                    listOf(
                        pref(Trait.Sweetness, L),
                        pref(Trait.Body, H),
                        pref(Trait.Intensity, N),
                        pref(Trait.Aftertaste, H),
                    ),
                ),
            )

        assertEquals("DFXE", (readiness as ProfileReadiness.Ready).type.code)
    }

    @Test
    fun `every axis neutral still gives a type`() {
        val readiness =
            resolve(
                profile(Trait.shared.map { pref(it, N) }),
            )

        assertEquals("XXXX", (readiness as ProfileReadiness.Ready).type.code)
    }

    // 표본이 없어 아직 판단 못 한 축은 유형을 막는다 — 중립과 구분되는 지점.
    @Test
    fun `an axis without a verdict blocks the type and reports the distance`() {
        val readiness =
            resolve(
                profile(
                    listOf(
                        pref(Trait.Sweetness, L),
                        pref(Trait.Body, H),
                        pref(Trait.Intensity, H),
                        pref(Trait.Aftertaste, null, samples = 2),
                    ),
                ),
            )

        assertTrue(readiness is ProfileReadiness.NotReady)
        assertEquals(
            TasteThresholds.MIN_SAMPLES - 2,
            (readiness as ProfileReadiness.NotReady).recordsNeeded,
        )
    }

    @Test
    fun `no records at all asks for the full floor`() {
        val readiness =
            resolve(profile(Trait.shared.map { pref(it, null, samples = 0) }))

        assertEquals(
            TasteThresholds.MIN_SAMPLES,
            (readiness as ProfileReadiness.NotReady).recordsNeeded,
        )
    }

    @Test
    fun `a judged drink-specific axis alone does not produce a type`() {
        // 탄닌은 유형에 관여하지 않는다.
        val readiness =
            resolve(
                profile(
                    Trait.shared.map { pref(it, null, samples = 1) } + pref(Trait.Tannin, H),
                ),
            )

        assertTrue(readiness is ProfileReadiness.NotReady)
    }
}
