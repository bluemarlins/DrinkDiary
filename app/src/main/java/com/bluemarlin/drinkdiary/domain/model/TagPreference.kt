package com.bluemarlin.drinkdiary.domain.model

data class TagValueRating(
    // 저장된 값 이름(예: "Peated"). 화면 문구는 UI가 붙인다.
    val value: String,
    val samples: Int,
    val averageRating: Double,
)

data class TagPreference(
    val category: TagCategory,
    // 만족도 높은 순. 표본이 적은 값도 포함된다 — 감추면 사용자가 근거를 확인할 수 없다.
    val values: List<TagValueRating>,
    // 값들 사이의 차이가 말할 만한가. false 면 나열은 하되 선호라고 단정하지 않는다.
    val meaningfulGap: Boolean,
) {
    val totalSamples: Int get() = values.sumOf { it.samples }

    val best: TagValueRating? get() = values.firstOrNull()
}
