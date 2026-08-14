package com.bluemarlin.drinkdiary.ui

import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
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
