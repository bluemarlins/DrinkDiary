package com.bluemarlin.drinkdiary.domain.model

enum class CollectionStatus {
    Normal,
    Repurchase,
    NotForMe,
    ;

    companion object {
        fun fromName(name: String?): CollectionStatus? = entries.firstOrNull { it.name == name }
    }
}
