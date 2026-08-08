package com.bluemarlin.drinkdiary.ui.theme

import android.os.Build
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
    // Reuses the Rose family instead of M3's baseline red — errors/destructive actions
    // read as part of the app's palette ("절제된 감정 표현", research-component-motion-ux.md
    // section 4) rather than a jarring off-brand color.
    error = Rose80,
    onError = Rose10,
    errorContainer = Rose30,
    onErrorContainer = Rose90,
    background = DeepForest10,
    onBackground = WarmOffWhite90,
    surface = DeepForest10,
    onSurface = WarmOffWhite90,
    surfaceVariant = DeepForest20,
    onSurfaceVariant = MoodyOnSurfaceVariant,
    outline = MoodyOutline,
    surfaceContainerLowest = DeepForest10,
    surfaceContainerLow = DeepForest15,
    surfaceContainer = DeepForest20,
    surfaceContainerHigh = DeepForest25,
    surfaceContainerHighest = DeepForest30,
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
    error = Rose40,
    onError = Color.White,
    errorContainer = Rose90,
    onErrorContainer = Rose10,
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
    // Resolved by the caller (MainActivity) from the user's Auto/Dark/Light setting
    // (ui/settings) — Auto follows the system day/night setting. The dark & moody
    // "wine cellar" palette (DarkColorScheme below) is still the app's primary brand
    // look, but it's user-toggleable rather than forced. See app/docs/design/
    // research-immersive-ui.md for the palette's design rationale.
    darkTheme: Boolean,
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
