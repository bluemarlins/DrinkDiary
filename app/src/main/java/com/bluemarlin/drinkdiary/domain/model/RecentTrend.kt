package com.bluemarlin.drinkdiary.domain.model

// 최근 몇 잔이 그 이전과 어떻게 달랐는지. **취향 판정이 아니다.**
//
// 유형은 잘 안 바뀌는 것이 결함이 아니라 설계다(software-architecture.md 4-1 —
// 취향이 매주 뒤집히면 신뢰가 무너진다). 그래서 유형을 흔드는 대신 그 아래에
// 매번 달라지는 층을 하나 얹는다. 이쪽이 말하는 것은 "무엇을 어떻게 답했는가"이지
// "무엇을 좋아하는가"가 아니다 — prd.md F3-3 (a).
data class RecentTrend(
    val recentCount: Int,
    val earlierCount: Int,
    val recentAverageRating: Double,
    val earlierAverageRating: Double,
    // 최근 들어 답이 한쪽으로 옮겨간 축. 없으면 null — 변화가 없는데 방향을 지어내지 않는다.
    val shift: TraitShift?,
) {
    val ratingDelta: Double get() = recentAverageRating - earlierAverageRating
}

data class TraitShift(
    val trait: Trait,
    // 최근에 기운 쪽. Mid는 방향이 아니므로 여기 오지 않는다.
    val direction: TraitAnswer,
)
