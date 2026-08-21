package com.bluemarlin.drinkdiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleDataTest {
    @Test
    fun `sample records cover 5 axes and 5 levels of taste`() {
        val wines = SampleData.wineRecords
        val whiskeys = SampleData.whiskeyRecords

        assertEquals(12, wines.size)
        assertEquals(10, whiskeys.size)
        assertEquals(22, SampleData.allRecords.size)

        // 와인 5축 (Sweetness, Acidity, Tannin, Body, Aftertaste) 전수 포함 확인
        wines.forEach { record ->
            assertEquals(DrinkType.Wine, record.type)
            val answers = record.taste.answers
            assertTrue(answers.containsKey(Trait.Sweetness))
            assertTrue(answers.containsKey(Trait.Acidity))
            assertTrue(answers.containsKey(Trait.Tannin))
            assertTrue(answers.containsKey(Trait.Body))
            assertTrue(answers.containsKey(Trait.Aftertaste))
            assertEquals(5, answers.size)
        }

        // 위스키 5축 (Sweetness, Body, Peat, AlcoholBurn, Aftertaste) 전수 포함 확인
        whiskeys.forEach { record ->
            assertEquals(DrinkType.Whiskey, record.type)
            val answers = record.taste.answers
            assertTrue(answers.containsKey(Trait.Sweetness))
            assertTrue(answers.containsKey(Trait.Body))
            assertTrue(answers.containsKey(Trait.Peat))
            assertTrue(answers.containsKey(Trait.AlcoholBurn))
            assertTrue(answers.containsKey(Trait.Aftertaste))
            assertEquals(5, answers.size)
        }
    }
}
