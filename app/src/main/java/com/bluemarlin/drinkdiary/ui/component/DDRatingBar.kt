package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// 눈금도 축도 없는 막대 하나. 읽을 것이 "어느 쪽이 더 긴가" 하나뿐이라 그 이상은
// 입문자에게 해석 부담만 준다(이번 달 회고의 주종 막대와 같은 판단).
@Composable
internal fun DDBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(BAR_HEIGHT)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                // 길이는 옆에 적힌 숫자가 이미 말한다. 스크린 리더가 같은 것을 두 번 읽을 이유가 없다.
                .clearAndSetSemantics { },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(BAR_HEIGHT)
                    .clip(MaterialTheme.shapes.small)
                    .background(color),
        )
    }
}

// 라벨별 만족도 한 줄(prd.md F3-4 (b)).
//
// **막대가 나타내는 것은 평균 만족도이지 빈도가 아니다.** 선택 비율을 막대로 그리면
// 벤치마크에서 이미 반려한 "달콤함 75%"가 되고, 화면이 근거라고 내놓는 것이 실제 판정
// 근거와 갈라진다.
//
// 색은 두 가지뿐이다. 대조에 뽑힌 줄만 강조색을 쓰고 나머지는 중립색이다 —
// 액센트를 늘리면 design-system.md 2절의 "한 화면 내 액센트 최대 2개"가 무너진다.
@Composable
fun DDRatingBar(
    label: String,
    value: String,
    fraction: Float,
    modifier: Modifier = Modifier,
    emphasised: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xxs),
    ) {
        // **점수를 오른쪽 끝에 두지 않는다.** 거기는 FAB가 떠 있는 자리다 — 표본 수를 왼쪽으로
        // 옮긴 것과 같은 이유이고, 그때 점수만 남겨 둔 탓에 같은 함정을 다시 밟았다
        // (에뮬레이터에서 확인: "최근 5잔 … 2.4점"의 점수가 FAB에 가려짐).
        // 길이 비교는 어차피 막대가 하므로 숫자를 굳이 열 맞춰 세울 이유가 없다.
        Text(
            text = "$label · $value",
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (emphasised) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
        DDBar(
            fraction = fraction,
            color =
                if (emphasised) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
        )
    }
}

private val BAR_HEIGHT = 6.dp
