package com.bluemarlin.drinkdiary.domain.model

// Unsure는 "중간"이 아니라 "판단하지 못함"이다. 선호 비교에서 제외된다.
enum class TraitAnswer {
    Low,
    Unsure,
    High,
    ;

    val isDirectional: Boolean get() = this != Unsure

    companion object {
        fun fromName(name: String?): TraitAnswer? = entries.firstOrNull { it.name == name }
    }
}
