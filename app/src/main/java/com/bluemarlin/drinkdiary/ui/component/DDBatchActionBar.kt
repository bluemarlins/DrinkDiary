package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// 명세 1절 3번의 "Selection Mode 일괄 작업 바". 지금 담는 작업은 삭제 하나뿐이다 —
// prd.md F1-2가 요구하는 것이 그것뿐이고, 쓰지 않을 자리를 미리 만들어두지 않는다.
//
// 지우기가 **destructive 버튼**인 것이 중요하다. 되돌리기가 없어서(사용자 확정) 이 버튼과
// 그다음 확인 팝업이 방어선의 전부다.
@Composable
fun DDBatchActionBar(
    selectedCount: Int,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
                    .padding(horizontal = DrinkDiarySpacing.md, vertical = DrinkDiarySpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${selectedCount}개 선택",
                style = MaterialTheme.typography.titleMedium,
            )
            DDDestructiveButton(text = "지우기", onClick = onDelete)
        }
    }
}
