package com.bluemarlin.drinkdiary.domain.model

// 대시보드가 **읽지 않아도 보이게** 하는 층(prd.md F3-4 (a)).
//
// 담는 것은 기록에서 곧바로 나오는 사실뿐이다 — 계산도 판정도 없다. 그래서 유형 아래에
// 놓여도 판정 근거로 오독될 여지가 없다.
//
// **랭킹이 아니다.** 남과 비교하지 않고 순위표를 만들지 않는다(branding.md 2-1의 배지·랭킹 금지).
// "내 기록 중 이것"이지 "1위"가 아니다.
enum class HighlightKind {
    TopRated,
    MostRepeated,
    Latest,
}

data class DrinkHighlight(
    val kind: HighlightKind,
    val record: DrinkRecord,
    // 같은 이름으로 몇 번 기록했는지. MostRepeated에서만 1보다 크다.
    val repeatCount: Int = 1,
)
