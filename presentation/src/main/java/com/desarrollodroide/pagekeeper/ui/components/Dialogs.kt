package com.desarrollodroide.pagekeeper.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.desarrollodroide.pagekeeper.R

@Composable
fun SimpleDialog(
    title: String = "",
    content: String = "",
    icon: ImageVector? = null,
    confirmButtonText: String = "",
    dismissButtonText: String = "",
    openDialog: MutableState<Boolean>,
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
) {
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                openDialog.value = false
            },
            icon = { if (icon != null) Icon(imageVector = icon, contentDescription = null) },
            title = {
                if (title.isNotEmpty()){
                    Text(text = title)
                }
            },
            text = {
                if (content.isNotEmpty()){
                    Box(modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center),
                            text = content
                        )
                    }
                }
            },
            confirmButton = {
                if (confirmButtonText.isNotEmpty()){
                    TextButton(
                        onClick = {
                            openDialog.value = false
                            onConfirm?.invoke()
                        }
                    ) {
                        Text(confirmButtonText)
                    }
                }
             },
            dismissButton = {
                if (dismissButtonText.isNotEmpty()){
                    TextButton(
                        onClick = {
                            openDialog.value = false
                            onDismiss?.invoke()
                        }
                    ) {
                        Text(dismissButtonText)
                    }
                }
            },
            properties = properties
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String = "",
    content: String = "",
    confirmButton: String = "Accept",
    dismissButton: String = "",
    icon: ImageVector? = null,
    onConfirm: (() -> Unit)? = null,
    openDialog: MutableState<Boolean>,
    properties: DialogProperties = DialogProperties(),
    ){
    SimpleDialog(
        title = title,
        content = content,
        icon = icon,
        onConfirm = onConfirm,
        confirmButtonText = confirmButton,
        dismissButtonText = dismissButton,
        openDialog = openDialog,
        properties = properties
    )
}

@Composable
fun InfiniteProgressDialog(
    title: String? = null,
    properties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Column(
            horizontalAlignment = CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.clip(CircleShape),
            ) {
                Column(
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.progressDialog_margin))
                ) {
                    CircularProgressIndicator(
                        strokeWidth = dimensionResource(id = R.dimen.progressDialog_stroke),
                        modifier = Modifier
                            .height(dimensionResource(id = R.dimen.progressDialog_size))
                            .width(dimensionResource(id = R.dimen.progressDialog_size))
                    )

                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (title != null){
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20)),
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp),
                        text = title)
                }
            }
        }
    }
}

@Composable
fun ErrorDialog(
    title: String = "",
    content: String = "",
    openDialog: MutableState<Boolean>,
    onConfirm: (() -> Unit)? = null,
){
    SimpleDialog(
        title = title,
        content = content,
        icon = Icons.Default.Error,
        confirmButtonText = "Accept",
        openDialog = openDialog,
        onConfirm = onConfirm,
        )
}

@Composable
fun UpdateCacheDialog(
    showDialog: MutableState<Boolean>,
    onConfirm: (keepOldTitle: Boolean, updateArchive: Boolean, updateEbook: Boolean) -> Unit,
    defaultKeepOldTitle: Boolean,
    defaultUpdateArchive: Boolean,
    defaultUpdateEbook: Boolean
) {
    if (showDialog.value) {
        var keepOldTitleChecked by remember { mutableStateOf(defaultKeepOldTitle) }
        var updateArchiveChecked by remember { mutableStateOf(defaultUpdateArchive) }
        var updateEbookChecked by remember { mutableStateOf(defaultUpdateEbook) }

        AlertDialog(
            onDismissRequest = {  },
            title = { Text("Update cache for selected bookmarks? This action is irreversible.") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = keepOldTitleChecked, onCheckedChange = { keepOldTitleChecked = it })
                        Text("Keep the old title and excerpt", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = updateArchiveChecked, onCheckedChange = { updateArchiveChecked = it })
                        Text("Update archive as well", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = updateEbookChecked, onCheckedChange = { updateEbookChecked = it })
                        Text("Update Ebook as well", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showDialog.value = false
                    onConfirm(keepOldTitleChecked, updateArchiveChecked, updateEbookChecked)}) {
                    Text("Update")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog.value = false
                }) {
                    Text("Cancel")
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }
}



