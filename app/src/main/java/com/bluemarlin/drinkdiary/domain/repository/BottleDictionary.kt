package com.bluemarlin.drinkdiary.domain.repository

import com.bluemarlin.drinkdiary.domain.model.BottleEntry
import com.bluemarlin.drinkdiary.domain.model.BottleFacts
import com.bluemarlin.drinkdiary.domain.model.DrinkType

interface BottleDictionary {
    // 못 찾으면 null. 못 찾았다는 사실을 화면에 표시하지 않는다(사용자 결정, 2026-08-14) —
    // 사전에 없는 술마다 "모름"이 뜨면 화면이 결측 표시로 덮인다.
    fun lookup(
        type: DrinkType,
        name: String,
    ): BottleFacts?
}

// 이름으로 찾는다. 기록에 사전 id를 저장하지 않으므로 사전을 고치면 **과거 기록에도 바로 반영된다.**
// 사용자가 이름을 고치면 매칭도 따라 바뀌는데, 그것이 맞는 동작이다.
class BottleMatcher(
    private val entriesProvider: () -> List<BottleEntry>,
) : BottleDictionary {
    override fun lookup(
        type: DrinkType,
        name: String,
    ): BottleFacts? {
        val needle = normalize(name)
        if (needle.isEmpty()) return null

        // 여러 개가 걸리면 가장 긴 별칭이 이긴다 — "발베니12"와 "발베니12더블우드"가 함께
        // 걸릴 때 더 구체적인 쪽을 골라야 한다.
        return entriesProvider()
            .asSequence()
            .filter { it.type == type }
            .mapNotNull { entry ->
                entry.aliases
                    .map(::normalize)
                    .filter { it.isNotEmpty() && needle.contains(it) }
                    .maxByOrNull { it.length }
                    ?.let { it.length to entry }
            }.maxByOrNull { it.first }
            ?.second
            ?.facts
    }

    companion object {
        // "발베니 12년", "Balvenie 12", "발베니12" 가 모두 같은 것으로 읽혀야 한다.
        // 반대로 "아드벡"만 적으면 어느 아드벡인지 알 수 없으므로 매칭되지 않는다 —
        // 별칭이 입력의 부분 문자열이어야 하기 때문이다.
        fun normalize(raw: String): String =
            raw
                .lowercase()
                .replace("년산", "")
                .replace("년", "")
                .filter { it.isLetterOrDigit() }
    }
}
