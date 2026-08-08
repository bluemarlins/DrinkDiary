package com.bluemarlin.drinkdiary.debug

import android.content.Context
import android.net.Uri
import com.bluemarlin.drinkdiary.AppContainer
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordInput
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

// Debug-build-only dummy data (30 well-known wines/whiskeys/beers with agy-generated
// original illustrations, not scraped label photos — see app/docs/dev/seed-data.md) so
// new UI/components can be previewed with realistic-looking content instead of an empty
// database. This class lives only in the `debug` source set — app/src/release/.../
// debug/DebugSeeder.kt provides a no-op counterpart for release builds, since Kotlin
// source sets are additive per variant rather than override-based.
object DebugSeeder {
    private const val DAY_MILLIS = 24L * 60 * 60 * 1000

    suspend fun seedIfNeeded(context: Context, appContainer: AppContainer) {
        val alreadySeeded = appContainer.observeDrinkRecordsUseCase(DrinkRecordFilter()).first().isNotEmpty()
        if (alreadySeeded) return

        val now = System.currentTimeMillis()
        SEED_RECORDS.forEach { seed ->
            appContainer.saveDrinkRecordUseCase(
                DrinkRecordInput(
                    type = seed.type,
                    name = seed.name,
                    imageUri = copySeedImage(context, seed.imageAsset),
                    priceText = seed.price.toString(),
                    tastingNote = seed.tastingNote,
                    rating = seed.rating,
                    collectionStatus = seed.collectionStatus,
                    recordedAtMillis = now - seed.daysAgo * DAY_MILLIS,
                ),
            )
        }
    }

