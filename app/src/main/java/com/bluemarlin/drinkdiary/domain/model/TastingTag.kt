package com.bluemarlin.drinkdiary.domain.model

/**
 * Tasting vocabulary catalog — see app/docs/research/tasting-vocabulary.md for the
 * researched source vocabulary and app/docs/dev/database-design.md for the storage format.
 *
 * `key` is what gets persisted and must never change: renaming a key would orphan every
 * record already tagged with it. `label` is pure UI copy and can be reworded freely, which
 * is what lets the display labels stay within the 5-character chip budget without touching
 * the database.
 *
 * Concepts shared across drink types deliberately share one key (시트러스 is `citrus`
 * whether it was tasted in wine, whiskey, or beer) so that tag-frequency analysis can read
 * a taste profile across the whole collection rather than per type. Where the same word
 * means different things in different categories — 스모키 as an aroma vs. as a finish — the
 * keys stay distinct.
 */
data class TastingTag(
    val key: String,
    val label: String,
    val category: TagCategory,
    val isBasic: Boolean,
)

/**
 * Category names differ slightly per drink type in the source research (whiskey says
 * 피니시 where beer says 목넘김/여운), but the underlying four axes are the same, so one
 * enum covers all three. [DrinkType.tagCategoryLabel] provides the per-type wording.
 */
enum class TagCategory {
    Aroma,
    Flavor,
    Texture,
    Finish,
}

fun DrinkType.tagCategoryLabel(category: TagCategory): String = when (category) {
    TagCategory.Aroma -> "향"
    TagCategory.Flavor -> "맛"
    TagCategory.Texture -> when (this) {
        DrinkType.Wine -> "바디/질감"
        DrinkType.Whiskey -> "질감/바디"
        DrinkType.Beer -> "탄산/질감"
    }
    TagCategory.Finish -> when (this) {
        DrinkType.Wine -> "여운"
        DrinkType.Whiskey -> "피니시"
        DrinkType.Beer -> "목넘김/여운"
    }
}

fun DrinkType.tastingTags(): List<TastingTag> = when (this) {
    DrinkType.Wine -> WineTags
    DrinkType.Whiskey -> WhiskeyTags
    DrinkType.Beer -> BeerTags
}

/** Resolves a stored key back to its catalog entry, or null for a user-entered custom tag. */
fun findTastingTag(key: String): TastingTag? = TagsByKey[key]

private val WineTags = listOf(
    TastingTag("citrus", "시트러스", TagCategory.Aroma, isBasic = true),
    TastingTag("red_fruit", "붉은 과일", TagCategory.Aroma, isBasic = true),
    TastingTag("black_fruit", "검은 과일", TagCategory.Aroma, isBasic = true),
    TastingTag("tropical_fruit", "열대 과일", TagCategory.Aroma, isBasic = true),
    TastingTag("floral", "꽃향", TagCategory.Aroma, isBasic = true),
    TastingTag("herbal", "풀/허브", TagCategory.Aroma, isBasic = true),
    TastingTag("oak", "오크", TagCategory.Aroma, isBasic = true),
    TastingTag("vanilla", "바닐라", TagCategory.Aroma, isBasic = true),
    TastingTag("stone_fruit", "핵과류", TagCategory.Aroma, isBasic = false),
    TastingTag("dried_fruit", "말린 과일", TagCategory.Aroma, isBasic = false),
    TastingTag("leather_earth", "가죽/흙", TagCategory.Aroma, isBasic = false),
    TastingTag("bready_yeast", "빵/효모", TagCategory.Aroma, isBasic = false),

    TastingTag("dry", "드라이", TagCategory.Flavor, isBasic = true),
    TastingTag("off_dry", "연한 단맛", TagCategory.Flavor, isBasic = true),
    TastingTag("sweet", "달콤함", TagCategory.Flavor, isBasic = true),
    TastingTag("tart", "새콤함", TagCategory.Flavor, isBasic = true),
    TastingTag("bitter", "쌉싸름함", TagCategory.Flavor, isBasic = true),
    TastingTag("ripe_fruit", "완숙 과일", TagCategory.Flavor, isBasic = true),
    TastingTag("sour", "시큼함", TagCategory.Flavor, isBasic = false),
    TastingTag("saline", "짭조름함", TagCategory.Flavor, isBasic = false),
    TastingTag("spicy", "스파이시", TagCategory.Flavor, isBasic = false),
    TastingTag("complex", "복합적", TagCategory.Flavor, isBasic = false),

    TastingTag("light_body", "가벼움", TagCategory.Texture, isBasic = true),
    TastingTag("full_body", "묵직함", TagCategory.Texture, isBasic = true),
    TastingTag("smooth", "부드러움", TagCategory.Texture, isBasic = true),
    TastingTag("astringent", "떫은맛", TagCategory.Texture, isBasic = true),
    TastingTag("silky", "매끄러움", TagCategory.Texture, isBasic = false),
    TastingTag("oily", "유질감", TagCategory.Texture, isBasic = false),
    TastingTag("creamy", "크리미", TagCategory.Texture, isBasic = false),
    TastingTag("crisp", "쨍함", TagCategory.Texture, isBasic = false),
    TastingTag("firm_tannin", "빳빳함", TagCategory.Texture, isBasic = false),

    TastingTag("clean_finish", "깔끔함", TagCategory.Finish, isBasic = true),
    TastingTag("long_finish", "긴 여운", TagCategory.Finish, isBasic = true),
    TastingTag("short_finish", "짧은 여운", TagCategory.Finish, isBasic = true),
    TastingTag("gentle_finish", "은은함", TagCategory.Finish, isBasic = true),
    TastingTag("coarse_finish", "텁텁함", TagCategory.Finish, isBasic = true),
    TastingTag("bitter_finish", "쓴 끝맛", TagCategory.Finish, isBasic = true),
    TastingTag("warming", "열감", TagCategory.Finish, isBasic = false),
    TastingTag("complex_finish", "복합 여운", TagCategory.Finish, isBasic = false),
)

