package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.usecase.TasteThresholds

// 화면에 "왜 아직 판정되지 않았는지"를 정직하게 보여주기 위한 분류.
// 표본 부족과 차이 불명확은 사용자가 할 일이 다르다 — 전자는 "더 기록하기",
// 후자는 "그냥 그런 축일 수 있다".
enum class TraitStatus {
    Resolved,
    NeedsSamples,
    NeedsClearerGap,
    MostlyUnsure,
}

fun traitStatus(pref: TraitPreference): TraitStatus =
    when {
        pref.resolved -> TraitStatus.Resolved
        pref.unsureSamples > pref.highSamples + pref.lowSamples -> TraitStatus.MostlyUnsure
        pref.highSamples < TasteThresholds.MIN_SAMPLES_PER_SIDE ||
            pref.lowSamples < TasteThresholds.MIN_SAMPLES_PER_SIDE -> TraitStatus.NeedsSamples
        else -> TraitStatus.NeedsClearerGap
    }

// 표본 부족일 때 "적어도 몇 개는 더 필요한지"의 하한값.
// 실제로 필요한 개수는 다음 기록이 어느 방향으로 나올지에 따라 더 늘 수 있으므로 하한만 말한다.
fun minimumRecordsNeeded(pref: TraitPreference): Int {
    val needHigh = (TasteThresholds.MIN_SAMPLES_PER_SIDE - pref.highSamples).coerceAtLeast(0)
    val needLow = (TasteThresholds.MIN_SAMPLES_PER_SIDE - pref.lowSamples).coerceAtLeast(0)
    return maxOf(needHigh, needLow)
}
