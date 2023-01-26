package com.shiori.androidclient.ui.login

import android.content.res.Configuration
import android.webkit.URLUtil
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shiori.androidclient.R
import com.shiori.androidclient.ui.components.ConfirmDialog
import com.shiori.androidclient.ui.components.SimpleDialog
import com.shiori.androidclient.ui.theme.ShioriTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onRegister: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val context = LocalContext.current
        LoginContent(
            checked = loginViewModel.rememberSession,
            onRegister = onRegister,
            onClickLoginButton = {

            },
            onClickRegisterButton = {
                loginViewModel.loginError.value = true
            },
            onCheckedRememberSessionChange = {
                loginViewModel.rememberSession.value = it
            },
            onRememberPassword = {

            },
            user = loginViewModel.userName,
            password = loginViewModel.password,
            serverUrl = loginViewModel.serverUrl
        )
    }
    ConfirmDialog(
        title = "Error",
        content = "Network error",
        openDialog = loginViewModel.loginError
    )
//    ErrorDialog(
//        message = stringResource(R.string.wrongUserOrPassword),
//        showDialog = loginViewModel.loginError
//    )
//    ErrorDialog(
//        message = stringResource(R.string.networkError),
//        showDialog = loginViewModel.netWorkError
//    )
    ProgressDialog(
        showDialog = loginViewModel.loading
    )
}

@Composable
fun ProgressDialog(
    showDialog: MutableState<Boolean>
) {
    if (showDialog.value) {
        //InfiniteProgressDialog(onDismissRequest = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    user: MutableState<TextFieldValue>,
    password: MutableState<TextFieldValue>,
    serverUrl: MutableState<TextFieldValue>,
    checked: MutableState<Boolean>,
    onRegister: () -> Unit,
    onRememberPassword: () -> Unit,
    onClickLoginButton: () -> Unit,
    onClickRegisterButton: () -> Unit,
    onCheckedRememberSessionChange: ((Boolean) -> Unit),
) {
    val serverErrorState = remember { mutableStateOf(false) }
    val userErrorState = remember { mutableStateOf(false) }
    val passwordErrorState = remember { mutableStateOf(false) }
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
            ServerUrlTextField(serverUrl, serverErrorState)
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
            Spacer(Modifier.size(10.dp))
            Divider(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.size(10.dp))
            RegisterButton(onClickRegisterButton)
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
            onRegister = {},
            onCheckedRememberSessionChange = {},
            onRememberPassword = {},
            onClickLoginButton = {},
            onClickRegisterButton = {}
        )
    }
}