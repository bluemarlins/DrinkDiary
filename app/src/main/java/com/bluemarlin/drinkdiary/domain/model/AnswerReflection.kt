package com.bluemarlin.drinkdiary.domain.model

// 판정 전 구간에서 되비칠 것 — **답한 내용이지 취향이 아니다**(prd.md F3-3 (d)).
//
// 이 구분이 이 파일의 존재 이유다. 우리 판정은 축값과 만족도의 **상관**이지 답의 빈도가
// 아니다. "묵직한 쪽으로 답한 잔이 많다"와 "묵직한 것을 좋아한다"는 다른 말이고, 전자를
// 후자로 부르는 순간 화면이 근거라고 내놓는 숫자가 실제 판정 근거가 아니게 된다
// (리서치의 P1 [충돌] — "달콤함 75%" 제안을 걸렀던 것과 같은 오류).
//
// 그러면 왜 보여주는가. 이 구간의 화면이 진행 게이지 하나뿐이라 첫 줄이 "당신의 데이터로는
// 아직 아무 말도 못 한다"가 되기 때문이다. 중립을 결함처럼 말하지 않는다는 원칙이
// (branding.md 4-5) 표본 부족 구간에도 적용된다.
data class AnswerReflection(
    val leanings: List<TraitLeaning>,
) {
    val isEmpty: Boolean get() = leanings.isEmpty()

    companion object {
        val Empty = AnswerReflection(emptyList())
    }
}

data class TraitLeaning(
    val trait: Trait,
    // 답이 몰린 쪽. Mid는 방향이 아니므로 여기 오지 않는다.
    val direction: TraitAnswer,
)
