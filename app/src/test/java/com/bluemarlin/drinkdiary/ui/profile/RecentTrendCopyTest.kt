package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.RecentTrend
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TraitShift
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentTrendCopyTest {
    private fun trend(
        shift: TraitShift? = null,
        recent: Double = 3.0,
        earlier: Double = 3.0,
    ) = RecentTrend(
        recentCount = 5,
        earlierCount = 4,
        recentAverageRating = recent,
        earlierAverageRating = earlier,
        shift = shift,
    )

    private fun allLines(t: RecentTrend) = RecentTrendCopy.lines(t) + RecentTrendCopy.caption(t)

    private val everyShift =
        Trait.shared.flatMap { trait ->
            listOf(TraitAnswer.Low, TraitAnswer.High).map { TraitShift(trait, it) }
        }

    // 이 카드가 취향 판정으로 읽히면 화면이 근거라고 내놓는 것과 실제 판정 근거가 갈라진다.
    // 유형 카드 **바로 아래**에 놓이므로 그 혼동은 가정이 아니라 예정된 일이다.
    @Test
    fun `no line ever claims a preference`() {
        val banned = listOf("좋아", "취향", "선호", "싫어", "맞으세요")

        (everyShift.map { trend(shift = it) } + trend())
            .flatMap { allLines(it) }
            .forEach { line ->
                banned.forEach { word ->
                    assertFalse("$line 에 '$word' 가 있다", line.contains(word))
                }
            }
    }

    // 축값 평균은 방향을 고르는 데만 쓴다. 숫자로 내보내면 그것이 판정 근거로 읽힌다.
    @Test
    fun `the only numbers shown are counts and ratings`() {
        val line = RecentTrendCopy.lines(trend(shift = TraitShift(Trait.Body, TraitAnswer.High), recent = 4.5))

        assertTrue(line.any { it.contains("4.5점") })
        // 0~2 척도의 축값이 화면에 나오는 일은 없어야 한다.
        assertFalse(line.any { it.contains("1.4") || it.contains("2.0") })
    }

    @Test
    fun `every sentence ends in 해요체`() {
        (everyShift.map { trend(shift = it) } + trend(recent = 5.0) + trend(recent = 1.0) + trend())
            .flatMap { allLines(it) }
            .forEach { line ->
                assertTrue("$line 가 해요체로 끝나지 않는다", line.endsWith("요."))
                assertFalse("$line 에 합니다체가 섞였다", line.contains("습니다") || line.contains("합니다"))
            }
    }

    // 조사를 조립하는 이상 축 이름이 바뀌면 "향의 세기은"이 화면에 뜬다.
    @Test
    fun `the topic particle follows the trait name`() {
        fun shiftLine(trait: Trait) = RecentTrendCopy.lines(trend(shift = TraitShift(trait, TraitAnswer.High))).first()

        assertTrue(shiftLine(Trait.Body).startsWith("무게감은"))
        assertTrue(shiftLine(Trait.Intensity).startsWith("향의 세기는"))
        assertTrue(shiftLine(Trait.Sweetness).startsWith("단맛은"))
        assertTrue(shiftLine(Trait.Aftertaste).startsWith("여운은"))
    }

    @Test
    fun `a rating that barely moved is called similar, not higher`() {
        assertTrue(RecentTrendCopy.lines(trend(recent = 3.2, earlier = 3.0))[1].contains("비슷해요"))
        assertTrue(RecentTrendCopy.lines(trend(recent = 3.6, earlier = 3.0))[1].contains("높아요"))
        assertTrue(RecentTrendCopy.lines(trend(recent = 2.4, earlier = 3.0))[1].contains("낮아요"))
    }

    @Test
    fun `no shift is stated plainly rather than hidden`() {
        assertTrue(RecentTrendCopy.lines(trend())[0].contains("크게 다르지 않아요"))
    }
}
