package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// **이 컴포넌트가 있는 이유는 `heightIn(min = 48.dp)` 한 줄이다.**
//
// Material3 1.3.0의 칩은 `FilterChipTokens.ContainerHeight = 32dp`를 그대로 최종 높이로 쓰고
// `minimumInteractiveComponentSize`를 적용하지 않는다. 명세 2절 5번은 48×48dp 보장은 물론
// "36dp 미만 배치 금지"라고 쓰는데, 화면마다 생 `FilterChip`을 쓰는 동안 다섯 곳이 전부 32dp로 샜다.
// 규칙은 걸 자리가 있어야 지켜진다.
//
// 색도 명시한다. M3 기본값은 선택 상태에 `secondaryContainer`를 쓰는데 우리 매핑에서 그건
// 위스키 앰버다. 명세 3.1절은 `PrimaryContainer`를 "선택된 칩 배경",
// `SurfaceSunk`(= `surfaceVariant`)를 "칩 미선택 상태"로 정한다.
@Composable
fun DDChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        modifier = modifier.heightIn(min = 48.dp),
        shape = MaterialTheme.shapes.small,
        colors =
            FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.primary,
            ),
        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary,
            ),
    )
}

// 명세 5.2절 `DDTagChipGroup`. 해제(선택 취소)는 여기서 다루지 않는다 — 축마다 규칙이 다르고
// (라벨 태그는 다시 누르면 해제, 취향 편집은 해제 없음), 그 판단은 화면이 한다.
@Composable
fun <T> DDTagChipGroup(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs),
        verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs),
    ) {
        options.forEach { option ->
            DDChip(
                label = label(option),
                selected = selected == option,
                onClick = { onSelect(option) },
            )
        }
    }
}
