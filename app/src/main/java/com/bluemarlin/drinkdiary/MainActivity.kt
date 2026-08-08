package com.bluemarlin.drinkdiary

import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bluemarlin.drinkdiary.ads.ConsentManager
import com.bluemarlin.drinkdiary.ui.navigation.DrinkDiaryApp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // App is forced dark regardless of system setting (see DrinkDiaryTheme) —
        // force light system-bar icons to match, instead of enableEdgeToEdge()'s
        // default auto-detection based on the system day/night setting.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val appContainer = (application as DrinkDiaryApplication).appContainer
        ConsentManager(this).requestConsentAndInitialize {
            appContainer.interstitialAdManager.preload(this)
        }
        setContent {
            DrinkDiaryTheme {
                DrinkDiaryApp()
            }
        }
    }
}