    // Mirrors DDImagePicker's copyImageToInternalStorage (Components.kt) so seeded
    // images are stored exactly like a real user-picked photo would be.
    private suspend fun copySeedImage(context: Context, assetName: String): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val imageDir = File(context.filesDir, "drink_record_images").apply { mkdirs() }
                val imageFile = File(imageDir, "${UUID.randomUUID()}.jpg")
                context.assets.open("seed_images/$assetName").use { input ->
                    imageFile.outputStream().use { output -> input.copyTo(output) }
                }
                Uri.fromFile(imageFile).toString()
            }.getOrNull()
        }

    private data class Seed(
        val type: DrinkType,
        val name: String,
        val price: Long,
        val tastingNote: String,
        val imageAsset: String,
        val rating: Double,
        val collectionStatus: CollectionStatus,
        val daysAgo: Long,
    )

    // Interleaved wine/whiskey/beer order, daysAgo spread 0..87 (~3 months) so the
    // Dashboard calendar's current-week/current-month coverage is meaningful regardless
    // of when this actually runs. Data researched by agy — see app/docs/dev/seed-data.md.
    private val SEED_RECORDS = listOf(
        Seed(DrinkType.Wine, "몬테스 알파 카베르네 소비뇽", 40000,
            "바디감이 묵직하고 블랙베리와 자두 향이 진하다. 데일리로 마시기 부담 없는 무난한 와인.",
            "wine_01.jpg", 4.5, CollectionStatus.Repurchase, 0),
        Seed(DrinkType.Whiskey, "조니워커 블랙 라벨 12년", 50000,
            "적당한 스모키함과 달달한 바닐라 향의 조화. 하이볼로 마셔도 좋고 니트로 마셔도 부드럽다.",
            "whiskey_01.jpg", 3.5, CollectionStatus.Normal, 3),
        Seed(DrinkType.Beer, "기네스 드래프트", 3000,
            "부드럽고 크리미한 거품이 예술이다. 쌉쌀한 커피와 다크 초콜릿 풍미가 느껴지는 완벽한 흑맥주.",
            "beer_01.jpg", 5.0, CollectionStatus.Repurchase, 6),
        Seed(DrinkType.Wine, "1865 싱글 빈야드 카베르네 소비뇽", 45000,
            "풀바디의 묵직함과 부드러운 타닌이 돋보인다. 끝에 남는 은은한 바닐라 향이 매력적이다.",
            "wine_02.jpg", 2.5, CollectionStatus.NotForMe, 9),
        Seed(DrinkType.Whiskey, "맥캘란 12년 셰리 오크", 150000,
            "꾸덕한 건과일과 셰리 와인의 달큰한 향이 매력적이다. 스파이시함 없이 부드럽게 넘어가는 고급스러운 맛.",
            "whiskey_02.jpg", 4.0, CollectionStatus.Normal, 12),
        Seed(DrinkType.Beer, "호가든", 3000,
            "오렌지 껍질과 고수 씨앗이 주는 상큼하고 향긋한 풍미. 밀맥주 특유의 부드러움이 산뜻하게 다가온다.",
            "beer_02.jpg", 4.5, CollectionStatus.Repurchase, 15),
        Seed(DrinkType.Wine, "샤토 딸보 2019", 150000,
            "클래식한 보르도 와인의 정석. 가죽 향과 흙내음, 붉은 과실 향이 복합적으로 어우러져 고급스럽다.",
            "wine_03.jpg", 3.5, CollectionStatus.Normal, 18),
        Seed(DrinkType.Whiskey, "발베니 12년 더블우드", 120000,
            "꿀 같은 달콤함과 은은한 바닐라 향이 일품. 셰리 캐스크의 여운이 기분 좋게 남아 입문용으로 최고다.",
            "whiskey_03.jpg", 5.0, CollectionStatus.Repurchase, 21),
        Seed(DrinkType.Beer, "하이네켄", 3000,
            "특유의 쌉싸름함과 톡 쏘는 탄산이 매력적인 라거. 피자나 튀김과 함께할 때 가장 빛을 발한다.",
            "beer_03.jpg", 2.5, CollectionStatus.NotForMe, 24),
        Seed(DrinkType.Wine, "투핸즈 엔젤스 쉐어 쉬라즈", 60000,
            "호주 쉬라즈 특유의 진한 과일 잼 향과 스파이시함이 훅 들어온다. 고기와 정말 잘 어울리는 녀석.",
            "wine_04.jpg", 4.0, CollectionStatus.Normal, 27),
        Seed(DrinkType.Whiskey, "글렌피딕 15년", 140000,
            "과일의 상큼함과 벌꿀의 달콤함, 그리고 약간의 스파이시함이 복합적이다. 밸런스가 정말 훌륭한 싱글몰트.",
            "whiskey_04.jpg", 4.5, CollectionStatus.Repurchase, 30),
        Seed(DrinkType.Beer, "아사히 수퍼드라이", 3000,
            "이름 그대로 깔끔하고 드라이하게 떨어지는 끝맛. 목넘김이 시원해서 더운 여름날 벌컥벌컥 마시기 최고.",
            "beer_04.jpg", 3.5, CollectionStatus.Normal, 33),
        Seed(DrinkType.Wine, "돔 페리뇽 빈티지", 350000,
            "섬세하고 끊임없이 올라오는 기포가 예술이다. 고소한 브리오슈와 상큼한 시트러스 향이 입안을 꽉 채운다.",
            "wine_05.jpg", 5.0, CollectionStatus.Repurchase, 36),
        Seed(DrinkType.Whiskey, "짐빔 화이트 라벨", 35000,
            "버번 특유의 바닐라와 캐러멜 향이 직관적으로 다가온다. 가성비가 좋아 콜라와 섞어 버번콕으로 마시기 훌륭하다.",
            "whiskey_05.jpg", 2.5, CollectionStatus.NotForMe, 39),
        Seed(DrinkType.Beer, "칭따오", 3000,
            "청량하고 깔끔한 맛이 양꼬치는 물론이고 매콤한 음식과 찰떡궁합. 탄산감이 좋고 가벼운 라거다.",
            "beer_05.jpg", 4.0, CollectionStatus.Normal, 42),
        Seed(DrinkType.Wine, "클라우디 베이 소비뇽 블랑", 55000,
            "잔에 따르자마자 퍼지는 청사과와 패션후르츠 향이 상쾌하다. 산미가 톡톡 튀어 여름에 마시기 딱 좋다.",
            "wine_06.jpg", 4.5, CollectionStatus.Repurchase, 45),
        Seed(DrinkType.Whiskey, "메이커스 마크", 60000,
            "밀을 사용해서 그런지 일반 버번보다 부드럽고 달콤하다. 붉은 밀랍이 시각적으로도 즐거움을 주는 최애 버번.",
            "whiskey_06.jpg", 3.5, CollectionStatus.Normal, 48),
        Seed(DrinkType.Beer, "빅 웨이브 골든 에일", 4000,
            "열대 과일의 달콤한 향이 코를 찌르지만 맛은 의외로 깔끔하고 시원하다. 휴양지 해변에 있는 듯한 기분.",
            "beer_06.jpg", 5.0, CollectionStatus.Repurchase, 51),
        Seed(DrinkType.Wine, "모엣 샹동 임페리얼", 85000,
            "언제 마셔도 기분 좋은 축배의 맛. 청사과와 감귤류의 풋풋함에 약간의 비스킷 향이 더해져 밸런스가 훌륭하다.",
            "wine_07.jpg", 2.5, CollectionStatus.NotForMe, 54),
        Seed(DrinkType.Whiskey, "산토리 가쿠빈", 45000,
            "하이볼의 영원한 스탠다드. 달콤한 향과 드라이한 끝맛 덕분에 탄산수와 레몬을 곁들이면 최고의 청량감을 선사한다.",
            "whiskey_07.jpg", 4.0, CollectionStatus.Normal, 57),
        Seed(DrinkType.Beer, "블루문", 3500,
            "오렌지를 곁들여 마시면 향긋함이 두 배가 된다. 달콤하면서도 약간의 스파이시함이 있는 매력적인 밀맥주.",
            "beer_07.jpg", 4.5, CollectionStatus.Repurchase, 60),
        Seed(DrinkType.Wine, "캔달 잭슨 빈트너스 리저브 샤르도네", 45000,
            "오크통 숙성에서 오는 버터, 바닐라 향이 아주 찐하다. 묵직한 화이트 와인이 당길 때 최고의 선택.",
            "wine_08.jpg", 3.5, CollectionStatus.Normal, 63),
        Seed(DrinkType.Whiskey, "야마자키 12년", 350000,
            "은은한 꽃향기와 과일 향, 그리고 일본 특유의 단정한 오크 향이 조화롭다. 구하기 힘들어서 그렇지 맛은 정말 훌륭하다.",
            "whiskey_08.jpg", 5.0, CollectionStatus.Repurchase, 66),
        Seed(DrinkType.Beer, "블랑 1664", 3000,
            "오렌지와 시트러스 향이 굉장히 풍부하고 상큼하다. 맥주 특유의 쓴맛이 적어 부담 없이 즐기기 좋은 밀맥주.",
            "beer_08.jpg", 2.5, CollectionStatus.NotForMe, 69),
        Seed(DrinkType.Wine, "이기갈 꼬뜨 뒤 론 루즈", 30000,
            "가성비 론 와인의 대명사. 붉은 베리류의 상큼함과 후추 같은 스파이시함이 가볍게 즐기기 좋다.",
            "wine_09.jpg", 4.0, CollectionStatus.Normal, 72),
        Seed(DrinkType.Whiskey, "라프로익 10년", 110000,
            "병원 소독약 같은 강렬한 피트 향이 처음엔 당황스럽지만, 마시다 보면 그 짭짤함과 훈제 향에 중독된다.",
            "whiskey_09.jpg", 4.5, CollectionStatus.Repurchase, 75),
        Seed(DrinkType.Beer, "스텔라 아르투아", 3000,
            "쌉쌀하면서도 고소한 몰트 향이 깔끔하게 어우러진다. 밸런스가 좋아 어떤 안주와도 무난하게 잘 어울린다.",
            "beer_09.jpg", 3.5, CollectionStatus.Normal, 78),
        Seed(DrinkType.Wine, "오퍼스 원 2018", 800000,
            "압도적인 스케일과 우아함. 블랙베리, 다크 초콜릿, 에스프레소 향이 겹겹이 느껴지며 여운이 끝없이 이어진다.",
            "wine_10.jpg", 5.0, CollectionStatus.Repurchase, 81),
        Seed(DrinkType.Whiskey, "우드포드 리저브", 120000,
            "캐러멜, 다크 초콜릿, 스파이시한 향이 묵직하게 다가오는 프리미엄 버번. 니트로 천천히 음미하기 좋다.",
            "whiskey_10.jpg", 2.5, CollectionStatus.NotForMe, 84),
        Seed(DrinkType.Beer, "구스 아일랜드 IPA", 3500,
            "쌉싸름한 홉의 풍미와 시트러스한 과일 향이 진하게 느껴지는 IPA. 향긋하면서도 펀치감이 있어 매력적이다.",
            "beer_10.jpg", 4.0, CollectionStatus.Normal, 87),
    )
}
