package com.desarrollodroide.pagekeeper.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.desarrollodroide.pagekeeper.ui.theme.LocalExtendedColors

/*
 * Every dialog in the app is drawn by ShioriAlertDialog: a 32dp container, the icon on one of the
 * redesign's shapes coloured by what the dialog is about, a heavy title, and a filled pill for the
 * main action. They used to be stock AlertDialogs with two text buttons each, and nothing told a
 * delete apart from a question.
 */

/** What a dialog is about. It colours the badge, and [Danger] is the one whose action is red. */
enum class DialogTone { Brand, Warning, Danger }

@Composable
fun ShioriAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tone: DialogTone = DialogTone.Brand,
    dismissButton: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    text: (@Composable () -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon?.let { { DialogBadge(icon = it, tone = tone) } },
        title = if (title.isNotEmpty()) {
            {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 26.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp,
                    ),
                    textAlign = if (icon != null) TextAlign.Center else TextAlign.Start,
                )
            }
        } else null,
        text = text,
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        properties = properties,
    )
}

/** The icon a dialog leads with, on a shape: a cookie for the ordinary, a sun for a warning. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DialogBadge(icon: ImageVector, tone: DialogTone) {
    val scheme = MaterialTheme.colorScheme
    val extended = LocalExtendedColors.current
    val container = when (tone) {
        DialogTone.Brand -> scheme.primaryContainer
        DialogTone.Warning -> extended.goldContainer
        DialogTone.Danger -> scheme.errorContainer
    }
    val content = when (tone) {
        DialogTone.Brand -> scheme.onPrimaryContainer
        DialogTone.Warning -> extended.onGoldContainer
        DialogTone.Danger -> scheme.onErrorContainer
    }
    val shape: Shape = when (tone) {
        DialogTone.Brand -> MaterialShapes.Cookie9Sided.toShape()
        DialogTone.Warning, DialogTone.Danger -> MaterialShapes.Sunny.toShape()
    }
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(shape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = content, modifier = Modifier.size(28.dp))
    }
}

/** The main action: filled and fully round, red when it destroys something. */
@Composable
fun DialogConfirmButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(48.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 24.dp),
        colors = if (destructive) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            )
        } else {
            ButtonDefaults.buttonColors()
        },
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DialogDismissButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.height(48.dp), shape = CircleShape) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

/** A side remark set apart from the message, like "your unsent changes are kept". */
@Composable
fun DialogNote(text: String, icon: ImageVector = Icons.Outlined.Info) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text = text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

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
    tone: DialogTone = DialogTone.Brand,
    destructive: Boolean = false,
    note: String? = null,
) {
    if (openDialog.value) {
        ShioriAlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                openDialog.value = false
            },
            title = title,
            icon = icon,
            tone = tone,
            text = if (content.isNotEmpty() || note != null) {
                {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (content.isNotEmpty()) {
                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                        if (note != null) {
                            DialogNote(text = note)
                        }
                    }
                }
            } else null,
            confirmButton = {
                if (confirmButtonText.isNotEmpty()) {
                    DialogConfirmButton(
                        text = confirmButtonText,
                        destructive = destructive,
                        onClick = {
                            openDialog.value = false
                            onConfirm?.invoke()
                        },
                    )
                }
            },
            dismissButton = if (dismissButtonText.isNotEmpty()) {
                {
                    DialogDismissButton(
                        text = dismissButtonText,
                        onClick = {
                            openDialog.value = false
                            onDismiss?.invoke()
                        },
                    )
                }
            } else null,
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
    tone: DialogTone = DialogTone.Brand,
    destructive: Boolean = false,
    note: String? = null,
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
        properties = properties,
        tone = tone,
        destructive = destructive,
        note = note,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ContainedLoadingIndicator brings its own tonal container and shape-morphing
            // animation, so the hand-rolled circular Surface around a spinner is no longer needed.
            ContainedLoadingIndicator()
            if (title != null) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
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
    properties: DialogProperties = DialogProperties(),
) {
    SimpleDialog(
        title = title,
        content = content,
        icon = Icons.Default.Error,
        tone = DialogTone.Danger,
        confirmButtonText = "Accept",
        openDialog = openDialog,
        onConfirm = onConfirm,
        properties = properties,
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

        ShioriAlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            icon = Icons.Outlined.Sync,
            title = "Update cache?",
            text = {
                Column {
                    // The title used to be the whole warning in one long question; the warning
                    // now sits here, and the title stays short enough to set large.
                    Text(
                        text = "The saved copy of the page is fetched again. This can't be undone.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    CacheOption(
                        text = "Keep the old title and excerpt",
                        checked = keepOldTitleChecked,
                        enabled = !isLoading,
                        onCheckedChange = { keepOldTitleChecked = it },
                    )
                    CacheOption(
                        text = "Update archive as well",
                        checked = updateArchiveChecked,
                        enabled = !isLoading,
                        onCheckedChange = { updateArchiveChecked = it },
                    )
                    CacheOption(
                        text = "Update Ebook as well",
                        checked = updateEbookChecked,
                        enabled = !isLoading,
                        onCheckedChange = { updateEbookChecked = it },
                    )
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
                    DialogDismissButton(text = "Cancel", onClick = { showDialog.value = false })
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }
}

/** One box of the update dialog. The row is the control, so the label is part of the target. */
@Composable
private fun CacheOption(
    text: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(horizontal = 4.dp),
    ) {
        Checkbox(enabled = enabled, checked = checked, onCheckedChange = null)
        Text(
            text = text,
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
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
        ShioriAlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            icon = icon,
            title = title,
            text = if (content.isNotEmpty()) {
                {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            } else null,
            confirmButton = {
                DialogConfirmButton(
                    text = "Share",
                    onClick = {
                        showDialog.value = false
                        onClickOption?.invoke(2)
                    },
                )
            },
            dismissButton = {
                DialogDismissButton(text = "Cancel", onClick = { showDialog.value = false })
            },
            properties = properties
        )
    }
}
