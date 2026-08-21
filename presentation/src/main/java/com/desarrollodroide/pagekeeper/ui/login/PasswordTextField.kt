package com.desarrollodroide.pagekeeper.ui.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

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
        modifier = modifier.fillMaxWidth(),
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
