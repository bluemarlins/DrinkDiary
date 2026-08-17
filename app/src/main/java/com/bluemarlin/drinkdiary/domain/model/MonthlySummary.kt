package com.bluemarlin.drinkdiary.domain.model

// 이번 달에 무엇을 마셨는지의 요약. **취향 판정이 아니다** — 판정은 상관에서 나오고
// 이쪽은 단순 집계다. 둘을 한 화면에 두되 섞지 않는 이유는 prd.md F3이 요약을
// "사용자 자신의 만족도에서만" 도출하라고 정하기 때문이다. 집계는 근거가 아니라 회고다.
data class MonthlySummary(
    val total: Int,
    val byType: List<TypeCount>,
    // 기록이 없으면 평균도 없다. 0.0으로 채우면 "0점을 줬다"로 읽힌다.
    val averageRating: Double?,
    val repurchaseCount: Int,
    // 이번 달 가장 높게 준 한 잔. 동점이면 최근 것.
    val topRecord: DrinkRecord?,
) {
    val isEmpty: Boolean get() = total == 0

    data class TypeCount(
        val type: DrinkType,
        val count: Int,
    )

    companion object {
        val Empty =
            MonthlySummary(
                total = 0,
                byType = emptyList(),
                averageRating = null,
                repurchaseCount = 0,
                topRecord = null,
            )
    }
}
