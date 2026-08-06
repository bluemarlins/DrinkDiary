package com.bluemarlin.drinkdiary.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Anchored adaptive banner shown above the bottom navigation bar on top-level screens
 * (Dashboard/Collection) — see DDScreenScaffold's `showBannerAd` slot and
 * app/docs/research/ad-monetization.md section 2 for placement rationale.
 */
@Composable
fun DDBannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = AdConfig.BANNER_AD_UNIT_ID
        }
    }
    DisposableEffect(adView) {
        adView.loadAd(AdRequest.Builder().build())
        onDispose { adView.destroy() }
    }
    AndroidView(modifier = modifier.fillMaxWidth(), factory = { adView })
}
