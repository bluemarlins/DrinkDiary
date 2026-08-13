package com.bluemarlin.drinkdiary.domain.model

// 같은 술도 음용 방법에 따라 다르게 느껴진다(P4). 위스키 기록에만 쓴다.
enum class ServingStyle {
    Neat,
    OnTheRocks,
    WithWater,
    Highball,
    ;

    companion object {
        fun fromName(name: String?): ServingStyle? = entries.firstOrNull { it.name == name }
    }
}
