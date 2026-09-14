package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.DialogProperties
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog

/**
 * The feed's error dialog.
 *
 * Accept only did something for an expired session; any other error stayed in the view model, so
 * the dialog closed but the state still said "error". The next failure wrote the same error again,
 * nothing changed, and nothing was shown: from the second failed "Add tags" on, the app was silent.
 * Seen on a device, offline.
 */
@Composable
internal fun FeedErrorDialog(
    error: String?,
    onAccept: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    if (error.isNullOrEmpty()) return
    ConfirmDialog(
        icon = Icons.Default.Error,
        title = "Error",
        content = error,
        openDialog = remember { mutableStateOf(true) },
        onConfirm = {
            if (error == SESSION_HAS_BEEN_EXPIRED) onSessionExpired() else onAccept()
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        ),
    )
}
