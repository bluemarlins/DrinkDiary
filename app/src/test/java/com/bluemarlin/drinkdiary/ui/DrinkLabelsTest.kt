package com.bluemarlin.drinkdiary.ui

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
    fun `pole reflects the winning side only`() {
        assertEquals("달콤", DrinkLabels.pole(Trait.Sweetness, TraitAnswer.High))
        assertEquals("드라이", DrinkLabels.pole(Trait.Sweetness, TraitAnswer.Low))
        assertEquals("스모키함", DrinkLabels.pole(Trait.Peat, TraitAnswer.High))
    }

    // Unsure는 방향이 아니다. 축의 어느 한쪽으로 표시되면 사용자가 답하지 않은 것을
    // 답한 것으로 읽게 된다.
    @Test
    fun `unsure never renders as a direction`() {
        Trait.entries.forEach { trait ->
            val unsure = DrinkLabels.answer(trait, TraitAnswer.Unsure)
            assertEquals("잘 모르겠어요", unsure)
            assertNotEquals(DrinkLabels.pole(trait, TraitAnswer.High), unsure)
            assertNotEquals(DrinkLabels.pole(trait, TraitAnswer.Low), unsure)
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