private val WhiskeyTags = listOf(
    TastingTag("vanilla", "바닐라", TagCategory.Aroma, isBasic = true),
    TastingTag("caramel", "캐러멜", TagCategory.Aroma, isBasic = true),
    TastingTag("smoky", "스모키", TagCategory.Aroma, isBasic = true),
    TastingTag("citrus", "시트러스", TagCategory.Aroma, isBasic = true),
    TastingTag("nutty_aroma", "견과류", TagCategory.Aroma, isBasic = true),
    TastingTag("floral", "꽃향", TagCategory.Aroma, isBasic = true),
    TastingTag("chocolate", "초콜릿", TagCategory.Aroma, isBasic = true),
    TastingTag("peat", "피트", TagCategory.Aroma, isBasic = false),
    TastingTag("dried_fruit", "말린 과일", TagCategory.Aroma, isBasic = false),
    TastingTag("spicy_aroma", "스파이시", TagCategory.Aroma, isBasic = false),
    TastingTag("leather_tobacco", "가죽/담배", TagCategory.Aroma, isBasic = false),
    TastingTag("grain_malt", "곡물/맥아", TagCategory.Aroma, isBasic = false),

    TastingTag("sweet", "달콤함", TagCategory.Flavor, isBasic = true),
    TastingTag("spicy", "스파이시", TagCategory.Flavor, isBasic = true),
    TastingTag("bitter", "쌉싸름함", TagCategory.Flavor, isBasic = true),
    TastingTag("jammy", "과일 잼", TagCategory.Flavor, isBasic = true),
    TastingTag("nutty", "고소함", TagCategory.Flavor, isBasic = true),
    TastingTag("astringent", "떫은맛", TagCategory.Flavor, isBasic = false),
    TastingTag("saline", "짭조름함", TagCategory.Flavor, isBasic = false),
    TastingTag("medicinal_herb", "한약재", TagCategory.Flavor, isBasic = false),
    TastingTag("sour", "시큼함", TagCategory.Flavor, isBasic = false),

    TastingTag("smooth", "부드러움", TagCategory.Texture, isBasic = true),
    TastingTag("hot", "타는 듯함", TagCategory.Texture, isBasic = true),
    TastingTag("light_body", "가벼움", TagCategory.Texture, isBasic = true),
    TastingTag("full_body", "묵직함", TagCategory.Texture, isBasic = true),
    TastingTag("oily", "기름짐", TagCategory.Texture, isBasic = false),
    TastingTag("creamy", "크리미", TagCategory.Texture, isBasic = false),
    TastingTag("sticky", "끈적함", TagCategory.Texture, isBasic = false),
    TastingTag("sharp", "날카로움", TagCategory.Texture, isBasic = false),

    TastingTag("long_finish", "긴 여운", TagCategory.Finish, isBasic = true),
    TastingTag("clean_finish", "깔끔함", TagCategory.Finish, isBasic = true),
    TastingTag("gentle_finish", "은은함", TagCategory.Finish, isBasic = true),
    TastingTag("alcohol_spicy", "알싸함", TagCategory.Finish, isBasic = true),
    TastingTag("nutty_finish", "고소함", TagCategory.Finish, isBasic = true),
    TastingTag("smoky_finish", "스모키", TagCategory.Finish, isBasic = false),
    TastingTag("astringent_finish", "떫은 끝", TagCategory.Finish, isBasic = false),
    TastingTag("saline_finish", "짠 끝맛", TagCategory.Finish, isBasic = false),
)

