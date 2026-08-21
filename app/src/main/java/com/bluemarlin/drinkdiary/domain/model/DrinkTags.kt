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

enum class TagCategory(
    // 사용자가 입력하지 않고 내장 사전이 채우는 값. 사용자에게 묻지 않는다.
    val fromDictionary: Boolean = false,
) {
    WhiskyStyle,
    Peat,
    WineColor,
    AbvBand,
    Origin,
    Cask(fromDictionary = true),
    WineStyle(fromDictionary = true),
    ;

    // 그 주종에서 다룰 태그인지. 도수·산지는 둘 다에 해당한다.
    fun appliesTo(type: DrinkType): Boolean =
        when (this) {
            WhiskyStyle, Peat, Cask -> type == DrinkType.Whiskey
            WineColor, WineStyle -> type == DrinkType.Wine
            AbvBand, Origin -> true
        }

    companion object {
        // 사용자에게 물을 수 있는 것만. 사전이 채우는 값은 질문 대상이 아니다.
        fun of(type: DrinkType): List<TagCategory> = entries.filter { it.appliesTo(type) && !it.fromDictionary }
    }
}

// 아직 한 잔도 없는 값을 말하려면 그 카테고리가 가질 수 있는 값을 알아야 한다(prd.md F3-3 (b)).
//
// **뭉뚱그리는 값은 뺀다.** 캐스크의 "여러 캐스크"와 와인 색의 "그 외"는 마셔서 채울 수 있는
// 칸이 아니라 나머지를 담는 자루다. "그 외는 아직 없어요"는 사용자가 할 수 있는 일이 없는 말이다.
//
// enum 바깥에 두는 이유는 이름 충돌이다 — `TagCategory.WhiskyStyle`(카테고리)과
// `WhiskyStyle`(값)이 같은 이름이라 enum 안에서는 값 쪽을 가리킬 수 없다.
val TagCategory.gapCandidates: List<String>
    get() =
        when (this) {
            TagCategory.WhiskyStyle -> WhiskyStyle.entries.filter { it != WhiskyStyle.Other }.map { it.name }
            TagCategory.Peat -> PeatTag.entries.map { it.name }
            TagCategory.WineColor -> WineColor.entries.filter { it != WineColor.Other }.map { it.name }
            TagCategory.AbvBand -> AbvBand.entries.map { it.name }
            TagCategory.Origin -> Origin.entries.filter { it != Origin.Other }.map { it.name }
            TagCategory.Cask -> CaskGroup.entries.filter { it != CaskGroup.Mixed }.map { it.name }
            TagCategory.WineStyle -> WineStyle.entries.map { it.name }
        }

enum class WhiskyStyle { SingleMalt, BlendedMalt, Blended, Bourbon, Rye, Other }

enum class PeatTag { Peated, Unpeated }

// 와인 분류 (레드, 화이트, 스파클링, 내추럴, 포트/주정강화, 그 외: 로제/기타)
enum class WineColor { Red, White, Sparkling, Natural, Port, Other }

// 구간의 실제 경계는 주종마다 다르다(위스키 40%대 / 43~45 / 46+, 와인 12 이하 / 13~14 / 15+).
// 도메인은 구간의 순서만 알고, 경계 문구는 UI가 주종에 맞춰 붙인다.
enum class AbvBand { Low, Mid, High }

enum class Origin {
    France,
    Italy,
    Spain,
    Germany,
    Portugal,
    USA,
    Chile,
    Argentina,
    Australia,
    NewZealand,
    SouthAfrica,
    Scotland,
    Ireland,
    Japan,
    Canada,
    Taiwan,
    Korea,
    Other,
    ;

    companion object {
        val wineOrigins: List<Origin> =
            listOf(
                France,
                Italy,
                Spain,
                Germany,
                Portugal,
                USA,
                Chile,
                Argentina,
                Australia,
                NewZealand,
                SouthAfrica,
                Other,
            )

        val whiskyOrigins: List<Origin> =
            listOf(Scotland, USA, Ireland, Japan, Canada, Taiwan, Korea, Other)

        fun of(type: DrinkType): List<Origin> =
            when (type) {
                DrinkType.Wine -> wineOrigins
                DrinkType.Whiskey -> whiskyOrigins
            }
    }
}

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
                origin =
                    stored[TagCategory.Origin]?.let { name ->
                        Origin.entries.find { it.name == name }
                            ?: when (name) {
                                "OldWorld", "NewWorld" -> Origin.Other
                                else -> null
                            }
                    },
            )
    }
}
