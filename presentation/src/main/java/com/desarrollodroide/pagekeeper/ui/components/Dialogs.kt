package com.desarrollodroide.pagekeeper.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
                if (title.isNotEmpty()) {
                    Text(text = title)
                }
            },
            text = {
                if (content.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center),
                            text = content
                        )
                    }
                }
            },
            confirmButton = {
                if (confirmButtonText.isNotEmpty()) {
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
                if (dismissButtonText.isNotEmpty()) {
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
    onDismiss: (() -> Unit)? = null,
    openDialog: MutableState<Boolean>,
    properties: DialogProperties = DialogProperties(),
) {
    SimpleDialog(
        title = title,
        content = content,
        icon = icon,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
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
            if (title != null) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20)),
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp),
                        text = title
                    )
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
) {
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
    isLoading: Boolean,
    showDialog: MutableState<Boolean>,
    onConfirm: (keepOldTitle: Boolean, updateArchive: Boolean, updateEbook: Boolean) -> Unit,
) {
    if (showDialog.value) {
        var keepOldTitleChecked by remember { mutableStateOf(false) }
        var updateArchiveChecked by remember { mutableStateOf(false) }
        var updateEbookChecked by remember { mutableStateOf(false) }

        val wasLoading = remember { mutableStateOf(isLoading) }
        LaunchedEffect(isLoading) {
            if (wasLoading.value && !isLoading) {
                showDialog.value = false
            }
            wasLoading.value = isLoading
        }

        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            title = { Text("Update cache for selected bookmark? This action is irreversible.") },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(enabled = !isLoading) {
                                keepOldTitleChecked = !keepOldTitleChecked
                            }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            enabled = !isLoading,
                            checked = keepOldTitleChecked,
                            onCheckedChange = null
                        )
                        Text("Keep the old title and excerpt", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(enabled = !isLoading) {
                                updateArchiveChecked = !updateArchiveChecked
                            }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            enabled = !isLoading,
                            checked = updateArchiveChecked,
                            onCheckedChange = null
                        )
                        Text("Update archive as well", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(enabled = !isLoading) {
                                updateEbookChecked = !updateEbookChecked
                            }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            enabled = !isLoading,
                            checked = updateEbookChecked,
                            onCheckedChange = null
                        )
                        Text("Update Ebook as well", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            },
            confirmButton = {
                LoadingButton(
                    text = "Update",
                    onClick = {
                        onConfirm(keepOldTitleChecked, updateArchiveChecked, updateEbookChecked)
                    },
                    loading = isLoading)
            },

            dismissButton = {
                AnimatedVisibility (
                    enter = fadeIn(),
                    exit = fadeOut(),
                    visible = !isLoading
                ){
                    Button(onClick = {
                        showDialog.value = false
                    }) {
                        Text("Cancel")
                    }
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }
}

@Composable
fun EpubOptionsDialog(
    title: String = "",
    content: String = "",
    icon: ImageVector? = null,
    onClickOption: ((Int) -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    showDialog: MutableState<Boolean>
) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            icon = { if (icon != null) Icon(imageVector = icon, contentDescription = null) },
            title = {
                if (title.isNotEmpty()) {
                    Text(text = title)
                }
            },
            text = {
                if (content.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center),
                            text = content
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.padding(all = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        showDialog.value = false
                    }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        showDialog.value = false
                        onClickOption?.invoke(2)
                    }) {
                        Text("Share")
                    }
                }
            },
            properties = properties
        )
    }
}