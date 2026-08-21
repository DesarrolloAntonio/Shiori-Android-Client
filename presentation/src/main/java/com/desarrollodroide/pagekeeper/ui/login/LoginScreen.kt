package com.desarrollodroide.pagekeeper.ui.login

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.pagekeeper.R
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.FormMaxWidth
import com.desarrollodroide.model.User
import androidx.compose.runtime.getValue
import com.desarrollodroide.data.helpers.SHIORI_GITHUB_URL
import com.desarrollodroide.model.LivenessResponse
import com.desarrollodroide.pagekeeper.ui.settings.LinkableText

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onSuccess: (User) -> Unit,
) {
    val loginUiState: UiState<User> by loginViewModel.userUiState.collectAsStateWithLifecycle()
    val livenessUiState: UiState<LivenessResponse> by loginViewModel.livenessUiState.collectAsStateWithLifecycle()
    val serverAvailabilityUiState: UiState<LivenessResponse> by loginViewModel.serverAvailabilityUiState.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LoginContent(
            loginUiState = loginUiState,
            checked = loginViewModel.rememberSession,
            userErrorState = loginViewModel.userNameError,
            passwordErrorState = loginViewModel.passwordError,
            urlErrorState = loginViewModel.urlError,
            onClickLoginButton = {
                loginViewModel.checkSystemLiveness()
            },
            onCheckedRememberSessionChange = {
                loginViewModel.rememberSession.value = it
            },
            onSuccess = {
                loginViewModel.clearState()
                onSuccess.invoke(it)
            },
            user = loginViewModel.userName,
            password = loginViewModel.password,
            serverUrl = loginViewModel.serverUrl,
            onClearError = {
                loginViewModel.clearState()
            },
            livenessUiState = livenessUiState,
            serverAvailabilityUiState = serverAvailabilityUiState,
            onClickTestButton = {
                loginViewModel.checkServerAvailability()
            },
            resetServerAvailabilityState = {
                loginViewModel.resetServerAvailabilityUiState()
            }
        )
    }
}

@Composable
fun LoginContent(
    user: MutableState<String>,
    password: MutableState<String>,
    serverUrl: MutableState<String>,
    checked: MutableState<Boolean>,
    urlErrorState: MutableState<Boolean>,
    userErrorState: MutableState<Boolean>,
    passwordErrorState: MutableState<Boolean>,
    onSuccess: (User) -> Unit,
    onClickLoginButton: () -> Unit,
    onClickTestButton: () -> Unit,
    onClearError: () -> Unit,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
    loginUiState: UiState<User>,
    livenessUiState: UiState<LivenessResponse>,
    serverAvailabilityUiState: UiState<LivenessResponse>,
    resetServerAvailabilityState: () -> Unit
) {
    if (loginUiState.isLoading || livenessUiState.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
    }
    if (!livenessUiState.error.isNullOrEmpty()) {
        ConfirmDialog(
            icon = Icons.Default.Error,
            title = "Error",
            content = livenessUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                onClearError.invoke()
            }
        )
        Log.v("loginUiState", "Error")
    }
    if (!loginUiState.error.isNullOrEmpty()) {
        ConfirmDialog(
            icon = Icons.Default.Error,
            title = "Error",
            content = loginUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                onClearError.invoke()
            }
        )
        Log.v("loginUiState", "Error")
    } else if (loginUiState.data == null && !loginUiState.idle) {
        ContentViews(
            serverUrl = serverUrl,
            urlErrorState = urlErrorState,
            user = user,
            userErrorState = userErrorState,
            password = password,
            passwordErrorState = passwordErrorState,
            onClickLoginButton = onClickLoginButton,
            checked = checked,
            onCheckedRememberSessionChange = onCheckedRememberSessionChange,
            isTestingServer = serverAvailabilityUiState.isLoading,
            onClickTestButton = onClickTestButton,
            serverAvailabilityUiState = serverAvailabilityUiState,
            serverVersion = serverAvailabilityUiState.data?.message?.version ?: "",
            resetServerAvailabilityState = resetServerAvailabilityState
        )
    } else if (loginUiState.data != null) {
        LaunchedEffect(Unit) {
            onSuccess.invoke(loginUiState.data)
        }
    }
}

@Composable
private fun ContentViews(
    serverUrl: MutableState<String>,
    urlErrorState: MutableState<Boolean>,
    user: MutableState<String>,
    userErrorState: MutableState<Boolean>,
    password: MutableState<String>,
    passwordErrorState: MutableState<Boolean>,
    isTestingServer: Boolean,
    onClickLoginButton: () -> Unit,
    onClickTestButton: () -> Unit,
    checked: MutableState<Boolean>,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
    serverAvailabilityUiState: UiState<LivenessResponse>,
    serverVersion: String,
    resetServerAvailabilityState: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Image(
            painter = painterResource(id = R.drawable.curved_wave_bottom),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            // FillBounds, not Crop. The drawable is 1440x560, about 2.6:1. On a phone the band
            // below happens to be almost exactly that ratio so nothing is lost, but on a 1280dp
            // tablet the same band is 8.5:1, and Crop scales the wave to some 500dp tall and shows
            // the middle slice of it: solid fill with a severed piece of curve. Mapping the whole
            // drawable onto the band flattens the crest on wide screens, which is what a
            // decorative wave is supposed to do, and leaves phones as they were.
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .align(Alignment.BottomCenter)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                // The form used to sit in a fixed-height Box, so on short screens (or with the
                // keyboard up) the login button went off-screen with no way to reach it.
                .verticalScroll(rememberScrollState())
                .imePadding()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(110.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Sign in to your Shiori server",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                // widthIn before fillMaxWidth. The other way round, fillMaxWidth pins the width
                // to the parent's max and there is nothing left for widthIn to clamp.
                modifier = Modifier
                    .widthIn(max = FormMaxWidth)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ServerUrlTextField(
                        serverUrl = serverUrl,
                        serverErrorState = urlErrorState,
                        serverAvailabilityUiState = serverAvailabilityUiState,
                        serverVersion = serverVersion,
                        resetServerAvailabilityState = resetServerAvailabilityState,
                        onClick = onClickTestButton,
                        isTestingServer = isTestingServer
                    )
                    UserTextField(
                        user = user,
                        userErrorState = userErrorState
                    )
                    PasswordTextField(
                        password = password,
                        passwordErrorState = passwordErrorState
                    )
                    RememberSessionSection(
                        checked = checked,
                        onCheckedChange = onCheckedRememberSessionChange
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LoginButton(
                        user = user,
                        userErrorState = userErrorState,
                        password = password,
                        passwordErrorState = passwordErrorState,
                        onClickLoginButton = onClickLoginButton,
                        serverErrorState = urlErrorState
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinkableText(
                text = "Server Setup Guide",
                url = SHIORI_GITHUB_URL
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ShioriTheme(
        dynamicColor = false
    ) {
        LoginContent(
            user = remember { mutableStateOf("User") },
            password = remember { mutableStateOf("Pass") },
            serverUrl = remember { mutableStateOf("ServerUrl") },
            checked = remember { mutableStateOf(true) },
            urlErrorState = remember { mutableStateOf(true) },
            userErrorState = remember { mutableStateOf(true) },
            passwordErrorState = remember { mutableStateOf(true) },
            onSuccess = {},
            onClickLoginButton = {},
            onCheckedRememberSessionChange = {},
            onClearError = {},
            loginUiState = UiState(data = null, idle = false),
            livenessUiState = UiState(false),
            serverAvailabilityUiState = UiState(data = null, idle = false),
            onClickTestButton = {},
            resetServerAvailabilityState = {}
        )
    }
}