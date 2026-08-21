package com.bluemarlin.drinkdiary.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.WineColor
import java.io.File
import java.io.FileOutputStream

object SamplePhotoGenerator {
    fun ensureSamplePhotos(context: Context): Map<String, String> {
        val directory = File(context.filesDir, "photos").apply { mkdirs() }
        val result = mutableMapOf<String, String>()

        val samples =
            listOf(
                // Wine
                SamplePhotoSpec(
                    "샤또 마고 2018",
                    DrinkType.Wine,
                    WineColor.Red,
                    Color.parseColor("#4A0E17"),
                    Color.parseColor("#1F060A"),
                ),
                SamplePhotoSpec(
                    "도멘 드 라 로마네 꽁띠 그랑 에셰조 2017",
                    DrinkType.Wine,
                    WineColor.Red,
                    Color.parseColor("#58111A"),
                    Color.parseColor("#25050A"),
                ),
                SamplePhotoSpec(
                    "루이 자도 샤블리 2021",
                    DrinkType.Wine,
                    WineColor.White,
                    Color.parseColor("#C8B88A"),
                    Color.parseColor("#4A4228"),
                ),
                SamplePhotoSpec(
                    "클라우디 베이 소비뇽 블랑 2023",
                    DrinkType.Wine,
                    WineColor.White,
                    Color.parseColor("#BAC8A0"),
                    Color.parseColor("#3B4A28"),
                ),
                SamplePhotoSpec(
                    "카사노바 디 네리 브루넬로 디 몬탈치노 2016",
                    DrinkType.Wine,
                    WineColor.Red,
                    Color.parseColor("#50141E"),
                    Color.parseColor("#1E060C"),
                ),
                SamplePhotoSpec(
                    "우나니메 2018",
                    DrinkType.Wine,
                    WineColor.Red,
                    Color.parseColor("#3D101C"),
                    Color.parseColor("#14040A"),
                ),
                SamplePhotoSpec(
                    "돔 페리뇽 2013",
                    DrinkType.Wine,
                    WineColor.Sparkling,
                    Color.parseColor("#384030"),
                    Color.parseColor("#151A10"),
                ),
                SamplePhotoSpec(
                    "파이퍼 하이직 뀌베 브뤼",
                    DrinkType.Wine,
                    WineColor.Sparkling,
                    Color.parseColor("#9E2A2B"),
                    Color.parseColor("#3F0F10"),
                ),
                SamplePhotoSpec(
                    "루시 마고 와일드맨 블랑 2022",
                    DrinkType.Wine,
                    WineColor.Natural,
                    Color.parseColor("#D4A373"),
                    Color.parseColor("#593D22"),
                ),
                SamplePhotoSpec(
                    "그라함 20년 토니 포트",
                    DrinkType.Wine,
                    WineColor.Port,
                    Color.parseColor("#6B2D2D"),
                    Color.parseColor("#260E0E"),
                ),
                SamplePhotoSpec(
                    "켄달 잭슨 빈트너스 리저브 카베르네 소비뇽 2020",
                    DrinkType.Wine,
                    WineColor.Red,
                    Color.parseColor("#421820"),
                    Color.parseColor("#18060A"),
                ),
                SamplePhotoSpec(
                    "몬테스 클래식 카베르네 소비뇽 2022",
                    DrinkType.Wine,
                    WineColor.Red,
                    Color.parseColor("#381820"),
                    Color.parseColor("#14060A"),
                ),
                // Whiskey
                SamplePhotoSpec(
                    "글렌피딕 12년",
                    DrinkType.Whiskey,
                    null,
                    Color.parseColor("#2B4436"),
                    Color.parseColor("#0E1A14"),
                ),
                SamplePhotoSpec(
                    "발베니 12년 더블우드",
                    DrinkType.Whiskey,
                    null,
                    Color.parseColor("#C2884A"),
                    Color.parseColor("#422A12"),
                ),
                SamplePhotoSpec(
                    "라프로익 10년",
                    DrinkType.Whiskey,
                    null,
                    Color.parseColor("#243E36"),
                    Color.parseColor("#0B1713"),
                ),
                SamplePhotoSpec(
                    "글렌알라키 10년 CS 배치 9",
                    DrinkType.Whiskey,
                    null,
                    Color.parseColor("#5A251D"),
                    Color.parseColor("#1F0A06"),
                ),
                SamplePhotoSpec(
                    "조니워커 그린 라벨",
                    DrinkType.Whiskey,
                    null,
                    Color.parseColor("#2E4B3E"),
                    Color.parseColor("#0F1C16"),
                ),
                SamplePhotoSpec(
                    "몽키 숄더",
                    DrinkType.Whiskey,
                    null,
                    Color.parseColor("#B87333"),
                    Color.parseColor("#3E240D"),
                ),
                SamplePhotoSpec(
                    "발렌타인 17년",
                    DrinkType.Whiskey,
                    null,
                    Color.parseColor("#24334A"),
                    Color.parseColor("#0D1420"),
                ),
                SamplePhotoSpec(
                    "와일드 터키 레어 브리드",
                    DrinkType.Whiskey,
                    null,
                    Color.parseColor("#8C4722"),
                    Color.parseColor("#2E1408"),
                ),
                SamplePhotoSpec(
                    "불렛 라이",
                    DrinkType.Whiskey,
                    null,
                    Color.parseColor("#9E5B32"),
                    Color.parseColor("#361D0D"),
                ),
                SamplePhotoSpec(
                    "기원 배치 1",
                    DrinkType.Whiskey,
                    null,
                    Color.parseColor("#4A3B32"),
                    Color.parseColor("#1A1410"),
                ),
                SamplePhotoSpec(
                    "카발란 솔리스트 비노바리끄",
                    DrinkType.Whiskey,
                    null,
                    Color.parseColor("#631D27"),
                    Color.parseColor("#21070B"),
                ),
            )

        samples.forEachIndexed { index, spec ->
            val file = File(directory, "sample_${index + 1}.jpg")
            if (!file.exists() || file.length() == 0L) {
                generatePhoto(file, spec)
            }
            result[spec.name] = Uri.fromFile(file).toString()
        }

        return result
    }

