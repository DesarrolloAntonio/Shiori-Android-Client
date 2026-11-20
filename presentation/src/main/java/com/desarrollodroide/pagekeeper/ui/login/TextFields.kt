package com.desarrollodroide.pagekeeper.ui.login

import android.webkit.URLUtil
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        else -> null
    }

    LoginTextField(
        value = serverUrl.value,
        onValueChange = {
            serverErrorState.value = !URLUtil.isValidUrl(it)
            serverUrl.value = it
            if (serverUrlAvailable || serverAvailabilityUiState.error != null) {
                resetServerAvailabilityState()
            }
        },
        modifier = modifier.onFocusChanged { focusState ->
            if (isFocused && !focusState.isFocused && URLUtil.isValidUrl(serverUrl.value)) {
                onClick()
            }
            isFocused = focusState.isFocused
        },
        label = "Server",
        placeholder = "https://shiori.example.com",
        leadingIcon = Icons.Outlined.Dns,
        trailingIcon = when {
            isTestingServer -> {
                { LoadingIndicator(modifier = Modifier.size(24.dp)) }
            }
            serverUrlAvailable -> {
                {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Server reachable",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            else -> null
        },
        isError = serverErrorState.value || !availabilityError.isNullOrEmpty(),
        supportingText = supporting,
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
    LoginTextField(
        value = user.value,
        onValueChange = {
            if (userErrorState.value) userErrorState.value = false
            user.value = it
        },
        modifier = modifier.semantics { contentType = ContentType.Username },
        label = "Username",
        leadingIcon = Icons.Outlined.Person,
        isError = userErrorState.value,
        supportingText = if (userErrorState.value) {
            // The only check is that it is not empty (LoginButton), so that is what it says.
            { Text("Required") }
        } else null,
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

    LoginTextField(
        value = password.value,
        onValueChange = {
            if (passwordErrorState.value) passwordErrorState.value = false
            password.value = it
        },
        modifier = modifier.semantics { contentType = ContentType.Password },
        label = "Password",
        leadingIcon = Icons.Outlined.Lock,
        trailingIcon = {
            IconButton(onClick = { passwordHidden = !passwordHidden }) {
                Icon(
                    imageVector = if (passwordHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordHidden) "Show password" else "Hide password",
                )
            }
        },
        isError = passwordErrorState.value,
        supportingText = if (passwordErrorState.value) {
            { Text("Required") }
        } else null,
        visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
    )
}

/**
 * A filled field with no underline: a rounded tonal block, the way the rest of the redesign draws
 * inputs. Focus tints it towards the brand colour and an error towards the error colour, which is
 * what the underline used to say.
 *
 * supportingText is the M3 slot for validation messages: it inherits the error colour from isError
 * instead of a hardcoded red that ignores the theme.
 */
@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isError: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val scheme = MaterialTheme.colorScheme
    val resting = scheme.surfaceContainerHigh
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = { Icon(imageVector = leadingIcon, contentDescription = null) },
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = resting,
            focusedContainerColor = lerp(resting, scheme.primaryContainer, 0.55f),
            errorContainerColor = lerp(resting, scheme.errorContainer, 0.6f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}
