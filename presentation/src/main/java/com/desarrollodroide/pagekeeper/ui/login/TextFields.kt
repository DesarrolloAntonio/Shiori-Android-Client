package com.desarrollodroide.pagekeeper.ui.login

import android.webkit.URLUtil
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.LivenessResponse
import com.desarrollodroide.pagekeeper.ui.components.UiState

/*
 * The three fields of the login form. One file rather than three, because none of them is used
 * anywhere else and each was too small to be worth finding on its own.
 */

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

@Composable
fun UserTextField(
    user: MutableState<String>,
    userErrorState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = user.value,
        onValueChange = {
            if (userErrorState.value) userErrorState.value = false
            user.value = it
        },
        modifier = modifier
            .semantics { contentType = ContentType.Username }
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null) },
        label = { Text(text = "Username") },
        isError = userErrorState.value,
        // supportingText is the M3 slot for validation messages: it reserves its own line so the
        // form doesn't jump when an error appears, and it inherits the error colour from isError
        // instead of a hardcoded Color.Red that ignores the theme.
        supportingText = if (userErrorState.value) {
            { Text("Invalid username") }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Next,
        ),
    )
}

@Composable
fun PasswordTextField(
    password: MutableState<String>,
    passwordErrorState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    var passwordHidden by remember { mutableStateOf(true) }

    OutlinedTextField(
        value = password.value,
        onValueChange = {
            if (passwordErrorState.value) passwordErrorState.value = false
            password.value = it
        },
        modifier = modifier
            .semantics { contentType = ContentType.Password }
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { passwordHidden = !passwordHidden }) {
                Icon(
                    imageVector = if (passwordHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordHidden) "Show password" else "Hide password",
                )
            }
        },
        label = { Text(text = "Password") },
        isError = passwordErrorState.value,
        supportingText = if (passwordErrorState.value) {
            { Text("Required") }
        } else null,
        visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
    )
}
