package com.bluemarlin.drinkdiary.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class DrinkDiaryChartColors(
    val wine: Color,
    val whiskey: Color,
    val beer: Color,
)

private val DarkColorScheme = darkColorScheme(
    primary = BottleGreenDark,
    onPrimary = CellarInkDark,
    primaryContainer = BottleGreenContainerDark,
    onPrimaryContainer = CellarOnSurfaceDark,
    secondary = MaltAmberDark,
    onSecondary = CellarInkDark,
    secondaryContainer = MaltAmberContainerDark,
    onSecondaryContainer = CellarOnSurfaceDark,
    tertiary = WineBerryDark,
    onTertiary = CellarInkDark,
    tertiaryContainer = WineBerryContainerDark,
    onTertiaryContainer = CellarOnSurfaceDark,
    background = CellarInkDark,
    onBackground = CellarOnSurfaceDark,
    surface = CellarSurfaceDark,
    onSurface = CellarOnSurfaceDark,
    surfaceVariant = CellarSurfaceVariantDark,
    onSurfaceVariant = CellarOnSurfaceVariantDark,
    outline = CellarOutlineDark,
    outlineVariant = CellarOutlineVariantDark,
    error = CorkErrorDark,
)

private val LightColorScheme = lightColorScheme(
    primary = BottleGreenLight,
    onPrimary = DrinkSurfaceLight,
    primaryContainer = BottleGreenContainerLight,
    onPrimaryContainer = DrinkOnSurfaceLight,
    secondary = MaltAmberLight,
    onSecondary = DrinkSurfaceLight,
    secondaryContainer = MaltAmberContainerLight,
    onSecondaryContainer = DrinkOnSurfaceLight,
    tertiary = WineBerryLight,
    onTertiary = DrinkSurfaceLight,
    tertiaryContainer = WineBerryContainerLight,
    onTertiaryContainer = DrinkOnSurfaceLight,
    background = DrinkPaperLight,
    onBackground = DrinkOnSurfaceLight,
    surface = DrinkSurfaceLight,
    onSurface = DrinkOnSurfaceLight,
    surfaceVariant = DrinkSurfaceVariantLight,
    onSurfaceVariant = DrinkOnSurfaceVariantLight,
    outline = DrinkOutlineLight,
    outlineVariant = DrinkOutlineVariantLight,
    error = CorkErrorLight,
)

private val LightChartColors = DrinkDiaryChartColors(
    wine = ChartWineLight,
    whiskey = ChartWhiskeyLight,
    beer = ChartBeerLight,
)

private val DarkChartColors = DrinkDiaryChartColors(
    wine = ChartWineDark,
    whiskey = ChartWhiskeyDark,
    beer = ChartBeerDark,
)

val LocalDrinkDiaryChartColors = staticCompositionLocalOf { LightChartColors }

object DrinkDiaryThemeTokens {
    val chartColors: DrinkDiaryChartColors
        @Composable
        get() = LocalDrinkDiaryChartColors.current
}

@Composable
fun DrinkDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val chartColors = if (darkTheme) DarkChartColors else LightChartColors

    CompositionLocalProvider(LocalDrinkDiaryChartColors provides chartColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = DrinkDiaryShapes,
            content = content
        )
    }
}
