package com.desarrollodroide.pagekeeper.ui.login

import android.webkit.URLUtil
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.LivenessResponse
import com.desarrollodroide.pagekeeper.ui.components.UiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ServerUrlTextField(
    serverAvailabilityUiState: UiState<LivenessResponse>,
    serverUrl: MutableState<String>,
    serverErrorState: MutableState<Boolean>,
    serverVersion: String,
    resetServerAvailabilityState: () -> Unit,
    onClick: () -> Unit,
    isTestingServer: Boolean,
    modifier: Modifier = Modifier,
) {
    val serverUrlAvailable = serverAvailabilityUiState.data?.ok == true
    var isFocused by remember { mutableStateOf(false) }
    val availabilityError = serverAvailabilityUiState.error

    // One supporting line covers all three states (invalid url / reachability error / server
    // version), so the field keeps a stable height instead of growing an extra row per state.
    val supporting: (@Composable () -> Unit)? = when {
        serverErrorState.value -> {
            { Text("Invalid url") }
        }
        !availabilityError.isNullOrEmpty() -> {
            { Text(availabilityError) }
        }
        serverUrlAvailable && serverVersion.isNotEmpty() -> {
            {
                Text(
                    text = "Server v$serverVersion",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        else -> null
    }

    OutlinedTextField(
        value = serverUrl.value,
        onValueChange = {
            serverErrorState.value = !URLUtil.isValidUrl(it)
            serverUrl.value = it
            if (serverUrlAvailable || serverAvailabilityUiState.error != null) {
                resetServerAvailabilityState()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (isFocused && !focusState.isFocused && URLUtil.isValidUrl(serverUrl.value)) {
                    onClick()
                }
                isFocused = focusState.isFocused
            },
        shape = MaterialTheme.shapes.medium,
        leadingIcon = { Icon(imageVector = Icons.Filled.Link, contentDescription = null) },
        trailingIcon = {
            when {
                isTestingServer -> LoadingIndicator(modifier = Modifier.size(24.dp))
                serverUrlAvailable -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Server reachable",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        label = { Text(text = "Server url") },
        isError = serverErrorState.value || !availabilityError.isNullOrEmpty(),
        supportingText = supporting,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
        ),
    )
}
