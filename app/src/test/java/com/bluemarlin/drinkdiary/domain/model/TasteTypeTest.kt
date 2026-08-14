package com.bluemarlin.drinkdiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TasteTypeTest {
    private fun judgements(
        sweet: TastePreference?,
        body: TastePreference?,
        intensity: TastePreference?,
        after: TastePreference?,
    ) = mapOf(
        Trait.Sweetness to sweet,
        Trait.Body to body,
        Trait.Intensity to intensity,
        Trait.Aftertaste to after,
    )

    private val H = TastePreference.High
    private val L = TastePreference.Low
    private val N = TastePreference.Neutral

    @Test
    fun `code follows the fixed axis order`() {
        assertEquals("DFRE", TasteType.from(judgements(L, H, H, H))?.code)
    }

    @Test
    fun `opposite answers produce the opposite code`() {
        assertEquals("SLMQ", TasteType.from(judgements(H, L, L, L))?.code)
    }

    // 중립은 판정 결과이므로 유형을 막지 않는다. 이걸 막으면 사용자의 약 80%가 유형을 못 본다.
    @Test
    fun `a neutral axis still produces a type`() {
        val type = TasteType.from(judgements(L, H, N, H))

        assertEquals("DFXE", type?.code)
        assertEquals(listOf(Trait.Intensity), type?.neutral)
    }

    @Test
    fun `every axis neutral is a valid type, not a failure`() {
        val type = TasteType.from(judgements(N, N, N, N))

        assertEquals("XXXX", type?.code)
        assertTrue(type?.directional?.isEmpty() == true)
    }

    // 중립("취향이 없다")과 미판정("아직 모른다")은 다르다. 후자만 유형을 막는다.
    @Test
    fun `an axis with no verdict yet means no type`() {
        assertNull(TasteType.from(judgements(L, H, null, H)))
    }

    @Test
    fun `a missing axis means no type at all`() {
        assertNull(TasteType.from(mapOf(Trait.Sweetness to L, Trait.Body to H)))
    }

    @Test
    fun `directional keeps the fixed axis order`() {
        val type = TasteType.from(judgements(H, N, L, N))!!

        assertEquals(listOf(Trait.Sweetness, Trait.Intensity), type.directional.map { it.first })
        assertEquals(listOf(Trait.Body, Trait.Aftertaste), type.neutral)
    }

    @Test
    fun `only shared traits take part in the type`() {
        assertEquals(TasteType.axisOrder, Trait.shared)
    }

    @Test
    fun `all 81 combinations produce a distinct code`() {
        val all = TastePreference.entries
        val codes =
            all.flatMap { s ->
                all.flatMap { b ->
                    all.flatMap { i ->
                        all.map { a -> TasteType(s, b, i, a).code }
                    }
                }
            }

        assertEquals(81, codes.size)
        assertEquals(81, codes.toSet().size)
    }
}
