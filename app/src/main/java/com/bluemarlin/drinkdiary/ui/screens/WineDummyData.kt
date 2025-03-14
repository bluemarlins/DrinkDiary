package com.bluemarlin.drinkdiary.ui.screens

import com.bluemarlin.drinkdiary.R

enum class WineType(name: String) {
    RED("Red"),
    WHITE("White"),
    SPARKLING("Sparkling"),
    OTHERS("Others")
}

data class Wine(
    val name: String,
    val rating: Float,
    val price: String,
    val imageRes: Int,
    val purchaseDate: String,
    val purchaseLocation: String,
    val tastingNotes: String,
    val type: WineType = WineType.OTHERS
)

// 샘플 데이터 생성
val sampleWines = listOf(
    Wine(
        name = "Château Margaux",
        rating = 4.8f,
        price = "45000",
        imageRes = R.drawable.wine_sample,
        purchaseDate = "2024-01-15",
        purchaseLocation = "Fine Wines & Spirits",
        tastingNotes = "우아한 탄닌과 블랙베리, 바닐라 향이 어우러진 깊은 맛."
    ),
    Wine(
        name = "Opus One",
        rating = 4.6f,
        price = "35000",
        imageRes = R.drawable.wine_sample,
        purchaseDate = "2024-02-10",
        purchaseLocation = "Napa Valley Wine Shop",
        tastingNotes = "진한 체리와 초콜릿 향이 조화를 이루며 부드러운 피니시가 특징."
    ),
    Wine(
        name = "Sassicaia",
        rating = 4.7f,
        price = "32000",
        imageRes = R.drawable.wine_sample,
        purchaseDate = "2023-12-05",
        purchaseLocation = "Tuscany Wine House",
        tastingNotes = "풍부한 블랙커런트 향과 약간의 스파이시한 느낌이 돋보임."
    ),
    Wine(
        name = "Penfolds Grange",
        rating = 4.9f,
        price = "50000",
        imageRes = R.drawable.wine_sample,
        purchaseDate = "2024-03-22",
        purchaseLocation = "Australian Wine Market",
        tastingNotes = "짙은 오크 향과 깊은 베리류의 조화가 인상적인 풀바디 와인."
    ),
    Wine(
        name = "Vega Sicilia Unico",
        rating = 4.7f,
        price = "48000",
        imageRes = R.drawable.wine_sample,
        purchaseDate = "2024-01-30",
        purchaseLocation = "Spanish Wine Emporium",
        tastingNotes = "오랜 숙성을 거친 벨벳 같은 질감과 허브 향이 매력적."
    ),
    Wine(
        name = "Dom Pérignon",
        rating = 4.5f,
        price = "30000",
        imageRes = R.drawable.wine_sample,
        purchaseDate = "2023-11-18",
        purchaseLocation = "Champagne Boutique",
        tastingNotes = "상쾌한 사과와 시트러스 노트가 균형 잡힌 샴페인."
    ),
    Wine(
        name = "Ridge Monte Bello",
        rating = 4.8f,
        price = "37000",
        imageRes = R.drawable.wine_sample,
        purchaseDate = "2024-02-28",
        purchaseLocation = "California Fine Wines",
        tastingNotes = "우아한 산미와 다크 초콜릿 향이 매력적인 와인."
    ),
    Wine(
        name = "Gaja Barbaresco",
        rating = 4.6f,
        price = "34000",
        imageRes = R.drawable.wine_sample,
        purchaseDate = "2024-01-05",
        purchaseLocation = "Italian Wine Cellar",
        tastingNotes = "풍부한 체리 향과 타닌이 절묘한 밸런스를 이루는 와인."
    ),
    Wine(
        name = "Château Latour",
        rating = 4.9f,
        price = "47000",
        imageRes = R.drawable.wine_sample,
        purchaseDate = "2024-03-10",
        purchaseLocation = "Bordeaux Grand Cru",
        tastingNotes = "묵직한 바디감과 자두, 오크 향이 깊게 어우러짐."
    ),
    Wine(
        name = "Screaming Eagle",
        rating = 5.0f,
        price = "60000",
        imageRes = R.drawable.wine_sample,
        purchaseDate = "2024-02-15",
        purchaseLocation = "Exclusive Wine Auction",
        tastingNotes = "희귀한 한정판 와인으로, 블랙베리와 카카오 향이 인상적."
    )
)

