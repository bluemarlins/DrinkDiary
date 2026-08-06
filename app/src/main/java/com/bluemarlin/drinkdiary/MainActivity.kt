package com.bluemarlin.drinkdiary

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bluemarlin.drinkdiary.ads.ConsentManager
import com.bluemarlin.drinkdiary.ui.navigation.DrinkDiaryApp
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
