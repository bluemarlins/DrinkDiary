package com.bluemarlin.drinkdiary.domain.usecase

// 선호 판정 임계치. 실사용 데이터로 튜닝할 값이며, 지금 값은 시뮬레이션에서 고른 초기값이다.
//
// 구 임계치(MIN_SAMPLES_PER_SIDE=3, MIN_RATING_GAP=0.5)는 표본이 늘수록 관문이 열리고
// 그다음엔 노이즈만으로 격차가 나서, **없는 취향을 지어내는 비율이 기록과 함께 올라갔다**
// (10잔 8.6% → 20잔 49.6% → 40잔 61.1%). 상관 기반은 그 값이 꺾인다(40잔 42.4%).
object TasteThresholds {
    // 축 하나를 판단하기 위한 최소 기록 수(그 축에 답이 달린 기록).
    const val MIN_SAMPLES = 6

    // 이 미만의 상관은 중립으로 본다. 낮추면 유형이 빨리 나오지만 없는 취향을 더 지어낸다.
    const val MIN_CORRELATION = 0.45
}
