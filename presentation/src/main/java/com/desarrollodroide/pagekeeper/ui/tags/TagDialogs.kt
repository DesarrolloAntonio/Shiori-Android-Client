package com.desarrollodroide.pagekeeper.ui.tags

import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.RoundedCornerShape
import com.desarrollodroide.pagekeeper.ui.components.DialogDismissButton
import com.desarrollodroide.pagekeeper.ui.components.DialogConfirmButton
import com.desarrollodroide.pagekeeper.ui.components.ShioriAlertDialog
import com.desarrollodroide.pagekeeper.ui.components.DialogTone
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

/**
 * Create/rename dialog.
 *
 * Duplicate names are rejected in the dialog rather than by the server, because the server answers
 * a duplicate with a 500 that says nothing useful to show a user.
 */
@Composable
fun TagNameDialog(
    title: String,
    confirmLabel: String,
    initialName: String,
    existingNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    icon: ImageVector = Icons.Outlined.Edit,
    tone: DialogTone = DialogTone.Warning,
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    val trimmed = name.trim()
    val isDuplicate = remember(trimmed, existingNames) {
        existingNames.any { it.equals(trimmed, ignoreCase = true) }
    }
    val canConfirm = trimmed.isNotEmpty() && !isDuplicate

    ShioriAlertDialog(
        onDismissRequest = onDismiss,
        icon = icon,
        tone = tone,
        title = title,
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                label = { Text("Name") },
                singleLine = true,
                isError = isDuplicate,
                supportingText = if (isDuplicate) {
                    { Text("A tag with that name already exists") }
                } else null,
            )
        },
        confirmButton = {
            DialogConfirmButton(text = confirmLabel, onClick = { onConfirm(trimmed) }, enabled = canConfirm)
        },
        dismissButton = {
            DialogDismissButton(text = "Cancel", onClick = onDismiss)
        },
    )
}

@Composable
fun DeleteTagDialog(
    tag: Tag,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    ShioriAlertDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.Delete,
        tone = DialogTone.Danger,
        title = "Delete \"${tag.name}\"?",
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                text = if (tag.nBookmarks > 0) {
                    "It will be removed from ${tag.nBookmarks} bookmark" +
                        (if (tag.nBookmarks == 1) "." else "s.") +
                        " The bookmarks themselves are kept."
                } else {
                    "This tag is not used by any bookmark."
                }
            )
        },
        confirmButton = {
            DialogConfirmButton(text = "Delete", onClick = onConfirm, destructive = true)
        },
        dismissButton = {
            DialogDismissButton(text = "Cancel", onClick = onDismiss)
        },
    )
}

@Preview
@Composable
private fun TagNameDialogPreview() {
    ShioriTheme {
        TagNameDialog(
            title = "Rename tag",
            confirmLabel = "Rename",
            initialName = "android",
            existingNames = listOf("kotlin"),
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview
@Composable
private fun DeleteTagDialogPreview() {
    ShioriTheme {
        DeleteTagDialog(
            tag = Tag(id = 1, name = "android", selected = false, nBookmarks = 42),
            onDismiss = {},
            onConfirm = {},
        )
    }
}
