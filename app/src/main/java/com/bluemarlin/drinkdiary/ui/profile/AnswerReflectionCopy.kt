package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.AnswerReflection
import com.bluemarlin.drinkdiary.ui.DrinkLabels

// 판정 전 구간의 문구(prd.md F3-3 (d)).
//
// **제목이 결핍을 말하지 않는다.** 구 문구 "아직 취향을 판단하기엔 일러요"는 첫 줄부터
// "당신의 데이터로는 아직 아무 말도 못 한다"였고, branding.md 4-5("중립을 결함처럼 말하지
// 않는다")와 2-3(심사 어휘 '판단'을 피한다)에 동시에 걸린다.
//
// **개수를 약속하지 않는다.** 구 문구 "N개만 더 남기면 유형이 나와요"는 prd.md 7절-2가
// 금지한 것이다. 임계치는 축마다 독립적으로 차므로 그 숫자는 대부분의 사용자에게 거짓말이
// 된다 — 6잔은 네 축을 모두 같은 방향으로 몰아 답한 최선의 경우였다. 남은 거리는 게이지가
// 말하고, 문장은 숫자를 대지 않는다.
object AnswerReflectionCopy {
    const val TITLE = "취향이 쌓이는 중이에요"

    fun description(
        recordCount: Int,
        reflection: AnswerReflection,
    ): String =
        when {
            recordCount == 0 -> "첫 기록을 남기면 여기서 취향이 보이기 시작해요."
            reflection.isEmpty -> "지금까지 ${recordCount}개를 기록했어요. 답이 더 모이면 여기가 채워져요."
            // 되비침을 취향이라고 부르지 않는 일을 이 한 문장이 한다.
            else -> "지금까지 ${recordCount}개를 기록했어요. 취향이라고 부르기엔 이르지만, 남기신 답은 이렇게 모였어요."
        }

    // 축 이름을 앞에 붙이지 않는다. 양극 이름이 이미 어느 축인지 말해 주고, 붙이면
    // "여운은 긴 여운 쪽으로"처럼 같은 말이 두 번 나온다.
    fun lines(reflection: AnswerReflection): List<String> =
        reflection.leanings.map { leaning ->
            "${DrinkLabels.answer(leaning.trait, leaning.direction)} 쪽으로 답하셨어요."
        }
}
