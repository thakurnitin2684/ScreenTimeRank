package com.thakurnitin2684.screentimerank

import android.view.View
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAdView


class UnifiedNativeAdViewHolder internal constructor(view: View) {
    val adView: UnifiedNativeAdView = view.findViewById<View>(R.id.ad_view) as UnifiedNativeAdView

    init {

        adView.mediaView = adView.findViewById<View>(R.id.ad_media) as MediaView

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
    }


}