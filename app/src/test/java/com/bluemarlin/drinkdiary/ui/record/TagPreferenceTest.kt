package com.bluemarlin.drinkdiary.ui.record

import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TagPreferenceTest {
    private fun split(
        type: DrinkType,
        chosen: Set<TagCategory>,
    ) = remainingTags(type).partition { it in chosen }

    @Test
    fun `choosing nothing leaves everything folded away`() {
        val (always, folded) = split(DrinkType.Whiskey, emptySet())

        assertTrue("기본값은 아무것도 승격하지 않는다", always.isEmpty())
        assertEquals(remainingTags(DrinkType.Whiskey), folded)
    }

    @Test
    fun `a chosen tag moves out of the fold and is not duplicated`() {
        val (always, folded) = split(DrinkType.Whiskey, setOf(TagCategory.Peat))

        assertEquals(listOf(TagCategory.Peat), always)
        assertTrue("승격된 태그가 접힌 쪽에 남으면 두 번 보인다", TagCategory.Peat !in folded)
        assertEquals(remainingTags(DrinkType.Whiskey).size, always.size + folded.size)
    }

    // 위스키에서 피트를 골라도 와인 기록에는 없는 항목이다.
    @Test
    fun `a choice never adds a tag the drink does not have`() {
        val (always, _) = split(DrinkType.Wine, setOf(TagCategory.Peat, TagCategory.Origin))

        assertEquals(listOf(TagCategory.Origin), always)
    }

    // 첫 화면이 물은 것은 어떤 선택으로도 되살아나지 않는다.
    @Test
    fun `promoted tags cannot be re-added by choosing them`() {
        DrinkType.entries.forEach { type ->
            val (always, folded) = split(type, TagCategory.entries.toSet())

            promotedTags(type).forEach { promoted ->
                assertTrue("$type: $promoted 를 두 번 묻는다", promoted !in always && promoted !in folded)
            }
        }
    }
}
