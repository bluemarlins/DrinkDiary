package com.bluemarlin.drinkdiary.domain.model

@JvmInline
value class TasteInput(
    val answers: Map<Trait, TraitAnswer> = emptyMap(),
) {
    operator fun get(trait: Trait): TraitAnswer? = answers[trait]

    fun with(
        trait: Trait,
        answer: TraitAnswer,
    ): TasteInput = TasteInput(answers + (trait to answer))

    // 한쪽으로 치우친다고 답한 축의 수. '보통'은 세지 않는다.
    // 버려진다는 뜻이 아니다 — 판정에는 그대로 쓰인다. 기록 직후 화면에서
    // "몇 축에 뚜렷한 인상을 남겼는지" 보여주기 위한 값이다.
    val leaningCount: Int get() = answers.count { it.value != TraitAnswer.Mid }
}
