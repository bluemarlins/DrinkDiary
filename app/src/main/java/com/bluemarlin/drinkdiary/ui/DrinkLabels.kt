package com.bluemarlin.drinkdiary.ui

import com.bluemarlin.drinkdiary.domain.model.AbvBand
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Origin
import com.bluemarlin.drinkdiary.domain.model.PeatTag
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.WhiskyStyle
import com.bluemarlin.drinkdiary.domain.model.WineColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// 기록 화면과 컬렉션 화면이 같은 것을 다른 말로 부르지 않도록 라벨을 한곳에 모은다.
// 문구는 리소스가 아니라 코드에 둔다(software-architecture.md 7절 — 한국어 단일 확정).
object DrinkLabels {
    fun drinkType(type: DrinkType): String =
        when (type) {
            DrinkType.Wine -> "와인"
            DrinkType.Whiskey -> "위스키"
        }

    fun collectionStatus(status: CollectionStatus): String =
        when (status) {
            CollectionStatus.Normal -> "그냥 그래요"
            CollectionStatus.Repurchase -> "또 살래요"
            CollectionStatus.NotForMe -> "안 맞아요"
        }

    fun servingStyle(style: ServingStyle): String =
        when (style) {
            ServingStyle.Neat -> "니트"
            ServingStyle.OnTheRocks -> "온더락"
            ServingStyle.WithWater -> "물 타서"
            ServingStyle.Highball -> "하이볼"
        }

    fun trait(trait: Trait): String =
        when (trait) {
            Trait.Sweetness -> "단맛"
            Trait.Body -> "무게감"
            Trait.Intensity -> "향의 세기"
            Trait.Aftertaste -> "여운"
            Trait.Acidity -> "산미"
            Trait.Tannin -> "떫음"
            Trait.Peat -> "스모키함"
            Trait.AlcoholBurn -> "알코올감"
        }

    // 판정된 선호를 가리키는 말. 중립은 방향이 없으므로 여기서 다루지 않는다.
    fun preference(
        trait: Trait,
        preference: TastePreference,
    ): String =
        when (preference) {
            TastePreference.High -> poles(trait).second
            TastePreference.Low -> poles(trait).first
            TastePreference.Neutral -> "가리지 않음"
        }

    // 기록에 남은 답. '보통'은 축의 어느 쪽도 아니다.
    fun answer(
        trait: Trait,
        answer: TraitAnswer,
    ): String =
        when (answer) {
            TraitAnswer.High -> poles(trait).second
            TraitAnswer.Low -> poles(trait).first
            TraitAnswer.Mid -> "보통"
        }

    private fun poles(trait: Trait): Pair<String, String> {
        val (low, high) =
            when (trait) {
                Trait.Sweetness -> "드라이" to "달콤"
                Trait.Body -> "가벼움" to "묵직함"
                Trait.Intensity -> "은은함" to "진함"
                Trait.Aftertaste -> "산뜻함" to "긴 여운"
                Trait.Acidity -> "부드러움" to "산뜻함"
                Trait.Tannin -> "안 떫음" to "떫음"
                Trait.Peat -> "스모키하지 않음" to "스모키함"
                Trait.AlcoholBurn -> "부드러움" to "화끈함"
            }
        return low to high
    }

    fun tagCategory(category: TagCategory): String =
        when (category) {
            TagCategory.WhiskyStyle -> "종류"
            TagCategory.Peat -> "피트"
            TagCategory.WineColor -> "색"
            TagCategory.AbvBand -> "도수"
            TagCategory.Origin -> "산지"
        }

    // 저장된 값 이름을 화면 문구로. 모르는 값은 그대로 보여준다 — 태그 집합이 바뀌는 중이라
    // 빈칸으로 두면 무엇이 사라졌는지 알 수 없다.
    fun tagValue(
        category: TagCategory,
        value: String,
        type: DrinkType? = null,
    ): String =
        when (category) {
            TagCategory.WhiskyStyle ->
                when (value) {
                    WhiskyStyle.SingleMalt.name -> "싱글몰트"
                    WhiskyStyle.Blended.name -> "블렌디드"
                    WhiskyStyle.Bourbon.name -> "버번"
                    else -> value
                }

            TagCategory.Peat ->
                when (value) {
                    PeatTag.Peated.name -> "스모키함"
                    PeatTag.Unpeated.name -> "스모키하지 않음"
                    else -> value
                }

            TagCategory.WineColor ->
                when (value) {
                    WineColor.Red.name -> "레드"
                    WineColor.White.name -> "화이트"
                    WineColor.Other.name -> "그 외"
                    else -> value
                }

            // 구간 경계는 주종마다 다르다. 주종을 모르면 순서만 말한다.
            TagCategory.AbvBand -> abvLabel(value, type)

            TagCategory.Origin ->
                when (value) {
                    Origin.OldWorld.name -> "구대륙"
                    Origin.NewWorld.name -> "신대륙"
                    else -> value
                }
        }

    private fun abvLabel(
        value: String,
        type: DrinkType?,
    ): String {
        val band = AbvBand.entries.find { it.name == value } ?: return value
        return when (type) {
            DrinkType.Whiskey ->
                when (band) {
                    AbvBand.Low -> "40%대"
                    AbvBand.Mid -> "43~45%"
                    AbvBand.High -> "46% 이상"
                }

            DrinkType.Wine ->
                when (band) {
                    AbvBand.Low -> "12% 이하"
                    AbvBand.Mid -> "13~14%"
                    AbvBand.High -> "15% 이상"
                }

            null ->
                when (band) {
                    AbvBand.Low -> "낮은 도수"
                    AbvBand.Mid -> "중간 도수"
                    AbvBand.High -> "높은 도수"
                }
        }
    }

    // 주종 · 빈티지/음용방법 · 날짜. 없는 값은 자리를 차지하지 않는다.
    fun subtitle(record: DrinkRecord): String =
        listOfNotNull(
            drinkType(record.type),
            record.vintage?.toString(),
            record.servingStyle?.let { servingStyle(it) },
            date(record.recordedAtMillis),
        ).joinToString(" · ")

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA)

    fun date(millis: Long): String =
        Instant
            .ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(dateFormat)

    fun price(won: Long): String = "%,d원".format(Locale.KOREA, won)
}
