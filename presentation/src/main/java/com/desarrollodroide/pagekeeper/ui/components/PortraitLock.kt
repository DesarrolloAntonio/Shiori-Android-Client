package com.desarrollodroide.pagekeeper.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/**
 * Whether a device is small enough that a screen is better off pinned to portrait.
 *
 * Takes the *smallest* width, which is a property of the device rather than of how it is being
 * held, so the answer does not flip as the user rotates. 600dp is the Material and Android
 * boundary between a phone and a tablet, and it is also what a folding phone crosses when it
 * opens: folded it is a phone and gets pinned, unfolded it is not and does not.
 */
fun shouldLockToPortrait(smallestWidthDp: Int): Boolean = smallestWidthDp < PortraitLockMaxWidth

/** Below this a window in landscape has too little height left to lay a form down the page. */
const val PortraitLockMaxWidth: Int = 600

/**
 * Pins the screen to portrait while this composable is in the composition, on phones only.
 *
 * Login in landscape on a phone leaves around 360dp of height, and the card alone is 330dp of it,
 * so it scrolls before the keyboard is even up. There is nothing useful to do with the extra
 * width at that size either — the two column layout wants 600dp — so the honest answer is not to
 * offer landscape at all.
 *
 * The previous value is restored on the way out rather than being set to a fixed default, so this
 * cannot quietly become the orientation policy for the whole app.
 *
 * Note for when the app moves to targetSdk 36: Android 16 ignores orientation requests on displays
 * of 600dp and wider. That does not affect this, because this only ever asks on displays narrower
 * than that, but the API is on its way out.
 */
@Composable
fun LockPortraitOnPhone() {
    val context = LocalContext.current
    val smallestWidthDp = LocalConfiguration.current.smallestScreenWidthDp

    DisposableEffect(smallestWidthDp) {
        val activity = context.findActivity()
        if (activity == null || !shouldLockToPortrait(smallestWidthDp)) {
            return@DisposableEffect onDispose { }
        }
        val previous = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose { activity.requestedOrientation = previous }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