    private fun generatePhoto(
        file: File,
        spec: SamplePhotoSpec,
    ) {
        val width = 600
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient Background
        val bgPaint =
            Paint().apply {
                shader =
                    LinearGradient(0f, 0f, 0f, height.toFloat(), spec.topColor, spec.bottomColor, Shader.TileMode.CLAMP)
            }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Bottle Silhouette
        val bottlePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 40
                style = Paint.Style.FILL
            }

        val centerX = width / 2f
        // Body
        val bodyRect = RectF(centerX - 90f, 300f, centerX + 90f, 680f)
        canvas.drawRoundRect(bodyRect, 24f, 24f, bottlePaint)

        // Neck
        val neckRect = RectF(centerX - 24f, 160f, centerX + 24f, 300f)
        canvas.drawRoundRect(neckRect, 8f, 8f, bottlePaint)

        // Cap
        val capRect = RectF(centerX - 28f, 130f, centerX + 28f, 160f)
        canvas.drawRoundRect(capRect, 4f, 4f, bottlePaint)

        // Label on bottle
        val labelPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 80
                style = Paint.Style.FILL
            }
        val labelRect = RectF(centerX - 70f, 380f, centerX + 70f, 560f)
        canvas.drawRoundRect(labelRect, 8f, 8f, labelPaint)

        // Text on label
        val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                alpha = 180
                textSize = 28f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
        val subTextPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.DKGRAY
                alpha = 180
                textSize = 18f
                textAlign = Paint.Align.CENTER
            }

        val typeText = if (spec.type == DrinkType.Wine) "WINE" else "WHISKY"
        canvas.drawText(typeText, centerX, 430f, subTextPaint)

        val shortName = if (spec.name.length > 8) spec.name.take(8) + ".." else spec.name
        canvas.drawText(shortName, centerX, 480f, textPaint)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
    }
}

private data class SamplePhotoSpec(
    val name: String,
    val type: DrinkType,
    val wineColor: WineColor?,
    val topColor: Int,
    val bottomColor: Int,
)
