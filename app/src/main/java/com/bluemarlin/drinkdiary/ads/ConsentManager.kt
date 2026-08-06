package com.bluemarlin.drinkdiary.ads

import android.app.Activity
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Wraps Google's User Messaging Platform (UMP) consent flow. EEA/UK users must confirm
 * (or decline) personalized-ads consent before ad requests are made; UMP determines this
 * automatically from the device's location, so non-EEA/UK users (the primary Korean
 * audience) skip the form entirely. See app/docs/research/ad-monetization.md section 4.
 */
class ConsentManager(private val activity: Activity) {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)

    fun requestConsentAndInitialize(onReady: () -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    initializeAdsIfPossible(onReady)
                }
            },
            {
                // Consent info update failed (e.g. no network) — proceed without blocking the app.
                initializeAdsIfPossible(onReady)
            },
        )
    }

    private fun initializeAdsIfPossible(onReady: () -> Unit) {
        if (consentInformation.canRequestAds()) {
            MobileAds.initialize(activity) { onReady() }
        }
    }
}
