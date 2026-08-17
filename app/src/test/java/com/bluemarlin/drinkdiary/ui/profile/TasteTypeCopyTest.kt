package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.TasteType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TasteTypeCopyTest {
    private val H = TastePreference.High
    private val L = TastePreference.Low
    private val N = TastePreference.Neutral

    @Test
    fun `DFRE reads as a full sentence`() {
        val type = TasteType(L, H, H, H)

        assertEquals("DFRE", type.code)
        assertEquals("드라이한 묵직한 취향", TasteTypeCopy.shortName(type))
        assertEquals("드라이하고 묵직하며 향이 진하고 여운이 길어요.", TasteTypeCopy.sentence(type))
    }

    @Test
    fun `SLMQ reads as a full sentence`() {
        val type = TasteType(H, L, L, L)

        assertEquals("SLMQ", type.code)
        assertEquals("달콤한 가벼운 취향", TasteTypeCopy.shortName(type))
        assertEquals("달콤하고 가벼우며 향이 은은하고 산뜻하게 끝나요.", TasteTypeCopy.sentence(type))
    }

    // 중립 축이 있어도 문장이 끝나야 한다. 마지막 방향 축이 종결형으로 닫히는지 본다.
    @Test
    fun `a neutral axis is named at the end, not treated as missing`() {
        val type = TasteType(L, H, N, H)

        assertEquals("DFXE", type.code)
        assertEquals("드라이한 묵직한 취향", TasteTypeCopy.shortName(type))
        assertEquals(
            "드라이하고 묵직하며 여운이 길어요. 향의 세기는 크게 가리지 않으세요.",
            TasteTypeCopy.sentence(type),
        )
    }

    @Test
    fun `a single direction still forms a sentence`() {
        val type = TasteType(H, N, N, N)

        assertEquals("SXXX", type.code)
        assertEquals("달콤한 취향", TasteTypeCopy.shortName(type))
        assertEquals(
            "달콤해요. 무게감과 향의 세기와 여운은 크게 가리지 않으세요.",
            TasteTypeCopy.sentence(type),
        )
    }

    @Test
    fun `all neutral is phrased as a preference, not an absence`() {
        val type = TasteType(N, N, N, N)

        assertEquals("XXXX", type.code)
        assertEquals("고루 즐기는 취향", TasteTypeCopy.shortName(type))
        assertEquals("어떤 스타일이든 두루 즐기시네요.", TasteTypeCopy.sentence(type))
    }

    // 중립을 결핍처럼 말하지 않는다 — branding.md 4-5절.
    @Test
    fun `neutral copy never says the user lacks something`() {
        val all = TastePreference.entries
        val phrases =
            all.flatMap { s ->
                all.flatMap { b ->
                    all.flatMap { i ->
                        all.map { a -> TasteTypeCopy.sentence(TasteType(s, b, i, a)) }
                    }
                }
            }

        assertEquals(81, phrases.size)
        phrases.forEach { phrase ->
            assertFalse(phrase, phrase.contains("아직"))
            assertFalse(phrase, phrase.contains("없"))
            assertFalse(phrase, phrase.contains("부족"))
            assertTrue(phrase, phrase.endsWith("."))
        }
    }

    // 종결은 해요체다(branding.md 2-3절). 여기가 어미가 실제로 갈라졌던 자리다 —
    // `ending()`만 합니다체여서 "향이 진합니다. …가리지 않으세요."처럼 한 문장 안에서 바뀌었다.
    // 81가지를 전부 보므로 어느 축 조합으로 되돌아와도 걸린다.
    @Test
    fun `every taste sentence stays in 해요체`() {
        val all = TastePreference.entries
        all.forEach { s ->
            all.forEach { b ->
                all.forEach { i ->
                    all.forEach { a ->
                        val phrase = TasteTypeCopy.sentence(TasteType(s, b, i, a))
                        assertFalse(phrase, phrase.contains("니다"))
                    }
                }
            }
        }
    }

    // 조사를 조립하는 이상 축 이름이 바뀌어도 "여운는"이 나오면 안 된다.
    @Test
    fun `korean particles agree with the preceding noun`() {
        assertEquals("단맛은", Josa.topic("단맛"))
        assertEquals("향의 세기는", Josa.topic("향의 세기"))
        assertEquals("여운은", Josa.topic("여운"))
        assertEquals("무게감과", Josa.and("무게감"))
        assertEquals("향의 세기와", Josa.and("향의 세기"))
    }
}
