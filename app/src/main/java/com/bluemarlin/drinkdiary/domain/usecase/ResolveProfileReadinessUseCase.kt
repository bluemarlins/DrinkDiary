package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TasteType

class ResolveProfileReadinessUseCase {
    operator fun invoke(profile: TasteProfile): ProfileReadiness {
        val shared = profile.preferences.filter { it.trait.shared }

        val type =
            TasteType.from(
                shared.mapNotNull { pref -> pref.direction?.let { pref.trait to it } }.toMap(),
            )
        if (type != null) return ProfileReadiness.Ready(type)

        // 유형은 shared 축으로만 성립하므로 진행도도 shared 축으로 판단한다.
        if (shared.none { it.resolved }) return ProfileReadiness.NotReady

        val unresolved = shared.filterNot { it.resolved }
        return ProfileReadiness.Partial(
            unresolved = unresolved.map { it.trait },
            // 지각하지 못해서 막힌 축과 경험이 부족해서 막힌 축은 사용자가 할 일이 다르다.
            blockedByUnsure =
                unresolved
                    .filter { it.unsureSamples > it.highSamples + it.lowSamples }
                    .map { it.trait },
        )
    }
}
