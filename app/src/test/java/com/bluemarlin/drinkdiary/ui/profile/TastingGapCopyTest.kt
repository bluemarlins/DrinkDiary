package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.CaskGroup
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TastingGap
import com.bluemarlin.drinkdiary.domain.model.gapCandidates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TastingGapCopyTest {
    private fun gap(
        category: TagCategory,
        recorded: String,
        missing: String,
        samples: Int = 3,
    ) = TastingGap(category, recorded, samples, missing)

    // 카테고리마다 값 한 쌍씩 — 문구가 어느 카테고리에서도 무너지지 않아야 한다.
    private val everyGap =
        TagCategory.entries.mapNotNull { category ->
            val values = category.gapCandidates
            if (values.size < 2) null else gap(category, values.first(), values.last())
        }

    @Test
    fun `the research example comes out verbatim`() {
        val sentence =
            TastingGapCopy.sentence(
                gap(TagCategory.Cask, CaskGroup.Sherry.name, CaskGroup.Bourbon.name),
                DrinkType.Whiskey,
            )

        assertEquals("셰리 캐스크는 3잔 마셨는데, 버번 캐스크는 아직 없어요.", sentence)
    }

    // 권유하는 순간 이것은 추천이 되고, 추천이라면 무엇을 근거로 권하느냐는 물음이 따라온다.
    // 우리에게는 남의 평점이 없고, 있어도 쓰지 않는다.
    @Test
    fun `nothing here recommends, urges, or scolds`() {
        val banned = listOf("추천", "드셔보", "마셔보세요", "어떠세요", "고민", "지뢰", "좋아", "해보세요")

        everyGap.forEach { g ->
            listOf(DrinkType.Whiskey, DrinkType.Wine, null).forEach { type ->
                val sentence = TastingGapCopy.sentence(g, type)
                banned.forEach { word ->
                    assertFalse("$sentence 에 '$word' 가 있다", sentence.contains(word))
                }
            }
        }
    }

    // 비어 있는 쪽에 "0잔"을 붙이지 않는다. 0은 성적이 아니라 아직 안 한 일이다.
    @Test
    fun `the empty side never shows a zero`() {
        everyGap.forEach { g ->
            assertFalse(TastingGapCopy.sentence(g, DrinkType.Whiskey).contains("0잔"))
        }
    }

    @Test
    fun `every sentence ends in 해요체`() {
        everyGap.forEach { g ->
            val sentence = TastingGapCopy.sentence(g, DrinkType.Whiskey)
            assertTrue("$sentence 가 해요체로 끝나지 않는다", sentence.endsWith("아직 없어요."))
        }
    }
}
