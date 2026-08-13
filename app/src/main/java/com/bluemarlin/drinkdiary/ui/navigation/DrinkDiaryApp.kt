package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bluemarlin.drinkdiary.ui.record.RecordFlow

// 재정의 진행 중 — 현재는 F2(취향 입력) 검증 흐름만 붙어 있다.
// profile/collection/lookup/share/settings 는 software-architecture.md 6절에 따라 이어 붙인다.
@Composable
fun DrinkDiaryApp(modifier: Modifier = Modifier) {
    // enableEdgeToEdge() 상태이므로 앱이 직접 시스템 바 영역을 피해야 한다.
    // 이걸 빼면 상단 진행 표시가 상태바 뒤로 숨는다(실기기에서 확인된 결함).
    Surface(modifier = modifier.fillMaxSize()) {
        RecordFlow(
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        )
    }
}
