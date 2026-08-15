package com.bluemarlin.drinkdiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrinkTagsTest {
    @Test
    fun `each drink type is asked only what its label can answer`() {
        val whiskey = TagCategory.of(DrinkType.Whiskey)
        val wine = TagCategory.of(DrinkType.Wine)

        assertTrue(whiskey.contains(TagCategory.WhiskyStyle))
        assertTrue(whiskey.contains(TagCategory.Peat))
        assertFalse(whiskey.contains(TagCategory.WineColor))

        assertTrue(wine.contains(TagCategory.WineColor))
        assertFalse(wine.contains(TagCategory.Peat))

        // 도수와 산지는 둘 다 라벨에 있다.
        listOf(whiskey, wine).forEach {
            assertTrue(it.contains(TagCategory.AbvBand))
            assertTrue(it.contains(TagCategory.Origin))
        }
    }

    @Test
    fun `entries expose only the tags that were filled`() {
        val tags = DrinkTags(peat = PeatTag.Peated, abvBand = AbvBand.High)

        assertEquals(
            listOf(TagCategory.Peat, TagCategory.AbvBand),
            tags.entries.map { it.first },
        )
        assertEquals(PeatTag.Peated.name, tags[TagCategory.Peat])
        assertNull(tags[TagCategory.Origin])
    }

    @Test
    fun `empty tags are a valid record, not an error`() {
        assertTrue(DrinkTags().isEmpty)
        assertTrue(DrinkTags().entries.isEmpty())
    }

    @Test
    fun `stored values round-trip`() {
        val tags =
            DrinkTags(
                whiskyStyle = WhiskyStyle.Bourbon,
                peat = PeatTag.Unpeated,
                wineColor = WineColor.Red,
                abvBand = AbvBand.Mid,
                origin = Origin.NewWorld,
            )

        assertEquals(tags, DrinkTags.from(tags.entries.toMap()))
    }

    // 태그 집합은 아직 가설이라 값이 바뀔 수 있다. 하나가 사라졌다고 기록 전체를 못 읽으면 안 된다.
    @Test
    fun `an unknown stored value is dropped, not fatal`() {
        val tags =
            DrinkTags.from(
                mapOf(
                    TagCategory.Peat to "Smoky",
                    TagCategory.Origin to Origin.OldWorld.name,
                ),
            )

        assertNull(tags.peat)
        assertEquals(Origin.OldWorld, tags.origin)
    }
}
