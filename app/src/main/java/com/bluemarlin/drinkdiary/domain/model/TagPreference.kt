package com.bluemarlin.drinkdiary.domain.model

data class TagValueRating(
    // 저장된 값 이름(예: "Peated"). 화면 문구는 UI가 붙인다.
    val value: String,
    val samples: Int,
    val averageRating: Double,
)

// 높게 준 쪽과 낮게 준 쪽. Vivino의 `What You Like / Dislike`와 같은 자리이고,
// 공식 문서가 밝힌 용례도 우리와 같다 — **매장에서 꺼내 어떤 스타일을 좋아하는지 확인하는 용도**
// (departments/researcher/dashboard-competitiveness-2026-08.md 1-3).
//
// 표본이 모자란 값은 여기 오지 않는다. 한 잔짜리 값이 "낮게 준 쪽"으로 뽑히면 우연이
// 결론처럼 보인다.
data class TagContrast(
    val higher: TagValueRating,
    val lower: TagValueRating,
)

data class TagPreference(
    val category: TagCategory,
    // 만족도 높은 순. 표본이 적은 값도 포함된다 — 감추면 사용자가 근거를 확인할 수 없다.
    val values: List<TagValueRating>,
    // 값들 사이의 차이가 말할 만한가. false 면 나열은 하되 선호라고 단정하지 않는다.
    val meaningfulGap: Boolean,
    // 차이가 말할 만할 때만 채워진다. 차이가 없는데 순위를 보여주면 없는 선호가
    // 있는 것처럼 읽힌다(prd.md F3-3 (c)).
    val contrast: TagContrast? = null,
) {
    val totalSamples: Int get() = values.sumOf { it.samples }

    val best: TagValueRating? get() = values.firstOrNull()
}
