package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.runtime.Composable
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Trait

data class ProbeCopy(
    val question: String,
    val lowLabel: String,
    val highLabel: String,
)

@Composable
fun probeCopy(
    type: DrinkType,
    trait: Trait,
): ProbeCopy =
    when (trait) {
        Trait.Sweetness ->
            when (type) {
                DrinkType.Wine ->
                    ProbeCopy(
                        question = "당도는 어땠나요?",
                        lowLabel = "드라이했어요",
                        highLabel = "달콤했어요",
                    )
                DrinkType.Whiskey ->
                    ProbeCopy(
                        question = "단맛은 어땠나요?",
                        lowLabel = "드라이했어요",
                        highLabel = "달큰했어요",
                    )
            }
        Trait.Acidity ->
            ProbeCopy(
                question = "산미가 느껴졌나요?",
                lowLabel = "부드러웠어요",
                highLabel = "산뜻·새콤했어요",
            )
        Trait.Tannin ->
            ProbeCopy(
                question = "떫은맛(탄닌)은 어땠나요?",
                lowLabel = "부드러웠어요",
                highLabel = "떫고 묵직했어요",
            )
        Trait.Body ->
            when (type) {
                DrinkType.Wine ->
                    ProbeCopy(
                        question = "바디감(무게감)은 어땠나요?",
                        lowLabel = "가벼웠어요",
                        highLabel = "묵직했어요",
                    )
                DrinkType.Whiskey ->
                    ProbeCopy(
                        question = "바디감(질감)은 어땠나요?",
                        lowLabel = "가벼웠어요",
                        highLabel = "오일리·묵직했어요",
                    )
            }
        Trait.Peat ->
            ProbeCopy(
                question = "스모키·피트향이 났나요?",
                lowLabel = "안 났어요",
                highLabel = "스모키·피티했어요",
            )
        Trait.AlcoholBurn ->
            ProbeCopy(
                question = "알코올 자극(부즈)은 어땠나요?",
                lowLabel = "부드러웠어요",
                highLabel = "화끈·자극적이었어요",
            )
        Trait.Aftertaste ->
            ProbeCopy(
                question = "여운(피니시)은 길었나요?",
                lowLabel = "깔끔하게 끝났어요",
                highLabel = "오래 맴돌았어요",
            )
        Trait.Intensity ->
            ProbeCopy(
                question = "향이 진했나요?",
                lowLabel = "은은했어요",
                highLabel = "진했어요",
            )
    }
