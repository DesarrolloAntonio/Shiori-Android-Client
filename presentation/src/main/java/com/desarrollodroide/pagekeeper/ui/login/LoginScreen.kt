package com.desarrollodroide.pagekeeper.ui.login

import android.content.res.Configuration
import android.util.Log
import android.webkit.URLUtil
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.pagekeeper.R
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.model.User
import androidx.compose.runtime.getValue

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onRegister: () -> Unit,
    onSuccess: (User) -> Unit,
    ) {
    val coroutineScope = rememberCoroutineScope()
    val loginUiState: UiState<User> by loginViewModel.userUiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val context = LocalContext.current
        LoginContent(
            loginUiState = loginUiState,
            checked = loginViewModel.rememberSession,
            onRegister = onRegister,
            userErrorState = loginViewModel.userNameError,
            passwordErrorState = loginViewModel.passwordError,
            urlErrorState = loginViewModel.urlError,
            onClickLoginButton = {
                //coroutineScope.launch {
                    loginViewModel.sendLogin()
                //}
            },
            onClickRegisterButton = {},
            onCheckedRememberSessionChange = {
                loginViewModel.rememberSession.value = it
            },
            onRememberPassword = {

            },
            onSuccess = {
                loginViewModel.clearError()
                onSuccess.invoke(it)
                        },
            user = loginViewModel.userName,
            password = loginViewModel.password,
            serverUrl = loginViewModel.serverUrl,
            onClearError = {
                loginViewModel.clearError()
            },
        )
    }

//    ErrorDialog(
//        message = stringResource(R.string.wrongUserOrPassword),
//        showDialog = loginViewModel.loginError
//    )
//    ErrorDialog(
//        message = stringResource(R.string.networkError),
//        showDialog = loginViewModel.netWorkError
//    )
//    ProgressDialog(
//        showDialog = loginViewModel.loading
//    )
}
//
//@Composable
//private fun ProgressDialog(
//    showDialog: MutableState<Boolean>
//) {
//    if (showDialog.value) {
//        InfiniteProgressDialog {}
//    }
//}

@Composable
fun LoginContent(
    user: MutableState<TextFieldValue>,
    password: MutableState<TextFieldValue>,
    serverUrl: MutableState<TextFieldValue>,
    checked: MutableState<Boolean>,
    urlErrorState: MutableState<Boolean>,
    userErrorState: MutableState<Boolean>,
    passwordErrorState: MutableState<Boolean>,
    onRegister: () -> Unit,
    onSuccess: (User) -> Unit,
    onRememberPassword: () -> Unit,
    onClickLoginButton: () -> Unit,
    onClickRegisterButton: () -> Unit,
    onClearError: () -> Unit ,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
    loginUiState: UiState<User>,
) {
    if (loginUiState.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
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
            serverUrl,
            urlErrorState,
            user,
            userErrorState,
            password,
            passwordErrorState,
            onClickLoginButton,
            checked,
            onCheckedRememberSessionChange,
            onClickRegisterButton
        )
    } else if (loginUiState.data != null){
        LaunchedEffect(Unit){
            onSuccess.invoke(loginUiState.data)
        }
    }
//    when(loginUiState){
//        is LoginUiState.Error -> {
//            ConfirmDialog(
//                icon = Icons.Default.Error,
//                title = "Error",
//                content = loginUiState.error,
//                openDialog = remember { mutableStateOf(true) },
//                onConfirm = {
//                    onClearError.invoke()
//                }
//            )
//            Log.v("loginUiState", "Error")
//        }
//        is LoginUiState.Loading -> {
//            InfiniteProgressDialog(onDismissRequest = {})
//            Log.v("loginUiState", "Loading")
//        }
//        is LoginUiState.Success -> {
//            if (loginUiState.userSession == null || !loginUiState.userSession.hasSession() ){
//                ContentViews(
//                    serverUrl,
//                    urlErrorState,
//                    user,
//                    userErrorState,
//                    password,
//                    passwordErrorState,
//                    onClickLoginButton,
//                    checked,
//                    onCheckedRememberSessionChange,
//                    onClickRegisterButton
//                )
//            } else {
//                LaunchedEffect(Unit){
//                    onSuccess.invoke(loginUiState.userSession)
//                }
//            }
//        }
//        is LoginUiState.Idle -> { }

   // }

}

