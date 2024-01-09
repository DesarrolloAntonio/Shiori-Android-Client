package com.desarrollodroide.pagekeeper.ui.login

import android.webkit.URLUtil
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ServerUrlTextField(
    serverUrl: MutableState<String>,
    serverErrorState: MutableState<Boolean>
) {
    Column() {
        OutlinedTextField(
            value = serverUrl.value,
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Link, contentDescription = null)
            },
            onValueChange = {
                serverErrorState.value = !URLUtil.isValidUrl(it)
                serverUrl.value = it
            },
            isError = serverErrorState.value,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Server url")
            },
        )
        if (serverErrorState.value) {
            Text(
                modifier = Modifier.align(Alignment.End),
                color = Color.Red,
                text = "Invalid url"
            )
        }
    }
}