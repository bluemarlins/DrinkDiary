package com.bluemarlin.drinkdiary.ads

/**
 * Ad unit IDs. These are Google's official public test IDs — always resolve to test
 * creatives, safe to ship in debug builds, and never generate real impressions/revenue.
 *
 * TODO(Phase 4/release): swap these for the real ad unit IDs created under the
 * `spicyrabbit` AdMob account before submitting a release build. See
 * app/docs/service-launch-roadmap.md Phase 2/4 and app/docs/research/ad-monetization.md
 * for the placement rationale (banner on Dashboard/Collection, capped interstitial after
 * RecordEditor save).
 */
object AdConfig {
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    /** Show an interstitial at most once every this many successful record saves. */
    const val INTERSTITIAL_SAVE_FREQUENCY = 4
}
