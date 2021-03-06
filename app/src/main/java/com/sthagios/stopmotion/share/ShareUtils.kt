package com.sthagios.stopmotion.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   05.06.16
 */

fun Context.shareGif(shareUriString: String, name: String = "gif_name") {
    val shareIntent = Intent()
    shareIntent.action = Intent.ACTION_SEND;
    shareIntent.putExtra(Intent.EXTRA_TEXT, "Stopmotion")
    try {
        val shareUri = Uri.parse(shareUriString)
        Log.d("Sharing", "Sharing $shareUri")
        shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri)
        shareIntent.type = "image/*"

        startActivity(Intent.createChooser(shareIntent, "Stopmotion sharing"))


        val payload = Bundle();
        payload.putString("image_name", name);
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SHARE, payload)

    } catch (e: Exception) {
        e.printStackTrace()
    }
}