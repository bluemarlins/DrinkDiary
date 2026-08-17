package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.RecentTrend
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TraitShift
import org.junit.Assert.assertEquals
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

    private fun allSentences(t: RecentTrend) =
        listOf(RecentTrendCopy.caption(t), RecentTrendCopy.shiftLine(t), RecentTrendCopy.verdict(t))

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
            .flatMap { allSentences(it) }
            .forEach { line ->
                banned.forEach { word ->
                    assertFalse("$line 에 '$word' 가 있다", line.contains(word))
                }
            }
    }

    // 축값 평균은 방향을 고르는 데만 쓴다. 숫자로 내보내면 그것이 판정 근거로 읽힌다.
    @Test
    fun `the only numbers shown are counts and ratings`() {
        val t = trend(shift = TraitShift(Trait.Body, TraitAnswer.High), recent = 4.5)

        assertEquals("4.5점", RecentTrendCopy.recentBar(t).value)
        // 0~2 척도의 축값이 화면에 나오는 일은 없어야 한다.
        assertFalse(allSentences(t).any { it.contains("1.4") || it.contains("2.0") })
    }

    // 막대 길이는 5점 척도의 비율이다. 빈도가 아니다(prd.md F3-4 (b)).
    @Test
    fun `the bar length is the rating out of five`() {
        val t = trend(recent = 4.0, earlier = 2.5)

        assertEquals(0.8f, RecentTrendCopy.recentBar(t).fraction, 1e-6f)
        assertEquals(0.5f, RecentTrendCopy.earlierBar(t).fraction, 1e-6f)
    }

    @Test
    fun `every sentence ends in 해요체`() {
        (everyShift.map { trend(shift = it) } + trend(recent = 5.0) + trend(recent = 1.0) + trend())
            .flatMap { allSentences(it) }
            .forEach { line ->
                assertTrue("$line 가 해요체로 끝나지 않는다", line.endsWith("요."))
                assertFalse("$line 에 합니다체가 섞였다", line.contains("습니다") || line.contains("합니다"))
            }
    }

    // 축 이름을 앞에 붙이면 "여운은 그 이전보다 긴 여운에"처럼 같은 말이 두 번 나온다.
    @Test
    fun `the line does not repeat the trait name`() {
        fun shiftLine(trait: Trait) = RecentTrendCopy.shiftLine(trend(shift = TraitShift(trait, TraitAnswer.High)))

        assertEquals("그 이전보다 긴 여운에 가깝게 답하셨어요.", shiftLine(Trait.Aftertaste))
        assertEquals("그 이전보다 묵직함에 가깝게 답하셨어요.", shiftLine(Trait.Body))
    }

    // 임계 미만의 차이는 말로도 색으로도 차이라고 하지 않는다.
    @Test
    fun `a rating that barely moved is called similar, and no bar is emphasised`() {
        val flat = trend(recent = 3.2, earlier = 3.0)

        assertTrue(RecentTrendCopy.verdict(flat).contains("비슷하게"))
        assertFalse(RecentTrendCopy.recentBar(flat).emphasised)
        assertFalse(RecentTrendCopy.earlierBar(flat).emphasised)
    }

    // **강조색은 점수가 높은 쪽에 붙는다. '최근'에 붙는 것이 아니다.**
    // 라벨 절에서 초록이 "높게 준 쪽"이므로, 여기서 초록이 "최근"을 뜻하면 최근에 더 낮게
    // 준 사용자는 낮은 막대가 초록인 화면을 본다 — 에뮬레이터에서 실제로 그렇게 나왔다.
    @Test
    fun `the emphasis follows the higher score, not recency`() {
        val worse = trend(recent = 2.4, earlier = 4.4)
        assertTrue(RecentTrendCopy.verdict(worse).contains("낮게"))
        assertFalse(RecentTrendCopy.recentBar(worse).emphasised)
        assertTrue(RecentTrendCopy.earlierBar(worse).emphasised)

        val better = trend(recent = 4.4, earlier = 2.4)
        assertTrue(RecentTrendCopy.verdict(better).contains("높게"))
        assertTrue(RecentTrendCopy.recentBar(better).emphasised)
        assertFalse(RecentTrendCopy.earlierBar(better).emphasised)
    }

    @Test
    fun `no shift is stated plainly rather than hidden`() {
        assertTrue(RecentTrendCopy.shiftLine(trend()).contains("크게 다르지 않아요"))
    }
}
