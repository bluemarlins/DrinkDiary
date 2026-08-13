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

    // 방향을 지각한 축의 수. Unsure는 세지 않는다.
    val directionalCount: Int get() = answers.count { it.value.isDirectional }
}
