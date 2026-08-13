package com.bluemarlin.drinkdiary.domain.usecase

// 선호 판정 임계치. 실사용 데이터로 튜닝할 값이며, 지금 값은 근거 없는 초기 추정이다.
// 낮추면 유형이 빨리 나오지만 틀릴 확률이 오르고, 올리면 정확하지만 오래 걸린다.
// 틀린 유형은 앱 전체의 판단을 의심받게 하므로 초기값은 보수적으로 잡았다.
object TasteThresholds {
    // 한 방향(High/Low)당 최소 표본 수. 양쪽 모두 충족해야 판정을 시도한다.
    const val MIN_SAMPLES_PER_SIDE = 3

    // 유의미하다고 볼 평균 만족도 차이 (평점 0.0~5.0 기준).
    const val MIN_RATING_GAP = 0.5
}
