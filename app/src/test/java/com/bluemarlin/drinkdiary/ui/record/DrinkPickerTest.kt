package com.bluemarlin.drinkdiary.ui.record

import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrinkPickerTest {
    // 첫 화면이 물은 것을 TagPicker가 또 물으면 사용자는 같은 질문을 두 번 받는다.
    @Test
    fun `what the first screen asks is not asked again`() {
        DrinkType.entries.forEach { type ->
            val promoted = promotedTags(type)
            val remaining = TagCategory.of(type).filterNot { it in promoted }

            assertTrue("승격된 태그가 있어야 한다", promoted.isNotEmpty())
            promoted.forEach { category ->
                assertFalse("$type: $category 를 두 번 묻는다", remaining.contains(category))
            }
        }
    }

    @Test
    fun `promoted tags belong to the drink that is asked`() {
        assertEquals(setOf(TagCategory.WineColor, TagCategory.Origin), promotedTags(DrinkType.Wine))
        assertEquals(setOf(TagCategory.WhiskyStyle, TagCategory.Origin), promotedTags(DrinkType.Whiskey))

        // 승격 대상은 그 주종에 해당하는 태그여야 한다.
        DrinkType.entries.forEach { type ->
            promotedTags(type).forEach { category ->
                assertTrue(category.appliesTo(type))
            }
        }
    }

    // 도수·피트는 여전히 선택이다.
    @Test
    fun `optional tags stay optional`() {
        val wine = TagCategory.of(DrinkType.Wine).filterNot { it in promotedTags(DrinkType.Wine) }
        val whiskey = TagCategory.of(DrinkType.Whiskey).filterNot { it in promotedTags(DrinkType.Whiskey) }

        assertEquals(listOf(TagCategory.AbvBand), wine)
        assertEquals(listOf(TagCategory.Peat, TagCategory.AbvBand), whiskey)
    }
}
