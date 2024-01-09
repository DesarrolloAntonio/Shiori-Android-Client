package com.desarrollodroide.pagekeeper.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun UserTextField(
    user: MutableState<String>,
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
                modifier = Modifier.align(Alignment.End),
                color = Color.Red,
                text = "Invalid username"
            )
        }
    }
}