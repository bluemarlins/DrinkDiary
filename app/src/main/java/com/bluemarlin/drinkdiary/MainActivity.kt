package com.bluemarlin.drinkdiary

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.bluemarlin.drinkdiary.ads.ConsentManager
import com.bluemarlin.drinkdiary.domain.model.ThemeMode
import com.bluemarlin.drinkdiary.ui.navigation.DrinkDiaryApp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Status/nav bar icon color is corrected reactively below (LaunchedEffect) once
        // the resolved theme (Auto/Dark/Light preference) is known — this initial call
        // just opts into edge-to-edge, the actual icon appearance is set right after.
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val appContainer = (application as DrinkDiaryApplication).appContainer
        ConsentManager(this).requestConsentAndInitialize {
            appContainer.interstitialAdManager.preload(this)
        }
        setContent {
            val themeMode by appContainer.observeThemeModeUseCase().collectAsState(initial = ThemeMode.Auto)
            val systemInDarkTheme = isSystemInDarkTheme()
            val resolvedDarkTheme = when (themeMode) {
                ThemeMode.Auto -> systemInDarkTheme
                ThemeMode.Dark -> true
                ThemeMode.Light -> false
            }

            val view = LocalView.current
            LaunchedEffect(resolvedDarkTheme) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !resolvedDarkTheme
                insetsController.isAppearanceLightNavigationBars = !resolvedDarkTheme
            }

            DrinkDiaryTheme(darkTheme = resolvedDarkTheme) {
                DrinkDiaryApp()
            }
        }
    }
}
