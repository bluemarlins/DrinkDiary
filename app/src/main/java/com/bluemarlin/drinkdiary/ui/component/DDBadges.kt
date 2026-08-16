package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.ui.DrinkLabels

@Composable
fun DDDrinkBadge(
    drinkType: DrinkType,
    label: String? = null,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor, defaultLabel) =
        when (drinkType) {
            DrinkType.Wine ->
                Triple(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.tertiary,
                    "와인",
                )
            DrinkType.Whiskey ->
                Triple(
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.secondary,
                    "위스키",
                )
        }

    val displayLabel = if (label.isNullOrBlank()) defaultLabel else "$defaultLabel · $label"

    DDSemanticBadge(
        text = displayLabel,
        containerColor = bgColor,
        contentColor = textColor,
        modifier = modifier,
    )
}

// 문구를 파라미터로 받지 않는다. 기본값이 `"★ 다시 살래요"`였는데 `DrinkLabels`는 같은 상태를
// `"또 살래요"`라고 불러서, 목록과 상세가 한 상태를 두 이름으로 말했다. `★`도 뺐다 —
// 명세 2-4의 아이콘 규격(24×24dp 벡터) 밖에 있는 문자 아이콘이다.
@Composable
fun DDRepurchaseBadge(modifier: Modifier = Modifier) {
    DDSemanticBadge(
        text = DrinkLabels.collectionStatus(CollectionStatus.Repurchase),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
fun DDSemanticBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(MaterialTheme.shapes.small)
                .background(containerColor)
                .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}
