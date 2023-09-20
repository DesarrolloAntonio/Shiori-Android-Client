package com.shiori.androidclient.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

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

fun Context.openUrlInBrowser(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        Toast.makeText(this, "Error opening URL", Toast.LENGTH_SHORT).show()
    }
}

