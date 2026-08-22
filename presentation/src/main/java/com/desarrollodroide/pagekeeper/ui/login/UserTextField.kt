package com.desarrollodroide.pagekeeper.ui.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions

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
