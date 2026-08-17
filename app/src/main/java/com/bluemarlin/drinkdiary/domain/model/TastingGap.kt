package com.bluemarlin.drinkdiary.domain.model

// 한쪽만 쌓이고 반대쪽은 비어 있는 자리(prd.md F3-3 (b)).
//
// **추천이 아니라 공백 안내다.** 추천은 남의 평점이 있어야 성립하지만, 공백은 내 기록만으로
// 안다 — 무엇이 좋다고 말하지 않고 무엇이 **없다**고만 말한다.
//
// 이것은 4-2의 IMPORTANT("한쪽 방향 표본만 있으면 비교가 불가능해 판단 유보가 된다")를
// 사용자 언어로 옮긴 것이다. 다양성 부족은 우리 알고리즘의 사정이지만, 그 결과를 겪는 것은
// "왜 아직 아무것도 안 나오지"라고 묻는 사용자다.
data class TastingGap(
    val category: TagCategory,
    // 이미 쌓인 쪽. 표본이 임계 이상이라 한쪽으로 쏠렸다고 말할 수 있다.
    val recordedValue: String,
    val recordedSamples: Int,
    // 아직 한 잔도 없는 값.
    val missingValue: String,
)
