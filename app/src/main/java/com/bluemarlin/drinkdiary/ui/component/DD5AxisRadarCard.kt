package com.bluemarlin.drinkdiary.ui.component

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing
import kotlin.math.cos
import kotlin.math.sin

data class RadarAxisData(
    val label: String,
    val value: Double, // 1.0 ~ 5.0
)

@Composable
fun DD5AxisRadarCard(
    axes: List<RadarAxisData>,
    accentColor: Color,
    subTitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(DrinkDiarySpacing.md),
            verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs),
                ) {
                    Text("🕸️", fontSize = 15.sp)
                    Text("5축 테이스팅 레이더", style = MaterialTheme.typography.titleMedium)
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ) {
                    Text(
                        text = subTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            if (axes.isEmpty()) {
                Text(
                    text = "아직 테이스팅 기록이 충분하지 않아요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = DrinkDiarySpacing.lg),
                )
            } else {
                RadarChart(
                    axes = axes,
                    accentColor = accentColor,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = DrinkDiarySpacing.xs),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    axes.forEach { axis ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = axis.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                            )
                            Text(
                                text = if (axis.value > 0) String.format("%.1f", axis.value) else "-",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RadarChart(
    axes: List<RadarAxisData>,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = (size.minDimension / 2f) - 36.dp.toPx()
        val numAxes = axes.size
        if (numAxes < 3) return@Canvas

        // 1. 거미줄 배경 격자선 (1~5 레벨)
        for (level in 1..5) {
            val levelRadius = (radius / 5f) * level
            val webPath = Path()
            for (i in 0 until numAxes) {
                val angle = (Math.PI * 2 / numAxes) * i - Math.PI / 2
                val x = centerX + levelRadius * cos(angle).toFloat()
                val y = centerY + levelRadius * sin(angle).toFloat()
                if (i == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
            }
            webPath.close()
            drawPath(
                path = webPath,
                color = if (level == 5) outlineColor.copy(alpha = 0.6f) else outlineColor.copy(alpha = 0.2f),
                style = Stroke(width = if (level == 5) 1.5.dp.toPx() else 1.dp.toPx()),
            )
        }

        // 2. 축 선 & 라벨
        val textPaint =
            Paint().apply {
                color = onSurfaceVariant.toArgb()
                textSize = 12.sp.toPx()
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

        for (i in 0 until numAxes) {
            val angle = (Math.PI * 2 / numAxes) * i - Math.PI / 2
            val spokeX = centerX + radius * cos(angle).toFloat()
            val spokeY = centerY + radius * sin(angle).toFloat()

            drawLine(
                color = outlineColor.copy(alpha = 0.35f),
                start = Offset(centerX, centerY),
                end = Offset(spokeX, spokeY),
                strokeWidth = 1.dp.toPx(),
            )

            // 라벨 위치
            val labelRadius = radius + 20.dp.toPx()
            val labelX = centerX + labelRadius * cos(angle).toFloat()
            val labelY = centerY + labelRadius * sin(angle).toFloat() + 4.dp.toPx()

            drawContext.canvas.nativeCanvas.drawText(
                axes[i].label,
                labelX,
                labelY,
                textPaint,
            )
        }

        // 3. 데이터 폴리곤 (1.0~5.0)
        val dataPath = Path()
        val points = mutableListOf<Offset>()

        for (i in 0 until numAxes) {
            val value = axes[i].value.coerceIn(0.5, 5.0)
            val pointRadius = (radius / 5f) * value.toFloat()
            val angle = (Math.PI * 2 / numAxes) * i - Math.PI / 2
            val x = centerX + pointRadius * cos(angle).toFloat()
            val y = centerY + pointRadius * sin(angle).toFloat()

            points.add(Offset(x, y))
            if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()

        // 면 채우기
        drawPath(
            path = dataPath,
            color = accentColor.copy(alpha = 0.3f),
            style = Fill,
        )

        // 외곽선
        drawPath(
            path = dataPath,
            color = accentColor,
            style = Stroke(width = 2.5.dp.toPx()),
        )

        // 꼭짓점 점
        points.forEach { point ->
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = point,
            )
            drawCircle(
                color = accentColor,
                radius = 4.dp.toPx(),
                center = point,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
