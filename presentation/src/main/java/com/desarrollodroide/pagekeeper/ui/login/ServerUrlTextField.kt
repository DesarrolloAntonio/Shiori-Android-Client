package com.desarrollodroide.pagekeeper.ui.login

import android.webkit.URLUtil
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.LivenessResponse
import com.desarrollodroide.pagekeeper.ui.components.UiState

@Composable
fun ServerUrlTextField(
    modifier: Modifier,
    serverAvailabilityUiState: UiState<LivenessResponse>,
    serverUrl: MutableState<String>,
    serverErrorState: MutableState<Boolean>,
    serverVersion: String,
    resetServerAvailabilityState: () -> Unit,
    onClick: () -> Unit,
    isTestingServer: Boolean
) {
    val serverUrlAvailable = serverAvailabilityUiState.data?.ok == true
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = serverUrl.value,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (isTestingServer) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else if (serverUrlAvailable) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "visibility",
                    )
                }
            },
            onValueChange = {
                serverErrorState.value = !URLUtil.isValidUrl(it)
                serverUrl.value = it
                if (serverUrlAvailable || serverAvailabilityUiState.error != null) {
                    resetServerAvailabilityState()
                }
            },
            isError = serverErrorState.value,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (isFocused && !focusState.isFocused && URLUtil.isValidUrl(serverUrl.value)) {
                        onClick()
                    }
                    isFocused = focusState.isFocused
                },
            label = {
                Text(text = "Server url")
            },
            singleLine = true,
            maxLines = 1
        )

        AnimatedVisibility(visible = serverErrorState.value) {
            Text(
                modifier = Modifier.align(Alignment.End),
                color = Color.Red,
                text = "Invalid url"
            )
        }

        AnimatedVisibility(visible = serverUrlAvailable && serverVersion.isNotEmpty()) {
            Text(
                modifier = Modifier.align(Alignment.Start),
                text = "Server v$serverVersion"
            )
        }

        AnimatedVisibility(visible = serverAvailabilityUiState.error != null) {
            Text(
                modifier = Modifier.align(Alignment.End),
                color = Color.Red,
                text = serverAvailabilityUiState.error ?: ""
            )
        }
    }
}