package com.bluemarlin.drinkdiary.domain.model

data class TraitPreference(
    val trait: Trait,
    // 판정 결과. null 은 "아직 판단할 표본이 없다"이며 Neutral("취향이 없다")과 다르다.
    val preference: TastePreference?,
    // 그 축에 답이 달린 기록 수.
    val samples: Int,
    // |상관|. 확신도를 그대로 보여줄 수 있게 남긴다 — 이전 이진 관문에는 없던 값이다.
    val strength: Double,
    // '보통'으로 답한 수. 압도적으로 크면 그 축은 입문자가 지각하지 못하는 축이라는 신호다
    // (Unsure를 없애면서 잃은 진단을 이걸로 갈음한다 — prd.md F2).
    val midSamples: Int,
) {
    val evaluated: Boolean get() = preference != null

    val midRatio: Double get() = if (samples == 0) 0.0 else midSamples.toDouble() / samples
}

data class TasteProfile(
    val scope: TypeScope,
    val recordCount: Int,
    val preferences: List<TraitPreference>,
) {
    val type: TasteType?
        get() =
            TasteType.from(
                preferences.filter { it.trait.shared }.associate { it.trait to it.preference },
            )

    fun preference(trait: Trait): TraitPreference? = preferences.firstOrNull { it.trait == trait }
}

sealed interface ProfileReadiness {
    // 유형이 나왔다. 중립 축이 섞여 있어도 유형이다.
    data class Ready(
        val type: TasteType,
    ) : ProfileReadiness

    // 아직 판단할 표본이 없다. Partial 상태는 두지 않는다 — 기본 경로가 공통 축 4개를 매번
    // 함께 묻기 때문에 축별 표본 수가 사실상 같고, "일부만 표본이 찬" 상태가 생기지 않는다.
    data class NotReady(
        val recordsNeeded: Int,
    ) : ProfileReadiness
}
