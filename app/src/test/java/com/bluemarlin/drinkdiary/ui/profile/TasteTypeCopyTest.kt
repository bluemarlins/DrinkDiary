package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.TasteType
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import org.junit.Assert.assertEquals
import org.junit.Test

class TasteTypeCopyTest {
    private fun type(
        sweetness: TraitAnswer,
        body: TraitAnswer,
        intensity: TraitAnswer,
        aftertaste: TraitAnswer,
    ) = TasteType(sweetness, body, intensity, aftertaste)

    // taste-type-naming.md 3절의 예시 3개를 그대로 재현한다 — 문구가 문서와 어긋나면
    // 사용자에게 확정된 규칙과 다른 것이 노출된다.
    @Test
    fun `DFRE matches the spec example`() {
        val dfre = type(TraitAnswer.Low, TraitAnswer.High, TraitAnswer.High, TraitAnswer.High)

        assertEquals("DFRE", dfre.code)
        assertEquals("묵직한 진한 취향", TasteTypeCopy.shortName(dfre))
        assertEquals("드라이하고 묵직하며, 진한 향에 여운이 깁니다", TasteTypeCopy.sentence(dfre))
    }

    @Test
    fun `SLMQ matches the spec example`() {
        val slmq = type(TraitAnswer.High, TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low)

        assertEquals("SLMQ", slmq.code)
        assertEquals("가벼운 은은한 취향", TasteTypeCopy.shortName(slmq))
        assertEquals("달콤하고 가벼우며, 은은한 향에 산뜻하게 끝납니다", TasteTypeCopy.sentence(slmq))
    }

    @Test
    fun `DLRQ matches the spec example`() {
        val dlrq = type(TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.High, TraitAnswer.Low)

        assertEquals("DLRQ", dlrq.code)
        assertEquals("가벼운 진한 취향", TasteTypeCopy.shortName(dlrq))
        assertEquals("드라이하고 가벼우며, 진한 향에 산뜻하게 끝납니다", TasteTypeCopy.sentence(dlrq))
    }

    @Test
    fun `poleLabel reflects the winning side only`() {
        assertEquals("달콤", TasteTypeCopy.poleLabel(Trait.Sweetness, TraitAnswer.High))
        assertEquals("드라이", TasteTypeCopy.poleLabel(Trait.Sweetness, TraitAnswer.Low))
        assertEquals("스모키함", TasteTypeCopy.poleLabel(Trait.Peat, TraitAnswer.High))
    }
}
