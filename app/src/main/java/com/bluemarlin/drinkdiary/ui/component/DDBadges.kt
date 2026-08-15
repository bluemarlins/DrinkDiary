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
import com.bluemarlin.drinkdiary.domain.model.DrinkType

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

@Composable
fun DDRepurchaseBadge(
    modifier: Modifier = Modifier,
    text: String = "★ 다시 살래요",
) {
    DDSemanticBadge(
        text = text,
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
