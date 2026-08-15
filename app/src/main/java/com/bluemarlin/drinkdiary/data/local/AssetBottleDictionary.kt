package com.bluemarlin.drinkdiary.data.local

import android.content.Context
import com.bluemarlin.drinkdiary.domain.model.BottleEntry
import com.bluemarlin.drinkdiary.domain.model.BottleFacts
import com.bluemarlin.drinkdiary.domain.model.CaskGroup
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.WineStyle
import org.json.JSONArray

// 사전을 assets의 JSON에서 읽는다.
//
// 서드파티 파서를 넣지 않는다 — 플랫폼 org.json 으로 충분하다(harness.md §10).
// 파싱은 첫 조회 때 한 번만 하고 메모리에 둔다. 100종 규모라 부담이 없고,
// 사전은 앱 수명 동안 바뀌지 않는다.
class AssetBottleDictionary(
    private val context: Context,
    private val assetName: String = "bottles.json",
) {
    val entries: List<BottleEntry> by lazy { runCatching(::parse).getOrElse { emptyList() } }

    private fun parse(): List<BottleEntry> {
        val json = context.assets.open(assetName).use { it.readBytes().decodeToString() }
        val array = JSONArray(json)

        return (0 until array.length()).mapNotNull { index ->
            val row = array.getJSONObject(index)
            val type = enumOrNull<DrinkType>(row.optString("type")) ?: return@mapNotNull null
            val name = row.optString("name").takeIf { it.isNotBlank() } ?: return@mapNotNull null

            val aliasArray = row.optJSONArray("aliases")
            val aliases =
                buildSet {
                    add(name)
                    if (aliasArray != null) {
                        for (i in 0 until aliasArray.length()) {
                            aliasArray.optString(i).takeIf { it.isNotBlank() }?.let(::add)
                        }
                    }
                }

            val facts =
                BottleFacts(
                    // 모르는 값은 조용히 버린다. 사전은 계속 고쳐질 것이고,
                    // 값 하나가 낯설다고 항목 전체를 못 읽으면 안 된다.
                    cask = enumOrNull<CaskGroup>(row.optString("cask")),
                    wineStyle = enumOrNull<WineStyle>(row.optString("wineStyle")),
                )

            // 아는 게 하나도 없는 항목은 넣지 않는다 — 매칭돼봐야 말할 것이 없다.
            if (facts.cask == null && facts.wineStyle == null) return@mapNotNull null

            BottleEntry(type = type, displayName = name, aliases = aliases, facts = facts)
        }
    }

    private inline fun <reified T : Enum<T>> enumOrNull(raw: String?): T? =
        raw?.takeIf { it.isNotBlank() }?.let { name -> enumValues<T>().find { it.name == name } }
}
