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
    // DrinkDiary's dark & moody "wine cellar" palette is the app's brand identity,
    // not a user-toggleable dark mode — always render dark regardless of the
    // system theme setting. See app/docs/design/research-immersive-ui.md.
    darkTheme: Boolean = true,
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