@Composable
private fun ContentViews(
    serverUrl: MutableState<TextFieldValue>,
    urlErrorState: MutableState<Boolean>,
    user: MutableState<TextFieldValue>,
    userErrorState: MutableState<Boolean>,
    password: MutableState<TextFieldValue>,
    passwordErrorState: MutableState<Boolean>,
    onClickLoginButton: () -> Unit,
    checked: MutableState<Boolean>,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
    onClickRegisterButton: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.curved_wave_top),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.curved_wave_bottom),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(150.dp)
                .align(Alignment.BottomCenter)
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Bottom,
        ) {
            ServerUrlTextField(serverUrl, urlErrorState)
            Spacer(modifier = Modifier.height(10.dp))
            UserTextField(user, userErrorState)
            Spacer(modifier = Modifier.height(10.dp))
            PasswordTextField(password, passwordErrorState)
            Spacer(Modifier.size(14.dp))
            LoginButton(user, userErrorState, password, passwordErrorState, onClickLoginButton)
            RememberSessionSection(
                checked = checked,
                onCheckedChange = onCheckedRememberSessionChange
            )
//            Spacer(Modifier.size(10.dp))
//            Divider(modifier = Modifier.fillMaxWidth())
//            Spacer(Modifier.size(10.dp))
//            RegisterButton(onClickRegisterButton)
        }
    }
}

@Composable
private fun RegisterButton(onClickRegisterButton: () -> Unit) {
    Button(
        onClick = onClickRegisterButton,
        modifier = Modifier.fillMaxWidth(),
        content = {
            Text("Register")
        },
    )
}

@Composable
private fun LoginButton(
    user: MutableState<TextFieldValue>,
    userErrorState: MutableState<Boolean>,
    password: MutableState<TextFieldValue>,
    passwordErrorState: MutableState<Boolean>,
    onClickLoginButton: () -> Unit
) {
    Button(
        onClick = {
            if (user.value.text.isEmpty()) {
                userErrorState.value = true
            }
            if (password.value.text.isEmpty()) {
                passwordErrorState.value = true
            }
            if (user.value.text.isNotEmpty() && password.value.text.isNotEmpty()) {
                passwordErrorState.value = false
                userErrorState.value = false
                onClickLoginButton.invoke()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        content = {
            Text("Login")
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PasswordTextField(
    password: MutableState<TextFieldValue>,
    passwordErrorState: MutableState<Boolean>
) {
    Column() {
        val passwordVisibility = remember { mutableStateOf(true) }
        OutlinedTextField(
            value = password.value,
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Lock, contentDescription = null)
            },
            onValueChange = {
                if (passwordErrorState.value) {
                    passwordErrorState.value = false
                }
                password.value = it
            },
            isError = passwordErrorState.value,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Password")
            },
            trailingIcon = {
                IconButton(onClick = {
                    passwordVisibility.value = !passwordVisibility.value
                }) {
                    Icon(
                        imageVector = if (passwordVisibility.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "visibility",
                        tint = Color.Gray
                    )
                }
            },
            visualTransformation = if (passwordVisibility.value) PasswordVisualTransformation() else VisualTransformation.None
        )
        if (passwordErrorState.value) {
            Text(
                text = "Required",
                color = Color.Red,
                modifier = Modifier.Companion.align(Alignment.End)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun UserTextField(
    user: MutableState<TextFieldValue>,
    userErrorState: MutableState<Boolean>
) {
    Column {
    OutlinedTextField(
        value = user.value,
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Person, contentDescription = null)
        },
        onValueChange = {
            if (userErrorState.value) {
                userErrorState.value = false
            }
            user.value = it
        },
        isError = userErrorState.value,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(text = "UserName")
        },
    )
    if (userErrorState.value) {
        Text(
            modifier = Modifier.Companion.align(Alignment.End),
            color = Color.Red,
            text = "Invalid username"
        )
    }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ServerUrlTextField(
    serverUrl: MutableState<TextFieldValue>,
    serverErrorState: MutableState<Boolean>
) {
    Column() {
        OutlinedTextField(
            value = serverUrl.value,
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Link, contentDescription = null)
            },
            onValueChange = {
                serverErrorState.value = !URLUtil.isValidUrl(it.text)
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
                modifier = Modifier.Companion.align(Alignment.End),
                color = Color.Red,
                text = "Invalid url"
            )
        }
    }

}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ShioriTheme() {
        LoginContent(
            user = remember { mutableStateOf(TextFieldValue("User")) },
            password = remember { mutableStateOf(TextFieldValue("Pass")) },
            serverUrl = remember { mutableStateOf(TextFieldValue("ServerUrl")) },
            checked = remember { mutableStateOf(true) },
            urlErrorState = remember { mutableStateOf(true) },
            userErrorState = remember { mutableStateOf(true) },
            passwordErrorState = remember { mutableStateOf(true) },
            onRegister = {},
            onSuccess = {},
            onRememberPassword = {},
            onClickLoginButton = {},
            onClickRegisterButton = {},
            onCheckedRememberSessionChange = {},
            onClearError = {},
            loginUiState = UiState(false)
        )
    }
}