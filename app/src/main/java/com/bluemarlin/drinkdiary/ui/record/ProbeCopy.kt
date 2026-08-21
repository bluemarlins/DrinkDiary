package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.runtime.Composable
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer

data class ProbeCopy(
    val question: String,
    val veryLowLabel: String,
    val lowLabel: String,
    val midLabel: String = "보통이었어요",
    val highLabel: String,
    val veryHighLabel: String,
) {
    val options: List<Pair<TraitAnswer, String>>
        get() =
            listOf(
                TraitAnswer.VeryLow to veryLowLabel,
                TraitAnswer.Low to lowLabel,
                TraitAnswer.Mid to midLabel,
                TraitAnswer.High to highLabel,
                TraitAnswer.VeryHigh to veryHighLabel,
            )
}

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
                        veryLowLabel = "매우 드라이했어요",
                        lowLabel = "드라이했어요",
                        midLabel = "은은한 단맛 / 보통",
                        highLabel = "달콤했어요",
                        veryHighLabel = "매우 달콤했어요",
                    )
                DrinkType.Whiskey ->
                    ProbeCopy(
                        question = "단맛은 어땠나요?",
                        veryLowLabel = "매우 드라이했어요",
                        lowLabel = "드라이했어요",
                        midLabel = "은은한 단맛 / 보통",
                        highLabel = "달큰했어요",
                        veryHighLabel = "매우 달았어요",
                    )
            }
        Trait.Acidity ->
            ProbeCopy(
                question = "산미(신맛)는 어땠나요?",
                veryLowLabel = "산미가 거의 없었어요",
                lowLabel = "부드럽고 둥글었어요",
                midLabel = "적당히 산뜻했어요",
                highLabel = "새콤하고 선명했어요",
                veryHighLabel = "짜릿하게 높았어요",
            )
        Trait.Tannin ->
            ProbeCopy(
                question = "떫은맛(탄닌)은 어땠나요?",
                veryLowLabel = "매끄럽고 떫지 않았어요",
                lowLabel = "부드러운 편이었어요",
                midLabel = "적당히 느껴졌어요",
                highLabel = "떫고 묵직했어요",
                veryHighLabel = "입안이 꽉 조였어요",
            )
        Trait.Body ->
            when (type) {
                DrinkType.Wine ->
                    ProbeCopy(
                        question = "바디감(무게감)은 어땠나요?",
                        veryLowLabel = "아주 가벼웠어요",
                        lowLabel = "가벼운 편이었어요",
                        midLabel = "중간 무게감이었어요",
                        highLabel = "묵직하고 꽉 찼어요",
                        veryHighLabel = "아주 묵직했어요",
                    )
                DrinkType.Whiskey ->
                    ProbeCopy(
                        question = "바디감(질감)은 어땠나요?",
                        veryLowLabel = "가볍고 산뜻했어요",
                        lowLabel = "부드러운 편이었어요",
                        midLabel = "적당히 오일리했어요",
                        highLabel = "묵직하고 오일리했어요",
                        veryHighLabel = "아주 진득하고 묵직했어요",
                    )
            }
        Trait.Peat ->
            ProbeCopy(
                question = "스모키·피트향이 났나요?",
                veryLowLabel = "전혀 안 났어요",
                lowLabel = "은은하게 스쳤어요",
                midLabel = "적당히 피티했어요",
                highLabel = "스모키·피트향이 강했어요",
                veryHighLabel = "강렬한 피트 폭탄이었어요",
            )
        Trait.AlcoholBurn ->
            ProbeCopy(
                question = "알코올 자극(부즈)은 어땠나요?",
                veryLowLabel = "알코올 느낌 없이 순했어요",
                lowLabel = "부드럽게 넘어갔어요",
                midLabel = "적당한 온열감이었어요",
                highLabel = "화끈하고 찌릿했어요",
                veryHighLabel = "아주 강렬하게 타올랐어요",
            )
        Trait.Aftertaste ->
            ProbeCopy(
                question = "여운(피니시)은 길었나요?",
                veryLowLabel = "마시자마자 끝났어요",
                lowLabel = "비교적 깔끔했어요",
                midLabel = "적당히 맴돌았어요",
                highLabel = "오래 머물렀어요",
                veryHighLabel = "아주 길게 계속 남았어요",
            )
        Trait.Intensity ->
            ProbeCopy(
                question = "향과 풍미의 강도는 어땠나요?",
                veryLowLabel = "아주 은은하고 연했어요",
                lowLabel = "은은한 편이었어요",
                midLabel = "적당했어요",
                highLabel = "풍부하고 진했어요",
                veryHighLabel = "폭발적으로 진했어요",
            )
    }
