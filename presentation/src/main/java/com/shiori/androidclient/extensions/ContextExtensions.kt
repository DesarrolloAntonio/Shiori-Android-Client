package com.shiori.androidclient.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.shareText(text: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    startActivity(Intent.createChooser(shareIntent, null))
}

fun String.openInBrowser(context: Context): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(this))
    return if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
        true
    } else {
        false
    }
}
