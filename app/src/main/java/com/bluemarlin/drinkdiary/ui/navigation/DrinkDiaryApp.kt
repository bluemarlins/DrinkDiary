package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bluemarlin.drinkdiary.ui.record.RecordFlow

// 재정의 진행 중 — 현재는 F2(취향 입력) 검증 흐름만 붙어 있다.
// profile/collection/lookup/share/settings 는 software-architecture.md 6절에 따라 이어 붙인다.
@Composable
fun DrinkDiaryApp(modifier: Modifier = Modifier) {
    RecordFlow(modifier = modifier)
}
