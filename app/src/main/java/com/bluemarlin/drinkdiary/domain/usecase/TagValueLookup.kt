package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.repository.BottleDictionary

// 사용자가 답한 태그와 사전이 아는 사실을 한 자리에서 꺼낸다. 어디서 왔든 판정은 같다 —
// "그 값을 가진 기록의 만족도"를 볼 뿐이다(software-architecture.md 4-2).
//
// **태그 선호와 공백 안내가 같은 값을 봐야 한다.** 한쪽만 사전을 참조하면 같은 화면에
// "셰리 캐스크 5잔"과 "셰리 캐스크는 아직 없어요"가 나란히 뜬다.
internal fun DrinkRecord.tagValue(
    category: TagCategory,
    dictionary: BottleDictionary,
): String? =
    if (category.fromDictionary) {
        dictionary.lookup(type, name)?.let { facts ->
            when (category) {
                TagCategory.Cask -> facts.cask?.name
                TagCategory.WineStyle -> facts.wineStyle?.name
                else -> null
            }
        }
    } else {
        tags[category]
    }
