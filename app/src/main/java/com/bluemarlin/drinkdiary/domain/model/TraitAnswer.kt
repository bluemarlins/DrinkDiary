package com.bluemarlin.drinkdiary.domain.model

// 사용자가 축 하나에 답할 수 있는 5단계 척도 값.
enum class TraitAnswer(
    // 상관 계산에 쓰는 순서값 (1~5).
    val level: Int,
) {
    VeryLow(1),
    Low(2),
    Mid(3),
    High(4),
    VeryHigh(5),
    ;

    companion object {
        fun fromName(name: String?): TraitAnswer? = entries.firstOrNull { it.name == name }
    }
}
