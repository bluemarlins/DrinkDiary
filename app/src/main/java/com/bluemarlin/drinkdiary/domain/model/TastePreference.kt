package com.bluemarlin.drinkdiary.domain.model

// 축 하나에 대한 판정 결과.
//
// Neutral은 결핍이 아니라 결론이다 — "이 축은 만족도와 상관이 없다". 이걸 1급으로 인정하지
// 않으면, 축마다 1/3 확률로 취향이 없다고 할 때 네 축 모두 방향이 있을 확률은 (2/3)^4 = 19.8%뿐이라
// 사용자의 80%가 영원히 유형을 못 본다. 그 압력이 판정기로 하여금 없는 취향을 지어내게 만든다
// (구 알고리즘은 기록 40잔 기준 취향 없는 축의 61%에 방향을 매겼다).
//
// "표본이 부족하다"는 이 열거형으로 표현하지 않는다. 그건 판정 이전의 상태라 null로 둔다 —
// TraitPreference.preference 참고.
enum class TastePreference {
    Low,
    Neutral,
    High,
}
