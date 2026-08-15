package com.bluemarlin.drinkdiary.ui.record

import com.bluemarlin.drinkdiary.domain.model.TagCategory

// 기본 경로 밖으로 끌어올릴 수 있는 태그. **첫 기록 직후의 물음과 설정 화면이 같은 목록을 본다** —
// 두 곳이 갈라지면 프롬프트에서 켠 항목을 설정에서 끌 수 없는 상태가 생긴다.
//
// 셋뿐인 이유: 라벨만 보고 답할 수 있어야 하고(캐스크·품종은 사전이 채운다), 첫 화면이 이미 물은
// 것(주종별 분류)은 여기 없어야 한다.
val promotableTags: List<Pair<TagCategory, String>> =
    listOf(
        TagCategory.Peat to "스모키한지 — 위스키를 가장 크게 나누는 기준이에요",
        TagCategory.AbvBand to "라벨에 늘 적혀 있어요",
        TagCategory.Origin to "구대륙 / 신대륙",
    )
