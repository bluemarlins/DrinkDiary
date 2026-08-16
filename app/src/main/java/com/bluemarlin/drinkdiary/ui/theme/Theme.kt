package com.bluemarlin.drinkdiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// M3 ColorScheme에는 명세 3.1절의 `InkFaint`(비활성 텍스트, 플레이스홀더)에 해당하는 슬롯이 없다.
// `outline`은 이제 `LineStrong`(테두리)이므로 비활성 '텍스트' 색으로 쓸 수 없다.
@Immutable
data class DrinkDiaryExtendedColors(
    val inkFaint: Color,
)

private val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryDark,
        onPrimary = PaperDark,
        primaryContainer = PrimaryContainerDark,
        onPrimaryContainer = InkDark,
        secondary = MaltDark,
        onSecondary = PaperDark,
        secondaryContainer = MaltContainerDark,
        onSecondaryContainer = InkDark,
        tertiary = WineDark,
        onTertiary = PaperDark,
        tertiaryContainer = WineContainerDark,
        onTertiaryContainer = InkDark,
        background = PaperDark,
        onBackground = InkDark,
        surface = SurfaceDark,
        onSurface = InkDark,
        surfaceVariant = SurfaceSunkDark,
        onSurfaceVariant = InkSoftDark,
        outline = LineStrongDark,
        outlineVariant = LineDark,
        error = DestructiveDark,
        onError = PaperDark,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = PrimaryLight,
        onPrimary = PaperLight,
        primaryContainer = PrimaryContainerLight,
        onPrimaryContainer = InkLight,
        secondary = MaltLight,
        onSecondary = PaperLight,
        secondaryContainer = MaltContainerLight,
        onSecondaryContainer = InkLight,
        tertiary = WineLight,
        onTertiary = PaperLight,
        tertiaryContainer = WineContainerLight,
        onTertiaryContainer = InkLight,
        background = PaperLight,
        onBackground = InkLight,
        surface = SurfaceLight,
        onSurface = InkLight,
        surfaceVariant = SurfaceSunkLight,
        onSurfaceVariant = InkSoftLight,
        outline = LineStrongLight,
        outlineVariant = LineLight,
        error = DestructiveLight,
        onError = PaperLight,
    )

private val LightExtendedColors = DrinkDiaryExtendedColors(inkFaint = InkFaintLight)

private val DarkExtendedColors = DrinkDiaryExtendedColors(inkFaint = InkFaintDark)

val LocalDrinkDiaryExtendedColors = staticCompositionLocalOf { LightExtendedColors }

object DrinkDiaryThemeTokens {
    val inkFaint: Color
        @Composable
        get() = LocalDrinkDiaryExtendedColors.current.inkFaint
}

// 다이내믹 컬러는 두지 않는다. 켜지는 순간 명세 3.1절 팔레트가 통째로 무시되므로
// 디자인 시스템과 양립하지 않는다 — 쓰지 않는 스위치라도 남겨두면 언젠가 켜진다.
@Composable
fun DrinkDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalDrinkDiaryExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = DrinkDiaryShapes,
            content = content,
        )
    }
}
