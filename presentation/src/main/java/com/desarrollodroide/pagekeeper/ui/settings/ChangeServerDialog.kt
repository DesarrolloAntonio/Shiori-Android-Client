package com.desarrollodroide.pagekeeper.ui.settings

import android.webkit.URLUtil
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.desarrollodroide.pagekeeper.ui.components.DialogConfirmButton
import com.desarrollodroide.pagekeeper.ui.components.DialogDismissButton
import com.desarrollodroide.pagekeeper.ui.components.DialogNote
import com.desarrollodroide.pagekeeper.ui.components.ShioriAlertDialog

/**
 * A new address for the server this device is signed in to (issue #64).
 *
 * Saved only once that address accepts the current session as the same user, which the view model
 * checks: anything else, a different server included, still means logging out.
 */
@Composable
fun ChangeServerDialog(
    currentUrl: String,
    state: ServerChange,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var url by rememberSaveable { mutableStateOf(currentUrl) }
    val checking = state is ServerChange.Checking
    val looksValid = URLUtil.isValidUrl(url.trim())
    val changed = url.trim().trimEnd('/') != currentUrl.trim().trimEnd('/')

    ShioriAlertDialog(
        onDismissRequest = { if (!checking) onDismiss() },
        icon = Icons.Filled.Dns,
        title = "Change server address",
        // Not dismissible mid-check, so a late answer can't land on a closed dialog.
        properties = DialogProperties(dismissOnBackPress = !checking, dismissOnClickOutside = !checking),
        text = {
            // The field first, and the whole thing scrolls: on a phone in landscape the icon and
            // title leave so little height that the field, under an explanation, was squashed to
            // a sliver with its text unreadable.
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !checking,
                    singleLine = true,
                    label = { Text("Server url") },
                    isError = url.isNotBlank() && !looksValid,
                    supportingText = if (url.isNotBlank() && !looksValid) {
                        { Text("Include http:// or https://") }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done,
                    ),
                )
                if (state is ServerChange.Failed) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                DialogNote(
                    "For when your server moved to a new domain, IP or port. You stay signed in, " +
                        "and changes waiting to upload are kept."
                )
            }
        },
        confirmButton = {
            DialogConfirmButton(
                text = if (checking) "Checking…" else "Save",
                onClick = { onSave(url) },
                enabled = looksValid && changed && !checking,
            )
        },
        dismissButton = { DialogDismissButton(text = "Cancel", onClick = onDismiss) },
    )
}
