package com.bluemarlin.drinkdiary.domain.model

import com.bluemarlin.drinkdiary.R

enum class CollectionStatus(
    val labelRes: Int,
) {
    Normal(R.string.collection_status_normal),
    Repurchase(R.string.collection_status_repurchase),
    NotForMe(R.string.collection_status_not_for_me),
    ;

    companion object {
        fun fromStorageValue(value: String): CollectionStatus? = entries.firstOrNull { it.name == value }
    }
}
