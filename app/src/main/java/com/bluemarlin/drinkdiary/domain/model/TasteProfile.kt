package com.bluemarlin.drinkdiary.domain.model

data class TraitPreference(
    val trait: Trait,
    // 판정된 선호 방향. 판정 불가면 null.
    val direction: TraitAnswer?,
    val highSamples: Int,
    val lowSamples: Int,
    val unsureSamples: Int,
) {
    val resolved: Boolean get() = direction != null

    // 그 축을 지각하지 못한 비율. 높으면 질문이나 축 자체를 의심해야 한다.
    val unsureRatio: Double
        get() {
            val total = highSamples + lowSamples + unsureSamples
            return if (total == 0) 0.0 else unsureSamples.toDouble() / total
        }
}

data class TasteProfile(
    val scope: TypeScope,
    val recordCount: Int,
    val preferences: List<TraitPreference>,
) {
    val type: TasteType?
        get() =
            TasteType.from(
                preferences
                    .filter { it.trait.shared }
                    .mapNotNull { p ->
                        p.direction?.let { p.trait to it }
                    }.toMap(),
            )

    fun preference(trait: Trait): TraitPreference? = preferences.firstOrNull { it.trait == trait }
}

sealed interface ProfileReadiness {
    // 유형이 나왔다.
    data class Ready(
        val type: TasteType,
    ) : ProfileReadiness

    // 일부 축은 판정됐으나 유형은 아직 성립하지 않는다.
    data class Partial(
        val unresolved: List<Trait>,
        val blockedByUnsure: List<Trait>,
    ) : ProfileReadiness

    // 판정된 축이 하나도 없다.
    data object NotReady : ProfileReadiness
}
