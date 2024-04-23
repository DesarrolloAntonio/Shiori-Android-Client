package com.desarrollodroide.pagekeeper.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun Context.shareText(text: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    startActivity(Intent.createChooser(shareIntent, null))
}

fun Context.openUrlInBrowser(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val chooser = Intent.createChooser(intent, "Open with")
    startActivity(chooser)
}


fun Context.shareEpubFile(file: File) {
    val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", file)

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "application/epub+zip"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(intent, "Share EPUB"))
}

fun Context.sendFeedbackEmail() {
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("desarrollodroide@gmail.com"))
    }
    val chooserIntent = Intent.createChooser(emailIntent, "Choose an email app:")
    startActivity(chooserIntent)
}


