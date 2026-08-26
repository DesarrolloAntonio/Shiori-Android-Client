package com.desarrollodroide.pagekeeper.ui.tags

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
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    val trimmed = name.trim()
    val isDuplicate = remember(trimmed, existingNames) {
        existingNames.any { it.equals(trimmed, ignoreCase = true) }
    }
    val canConfirm = trimmed.isNotEmpty() && !isDuplicate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                label = { Text("Name") },
                singleLine = true,
                isError = isDuplicate,
                supportingText = if (isDuplicate) {
                    { Text("A tag with that name already exists") }
                } else null,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(trimmed) },
                enabled = canConfirm,
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun DeleteTagDialog(
    tag: Tag,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
        title = { Text("Delete \"${tag.name}\"?") },
        text = {
            Text(
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
            TextButton(onClick = onConfirm) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
