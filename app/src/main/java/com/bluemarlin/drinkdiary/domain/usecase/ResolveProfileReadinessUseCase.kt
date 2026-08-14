package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TasteType

class ResolveProfileReadinessUseCase {
    operator fun invoke(profile: TasteProfile): ProfileReadiness {
        val shared = profile.preferences.filter { it.trait.shared }

        val type =
            TasteType.from(shared.associate { it.trait to it.preference })
        if (type != null) return ProfileReadiness.Ready(type)

        // 표본이 가장 적은 축을 기준으로 남은 거리를 말한다. 기본 경로가 공통 축 4개를 매번
        // 함께 묻기 때문에 이 값은 사실상 축 전체에 대한 답이다.
        val fewest = shared.minOfOrNull { it.samples } ?: 0
        return ProfileReadiness.NotReady(
            recordsNeeded = (TasteThresholds.MIN_SAMPLES - fewest).coerceAtLeast(1),
        )
    }
}
