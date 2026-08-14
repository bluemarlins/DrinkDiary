package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.usecase.TasteThresholds

// 화면이 세 상태를 구분해서 말하게 한다. 셋은 사용자가 할 일이 다르다.
enum class TraitStatus {
    // 방향이 나왔다.
    Resolved,

    // 취향이 없다는 결론. 결핍이 아니다 — 기록을 더 해도 바뀔 일이 아니라는 뜻에 가깝다.
    Neutral,

    // 아직 판단할 표본이 없다. 위 둘과 달리 기록을 더 하면 해소된다.
    NeedsRecords,
}

fun traitStatus(pref: TraitPreference): TraitStatus =
    when (pref.preference) {
        null -> TraitStatus.NeedsRecords
        TastePreference.Neutral -> TraitStatus.Neutral
        else -> TraitStatus.Resolved
    }

// 표본이 모자란 축에 몇 개가 더 필요한지. 정확한 수이지 하한이 아니다 —
// 상관 방식은 양쪽 표본을 따로 요구하지 않아서 "다음 기록이 어느 방향이냐"에 좌우되지 않는다.
fun recordsNeeded(pref: TraitPreference): Int = (TasteThresholds.MIN_SAMPLES - pref.samples).coerceAtLeast(0)
