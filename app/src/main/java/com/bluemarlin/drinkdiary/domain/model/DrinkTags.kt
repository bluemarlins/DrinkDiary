package com.bluemarlin.drinkdiary.domain.model

// 라벨을 보고 5초 안에 고를 수 있는 사실(PRD F2-1). 감각 축과 달리 지각이 필요 없고,
// 그래서 노이즈가 없다.
//
// 여기 있는 것과 없는 것의 기준은 두 가지다.
//  1) 라벨에서 읽히는가 — 캐스크는 법적 표기 의무가 아니고, 구세계 와인은 품종 대신 지역명만
//     적는다(우리 데이터로 와인의 62%). 그 둘은 태그가 아니라 내장 사전 소관이다.
//  2) 값이 적은가 — 20잔 기록 기준 값당 3건은 모여야 판정할 수 있다. 품종(25값)은 값당 0.8건이라
//     채움률 100%여도 표본이 안 된다.
// departments/planner/structural-attributes-2026-08.md

enum class TagCategory {
    WhiskyStyle,
    Peat,
    WineColor,
    AbvBand,
    Origin,
    ;

    // 그 주종에서 물어볼 태그인지. 도수·산지는 둘 다에 해당한다.
    fun appliesTo(type: DrinkType): Boolean =
        when (this) {
            WhiskyStyle, Peat -> type == DrinkType.Whiskey
            WineColor -> type == DrinkType.Wine
            AbvBand, Origin -> true
        }

    companion object {
        fun of(type: DrinkType): List<TagCategory> = entries.filter { it.appliesTo(type) }
    }
}

enum class WhiskyStyle { SingleMalt, Blended, Bourbon }

enum class PeatTag { Peated, Unpeated }

// 잔만 봐도 아는 값이라 "모름"이 없다. 그 외는 로제·주정강화처럼 실제로 존재하는 분류다.
enum class WineColor { Red, White, Sparkling, Other }

// 구간의 실제 경계는 주종마다 다르다(위스키 40%대 / 43~45 / 46+, 와인 12 이하 / 13~14 / 15+).
// 도메인은 구간의 순서만 알고, 경계 문구는 UI가 주종에 맞춰 붙인다.
enum class AbvBand { Low, Mid, High }

enum class Origin { OldWorld, NewWorld }

data class DrinkTags(
    val whiskyStyle: WhiskyStyle? = null,
    val peat: PeatTag? = null,
    val wineColor: WineColor? = null,
    val abvBand: AbvBand? = null,
    val origin: Origin? = null,
) {
    // 판정 알고리즘이 태그 종류를 몰라도 돌 수 있게 (카테고리, 값) 쌍으로 편다.
    val entries: List<Pair<TagCategory, String>>
        get() =
            listOfNotNull(
                whiskyStyle?.let { TagCategory.WhiskyStyle to it.name },
                peat?.let { TagCategory.Peat to it.name },
                wineColor?.let { TagCategory.WineColor to it.name },
                abvBand?.let { TagCategory.AbvBand to it.name },
                origin?.let { TagCategory.Origin to it.name },
            )

    operator fun get(category: TagCategory): String? = entries.firstOrNull { it.first == category }?.second

    val isEmpty: Boolean get() = entries.isEmpty()

    companion object {
        // 저장된 문자열에서 되돌린다. 모르는 값은 조용히 버린다 — 태그 집합은 아직 가설이라
        // 값이 바뀔 수 있고, 하나가 사라졌다고 기록 전체를 못 읽으면 안 된다.
        fun from(stored: Map<TagCategory, String>): DrinkTags =
            DrinkTags(
                whiskyStyle =
                    stored[TagCategory.WhiskyStyle]?.let { name ->
                        WhiskyStyle.entries.find { it.name == name }
                    },
                peat = stored[TagCategory.Peat]?.let { name -> PeatTag.entries.find { it.name == name } },
                wineColor = stored[TagCategory.WineColor]?.let { name -> WineColor.entries.find { it.name == name } },
                abvBand = stored[TagCategory.AbvBand]?.let { name -> AbvBand.entries.find { it.name == name } },
                origin = stored[TagCategory.Origin]?.let { name -> Origin.entries.find { it.name == name } },
            )
    }
}
