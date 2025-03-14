package com.bluemarlin.drinkdiary.ui.screens

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun WineRatingBar(
    rating: Float,  // ⭐ 외부에서 값 전달받음
    onRatingChange: (Float) -> Unit  // ⭐ 변경된 값 전달하는 콜백
) {
    val maxRating = 5
    val stepSize = 0.2f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Rating: ${String.format("%.1f", rating)}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val starWidth = size.width / maxRating
                        val newRating = (offset.x / starWidth) * stepSize
                        val roundedRating = (newRating * (1 / stepSize)).roundToInt() * stepSize
                        onRatingChange(
                            roundedRating.coerceIn(
                                0f,
                                maxRating.toFloat()
                            )
                        ) // ⭐ 부모로 값 전달
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val starWidth = size.width / maxRating
                        val newRating = rating + (dragAmount.x / starWidth) * stepSize
                        onRatingChange(newRating.coerceIn(0f, maxRating.toFloat())) // ⭐ 부모로 값 전달
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until maxRating) {
                val starFill = when {
                    rating > i + 0.8f -> 1f // 완전 채워진 별
                    rating > i + 0.2f -> 0.5f // 반 채워진 별
                    else -> 0f // 빈 별
                }
                Icon(
                    imageVector = when (starFill) {
                        1f -> Icons.Filled.Star
                        // TODO: Need to add proper images
                        0.5f -> Icons.Filled.ThumbUp
                        else -> Icons.Outlined.Star
                    },
                    contentDescription = "Rating Star",
                    tint = Color(0xFFFFD700), // 금색 별
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}