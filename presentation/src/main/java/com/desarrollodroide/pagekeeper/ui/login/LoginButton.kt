package com.desarrollodroide.pagekeeper.ui.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier


@Composable
fun LoginButton(
    user: MutableState<String>,
    userErrorState: MutableState<Boolean>,
    password: MutableState<String>,
    passwordErrorState: MutableState<Boolean>,
    onClickLoginButton: () -> Unit,
    serverErrorState: MutableState<Boolean>
) {
    Button(
        onClick = {
            if (user.value.isEmpty()) {
                userErrorState.value = true
            }
            if (password.value.isEmpty()) {
                passwordErrorState.value = true
            }
            if (user.value.isNotEmpty() && password.value.isNotEmpty() && !serverErrorState.value) {
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