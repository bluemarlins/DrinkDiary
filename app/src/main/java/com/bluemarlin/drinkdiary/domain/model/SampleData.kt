package com.bluemarlin.drinkdiary.domain.model

/**
 * 5축 5단계 척도(VeryLow~VeryHigh), 12개국 와인 산지, 8개국 위스키 산지, 6대 분류 체계에 맞춘
 * 개발 및 테스트, 대시보드 UI 프리뷰용 표준 더미 데이터셋입니다.
 */
object SampleData {
    val wineRecords: List<DrinkRecord> =
        listOf(
            DrinkRecord(
                id = 1L,
                type = DrinkType.Wine,
                name = "샤또 마고 2018",
                vintage = 2018,
                tags = DrinkTags(wineColor = WineColor.Red, origin = Origin.France),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.VeryLow)
                        .with(Trait.Acidity, TraitAnswer.High)
                        .with(Trait.Tannin, TraitAnswer.VeryHigh)
                        .with(Trait.Body, TraitAnswer.VeryHigh)
                        .with(Trait.Aftertaste, TraitAnswer.VeryHigh),
                rating = 5.0,
                collectionStatus = CollectionStatus.Repurchase,
                price = 1200000L,
                place = "파인다이닝",
                memo = "우아하고 실키한 탄닌, 블랙베리와 삼나무의 끝없는 여운.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 2,
            ),
            DrinkRecord(
                id = 2L,
                type = DrinkType.Wine,
                name = "도멘 드 라 로마네 꽁띠 그랑 에셰조 2017",
                vintage = 2017,
                tags = DrinkTags(wineColor = WineColor.Red, origin = Origin.France),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.VeryLow)
                        .with(Trait.Acidity, TraitAnswer.VeryHigh)
                        .with(Trait.Tannin, TraitAnswer.Mid)
                        .with(Trait.Body, TraitAnswer.Mid)
                        .with(Trait.Aftertaste, TraitAnswer.VeryHigh),
                rating = 5.0,
                collectionStatus = CollectionStatus.Repurchase,
                price = 2500000L,
                place = "와인바",
                memo = "말린 장미와 붉은 체리, 압도적인 미네랄과 섬세한 산미.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 5,
            ),
            DrinkRecord(
                id = 3L,
                type = DrinkType.Wine,
                name = "루이 자도 샤블리 2021",
                vintage = 2021,
                tags = DrinkTags(wineColor = WineColor.White, origin = Origin.France),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.VeryLow)
                        .with(Trait.Acidity, TraitAnswer.VeryHigh)
                        .with(Trait.Tannin, TraitAnswer.VeryLow)
                        .with(Trait.Body, TraitAnswer.Low)
                        .with(Trait.Aftertaste, TraitAnswer.High),
                rating = 4.5,
                collectionStatus = CollectionStatus.Repurchase,
                price = 48000L,
                place = "이마트",
                memo = "날카롭고 청량한 산미, 굴/해산물과 최고의 궁합.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 8,
            ),
            DrinkRecord(
                id = 4L,
                type = DrinkType.Wine,
                name = "클라우디 베이 소비뇽 블랑 2023",
                vintage = 2023,
                tags = DrinkTags(wineColor = WineColor.White, origin = Origin.NewZealand),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.Low)
                        .with(Trait.Acidity, TraitAnswer.VeryHigh)
                        .with(Trait.Tannin, TraitAnswer.VeryLow)
                        .with(Trait.Body, TraitAnswer.Mid)
                        .with(Trait.Aftertaste, TraitAnswer.High),
                rating = 4.5,
                collectionStatus = CollectionStatus.Repurchase,
                price = 45000L,
                place = "바틀샵",
                memo = "패션후르츠와 시트러스, 풀내음의 폭발적인 아로마.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 12,
            ),
            DrinkRecord(
                id = 5L,
                type = DrinkType.Wine,
                name = "카사노바 디 네리 브루넬로 디 몬탈치노 2016",
                vintage = 2016,
                tags = DrinkTags(wineColor = WineColor.Red, origin = Origin.Italy),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.VeryLow)
                        .with(Trait.Acidity, TraitAnswer.High)
                        .with(Trait.Tannin, TraitAnswer.High)
                        .with(Trait.Body, TraitAnswer.High)
                        .with(Trait.Aftertaste, TraitAnswer.VeryHigh),
                rating = 4.8,
                collectionStatus = CollectionStatus.Repurchase,
                price = 140000L,
                place = "콜키지 와인모임",
                memo = "산지오베제의 맑고 힘있는 붉은 과실, 가죽과 발사믹의 복합미.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 15,
            ),
            DrinkRecord(
                id = 6L,
                type = DrinkType.Wine,
                name = "우나니메 2018",
                vintage = 2018,
                tags = DrinkTags(wineColor = WineColor.Red, origin = Origin.Argentina),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.Low)
                        .with(Trait.Acidity, TraitAnswer.Mid)
                        .with(Trait.Tannin, TraitAnswer.High)
                        .with(Trait.Body, TraitAnswer.VeryHigh)
                        .with(Trait.Aftertaste, TraitAnswer.High),
                rating = 4.0,
                collectionStatus = CollectionStatus.Normal,
                price = 42000L,
                place = "코스트코",
                memo = "진득한 말벡 블렌딩, 스테이크와 찰떡.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 20,
            ),
            DrinkRecord(
                id = 7L,
                type = DrinkType.Wine,
                name = "돔 페리뇽 2013",
                vintage = 2013,
                tags = DrinkTags(wineColor = WineColor.Sparkling, origin = Origin.France),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.VeryLow)
                        .with(Trait.Acidity, TraitAnswer.VeryHigh)
                        .with(Trait.Tannin, TraitAnswer.VeryLow)
                        .with(Trait.Body, TraitAnswer.High)
                        .with(Trait.Aftertaste, TraitAnswer.VeryHigh),
                rating = 5.0,
                collectionStatus = CollectionStatus.Repurchase,
                price = 350000L,
                place = "호텔 라운지",
                memo = "극도로 섬세한 기포, 구운 브리오슈와 백도 복숭아 향.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 25,
            ),
            DrinkRecord(
                id = 8L,
                type = DrinkType.Wine,
                name = "파이퍼 하이직 뀌베 브뤼",
                tags = DrinkTags(wineColor = WineColor.Sparkling, origin = Origin.France),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.Low)
                        .with(Trait.Acidity, TraitAnswer.High)
                        .with(Trait.Tannin, TraitAnswer.VeryLow)
                        .with(Trait.Body, TraitAnswer.Mid)
                        .with(Trait.Aftertaste, TraitAnswer.High),
                rating = 4.2,
                collectionStatus = CollectionStatus.Normal,
                price = 65000L,
                place = "면세점",
                memo = "경쾌한 풋사과와 시트러스, 파티용으로 훌륭함.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 30,
            ),
            DrinkRecord(
                id = 9L,
                type = DrinkType.Wine,
                name = "루시 마고 와일드맨 블랑 2022",
                vintage = 2022,
                tags = DrinkTags(wineColor = WineColor.Natural, origin = Origin.Australia),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.Low)
                        .with(Trait.Acidity, TraitAnswer.High)
                        .with(Trait.Tannin, TraitAnswer.Low)
                        .with(Trait.Body, TraitAnswer.Mid)
                        .with(Trait.Aftertaste, TraitAnswer.Mid),
                rating = 3.8,
                collectionStatus = CollectionStatus.Normal,
                price = 75000L,
                place = "내추럴 와인바",
                memo = "효모향과 감귤, 쿰쿰하면서도 펑키하고 독특한 뉘앙스.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 35,
            ),
            DrinkRecord(
                id = 10L,
                type = DrinkType.Wine,
                name = "그라함 20년 토니 포트",
                tags = DrinkTags(wineColor = WineColor.Port, origin = Origin.Portugal),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.VeryHigh)
                        .with(Trait.Acidity, TraitAnswer.Mid)
                        .with(Trait.Tannin, TraitAnswer.Low)
                        .with(Trait.Body, TraitAnswer.VeryHigh)
                        .with(Trait.Aftertaste, TraitAnswer.VeryHigh),
                rating = 4.7,
                collectionStatus = CollectionStatus.Repurchase,
                price = 95000L,
                place = "바틀샵",
                memo = "말린 무화과, 호두, 카라멜의 달콤하고 농후한 디저트 와인.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 40,
            ),
            DrinkRecord(
                id = 11L,
                type = DrinkType.Wine,
                name = "켄달 잭슨 빈트너스 리저브 카베르네 소비뇽 2020",
                vintage = 2020,
                tags = DrinkTags(wineColor = WineColor.Red, origin = Origin.USA),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.Low)
                        .with(Trait.Acidity, TraitAnswer.Mid)
                        .with(Trait.Tannin, TraitAnswer.Mid)
                        .with(Trait.Body, TraitAnswer.High)
                        .with(Trait.Aftertaste, TraitAnswer.Mid),
                rating = 3.5,
                collectionStatus = CollectionStatus.Normal,
                price = 35000L,
                place = "트레이더스",
                memo = "달큰한 바닐라 오크와 검은 자두, 무난한 데일리.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 45,
            ),
            DrinkRecord(
                id = 12L,
                type = DrinkType.Wine,
                name = "몬테스 클래식 카베르네 소비뇽 2022",
                vintage = 2022,
                tags = DrinkTags(wineColor = WineColor.Red, origin = Origin.Chile),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.Low)
                        .with(Trait.Acidity, TraitAnswer.Mid)
                        .with(Trait.Tannin, TraitAnswer.Mid)
                        .with(Trait.Body, TraitAnswer.Mid)
                        .with(Trait.Aftertaste, TraitAnswer.Low),
                rating = 2.5,
                collectionStatus = CollectionStatus.Normal,
                price = 15000L,
                place = "편의점",
                memo = "피망과 풀 비린내가 도드라지고 거친 알코올감.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 50,
            ),
        )

    val whiskeyRecords: List<DrinkRecord> =
        listOf(
            DrinkRecord(
                id = 101L,
                type = DrinkType.Whiskey,
                name = "발베니 12년 더블우드",
                tags = DrinkTags(whiskyStyle = WhiskyStyle.SingleMalt, origin = Origin.Scotland),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.High)
                        .with(Trait.Body, TraitAnswer.Mid)
                        .with(Trait.Peat, TraitAnswer.VeryLow)
                        .with(Trait.AlcoholBurn, TraitAnswer.Low)
                        .with(Trait.Aftertaste, TraitAnswer.High),
                rating = 4.5,
                collectionStatus = CollectionStatus.Repurchase,
                price = 110000L,
                place = "바틀샵",
                memo = "꿀, 바닐라, 셰리 오크의 달콤함과 부드러운 목넘김.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 3,
            ),
            DrinkRecord(
                id = 102L,
                type = DrinkType.Whiskey,
                name = "라프로익 10년",
                tags = DrinkTags(whiskyStyle = WhiskyStyle.SingleMalt, origin = Origin.Scotland, peat = PeatTag.Peated),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.Low)
                        .with(Trait.Body, TraitAnswer.High)
                        .with(Trait.Peat, TraitAnswer.VeryHigh)
                        .with(Trait.AlcoholBurn, TraitAnswer.Mid)
                        .with(Trait.Aftertaste, TraitAnswer.VeryHigh),
                rating = 4.8,
                collectionStatus = CollectionStatus.Repurchase,
                price = 95000L,
                place = "몰트바",
                memo = "정로환과 요오드, 강렬한 피트 스모크와 짭조름한 바다내음.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 7,
            ),
            DrinkRecord(
                id = 103L,
                type = DrinkType.Whiskey,
                name = "글렌알라키 10년 CS 배치 9",
                tags = DrinkTags(whiskyStyle = WhiskyStyle.SingleMalt, origin = Origin.Scotland),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.VeryHigh)
                        .with(Trait.Body, TraitAnswer.VeryHigh)
                        .with(Trait.Peat, TraitAnswer.VeryLow)
                        .with(Trait.AlcoholBurn, TraitAnswer.High)
                        .with(Trait.Aftertaste, TraitAnswer.VeryHigh),
                rating = 5.0,
                collectionStatus = CollectionStatus.Repurchase,
                price = 165000L,
                place = "바틀샵",
                memo = "58.1도의 폭발적인 도수, 다크 초콜릿과 건포도의 묵직한 셰리 폭탄.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 10,
            ),
            DrinkRecord(
                id = 104L,
                type = DrinkType.Whiskey,
                name = "조니워커 그린 라벨",
                tags =
                    DrinkTags(
                        whiskyStyle = WhiskyStyle.BlendedMalt,
                        origin = Origin.Scotland,
                        peat = PeatTag.Peated,
                    ),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.Mid)
                        .with(Trait.Body, TraitAnswer.High)
                        .with(Trait.Peat, TraitAnswer.Mid)
                        .with(Trait.AlcoholBurn, TraitAnswer.Low)
                        .with(Trait.Aftertaste, TraitAnswer.High),
                rating = 4.6,
                collectionStatus = CollectionStatus.Repurchase,
                price = 78000L,
                place = "트레이더스",
                memo = "탈리스커와 링크우드의 완벽한 조화. 은은한 피트와 신선한 풀향.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 14,
            ),
            DrinkRecord(
                id = 105L,
                type = DrinkType.Whiskey,
                name = "몽키 숄더",
                tags = DrinkTags(whiskyStyle = WhiskyStyle.BlendedMalt, origin = Origin.Scotland),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.High)
                        .with(Trait.Body, TraitAnswer.Mid)
                        .with(Trait.Peat, TraitAnswer.VeryLow)
                        .with(Trait.AlcoholBurn, TraitAnswer.Low)
                        .with(Trait.Aftertaste, TraitAnswer.Mid),
                rating = 4.0,
                collectionStatus = CollectionStatus.Normal,
                price = 52000L,
                place = "이마트",
                memo = "바닐라와 오렌지 지스트, 하이볼과 니트 모두 무난함.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 18,
            ),
            DrinkRecord(
                id = 106L,
                type = DrinkType.Whiskey,
                name = "발렌타인 17년",
                tags = DrinkTags(whiskyStyle = WhiskyStyle.Blended, origin = Origin.Scotland),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.Mid)
                        .with(Trait.Body, TraitAnswer.Mid)
                        .with(Trait.Peat, TraitAnswer.Low)
                        .with(Trait.AlcoholBurn, TraitAnswer.VeryLow)
                        .with(Trait.Aftertaste, TraitAnswer.High),
                rating = 4.2,
                collectionStatus = CollectionStatus.Normal,
                price = 135000L,
                place = "면세점",
                memo = "극강의 밸런스와 부드러움, 과실과 오크의 은은한 여운.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 22,
            ),
            DrinkRecord(
                id = 107L,
                type = DrinkType.Whiskey,
                name = "와일드 터키 레어 브리드",
                tags = DrinkTags(whiskyStyle = WhiskyStyle.Bourbon, origin = Origin.USA),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.High)
                        .with(Trait.Body, TraitAnswer.VeryHigh)
                        .with(Trait.Peat, TraitAnswer.VeryLow)
                        .with(Trait.AlcoholBurn, TraitAnswer.High)
                        .with(Trait.Aftertaste, TraitAnswer.VeryHigh),
                rating = 4.7,
                collectionStatus = CollectionStatus.Repurchase,
                price = 89000L,
                place = "주류상회",
                memo = "58.4도 버번의 묵직한 타격감, 카라멜, 바닐라, 스파이시의 절정.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 28,
            ),
            DrinkRecord(
                id = 108L,
                type = DrinkType.Whiskey,
                name = "불렛 라이",
                tags = DrinkTags(whiskyStyle = WhiskyStyle.Rye, origin = Origin.USA),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.Low)
                        .with(Trait.Body, TraitAnswer.Mid)
                        .with(Trait.Peat, TraitAnswer.VeryLow)
                        .with(Trait.AlcoholBurn, TraitAnswer.Mid)
                        .with(Trait.Aftertaste, TraitAnswer.Mid),
                rating = 3.8,
                collectionStatus = CollectionStatus.Normal,
                price = 62000L,
                place = "칵테일바",
                memo = "알싸한 후추와 허브, 산뜻하고 드라이한 호밀 풍미.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 32,
            ),
            DrinkRecord(
                id = 109L,
                type = DrinkType.Whiskey,
                name = "기원 배치 1",
                tags = DrinkTags(whiskyStyle = WhiskyStyle.SingleMalt, origin = Origin.Korea),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.Mid)
                        .with(Trait.Body, TraitAnswer.High)
                        .with(Trait.Peat, TraitAnswer.Low)
                        .with(Trait.AlcoholBurn, TraitAnswer.Mid)
                        .with(Trait.Aftertaste, TraitAnswer.High),
                rating = 4.5,
                collectionStatus = CollectionStatus.Repurchase,
                price = 150000L,
                place = "증류소 직구매",
                memo = "한국 최초의 싱글몰트, 풍성한 오크 스파이스와 고소한 맥아 풍미.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 38,
            ),
            DrinkRecord(
                id = 110L,
                type = DrinkType.Whiskey,
                name = "카발란 솔리스트 비노바리끄",
                tags = DrinkTags(whiskyStyle = WhiskyStyle.SingleMalt, origin = Origin.Taiwan),
                taste =
                    TasteInput()
                        .with(Trait.Sweetness, TraitAnswer.VeryHigh)
                        .with(Trait.Body, TraitAnswer.VeryHigh)
                        .with(Trait.Peat, TraitAnswer.VeryLow)
                        .with(Trait.AlcoholBurn, TraitAnswer.VeryHigh)
                        .with(Trait.Aftertaste, TraitAnswer.VeryHigh),
                rating = 4.9,
                collectionStatus = CollectionStatus.Repurchase,
                price = 280000L,
                place = "대만 여행",
                memo = "STR 와인 캐스크의 열대과일과 멜론, 짙은 다크초콜릿의 폭발적 풍미.",
                recordedAtMillis = System.currentTimeMillis() - 86400000L * 42,
            ),
        )

    val allRecords: List<DrinkRecord> = wineRecords + whiskeyRecords
}
