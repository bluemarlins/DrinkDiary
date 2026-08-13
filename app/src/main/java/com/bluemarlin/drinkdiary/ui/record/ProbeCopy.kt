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
                        question = "달았나요?",
                        lowLabel = "드라이했어요",
                        highLabel = "달았어요",
                    )
                DrinkType.Whiskey ->
                    ProbeCopy(
                        question = "달큰했나요?",
                        lowLabel = "드라이했어요",
                        highLabel = "달았어요",
                    )
            }
        Trait.Body ->
            ProbeCopy(
                question = "묵직했나요?",
                lowLabel = "가벼웠어요",
                highLabel = "묵직했어요",
            )
        Trait.Intensity ->
            when (type) {
                DrinkType.Wine ->
                    ProbeCopy(
                        question = "향이 진했나요?",
                        lowLabel = "은은했어요",
                        highLabel = "진했어요",
                    )
                DrinkType.Whiskey ->
                    ProbeCopy(
                        question = "향이 강했나요?",
                        lowLabel = "은은했어요",
                        highLabel = "진했어요",
                    )
            }
        Trait.Aftertaste ->
            ProbeCopy(
                question = "여운이 길었나요?",
                lowLabel = "금방 사라졌어요",
                highLabel = "오래 남았어요",
            )
        Trait.Acidity ->
            ProbeCopy(
                question = "산미가 느껴졌나요?",
                lowLabel = "부드러웠어요",
                highLabel = "산뜻했어요",
            )
        Trait.Tannin ->
            ProbeCopy(
                question = "떫었나요?",
                lowLabel = "안 떫었어요",
                highLabel = "떫었어요",
            )
        Trait.Peat ->
            ProbeCopy(
                question = "스모키했나요?",
                lowLabel = "안 났어요",
                highLabel = "스모키했어요",
            )
        Trait.AlcoholBurn ->
            ProbeCopy(
                question = "알코올이 화끈했나요?",
                lowLabel = "부드러웠어요",
                highLabel = "화끈했어요",
            )
    }
