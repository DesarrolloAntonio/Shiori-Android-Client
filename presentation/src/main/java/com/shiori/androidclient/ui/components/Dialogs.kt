package com.shiori.androidclient.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector


// https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#alertdialog

@Composable
fun SimpleDialog(
    title: String = "",
    content: String = "",
    icon: ImageVector,
    confirmButtonText: String = "",
    dismissButtonText: String = "",
    openDialog: MutableState<Boolean>,
) {
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                openDialog.value = false
            },
            icon = { Icon(imageVector = icon, contentDescription = null) },
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
                        }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String = "",
    content: String = "",
    confirmButton: String = "Confirm",
    dismissButton: String = "",
    openDialog: MutableState<Boolean>,
    ){
    SimpleDialog(
        title = title,
        content = content,
        icon = Icons.Filled.Info,
        confirmButtonText = confirmButton,
        openDialog = openDialog
    )
}



