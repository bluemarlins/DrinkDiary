package com.bluemarlin.drinkdiary.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Shows a full-screen interstitial after a record is saved, capped to at most once every
 * [AdConfig.INTERSTITIAL_SAVE_FREQUENCY] saves so it never interrupts the input flow itself
 * (research finding: interstitials must appear at the post-save transition, never mid-form).
 * See app/docs/research/ad-monetization.md section 2.
 */
class InterstitialAdManager(context: Context) {
    private val prefs = context.getSharedPreferences("ads_prefs", Context.MODE_PRIVATE)
    private var interstitialAd: InterstitialAd? = null

    fun preload(context: Context) {
        if (interstitialAd != null) return
        InterstitialAd.load(
            context,
            AdConfig.INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            },
        )
    }

    /**
     * Increments the save counter and shows the interstitial if it's due and loaded.
     * [onProceed] is always called exactly once — immediately if the ad isn't due/ready,
     * or after the ad is dismissed/fails to show.
     */
    fun maybeShowAfterSave(activity: Activity, onProceed: () -> Unit) {
        val saveCount = prefs.getInt(KEY_SAVE_COUNT, 0) + 1
        prefs.edit().putInt(KEY_SAVE_COUNT, saveCount).apply()

        val ad = interstitialAd
        if (ad == null || saveCount % AdConfig.INTERSTITIAL_SAVE_FREQUENCY != 0) {
            preload(activity)
            onProceed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preload(activity)
                onProceed()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                preload(activity)
                onProceed()
            }
        }
        ad.show(activity)
    }

    private companion object {
        const val KEY_SAVE_COUNT = "interstitial_save_count"
    }
}
