package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.AnswerReflection
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TraitLeaning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerReflectionCopyTest {
    private val everyLeaning =
        Trait.shared.flatMap { trait ->
            listOf(TraitAnswer.Low, TraitAnswer.High).map { TraitLeaning(trait, it) }
        }

    private val full = AnswerReflection(everyLeaning.filter { it.direction == TraitAnswer.High })

    private fun allCopy(): List<String> =
        AnswerReflectionCopy.lines(full) +
            everyLeaning.flatMap { AnswerReflectionCopy.lines(AnswerReflection(listOf(it))) } +
            listOf(
                AnswerReflectionCopy.TITLE,
                AnswerReflectionCopy.description(0, AnswerReflection.Empty),
                AnswerReflectionCopy.description(3, AnswerReflection.Empty),
                AnswerReflectionCopy.description(3, full),
            )

    // 이 구간의 첫 줄이 "당신의 데이터로는 아직 아무 말도 못 한다"가 되면 안 된다.
    // 심사 어휘('판단'·'판정')도 쓰지 않는다 — branding.md 2-3 · 4-5.
    @Test
    fun `nothing here declares a deficiency or judges the user`() {
        val banned = listOf("판단", "판정", "부족", "일러요", "아직 취향이 없")

        allCopy().forEach { text ->
            banned.forEach { word ->
                assertFalse("$text 에 '$word' 가 있다", text.contains(word))
            }
        }
    }

    // 되비침을 취향이라고 부르지 않는다. 우리 판정은 답의 빈도가 아니라 상관이다.
    @Test
    fun `a leaning is never called a preference`() {
        everyLeaning.forEach { leaning ->
            val line = AnswerReflectionCopy.lines(AnswerReflection(listOf(leaning))).single()

            assertTrue("$line 가 '답하셨어요'로 끝나지 않는다", line.endsWith("쪽으로 답하셨어요."))
            listOf("좋아", "선호", "취향").forEach { word ->
                assertFalse("$line 에 '$word' 가 있다", line.contains(word))
            }
        }
    }

    // prd.md 7절-2. "N개만 더 남기면 유형이 나와요"는 임계치가 축마다 독립적으로 차므로
    // 대부분의 사용자에게 거짓말이 된다.
    @Test
    fun `no copy promises a number of records`() {
        listOf(
            AnswerReflectionCopy.description(0, AnswerReflection.Empty),
            AnswerReflectionCopy.description(1, AnswerReflection.Empty),
            AnswerReflectionCopy.description(5, full),
        ).forEach { text ->
            listOf("개만 더", "잔만 더", "더 남기면", "더 기록하면").forEach { promise ->
                assertFalse("$text 가 개수를 약속한다", text.contains(promise))
            }
        }
    }

    // 축 이름을 앞에 붙이면 "여운은 긴 여운 쪽으로"처럼 같은 말이 두 번 나온다.
    @Test
    fun `the line does not repeat the trait name`() {
        val line =
            AnswerReflectionCopy
                .lines(AnswerReflection(listOf(TraitLeaning(Trait.Aftertaste, TraitAnswer.High))))
                .single()

        assertEquals("긴 여운 쪽으로 답하셨어요.", line)
    }

    @Test
    fun `an empty reflection has no lines but still has something to say`() {
        assertTrue(AnswerReflectionCopy.lines(AnswerReflection.Empty).isEmpty())
        assertTrue(AnswerReflectionCopy.description(0, AnswerReflection.Empty).endsWith("요."))
        assertTrue(AnswerReflectionCopy.description(3, AnswerReflection.Empty).endsWith("요."))
    }
}