private val BeerTags = listOf(
    TastingTag("hoppy_grass", "홉/풀잎", TagCategory.Aroma, isBasic = true),
    TastingTag("citrus", "시트러스", TagCategory.Aroma, isBasic = true),
    TastingTag("malty_aroma", "맥아향", TagCategory.Aroma, isBasic = true),
    TastingTag("caramel", "캐러멜", TagCategory.Aroma, isBasic = true),
    TastingTag("tropical_fruit", "열대 과일", TagCategory.Aroma, isBasic = true),
    TastingTag("floral", "꽃향", TagCategory.Aroma, isBasic = true),
    TastingTag("banana_clove", "바나나", TagCategory.Aroma, isBasic = false),
    TastingTag("funky", "퀴퀴함", TagCategory.Aroma, isBasic = false),
    TastingTag("roast_coffee", "훈연/커피", TagCategory.Aroma, isBasic = false),
    TastingTag("piney", "솔향/송진", TagCategory.Aroma, isBasic = false),

    TastingTag("bitter", "쌉쌀함", TagCategory.Flavor, isBasic = true),
    TastingTag("malty_flavor", "구수함", TagCategory.Flavor, isBasic = true),
    TastingTag("sweet", "달콤함", TagCategory.Flavor, isBasic = true),
    TastingTag("balance", "밸런스", TagCategory.Flavor, isBasic = true),
    TastingTag("watery", "밍밍함", TagCategory.Flavor, isBasic = true),
    TastingTag("choco_coffee", "초코/커피", TagCategory.Flavor, isBasic = true),
    TastingTag("tart", "새콤함", TagCategory.Flavor, isBasic = false),
    TastingTag("astringent", "떫은맛", TagCategory.Flavor, isBasic = false),

    TastingTag("refreshing", "청량함", TagCategory.Texture, isBasic = true),
    TastingTag("smooth", "부드러움", TagCategory.Texture, isBasic = true),
    TastingTag("high_carb", "강한 탄산", TagCategory.Texture, isBasic = true),
    TastingTag("full_body", "묵직함", TagCategory.Texture, isBasic = true),
    TastingTag("light_body", "가벼움", TagCategory.Texture, isBasic = true),
    TastingTag("coating", "텁텁함", TagCategory.Texture, isBasic = true),
    TastingTag("thick", "걸쭉함", TagCategory.Texture, isBasic = false),
    TastingTag("dry_texture", "드라이함", TagCategory.Texture, isBasic = false),

    TastingTag("clean_finish", "깔끔함", TagCategory.Finish, isBasic = true),
    TastingTag("bitter_finish", "쓴 끝맛", TagCategory.Finish, isBasic = true),
    TastingTag("crisp_finish", "개운함", TagCategory.Finish, isBasic = true),
    TastingTag("sweet_finish", "단 여운", TagCategory.Finish, isBasic = true),
    TastingTag("coarse_finish", "텁텁함", TagCategory.Finish, isBasic = true),
    TastingTag("easy_drinking", "술술 넘김", TagCategory.Finish, isBasic = true),
    TastingTag("hot_finish", "화끈거림", TagCategory.Finish, isBasic = false),
    TastingTag("sour_finish", "신 끝맛", TagCategory.Finish, isBasic = false),
)

// Shared keys legitimately appear in several type lists, so later entries with the same key
// are dropped rather than treated as a conflict. Labels for a shared key are kept identical
// across types except where the source vocabulary genuinely words it differently
// (oily: 유질감 in wine vs 기름짐 in whiskey) — lookups here resolve to the first occurrence,
// which only affects display of a tag on a record whose type no longer offers it.
private val TagsByKey: Map<String, TastingTag> =
    (WineTags + WhiskeyTags + BeerTags).associateBy { it.key }
