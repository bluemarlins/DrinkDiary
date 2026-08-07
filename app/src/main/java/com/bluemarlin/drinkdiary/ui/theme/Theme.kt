package com.bluemarlin.drinkdiary.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CellarGreen80,
    onPrimary = CellarGreen10,
    primaryContainer = CellarGreen30,
    onPrimaryContainer = CellarGreen90,
    secondary = MaltGold80,
    onSecondary = MaltGold10,
    secondaryContainer = MaltGold30,
    onSecondaryContainer = MaltGold90,
    tertiary = Rose80,
    onTertiary = Rose10,
    tertiaryContainer = Rose30,
    onTertiaryContainer = Rose90,
    background = WarmCharcoal10,
    onBackground = WarmOffWhite90,
    surface = WarmCharcoal10,
    onSurface = WarmOffWhite90,
    surfaceVariant = WarmCharcoal20,
    onSurfaceVariant = WarmGray80,
    outline = WarmGray60,
    surfaceContainerLowest = WarmCharcoal10,
    surfaceContainerLow = WarmCharcoal15,
    surfaceContainer = WarmCharcoal20,
    surfaceContainerHigh = WarmCharcoal25,
    surfaceContainerHighest = WarmCharcoal30,
)

private val LightColorScheme = lightColorScheme(
    primary = CellarGreen40,
    onPrimary = Color.White,
    primaryContainer = CellarGreen90,
    onPrimaryContainer = CellarGreen10,
    secondary = MaltGold40,
    onSecondary = Color.White,
    secondaryContainer = MaltGold90,
    onSecondaryContainer = MaltGold10,
    tertiary = Rose40,
    onTertiary = Color.White,
    tertiaryContainer = Rose90,
    onTertiaryContainer = Rose10,
    background = Cream99,
    onBackground = WarmCharcoal10,
    surface = Cream99,
    onSurface = WarmCharcoal10,
    surfaceVariant = Cream95,
    onSurfaceVariant = WarmGray30,
    outline = WarmGray60,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Cream97,
    surfaceContainer = Cream95,
    surfaceContainerHigh = Cream92,
    surfaceContainerHighest = Cream88,
)

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = DrinkDiaryShapes,
        content = content
    )
}
