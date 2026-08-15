package com.bluemarlin.drinkdiary.domain.model

// 앱이 미리 아는 술의 사실. 사용자가 입력하지 않는다.
//
// 이 둘이 사전 몫인 이유는 취향 연결이 약해서가 아니라 정반대다 — **가장 강한데 라벨에서
// 읽히지 않는다.** 캐스크는 법적 표기 의무가 아니고, 구세계 와인은 품종 대신 지역명만 적는다
// (우리 데이터로 와인의 62%). departments/planner/structural-attributes-2026-08.md
//
// 값은 원본이 아니라 **그룹**이다. 품종 원본 25값은 20잔 기록에서 값당 0.8건이라 판정이 불가능하고,
// 캐스크 원본 표기도 "First Fill Oloroso Sherry Seasoned European Oak"처럼 흩어진다.
data class BottleFacts(
    val cask: CaskGroup? = null,
    val wineStyle: WineStyle? = null,
)

enum class CaskGroup { Bourbon, Sherry, VirginOak, Mixed }

enum class WineStyle { LightRed, FullRed, DryWhite, AromaticWhite }

// 사전 한 항목. 이름으로 찾으므로 별칭이 핵심이다 —
// 사용자는 "발베니 12", "발베니 12년", "Balvenie 12" 중 무엇으로도 적을 수 있다.
data class BottleEntry(
    val type: DrinkType,
    val displayName: String,
    val aliases: Set<String>,
    val facts: BottleFacts,
)
