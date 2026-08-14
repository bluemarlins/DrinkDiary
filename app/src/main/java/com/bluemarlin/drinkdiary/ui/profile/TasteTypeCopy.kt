package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.TasteType
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.ui.DrinkLabels

// 별칭을 손으로 짓지 않고 축 값에서 조립한다. 81가지를 일일이 짓는 것은 애초에 불가능하므로
// 파생 규칙이 유일한 방법이다(taste-type-naming.md 3절).
//
// 중립을 결핍처럼 쓰지 않는다 — "아직 취향이 없다"가 아니라 "가리지 않는다"다
// (branding.md 4-5절 '지켜야 할 선').
object TasteTypeCopy {
    fun shortName(type: TasteType): String {
        val words = type.directional.take(2).map { (trait, preference) -> adjective(trait, preference) }
        return if (words.isEmpty()) "고루 즐기는 취향" else words.joinToString(" ") + " 취향"
    }

    fun sentence(type: TasteType): String {
        val directional = type.directional
        if (directional.isEmpty()) return "어느 쪽에도 치우치지 않고 두루 즐기시네요."

        // 마지막 절만 종결형으로 닫는다. 축이 하나뿐이어도 문장이 성립한다.
        val body =
            directional
                .dropLast(1)
                .joinToString("") { (trait, preference) -> connecting(trait, preference) + " " }
                .plus(directional.last().let { (trait, preference) -> ending(trait, preference) })

        val neutral = type.neutral
        if (neutral.isEmpty()) return "$body."

        val names =
            neutral
                .map { DrinkLabels.trait(it) }
                .reduce { acc, name -> Josa.and(acc) + " " + name }
        return "$body. ${Josa.topic(names)} 크게 가리지 않으세요."
    }

    private fun adjective(
        trait: Trait,
        preference: TastePreference,
    ): String {
        val high = preference == TastePreference.High
        return when (trait) {
            Trait.Sweetness -> if (high) "달콤한" else "드라이한"
            Trait.Body -> if (high) "묵직한" else "가벼운"
            Trait.Intensity -> if (high) "진한" else "은은한"
            Trait.Aftertaste -> if (high) "여운이 긴" else "산뜻한"
            else -> DrinkLabels.trait(trait)
        }
    }

    private fun connecting(
        trait: Trait,
        preference: TastePreference,
    ): String {
        val high = preference == TastePreference.High
        return when (trait) {
            Trait.Sweetness -> if (high) "달콤하고" else "드라이하고"
            Trait.Body -> if (high) "묵직하며" else "가벼우며"
            Trait.Intensity -> if (high) "향이 진하고" else "향이 은은하고"
            Trait.Aftertaste -> if (high) "여운이 길고" else "산뜻하게 끝나고"
            else -> DrinkLabels.trait(trait)
        }
    }

    private fun ending(
        trait: Trait,
        preference: TastePreference,
    ): String {
        val high = preference == TastePreference.High
        return when (trait) {
            Trait.Sweetness -> if (high) "달콤합니다" else "드라이합니다"
            Trait.Body -> if (high) "묵직합니다" else "가볍습니다"
            Trait.Intensity -> if (high) "향이 진합니다" else "향이 은은합니다"
            Trait.Aftertaste -> if (high) "여운이 깁니다" else "산뜻하게 끝납니다"
            else -> DrinkLabels.trait(trait)
        }
    }
}

// 문구를 조립하는 이상 조사도 조립해야 한다. 축 이름이 바뀌면 "여운는" 같은 것이 화면에 뜬다.
internal object Josa {
    fun topic(word: String): String = word + if (hasFinalConsonant(word)) "은" else "는"

    fun and(word: String): String = word + if (hasFinalConsonant(word)) "과" else "와"

    private fun hasFinalConsonant(word: String): Boolean {
        val last = word.lastOrNull { !it.isWhitespace() } ?: return false
        if (last !in '가'..'힣') return false
        return (last.code - 0xAC00) % 28 != 0
    }
}
