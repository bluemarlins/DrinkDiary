package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// 첫 기록 **직후**에 한 번만 묻는다.
//
// 첫 기록 '전'에 묻지 않는 이유는 prd.md S1이다 — "온보딩이 길면 여기서 이탈한다".
// 그리고 그 시점의 사용자는 피트가 뭔지도 모른다. 한 번 겪고 나면 무엇을 묻는 건지 알고,
// 그때는 이미 가치를 받은 뒤라 마찰로 느껴지지 않는다.
//
// 기록 1건만 이 설정 이전이라 표본이 어긋나는 폭이 무시할 수준이다 —
// 나중에 설정에서 바꾸는 방식은 이 문제가 훨씬 크다. 그래서 설정에도 같은 경고를 적어 뒀다.
//
// 목록은 promotableTags 한 곳에서 온다 — 설정 화면과 갈라지면 여기서 켠 것을 끌 수 없게 된다.

@Composable
fun TagPreferencePrompt(
    onConfirm: (Set<TagCategory>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf(emptySet<TagCategory>()) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("앞으로 이런 것도 매번 물어볼까요?", style = MaterialTheme.typography.titleMedium)
        Text(
            // 설정 화면이 생겼으므로 이제 이 약속을 지킬 수 있다. 없는 화면을 약속하지 않는다.
            text = "고르지 않으면 지금처럼 '더 남기기' 안에 그대로 있어요. 설정에서 언제든 바꿀 수 있어요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        promotableTags.forEach { (category, hint) ->
            Column(verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xxs)) {
                FilterChip(
                    selected = category in selected,
                    onClick = {
                        selected = if (category in selected) selected - category else selected + category
                    },
                    label = { Text(DrinkLabels.tagCategory(category)) },
                )
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Button(onClick = { onConfirm(selected) }, modifier = Modifier.fillMaxWidth()) {
            // 아무것도 안 고른 것도 대답이다 — 이 버튼을 누르면 다시 묻지 않는다.
            Text(if (selected.isEmpty()) "이대로 할게요" else "이렇게 물어봐 주세요")
        }
    }
}
