package com.bluemarlin.drinkdiary.ui

import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone

class DrinkLabelsTest {
    @Test
    fun `a judged preference reads as the winning side`() {
        assertEquals("달콤", DrinkLabels.preference(Trait.Sweetness, TastePreference.High))
        assertEquals("드라이", DrinkLabels.preference(Trait.Sweetness, TastePreference.Low))
        assertEquals("스모키함", DrinkLabels.preference(Trait.Peat, TastePreference.High))
    }

    // '보통'은 축의 어느 쪽도 아니다. 한쪽으로 표시되면 사용자가 하지 않은 답을 한 것으로 만든다.
    @Test
    fun `mid never renders as a direction`() {
        Trait.entries.forEach { trait ->
            val mid = DrinkLabels.answer(trait, TraitAnswer.Mid)
            assertEquals("보통", mid)
            assertNotEquals(DrinkLabels.answer(trait, TraitAnswer.High), mid)
            assertNotEquals(DrinkLabels.answer(trait, TraitAnswer.Low), mid)
        }
    }

    @Test
    fun `neutral preference never borrows a pole label`() {
        Trait.entries.forEach { trait ->
            val neutral = DrinkLabels.preference(trait, TastePreference.Neutral)
            assertNotEquals(DrinkLabels.preference(trait, TastePreference.High), neutral)
            assertNotEquals(DrinkLabels.preference(trait, TastePreference.Low), neutral)
        }
    }

    @Test
    fun `every trait has a distinct label`() {
        val labels = Trait.entries.map { DrinkLabels.trait(it) }
        assertEquals(labels.size, labels.toSet().size)
    }

    // 자정 직후에 남긴 기록은 UTC로 읽으면 전날로 밀린다. 사용자가 "어젯밤" 마신 것이
    // 목록에서 하루 어긋나 보이는 실제 증상이라 시간대를 고정해 확인한다.
    @Test
    fun `date renders in the device timezone, not UTC`() {
        val seoul = ZoneId.of("Asia/Seoul")
        val justAfterMidnight =
            ZonedDateTime.of(2026, 8, 14, 0, 30, 0, 0, seoul).toInstant().toEpochMilli()

        val previous = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(seoul))
            assertEquals("2026.08.14", DrinkLabels.date(justAfterMidnight))

            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            assertEquals("2026.08.13", DrinkLabels.date(justAfterMidnight))
        } finally {
            TimeZone.setDefault(previous)
        }
    }

    @Test
    fun `price is grouped and suffixed`() {
        assertEquals("45,000원", DrinkLabels.price(45_000L))
        assertEquals("0원", DrinkLabels.price(0L))
    }
}
