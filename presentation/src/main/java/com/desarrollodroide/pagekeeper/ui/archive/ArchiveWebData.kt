package com.desarrollodroide.pagekeeper.ui.archive

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import java.io.File

/**
 * The archive viewer's session cookie and cache.
 *
 * Shiori serves an archive only to a signed-in user, and a WebView can't add a header to the
 * requests a page makes for its own images and styles. The session goes in the `token` cookie,
 * which Shiori 1.8 reads as well as the header. That cookie is a live session, and the server marks
 * archive files cacheable for a year, so neither is left behind: both are cleared when the viewer
 * closes and again at logout, which also covers a process killed while the viewer was open.
 */
object ArchiveWebData {

    fun putSession(serverUrl: String, token: String) {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setCookie(serverUrl, "token=$token; Path=/")
            flush()
        }
    }

    fun removeSession() {
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
    }

    /** At logout, when no WebView is alive to clear its own cache. */
    fun clearAll(context: Context) {
        removeSession()
        WebStorage.getInstance().deleteAllData()
        File(context.cacheDir, "WebView").deleteRecursively()
    }
}
